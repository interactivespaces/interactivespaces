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

package interactivespaces.liveactivity.runtime;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;

import com.google.common.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A runner for a Live Activity.
 *
 * @author Keith M. Hughes
 */
public class BasicLiveActivityRunner implements LiveActivityRunner {

  /**
   * The amount of time to wait for the instance lock in milliseconds.
   */
  private static final long INSTANCE_LOCK_WAIT_TIME = 20000;

  /**
   * UUID of the activity.
   */
  private final String uuid;

  /**
   * The locally installed activity for this activity.
   */
  private final InstalledLiveActivity installedActivity;

  /**
   * Runner for the activity.
   */
  private final ActivityWrapper activityWrapper;

  /**
   * File system for the activity.
   */
  private final InternalLiveActivityFilesystem activityFilesystem;

  /**
   * The activity configuration.
   */
  private final LiveActivityConfiguration configuration;

  /**
   * The listener for runner events.
   */
  private final LiveActivityRunnerListener liveActivityRunnerListener;

  /**
   * The live activity runtime this activity is running under.
   */
  private LiveActivityRuntime liveActivityRuntime;

  /**
   * The activity being run.
   *
   * <p>
   * This can be {@code null} if an instance hasn't been created yet.
   */
  private Activity instance;

  /**
   * The activity status of an activity.
   */
  private ActivityStatus cachedActivityStatus;

  /**
   * A lock for anything working with the instance being controlled.
   */
  private final ReentrantLock instanceLock = new ReentrantLock();

  /**
   * The current state of the instance lock.
   */
  private InstanceLockState instanceLockState;

