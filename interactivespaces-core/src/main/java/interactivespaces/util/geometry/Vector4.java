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
 * A 4 component vector.
 *
 * <p>
 * Can be used for things such as homogenous coordinates.
 *
 * @author Keith M. Hughes
 */
public class Vector4 {

  /**
   * First component of the vector.
   */
  double v0;

  /**
   * Second component of the vector.
   */
  double v1;

  /**
   * Third component of the vector.
   */
  double v2;

  /**
   * Fourth component of the vector.
   */
  double v3;

  /**
   * Construct a new vector with components all equal to 0.
   */
  public Vector4() {
    this(0.0, 0.0, 0.0, 0.0);
  }

  /**
   * Create a 4 vector with the given coordinates.
   *
   * @param v0
   *          the first component
   * @param v1
   *          the second component
   * @param v2
   *          the third component
   * @param v3
   *          the fourth component
   */
  public Vector4(double v0, double v1, double v2, double v3) {
    this.v0 = v0;
    this.v1 = v1;
    this.v2 = v2;
    this.v3 = v3;
  }

  /**
   * Create a 4 vector with the given vector's coordinates.
   *
   * @param v
   *          the vector
   */
  public Vector4(Vector4 v) {
    this(v.v0, v.v1, v.v2, v.v3);
  }

  /**
   * Create a 4 vector with the given vector's coordinates. The 4th component
   * will be 1.
   *
   * @param v
   *          the vector
   */
  public Vector4(Vector3 v) {
    this(v.v0, v.v1, v.v2, 1.0);
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
  public Vector4 setX(double x) {
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
  public Vector4 setY(double y) {
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
  public Vector4 setZ(double z) {
    v2 = z;

    return this;
  }

  /**
   * Get the W coordinate of the vector.
   *
   * @return the W coordinate
   */
  public double getW() {
    return v3;
  }

  /**
   * Set the W coordinate of the vector.
   *
   * @param w
   *          the W coordinate
   *
   * @return this vector
   */
  public Vector4 setW(double w) {
    v3 = w;

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
  public Vector4 setV0(double v) {
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
  public Vector4 setV1(double v) {
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
  public Vector4 setV2(double v) {
    this.v2 = v;

    return this;
  }

  /**
   * Get the fourth component of the vector.
   *
   * @return the fourth component
   */
  public double getV3() {
    return v3;
  }

  /**
   * Set the fourth component of the vector.
   *
   * @param v
   *          the new component value
   *
   * @return this vector
   */
  public Vector4 setV3(double v) {
    this.v3 = v;

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
   * @param v3
   *          the fourth component value
   *
   * @return this vector
   */
  public Vector4 set(double v0, double v1, double v2, double v3) {
    this.v0 = v0;
    this.v1 = v1;
    this.v2 = v2;
    this.v3 = v3;

    return this;
  }

  /**
   * Multiply the current vector by the matrix.
   *
   * @param m
   *          the matrix to multiply by
   *
   * @return a new vector whose components are result of the multiplication
   */
  public Vector4 multiply(Matrix4 m) {
    return new Vector4(this).multiplySelf(m);
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
  public Vector4 multiplySelf(Matrix4 m) {
    double tv0 = v0 * m.matrix[0][0] + v1 * m.matrix[0][1] + v2 * m.matrix[0][2] + m.matrix[0][3];
    double tv1 = v0 * m.matrix[1][0] + v1 * m.matrix[1][1] + v2 * m.matrix[1][2] + m.matrix[1][3];
    double tv2 = v0 * m.matrix[2][0] + v1 * m.matrix[2][1] + v2 * m.matrix[2][2] + m.matrix[2][3];

    double tv3 = v0 * m.matrix[3][0] + v1 * m.matrix[3][1] + v2 * m.matrix[3][2] + m.matrix[3][3];

    v0 = tv0;
    v1 = tv1;
    v2 = tv2;
    v3 = tv3;

    return this;
  }
}
