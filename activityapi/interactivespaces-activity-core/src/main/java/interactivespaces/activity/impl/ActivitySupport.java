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

package interactivespaces.activity.impl;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityListenerCollection;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * Common support for building an Interactive Spaces activity.
 *
 * @author Keith M. Hughes
 */
public abstract class ActivitySupport implements Activity {

  /**
   * The activity installation directory.
   */
  private ActivityFilesystem activityFilesystem;

  /**
   * Name of the activity.
   */
  private String name;

  /**
   * UUID of the activity.
   */
  private String uuid;

  /**
   * The activity configuration.
   */
  private Configuration configuration;

  /**
   * The current state of the activity.
   */
  private ActivityStatus activityStatus = new ActivityStatus(ActivityState.READY, null);

  /**
   * The collection of all activity listeners.
   */
  private ActivityListenerCollection activityListeners = new ActivityListenerCollection(this);

  /**
   * The context for executing the activity.
   */
  private ActivityExecutionContext executionContext;

  /**
   * The runtime this activity is running under.
   */
  private ActivityRuntime activityRuntime;

  /**
   * The Interactive Spaces environment the activity will run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The activity specific logger.
   */
  private Log log;

  @Override
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    if (spaceEnvironment != null) {
      return spaceEnvironment;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The space environment has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public ActivityFilesystem getActivityFilesystem() {
    if (activityFilesystem != null) {
      return activityFilesystem;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The activity filesystem has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void setActivityFilesystem(ActivityFilesystem activityFilesystem) {
    this.activityFilesystem = activityFilesystem;
  }

  @Override
  public ActivityRuntime getActivityRuntime() {
    if (activityRuntime != null) {
      return activityRuntime;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The activity runtime has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void setLog(Log log) {
    this.log = log;
  }

  @Override
  public Log getLog() {
    if (log != null) {
      return log;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The activity logger has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void setActivityRuntime(ActivityRuntime activityRuntime) {
    this.activityRuntime = activityRuntime;
  }

  @Override
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;

    setName(configuration.getPropertyString(CONFIGURATION_PROPERTY_ACTIVITY_NAME));
  }

  @Override
  public Configuration getConfiguration() {
    if (configuration != null) {
      return configuration;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The configuration has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public String getName() {
    if (name != null) {
      return name;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The activity name has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getUuid() {
    if (uuid != null) {
      return uuid;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The uuid has not been set yet. Are you accessing in the constructor?");
    }

  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public void setExecutionContext(ActivityExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  @Override
  public ActivityExecutionContext getExecutionContext() {
    if (executionContext != null) {
      return executionContext;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The activity execution context has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void addActivityListener(ActivityListener listener) {
    activityListeners.addListener(listener);
  }

  @Override
  public void removeActivityListener(ActivityListener listener) {
    activityListeners.removeListener(listener);
  }

  @Override
  public ActivityStatus getActivityStatus() {
    if (activityStatus != null) {
      return activityStatus;
    } else {
      throw new SimpleInteractiveSpacesException(
          "The activity status has not been set yet. Are you accessing in the constructor?");
    }
  }

  @Override
  public void setActivityStatus(ActivityStatus activityStatus) {
    activityStatus = potentiallyModifyStatus(activityStatus);

    ActivityStatus oldStatus = this.activityStatus;
    this.activityStatus = activityStatus;

    activityListeners.signalActivityStatusChange(oldStatus, activityStatus);
  }

  /**
   * Potentially modify the status of the activity.
   *
   * <p>
   * This can be used to add information to the status that may not have been available to whoever created the status
   * initially, e.g. full runtime detail of the activity.
   *
   * @param activityStatus
   *          the status
   *
   * @return either a new status with modifications or the original status if no changes were made
   */
  protected ActivityStatus potentiallyModifyStatus(ActivityStatus activityStatus) {
    // Default is no rewrite
    return activityStatus;
  }

  /**
   * Set the activity state.
   *
   * @param state
   *          the new activity status
   */
  protected void setActivityStatus(ActivityState state) {
    setActivityStatus(state, null, null);
  }

  /**
   * Set the activity state.
   *
   * <p>
   * The exception field will be {@code null}.
   *
   * @param state
   *          new status of the activity.
   * @param description
   *          new description of the activity, can be {@code null}.
   */
  protected void setActivityStatus(ActivityState state, String description) {
    setActivityStatus(state, description, null);
  }

  /**
   * Set the activity state.
   *
   * @param state
   *          new status of the activity
   * @param description
   *          new description of the activity. can be {@code null}
   * @param exception
   *          exception that occurred, can be {@code null}
   */
  protected void setActivityStatus(ActivityState state, String description, Throwable exception) {
    setActivityStatus(new ActivityStatus(state, description, exception));
  }

  /**
   * Log an error from an exception.
   *
   * @param message
   *          the base message
   * @param e
   *          the exception
   */
  protected void logException(String message, Throwable e) {
    if (e instanceof SimpleInteractiveSpacesException) {
      getLog().error(message + ": " + ((SimpleInteractiveSpacesException) e).getCompoundMessage());
    } else {
      getLog().error(message, e);
    }
  }
}
