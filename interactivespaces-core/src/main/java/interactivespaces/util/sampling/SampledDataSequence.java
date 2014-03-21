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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A sequence of data sampled from a variety of named sources.
 *
 * @author Keith M. Hughes
 */
public class SampledDataSequence {

  /**
   * The samples which have been captured indexed by their source.
   */
  private final List<SampledDataFrame> frames = Lists.newArrayList();

  /**
   * Clear the sequence out.
   */
  public void reset() {
    frames.clear();
  }

  /**
   * Add in a new frame of samples.
   *
   * @param source
   *          name of the source of the samples
   * @param samples
   *          the samples
   * @param timestamp
   *          timestamp of the frame
   */
  public synchronized void addFrame(String source, int[] samples, long timestamp) {
    frames.add(new SampledDataFrame(source, samples, timestamp));
  }

  /**
   * Get the frames of the sequence.
   *
   * @return the frames of the sequence
   */
  public List<SampledDataFrame> getFrames() {
    return frames;
  }

  /**
   * A frame of sampled data. The frame consists of a name for the source of
   * data and a collection of sample values.
   *
   * @author Keith M. Hughes
   */
  public static class SampledDataFrame {
    /**
     * The source for the frame.
     */
    private final String source;

    /**
     * The samples for the frame.
     */
    private final int[] samples;

    /**
     * Timestamp of the frame.
     */
    private final long timestamp;

    /**
     * Construct a new frame.
     *
     * @param source
     *          the name of the sample source
     * @param samples
     *          the samples from the source
     * @param timestamp
     *          the timestamp of when the data was captured
     */
    public SampledDataFrame(String source, int[] samples, long timestamp) {
      this.source = source;
      this.samples = samples;
      this.timestamp = timestamp;
    }

    /**
     * Get the source of the frame.
     *
     * @return the source of the frame
     */
    public String getSource() {
      return source;
    }

    /**
     * Get the samples from the frame.
     *
     * @return the samples from the frame
     */
    public int[] getSamples() {
      return samples;
    }

    /**
     * Get the timestamp for the frame.
     *
     * @return the timestamp for the frame
     */
    public long getTimestamp() {
      return timestamp;
    }
  }
}
