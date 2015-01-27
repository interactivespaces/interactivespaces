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

package interactivespaces.activity;

import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * An activity for Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public interface Activity extends ActivityControl {

  /**
   * The configuration property which gives the name of the activity.
   */
  String CONFIGURATION_PROPERTY_ACTIVITY_NAME = "space.activity.name";

  /**
   * The configuration property which gives the log level of the activity.
   */
  String CONFIGURATION_PROPERTY_LOG_LEVEL = "space.activity.log.level";

  /**
   * Get the Interactive Spaces environment the activity is running under.
   *
   * @return space environment for this activity
   */
  InteractiveSpacesEnvironment getSpaceEnvironment();

  /**
   * Set the Interactive Spaces environment the activity is running under.
   *
   * @param spaceEnvironment
   *          space environment for this activity
   */
  void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment);

  /**
   * Set the activity configuration.
   *
   * @param configuration
   *          the activity configuration
   */
  void setConfiguration(Configuration configuration);

  /**
   * Get the current activity configuration.
   *
   * @return the current activity configuration
   */
  Configuration getConfiguration();

  /**
   * Get the activity runtime the activity is running under.
   *
   * @return the activity runtime
   */
  ActivityRuntime getActivityRuntime();

  /**
   * Set the activity runtime the activity is running under.
   *
   * @param activityRuntime
   *          the activity runtime
   */
  void setActivityRuntime(ActivityRuntime activityRuntime);

  /**
   * The configuration has been updated.
   *
   * @param update
   *          the full update, will be {@code null} when called during setup,
   *          though the initial activity configuration will be valid
   */
  void updateConfiguration(Map<String, String> update);

  /**
   * Is the activity activated?
   *
   * @return {@code true} if the activity is activated
   */
  boolean isActivated();

  /**
   * Get the activity's file system.
   *
   * @return the activity's file system
   */
  ActivityFilesystem getActivityFilesystem();

  /**
   * Set the activity's file system.
   *
   * @param activityFilesystem
   *          the activity's file system
   */
  void setActivityFilesystem(ActivityFilesystem activityFilesystem);

  /**
   * Set the log the activity should use.
   *
   * @param log
   *          logger to use
   */
  void setLog(Log log);

  /**
   * Get the activity's logger.
   *
   * @return log
   */
  Log getLog();

  /**
   * Get the name of the activity.
   *
   * @return the name of the activity
   */
  String getName();

  /**
   * Set the name of this activity.
   *
   * @param name
   *          the name to set
   */
  void setName(String name);

  /**
   * Get the UUID of the activity.
   *
   * @return the UUID of the activity
   */
  String getUuid();

  /**
   * Set the UUID of this activity.
   *
   * @param uuid
   *          the uuid to set
   */
  void setUuid(String uuid);

  /**
   * Do a check on the activity state.
   */
  void checkActivityState();

  /**
   * What status is the activity in?
   *
   * @return the activity status
   */
  ActivityStatus getActivityStatus();

  /**
   * Set the activity status.
   *
   * @param activityStatus
   *          the new activity status
   */
  void setActivityStatus(ActivityStatus activityStatus);

  /**
   * The activity didn't start. Do any cleanup necessary and clear its status.
   */
  void handleStartupFailure();

  /**
   * Set the activity execution context for the activity.
   *
   * @param context
   *          execution context to use
   */
  void setExecutionContext(ActivityExecutionContext context);

  /**
   * Get the execution context.
   *
   * @return activity execution context
   */
  ActivityExecutionContext getExecutionContext();

  /**
   * Add a new activity listener to the activity.
   *
   * @param listener
   *          the new listener
   */
  void addActivityListener(ActivityListener listener);

  /**
   * Remove an activity listener from the activity.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener
   */
  void removeActivityListener(ActivityListener listener);
}
