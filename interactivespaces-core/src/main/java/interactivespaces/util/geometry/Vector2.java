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

import interactivespaces.util.math.MathUtils;

import java.util.Collection;

/**
 * A 2D vector.
 *
 * @author Keith M. Hughes
 */
public class Vector2 {

  /**
   * Linearly interpolate along the line between {@code vector0} and {@code vector1}.
   *
   * <ul>
   * <li>If {@code amount} is {@code 0}, the value will be {@code  vector0}.</li>
   * <li>If {@code amount} is {@code 1}, the value will be {@code  vector1}.</li>
   * </ul>
   *
   * @param vector0
   *          the origin point
   * @param vector1
   *          the direction point
   * @param amount
   *          the percentage between the origin and the direction
   * @param answer
   *          where to place the answer
   *
   * @return the answer
   */
  public static Vector2 linearInterpolate(Vector2 vector0, Vector2 vector1, double amount, Vector2 answer) {
    answer.v0 = MathUtils.linearInterpolate(vector0.v0, vector1.v0, amount);
    answer.v1 = MathUtils.linearInterpolate(vector0.v1, vector1.v1, amount);

    return answer;
  }

  /**
   * Linearly interpolate along the line between {@code  vector0} and {@code vector1}.
   *
   * <ul>
   * <li>If {@code amount} is {@code 0}, the value will be {@code vector0}.</li>
   * <li>If {@code amount} is {@code 1}, the value will be {@code vector1}.</li>
   * </ul>
   *
   * @param vector0
   *          the origin point
   * @param vector1
   *          the direction point
   * @param amount
   *          the percentage between the origin and the direction
   *
   * @return a new vector containing the answer
   */
  public static Vector2 linearInterpolate(Vector2 vector0, Vector2 vector1, double amount) {
    return linearInterpolate(vector0, vector1, amount, new Vector2());
  }

  /**
   * Calculate the center point of a collection of vectors.
   *
   * <p>
   * This is calculated as the average of each component.
   *
   * @param vectors
   *          the collection of vectors
   * @param answer
   *          the vector in which to place the answer
   *
   * @return the answer
   */
  public static Vector2 calculateVectorsCenter(Collection<Vector2> vectors, Vector2 answer) {
    double sumV0 = 0.0;
    double sumV1 = 0.0;

    for (Vector2 vector : vectors) {
      sumV0 += vector.v0;
      sumV1 += vector.v1;
    }

    answer.set(sumV0 / vectors.size(), sumV1 / vectors.size());

    return answer;
  }

  /**
   * Find the point on the line nearest to the given point.
   *
   * @param linePoint0
   *          one of the points on the line
   * @param linePoint1
   *          the other point on the line
   * @param point
   *          the given point
   *
   * @return the answer in a newly created vector
   */
  public static Vector2 nearestPointOnLine(Vector2 linePoint0, Vector2 linePoint1, Vector2 point) {
    return nearestPointOnLine(linePoint0, linePoint1, point, new Vector2());
  }

  /**
   * Find the point on the line nearest to the given point.
   *
   * @param linePoint0
   *          one of the points on the line
   * @param linePoint1
   *          the other point on the line
   * @param point
   *          the given point
   * @param answer
   *          where the answer will be stored
   *
   * @return the answer
   *
   * @throws ArithmeticException
   *           the line points are the same, so no line is determined
   */
  public static Vector2 nearestPointOnLine(Vector2 linePoint0, Vector2 linePoint1, Vector2 point, Vector2 answer)
      throws ArithmeticException {
    // The algorithm here is simple. We want the intersection of the line
    // perpendicular to the original line which contains the target point of
    // interest. This intersection will be the point on the line closest to the
    // target point.

    // Take the two line points and figure out the
    // equation of the form a x + b y + c = 0.
    // This is done by taking the two line points, putting them in homogenuous
    // coordinates (x, y, 1) and taking their cross product.

    double dx = linePoint1.v0 - linePoint0.v0;
    double dy = linePoint1.v1 - linePoint0.v1;

    if (dx == 0.0 && dy == 0.0) {
      throw new ArithmeticException("Line points are the same");
    }

    // a = -dy
    // b = dx
    double c = linePoint0.v0 * linePoint1.v1 - linePoint0.v1 * linePoint1.v0;

    // Now we need a line that is perpendicular to the above line.
    // This line will be -b x + a y + cp = 0, or
    // dx x + dy y + cp = 0.
    double cp = -dx * point.v0 - dy * point.v1;

    // If we have two lines of the form a x + b y + c = 0, we can treat them
    // as 3 vectors (a,b,c). If we take their cross product, we will have their
    // intersection point.
    // The point on the line closest will be the intersection of the original
    // line with the perpendicular
    // After the cross product we will have
    double w = -dx * dx - dy * dy;

    answer.v0 = (dx * cp - dy * c) / w;
    answer.v1 = (dx * c + dy * cp) / w;

    return answer;
  }

