/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.event.simple;

/**
 * A very simple event sampler.
 *
 * <p>
 * This will sample at a uniform rate as determined by the sample length. Every
 * sampleLength calls will trigger a sample.
 *
 * @author Keith M. Hughes
 */
public class EventDividerSampler {

  /**
   * How many counts before a sample is allowed.
   */
  private int sampleLength;

  /**
   * The current count.
   */
  private int currentCount;

  /**
   * Construct a new sampler.
   *
   * @param initialSampleLength
   *          the initial sample length
   */
  public EventDividerSampler(int initialSampleLength) {
    this.sampleLength = initialSampleLength;
  }

  /**
   * Reset the sampler.
   */
  public synchronized void reset() {
    currentCount = 0;
  }

  /**
   * Sample the divider.
   *
   * @return {@code true} if the client should sample.
   */
  public synchronized boolean sample() {
    boolean result = currentCount == 0;
    currentCount = (++currentCount) % sampleLength;
    return result;
  }

  /**
   * Change the sample length.
   *
   * @param sampleLength
   *          the new sample length
   */
  public synchronized void setSampleLength(int sampleLength) {
    this.sampleLength = sampleLength;
  }

  /**
   * Get the current sample length.
   *
   * @return the sample length
   */
  public synchronized int getSampleLength() {
    return sampleLength;
  }
}
