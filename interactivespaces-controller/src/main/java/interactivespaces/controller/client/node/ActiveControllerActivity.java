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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityControl;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.domain.InstalledLiveActivity;

import com.google.common.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An activity which is live in the controller.
 *
 * @author Keith M. Hughes
 */
public class ActiveControllerActivity implements ActivityControl {

  /**
   * Initial status for an activity.
   */
  private static final ActivityStatus INITIAL_ACTIVITY_STATUS = new ActivityStatus(ActivityState.READY, "");

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
  private final ActivityFilesystem activityFilesystem;

  /**
   * The activity configuration.
   */
  private final LiveActivityConfiguration configuration;

  /**
   * The controller which contains the activity
   *
   * <p>
   * This can be null if an instance hasn't been created yet.
   */
  private final SpaceController controller;

  /**
   * The activity being run.
   *
   * <p>
   * This can be null if an instance hasn't been created yet.
   */
  private Activity instance;

  /**
   * The activity status of an activity.
   */
  private ActivityStatus cachedActivityStatus = INITIAL_ACTIVITY_STATUS;

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
   * @param controller
   *          the controller the activity is running under
   */
  public ActiveControllerActivity(InstalledLiveActivity installedActivity, ActivityWrapper activityWrapper,
      ActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration, SpaceController controller) {
    this.uuid = installedActivity.getUuid();
    this.installedActivity = installedActivity;
    this.activityWrapper = activityWrapper;
    this.activityFilesystem = activityFilesystem;
    this.configuration = configuration;
    this.controller = controller;

    instanceLockState = InstanceLockState.NEUTRAL;
  }

  /**
   * Get the UUID of the underlying live activity.
   *
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Update the configuration of the activity.
   *
   * @param update
   *          a map of the configuration update
   */
  public void updateConfiguration(Map<String, Object> update) {
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
          configuration.load();
          instance = activityWrapper.newInstance();
          controller.initializeActivityInstance(installedActivity, activityFilesystem, instance, configuration,
              activityWrapper.newExecutionContext());
          instance.startup();
        } else {
          throw new SimpleInteractiveSpacesException(String.format(
              "Attempt to start activity %s which is already started", uuid));
        }
      } catch (Exception e) {
        controller.getSpaceEnvironment().getLog().error("Error starting activity", e);
        setActivityStatusUnprotected(new ActivityStatus(ActivityState.STARTUP_FAILURE, null, e));
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
          instance.shutdown();

          // Sample the status after a complete shutdown.
          getActivityStatusUnprotected();

          instance = null;
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
          instance.activate();
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
          instance.deactivate();
        } else {
          throw new SimpleInteractiveSpacesException(String.format(
              "Attempt to deactivate activity %s which is not started", uuid));
        }
      } finally {
        releaseInstanceLock();
      }
    }
  }

  /**
   * Return the current status of the live activity.
   *
   * This will sample the activity if it can.
   *
   * @return the status of the activity
   */
  public ActivityState getActivityState() {
    return getActivityStatus().getState();
  }

  /**
   * Get the state of the live activity.
   *
   * <p>
   * This will be the last known state of the activity. The activity itself will be sampled.
   *
   * @return the state of the activity
   */
  public ActivityStatus getActivityStatus() {
    if (obtainInstanceLock(InstanceLockState.STATUS)) {
      try {
        return getActivityStatusUnprotected();
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
  private ActivityStatus getActivityStatusUnprotected() {
    if (instance != null) {
      instance.checkActivityState();

      cachedActivityStatus = instance.getActivityStatus();
    }

    return cachedActivityStatus;
  }

  /**
   * Get the last known activity status. Does not sample the activity.
   *
   * @return the cached activity status
   */
  public ActivityStatus getCachedActivityStatus() {
    return cachedActivityStatus;
  }

  /**
   * get the instance of the activity.
   *
   * @return the instance (can be {@code null} if the activity has not been
   *         started)
   */
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
   * Get the instance lock.
   *
   * <p>
   * This method blocks until the lock is obtained, but logs when it cannot get
   * the lock in a timely manner.
   *
   * @param newInstanceLockState
   *          the state to go into while the lock is held
   *
   * @return {@code true} if the lock was obtained, {@code false} if the thread
   *         trying to acquire was interrupted
   */
  private boolean obtainInstanceLock(InstanceLockState newInstanceLockState) {
    try {
      long time = controller.getSpaceEnvironment().getTimeProvider().getCurrentTime();
      while (!instanceLock.tryLock(INSTANCE_LOCK_WAIT_TIME, TimeUnit.MILLISECONDS)) {
        controller
            .getSpaceEnvironment()
            .getLog()
            .warn(
                String.format("A wait on the activity instance lock for %s has been blocked for"
                    + " %d milliseconds as the instance is in %s", uuid, (controller.getSpaceEnvironment()
                    .getTimeProvider().getCurrentTime() - time), instanceLockState));
      }

      // Check if the current thread has entered more than once.
      if (instanceLock.getHoldCount() > 1) {
        // Locks are tested outside of the finally block that unlocks them so
        // this needs to be unlocked to get the hold count back down.
        instanceLock.unlock();

        throw new SimpleInteractiveSpacesException(String.format(
            "Nested calls to instanceLock protected methods in %s for activity %s",
            ActiveControllerActivity.class.getName(), uuid));
      }

      instanceLockState = newInstanceLockState;

      return true;
    } catch (InterruptedException e) {
      controller.getSpaceEnvironment().getLog()
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
  void setCachedActivityStatus(ActivityStatus cachedActivityStatus) {
    this.cachedActivityStatus = cachedActivityStatus;
  }

  /**
   * Set the state of the live activity.
   *
   * @param status
   *          activity status to set
   */
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

  /**
   * Set the activity status of the live activity, not protected by the instance lock.
   *
   * @param status
   *          activity status to set
   */
  private void setActivityStatusUnprotected(ActivityStatus status) {
    cachedActivityStatus = status;
    if (instance != null) {
      instance.setActivityStatus(status);
    }
  }

  /**
   * Get the activity wrapper for this instance.
   *
   * @return the activity wrapper
   */
  @VisibleForTesting
  public ActivityWrapper getActivityWrapper() {
    return activityWrapper;
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