  /**
   * Find the point on the line nearest to the given point.
   *
   * @param line0Point0
   *          one of the points on the first line
   * @param line0Point1
   *          the other point on the first line
   * @param line1Point0
   *          one of the points on the second line
   * @param line1Point1
   *          the other point on the second line
   *
   * @return the answer in a newly created vector
   *
   * @throws ArithmeticException
   *           the lines don't intersect
   */
  public static Vector2 intersectLines(Vector2 line0Point0, Vector2 line0Point1, Vector2 line1Point0,
      Vector2 line1Point1) {
    return intersectLines(line0Point0, line0Point1, line1Point0, line1Point1, new Vector2());
  }

  /**
   * Find the point on the line nearest to the given point.
   *
   * @param line0Point0
   *          one of the points on the first line
   * @param line0Point1
   *          the other point on the first line
   * @param line1Point0
   *          one of the points on the second line
   * @param line1Point1
   *          the other point on the second line
   * @param answer
   *          where the answer will be stored
   *
   * @return the answer
   *
   * @throws ArithmeticException
   *           the lines don't intersect
   */
  public static Vector2 intersectLines(Vector2 line0Point0, Vector2 line0Point1, Vector2 line1Point0,
      Vector2 line1Point1, Vector2 answer) throws ArithmeticException {
    // The algorithm here is simple. Calculate the lines in the form
    // a x + b y + c = 0 for both lines.
    // Treat this then as a 3 vector (a,b,c).
    // Take the cross product of these two vectors.
    // The result is the intersection point in homogeneous coordinates.

    // Take the two line points and figure out the
    // equation of the form a x + b y + c = 0.
    // This is done by taking the two line points, putting them in homogenuous
    // coordinates (x, y, 1) and taking their cross product.

    double a0 = line0Point0.v1 - line0Point1.v1;
    double b0 = line0Point1.v0 - line0Point0.v0;
    double c0 = line0Point0.v0 * line0Point1.v1 - line0Point0.v1 * line0Point1.v0;

    double a1 = line1Point0.v1 - line1Point1.v1;
    double b1 = line1Point1.v0 - line1Point0.v0;
    double c1 = line1Point0.v0 * line1Point1.v1 - line1Point0.v1 * line1Point1.v0;

    double w = a0 * b1 - a1 * b0;
    if (w == 0) {
      throw new ArithmeticException("The lines are parallel");
    }

    answer.v0 = (b0 * c1 - b1 * c0) / w;
    answer.v1 = (a1 * c0 - a0 * c1) / w;

    return answer;
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
   * Get a vector which has every component multiplied by a factor.
   *
   * @param factor0
   *          the factor for the first component
   * @param factor1
   *          the factor for the second component
   *
   * @return a new vector with components that are scaled
   */
  public Vector2 scale(double factor0, double factor1) {
    return new Vector2(this).scaleSelf(factor0, factor1);
  }

  /**
   * Get a vector which has every component multiplied by a factor.
   *
   * @param factor0
   *          the factor for the first component
   * @param factor1
   *          the factor for the second component
   *
   * @return this vector with components that are scaled
   */
  public Vector2 scaleSelf(double factor0, double factor1) {
    v0 *= factor0;
    v1 *= factor1;

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
   * Multiply the current vector by the matrix.
   *
   * @param m
   *          the matrix to multiply by
   *
   * @return a new vector whose components are result of the multiplication
   */
  public Vector2 multiply(Matrix3 m) {
    return new Vector2(this).multiplySelf(m);
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
  public Vector2 multiplySelf(Matrix3 m) {
    double tv0 = v0 * m.matrix[0][0] + v1 * m.matrix[0][1] + m.matrix[0][2];
    double tv1 = v0 * m.matrix[1][0] + v1 * m.matrix[1][1] + m.matrix[1][2];

    double w = v0 * m.matrix[2][0] + v1 * m.matrix[2][1] + m.matrix[2][2];

    v0 = tv0 / w;
    v1 = tv1 / w;

    return this;
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
   * Is this vectors equal to the other within some tolerance?
   *
   * @param v
   *          the second vector
   * @param tolerance
   *          the tolerance for equality
   *
   * @return {@code true} if each component is equal to the corresponding
   *         component within the tolerance factor
   */
  public boolean equal(Vector2 v, double tolerance) {
    return MathUtils.equals(v0, v.v0, tolerance) && MathUtils.equals(v1, v.v1, tolerance);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(v0);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(v1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Vector2 other = (Vector2) obj;
    if (Double.doubleToLongBits(v0) != Double.doubleToLongBits(other.v0)) {
      return false;
    }
    if (Double.doubleToLongBits(v1) != Double.doubleToLongBits(other.v1)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Vector2 [v0=" + v0 + ", v1=" + v1 + "]";
  }
}
