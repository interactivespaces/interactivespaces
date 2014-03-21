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

/**
 * A loader for data sequences.
 *
 * @author Keith M. Hughes
 */
public interface SampledDataSequenceLoader {

  /**
   * Save the data from the capture into the specified file.
   *
   * @param dataFile
   *          the file to save the data in
   * @param dataSequence
   *          the data sequence to save the data from
   */
  void save(File dataFile, SampledDataSequence dataSequence);

  /**
   * Load data into the enclosed data capture.
   *
   * @param dataFile
   *          the file to load
   * @param dataSequence
   *          the data sequence to load the data into
   *
   * @return the number of frames of samples in the capture
   */
  int load(File dataFile, SampledDataSequence dataSequence);
}
