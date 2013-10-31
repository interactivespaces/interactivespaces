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

import com.google.common.annotations.VisibleForTesting;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityControl;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.domain.InstalledLiveActivity;

import java.util.Map;

/**
 * An activity which is live in the controller.
 *
 * @author Keith M. Hughes
 */
public class ActiveControllerActivity implements ActivityControl {

  /**
   * Initial status for an activity
   */
  private static final ActivityStatus INITIAL_ACTIVITY_STATUS = new ActivityStatus(
      ActivityState.READY, "");

  /**
   * UUID of the activity.
   */
  private String uuid;

  /**
   * The locally installed activity for this activity.
   */
  private InstalledLiveActivity installedActivity;

  /**
   * Runner for the activity.
   */
  private ActivityWrapper activityWrapper;

  /**
   * File system for the activity.
   */
  private ActivityFilesystem activityFilesystem;

  /**
   * The activity configuration.
   */
  private LiveActivityConfiguration configuration;

  /**
   * The controller which contains the activity
   *
   * <p>
   * This can be null if an instance hasn't been created yet.
   */
  private SpaceController controller;

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
  private Object instanceLock = new Object();

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
  public ActiveControllerActivity(InstalledLiveActivity installedActivity,
      ActivityWrapper activityWrapper, ActivityFilesystem activityFilesystem,
      LiveActivityConfiguration configuration, SpaceController controller) {
    this.uuid = installedActivity.getUuid();
    this.installedActivity = installedActivity;
    this.activityWrapper = activityWrapper;
    this.activityFilesystem = activityFilesystem;
    this.configuration = configuration;
    this.controller = controller;
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
    configuration.update(update);

    synchronized (instanceLock) {
      if (instance != null && instance.getActivityStatus().getState().isRunning()) {
        instance.updateConfiguration(update);
      }
    }
  }

  @Override
  public void startup() {
    synchronized (instanceLock) {
      if (instance == null) {
        configuration.load();
        instance = activityWrapper.newInstance();
        controller.initializeActivityInstance(installedActivity, activityFilesystem, instance,
            configuration, activityWrapper.newExecutionContext());
        instance.startup();
      } else {
        throw new InteractiveSpacesException(String.format(
            "Attempt to start activity %s which is already started", uuid));
      }
    }
  }

  @Override
  public void shutdown() {
    // Can call shutdown multiple times.
    synchronized (instanceLock) {
      if (instance != null) {
        instance.shutdown();

        // Sample the status after a complete shutdown.
        getActivityStatus();

        instance = null;
      }
    }
  }

  @Override
  public void activate() {
    synchronized (instanceLock) {
      if (instance != null) {
        instance.activate();
      } else {
        throw new InteractiveSpacesException(String.format(
            "Attempt to activate activity %s which is not started", uuid));
      }
    }
  }

  @Override
  public void deactivate() {
    synchronized (instanceLock) {
      if (instance != null) {
        instance.deactivate();
      } else {
        throw new InteractiveSpacesException(String.format(
            "Attempt to deactivate activity %s which is not started", uuid));
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
   * This will be the last known state of the activity. The activity itself will
   * be sampled.
   *
   * @return the state of the activity
   */
  public ActivityStatus getActivityStatus() {
    synchronized (instanceLock) {
      if (instance != null) {
        instance.checkActivityState();

        cachedActivityStatus = instance.getActivityStatus();
      }
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
    synchronized (instanceLock) {
      return instance;
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
  void setCachedActivityStatus(ActivityStatus cachedActivityStatus) {
    this.cachedActivityStatus = cachedActivityStatus;
  }

}
