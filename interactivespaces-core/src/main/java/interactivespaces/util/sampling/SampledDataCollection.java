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

/**
 * A collection of data samples from a collection of named sources.
 *
 * @author Keith M. Hughes
 */
public class SampledDataCollection {

  /**
   * The collection of samples mapped by source names.
   */
  private final Map<String, int[]> data;

  /**
   * Construct a new sampled data collection with no data as of yet.
   */
  public SampledDataCollection() {
    this.data = Maps.newHashMap();
  }

  /**
   * Construct a new sampled data collection.
   *
   * @param data
   *          the data
   */
  public SampledDataCollection(Map<String, int[]> data) {
    this.data = data;
  }

  /**
   * Clear out the collection.
   */
  public void clear() {
    data.clear();
  }

  /**
   * Set the samples for a given source.
   *
   * @param source
   *          name of the source
   * @param samples
   *          the samples for the source
   */
  public void setSamples(String source, int[] samples) {
    data.put(source, samples);
  }

  /**
   * Get the samples for a particular source.
   *
   * @param name
   *          the name of the source
   *
   * @return the samples for the source, or {@code null} if the source had no
   *         samples
   */
  public int[] getSamples(String name) {
    return data.get(name);
  }

  /**
   * Get the underlying data map.
   *
   * @return the underlying data map
   */
  Map<String, int[]> getData() {
    return data;
  }
}
