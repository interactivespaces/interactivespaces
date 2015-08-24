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

package interactivespaces.controller;

/**
 * Current known state of the controller.
 *
 * @author Keith M. Hughes
 */
public enum SpaceControllerState {

  /**
   * Don't know the state.
   */
  UNKNOWN("space.controller.state.unknown", false, false),

  /**
   * Attempting a connection to the space controller.
   */
  CONNECT_ATTEMPT("space.controller.state.connect.attempt", true, false),

  /**
   * Unable to connect to the space controller.
   */
  CONNECT_FAILURE("space.controller.state.connect.failure", false, true),

  /**
   * The space controller is connected.
   */
  RUNNING("space.controller.state.running", false, false),

  /**
   * The space controller's connection has been lost.
   */
  CONNECTION_LOST("space.controller.state.connection.lost", false, true);

  /**
   * Message ID for the description.
   */
  private String description;

  /**
   * {@code true} if this is a transitional state.
   */
  private boolean transitional;

  /**
   * {@code true} if this is an error state.
   */
  private boolean error;

  /**
   * Construct a new state.
   *
   * @param description
   *          the key for the state description
   * @param transitional
   *          {@code true} if this is a transitional state
   * @param error
   *          {@code true} if this is an error state
   */
  SpaceControllerState(String description, boolean transitional, boolean error) {
    this.description = description;
    this.transitional = transitional;
    this.error = error;
  }

  /**
   * Get the description key.
   *
   * @return the description key
   */
  public String getDescription() {
    return description;
  }

  /**
   * Is this a transitional state?
   *
   * @return {@code true} if this is a transitional state
   */
  public boolean isTransitional() {
    return transitional;
  }

  /**
   * is this an error state?
   *
   * @return {@code true} if this is an error state
   */
  public boolean isError() {
    return error;
  }
}
