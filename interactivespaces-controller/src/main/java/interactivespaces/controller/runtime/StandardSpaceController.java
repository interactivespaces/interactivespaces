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

package interactivespaces.controller.runtime;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityControl;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStateTransition;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.MinimalLiveActivity;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.SpaceControllerStatus;
import interactivespaces.controller.activity.installation.ActivityInstallationListener;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.activity.installation.ActivityInstallationManager.RemoveActivityResult;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.controller.runtime.configuration.SpaceControllerConfigurationManager;
import interactivespaces.controller.runtime.logging.AlertStatusManager;
import interactivespaces.controller.runtime.logging.LoggingAlertStatusManager;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.LiveActivityRunner;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerListener;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerSampler;
import interactivespaces.liveactivity.runtime.SimpleLiveActivityRunnerSampler;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfigurationManager;
import interactivespaces.liveactivity.runtime.logging.LiveActivityLogFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.concurrency.SimpleSequentialEventQueue;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransition;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransition.TransitionResult;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitionerCollection;
import interactivespaces.util.uuid.JavaUuidGenerator;
import interactivespaces.util.uuid.UuidGenerator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of {@link SpaceController} which gives basic implementation. Does not supply communication for
 * the remote master.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceController extends BaseSpaceController implements SpaceControllerControl,
    LiveActivityRunnerListener {

  /**
   * The default number of milliseconds the controllerHeartbeat thread delays between beats.
   */
  public static final int HEARTBEAT_DELAY_DEFAULT = 10000;

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
   * All live activity runners in this controller, indexed by UUID.
   */
  private final Map<String, LiveActivityRunner> liveActivityRunners = Maps.newHashMap();

  /**
   * Sampler for live activity runners for this controller.
   */
  private LiveActivityRunnerSampler liveActivityRunnerSampler;

  /**
   * For important alerts worthy of paging, etc.
   */
  private AlertStatusManager alertStatusManager;

  /**
   * Receives activities deployed to the controller.
   */
  private final ActivityInstallationManager activityInstallationManager;

  /**
   * A loader for container activities.
   */
  private final LiveActivityRunnerFactory liveActivityRunnerFactory;

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
  private final LiveActivityLogFactory activityLogFactory;

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
  private SimpleGoalStateTransitionerCollection<ActivityState, ActivityControl> activityStateTransitioners;

  /**
   * The controller communicator for remote control.
   */
  private final SpaceControllerCommunicator controllerCommunicator;

  /**
   * A listener for installation events.
   */
  private final ActivityInstallationListener activityInstallationListener = new ActivityInstallationListener() {
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
    public void onActivityStatusChange(Activity activity, ActivityStatus oldStatus, ActivityStatus newStatus) {
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
   * The configuration manager for the controller.
   */
  private final SpaceControllerConfigurationManager spaceControllerConfigurationManager;

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
   * Construct a new StandardSpaceController.
   *
   * TODO(khughes): Fix this so it uses .set build pattern.
   *
   * @param activityInstallationManager
   *          the installation manager for activity installation
   * @param controllerRepository
   *          the repository for controller information
   * @param activeControllerActivityFactory
   *          the factory for making live activity instances for running
   * @param nativeActivityRunnerFactory
   *          the factory for working with native activities
   * @param configurationManager
   *          the manager for working with live activity configurations
   * @param activityStorageManager
   *          the storage manager for live activities
   * @param activityLogFactory
   *          the log factory for live activities
   * @param controllerCommunicator
   *          the communicator for something controlling the controller
   * @param controllerInfoPersister
   *          the persister for controller information
   * @param spaceSystemControl
   *          the system control for the container
   * @param dataBundleManager
   *          the manager for data bundle operations
   * @param spaceControllerConfigurationManager
   *          configuration manager for the space controller
   * @param spaceEnvironment
   *          the space environment to use
   */
  public StandardSpaceController(ActivityInstallationManager activityInstallationManager,
      LocalSpaceControllerRepository controllerRepository, LiveActivityRunnerFactory activeControllerActivityFactory,
      NativeActivityRunnerFactory nativeActivityRunnerFactory, LiveActivityConfigurationManager configurationManager,
      ActivityStorageManager activityStorageManager, LiveActivityLogFactory activityLogFactory,
      SpaceControllerCommunicator controllerCommunicator, SpaceControllerInfoPersister controllerInfoPersister,
      InteractiveSpacesSystemControl spaceSystemControl, ControllerDataBundleManager dataBundleManager,
      SpaceControllerConfigurationManager spaceControllerConfigurationManager,
      InteractiveSpacesEnvironment spaceEnvironment) {
    super(spaceEnvironment, nativeActivityRunnerFactory);
    this.activityInstallationManager = activityInstallationManager;
    this.controllerRepository = controllerRepository;
    this.liveActivityRunnerFactory = activeControllerActivityFactory;
    this.configurationManager = configurationManager;
    this.activityStorageManager = activityStorageManager;
    this.activityLogFactory = activityLogFactory;
    this.spaceControllerConfigurationManager = spaceControllerConfigurationManager;

    this.dataBundleManager = dataBundleManager;
    dataBundleManager.setSpaceController(this);
    dataBundleManager.setActivityStorageManager(activityStorageManager);

    this.controllerCommunicator = controllerCommunicator;
    controllerCommunicator.setControllerControl(this);

    this.controllerInfoPersister = controllerInfoPersister;

    this.spaceSystemControl = spaceSystemControl;

    final Log log = getSpaceEnvironment().getLog();

    // TODO(keith): Set this container-wide.
    eventQueue = new SimpleSequentialEventQueue(spaceEnvironment, log);

    // TODO(keith): Set this container-wide.
    alertStatusManager = new LoggingAlertStatusManager(log);

    liveActivityRunnerSampler = new SimpleLiveActivityRunnerSampler(spaceEnvironment, log);
  }

  @Override
  public void startup() {
    super.startup();

    confirmUuid();

    activityStateTransitioners = new SimpleGoalStateTransitionerCollection<ActivityState, ActivityControl>();

    final Log log = getSpaceEnvironment().getLog();

    // TODO(keith): Set this container-wide.
    eventQueue.startup();

    activityInstallationManager.addActivityInstallationListener(activityInstallationListener);

    controllerCommunicator.onStartup();

    controllerHeartbeat = controllerCommunicator.newSpaceControllerHeartbeat();
    controllerHeartbeatControl = getSpaceEnvironment().getExecutorService().scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          controllerHeartbeat.sendHeartbeat();
        } catch (Exception e) {
          log.error("Exception while trying to send a Space Controller heartbeat", e);
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
  private void handleActivityListenerOnActivityStatusChange(final Activity activity, final ActivityStatus oldStatus,
      final ActivityStatus newStatus) {
    // TODO(keith): Android hates garbage collection. This may need an object pool.
    eventQueue.addEvent(new Runnable() {
      @Override
      public void run() {
        handleActivityStateChange(activity, oldStatus, newStatus);
      }
    });
  }

  /**
   * Handle the status change from an activity.
   *
   * @param activity
   *          the activity whose status changed
   * @param oldStatus
   *          the old status
   * @param newStatus
   *          the new status
   */
  private void handleActivityStateChange(Activity activity, ActivityStatus oldStatus, ActivityStatus newStatus) {
    ActivityState newState = newStatus.getState();
    boolean error = newState.isError();

    // Want the log before anything else is tried.
    if (error) {
      getSpaceEnvironment().getLog().error(
          String.format("Error for live activity %s, state is now %s", activity.getUuid(), newState),
          newStatus.getException());

    } else {
      getSpaceEnvironment().getLog().info(
          String.format("Live activity %s, state is now %s", activity.getUuid(), newState));
    }

    // If went from not running to running we need to watch the activity
    if (!oldStatus.getState().isRunning() && newState.isRunning()) {
      liveActivityRunnerSampler.startSamplingRunner(getLiveActivityRunnerByUuid(activity.getUuid()));
    }

    publishActivityStatus(activity.getUuid(), newStatus);

    if (error) {
      alertStatusManager.announceLiveActivityStatus(getLiveActivityRunnerByUuid(activity.getUuid()));
    } else {
      activityStateTransitioners.transition(activity.getUuid(), newState);
    }
  }

  @Override
  public void onNoInstanceActivityStatusEvent(LiveActivityRunner runner) {
    // The only thing that should come in here are errors
    ActivityStatus status = runner.getCachedActivityStatus();
    if (status.getState().isError()) {
      publishActivityStatus(runner.getUuid(), status);
      alertStatusManager.announceLiveActivityStatus(runner);
    } else {
      getSpaceEnvironment()
          .getLog()
          .warn(
              String
                  .format(
                      "How odd,  a live activity runner for live activity %s has a no instance status event that isn't an error: %s",
                      runner.getUuid(), status));
    }

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

      getSpaceEnvironment().getLog().warn(String.format("No controller UUID found, generated UUID is %s", uuid));

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

        liveActivityRunnerSampler.shutdown();
        liveActivityRunnerSampler = null;

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
    for (InstalledLiveActivity activity : getAllInstalledLiveActivities()) {
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
              String.format("Unknown startup type %s for activity %s/%s", activity.getControllerStartupType(),
                  activity.getIdentifyingName(), activity.getUuid()));
      }
    }
  }

  @Override
  public void startupAllActivities() {
    for (LiveActivityRunner app : getAllActiveActivities()) {
      attemptActivityStartup(app);
    }
  }

  @Override
  public void shutdownAllActivities() {
    getSpaceEnvironment().getLog().info("Shutting down all activities");

    for (LiveActivityRunner app : getAllActiveActivities()) {
      attemptActivityShutdown(app);
    }
  }

  @Override
  public void configureController(Map<String, String> configuration) {
    getSpaceEnvironment().getLog().info("Configuring the space controller");
    getSpaceEnvironment().getLog().info(configuration);

    spaceControllerConfigurationManager.update(configuration);
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
   * Execute a capture controller data bundle request from the master. This will trigger the data content push back to
   * the master, and send off a success/failure message.
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
      getSpaceEnvironment().getLog().error("Error capturing data bundle", e);
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
   * Execute the restore data bundle operation. Will trigger the restore, and send back a success/failure message.
   *
   * @param bundleUri
   *          URI for the data bundle to be fetched from
   */
  private void executeRestoreControllerDataBundle(final String bundleUri) {
    try {
      dataBundleManager.restoreControllerDataBundle(bundleUri);
      controllerCommunicator.publishControllerDataStatus(SpaceControllerDataOperation.DATA_RESTORE,
          SpaceControllerStatus.SUCCESS, null);
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error("Error restoring data bundle", e);
      controllerCommunicator.publishControllerDataStatus(SpaceControllerDataOperation.DATA_RESTORE,
          SpaceControllerStatus.FAILURE, e);
    }
  }

  @Override
  public void startupActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Starting up activity %s", uuid));

    try {
      LiveActivityRunner activity = getLiveActivityRunnerByUuid(uuid, true);
      if (activity != null) {
        ActivityStatus status = activity.getCachedActivityStatus();
        if (!status.getState().isRunning()) {
          attemptActivityStartup(activity);
        } else {
          // The activity is running so just report what it is doing
          publishActivityStatus(uuid, status);
        }
      } else {
        getSpaceEnvironment().getLog().warn(String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(String.format("Error during startup of live activity %s", uuid), e);
      ActivityStatus status = new ActivityStatus(ActivityState.STARTUP_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void shutdownActivity(final String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Shutting down activity %s", uuid));

    try {
      LiveActivityRunner activity = getLiveActivityRunnerByUuid(uuid, false);
      if (activity != null) {
        attemptActivityShutdown(activity);
      } else {
        // The activity hasn't been active. Make sure it really exists then
        // send that it is ready.
        InstalledLiveActivity ia = controllerRepository.getInstalledLiveActivityByUuid(uuid);
        if (ia != null) {
          publishActivityStatus(uuid, LiveActivityRunner.LIVE_ACTIVITY_STATUS_READY);
        } else {
          // TODO(keith): Tell master the controller doesn't exist.
          getSpaceEnvironment().getLog().warn(String.format("Activity %s does not exist on controller", uuid));

          ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
          publishActivityStatus(uuid, status);
        }
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(String.format("Error during shutdown of live activity %s", uuid), e);

      ActivityStatus status = new ActivityStatus(ActivityState.SHUTDOWN_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void statusLiveActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Getting status of activity %s", uuid));

    LiveActivityRunner activity = getLiveActivityRunnerByUuid(uuid, false);
    if (activity != null) {
      final ActivityStatus activityStatus = activity.sampleActivityStatus();
      getSpaceEnvironment().getLog().info(String.format("Reporting activity status %s for %s", uuid, activityStatus));
      publishActivityStatus(activity.getUuid(), activityStatus);
    } else {
      InstalledLiveActivity liveActivity = controllerRepository.getInstalledLiveActivityByUuid(uuid);
      if (liveActivity != null) {
        getSpaceEnvironment().getLog().info(
            String.format("Reporting activity status %s for %s", uuid, LiveActivityRunner.LIVE_ACTIVITY_STATUS_READY));
        publishActivityStatus(uuid, LiveActivityRunner.LIVE_ACTIVITY_STATUS_READY);
      } else {
        getSpaceEnvironment().getLog().warn(String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    }
  }

  @Override
  public void activateActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Activating activity %s", uuid));

    // Can create since can immediately request activate
    try {
      LiveActivityRunner activity = getLiveActivityRunnerByUuid(uuid, true);
      if (activity != null) {
        attemptActivityActivate(activity);
      } else {
        getSpaceEnvironment().getLog().warn(String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(String.format("Error during activation of live activity %s", uuid), e);

      ActivityStatus status = new ActivityStatus(ActivityState.ACTIVATE_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void deactivateActivity(String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Deactivating activity %s", uuid));

    try {
      LiveActivityRunner runner = getLiveActivityRunnerByUuid(uuid, false);
      if (runner != null) {
        attemptActivityDeactivate(runner);
      } else {
        getSpaceEnvironment().getLog().warn(String.format("Activity %s does not exist on controller", uuid));

        ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error(String.format("Error during deactivation of live activity %s", uuid), e);

      ActivityStatus status = new ActivityStatus(ActivityState.DEACTIVATE_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void configureActivity(String uuid, Map<String, String> configuration) {
    getSpaceEnvironment().getLog().info(String.format("Configuring activity %s", uuid));

    LiveActivityRunner activity = getLiveActivityRunnerByUuid(uuid, true);
    if (activity != null) {
      activity.updateConfiguration(configuration);
    } else {
      getSpaceEnvironment().getLog().warn(String.format("Activity %s does not exist on controller", uuid));

      ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void cleanLiveActivityTmpData(String uuid) {
    LiveActivityRunner active = getLiveActivityRunnerByUuid(uuid);
    if (active != null) {
      if (active.getCachedActivityStatus().getState().isRunning()) {
        getSpaceEnvironment().getLog().warn(
            String.format("Attempting to clean activity tmp directory for a running activity %s. Aborting.", uuid));

        return;
      }
    }

    getSpaceEnvironment().getLog().info(String.format("Cleaning activity tmp directory for activity %s.", uuid));
    activityStorageManager.cleanTmpActivityDataDirectory(uuid);
  }

  @Override
  public void cleanLiveActivityPermanentData(String uuid) {
    LiveActivityRunner active = getLiveActivityRunnerByUuid(uuid);
    if (active != null) {
      if (active.getCachedActivityStatus().getState().isRunning()) {
        getSpaceEnvironment().getLog().warn(
            String.format("Attempting to clean activity permanent data directory for a running activity %s. Aborting.",
                uuid));

        return;
      }
    }

    getSpaceEnvironment().getLog().info(String.format("Cleaning activity permanent directory for activity %s.", uuid));
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
    for (InstalledLiveActivity activity : getAllInstalledLiveActivities()) {
      cleanLiveActivityTmpData(activity.getUuid());
    }
  }

  @Override
  public void cleanControllerPermanentDataAll() {
    getSpaceEnvironment().getLog().info("Cleaning live activity permanent directories");

    for (InstalledLiveActivity activity : getAllInstalledLiveActivities()) {
      cleanLiveActivityPermanentData(activity.getUuid());
    }
  }

  @Override
  public LiveActivityRunner getLiveActivityRunnerByUuid(String uuid) {
    return getLiveActivityRunnerByUuid(uuid, false);
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
   * Get an activity runner by UUID.
   *
   * @param uuid
   *          the UUID of the activity
   * @param create
   *          {@code true} if should create the activity entry from the controller repository if none found,
   *          {@code false} otherwise.
   *
   * @return the runner for the activity with the given UUID, {@code null} if no such activity
   */
  @VisibleForTesting
  LiveActivityRunner getLiveActivityRunnerByUuid(String uuid, boolean create) {
    LiveActivityRunner runner = null;
    synchronized (liveActivityRunners) {
      runner = liveActivityRunners.get(uuid);
      if (runner == null && create) {
        runner = newLiveActivityRunner(uuid);

        if (runner != null) {
          addLiveActivityRunner(uuid, runner);
        }
      }
    }

    if (runner == null) {
      getSpaceEnvironment().getLog().warn(String.format("Could not find live activity runner with uuid %s", uuid));
    }

    return runner;
  }

  /**
   * Add in a new active activity.
   *
   * @param uuid
   *          uuid of the activity
   * @param runner
   *          the live activity runner
   */
  @VisibleForTesting
  void addLiveActivityRunner(String uuid, LiveActivityRunner runner) {
    liveActivityRunners.put(uuid, runner);
  }

  /**
   * Create anew live activity runner from the repository.
   *
   * @param uuid
   *          UUID of the activity to create a runner for
   *
   * @return the new live activity runner
   */
  private LiveActivityRunner newLiveActivityRunner(String uuid) {
    InstalledLiveActivity liveActivity = controllerRepository.getInstalledLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      InternalLiveActivityFilesystem activityFilesystem = activityStorageManager.getActivityFilesystem(uuid);

      LiveActivityConfiguration activityConfiguration =
          configurationManager.newLiveActivityConfiguration(activityFilesystem);
      activityConfiguration.load();

      LiveActivityRunner runner =
          liveActivityRunnerFactory.newLiveActivityRunner(liveActivity, activityFilesystem, activityConfiguration,
              this, this);

      return runner;
    } else {
      return null;
    }
  }

  /**
   * Start up an activity.
   *
   * @param activity
   *          the activity to start up
   */
  private void attemptActivityStartup(LiveActivityRunner activity) {
    String uuid = activity.getUuid();
    getSpaceEnvironment().getLog().info(String.format("Attempting startup of activity %s", uuid));

    attemptActivityStateTransition(activity, ActivityStateTransition.STARTUP,
        "Attempt to startup live activity %s which was running, sending RUNNING", activity.getCachedActivityStatus());
  }

  /**
   * Attempt to shut an activity down.
   *
   * @param activity
   *          the activity to shutdown
   */
  private void attemptActivityShutdown(LiveActivityRunner activity) {
    attemptActivityStateTransition(activity, ActivityStateTransition.SHUTDOWN,
        "Attempt to shutdown live activity %s which wasn't running, sending READY",
        LiveActivityRunner.LIVE_ACTIVITY_STATUS_READY);
  }

  /**
   * Attempt to activate an activity.
   *
   * @param activity
   *          the activity to activate
   */
  private void attemptActivityActivate(LiveActivityRunner activity) {
    if (ActivityStateTransition.STARTUP.canTransition(activity.sampleActivityStatus().getState()) == TransitionResult.OK) {
      setupSequencedActiveTarget(activity);
    } else {
      attemptActivityStateTransition(activity, ActivityStateTransition.ACTIVATE,
          "Attempt to activate live activity %s which was activated, sending ACTIVE",
          LiveActivityRunner.LIVE_ACTIVITY_STATUS_ACTIVE);
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
  @SuppressWarnings("unchecked")
  private void setupSequencedActiveTarget(final LiveActivityRunner activity) {
    final String uuid = activity.getUuid();

    SimpleGoalStateTransitioner<ActivityState, ActivityControl> transitioner =
        new SimpleGoalStateTransitioner<ActivityState, ActivityControl>(activity, getSpaceEnvironment().getLog())
            .addTransitions(ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE);
    activityStateTransitioners.addTransitioner(uuid, transitioner);

    // TODO(keith): Android hates garbage collection. This may need an object pool.
    eventQueue.addEvent(new Runnable() {
      @Override
      public void run() {
        activityStateTransitioners.transition(uuid, activity.sampleActivityStatus().getState());
      }
    });
  }

  /**
   * Attempt to deactivate an activity.
   *
   * @param activity
   *          the activity to deactivate
   */
  private void attemptActivityDeactivate(LiveActivityRunner activity) {
    attemptActivityStateTransition(activity, ActivityStateTransition.DEACTIVATE,
        "Attempt to deactivate live activity %s which wasn't activated, sending RUNNING",
        LiveActivityRunner.LIVE_ACTIVITY_STATUS_RUNNING);
  }

  /**
   * Attempt to do a state transition on an activity.
   *
   * @param runner
   *          the runner for the activity
   * @param transition
   *          the transition to take place
   * @param noopMessage
   *          the message to log if this is a no-op
   * @param noopStatus
   *          the status to report if this is a no-op
   *
   * @return {@code true} if the transition actually happened
   */
  private boolean attemptActivityStateTransition(LiveActivityRunner runner,
      SimpleGoalStateTransition<ActivityState, ActivityControl> transition, String noopMessage,
      ActivityStatus noopStatus) {
    TransitionResult transitionResult = transition.attemptTransition(runner.sampleActivityStatus().getState(), runner);

    if (transitionResult == TransitionResult.OK) {
      return true;
    } else if (transitionResult == TransitionResult.ILLEGAL) {
      reportIllegalActivityStateTransition(runner, transition);
    } else if (transitionResult == TransitionResult.NOOP) {
      // If didn't do anything, report the message requested.
      getSpaceEnvironment().getLog().warn(String.format(noopMessage, runner.getUuid()));
      publishActivityStatus(runner.getUuid(), noopStatus);
    } else {
      getSpaceEnvironment().getLog().warn(
          String.format("Unexpected activity state transition %s for live activity %s", transitionResult,
              runner.getUuid()));
    }

    return false;
  }

  /**
   * Attempted an activity transition and it couldn't take place.
   *
   * @param activity
   *          the activity that was being transitioned
   * @param attemptedChange
   *          where the activity was going
   */
  private void reportIllegalActivityStateTransition(LiveActivityRunner activity,
      SimpleGoalStateTransition<ActivityState, ActivityControl> attemptedChange) {
    getSpaceEnvironment().getLog().error(
        String.format("Tried to %s activity %s, was in state %s\n", attemptedChange, activity.getUuid(), activity
            .getCachedActivityStatus().toString()));
  }

  /**
   * Get a list of all activities running in the controller.
   *
   * <p>
   * Returned in no particular order. A new collection is made each time.
   *
   * @return All activities running in the controller.
   */
  public Collection<LiveActivityRunner> getAllActiveActivities() {
    // TODO(keith): Think about how this should be in the controller.
    synchronized (liveActivityRunners) {
      return Lists.newArrayList(liveActivityRunners.values());
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
      ActivityStatus status = new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
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

  /**
   * Set the alert status manager.
   *
   * @param alertStatusManager
   *          the alert status manager
   */
  @VisibleForTesting
  void setAlertStatusManager(AlertStatusManager alertStatusManager) {
    this.alertStatusManager = alertStatusManager;
  }

  /**
   * Set the live activity runner sampler to use.
   *
   * @param liveActivityRunnerSampler
   *          the sampler to use
   */
  @VisibleForTesting
  void setLiveActivityRunnerSampler(LiveActivityRunnerSampler liveActivityRunnerSampler) {
    this.liveActivityRunnerSampler = liveActivityRunnerSampler;
  }

  /**
   * Set the event queue.
   *
   * @param eventQueue
   *          the event queue to use
   */
  @VisibleForTesting
  void setEventQueue(SequentialEventQueue eventQueue) {
    this.eventQueue = eventQueue;
  }

  /**
   * Get the activity listener used by the controller.
   *
   * @return the activity listener used by the controller
   */
  @VisibleForTesting
  ActivityListener getActivityListener() {
    return activityListener;
  }

  @Override
  protected Log getActivityLog(MinimalLiveActivity activity, Configuration configuration) {
    return activityLogFactory.createLogger(activity, configuration.getPropertyString(
        Activity.CONFIGURATION_PROPERTY_LOG_LEVEL, InteractiveSpacesEnvironment.LOG_LEVEL_ERROR));
  }
}
