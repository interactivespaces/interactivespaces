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
 * A sampler for determining whether a sample point should be considered part of
 * a blob.
 *
 * <p>
 * The sampler detects seed points, which will defined as a point with a high
 * confidence to be a blob.
 *
 * <p>
 * Once a seed is found, a blob point is a point that will have a high
 * confidence of being part of a blob given that a seed point has been detected
 * and the blob point is part of the growth from the seed.
 *
 * @author Keith M. Hughes
 */
public interface BlobSampleClassifier {

  /**
   * Is the current pixel a seed point for a blob?
   *
   * @param sample
   *          value of the pixel
   *
   * @return {@code true} is a seed point
   */
  boolean isSeedPoint(int sample);

  /**
   * Is the current pixel a blob point for a blob?
   *
   * @param sample
   *          value of the pixel
   *
   * @return {@code true} is a blob point
   */
  boolean isBlobPoint(int sample);
}
