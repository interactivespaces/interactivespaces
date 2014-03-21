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
 * A blob sample classifier which takes ranges supplied at construction.
 *
 * @author Keith M. Hughes
 */
public class RangeBlobSampleClassifier implements BlobSampleClassifier {

  /**
   * Bottom part of range for a seed.
   */
  private final int seedMinimum;

  /**
   * Bottom part of range for a seed.
   */
  private final int seedMaximum;

  /**
   * Bottom part of range for a blob.
   */
  private final int blobMinimum;

  /**
   * Bottom part of range for a blob.
   */
  private final int blobMaximum;

  /**
   * Construct the classifier.
   *
   * @param seedMinimum
   *          the minimum value for a seed
   * @param seedMaximum
   *          the maximum value for a seed
   * @param blobMinimum
   *          the minimum value for a blob once a seed is found
   * @param blobMaximum
   *          the maximum value for a blob once a seed is found
   */
  public RangeBlobSampleClassifier(int seedMinimum, int seedMaximum, int blobMinimum, int blobMaximum) {
    this.seedMinimum = seedMinimum;
    this.seedMaximum = seedMaximum;
    this.blobMinimum = blobMinimum;
    this.blobMaximum = blobMaximum;
  }

  @Override
  public boolean isSeedPoint(int sample) {
    return seedMinimum <= sample && sample <= seedMaximum;
  }

  @Override
  public boolean isBlobPoint(int sample) {
    return blobMinimum <= sample && sample <= blobMaximum;
  }
}