  /**
   * Construct a new active activity.
   *
   * @param installedActivity
   *          the installed activity this is part of
   * @param activityWrapper
   *          the wrapper for running the activity
   * @param activityFilesystem
   *          the activity's file system
   * @param configuration
   *          the configuration for the activity
   * @param liveActivityRunnerListener
   *          listener for live activity runner events
   * @param liveActivityRuntime
   *          the live activity runtime this runner is running under
   */
  public BasicLiveActivityRunner(InstalledLiveActivity installedActivity, ActivityWrapper activityWrapper,
      InternalLiveActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      LiveActivityRunnerListener liveActivityRunnerListener, LiveActivityRuntime liveActivityRuntime) {
    this.uuid = installedActivity.getUuid();
    this.installedActivity = installedActivity;
    this.activityWrapper = activityWrapper;
    this.activityFilesystem = activityFilesystem;
    this.configuration = configuration;
    this.liveActivityRunnerListener = liveActivityRunnerListener;
    this.liveActivityRuntime = liveActivityRuntime;

    cachedActivityStatus = INITIAL_ACTIVITY_STATUS;

    instanceLockState = InstanceLockState.NEUTRAL;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void updateConfiguration(Map<String, String> update) {
    if (obtainInstanceLock(InstanceLockState.CONFIGURE)) {
      try {
        configuration.update(update);

        if (instance != null && instance.getActivityStatus().getState().isRunning()) {
          instance.updateConfiguration(update);
        }
      } finally {
        releaseInstanceLock();
      }
    }
  }

  @Override
  public void startup() {
    if (obtainInstanceLock(InstanceLockState.STARTUP)) {
      try {
        if (instance == null) {
          try {
            configuration.load();
            instance = activityWrapper.newInstance();
            liveActivityRuntime.initializeActivityInstance(installedActivity, activityFilesystem, instance,
                configuration, activityWrapper.newExecutionContext());

            instance.startup();

            // Instances notify status changes through their event listeners, so the startup notification is already
            // handled. .

            // If not running, we want to punt on having an instance as the runner only holds onto an instance reference
            // if it successfully starts.
            if (!instance.getActivityStatus().getState().isRunning()) {
              instance = null;
            }

          } catch (Throwable e) {
            setActivityStatusUnprotected(new ActivityStatus(ActivityState.STARTUP_FAILURE, null, e));

            if (instance != null) {
              try {
                instance.handleStartupFailure();
              } catch (Throwable e1) {
                liveActivityRuntime.getSpaceEnvironment().getLog()
                    .error("Error cleaning up activity which could not start", e1);
              }
            }

          }
        } else {
          liveActivityRuntime.getSpaceEnvironment().getLog()
              .warn(String.format("Attempt to start activity %s that is already started", uuid));
        }
      } finally {
        releaseInstanceLock();
      }
    }
  }

  @Override
  public void shutdown() {
    // Can call shutdown multiple times.
    if (obtainInstanceLock(InstanceLockState.SHUTDOWN)) {
      try {
        if (instance != null) {
          try {
            instance.shutdown();

            // Sample the status after a complete shutdown.
            sampleActivityStatusUnprotected();

            instance = null;
          } catch (Throwable e) {
            setActivityStatusUnprotected(new ActivityStatus(ActivityState.SHUTDOWN_FAILURE, null, e));
          }
        } else {
          liveActivityRuntime.getSpaceEnvironment().getLog()
              .warn(String.format("Attempt to shut down activity %s that wasn't running", uuid));
        }
      } finally {
        releaseInstanceLock();
      }
    }
  }

  @Override
  public void activate() {
    if (obtainInstanceLock(InstanceLockState.ACTIVATE)) {
      try {
        if (instance != null) {
          try {
            instance.activate();
          } catch (Throwable e) {
            setActivityStatusUnprotected(new ActivityStatus(ActivityState.ACTIVATE_FAILURE, null, e));
          }
        } else {
          throw new SimpleInteractiveSpacesException(String.format(
              "Attempt to activate activity %s which is not started", uuid));
        }
      } finally {
        releaseInstanceLock();
      }
    }
  }

  @Override
  public void deactivate() {
    if (obtainInstanceLock(InstanceLockState.DEACTIVATE)) {
      try {
        if (instance != null) {
          try {
            instance.deactivate();
          } catch (Throwable e) {
            setActivityStatusUnprotected(new ActivityStatus(ActivityState.DEACTIVATE_FAILURE, null, e));
          }
        } else {
          throw new SimpleInteractiveSpacesException(String.format(
              "Attempt to deactivate activity %s which is not started", uuid));
        }
      } finally {
        releaseInstanceLock();
      }
    }
  }

  @Override
  public ActivityStatus sampleActivityStatus() {
    if (obtainInstanceLock(InstanceLockState.STATUS)) {
      try {
        return sampleActivityStatusUnprotected();
      } finally {
        releaseInstanceLock();
      }
    } else {
      throw new SimpleInteractiveSpacesException("Could not get activity status because could not get instance lock");
    }
  }

  /**
   * Get the state of the live activity, not protected by the instance lock.
   *
   * <p>
   * This will be the last known state of the activity. The activity itself will be sampled.
   *
   * @return the state of the activity
   */
  private ActivityStatus sampleActivityStatusUnprotected() {
    if (instance != null) {
      instance.checkActivityState();

      cachedActivityStatus = instance.getActivityStatus();
    }

    return cachedActivityStatus;
  }

  @Override
  public ActivityStatus getCachedActivityStatus() {
    return cachedActivityStatus;
  }

  @Override
  public Activity getInstance() {
    if (obtainInstanceLock(InstanceLockState.OBTAIN)) {
      try {
        return instance;
      } finally {
        releaseInstanceLock();
      }
    } else {
      throw new SimpleInteractiveSpacesException("Could not obtain instance");
    }
  }

  /**
   * Set the instance to be used for this active activity.
   *
   * @param instance
   *          the instance of the activity
   */
  @VisibleForTesting
  void setInstance(Activity instance) {
    this.instance = instance;
  }

  /**
   * Set the cached activity status.
   *
   * @param cachedActivityStatus
   *          the new cached activity status
   */
  @VisibleForTesting
  public void setCachedActivityStatus(ActivityStatus cachedActivityStatus) {
    this.cachedActivityStatus = cachedActivityStatus;
  }

  @Override
  public void setActivityStatus(ActivityStatus status) {
    if (obtainInstanceLock(InstanceLockState.STATUS)) {
      try {
        setActivityStatusUnprotected(status);
      } finally {
        releaseInstanceLock();
      }
    } else {
      throw new SimpleInteractiveSpacesException("Could not set activity status because could not get instance lock");
    }
  }

  @Override
  public String getDisplayName() {
    return installedActivity.getDisplayName();
  }

  /**
   * Set the activity status of the live activity, not protected by the instance lock.
   *
   * @param status
   *          activity status to set
   */
  private void setActivityStatusUnprotected(ActivityStatus status) {
    cachedActivityStatus = status;
    if (instance != null) {
      // Setting the status on the instance will trigger sending activity events to all activity listeners.
      instance.setActivityStatus(status);
    } else {
      liveActivityRunnerListener.onNoInstanceActivityStatusEvent(this);
    }
  }

  @Override
  @VisibleForTesting
  public ActivityWrapper getActivityWrapper() {
    return activityWrapper;
  }

  /**
   * Get the instance lock.
   *
   * <p>
   * This method blocks until the lock is obtained, but logs when it cannot get the lock in a timely manner.
   *
   * @param newInstanceLockState
   *          the state to go into while the lock is held
   *
   * @return {@code true} if the lock was obtained, {@code false} if the thread trying to acquire was interrupted
   */
  private boolean obtainInstanceLock(InstanceLockState newInstanceLockState) {
    try {
      long time = liveActivityRuntime.getSpaceEnvironment().getTimeProvider().getCurrentTime();
      while (!instanceLock.tryLock(INSTANCE_LOCK_WAIT_TIME, TimeUnit.MILLISECONDS)) {
        liveActivityRuntime
            .getSpaceEnvironment()
            .getLog()
            .warn(
                String.format("A wait on the activity instance lock for %s has been blocked for"
                    + " %d milliseconds as the instance is in %s", uuid, (liveActivityRuntime.getSpaceEnvironment()
                    .getTimeProvider().getCurrentTime() - time), instanceLockState));
      }

      // Check if the current thread has entered more than once.
      if (instanceLock.getHoldCount() > 1) {
        // Locks are tested outside of the finally block that unlocks them so
        // this needs to be unlocked to get the hold count back down.
        instanceLock.unlock();

        throw new SimpleInteractiveSpacesException(String.format(
            "Nested calls to instanceLock protected methods in %s for activity %s",
            BasicLiveActivityRunner.class.getName(), uuid));
      }

      instanceLockState = newInstanceLockState;

      return true;
    } catch (InterruptedException e) {
      liveActivityRuntime.getSpaceEnvironment().getLog()
          .warn(String.format("A wait on the activity instance lock for %s was interrupted", uuid));

      return false;
    }
  }

  /**
   * Release the instance lock.
   */
  private void releaseInstanceLock() {
    instanceLockState = InstanceLockState.NEUTRAL;

    instanceLock.unlock();
  }

  /**
   * State of the instance lock for this activity.
   */
  private enum InstanceLockState {
    /**
     * Nothing is using the instance lock at the moment.
     */
    NEUTRAL,

    /**
     * The activity is being configured.
     */
    CONFIGURE,

    /**
     * The activity is starting up.
     */
    STARTUP,

    /**
     * The instance is activating.
     */
    ACTIVATE,

    /**
     * The instance is deactivation.
     */
    DEACTIVATE,

    /**
     * The instance is having its status sampled.
     */
    STATUS,

    /**
     * The instance is being shut down.
     */
    SHUTDOWN,

    /**
     * The instance reference is being obtained.
     */
    OBTAIN
  }
}
