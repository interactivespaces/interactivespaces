/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.controller.client.node;

import com.google.common.collect.Maps;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStateTransition;
import interactivespaces.activity.ActivityStateTransition.TransitionResult;
import interactivespaces.activity.ActivityStateTransitioner;
import interactivespaces.activity.ActivityStateTransitionerCollection;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.component.CoreExistingActivityComponentFactory;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.configuration.LiveActivityConfigurationManager;
import interactivespaces.controller.activity.installation.ActivityInstallationListener;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.activity.installation.ActivityInstallationManager.RemoveActivityResult;
import interactivespaces.controller.client.node.ros.SpaceControllerActivityWatcher;
import interactivespaces.controller.client.node.ros.SpaceControllerActivityWatcherListener;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.logging.ActivityLogFactory;
import interactivespaces.controller.logging.SimpleAlertStatusManager;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.web.client.WebSocketClientService;
import interactivespaces.service.web.client.internal.netty.NettyWebSocketClientService;
import interactivespaces.service.web.server.WebServerService;
import interactivespaces.service.web.server.internal.netty.NettyWebServerService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.io.Files;
import interactivespaces.util.uuid.JavaUuidGenerator;
import interactivespaces.util.uuid.UuidGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of {@link SpaceController} which gives basic
 * implementation. Does not supply communication for the remote master.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceController implements SpaceController,
    SpaceControllerActivityWatcherListener, SpaceControllerControl {

  /**
   * The default number of milliseconds the activity activityWatcher thread
   * delays between scans.
   */
  private static final int WATCHER_DELAY_DEFAULT = 1000;

  /**
   * The default number of milliseconds the controllerHeartbeat thread delays
   * between beats.
   */
  public static final int HEARTBEAT_DELAY_DEFAULT = 10000;

  /**
   * A live activity status for installed live activities that currently aren't
   * running.
   */
  private static final ActivityStatus LIVE_ACTIVITY_READY_STATUS = new ActivityStatus(
      ActivityState.READY, null);

  /**
   * A live activity status for installed live activities that currently are
   * running.
   */
  private static final ActivityStatus LIVE_ACTIVITY_RUNNING_STATUS = new ActivityStatus(
      ActivityState.RUNNING, null);

  /**
   * A live activity status for installed live activities that currently are
   * activated.
   */
  private static final ActivityStatus LIVE_ACTIVITY_ACTIVE_STATUS = new ActivityStatus(
      ActivityState.ACTIVE, null);

  /**
   * The heartbeatLoop for this controller.
   */
  private SpaceControllerHeartbeat controllerHeartbeat;

  /**
   * Control for the controllerHeartbeat.
   */
  private ScheduledFuture<?> controllerHeartbeatControl;

  /**
   * Number of milliseconds the heartbeatLoop waits before each beat.
   */
  private long heartbeatDelay = HEARTBEAT_DELAY_DEFAULT;

  /**
   * All activities in this controller, indexed by UUID.
   */
  private Map<String, ActiveControllerActivity> activities = Maps.newHashMap();

  /**
   * Watches activities for this controller.
   */
  private SpaceControllerActivityWatcher activityWatcher;

  /**
   * Control for the activity watcher.
   */
  private ScheduledFuture<?> activityWatcherControl;

  /**
   * Number of milliseconds the activityWatcher waits before scanning for
   * activity state.
   */
  private long activityWatcherDelay = WATCHER_DELAY_DEFAULT;

  /**
   * For important alerts worthy of paging, etc.
   */
  private SimpleAlertStatusManager alertStatusManager;

  /**
   * Receives activities deployed to the controller.
   */
  private ActivityInstallationManager activityInstallationManager;

  /**
   * A loader for container activities.
   */
  private ActiveControllerActivityFactory activeControllerActivityFactory;

  /**
   * Local repository of controller information.
   */
  private LocalSpaceControllerRepository controllerRepository;

  /**
   * A factory for native app runners.
   */
  private NativeActivityRunnerFactory nativeActivityRunnerFactory;

  /**
   * The configuration manager for activities.
   */
  private LiveActivityConfigurationManager configurationManager;

  /**
   * The component factory to be used by this controller.
   */
  private ActivityComponentFactory activityComponentFactory;

  /**
   * Log factory for activities.
   */
  private ActivityLogFactory activityLogFactory;

  /**
   * The Interactive Spaces environment being run under.
   */
  protected InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The Interactive Spaces system controller.
   */
  protected InteractiveSpacesSystemControl spaceSystemControl;

  /**
   * Information about the controller
   */
  private SimpleSpaceController controllerInfo = new SimpleSpaceController();

  /**
   * The storage manager for activities.
   */
  private ActivityStorageManager activityStorageManager;

  /**
   * All activity state transitioners.
   */
  private ActivityStateTransitionerCollection activityStateTransitioners;

  /**
   * The controller communicator for remote control.
   */
  private SpaceControllerCommunicator controllerCommunicator;

  /**
   * A listener for installation events.
   */
  private ActivityInstallationListener activityInstallationListener =
      new ActivityInstallationListener() {
        @Override
        public void onActivityInstall(String uuid) {
          handleActivityInstall(uuid);
        }

        @Override
        public void onActivityRemove(String uuid, RemoveActivityResult result) {
          handleActivityRemove(uuid, result);
        }
      };

  /**
   * A listener for activity events.
   */
  private ActivityListener activityListener = new ActivityListener() {
    @Override
    public void onActivityStatusChange(Activity activity, ActivityStatus oldStatus,
        ActivityStatus newStatus) {
      handleActivityListenerOnActivityStatusChange(activity, oldStatus, newStatus);
    }
  };

  /**
   * The sequential event queue to be used for controller events.
   */
  private SequentialEventQueue eventQueue;

  /**
   * {@code true} if the controller was started up.
   */
  private volatile boolean startedUp = false;

  /**
   * The persister for information about the controller.
   */
  private SpaceControllerInfoPersister controllerInfoPersister;

  /**
   * The IS service for web servers.
   */
  private WebServerService webServerService;

  /**
   * The IS service for web socket clients.
   */
  private WebSocketClientService webSocketClientService;

  /**
   * File control for the controller.
   *
   * <p>
   * This can be {@code null} if it wasn't requested.
   */
  private SpaceControllerFileControl fileControl;

  public StandardSpaceController(ActivityInstallationManager activityInstallationManager,
      LocalSpaceControllerRepository controllerRepository,
      ActiveControllerActivityFactory activeControllerActivityFactory,
      NativeActivityRunnerFactory nativeAppRunnerFactory,
      LiveActivityConfigurationManager configurationManager,
      ActivityStorageManager activityStorageManager, ActivityLogFactory activityLogFactory,
      SpaceControllerCommunicator controllerCommunicator,
      SpaceControllerInfoPersister controllerInfoPersister,
      InteractiveSpacesSystemControl spaceSystemControl,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.activityInstallationManager = activityInstallationManager;
    this.controllerRepository = controllerRepository;
    this.activeControllerActivityFactory = activeControllerActivityFactory;
    this.nativeActivityRunnerFactory = nativeAppRunnerFactory;
    this.configurationManager = configurationManager;
    this.activityStorageManager = activityStorageManager;
    this.activityLogFactory = activityLogFactory;

    this.controllerCommunicator = controllerCommunicator;
    controllerCommunicator.setControllerControl(this);

    this.controllerInfoPersister = controllerInfoPersister;

    this.spaceSystemControl = spaceSystemControl;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    spaceEnvironment.getLog().info("Controller starting up");

    obtainControllerInfo();
    confirmUuid();

    setEnvironmentValues();

    activityComponentFactory = new CoreExistingActivityComponentFactory();
    activityStateTransitioners = new ActivityStateTransitionerCollection();

    // TODO(keith): Set this container-wide.
    eventQueue = new SequentialEventQueue(getSpaceEnvironment(), getSpaceEnvironment().getLog());
    eventQueue.startup();

    // TODO(keith): Set this container-wide.
    alertStatusManager = new SimpleAlertStatusManager();
    alertStatusManager.setLog(spaceEnvironment.getLog());

    activityWatcher = new SpaceControllerActivityWatcher(spaceEnvironment);
    activityWatcher.addListener(this);
    activityWatcherControl =
        getSpaceEnvironment().getExecutorService().scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            activityWatcher.scan();
          }
        }, activityWatcherDelay, activityWatcherDelay, TimeUnit.MILLISECONDS);

    activityInstallationManager.addActivityInstallationListener(activityInstallationListener);

    controllerCommunicator.onStartup();

    controllerHeartbeat = controllerCommunicator.newSpaceControllerHeartbeat();
    controllerHeartbeatControl =
        getSpaceEnvironment().getExecutorService().scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            try {
              controllerHeartbeat.sendHeartbeat();
            } catch (Exception e) {
              spaceEnvironment.getLog().error(
                  "Exception while trying to send a Space Controller heartbeat", e);
            }
          }
        }, heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);

    startupControllerControl();
    startupCoreControllerServices();

    startupAutostartActivities();

    controllerCommunicator.notifyRemoteMasterServerAboutStartup(controllerInfo);

    startedUp = true;

    // Make sure we shutdown all activities properly.
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        shutdown();
      }
    });
  }

  /**
   * Set values in the space environment that the controller provides.
   */
  public void setEnvironmentValues() {
    spaceEnvironment.setValue(ENVIRONMENT_CONTROLLER_NATIVE_RUNNER, nativeActivityRunnerFactory);
  }

  /**
   * Got a status change on an activity from the activity.
   *
   * @param activity
   *          the activity whose status changed
   * @param oldStatus
   *          the old status
   * @param newStatus
   *          the new status
   */
  private void handleActivityListenerOnActivityStatusChange(final Activity activity,
      ActivityStatus oldStatus, final ActivityStatus newStatus) {

    // TODO(keith): Android hates garbage collection. This may need an
    // object pool.
    eventQueue.addEvent(new Runnable() {
      @Override
      public void run() {
        controllerCommunicator.publishActivityStatus(activity.getUuid(), newStatus);

        activityStateTransitioners.transition(activity.getUuid(), newStatus.getState());
      }
    });
  }

  /**
   * Make sure the controller had a UUID. If not, generate one.
   */
  private void confirmUuid() {
    String uuid = controllerInfo.getUuid();
    if (uuid == null || uuid.trim().isEmpty()) {
      UuidGenerator uuidGenerator = new JavaUuidGenerator();
      uuid = uuidGenerator.newUuid();
      controllerInfo.setUuid(uuid);

      spaceEnvironment.getLog().warn(
          String.format("No controller UUID found, generated UUID is %s", uuid));

      controllerInfoPersister.persist(controllerInfo, spaceEnvironment);
    }
  }

  /**
   * Save the controller information in the configurations.
   */

  /**
   * Get controller information from the configs.
   */
  private void obtainControllerInfo() {
    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();

    controllerInfo.setUuid(systemConfiguration.getPropertyString(CONFIGURATION_CONTROLLER_UUID));
    controllerInfo
        .setName(systemConfiguration.getPropertyString(CONFIGURATION_CONTROLLER_NAME, ""));
    controllerInfo.setDescription(systemConfiguration.getPropertyString(
        CONFIGURATION_CONTROLLER_DESCRIPTION, ""));
    controllerInfo.setHostId(systemConfiguration
        .getRequiredPropertyString(InteractiveSpacesEnvironment.CONFIGURATION_HOSTID));
  }

  @Override
  public void shutdown() {
    if (startedUp) {
      try {
        spaceEnvironment.getLog().info("Controller shutting down");

        activityStateTransitioners.clear();

        if (fileControl != null) {
          fileControl.shutdown();
          fileControl = null;
        }

        shutdownAllActivities();

        shutdownCoreControllerServices();

        controllerHeartbeatControl.cancel(true);
        controllerHeartbeatControl = null;

        activityWatcherControl.cancel(true);
        activityWatcherControl = null;
        activityWatcher = null;

        eventQueue.shutdown();
        eventQueue = null;

        controllerCommunicator.onShutdown();
      } finally {
        startedUp = false;
      }
    }
  }

  @Override
  public ActivityComponentFactory getActivityComponentFactory() {
    return activityComponentFactory;
  }

  /**
   * Prepare an instance to run.
   *
   * @param activity
   *          information about the activity whose instance is to be initialized
   *          (think of as the class description.
   * @param activityFilesystem
   *          the filesystem for the activity instance
   * @param instance
   *          the instance of the activity being started up
   * @param configuration
   *          the configuration for the instance
   * @param executionContext
   *          the context for executing the activity in
   */
  public void initializeInstance(InstalledLiveActivity activity,
      ActivityFilesystem activityFilesystem, Activity instance, Configuration configuration,
      ActivityExecutionContext executionContext) {
    String uuid = activity.getUuid();
    instance.setController(this);
    instance.setUuid(uuid);

    instance.setConfiguration(configuration);
    instance.setActivityFilesystem(activityFilesystem);
    instance.setSpaceEnvironment(spaceEnvironment);
    instance.setLog(activityLogFactory.createLogger(activity, configuration.getPropertyString(
        Activity.CONFIGURATION_PROPERTY_LOG_LEVEL, InteractiveSpacesEnvironment.LOG_LEVEL_ERROR)));
    instance.setExecutionContext(executionContext);
    instance.addActivityListener(getActivityListener());

    initializeConfiguration(configuration, activityFilesystem);

    specificInstanceInitialization(instance);
  }

  /**
   * Startup the activities that need to start up when the controller starts.
   */
  private void startupAutostartActivities() {
    for (InstalledLiveActivity activity : controllerRepository.getAllInstalledLiveActivities()) {
      switch (activity.getControllerStartupType()) {
        case STARTUP:
          startupActivity(activity.getUuid());
          break;
        case ACTIVATE:
          activateActivity(activity.getUuid());
          break;
      }
    }
  }

  /**
   * Initialize the configuration with any special values needed for running.
   *
   * @param configuration
   *          the configuration to be modified
   * @param activityFilesystem
   *          the activities file system
   */
  private void initializeConfiguration(Configuration configuration,
      ActivityFilesystem activityFilesystem) {
    configuration.setValue("activity.installdir", activityFilesystem.getInstallDirectory()
        .getAbsolutePath());
    configuration.setValue("activity.logdir", activityFilesystem.getLogDirectory()
        .getAbsolutePath());
    configuration.setValue("activity.datadir", activityFilesystem.getPermanentDataDirectory()
        .getAbsolutePath());
    configuration.setValue("activity.tmpdir", activityFilesystem.getTempDataDirectory()
        .getAbsolutePath());
    InteractiveSpacesFilesystem filesystem = spaceEnvironment.getFilesystem();
    configuration.setValue("system.datadir", filesystem.getDataDirectory().getAbsolutePath());
    configuration.setValue("system.tmpdir", filesystem.getTempDirectory().getAbsolutePath());
  }

  /**
   * Perform any additional instance initialization needed.
   *
   * @param instance
   *          The activity instance to initialize
   */
  public void specificInstanceInitialization(Activity instance) {
    // Default is nothing
  }

  @Override
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  @Override
  public void startupAllActivities() {
    for (ActiveControllerActivity app : getAllActiveActivities()) {
      attemptActivityStartup(app);
    }
  }

  @Override
  public void shutdownAllActivities() {
    spaceEnvironment.getLog().info("Shutting down all activities");

    for (ActiveControllerActivity app : getAllActiveActivities()) {
      attemptActivityShutdown(app);
    }
  }

  @Override
  public void startupActivity(String uuid) {
    spaceEnvironment.getLog().info(String.format("Starting up activity %s", uuid));

    try {
      ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
      if (activity != null) {
        ActivityStatus status = activity.getCachedActivityStatus();
        if (!status.getState().isRunning()) {
          activityWatcher.watchActivity(activity);
          attemptActivityStartup(activity);
        } else {
          // The activity is running so just report what it is doing
          publishActivityStatus(uuid, status);
        }
      } else {
        spaceEnvironment.getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      ActivityStatus status = new ActivityStatus(ActivityState.STARTUP_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void shutdownActivity(final String uuid) {
    spaceEnvironment.getLog().info(String.format("Shutting down activity %s", uuid));

    ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
    if (activity != null) {
      attemptActivityShutdown(activity);
    } else {
      // The activity hasn't been active. Make sure it really exists then
      // send that it is ready.
      InstalledLiveActivity ia = controllerRepository.getInstalledLiveActivityByUuid(uuid);
      if (ia != null) {
        publishActivityStatus(uuid, LIVE_ACTIVITY_READY_STATUS);
      } else {
        // TODO(keith): Tell master the controller doesn't exist.
        spaceEnvironment.getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    }
  }

  @Override
  public void statusActivity(String uuid) {
    spaceEnvironment.getLog().info(String.format("Getting status of activity %s", uuid));

    final ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
    if (activity != null) {
      final ActivityStatus activityStatus = activity.getActivityStatus();
      spaceEnvironment.getLog().info(
          String.format("Reporting activity status %s for %s", uuid, activityStatus));
      publishActivityStatus(activity.getUuid(), activityStatus);
    } else {
      InstalledLiveActivity liveActivity =
          controllerRepository.getInstalledLiveActivityByUuid(uuid);
      if (liveActivity != null) {
        spaceEnvironment.getLog().info(
            String.format("Reporting activity status %s for %s", uuid, LIVE_ACTIVITY_READY_STATUS));
        publishActivityStatus(uuid, LIVE_ACTIVITY_READY_STATUS);
      } else {
        spaceEnvironment.getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    }
  }

  @Override
  public void activateActivity(String uuid) {
    spaceEnvironment.getLog().info(String.format("Activating activity %s", uuid));

    // Can create since can immediately request activate
    ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
    if (activity != null) {
      attemptActivityActivate(activity);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Activity %s does not exist on controller", uuid));

      ActivityStatus status =
          new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void deactivateActivity(String uuid) {
    spaceEnvironment.getLog().info(String.format("Deactivating activity %s", uuid));

    ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
    if (activity != null) {
      attemptActivityDeactivate(activity);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Activity %s does not exist on controller", uuid));

      ActivityStatus status =
          new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void configureActivity(String uuid, Map<String, Object> configuration) {
    spaceEnvironment.getLog().info(String.format("Configuring activity %s", uuid));

    ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
    if (activity != null) {
      activity.updateConfiguration(configuration);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Activity %s does not exist on controller", uuid));

      ActivityStatus status =
          new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void cleanActivityTmpData(String uuid) {
    ActiveControllerActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      if (active.getCachedActivityStatus().getState().isRunning()) {
        spaceEnvironment.getLog().warn(
            String.format(
                "Attempting to clean activity tmp directory for a running activity %s. Aborting.",
                uuid));

        return;
      }
    }

    activityStorageManager.cleanTmpActivityDataDirectory(uuid);
  }

  @Override
  public void cleanActivityPermanentData(String uuid) {
    ActiveControllerActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      if (active.getCachedActivityStatus().getState().isRunning()) {
        spaceEnvironment
            .getLog()
            .warn(
                String
                    .format(
                        "Attempting to clean activity permanent data directory for a running activity %s. Aborting.",
                        uuid));

        return;
      }
    }

    activityStorageManager.cleanPermanentActivityDataDirectory(uuid);
  }

  @Override
  public void cleanControllerTempData() {
    spaceEnvironment.getLog().info("Cleaning controller temp directory");

    Files.deleteDirectoryContents(spaceEnvironment.getFilesystem().getTempDirectory());
  }

  @Override
  public void cleanControllerPermanentData() {
    spaceEnvironment.getLog().info("Cleaning controller permanent directory");

    Files.deleteDirectoryContents(spaceEnvironment.getFilesystem().getDataDirectory());
  }

  @Override
  public NativeActivityRunnerFactory getNativeActivityRunnerFactory() {
    return nativeActivityRunnerFactory;
  }

  @Override
  public ActiveControllerActivity getActiveActivityByUuid(String uuid) {
    return getActiveActivityByUuid(uuid, false);
  }

  /**
   * Publish an activity status in a safe manner
   *
   * @param uuid
   *          uuid of the activity
   * @param status
   *          the status
   */
  private void publishActivityStatus(final String uuid, final ActivityStatus status) {
    eventQueue.addEvent(new Runnable() {
      @Override
      public void run() {
        controllerCommunicator.publishActivityStatus(uuid, status);
      }
    });
  }

  /**
   * Get an activity by UUID.
   *
   * @param uuid
   *          the UUID of the activity
   * @param create
   *          {@code true} if should create the activity entry from the
   *          controller repository if none found, {@code false} otherwise.
   *
   * @return The activity with the given UUID. null if no such activity.
   */
  ActiveControllerActivity getActiveActivityByUuid(String uuid, boolean create) {
    ActiveControllerActivity activity = null;
    synchronized (activities) {
      activity = activities.get(uuid);
      if (activity == null && create) {
        activity = newLiveActivityFromRepository(uuid);

        if (activity != null) {
          activities.put(uuid, activity);
        }
      }
    }

    if (activity == null)
      spaceEnvironment.getLog().warn(
          String.format("Could not find active live activity with uuid %s", uuid));

    return activity;
  }

  /**
   * Create an activity from the repository.
   *
   * @param uuid
   *          UUID of the activity to create
   *
   * @return The active app with a runner.
   */
  private ActiveControllerActivity newLiveActivityFromRepository(String uuid) {
    InstalledLiveActivity liveActivity = controllerRepository.getInstalledLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      ActivityFilesystem activityFilesystem = activityStorageManager.getActivityFilesystem(uuid);

      LiveActivityConfiguration activityConfiguration =
          configurationManager.getConfiguration(activityFilesystem);
      activityConfiguration.load();

      ActiveControllerActivity activity =
          activeControllerActivityFactory.newActiveActivity(liveActivity, activityFilesystem,
              activityConfiguration, this);

      return activity;
    } else {
      return null;
    }
  }

  /**
   * Start up an activity.
   *
   * @param activity
   *          The activity to start up.
   */
  private void attemptActivityStartup(ActiveControllerActivity activity) {
    String uuid = activity.getUuid();
    spaceEnvironment.getLog().info(String.format("Attempting startup of activity %s", uuid));

    try {
      switch (activity.getActivityState()) {
        case STARTUP_FAILURE:
        case SHUTDOWN_FAILURE:
        case CRASHED:
          // If crashed, try a shutdown first.
          activity.shutdown();

        case READY:
          activity.startup();

          break;

        case RUNNING:
          // If was already running, just signal RUNNING
          spaceEnvironment.getLog().warn(
              String.format(
                  "Attempt to startup live activity %s which was running, sending RUNNINg",
                  activity.getUuid()));
          publishActivityStatus(activity.getUuid(), LIVE_ACTIVITY_RUNNING_STATUS);

          break;

        default:
          reportIllegalActivityStateTransition(activity, ActivityStateTransition.STARTUP);
      }
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Unable to start activity %s", uuid), e);
      ActivityStatus status = new ActivityStatus(ActivityState.STARTUP_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  /**
   * Attempt to shut an activity down.
   *
   * @param activity
   *          the activity to shutdown
   */
  private void attemptActivityShutdown(ActiveControllerActivity activity) {
    ActivityStateTransition transition = ActivityStateTransition.SHUTDOWN;
    TransitionResult transitionResult =
        transition.attemptTransition(activity.getActivityState(), activity);

    if (transitionResult.equals(TransitionResult.ILLEGAL)) {
      reportIllegalActivityStateTransition(activity, transition);
    } else if (transitionResult.equals(TransitionResult.NOOP)) {
      // If was already shut down, just signal READY
      spaceEnvironment.getLog().warn(
          String.format("Attempt to shutdown live activity %s which wasn't running, sending READY",
              activity.getUuid()));
      publishActivityStatus(activity.getUuid(), LIVE_ACTIVITY_READY_STATUS);
    }
  }

  /**
   * Attempt to activate an activity.
   *
   * @param activity
   *          The app to activate.
   */
  private void attemptActivityActivate(ActiveControllerActivity activity) {
    switch (activity.getActivityState()) {
      case RUNNING:
      case ACTIVATE_FAILURE:
        activity.activate();

        break;

      case ACTIVE:
        // If was already shut down, just signal READY
        spaceEnvironment.getLog().warn(
            String.format(
                "Attempt to activate live activity %s which was activated, sending ACTIVE",
                activity.getUuid()));
        publishActivityStatus(activity.getUuid(), LIVE_ACTIVITY_ACTIVE_STATUS);
        break;

      case READY:
        setupSequencedActiveTarget(activity);
        break;

      default:
        reportIllegalActivityStateTransition(activity, ActivityStateTransition.ACTIVATE);
    }
  }

  /**
   * Need to set up a target of going to active after a startup.
   *
   * <p>
   * This will start moving towards the goal.
   *
   * @param activity
   *          the activity to go to startup
   */
  private void setupSequencedActiveTarget(final ActiveControllerActivity activity) {
    final String uuid = activity.getUuid();
    activityWatcher.watchActivity(activity);
    activityStateTransitioners.addTransitioner(
        uuid,
        new ActivityStateTransitioner(activity, ActivityStateTransitioner.transitions(
            ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE), spaceEnvironment
            .getLog()));

    // TODO(keith): Android hates garbage collection. This may need an
    // object pool.
    eventQueue.addEvent(new Runnable() {
      @Override
      public void run() {
        activityStateTransitioners.transition(uuid, activity.getActivityState());
      }
    });
  }

  /**
   * Attempt to deactivate an activity.
   *
   * @param activity
   *          The app to deactivate.
   */
  private void attemptActivityDeactivate(ActiveControllerActivity activity) {
    switch (activity.getActivityState()) {
      case ACTIVE:
      case DEACTIVATE_FAILURE:
        activity.deactivate();
        break;

      default:
        reportIllegalActivityStateTransition(activity, ActivityStateTransition.DEACTIVATE);
    }
  }

  /**
   * Attempted an activity transition and it couldn't take place.
   *
   * @param activity
   *          the activity that was being transitioned
   * @param attemptedChange
   *          where the activity was going
   */
  private void reportIllegalActivityStateTransition(ActiveControllerActivity activity,
      ActivityStateTransition attemptedChange) {
    spaceEnvironment.getLog().error(
        String.format("Tried to %s activity %s, was in state %s\n", attemptedChange,
            activity.getUuid(), activity.getActivityStatus().toString()));
  }

  /**
   * Get a list of all activities running in the controller.
   *
   * <p>
   * Returned in no particular order. A new collection is made each time.
   *
   * @return All activities running in the controller.
   */
  public Collection<ActiveControllerActivity> getAllActiveActivities() {
    // TODO(keith): Think about how this should be in the controller.
    synchronized (activities) {
      return new ArrayList<ActiveControllerActivity>(activities.values());
    }
  }

  @Override
  public void shutdownControllerContainer() {
    spaceSystemControl.shutdown();
  }

  @Override
  public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
    return controllerRepository.getAllInstalledLiveActivities();
  }

  @Override
  public void onWatcherActivityError(ActiveControllerActivity activity, ActivityStatus oldStatus,
      ActivityStatus newStatus) {
    // TODO(keith): No need to publish here?
    // publishActivityStatus(activity.getUuid(), newStatus);

    if (oldStatus.getState() == ActivityState.STARTUP_ATTEMPT
        && newStatus.getState() == ActivityState.STARTUP_FAILURE) {
      handleActivityCantStart(activity);
    } else {
      handleActivityFailure(activity);
    }
  }

  @Override
  public void onWatcherActivityStatusChange(ActiveControllerActivity activity,
      ActivityStatus oldStatus, ActivityStatus newStatus) {
    // TODO(keith): No need to publish here?
    // publishActivityStatus(activity.getUuid(), newStatus);
  }

  /**
   * An activity was unable to start up.
   *
   * @param activity
   *          the problematic activity
   */
  private void handleActivityCantStart(ActiveControllerActivity activity) {
    Activity instance = activity.getInstance();
    alertStatusManager.announceStatus(activity);

    // Need better policy, for now, just clean up app and we will let the
    // controller handle it.
    instance.handleStartupFailure();

    alertStatusManager.announceStatus(activity);
  }

  /**
   * An activity has had some sort of error.
   *
   * @param activity
   *          the problematic activity
   */
  private void handleActivityFailure(ActiveControllerActivity activity) {
    // Activity instance = activity.getInstance();
    alertStatusManager.announceStatus(activity);

    // TODO(keith): Something in instance needs to happen.
  }

  @Override
  public SimpleSpaceController getControllerInfo() {
    return controllerInfo;
  }

  /**
   * The activity installer is signaling an install.
   *
   * @param uuid
   *          UUID of the installed activity.
   */
  private void handleActivityInstall(String uuid) {
    // Nothing to do right now
  }

  /**
   * The activity installer is signaling a removal.
   *
   * @param uuid
   *          UUID of the installed activity.
   * @param result
   *          result of the removal
   */
  private void handleActivityRemove(String uuid, RemoveActivityResult result) {
    spaceEnvironment.getLog().info(String.format("Removed activity %s", uuid));

    if (result == RemoveActivityResult.DOESNT_EXIST) {
      ActivityStatus status =
          new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  /**
   * Get the space controller's activity listener.
   *
   * <p>
   * This is used for event transmission to the master, among other things.
   *
   * @return the activity listener
   */
  public ActivityListener getActivityListener() {
    return activityListener;
  }

  /**
   * Potentially start up any controller control points.
   */
  private void startupControllerControl() {
    boolean startupFileControl =
        Boolean.parseBoolean(spaceEnvironment.getSystemConfiguration().getRequiredPropertyString(
            InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_FILE_CONTROLLABLE));
    if (startupFileControl) {
      fileControl = new SpaceControllerFileControl(this, spaceSystemControl, spaceEnvironment);
      fileControl.startup();
    }
  }

  /**
   * Start up the core services that all controllers provide.
   */
  private void startupCoreControllerServices() {
    ServiceRegistry serviceRegistry = getSpaceEnvironment().getServiceRegistry();

    webServerService = new NettyWebServerService();
    serviceRegistry.registerService(webServerService);
    webServerService.startup();

    webSocketClientService = new NettyWebSocketClientService();
    serviceRegistry.registerService(webSocketClientService);
    webSocketClientService.startup();
  }

  /**
   * Shutdown the core services provided by all controllers.
   */
  private void shutdownCoreControllerServices() {
    ServiceRegistry serviceRegistry = getSpaceEnvironment().getServiceRegistry();

    serviceRegistry.unregisterService(webServerService);
    webServerService.shutdown();

    serviceRegistry.unregisterService(webSocketClientService);
    webSocketClientService.shutdown();
  }
}
