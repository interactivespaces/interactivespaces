/*
 * Copyright (C) 2013 Google Inc.
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

import interactivespaces.service.image.blob.BasicBlobDetector;
import interactivespaces.service.image.blob.BlobDetectionResult;
import interactivespaces.service.image.blob.BlobDetector;
import interactivespaces.service.image.blob.BlobSampleClassifier;
import interactivespaces.util.geometry.Rectangle2;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test the {@link BasicBlobDetector}.
 *
 * @author Keith M. Hughes
 */
public class BasicBlobDetectorTest {

  @Test
  public void testBlobDetector() {
    // Unfortunately the auto format changes takes away the grid that makes this
    // noticable where the blobs are.
    // 8x8 grid.
    int[] data =
        new int[] { 0, 0, 8, 9, 8, 0, 0, 0, 0, 0, 8, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 8, 8, 8, 0, 0, 0, 0,
            0, 8, 8, 8, 0, 0, 0, 0, 0, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };

    BlobDetector detector = new BasicBlobDetector();

    BlobDetectionResult blobs = detector.getBlobs(data, 8, 8, new BlobSampleClassifier() {
      @Override
      public boolean isSeedPoint(int sample) {
        return sample == 9;
      }

      @Override
      public boolean isBlobPoint(int sample) {
        return sample == 8;
      }
    });

    List<Rectangle2> boundingRectangles = blobs.getBoundingRectangles();
    System.out.println(boundingRectangles);

    List<Rectangle2> expected = Lists.newArrayList(new Rectangle2(2.0, 6.0, 3.0, 1.0), new Rectangle2(1.0, 2.0, 3.0, 2.0));
    Assert.assertEquals(expected.size(), boundingRectangles.size());
    for (int i = 0; i < expected.size(); i++) {
      Assert.assertTrue(expected.get(i).equal(boundingRectangles.get(i), 0.01));
    }
  }
}
