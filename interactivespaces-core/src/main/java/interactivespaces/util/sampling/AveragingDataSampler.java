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
 * A data sampler which does smoothing over a specified time window.
 *
 * <p>
 * Samples are kept for the length of a time window and averaged. For a
 * particular point, all values in the window are summed and then divided by the
 * size of the window.
 *
 * <p>
 * This class is not thread safe.
 *
 * @author Keith M. Hughes
 */
public class AveragingDataSampler implements DataSampler {

  /**
   * {@code true} if data is ready for processing.
   */
  private boolean ready;

  /**
   * The size of the window over which data will be averaged.
   */
  private final int sizeWindow;

  /**
   * Where the next samples will be stored.
   */
  private int writePointer = 0;

  /**
   * The samples which have come in. This is a circular buffer.
   */
  private final int[][] samples;

  /**
   * The sum of all samples in their current window.
   */
  private final int[] summedSamples;

  /**
   * The average of the samples across the window.
   */
  private final int[] averagedSamples;

  /**
   * Construct the sampler.
   *
   * @param numberSamples
   *          the number of samples in a snapshot
   * @param sizeWindow
   *          how many snapshots to hold in the averaging window
   */
  public AveragingDataSampler(int numberSamples, int sizeWindow) {
    summedSamples = new int[numberSamples];
    averagedSamples = new int[numberSamples];
    this.sizeWindow = sizeWindow;
    samples = new int[sizeWindow][];
  }

  @Override
  public void addSamples(int[] newSamples) {
    if (ready) {
      int[] sampleWriteArray = samples[writePointer];
      for (int i = 0; i < summedSamples.length; i++) {
        summedSamples[i] += newSamples[i] - sampleWriteArray[i];
        averagedSamples[i] = summedSamples[i] / sizeWindow;
      }
      samples[writePointer] = newSamples;
      writePointer = ++writePointer % sizeWindow;
    } else {
      for (int i = 0; i < summedSamples.length; i++) {
        summedSamples[i] += newSamples[i];
      }
      samples[writePointer] = newSamples;
      writePointer = ++writePointer % sizeWindow;
      if (writePointer == 0) {
        ready = true;
      }
    }
  }

  @Override
  public boolean hasReadyData() {
    return ready;
  }

  @Override
  public int[] getProcessedData() {
    return averagedSamples;
  }
}
