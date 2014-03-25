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

package interactivespaces.service.image.blob;

/**
 * A detector for blobs in a monochromatic sample space.
 *
 * @author Keith M. Hughes
 */
public interface BlobDetector {

  /**
   * Locate all blobs in the given data.
   *
   * @param data
   *          the data being scanned
   * @param numRows
   *          the number of rows of data
   * @param numColumns
   *          the number of columns of data
   * @param sampler
   *          the sampler for locating blob candidates
   *
   * @return the detection result
   */
  BlobDetectionResult getBlobs(int[] data, int numRows, int numColumns, BlobSampleClassifier sampler);

}