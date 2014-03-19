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

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStateTransition;
import interactivespaces.activity.ActivityStateTransition.TransitionResult;
import interactivespaces.activity.ActivityStateTransitioner;
import interactivespaces.activity.ActivityStateTransitionerCollection;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.MinimalLiveActivity;
import interactivespaces.controller.SpaceControllerStatus;
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
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.uuid.JavaUuidGenerator;
import interactivespaces.util.uuid.UuidGenerator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

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
public class StandardSpaceController extends BaseSpaceController implements
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
  private final long heartbeatDelay = HEARTBEAT_DELAY_DEFAULT;

  /**
   * Manager for space controller data operations.
   */
  private final ControllerDataBundleManager dataBundleManager;

  /**
   * All activities in this controller, indexed by UUID.
   */
  private final Map<String, ActiveControllerActivity> activeActivities = Maps.newHashMap();

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
  private final long activityWatcherDelay = WATCHER_DELAY_DEFAULT;

  /**
   * For important alerts worthy of paging, etc.
   */
  private SimpleAlertStatusManager alertStatusManager;

  /**
   * Receives activities deployed to the controller.
   */
  private final ActivityInstallationManager activityInstallationManager;

  /**
   * A loader for container activities.
   */
  private final ActiveControllerActivityFactory activeControllerActivityFactory;

  /**
   * Local repository of controller information.
   */
  private final LocalSpaceControllerRepository controllerRepository;

  /**
   * The configuration manager for activities.
   */
  private final LiveActivityConfigurationManager configurationManager;

  /**
   * Log factory for activities.
   */
  private final ActivityLogFactory activityLogFactory;

  /**
   * The Interactive Spaces system controller.
   */
  private final InteractiveSpacesSystemControl spaceSystemControl;

  /**
   * The storage manager for activities.
   */
  private final ActivityStorageManager activityStorageManager;

  /**
   * All activity state transitioners.
   */
  private ActivityStateTransitionerCollection activityStateTransitioners;

  /**
   * The controller communicator for remote control.
   */
  private final SpaceControllerCommunicator controllerCommunicator;

  /**
   * A listener for installation events.
   */
  private final ActivityInstallationListener activityInstallationListener =
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
  private final ActivityListener activityListener = new ActivityListener() {
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
  private final SpaceControllerInfoPersister controllerInfoPersister;

  /**
   * File control for the controller.
   *
   * <p>
   * This can be {@code null} if it wasn't requested.
   */
  private SpaceControllerFileControl fileControl;

  /**
   * Support for file operations.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create a new StandardSpaceController.
   *
   * TODO(khughes): Fix this so it uses .set build pattern.
   */
  public StandardSpaceController(ActivityInstallationManager activityInstallationManager,
      LocalSpaceControllerRepository controllerRepository,
      ActiveControllerActivityFactory activeControllerActivityFactory,
      NativeActivityRunnerFactory nativeAppRunnerFactory,
      LiveActivityConfigurationManager configurationManager,
      ActivityStorageManager activityStorageManager, ActivityLogFactory activityLogFactory,
      SpaceControllerCommunicator controllerCommunicator,
      SpaceControllerInfoPersister controllerInfoPersister,
      InteractiveSpacesSystemControl spaceSystemControl,
      InteractiveSpacesEnvironment spaceEnvironment, ControllerDataBundleManager dataBundleManager) {
    super(spaceEnvironment, nativeAppRunnerFactory);
    this.activityInstallationManager = activityInstallationManager;
    this.controllerRepository = controllerRepository;
    this.activeControllerActivityFactory = activeControllerActivityFactory;
    this.configurationManager = configurationManager;
    this.activityStorageManager = activityStorageManager;
    this.activityLogFactory = activityLogFactory;

    this.dataBundleManager = dataBundleManager;
    dataBundleManager.setSpaceController(this);

    this.controllerCommunicator = controllerCommunicator;
    controllerCommunicator.setControllerControl(this);

    this.controllerInfoPersister = controllerInfoPersister;

    this.spaceSystemControl = spaceSystemControl;
  }

  @Override
  public void startup() {
    super.startup();

    confirmUuid();

    activityStateTransitioners = new ActivityStateTransitionerCollection();

    // TODO(keith): Set this container-wide.
    eventQueue = new SequentialEventQueue(getSpaceEnvironment(), getSpaceEnvironment().getLog());
    eventQueue.startup();

    // TODO(keith): Set this container-wide.
    alertStatusManager = new SimpleAlertStatusManager();
    alertStatusManager.setLog(getSpaceEnvironment().getLog());

    activityWatcher = new SpaceControllerActivityWatcher(getSpaceEnvironment());
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
              getSpaceEnvironment().getLog().error(
                  "Exception while trying to send a Space Controller heartbeat", e);
            }
          }
        }, heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);

    dataBundleManager.startup();

    startupControllerControl();
    startupCoreControllerServices();

    startupAutostartActivities();

    controllerCommunicator.notifyRemoteMasterServerAboutStartup(getControllerInfo());

    startedUp = true;
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
    SimpleSpaceController simpleSpaceController = getControllerInfo();
    String uuid = simpleSpaceController.getUuid();
    if (uuid == null || uuid.trim().isEmpty()) {
      UuidGenerator uuidGenerator = new JavaUuidGenerator();
      uuid = uuidGenerator.newUuid();
      simpleSpaceController.setUuid(uuid);

      getSpaceEnvironment().getLog().warn(
          String.format("No controller UUID found, generated UUID is %s", uuid));

      controllerInfoPersister.persist(simpleSpaceController, getSpaceEnvironment());
    }
  }

  /**
   * Save the controller information in the configurations.
   */

  @Override
  public void shutdown() {
    super.shutdown();
    if (startedUp) {
      try {
        activityStateTransitioners.clear();

        if (fileControl != null) {
          fileControl.shutdown();
          fileControl = null;
        }

        shutdownAllActivities();

        shutdownCoreControllerServices();

        controllerHeartbeatControl.cancel(true);
        controllerHeartbeatControl = null;

        dataBundleManager.shutdown();

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
        case READY:
          break;
        default:
          getSpaceEnvironment().getLog().error(
              String.format("Unknown startup type %s for activity %s/%s",
                  activity.getControllerStartupType(), activity.getIdentifyingName(),
                  activity.getUuid()));
      }
    }
  }

  @Override
  public void startupAllActivities() {
    for (ActiveControllerActivity app : getAllActiveActivities()) {
      attemptActivityStartup(app);
    }
  }

  @Override
  public void shutdownAllActivities() {
    getSpaceEnvironment().getLog().info("Shutting down all activities");

    for (ActiveControllerActivity app : getAllActiveActivities()) {
      attemptActivityShutdown(app);
    }
  }

  @Override
  public void captureControllerDataBundle(final String bundleUri) {
    getSpaceEnvironment().getLog().info("Capture controller data bundle");
    getSpaceEnvironment().getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        executeCaptureControllerDataBundle(bundleUri);
      }
    });
  }

  /**
   * Execute a capture controller data bundle request from the master. This will
   * trigger the data content push back to the master, and send off a
   * success/failure message.
   *
   * @param bundleUri
   *          Uri bundle to send the captured data bundle to.
   */
  private void executeCaptureControllerDataBundle(final String bundleUri) {
    try {
      dataBundleManager.captureControllerDataBundle(bundleUri);
      controllerCommunicator.publishControllerDataStatus(SpaceControllerDataOperation.DATA_CAPTURE,
          SpaceControllerStatus.SUCCESS, null);
    } catch (Exception e) {
      controllerCommunicator.publishControllerDataStatus(SpaceControllerDataOperation.DATA_CAPTURE,
          SpaceControllerStatus.FAILURE, e);
    }
  }

  @Override
  public void restoreControllerDataBundle(final String bundleUri) {
    getSpaceEnvironment().getLog().info("Restore controller data bundle");
    getSpaceEnvironment().getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        executeRestoreControllerDataBundle(bundleUri);
      }
    });
  }

  /**
   * Execute the restore data bundle operation. Will trigger the restore, and
   * send back a success/failure message.
   *
   * @param bundleUri
   *          URI for the data bundle to be fetched from.
   */
  private void executeRestoreControllerDataBundle(final String bundleUri) {
    try {
      dataBundleManager.restoreControllerDataBundle(bundleUri);
      controllerCommunicator.publishControllerDataStatus(SpaceControllerDataOperation.DATA_RESTORE,
          SpaceControllerStatus.SUCCESS, null);
    } catch (Exception e) {
      controllerCommunicator.publishControllerDataStatus(SpaceControllerDataOperation.DATA_RESTORE,
          SpaceControllerStatus.FAILURE, e);
    }
  }

  @Override
  public void startupActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Starting up activity %s", uuid));

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
        getSpaceEnvironment().getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(
          String.format("Error during startup of live activity %s", uuid), e);
      ActivityStatus status = new ActivityStatus(ActivityState.STARTUP_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void shutdownActivity(final String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Shutting down activity %s", uuid));

    try {
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
          getSpaceEnvironment().getLog().warn(
              String.format("Activity %s does not exist on controller", uuid));

          ActivityStatus status =
              new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
          publishActivityStatus(uuid, status);
        }
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(
          String.format("Error during shutdown of live activity %s", uuid), e);

      ActivityStatus status = new ActivityStatus(ActivityState.SHUTDOWN_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void statusActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Getting status of activity %s", uuid));

   ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
    if (activity != null) {
      final ActivityStatus activityStatus = activity.getActivityStatus();
      getSpaceEnvironment().getLog().info(
          String.format("Reporting activity status %s for %s", uuid, activityStatus));
      publishActivityStatus(activity.getUuid(), activityStatus);
    } else {
      InstalledLiveActivity liveActivity =
          controllerRepository.getInstalledLiveActivityByUuid(uuid);
      if (liveActivity != null) {
        getSpaceEnvironment().getLog().info(
            String.format("Reporting activity status %s for %s", uuid, LIVE_ACTIVITY_READY_STATUS));
        publishActivityStatus(uuid, LIVE_ACTIVITY_READY_STATUS);
      } else {
        getSpaceEnvironment().getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    }
  }

  @Override
  public void activateActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Activating activity %s", uuid));

    // Can create since can immediately request activate
    try {
      ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
      if (activity != null) {
        attemptActivityActivate(activity);
      } else {
        getSpaceEnvironment().getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(
          String.format("Error during activation of live activity %s", uuid), e);

      ActivityStatus status = new ActivityStatus(ActivityState.ACTIVATE_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void deactivateActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Deactivating activity %s", uuid));

    try {
      ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
      if (activity != null) {
        attemptActivityDeactivate(activity);
      } else {
        getSpaceEnvironment().getLog().warn(
            String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(
          String.format("Error during deactivation of live activity %s", uuid), e);

      ActivityStatus status = new ActivityStatus(ActivityState.DEACTIVATE_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void configureActivity(String uuid, Map<String, Object> configuration) {
    getSpaceEnvironment().getLog().info(String.format("Configuring activity %s", uuid));

    ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
    if (activity != null) {
      activity.updateConfiguration(configuration);
    } else {
      getSpaceEnvironment().getLog().warn(
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
        getSpaceEnvironment().getLog().warn(
            String.format(
                "Attempting to clean activity tmp directory for a running activity %s. Aborting.",
                uuid));

        return;
      }
    }

    getSpaceEnvironment().getLog().info(
        String.format("Cleaning activity tmp directory for activity %s.", uuid));
    activityStorageManager.cleanTmpActivityDataDirectory(uuid);
  }

  @Override
  public void cleanActivityPermanentData(String uuid) {
    ActiveControllerActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      if (active.getCachedActivityStatus().getState().isRunning()) {
        getSpaceEnvironment()
            .getLog()
            .warn(
                String
                    .format(
                        "Attempting to clean activity permanent data directory for a running activity %s. Aborting.",
                        uuid));

        return;
      }
    }

    getSpaceEnvironment().getLog().info(
        String.format("Cleaning activity permanent directory for activity %s.", uuid));
    activityStorageManager.cleanPermanentActivityDataDirectory(uuid);
  }

  @Override
  public void cleanControllerTempData() {
    getSpaceEnvironment().getLog().info("Cleaning controller temp directory");

    fileSupport.deleteDirectoryContents(getSpaceEnvironment().getFilesystem().getTempDirectory());
  }

  @Override
  public void cleanControllerPermanentData() {
    getSpaceEnvironment().getLog().info("Cleaning controller permanent directory");

    fileSupport.deleteDirectoryContents(getSpaceEnvironment().getFilesystem().getDataDirectory());
  }

  @Override
  public void cleanControllerTempDataAll() {
    for (InstalledLiveActivity activity : controllerRepository.getAllInstalledLiveActivities()) {
      cleanActivityTmpData(activity.getUuid());
    }
  }

  @Override
  public void cleanControllerPermanentDataAll() {
    getSpaceEnvironment().getLog().info("Cleaning live activity permanent directories");

    for (InstalledLiveActivity activity : controllerRepository.getAllInstalledLiveActivities()) {
      cleanActivityPermanentData(activity.getUuid());
    }
  }

  @Override
  public ActiveControllerActivity getActiveActivityByUuid(String uuid) {
    return getActiveActivityByUuid(uuid, false);
  }

  /**
   * Publish an activity status in a safe manner.
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
    synchronized (activeActivities) {
      activity = activeActivities.get(uuid);
      if (activity == null && create) {
        activity = newActiveActivityFromRepository(uuid);

        if (activity != null) {
          addActiveActivity(uuid, activity);
        }
      }
    }

    if (activity == null) {
      getSpaceEnvironment().getLog().warn(
          String.format("Could not find active live activity with uuid %s", uuid));
    }

    return activity;
  }

  /**
   * Add in a new active activity.
   *
   * @param uuid
   *          uuid of the activity
   * @param activity
   *          the active activity
   */
  @VisibleForTesting
  void addActiveActivity(String uuid, ActiveControllerActivity activity) {
    activeActivities.put(uuid, activity);
  }

  /**
   * Create an active activity from the repository.
   *
   * @param uuid
   *          UUID of the activity to create
   *
   * @return The active app with a runner.
   */
  private ActiveControllerActivity newActiveActivityFromRepository(String uuid) {
    InstalledLiveActivity liveActivity = controllerRepository.getInstalledLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      InternalActivityFilesystem activityFilesystem = activityStorageManager.getActivityFilesystem(uuid);

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
    getSpaceEnvironment().getLog().info(String.format("Attempting startup of activity %s", uuid));

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
          getSpaceEnvironment().getLog().warn(
              String.format(
                  "Attempt to startup live activity %s which was running, sending RUNNINg",
                  activity.getUuid()));
          publishActivityStatus(activity.getUuid(), activity.getActivityStatus());

          break;

        default:
          reportIllegalActivityStateTransition(activity, ActivityStateTransition.STARTUP);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(String.format("Unable to start activity %s", uuid), e);
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
      getSpaceEnvironment().getLog().warn(
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
        // If was already active, then just re-publish the status.
        getSpaceEnvironment().getLog().warn(
            String.format(
                "Attempt to activate live activity %s which was activated, sending ACTIVE",
                activity.getUuid()));
        publishActivityStatus(activity.getUuid(), activity.getActivityStatus());
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
            ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE),
            getSpaceEnvironment().getLog()));

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
    getSpaceEnvironment().getLog().error(
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
    synchronized (activeActivities) {
      return new ArrayList<ActiveControllerActivity>(activeActivities.values());
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
    getSpaceEnvironment().getLog().info(String.format("Removed activity %s", uuid));

    if (result == RemoveActivityResult.DOESNT_EXIST) {
      ActivityStatus status =
          new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  protected void onActivityInitialization(Activity instance) {
    instance.addActivityListener(activityListener);
  }

  /**
   * Potentially start up any controller control points.
   */
  private void startupControllerControl() {
    boolean startupFileControl =
        getSpaceEnvironment().getSystemConfiguration().getRequiredPropertyBoolean(
            InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_FILE_CONTROLLABLE);
    if (startupFileControl) {
      fileControl = new SpaceControllerFileControl(this, spaceSystemControl, getSpaceEnvironment());
      fileControl.startup();
    }
  }

  /**
   * Set the file support to be used.
   *
   * <p>
   * This is for testing and no other reason.
   *
   * @param fileSupport
   *          the file support to use
   */
  @VisibleForTesting
  void setFileSupport(FileSupport fileSupport) {
    this.fileSupport = fileSupport;
  }

  @Override
  protected Log getActivityLog(MinimalLiveActivity activity, Configuration configuration) {
    return activityLogFactory.createLogger(activity, configuration.getPropertyString(
        Activity.CONFIGURATION_PROPERTY_LOG_LEVEL, InteractiveSpacesEnvironment.LOG_LEVEL_ERROR));
  }
}
