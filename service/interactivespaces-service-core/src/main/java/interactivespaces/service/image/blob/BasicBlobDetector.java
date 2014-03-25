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

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * A simple blob detector that uses recursive expansion from seed points to
 * locate the blobs.
 *
 * @author Keith M. Hughes
 */
public class BasicBlobDetector implements BlobDetector {

  @Override
  public BlobDetectionResult getBlobs(int[] data, int numRows, int numColumns, BlobSampleClassifier sampler) {
    BlobTask task = new BlobTask(data, numRows, numColumns, sampler);

    return task.findBlobs();
  }

  /**
   * A task for doing the blob tracking.
   *
   * @author Keith M. Hughes
   */
  static class BlobTask {

    /**
     * The data being scanned.
     */
    private final int[] data;

    /**
     * The maximum rows value for the data.
     */
    private final int maxRows;

    /**
     * The maximum column value for the data.
     */
    private final int maxColumns;

    /**
     * Data from the scan. This will say which blob a sample is part of, and
     * which samples are not part of any blob
     * {@link BlobDetectionResult.BLOB_DETECTION_NO_BLOB}.
     */
    private final int[] scanData;

    /**
     * The blob sampler to be used.
     */
    private final BlobSampleClassifier sampler;

    /**
     * List of rectangles as we find blobs.
     */
    private final List<Rectangle2> blobRectangles = Lists.newArrayList();

    /**
     * Current label.
     */
    private int currentLabel = 0;

    /**
     * Construct a new detection task.
     *
     * @param data
     *          the sample data
     * @param numRows
     *          the number of rows in the sample data
     * @param numColumns
     *          the number of columns in the sample data
     * @param sampler
     *          the blob sample classifier
     */
    public BlobTask(int[] data, int numRows, int numColumns, BlobSampleClassifier sampler) {
      this.data = data;
      this.maxRows = numRows - 1;
      this.maxColumns = numColumns - 1;
      this.sampler = sampler;

      scanData = new int[data.length];
      Arrays.fill(scanData, BlobDetectionResult.BLOB_DETECTION_NO_BLOB);
    }

    /**
     * Find all blobs in the current sample.
     *
     * @return the detection result
     */
    public BlobDetectionResult findBlobs() {
      scanForSeeds();

      return new BlobDetectionResult(scanData, blobRectangles);
    }

    /**
     * Scan for seeds to start growing into the blobs.
     */
    private void scanForSeeds() {
      int location = 0;
      for (int row = 0; row <= maxRows; row++) {
        for (int col = 0; col <= maxColumns; col++) {
          if (scanData[location] == BlobDetectionResult.BLOB_DETECTION_NO_BLOB) {
            int sample = data[location];

            if (sampler.isSeedPoint(sample)) {
              int label = currentLabel++;
              scanData[location] = label;

              Rectangle2 rect = new Rectangle2(col, maxRows - row, 0, 0);
              blobRectangles.add(rect);

              spreadBlob(label, location, row, col, rect);
            }
          }

          location++;
        }
      }
    }

    /**
     * A new blob seed has been located. Start expanding.
     *
     * @param label
     *          the label for the blob
     * @param location
     *          the location of the current point being spread
     * @param row
     *          row to be spread from
     * @param col
     *          column to be spread from
     * @param rect
     *          rectangle for this blob
     */
    private void spreadBlob(int label, int location, int row, int col, Rectangle2 rect) {
      // Check above
      if (0 < row) {
        spreadBlob0(label, location - maxColumns - 1, row - 1, col, rect);
      }

      // Check to the left
      if (0 < col) {
        spreadBlob0(label, location - 1, row, col - 1, rect);
      }

      // Check below
      if (row < maxRows) {
        spreadBlob0(label, location + maxColumns + 1, row + 1, col, rect);
      }

      // Check to the right
      if (col < maxColumns) {
        spreadBlob0(label, location + 1, row, col + 1, rect);
      }
    }

    /**
     * Check the location given. If it allows a spread, continue the search.
     *
     * @param label
     *          the label fir the current blob
     * @param location
     *          the location to sample
     * @param row
     *          the row of the location
     * @param col
     *          the column of the sample
     * @param rect
     *          the rectangle for the blob
     */
    private void spreadBlob0(int label, int location, int row, int col, Rectangle2 rect) {
      if (scanData[location] == BlobDetectionResult.BLOB_DETECTION_NO_BLOB && sampler.isBlobPoint(data[location])) {
        scanData[location] = label;

        rect.grow(col, maxRows - row);

        spreadBlob(label, location, row, col, rect);
      }
    }
  }
}
