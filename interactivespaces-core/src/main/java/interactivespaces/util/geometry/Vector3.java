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
 * A 3D vector.
 *
 * @author Keith M. Hughes
 */
public class Vector3 {

  /**
   * Create a vector which represents the X axis.
   *
   * @return the vector for the X axis
   */
  public static Vector3 newXAxis() {
    return new Vector3(1.0f, 0.0f, 0.0f);
  }

  /**
   * Create a vector which represents the Y axis.
   *
   * @return the vector for the Y axis
   */
  public static Vector3 newYAxis() {
    return new Vector3(0.0f, 1.0f, 0.0f);
  }

  /**
   * Create a vector which represents the Z axis.
   *
   * @return the vector for the Z axis
   */
  public static Vector3 newZAxis() {
    return new Vector3(0.0f, 0.0f, 1.0f);
  }

  /**
   * The first component of the vector.
   */
  double v0;

  /**
   * The second component of the vector.
   */
  double v1;

  /**
   * The third component of the vector.
   */
  double v2;

  /**
   * Construct a new vector with components all equal to 0.
   */
  public Vector3() {
    this(0.0, 0.0, 0.0);
  }

  /**
   * Construct a new vector with the supplied components where the 3rd component
   * is 0.
   *
   * @param v0
   *          the first component
   * @param v1
   *          the second component
   */
  public Vector3(double v0, double v1) {
    this(v0, v1, 0.0f);
  }

  /**
   * Construct a new vector with the suppliec components.
   *
   * @param v0
   *          the first component
   * @param v1
   *          the second component
   * @param v2
   *          the third component
   */
  public Vector3(double v0, double v1, double v2) {
    this.v0 = v0;
    this.v1 = v1;
    this.v2 = v2;
  }

