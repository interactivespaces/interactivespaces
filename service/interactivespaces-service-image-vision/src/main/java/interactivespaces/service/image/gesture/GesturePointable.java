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

import interactivespaces.util.geometry.Vector3;

/**
 * An object which can point. This could be a finger, a stylus, etc.
 *
 * @author Keith M. Hughes
 */
public class GesturePointable {

  /**
   * The ID of the pointable.
   */
  private final String id;

  /**
   * The position of the tip of the pointable. Coordinates in millimeters from
   * the gesture camera origin.
   */
  private final Vector3 tipPosition;

  /**
   * The direction of the pointable. This gives the unit vector.
   */
  private final Vector3 direction;

  /**
   * The velocity of the tip of the pointable. In millimeters per second.
   */
  private final Vector3 tipVelocity;

  /**
   * The length of the pointable. In millimeters.
   */
  private final double length;

  /**
   * {@code true} if the pointable is considered a tool.
   */
  private final boolean tool;

  /**
   * Construct a pointable.
   *
   * @param id
   *          ID of the pointable
   * @param tipPosition
   *          position of the pointable
   * @param direction
   *          the direction the pointable is pointing as a unit vector
   * @param tipVelocity
   *          the velocity of the tip
   * @param length
   *          the length of the pointable
   * @param tool
   *          {@code true} if the pointable is likely a tool and not a finger
   */
  public GesturePointable(String id, Vector3 tipPosition, Vector3 direction, Vector3 tipVelocity, double length,
      boolean tool) {
    this.id = id;
    this.tipPosition = tipPosition;
    this.direction = direction;
    this.tipVelocity = tipVelocity;
    this.length = length;
    this.tool = tool;
  }

  /**
   * Get the ID of the pointable.
   *
   * @return the ID of the pointable
   */
  public String getId() {
    return id;
  }

  /**
   * Get the direction of the tip of the pointable.
   *
   * @return the direction as a unit vector
   */
  public Vector3 getDirection() {
    return direction;
  }

  /**
   * Get the position of the tip of the pointable.
   *
   * @return the position relative to the gesture camera with coordinates in
   *         millimeters
   */
  public Vector3 getTipPosition() {
    return tipPosition;
  }

  /**
   * Get the velocity of the tip of the pointable.
   *
   * @return the velocity in millimeters per second relative to the gesture
   *         camera
   */
  public Vector3 getTipVelocity() {
    return tipVelocity;
  }

  /**
   * Get the length of the pointable.
   *
   * @return the length in millimeters
   */
  public double getLength() {
    return length;
  }

  /**
   * Is the pointable a tool?
   *
   * @return {@code true} if the pointable is a tool and not a finger
   */
  public boolean isTool() {
    return tool;
  }

  @Override
  public String toString() {
    return "GesturePointable [id=" + id + ", tipPosition=" + tipPosition + ", direction=" + direction
        + ", tipVelocity=" + tipVelocity + ", length=" + length + ", tool=" + tool + "]";
  }
}
