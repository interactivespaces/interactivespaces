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
 * A sampler for data. Data samples from some source are sent into the sampler
 * and some sort of processing happens on it. Examples of the sort of processing
 * would include things like oversampling and other filtering operations,
 *
 * @author Keith M. Hughes
 */
public interface DataSampler {

  /**
   * Add in a new set of data samples.
   *
   * @param newSamples
   *          the new sample data
   */
  void addSamples(int[] newSamples);

  /**
   * Is there processed data available?
   *
   * @return {@code true} if data is available for reading
   */
  boolean hasReadyData();

  /**
   * Get the processed data.
   *
   * <p>
   * This method is only valid if {@link #hasReadyData()} returns {@code true}.
   *
   * @return the processed data
   */
  int[] getProcessedData();
}
