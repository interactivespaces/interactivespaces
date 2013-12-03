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
public class Rectangle2 {

  /**
   * The x coordinate of the rectangle.
   */
  private double x;

  /**
   * The y coordinate of the rectangle.
   */
  private double y;

  /**
   * The width of the rectangle.
   */
  private double width;

  /**
   * The height of the rectangle.
   */
  private double height;

  /**
   * Construct a rectangle.
   *
   * <p>
   * All components will be {@code 0.0}.
   */
  public Rectangle2() {
    this(0.0, 0.0, 0.0, 0.0);
  }

  /**
   * Construct a rectangle.
   *
   * @param rect
   *          the rectangle to copy
   */
  public Rectangle2(Rectangle2 rect) {
    this(rect.x, rect.y, rect.width, rect.height);
  }

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
  public Rectangle2(double x, double y, double width, double height) {
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
   * Set the x coordinate of the rectangle.
   *
   * @param x
   *          the new x coordinate
   */
  public void setX(double x) {
    this.x = x;
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
   * Set the y coordinate of the rectangle.
   *
   * @param y
   *          the new y coordinate
   */
  public void setY(double y) {
    this.y = y;
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
   * Set the width of the rectangle.
   *
   * @param width
   *          the new width
   */
  public void setWidth(double width) {
    this.width = width;
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
   * Set the height of the rectangle.
   *
   * @param height
   *          the new height
   */
  public void setHeight(double height) {
    this.height = height;
  }

  /**
   * Get the bottom left corner of the rectangle.
   *
   * @return a new vector with the bottom left corner
   */
  public Vector2 getBottomLeft() {
    return new Vector2(x, y);
  }

  /**
   * Get the top left corner of the rectangle.
   *
   * @return a new vector with the top left corner
   */
  public Vector2 getTopLeft() {
    return new Vector2(x, y + height);
  }

  /**
   * Get the bottom right corner of the rectangle.
   *
   * @return a new vector with the bottom right corner
   */
  public Vector2 getBottomRight() {
    return new Vector2(x + width, y);
  }

  /**
   * Get the top right corner of the rectangle.
   *
   * @return a new vector with the top right corner
   */
  public Vector2 getTopRight() {
    return new Vector2(x + width, y + height);
  }

  /**
   * Does this rectangle contain the supplied rectangle?
   *
   * @param rect
   *          the supplied rectangle
   *
   * @return {@code true} if inside this rectangle
   */
  public boolean contains(Rectangle2 rect) {
    return containsX(rect.x) && containsX(rect.x + rect.width) && containsY(rect.y) && containsY(rect.y + rect.height);
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

  /**
   * Grow the rectangle to contain the given point.
   *
   * @param newX
   *          x coordinate of new point
   * @param newY
   *          y coordinate of new point
   */
  public void grow(double newX, double newY) {
    if (newX < x) {
      width += x - newX;
      x = newX;
    } else if (x + width < newX) {
      width = newX - x;
    }

    if (newY < y) {
      height += y - newY;
      y = newY;
    } else if (y + height < newY) {
      height = newY - y;
    }
  }

  /**
   * Get the center of the rectangle.
   *
   * @return the center
   */
  public Vector2 getCenter() {
    return new Vector2(x + width / 2, y + height / 2);
  }

  /**
   * Get the area of the rectangle.
   *
   * @return the area of the rectangle
   */
  public double getArea() {
    return width * height;
  }

  /**
   * Is this rectangle equal to the other within some fuzz factor?.
   *
   * @param r
   *          the second rectangle
   * @param fuzz
   *          the fuzz factor
   *
   * @return {@code true} if each component is equal to the corresponding
   *         component within the fuzz factor
   */
  public boolean equal(Rectangle2 r, double fuzz) {
    return Math.abs(x - r.x) < fuzz && Math.abs(y - r.y) < fuzz && Math.abs(height - r.height) < fuzz
        && Math.abs(width - r.width) < fuzz;
  }

  @Override
  public String toString() {
    return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
  }
}
