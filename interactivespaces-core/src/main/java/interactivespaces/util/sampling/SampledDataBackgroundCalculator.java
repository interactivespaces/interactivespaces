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
 * A calculator of backgrounds for sampled data.
 *
 * <p>
 * The background of a series of collected data will be values that the data
 * tends to have when there is nothing interesting happening in the signal. This
 * can be, for example, heat signature values from an infrared grid when there
 * is nothing in front of the sensor of interest.
 *
 * @author Keith M. Hughes
 */
public interface SampledDataBackgroundCalculator {

  /**
   * Clear the calculator out.
   */
  void reset();

  /**
   * Add in new samples into the calculator.
   *
   * @param name
   *          name of the source of the samples
   * @param samples
   *          the samples
   */
  void addFrame(String name, int[] samples);

  /**
   * Get the background from the calculator.
   *
   * @return a map of source names to the background for that source
   */
  SampledDataCollection getBackground();
}
