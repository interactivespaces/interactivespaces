/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityControl;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStateTransition;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.BaseActivityRuntime;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.MinimalLiveActivity;
import interactivespaces.liveactivity.runtime.alert.AlertStatusManager;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfigurationManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationListener;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager.RemoveActivityResult;
import interactivespaces.liveactivity.runtime.logging.LiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransition;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransition.TransitionResult;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitionerCollection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The standard implementation of a {@link LiveActivityRuntime}.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRuntime extends BaseActivityRuntime implements LiveActivityRuntime,
    LiveActivityRunnerListener {

  /**
   * The factory for live activity runners.
   */
  private LiveActivityRunnerFactory liveActivityRunnerFactory;

  /**
   * Local repository of live activity information.
   */
  private LocalLiveActivityRepository liveActivityRepository;

  /**
   * Receives activities deployed to the runtime.
   */
  private ActivityInstallationManager activityInstallationManager;

  /**
   * All live activity runners in this controller, indexed by UUID.
   */
  private final Map<String, LiveActivityRunner> liveActivityRunners = Maps.newHashMap();

  /**
   * Sampler for live activity runners for this controller.
   */
  private LiveActivityRunnerSampler liveActivityRunnerSampler;

  /**
   * Log factory for activities.
   */
  private LiveActivityLogFactory activityLogFactory;

  /**
   * The configuration manager for activities.
   */
  private LiveActivityConfigurationManager configurationManager;

  /**
   * The publisher for live activity statuses.
   */
  private LiveActivityStatusPublisher liveActivityStatusPublisher;

  /**
   * The storage manager for activities.
   */
  private LiveActivityStorageManager activityStorageManager;

  /**
   * All activity state transitioners.
   */
  private SimpleGoalStateTransitionerCollection<ActivityState, ActivityControl> activityStateTransitioners;

  /**
   * The sequential event queue to be used for controller events.
   */
  private SequentialEventQueue eventQueue;

  /**
   * For important alerts worthy of paging, etc.
   */
  private AlertStatusManager alertStatusManager;

  /**
   * The component factory for the runtime.
   */
  private LiveActivityRuntimeComponentFactory liveActivityRuntimeComponentFactory;

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
   * Construct a new runtime.
   *
   * @param liveActivityRuntimeComponentFactory
   *          the component factory for live activity runtimes
   * @param liveActivityRepository
   *          the repository for live activities
   * @param activityInstallationManager
   *          the installation manager for new live activities
   * @param activityLogFactory
   *          the log factory for live activities
   * @param configurationManager
   *          the configuration manager for live activities
   * @param activityStorageManager
   *          the storage manager for live activities
   * @param alertStatusManager
   *          the alerting manager for live activities
   * @param eventQueue
   *          the event queue to use
   * @param spaceEnvironment
   *          the space environment to run under
   */
  public StandardLiveActivityRuntime(LiveActivityRuntimeComponentFactory liveActivityRuntimeComponentFactory,
      LocalLiveActivityRepository liveActivityRepository, ActivityInstallationManager activityInstallationManager,
      LiveActivityLogFactory activityLogFactory, LiveActivityConfigurationManager configurationManager,
      LiveActivityStorageManager activityStorageManager, AlertStatusManager alertStatusManager,
      SequentialEventQueue eventQueue, InteractiveSpacesEnvironment spaceEnvironment) {
    super(liveActivityRuntimeComponentFactory.newNativeActivityRunnerFactory(), liveActivityRuntimeComponentFactory
        .newActivityComponentFactory(), spaceEnvironment);
    this.liveActivityRuntimeComponentFactory = liveActivityRuntimeComponentFactory;
    this.liveActivityRunnerFactory = liveActivityRuntimeComponentFactory.newLiveActivityRunnerFactory();
    this.liveActivityRepository = liveActivityRepository;
    this.activityInstallationManager = activityInstallationManager;
    this.activityLogFactory = activityLogFactory;
    this.configurationManager = configurationManager;
    this.activityStorageManager = activityStorageManager;
    this.alertStatusManager = alertStatusManager;
    this.eventQueue = eventQueue;

    liveActivityRunnerSampler = new SimpleLiveActivityRunnerSampler(spaceEnvironment, spaceEnvironment.getLog());
  }

  @Override
  public void startup() {
    activityStateTransitioners = new SimpleGoalStateTransitionerCollection<ActivityState, ActivityControl>();

    activityInstallationManager.addActivityInstallationListener(activityInstallationListener);

    liveActivityRunnerSampler.startup();

    liveActivityRuntimeComponentFactory.registerCoreServices(getSpaceEnvironment().getServiceRegistry());
  }

  @Override
  public void shutdown() {
    if (liveActivityRunnerSampler != null) {
      liveActivityRunnerSampler.shutdown();
      liveActivityRunnerSampler = null;
    }

    activityStateTransitioners.clear();

    liveActivityRuntimeComponentFactory.unregisterCoreServices(getSpaceEnvironment().getServiceRegistry());
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
  public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
    return liveActivityRepository.getAllInstalledLiveActivities();
  }

  @Override
  public void startupLiveActivity(String uuid) {
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
  public void shutdownLiveActivity(final String uuid) {
    getSpaceEnvironment().getLog().info(String.format("Shutting down activity %s", uuid));

    try {
      LiveActivityRunner activity = getLiveActivityRunnerByUuid(uuid, false);
      if (activity != null) {
        attemptActivityShutdown(activity);
      } else {
        // The activity hasn't been active. Make sure it really exists then
        // send that it is ready.
        InstalledLiveActivity ia = liveActivityRepository.getInstalledLiveActivityByUuid(uuid);
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
      InstalledLiveActivity liveActivity = liveActivityRepository.getInstalledLiveActivityByUuid(uuid);
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
  public void activateLiveActivity(String uuid) {
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
  public void deactivateLiveActivity(String uuid) {
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
  public void configureLiveActivity(String uuid, Map<String, String> configuration) {
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
  public void initializeActivityInstance(MinimalLiveActivity liveActivity, ActivityFilesystem activityFilesystem,
      Activity instance, Configuration configuration, ActivityExecutionContext executionContext) {

    // Set log first to enable logging of any configuration/startup errors.
    instance.setLog(getActivityLog(liveActivity, configuration));

    String uuid = liveActivity.getUuid();
    instance.setActivityRuntime(this);
    instance.setUuid(uuid);

    instance.setConfiguration(configuration);
    instance.setActivityFilesystem(activityFilesystem);
    instance.setSpaceEnvironment(getSpaceEnvironment());
    instance.setExecutionContext(executionContext);

    initializeActivityConfiguration(configuration, activityFilesystem);

    onActivityInitialization(instance);
  }

  /**
   * Perform any needed initialization from the runtime on an activity instance.
   *
   * @param instance
   *          the activity instance
   */
  private void onActivityInitialization(Activity instance) {
    instance.addActivityListener(activityListener);
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

  @Override
  public Log getActivityLog(MinimalLiveActivity activity, Configuration configuration) {
    return activityLogFactory.createLogger(activity, configuration.getPropertyString(
        Activity.CONFIGURATION_PROPERTY_LOG_LEVEL, InteractiveSpacesEnvironment.LOG_LEVEL_ERROR));
  }

  /**
   * Initialize the configuration with any special values needed for running.
   *
   * @param configuration
   *          the configuration to be modified
   * @param activityFilesystem
   *          the activities file system
   */
  private void initializeActivityConfiguration(Configuration configuration, ActivityFilesystem activityFilesystem) {
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_INSTALL, activityFilesystem
        .getInstallDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_LOG, activityFilesystem
        .getLogDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_DATA, activityFilesystem
        .getPermanentDataDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_TMP, activityFilesystem
        .getTempDataDirectory().getAbsolutePath());

    // TODO(keith): Move to interactivespaces-system during bootstrap
    InteractiveSpacesFilesystem filesystem = getSpaceEnvironment().getFilesystem();
    configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_SYSTEM_FILESYSTEM_DIR_DATA, filesystem
        .getDataDirectory().getAbsolutePath());
    configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_SYSTEM_FILESYSTEM_DIR_TMP, filesystem
        .getTempDirectory().getAbsolutePath());
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

  @Override
  public LiveActivityRunner getLiveActivityRunnerByUuid(String uuid) {
    return getLiveActivityRunnerByUuid(uuid, false);
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
    InstalledLiveActivity liveActivity = liveActivityRepository.getInstalledLiveActivityByUuid(uuid);
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

  /**
   * Publish the status of a live activity.
   *
   * @param uuid
   *          uuid of the live activity
   * @param status
   *          the status of the live activity
   */
  private void publishActivityStatus(String uuid, ActivityStatus status) {
    liveActivityStatusPublisher.publishActivityStatus(uuid, status);
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
  public void setLiveActivityStatusPublisher(LiveActivityStatusPublisher liveActivityStatusPublisher) {
    this.liveActivityStatusPublisher = liveActivityStatusPublisher;
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
   * Get the activity listener used by the controller.
   *
   * @return the activity listener used by the controller
   */
  @VisibleForTesting
  ActivityListener getActivityListener() {
    return activityListener;
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
}