  /**
   * Construct a vector whose components are the same as the supplied vector.
   *
   * @param v
   *          the supplied vector
   */
  public Vector3(Vector3 v) {
    this.v0 = v.v0;
    this.v1 = v.v1;
    this.v2 = v.v2;
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
  public Vector3 setX(double x) {
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
  public Vector3 setY(double y) {
    v1 = y;

    return this;
  }

  /**
   * Get the Z coordinate of the vector.
   *
   * @return the Z coordinate
   */
  public double getZ() {
    return v2;
  }

  /**
   * Set the Z coordinate of the vector.
   *
   * @param z
   *          the Z coordinate
   *
   * @return this vector
   */
  public Vector3 setZ(double z) {
    v2 = z;

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
  public Vector3 setV0(double v) {
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
  public Vector3 setV1(double v) {
    this.v1 = v;

    return this;
  }

  /**
   * Get the third component of the vector.
   *
   * @return the third component
   */
  public double getV2() {
    return v2;
  }

  /**
   * Set the third component of the vector.
   *
   * @param v
   *          the new component value
   *
   * @return this vector
   */
  public Vector3 setV2(double v) {
    this.v2 = v;

    return this;
  }

  /**
   * Set the components of the vector.
   *
   * @param v0
   *          the first component value
   * @param v1
   *          the second component value
   * @param v2
   *          the third component value
   *
   * @return this vector
   */
  public Vector3 set(double v0, double v1, double v2) {
    this.v0 = v0;
    this.v1 = v1;
    this.v2 = v2;

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
  public Vector3 set(Vector3 v) {
    return set(v.v0, v.v1, v.v2);
  }

  /**
   * Normalize the current vector.
   *
   * @return a new vector with the normalized components
   */
  public Vector3 normalize() {
    return new Vector3(this).normalizeSelf();
  }

  /**
   * Normalize the current vector.
   *
   * @return this vector with the normalized components
   */
  public Vector3 normalizeSelf() {
    double length = getLength();

    v0 /= length;
    v1 /= length;
    v2 /= length;

    return this;
  }

  /**
   * Get the length of the vector.
   *
   * @return the length of the vector
   */
  public double getLength() {
    return Math.sqrt(v0 * v0 + v1 * v1 + v2 * v2);
  }

  /**
   * Multiply the current vector by the matrix.
   *
   * @param m
   *          the matrix to multiply by
   *
   * @return a new vector whose components are result of the multiplication
   */
  public Vector3 multiply(Matrix4 m) {
    return new Vector3(this).multiplySelf(m);
  }

  /**
   * Multiple the current vector in homogeneous space, setting the current
   * vector to the result.
   *
   * @param m
   *          the vector to multiply by
   *
   * @return this vector whose components are result of the multiplication
   */
  public Vector3 multiplySelf(Matrix4 m) {
    double tv0 = v0 * m.matrix[0][0] + v1 * m.matrix[0][1] + v2 * m.matrix[0][2] + m.matrix[0][3];
    double tv1 = v0 * m.matrix[1][0] + v1 * m.matrix[1][1] + v2 * m.matrix[1][2] + m.matrix[1][3];
    double tv2 = v0 * m.matrix[2][0] + v1 * m.matrix[2][1] + v2 * m.matrix[2][2] + m.matrix[2][3];

    double w = v0 * m.matrix[3][0] + v1 * m.matrix[3][1] + v2 * m.matrix[3][2] + m.matrix[3][3];

    v0 = tv0 / w;
    v1 = tv1 / w;
    v2 = tv2 / w;

    return this;
  }

  /**
   * Add the supplied vector to this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return a new vector with components are the result of the addition
   */
  public Vector3 add(Vector3 v) {
    return new Vector3(this).addSelf(v);
  }

  /**
   * Add the supplied vector to this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return this vector with components are the result of the addition
   */
  public Vector3 addSelf(Vector3 v) {
    v0 += v.v0;
    v1 += v.v1;
    v2 += v.v2;

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
  public Vector3 subtract(Vector3 v) {
    return new Vector3(this).subtractSelf(v);
  }

  /**
   * Subtract the supplied vector from this vector.
   *
   * @param v
   *          the supplied vector
   *
   * @return this vector with components are the result of the subtraction
   */
  public Vector3 subtractSelf(Vector3 v) {
    v0 -= v.v0;
    v1 -= v.v1;
    v2 -= v.v2;

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
  public Vector3 scale(double factor) {
    return new Vector3(this).scaleSelf(factor);
  }

  /**
   * Get a vector which has every component multiplied by a factor.
   *
   * @param factor
   *          the factor
   *
   * @return this vector with components that are scaled
   */
  public Vector3 scaleSelf(double factor) {
    v0 *= factor;
    v1 *= factor;
    v2 *= factor;

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
  public Vector3 divide(double factor) {
    return new Vector3(this).divideSelf(factor);
  }

  /**
   * Divide all components of the current vector by a factor.
   *
   * @param factor
   *          the factor by which to divide the components
   *
   * @return this vector with components equal to the division
   */
  public Vector3 divideSelf(double factor) {
    v0 /= factor;
    v1 /= factor;
    v2 /= factor;

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
  public Vector3 limit(double magnitude) {
    return new Vector3(this).limitSelf(magnitude);
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
  public Vector3 limitSelf(double magnitude) {
    double length = getLength();
    if (length > magnitude) {
      double factor = magnitude / length;

      v0 *= factor;
      v1 *= factor;
      v2 *= factor;
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
  public double euclideanDistanceSquared(Vector3 v) {
    double xDiff = v0 - v.v0;
    double yDiff = v1 - v.v1;
    double zDiff = v2 - v.v2;

    return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
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
  public double euclideanDistance(Vector3 v) {
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
  public double euclideanDistanceSquared2(Vector3 v) {
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
  public double euclideanDistance2(Vector3 v) {
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
  public boolean equal(Vector3 v) {
    return v0 == v.v0 && v1 == v.v1 && v2 == v.v2;
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
  public boolean equal(Vector3 v, double fuzz) {
    return Math.abs(v0 - v.v0) < fuzz && Math.abs(v1 - v.v1) < fuzz && Math.abs(v2 - v.v2) < fuzz;
  }

  @Override
  public String toString() {
    return "Vector3 [v0=" + v0 + ", v1=" + v1 + ", v2=" + v2 + "]";
  }
}
