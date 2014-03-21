/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.sampling;

/**
 * A collection of useful utilities for working with samples.
 *
 * @author Keith M. Hughes
 */
public class SamplingUtils {

  /**
   * Apply a kernel filter to the samples.
   *
   * <p>
   * The edges are ignored.
   *
   * <p>
   * Kernels give a sequence of offsets in the sample data from the current
   * sample point to say what to put into the computation.
   *
   * @param samples
   *          the samples to run the filter on
   * @param kernel
   *          the kernel representing the filter
   * @param numRows
   *          the number of rows in the samples
   * @param numCols
   *          the number of columns in the samples
   *
   * @return a new array with the filtered data
   */
  public static int[] applyFilter(int[] samples, int[] kernel, int numRows, int numCols) {
    int[] newSamples = new int[samples.length];
    for (int col = 1; col < numCols - 1; col++) {
      int rowStart = 0;
      for (int row = 1; row < numRows - 1; row++) {
        rowStart += numCols;
        int sum = 0;
        int pos = col + rowStart;
        for (int i = 0; i < kernel.length; i++) {
          sum += samples[pos + kernel[i]];
        }
        newSamples[pos] = sum / kernel.length;
      }
    }

    return newSamples;
  }

  /**
   * Apply a kernel filter to the samples.
   *
   * <p>
   * The edges are taken into account and the kernel is truncated if it extends
   * outside of the grid for a given point.
   *
   * <p>
   * Kernels give a sequence of offsets in the sample data from the current
   * sample point to say what to put into the computation.
   *
   * @param samples
   *          the samples to run the filter on
   * @param kernel
   *          the kernel representing the filter
   * @param numRows
   *          the number of rows in the samples
   * @param numCols
   *          the number of columns in the samples
   *
   * @return a new array with the filtered data
   */
  public static int[] applyFilter2(int[] samples, int[] kernel, int numRows, int numCols) {
    int numSamples = numRows * numCols;

    int[] newSamples = new int[samples.length];
    for (int col = 0; col < numCols; col++) {
      int rowStart = -numCols;
      for (int row = 0; row < numRows; row++) {
        rowStart += numCols;
        int sum = 0;
        int pos = col + rowStart;
        int count = 0;
        for (int i = 0; i < kernel.length; i++) {
          int samplePosition = pos + kernel[i];
          if (0 <= samplePosition && samplePosition < numSamples) {
            count++;
            sum += samples[samplePosition];
          }
        }
        newSamples[pos] = sum / count;
      }
    }
    return newSamples;
  }

  /**
   * Generate a Gaussian kernel.
   *
   * @param numRows
   *          the number of rows in the data
   * @param numCols
   *          the number of columns in the data
   * @param weightCenter
   *          the weight the center of the kernel should have
   *
   * @return the Gaussian kernel
   */
  public static int[] generateGaussianKernel(int numRows, int numCols, int weightCenter) {
    int[] kernel = new int[8 + weightCenter];

    kernel[0] = -numCols - 1;
    kernel[1] = -numCols;
    kernel[2] = -numCols + 1;
    kernel[3] = -1;

    int pos = 4 + weightCenter;
    kernel[pos++] = 1;
    kernel[pos++] = numCols - 1;
    kernel[pos++] = numCols;
    kernel[pos] = numCols + 1;

    return kernel;
  }
}
