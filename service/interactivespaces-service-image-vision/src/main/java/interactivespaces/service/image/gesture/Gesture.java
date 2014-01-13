/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.image.gesture;

/**
 * A gesture.
 *
 * <p>
 * Gesture types will be different depending on which service implementation is
 * used as some hardware/software combinations will recognize some gesture types
 * and not others. For this reason, the gesture type is a string. Common
 * gestures will be described.
 *
 * @author Keith M. Hughes
 */
public class Gesture {

  /**
   * ID of the gesture.
   */
  private final String id;

  /**
   * Type of the gesture.
   */
  private final String type;

  /**
   * The state of the gesture.
   */
  private final GestureState state;

  /**
   * Duration since the start of the gesture in microseconds.
   */
  private final double duration;

  /**
   * Construct a gesture.
   *
   * @param id
   *          ID of the gesture
   * @param type
   *          the type of the gesture
   * @param state
   *          the current state of the gesture
   * @param duration
   *          how long the gesture has been happening, in microseconds
   */
  public Gesture(String id, String type, GestureState state, double duration) {
    this.id = id;
    this.type = type;
    this.state = state;
    this.duration = duration;
  }

  /**
   * Get the ID of the gesture.
   *
   * @return the ID of the gesture
   */
  public String getId() {
    return id;
  }

  /**
   * Get the type of the gesture.
   *
   * @return the type of the gesture
   */
  public String getType() {
    return type;
  }

  /**
   * Get the current state of the gesture.
   *
   * @return the current state
   */
  public GestureState getState() {
    return state;
  }

  /**
   * Get the amount of time the gesture has been going on.
   *
   * @return the time in microseconds
   */
  public double getDuration() {
    return duration;
  }

  @Override
  public String toString() {
    return "Gesture [id=" + id + ", type=" + type + ", state=" + state + ", duration=" + duration + "]";
  }

  /**
   * State of the gesture.
   *
   * @author Keith M. Hughes
   */
  public enum GestureState {

    /**
     * The gesture is starting.
     */
    START,

    /**
     * The gesture is in progress.
     */
    UPDATE,

    /**
     * The gesture has stopped.
     */
    STOP
  }
}
