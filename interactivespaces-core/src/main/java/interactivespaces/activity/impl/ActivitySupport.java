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
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
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
  private ActivityExecutionContext context;

  /**
   * The controller this activity is running under.
   */
  private SpaceController controller;

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
    return spaceEnvironment;
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public ActivityFilesystem getActivityFilesystem() {
    return activityFilesystem;
  }

  @Override
  public void setActivityFilesystem(ActivityFilesystem activityFilesystem) {
    this.activityFilesystem = activityFilesystem;
  }

  @Override
  public SpaceController getController() {
    return controller;
  }

  @Override
  public void setLog(Log log) {
    this.log = log;
  }

  @Override
  public Log getLog() {
    return log;
  }

  /**
   * Set the controller this activity is running under.
   *
   * @param controller
   *          the controller in charge of this activity
   */
  public void setController(SpaceController controller) {
    this.controller = controller;
  }

  @Override
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;

    setName(configuration.getPropertyString(CONFIGURATION_PROPERTY_ACTIVITY_NAME));
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public void setExecutionContext(ActivityExecutionContext context) {
    this.context = context;
  }

  @Override
  public ActivityExecutionContext getExecutionContext() {
    return context;
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
    return activityStatus;
  }

  /**
   * Set the activity status.
   *
   * @param activityStatus
   *          the new activity status
   */
  protected void setActivityStatus(ActivityStatus activityStatus) {
    ActivityStatus oldStatus = this.activityStatus;
    this.activityStatus = activityStatus;

    activityListeners.signalActivityStatusChange(oldStatus, activityStatus);
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
