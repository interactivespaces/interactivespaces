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
 * A 3x3 matrix.
 *
 * @author Keith M. Hughes
 */
public class Matrix3 {

  /**
   * The matrix entries.
   */
  double[][] matrix;

  /**
   * Construct an identity matrix.
   */
  public Matrix3() {
    matrix = new double[3][3];
  }

  /**
   * Construct a matrix identical to the given matrix.
   *
   * @param m
   *          the given matrix
   */
  public Matrix3(Matrix3 m) {
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
   * @param e10
   *          value for [1][0]
   * @param e11
   *          value for [1][1]
   * @param e12
   *          value for [1][2]
   * @param e20
   *          value for [2][0]
   * @param e21
   *          value for [2][1]
   * @param e22
   *          value for [2][2]
   */
  public Matrix3(double e00, double e01, double e02, double e10, double e11, double e12, double e20, double e21,
      double e22) {
    this();

    matrix[0][0] = e00;
    matrix[0][1] = e01;
    matrix[0][2] = e02;
    matrix[1][0] = e10;
    matrix[1][1] = e11;
    matrix[1][2] = e12;
    matrix[2][0] = e20;
    matrix[2][1] = e21;
    matrix[2][2] = e22;
  }

  /**
   * Make the current matrix into the identity matrix.
   *
   * @return this matrix
   */
  public Matrix3 identity() {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        matrix[i][j] = (i == j) ? 1.0 : 0.0;
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
  public Matrix3 set(Matrix3 m) {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
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
   * @param q
   *          the quaternion whose value is being taken
   *
   * @return the current matrix
   */
  public Matrix3 set(Quaternion q) {
    matrix[0][0] = 1.0 - 2.0 * (q.y * q.y + q.z * q.z);
    matrix[0][1] = 2.0 * (q.x * q.y - q.w * q.z);
    matrix[0][2] = 2.0 * (q.x * q.z + q.w * q.y);

    matrix[1][0] = 2.0 * (q.x * q.y + q.w * q.z);
    matrix[1][1] = 1.0 - 2.0 * (q.x * q.x + q.z * q.z);
    matrix[1][2] = 2.0 * (q.y * q.z - q.w * q.x);

    matrix[2][0] = 2.0 * (q.x * q.z - q.w * q.y);
    matrix[2][1] = 2.0 * (q.y * q.z + q.w * q.x);
    matrix[2][2] = 1.0 - 2.0 * (q.x * q.x + q.y * q.y);

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
  public Matrix3 setEntry(int row, int col, double value) {
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
  public Matrix3 multiply(Matrix3 m) {
    return new Matrix3(this).multiplySelf(m);
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
  public Matrix3 multiplySelf(Matrix3 m) {
    double[][] result = new double[3][3];
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        result[i][j] = matrix[i][0] * m.matrix[0][j] + matrix[i][1] * m.matrix[1][j] + matrix[i][2] * m.matrix[2][j];
      }
    }

    matrix = result;

    return this;
  }

  /**
   * Calculate the determinant of the matrix.
   *
   * @return the determinant of the matrix
   */
  public double determinant() {
    return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) - matrix[0][1]
        * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]) + matrix[0][2]
        * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
  }
}
