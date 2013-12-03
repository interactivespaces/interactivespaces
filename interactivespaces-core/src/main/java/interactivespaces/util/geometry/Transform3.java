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

import java.util.List;

/**
 * A 3D transform.
 *
 * @author Keith M. Hughes
 */
public class Transform3 {

  /**
   * The X axis.
   */
  private static final Vector3 X_AXIS = Vector3.newXAxis();

  /**
   * The Y axis.
   */
  private static final Vector3 Y_AXIS = Vector3.newYAxis();

  /**
   * The Z axis.
   */
  private static final Vector3 Z_AXIS = Vector3.newZAxis();

  /**
   * The transform matrix.
   */
  private final Matrix4 transform;

  /**
   * A temporary matrix for composing transforms.
   */
  private final Matrix4 temp;

  /**
   * Construct the transform currently as an identity transform.
   */
  public Transform3() {
    transform = new Matrix4().identity();

    temp = new Matrix4();
  }

  /**
   * Transform the given vector according to the current transform.
   *
   * @param v
   *        the given vector
   *
   * @return a newly constructed vector transformed
   */
  public Vector3 transform(Vector3 v) {
    return v.multiply(transform);
  }

  /**
   * Transform the given vector according to the current transform.
   *
   * @param v
   *        the given vector
   *
   * @return the given vector with its coordinates transformed
   */
  public Vector3 transformSelf(Vector3 v) {
    return v.multiplySelf(transform);
  }

  /**
   * Transform the given list of vectors according to the current transform.
   *
   * <p>
   * The original list of the original vectors is returned with just a new set
   * of coordinates in the vectors.
   *
   * @param vectors
   *          the given vectors
   *
   * @return this transform
   */
  public Transform3 transformSelf(List<Vector3> vectors) {
    for (Vector3 v : vectors) {
      transformSelf(v);
    }

    return this;
  }

  /**
   * Reset the transform to an identify transform.
   *
   * @return this transform
   */
  public Transform3 reset() {
    identity();

    return this;
  }

  /**
   * Multiply the transform by the supplied matrix.
   *
   * @param m
   *          the supplied matrix
   *
   * @return this transform
   */
  public Transform3 multiply(Matrix4 m) {
    transform.multiplySelf(m);

    return this;
  }

  /**
   * Set the transform to the supplied matrix.
   *
   * @param m
   *          the supplied matrix
   *
   * @return this transform
   */
  public Transform3 set(Matrix4 m) {
    transform.set(m);

    return this;
  }

  /**
   * Get the current transform matrix.
   *
   * @return the current transform matrix
   */
  public Matrix4 get() {
    return transform;
  }

  /**
   * Make the current transform the identity transform.
   *
   * @return this transform
   */
  public Transform3 identity() {
    transform.identity();

    return this;
  }

  /**
   * Translate by a given set of coordinates.
   *
   * @param tx
   *          the amount along the x axis
   * @param ty
   *          the amount along the y axis
   * @param tz
   *          the amount along the z axis
   *
   * @return this transform
   */
  public Transform3 translate(double tx, double ty, double tz) {
    temp.identity().setEntry(0, 3, tx).setEntry(1, 3, ty).setEntry(2, 3, tz);

    return multiply(temp);
  }

  /**
   * Scale the current transform by each of the following scales.
   *
   * @param sx
   *          the scale in x
   * @param sy
   *          the scale in y
   * @param sz
   *          the scale in z
   *
   * @return this transform
   */
  public Transform3 scale(double sx, double sy, double sz) {
    temp.identity().setEntry(0, 0, sx).setEntry(1, 1, sy).setEntry(2, 2, sz);

    return multiply(temp);
  }

  /**
   * Scale the transform by the corresponding components of the vector.
   *
   * @param s
   *          the scaling vector
   *
   * @return this transform
   */
  public Transform3 scale(Vector3 s) {
    return scale(s.getV0(), s.getV1(), s.getV2());
  }

  /**
   * Translate the current transform by the scaled vector.
   *
   * @param v
   *          the base vector for the translation
   * @param scale
   *          the factor by which the vector will be scaled
   *
   * @return this transform
   */
  public Transform3 translate(Vector3 v, double scale) {
    return translate(scale * v.getV0(), scale * v.getV1(), scale * v.getV2());
  }

  /**
   * Translate the transform by the supplied vector.
   *
   * @param v
   *          the vector by which to translate
   *
   * @return this transform
   */
  public Transform3 translate(Vector3 v) {
    return translate(v.getV0(), v.getV1(), v.getV2());
  }

  /**
   * Rotate the transform around a given axis by a given angle.
   *
   * @param axis
   *          the rotation axis
   * @param angle
   *          the angle in radians
   *
   * @return this transform
   */
  public Transform3 rotate(Vector3 axis, double angle) {
    Vector3 rotationAxis = axis.normalize();

    Quaternion q = new Quaternion(rotationAxis, angle);
    temp.set(q);

    return multiply(temp);
  }

  /**
   * Rotate the transform by the given quaternion.
   *
   * @param q
   *          the quaternion
   *
   * @return this transform
   */
  public Transform3 rotate(Quaternion q) {
    temp.set(q);

    return multiply(temp);
  }

  /**
   * Rotate the transformation around the X axis by a given angle.
   *
   * @param angle
   *          the rotation angle in radians
   *
   * @return the current transform
   */
  public Transform3 rotateX(double angle) {
    return rotate(X_AXIS, angle);
  }

  /**
   * Rotate the transformation around the Y axis by a given angle.
   *
   * @param angle
   *          the rotation angle in radians
   *
   * @return the current transform
   */
  public Transform3 rotateY(double angle) {
    return rotate(Y_AXIS, angle);
  }

  /**
   * Rotate the transformation around the Z axis by a given angle.
   *
   * @param angle
   *          the rotation angle in radians
   *
   * @return the current transform
   */
  public Transform3 rotateZ(double angle) {
    return rotate(Z_AXIS, angle);
  }
}
