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

package interactivespaces.util.math;

/**
 * A collection of useful math utilities.
 *
 * @author Keith M. Hughes
 */
public class MathUtils {

  /**
   * Are the two values equal to each other within a given tolerance?
   *
   * @param v1
   *          the first value
   * @param v2
   *          the second value
   * @param tolerance
   *          the equality tolerance
   *
   * @return {@code true} if the two values are within the given tolerance of
   *         each other
   */
  public static boolean equals(double v1, double v2, double tolerance) {
    return Math.abs(v1 - v2) < tolerance;
  }

  /**
   * Do a linear interpolation between two values.
   *
   * <p>
   * If {@code amount} is {@code 0.0}, the returned value will be {@code v1}. If
   * {@code amount} is {@code 1.0}, the returned value will be {@code v2}.
   *
   * @param v1
   *          the starting value
   * @param v2
   *          the ending value
   * @param amount
   *          the percentage to be between values
   *
   * @return the interpolated value
   */
  public static double linearInterpolate(double v1, double v2, double amount) {
    return (v2 - v1) * amount + v1;
  }
}
