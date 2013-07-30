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
  UNKNOWN("space.controller.state.unknown"),

  /**
   * Attempting a connection to the controller
   */
  CONNECT_ATTEMPT("space.controller.state.connect.attempt"),

  /**
   * Unable to connect to the controller
   */
  CONNECT_FAILURE("space.controller.state.connect.failure"),

  /**
   * The controller is running
   */
  RUNNING("space.controller.state.running");

  /**
   * Message ID for the description.
   */
  private String description;

  SpaceControllerState(String description) {
    this.description = description;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }
}
