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
 * A 2 dimensional rectangle.
 *
 * <ul>
 * <li>The width of the rectangle goes in the direction of increasing x.</li>
 * <li>The height of the rectangle goes in increasing y.</li>
 * </ul>
 *
 * @author Keith M. Hughes
 */
public class Rectangle {

  /**
   * The x coordinate of the rectangle.
   */
  private final double x;

  /**
   * The y coordinate of the rectangle.
   */
  private final double y;

  /**
   * The width of the rectangle.
   */
  private final double width;

  /**
   * The height of the rectangle.
   */
  private final double height;

  /**
   * Construct a rectangle.
   *
   * @param x
   *          the x coordinate of the rectangle
   * @param y
   *          the y coordinate of the rectangle
   * @param width
   *          the width of the rectangle
   * @param height
   *          the height of the rectangle
   */
  public Rectangle(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Get the x coordinate of the rectangle.
   *
   * @return the x coordinate
   */
  public double getX() {
    return x;
  }

  /**
   * Get the y coordinate of the rectangle.
   *
   * @return the y coordinate
   */
  public double getY() {
    return y;
  }

  /**
   * Get the width of the rectangle.
   *
   * @return the width
   */
  public double getWidth() {
    return width;
  }

  /**
   * Get the height of the rectangle.
   *
   * @return the height
   */
  public double getHeight() {
    return height;
  }

  /**
   * Does the rectangle contain the point found in the x and y components of the
   * vector?
   *
   * @param v
   *          the vector whose components are being checked
   *
   * @return {@code true} if inside the rectangle
   */
  public boolean contains(Vector2 v) {
    return containsX(v.v0) && containsY(v.v1);
  }

  /**
   * Does the rectangle contain the point found in the x and y components of the
   * vector?
   *
   * @param v
   *          the vector whose components are being checked
   *
   * @return {@code true} if inside the rectangle
   */
  public boolean contains(Vector3 v) {
    return containsX(v.v0) && containsY(v.v1);
  }

  /**
   * Does the rectangle contain the point described?
   *
   * @param cx
   *          the x coordinate of the point
   * @param cy
   *          the y coordinate of the point
   *
   * @return {@code true} if inside the rectangle
   */
  public boolean contains(double cx, double cy) {
    return containsX(cx) && containsY(cy);
  }

  /**
   * Does the rectangle contain the given x coordinate?
   *
   * @param cx
   *          the x coordinate to test
   *
   * @return {@code true} if the rectangle contains the given x coordinate
   */
  public boolean containsX(double cx) {
    return x <= cx && cx <= x + width;
  }

  /**
   * Does the rectangle contain the given y coordinate?
   *
   * @param cy
   *          the y coordinate to test
   *
   * @return {@code true} if the rectangle contains the given y coordinate
   */
  public boolean containsY(double cy) {
    return y <= cy && cy <= y + height;
  }
}
