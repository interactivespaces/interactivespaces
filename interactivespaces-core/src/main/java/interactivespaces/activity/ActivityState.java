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


/**
 * State of an activity.
 *
 * @author Keith M. Hughes
 */
public enum ActivityState {

  /**
   * The state of the activity is unknown.
   */
  UNKNOWN("space.activity.state.unknown", false, false, false),

  /**
   * The activity does not exist.
   */
  DOESNT_EXIST("space.activity.state.nonexistent", false, true, false),

  /**
   * There is an attempt to deploy the activity.
   */
  DEPLOY_ATTEMPT("space.activity.state.deployment.attempt", false, false, true),

  /**
   * A deployment attempt for the activity has failed.
   */
  DEPLOY_FAILURE("space.activity.state.deployment.failure", false, true, false),

  /**
   * The activity is ready to start up.
   */
  READY("space.activity.state.ready", false, false, false),

  /**
   * There is an attempt to start up the activity.
   */
  STARTUP_ATTEMPT("space.activity.state.start.attempt", false, false, true),

  /**
   * The activity has failed to start.
   */
  STARTUP_FAILURE("space.activity.state.start.failure", false, true, false),

  /**
   * The activity is running, but not active.
   */
  RUNNING("space.activity.state.running", true, false, false),

  /**
   * There is an attempt to activate the activity.
   */
  ACTIVATE_ATTEMPT("space.activity.state.activate.attempt", true, false, true),

  /**
   * The activity has failed to activate.
   */
  ACTIVATE_FAILURE("space.activity.state.activate.failure", true, true, false),

  /**
   * The activity is now active.
   */
  ACTIVE("space.activity.state.active", true, false, false),

  /**
   * There is an attempt to deactivate the activity.
   */
  DEACTIVATE_ATTEMPT("space.activity.state.deactivate.attempt", true, false, true),

  /**
   * The activity has failed to deactivate.
   */
  DEACTIVATE_FAILURE("space.activity.state.deactivate.failure", true, true, false),

  /**
   * There is an attempt to shut down the activity.
   */
  SHUTDOWN_ATTEMPT("space.activity.state.shutdown.attempt", true, false, true),

  /**
   * The activity has failed to shut down.
   */
  SHUTDOWN_FAILURE("space.activity.state.shutdown.failure", false, true, true),

  /**
   * The activity was running and has crashed.
   */
  CRASHED("space.activity.state.crashed", false, true, false),

  /**
   * There is an attempt to delete the activity.
   */
  DELETE_ATTEMPT("space.activity.state.deletion.attempt", false, false, true);

  /**
   * Text description of the state.
   */
  private String description;

  /**
   * {@code true} if this is a running state.
   */
  private boolean running;

  /**
   * {@code true} if this is an error state.
   */
  private boolean error;

  /**
   * {@code true} if this is a state an activity will not stay in.
   */
  private boolean transitional;

  /**
   * Construct a new activity state.
   *
   * @param description
   *          the description of the state
   * @param running
   *          {@code true} if the activity is running
   * @param error
   *          {@code true} if the state is an error state
   * @param transitional
   *          {@code true} if this is a state an activity will not stay in
   */
  ActivityState(String description, boolean running, boolean error, boolean transitional) {
    this.description = description;
    this.running = running;
    this.error = error;
    this.transitional = transitional;
  }

  /**
   * Get the description key for the state.
   *
   * @return a non-human consumable key which can be used for i18n.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Should the activity be considered running?
   *
   * @return {@code true} if the activity is considered to be running.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Should the activity be considered in error?
   *
   * @return {@code true} if the activity is considered to be in error.
   */
  public boolean isError() {
    return error;
  }

  /**
   * Is this a state an activity will not stay in?
   *
   * @return {@code true} if a transitional state
   */
  public boolean isTransitional() {
    return transitional;
  }
}
