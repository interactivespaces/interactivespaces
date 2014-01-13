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
 * A hand that is being tracked for gestures.
 *
 * @author Keith M. Hughes
 */
public class GestureHand {

  /**
   * ID of the hand.
   */
  private final String id;

  /**
   * A unit vector pointing from the center of the palm toward the fingers.
   */
  private final Vector3 direction;

  /**
   * The center of the palm measured in millimeters from the origin.
   */
  private final Vector3 palmPosition;

  /**
   * The speed of the palm in millimeters per second.
   */
  private final Vector3 palmVelocity;

  /**
   * A vector perpendicular to the plane formed by the palm of the hand. The
   * vector points downward out of the palm.
   */
  private final Vector3 palmNormal;

  /**
   * The center of a sphere fit to the curvature of the hand (as if it were
   * holding a ball).
   */
  private final Vector3 sphereCenter;

  /**
   * The radius of a sphere fit to the curvature of the hand. The radius changes
   * with the shape of the hand.
   */
  private final double sphereRadius;

  /**
   * Construct a hand.
   *
   * @param id
   *          ID of the hand
   * @param palmPosition
   *          position of the palm
   * @param palmVelocity
   *          velocity of the palm
   * @param palmNormal
   *          the normal of the palm as a unit vector
   * @param direction
   *          the direction of the hand which is along the fingers
   * @param sphereCenter
   *          the center of a sphere defined by the curl of the fingers
   * @param sphereRadius
   *          the radius of a sphere defined by the curl of the fingers
   */
  public GestureHand(String id, Vector3 palmPosition, Vector3 palmVelocity, Vector3 palmNormal, Vector3 direction,
      Vector3 sphereCenter, double sphereRadius) {
    this.id = id;
    this.palmPosition = palmPosition;
    this.palmVelocity = palmVelocity;
    this.palmNormal = palmNormal;
    this.direction = direction;
    this.sphereCenter = sphereCenter;
    this.sphereRadius = sphereRadius;
  }

  /**
   * Get the ID of the hand.
   *
   * @return the ID of the hand
   */
  public String getId() {
    return id;
  }

  /**
   * Get the position of the palm.
   *
   * @return the position of the palm in millimeters
   */
  public Vector3 getPalmPosition() {
    return palmPosition;
  }

  /**
   * Get the velocity of the palm.
   *
   * @return the velocity in millimeters per second
   */
  public Vector3 getPalmVelocity() {
    return palmVelocity;
  }

  /**
   * Get the normal vector to the palm.
   *
   * @return the normal vector as a unit vector
   */
  public Vector3 getPalmNormal() {
    return palmNormal;
  }

  /**
   * Get the direction of the hand, which is along the fingers.
   *
   * @return the direction as a unit vector
   */
  public Vector3 getDirection() {
    return direction;
  }

  /**
   * Get the center of the sphere defined by the curvature of the fingers.
   *
   * @return the center position in millimeters
   */
  public Vector3 getSphereCenter() {
    return sphereCenter;
  }

  /**
   * Get the radius of the sphere defined by the curvature of the fingers.
   *
   * @return the radius in millimeters
   */
  public double getSphereRadius() {
    return sphereRadius;
  }

  @Override
  public String toString() {
    return "GestureHand [id=" + id + ", direction=" + direction + ", palmPosition=" + palmPosition + ", palmVelocity="
        + palmVelocity + ", palmNormal=" + palmNormal + ", sphereCenter=" + sphereCenter + ", sphereRadius="
        + sphereRadius + "]";
  }
}
