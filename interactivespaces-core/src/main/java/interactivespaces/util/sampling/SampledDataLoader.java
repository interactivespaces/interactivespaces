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

import java.io.File;
import java.util.Map;

/**
 * A loader for sampled data files.
 *
 * <p>
 * Sampled data consists of a collection of sampled data from a variety of
 * sources. The data is indexed by the source name and contains the set of
 * samples from the source.
 *
 * @author Keith M. Hughes
 */
public interface SampledDataLoader {

  /**
   * Save the sampled data.
   *
   * @param dataFile
   *          the file to save the data in
   * @param data
   *          the data to be saved
   */
  void save(File dataFile, Map<String, int[]> data);

  /**
   * Save the sampled data.
   *
   * @param dataFile
   *          the file to save the data in
   * @param data
   *          the data to be saved
   */
  void save(File dataFile, SampledDataCollection data);

  /**
   * Load data into the enclosed data collection.
   *
   * <p>
   * The map is cleared before the data is loaded.
   *
   * @param dataFile
   *          the file to load
   * @param data
   *          where to store the data
   */
  void load(File dataFile, Map<String, int[]> data);

  /**
   * Load data into the enclosed data collection
   *
   * <p>
   * The collection is cleared before the data is loaded.
   *
   * @param dataFile
   *          the file to load
   * @param data
   *          where to store the data
   */
  void load(File dataFile, SampledDataCollection data);
}
