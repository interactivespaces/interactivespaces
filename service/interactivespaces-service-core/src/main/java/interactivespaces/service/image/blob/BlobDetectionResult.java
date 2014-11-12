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

import interactivespaces.util.geometry.Rectangle2;

import java.util.List;


/**
 * A result from running a blob detector.
 *
 * @author Keith M. Hughes
 */
public class BlobDetectionResult {

  /**
   * The value in the blob data when there is no blob at that location.
   */
  public static final int BLOB_DETECTION_NO_BLOB = -1;

  /**
   * The detection for the blobs.
   */
  private final int[] blobData;

  /**
   * The bounding rectangles.
   */
  private final List<Rectangle2> boundingRectangles;

  /**
   * Construct a blob detection result.
   *
   * @param blobData
   *          the blob data
   * @param boundingRectangles
   *          the bounding rectantgles for the blobs
   */
  public BlobDetectionResult(int[] blobData, List<Rectangle2> boundingRectangles) {
    this.blobData = blobData;
    this.boundingRectangles = boundingRectangles;
  }

  /**
   * Get the bounding rectangles for all blobs.
   *
   * @return the bounding rectangles for all blobs
   */
  public List<Rectangle2> getBoundingRectangles() {
    return boundingRectangles;
  }

  /**
   * Get the data for the blob.
   *
   * <p>
   * The data will give which pixels in the original data are associated with
   * which blob. The value {@link BlobDetectionResult#BLOB_DETECTION_NO_BLOB}
   * means no blob was found at that particular location. Values greater than or
   * equal to zero give the blob index in the bounding rectangle list.
   *
   * @return the blob data
   */
  public int[] getBlobData() {
    return blobData;
  }
}
