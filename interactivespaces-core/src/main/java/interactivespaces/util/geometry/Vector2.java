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
 * A 2D vector.
 *
 * @author Keith M. Hughes
 */
public class Vector2 {

  /**
   * The first component of the vector.
   */
  double v0;

  /**
   * The second component of the vector.
   */
  double v1;

  /**
   * Construct a new vector with components all equal to 0.
   */
  public Vector2() {
    this(0.0, 0.0);
  }

  /**
   * Construct a new vector with the supplied components.
   *
   * @param v0
   *          the first component
   * @param v1
   *          the second component
   */
  public Vector2(double v0, double v1) {
    this.v0 = v0;
    this.v1 = v1;
  }

  /**
   * Construct a vector whose components are the same as the supplied vector.
   *
   * @param v
   *          the supplied vector
   */
  public Vector2(Vector2 v) {
    this.v0 = v.v0;
    this.v1 = v.v1;
  }

  /**
   * Get the X coordinate of the vector.
   *
   * @return the X coordinate
   */
  public double getX() {
    return v0;
  }

  /**
   * Set the X coordinate of the vector.
   *
   * @param x
   *          the X coordinate
   *
   * @return this vector
   */
  public Vector2 setX(double x) {
    v0 = x;

    return this;
  }

  /**
   * Get the Y coordinate of the vector.
   *
   * @return the Y coordinate
   */
  public double getY() {
    return v1;
  }

  /**
   * Set the Y coordinate of the vector.
   *
   * @param y
   *          the Y coordinate
   *
   * @return this vector
   */
  public Vector2 setY(double y) {
    v1 = y;

    return this;
  }

  /**
   * Get the first component of the vector.
   *
   * @return first component
   */
  public double getV0() {
    return v0;
  }

  /**
   * Set the first component of the vector.
   *
   * @param v
   *          the new component value
   *
   * @return this vector
   */
  public Vector2 setV0(double v) {
    this.v0 = v;

    return this;
  }

  /**
   * Get the second component of the vector.
   *
   * @return the second component
   */
  public double getV1() {
    return v1;
  }

  /**
   * Set the second component of the vector.
   *
   * @param v
   *          the new component value
   *
   * @return this vector
   */
  public Vector2 setV1(double v) {
    this.v1 = v;

    return this;
  }

  /**
   * Set the components of the vector.
   *
   * @param v0
   *          the first component value
   * @param v1
   *          the second component value
   *
   * @return this vector
   */
  public Vector2 set(double v0, double v1) {
    this.v0 = v0;
    this.v1 = v1;

    return this;
  }

  /**
   * Set the components of the vector.
   *
   * @param v
   *          the vector to get the components from
   *
   * @return this vector
   */
  public Vector2 set(Vector2 v) {
    return set(v.v0, v.v1);
  }

  /**
   * Normalize the current vector.
   *
   * @return a new vector with the normalized components
   */
  public Vector2 normalize() {
    return new Vector2(this).normalizeSelf();
  }

  /**
   * Normalize the current vector.
   *
   * @return this vector with the normalized components
   */
  public Vector2 normalizeSelf() {
    double length = getLength();

    v0 /= length;
    v1 /= length;

    return this;
  }

  /**
   * Get the length of the vector.
   *
   * @return the length of the vector
   */
  public double getLength() {
    return Math.sqrt(v0 * v0 + v1 * v1);
  }

  /**
   * Add the supplied vector to this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return a new vector with components are the result of the addition
   */
  public Vector2 add(Vector2 v) {
    return new Vector2(this).addSelf(v);
  }

  /**
   * Add the supplied vector to this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return this vector with components are the result of the addition
   */
  public Vector2 addSelf(Vector2 v) {
    v0 += v.v0;
    v1 += v.v1;

    return this;
  }

  /**
   * Subtract the supplied vector from this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return a new vector with components are the result of the subtraction
   */
  public Vector2 subtract(Vector2 v) {
    return new Vector2(this).subtractSelf(v);
  }

  /**
   * Subtract the supplied vector from this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return this vector with components are the result of the subtraction
   */
  public Vector2 subtractSelf(Vector2 v) {
    v0 -= v.v0;
    v1 -= v.v1;

    return this;
  }

