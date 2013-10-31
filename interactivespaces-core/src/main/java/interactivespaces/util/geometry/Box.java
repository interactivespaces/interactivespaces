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

package interactivespaces.util.geometry;

/**
 * A 3 dimensional box.
 *
 * <ul>
 * <li>The width of the box goes in the direction of increasing x.</li>
 * <li>The height of the box goes in the direction of increasing y.</li>
 * <li>The depth of the box goes in increasing z.</li>
 * </ul>
 *
 * @author Keith M. Hughes
 */
public class Box {

  /**
   * The x coordinate of the box.
   */
  private final double x;

  /**
   * The y coordinate of the box.
   */
  private final double y;

  /**
   * The z coordinate of the box.
   */
  private final double z;

  /**
   * The width of the box.
   */
  private final double width;

  /**
   * The depth of the box.
   */
  private final double depth;

  /**
   * The height of the box.
   */
  private final double height;

  /**
   * Construct a box.
   *
   * @param x
   *          the x coordinate of the box
   * @param y
   *          the y coordinate of the box
   * @param z
   *          the z coordinate of the box
   * @param width
   *          the width of the box
   * @param depth
   *          the depth of the box
   * @param height
   *          the height of the box
   */
  public Box(double x, double y, double z, double width, double depth, double height) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.depth = depth;
    this.height = height;
  }

  /**
   * Get the x coordinate of the box.
   *
   * @return the x coordinate
   */
  public double getX() {
    return x;
  }

  /**
   * Get the y coordinate of the box.
   *
   * @return the y coordinate
   */
  public double getY() {
    return y;
  }

  /**
   * Get the z coordinate of the box.
   *
   * @return the z coordinate
   */
  public double getZ() {
    return z;
  }

  /**
   * Get the width of the box.
   *
   * @return the width
   */
  public double getWidth() {
    return width;
  }

  /**
   * Get the depth of the box.
   *
   * @return the depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Get the height of the box.
   *
   * @return the height
   */
  public double getHeight() {
    return height;
  }

  /**
   * Does the box contain the point found in the x and y components of the
   * vector?
   *
   * @param v
   *          the vector whose components are being checked
   *
   * @return {@code true} if inside the box
   */
  public boolean contains(Vector3 v) {
    return contains(v.v0, v.v1, v.v2);
  }

  /**
   * Does the box contain the point described?
   *
   * @param cx
   *          the x coordinate of the point
   * @param cy
   *          the y coordinate of the point
   * @param cz
   *          the z coordinate of the point
   *
   * @return {@code true} if inside the box
   */
  public boolean contains(double cx, double cy, double cz) {
    return (x <= cx && cx <= x + width) && (y <= cy && cy <= y + height)
        && (z <= cz && cz <= z + depth);
  }
}
