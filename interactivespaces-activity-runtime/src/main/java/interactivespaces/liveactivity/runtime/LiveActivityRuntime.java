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
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.MinimalLiveActivity;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.util.resource.ManagedResource;

import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * A runtime that can run live activities.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityRuntime extends ActivityRuntime, ManagedResource {

  /**
   * Get the logger for the indicated activity.
   *
   * @param activity
   *          activity to log
   * @param configuration
   *          configuration properties
   *
   * @return logger for this activity and configuration
   */
  Log getActivityLog(MinimalLiveActivity activity, Configuration configuration);

  /**
   * Start up all activities in the controller that aren't currently started.
   */
  void startupAllActivities();

  /**
   * Shut down all activities in the controller.
   */
  void shutdownAllActivities();

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to start.
   */
  void startupLiveActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to start.
   */
  void shutdownLiveActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to activate.
   */
  void activateLiveActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to deactivate
   */
  void deactivateLiveActivity(String uuid);

  /**
   * Cause a status check of an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to get the status of
   */
  void statusLiveActivity(String uuid);

  /**
   * Configure the activity.
   *
   * @param uuid
   *          uuid of the activity
   * @param configuration
   *          the configuration request
   */
  void configureLiveActivity(String uuid, Map<String, String> configuration);

  /**
   * Get all live activities installed on this controller.
   *
   * @return all locally installed activities
   */
  List<InstalledLiveActivity> getAllInstalledLiveActivities();

  /**
   * Get an activity by UUID.
   *
   * @param uuid
   *          the UUID of the activity
   *
   * @return the activity with the given UUID, {@code null} if no such activity
   */
  LiveActivityRunner getLiveActivityRunnerByUuid(String uuid);

  /**
   * Prepare an instance of an activity to run.
   *
   * @param liveActivity
   *          information about the live activity whose instance is to be initialized
   * @param activityFilesystem
   *          the filesystem for the activity instance
   * @param instance
   *          the instance of the activity being started up
   * @param configuration
   *          the configuration for the instance
   * @param executionContext
   *          execution context for this activity
   */
  void initializeActivityInstance(MinimalLiveActivity liveActivity, ActivityFilesystem activityFilesystem,
      Activity instance, Configuration configuration, ActivityExecutionContext executionContext);

  /**
   * Clean the temp data folder for a given activity.
   *
   * @param uuid
   *          uuid of the activity
   */
  void cleanLiveActivityTmpData(String uuid);

  /**
   * Clean the permanent data folder for a given activity.
   *
   * @param uuid
   *          uuid of the activity
   */
  void cleanLiveActivityPermanentData(String uuid);

  /**
   * Get a factory for native activities runners.
   *
   * @return the factory to use
   */
  @Override
  NativeActivityRunnerFactory getNativeActivityRunnerFactory();

  /**
   * Get the activity component factory for the controller.
   *
   * @return the factory for activity components
   */
  @Override
  ActivityComponentFactory getActivityComponentFactory();

  /**
   * Set the publisher for live activity status information.
   *
   * @param liveActivityStatusPublisher
   *          the publisher
   */
  void setLiveActivityStatusPublisher(LiveActivityStatusPublisher liveActivityStatusPublisher);
}