  /**
   * Get a vector which has every component multiplied by a factor.
   *
   * @param factor
   *          the factor
   *
   * @return a new vector with components that are scaled
   */
  public Vector2 scale(double factor) {
    return new Vector2(this).scaleSelf(factor);
  }

  /**
   * Get a vector which has every component multiplied by a factor.
   *
   * @param factor
   *          the factor
   *
   * @return this vector with components that are scaled
   */
  public Vector2 scaleSelf(double factor) {
    v0 *= factor;
    v1 *= factor;

    return this;
  }

  /**
   * Divide all components of the current vector by a factor.
   *
   * @param factor
   *          the factor by which to divide the components
   *
   * @return a new vector with components equal to the division
   */
  public Vector2 divide(double factor) {
    return new Vector2(this).divideSelf(factor);
  }

  /**
   * Divide all components of the current vector by a factor.
   *
   * @param factor
   *          the factor by which to divide the components
   *
   * @return this vector with components equal to the division
   */
  public Vector2 divideSelf(double factor) {
    v0 /= factor;
    v1 /= factor;

    return this;
  }

  /**
   * Limit the vector to a given magnitude. If the magnitude is less the vector
   * is left alone, if the magnitude is greater, it will be scaled to have a
   * length of the given magnitude.
   *
   * @param magnitude
   *          the magnitude limit
   *
   * @return a new vector with components that are limited
   */
  public Vector2 limit(double magnitude) {
    return new Vector2(this).limitSelf(magnitude);
  }

  /**
   * Limit the vector to a given magnitude. If the magnitude is less the vector
   * is left alone, if the magnitude is greater, it will be scaled to have a
   * length of the given magnitude.
   *
   * @param magnitude
   *          the magnitude limit
   *
   * @return this vector with components that are limited
   */
  public Vector2 limitSelf(double magnitude) {
    double length = getLength();
    if (length > magnitude) {
      double factor = magnitude / length;

      v0 *= factor;
      v1 *= factor;
    }

    return this;
  }

  /**
   * Calculate the square of the Euclidean distance between this vector and the
   * other using all 3 coordinates.
   *
   * @param v
   *          the second vector
   *
   * @return the Euclidean distance
   */
  public double euclideanDistanceSquared(Vector2 v) {
    double xDiff = v0 - v.v0;
    double yDiff = v1 - v.v1;

    return xDiff * xDiff + yDiff * yDiff;
  }

  /**
   * Calculate the Euclidean distance between this vector and the other using
   * all 3 coordinates.
   *
   * @param v
   *          the second vector
   *
   * @return the Euclidean distance
   */
  public double euclideanDistance(Vector2 v) {
    return Math.sqrt(euclideanDistanceSquared(v));
  }

  /**
   * Calculate the square of the Euclidean distance between this vector and the
   * other.
   *
   * @param v
   *          the second vector
   *
   * @return the Euclidean distance
   */
  public double euclideanDistanceSquared2(Vector2 v) {
    double xDiff = v0 - v.v0;
    double yDiff = v1 - v.v1;

    return xDiff * xDiff + yDiff * yDiff;
  }

  /**
   * Calculate the Euclidean distance between this vector and the second using
   * the first 2 coordinates.
   *
   * @param v
   *          the second vector
   *
   * @return the Euclidean distance
   */
  public double euclideanDistance2(Vector2 v) {
    return Math.sqrt(euclideanDistanceSquared2(v));
  }

  /**
   * Is this vector equal to the other?
   *
   * @param v
   *          the second vector
   *
   * @return {@code true} if each component is equal to the corresponding
   *         component.
   */
  public boolean equal(Vector2 v) {
    return v0 == v.v0 && v1 == v.v1;
  }

  /**
   * Is this vectors equal to the other within some fuzz factor?.
   *
   * @param v
   *          the second vector
   * @param fuzz
   *          the fuzz factor
   *
   * @return {@code true} if each component is equal to the corresponding
   *         component within the fuzz factor
   */
  public boolean equal(Vector2 v, double fuzz) {
    return Math.abs(v0 - v.v0) < fuzz && Math.abs(v1 - v.v1) < fuzz;
  }

  @Override
  public String toString() {
    return "Vector2 [v0=" + v0 + ", v1=" + v1 + "]";
  }
}
