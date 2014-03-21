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

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Calculate a background for a series of sampled data by calculating the mean
 * value for each sample.
 *
 * @author Keith M. Hughes
 */
public class MeanSampledDataBackgroundCalculator implements SampledDataBackgroundCalculator {

  /**
   * The samples which have been captured indexed by their source.
   */
  private final Map<String, long[]> data = Maps.newHashMap();

  /**
   * The samples which have been captured indexed by their source.
   */
  private final Map<String, Integer> numberFrames = Maps.newHashMap();

  @Override
  public void reset() {
    data.clear();
    numberFrames.clear();
  }

  @Override
  public synchronized void addFrame(String name, int[] samples) {
    long[] currentSamples = data.get(name);
    int currentNumberFrames;
    if (currentSamples == null) {
      currentSamples = new long[samples.length];
      data.put(name, currentSamples);
      currentNumberFrames = 0;
    } else {
      currentNumberFrames = numberFrames.get(name);
    }

    for (int i = 0; i < samples.length; i++) {
      currentSamples[i] += samples[i];
    }

    currentNumberFrames++;
    numberFrames.put(name, currentNumberFrames);
  }

  @Override
  public SampledDataCollection getBackground() {
    SampledDataCollection background = new SampledDataCollection();

    for (Entry<String, long[]> entry : data.entrySet()) {
      int totalNumberFrames = numberFrames.get(entry.getKey());
      long[] sumSamples = entry.getValue();
      int[] samples = new int[sumSamples.length];

      for (int i = 0; i < sumSamples.length; i++) {
        samples[i] = (int) (sumSamples[i] / totalNumberFrames);
      }

      background.setSamples(entry.getKey(), samples);
    }

    return background;
  }
}
