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
 * Quaternions are mathematical entities which are vary useful for certain types
 * of geometric operations.
 *
 * <p>
 * Quaternions with a length of 1 are used to represent rotations in a 3D space
 * without some of the typical problems of tracking rotations, such as gimbel
 * lock. If we have normalized quaternions q1 and q2, q1.multiply(q2) will give
 * the quaternion which represents the rotation of q2 followed by the rotation
 * of q1. Quaternions make it easy to do certain operations, such as interpolate
 * between angles because they do not suffer from gimbol lock but can
 * continuously vary from one angle to another.
 *
 * @author Keith M. Hughes
 */
public class Quaternion {

  /**
   * Create a new identity quaternion.
   *
   * @return the identity quaternion.
   */
  public static final Quaternion newIdentity() {
    return new Quaternion(1, 0, 0, 0);
  }

  /**
   * The w component of the quaternion.
   */
  double w;

  /**
   * The x component of the quaternion.
   */
  double x;

  /**
   * The y component of the quaternion.
   */
  double y;

  /**
   * The z component of the quaternion.
   */
  double z;

  /**
   * Construct a quaternion for the identity rotation.
   */
  public Quaternion() {
    this(1, 0, 0, 0);
  }

  /**
   * Construct a quaternion of the form {@code w+x*i+y*j+z*k}.
   *
   * @param w
   *          the w component
   * @param x
   *          the x component
   * @param y
   *          the y component
   * @param z
   *          the z component
   */
  public Quaternion(double w, double x, double y, double z) {
    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Creates a quaternion with the same value as the supplied quaternion.
   *
   * @param q
   *          the supplied quaternion
   */
  public Quaternion(Quaternion q) {
    this(q.w, q.x, q.y, q.z);
  }

  /**
   * Construct a quaternion where the supplied vector gives the appropriate
   * components.
   *
   * @param v
   *          the vactor supplying the quaternion components
   */
  public Quaternion(Vector4 v) {
    this(v.v0, v.v1, v.v2, v.v3);
  }

  /**
   * Creates a quaternion giving a rotation around a given axis.
   *
   * @param v
   *          the axis for the rotation
   * @param angle
   *          the angle of the rotation, in radians
   */
  public Quaternion(Vector3 v, double angle) {
    double h = Math.sin(angle / 2.0);

    w = Math.cos(angle / 2.0);
    x = h * v.v0;
    y = h * v.v1;
    z = h * v.v2;
  }

  /**
   * Get the w component of the quaternion.
   *
   * @return the w component of the quaternion
   */
  public double getW() {
    return w;
  }

  /**
   * Get the x component of the quaternion.
   *
   * @return the x component of the quaternion
   */
  public double getX() {
    return x;
  }

  /**
   * Get the y component of the quaternion.
   *
   * @return the y component of the quaternion
   */
  public double getY() {
    return y;
  }

  /**
   * Get the z component of the quaternion.
   *
   * @return the z component of the quaternion
   */
  public double getZ() {
    return z;
  }

  /**
   * Get the quaternion as a 4-vector.
   *
   * @return the 4 vector
   */
  public Vector4 getVector4() {
    return new Vector4(w, x, y, z);
  }

  /**
   * Get the length of the quaternion.
   *
   * @return the length of the quaternion
   */
  public double getLength() {
    return Math.sqrt(w * w + x * x + y * y + z * z);
  }

  /**
   * Set the current quaterion to have the same components as the supplied
   * quaternion.
   *
   * @param q
   *          the supplied quaternion
   *
   * @return the current quaternion
   */
  public Quaternion set(Quaternion q) {
    this.w = q.w;
    this.x = q.x;
    this.y = q.y;
    this.z = q.z;

    return this;
  }

  /**
   * Get the current quaternion as a 3x3 matrix.
   *
   * <p>
   * The result will be incorrect if the length of the quaternion is not 1.
   *
   * @return the matrix
   */
  public Matrix3 getMatrix3() {
    return new Matrix3().set(this);
  }

  /**
   * Get the current quaternion as a 4x4 matrix.
   *
   * <p>
   * The result will be incorrect if the length of the quaternion is not 1.
   *
   * @return the matrix
   */
  public Matrix4 getMatrix4() {
    return new Matrix4().set(this);
  }

  /**
   * Normalize the current quaternion.
   *
   * @return a new quaternion which is the normalized form.
   */
  public Quaternion normalize() {
    return new Quaternion(this).normalizeSelf();
  }

  /**
   * Normalize the current quaternion.
   *
   * @return this quaternion in the normalized form.
   */
  public Quaternion normalizeSelf() {
    return this.scaleSelf(1 / getLength());
  }

  /**
   * Is the current quaternion 0?
   *
   * @return {@code true} if all components are 0
   */
  public boolean isZero() {
    return w == 0 && x == 0 && y == 0 && z == 0;
  }

  /**
   * Negate all components of the quaternion.
   *
   * @return a newly constructed quaternion with the negated components.
   */
  public Quaternion negate() {
    return new Quaternion(this).negateSelf();
  }

  /**
   * Negate all components of the quaternion.
   *
   * @return this quaternion with the negated components.
   */
  public Quaternion negateSelf() {
    w = -w;
    x = -x;
    y = -y;
    z = -z;
    return this;
  }

  /**
   * Square the current quaternion.
   *
   * @return a newly constructed quaternion with the components of the square.
   */
  public Quaternion square() {
    return new Quaternion(this).squareSelf();
  }

  /**
   * Square the current quaternion.
   *
   * @return this quaternion with the components of the square.
   */
  public Quaternion squareSelf() {
    return this.multiplySelf(this);
  }

  /**
   * Conjugate the current quaternion, which gives {@code w-x*i-y*j-z*k}.
   *
   * @return a new quaternion with the conjugant values.
   */
  public Quaternion conjugate() {
    return new Quaternion(w, -x, -y, -z);
  }

  /**
   * Conjugate the current quaternion, which gives {@code w-x*i-y*j-z*k}.
   *
   * @return this quaternion with the conjugant values.
   */
  public Quaternion conjugateSelf() {
    x = -x;
    y = -y;
    z = -z;
    return this;
  }

  /**
   * Add the supplied quaternion to the current quaternion.
   *
   * @param q
   *          the supplied quaternion
   *
   * @return a newly constructed quaternion with the components of the addition
   */
  public Quaternion add(Quaternion q) {
    return new Quaternion(this).addSelf(q);
  }

  /**
   * Add the supplied quaternion to the current quaternion.
   *
   * @param q
   *          the supplied quaternion
   *
   * @return this quaternion with the components of the addition
   */
  public Quaternion addSelf(Quaternion q) {
    w += q.w;
    x += q.x;
    y += q.y;
    z += q.z;
    return this;
  }

  /**
   * Subtract the supplied quaternion from the current quaternion.
   *
   * @param q
   *          the supplied quaternion
   *
   * @return a newly constructed quaternion with the components of the
   *         subtraction
   */
  public Quaternion subtract(Quaternion q) {
    return new Quaternion(this).subtractSelf(q);
  }

  /**
   * Subtract the current quaternion from the supplied quaternion.
   *
   * @param q
   *          the supplied quaternion
   *
   * @return the current quaternion with the components of the subtraction
   */
  public Quaternion subtractSelf(Quaternion q) {
    w -= q.w;
    x -= q.x;
    y -= q.y;
    z -= q.z;
    return this;
  }

  /**
   * Scale the current quaternion by a scale factor.
   *
   * @param scale
   *          the scale factor
   *
   * @return a new quaternion with the scaled components
   */
  public Quaternion scale(double scale) {
    return new Quaternion(this).scaleSelf(scale);
  }

  /**
   * Scale the current quaternion by a scale factor.
   *
   * @param scale
   *          the scale factor
   *
   * @return this quaternion with the scaled components
   */
  public Quaternion scaleSelf(double scale) {
    w *= scale;
    x *= scale;
    y *= scale;
    z *= scale;

    return this;
  }

  /**
   * Multiple the current quaternion by another.
   *
   * @param q
   *          the quaternion to multiply by
   *
   * @return a new quaternion with the components of the multiplication
   */
  public Quaternion multiply(Quaternion q) {
    return new Quaternion(this).multiplySelf(q);
  }

  /**
   * Multiple the current quaternion by another.
   *
   * @param q
   *          the quaternion to multiply by
   *
   * @return this quaterion with the components of the multiplication
   */
  public Quaternion multiplySelf(Quaternion q) {
    double tw = this.w;
    double tx = this.x;
    double ty = this.y;
    double tz = this.z;

    this.w = q.w * tw - q.x * tx - q.y * ty - q.z * tz;
    this.x = q.w * tx + q.x * tw + q.y * tz - q.z * ty;
    this.y = q.w * ty - q.x * tz + q.y * tw + q.z * tx;
    this.z = q.w * tz + q.x * ty - q.y * tx + q.z * tw;

    return this;
  }

  /**
   * Get the multiplicative inverse of a quaternion.
   *
   * @return a newly constructed quaternion with the components of the inverse
   *
   * @throws ArithmeticException
   *           if the current quaternion is zero
   */
  public Quaternion inverse() throws ArithmeticException {
    if (isZero()) {
      throw new ArithmeticException();
    }

    return new Quaternion(this).inverseSelf();
  }

  /**
   * Get the multiplicative inverse of a quaternion.
   *
   * @return this quaternion with the components of the inverse
   *
   * @throws ArithmeticException
   *           if the current quaternion is zero
   */
  public Quaternion inverseSelf() throws ArithmeticException {
    if (isZero()) {
      throw new ArithmeticException("Cannot invert a zero quaternion");
    }

    conjugateSelf().scaleSelf(1.0f / getLength());

    return this;
  }

  @Override
  public String toString() {
    return "Quaternion [w=" + w + ", x=" + x + ", y=" + y + ", z=" + z + "]";
  }
}
