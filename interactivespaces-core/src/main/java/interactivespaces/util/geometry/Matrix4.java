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
 * A 4x4 matrix.
 *
 * @author Keith M. Hughes
 */
public class Matrix4 {

  /**
   * The matrix entries.
   */
  double[][] matrix;

  /**
   * Construct a 0 matrix.
   */
  public Matrix4() {
    matrix = new double[4][4];
  }

  /**
   * Construct a matrix with identical values to the given matrix.
   *
   * @param m
   *          the given matrix
   */
  public Matrix4(Matrix4 m) {
    this();

    set(m);
  }

  /**
   * Construct a matrix from the individual components.
   *
   * @param e00
   *          value for [0][0]
   * @param e01
   *          value for [0][1]
   * @param e02
   *          value for [0][2]
   * @param e03
   *          value for [0][3]
   * @param e10
   *          value for [1][0]
   * @param e11
   *          value for [1][1]
   * @param e12
   *          value for [1][2]
   * @param e13
   *          value for [1][3]
   * @param e20
   *          value for [2][0]
   * @param e21
   *          value for [2][1]
   * @param e22
   *          value for [2][2]
   * @param e23
   *          value for [2][3]
   * @param e30
   *          value for [3][0]
   * @param e31
   *          value for [3][1]
   * @param e32
   *          value for [3][2]
   * @param e33
   *          value for [3][3]
   */
  public Matrix4(double e00, double e01, double e02, double e03, double e10, double e11,
      double e12, double e13, double e20, double e21, double e22, double e23, double e30,
      double e31, double e32, double e33) {
    this();
    matrix[0][0] = e00;
    matrix[0][1] = e01;
    matrix[0][2] = e02;
    matrix[0][3] = e03;
    matrix[1][0] = e10;
    matrix[1][1] = e11;
    matrix[1][2] = e12;
    matrix[1][3] = e13;
    matrix[2][0] = e20;
    matrix[2][1] = e21;
    matrix[2][2] = e22;
    matrix[2][3] = e23;
    matrix[3][0] = e30;
    matrix[3][1] = e31;
    matrix[3][2] = e32;
    matrix[3][3] = e33;
  }

  /**
   * Make the current matrix into the identity matrix.
   *
   * @return this matrix
   */
  public Matrix4 identity() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        matrix[i][j] = (i == j) ? 1.0f : 0.0f;
      }
    }

    return this;
  }

  /**
   * Make the current matrix contain the exact same values as the supplied
   * matrix.
   *
   * @param m
   *          the matrix we are setting the current matrix to
   *
   * @return this matrix
   */
  public Matrix4 set(Matrix4 m) {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        matrix[i][j] = m.matrix[i][j];
      }
    }

    return this;
  }

  /**
   * Set the current matrix to be a representation of a quaternion.
   *
   * <p>
   * The matrix will be inconsistent if the quaternion does not have a length of
   * 1.
   *
   * <p>
   * The components of the fourth row and column will be 0, except for the 4th
   * component of the diagonal.
   *
   * @param q
   *          the quaternion whose value is being taken
   *
   * @return the current matrix
   */
  public Matrix4 set(Quaternion q) {
    matrix[0][0] = 1.0f - 2.0f * (q.y * q.y + q.z * q.z);
    matrix[0][1] = 2.0f * (q.x * q.y - q.w * q.z);
    matrix[0][2] = 2.0f * (q.x * q.z + q.w * q.y);
    matrix[0][3] = 0.0f;

    matrix[1][0] = 2.0f * (q.x * q.y + q.w * q.z);
    matrix[1][1] = 1.0f - 2.0f * (q.x * q.x + q.z * q.z);
    matrix[1][2] = 2.0f * (q.y * q.z - q.w * q.x);
    matrix[1][3] = 0.0f;

    matrix[2][0] = 2.0f * (q.x * q.z - q.w * q.y);
    matrix[2][1] = 2.0f * (q.y * q.z + q.w * q.x);
    matrix[2][2] = 1.0f - 2.0f * (q.x * q.x + q.y * q.y);
    matrix[2][3] = 0.0f;

    matrix[3][0] = 0.0f;
    matrix[3][1] = 0.0f;
    matrix[3][2] = 0.0f;
    matrix[3][3] = 1.0f;

    return this;
  }

  /**
   * Set a specific element of the matrix.
   *
   * @param row
   *          the element's row
   * @param col
   *          the element's column
   * @param value
   *          the new value for the element
   *
   * @return this matrix
   */
  public Matrix4 setEntry(int row, int col, double value) {
    matrix[row][col] = value;

    return this;
  }

  /**
   * Multiply the current matrix by the given matrix.
   *
   * @param m
   *          the matrix being multiplied by
   *
   * @return a new matrix given the result of the multiplication
   */
  public Matrix4 multiply(Matrix4 m) {
    return new Matrix4(this).multiplySelf(m);
  }

  /**
   * Multiply the current matrix by the given matrix and set the current matrix
   * to have the results.
   *
   * @param m
   *          the matrix being multiplied by
   *
   * @return the current matrix
   */
  public Matrix4 multiplySelf(Matrix4 m) {
    double[][] result = new double[4][4];
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        result[i][j] =
            matrix[i][0] * m.matrix[0][j] + matrix[i][1] * m.matrix[1][j] + matrix[i][2]
                * m.matrix[2][j] + matrix[i][3] * m.matrix[3][j];
      }
    }

    matrix = result;

    return this;
  }
}
