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

package interactivespaces.service.image.vision.opencv;

import interactivespaces.SimpleInteractiveSpacesException;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

/**
 * A collection of utilities for working with OpenCV {@link Mat} instances.
 *
 * @author Keith M. Hughes
 */
public class MatUtils {

  /**
   * Converts a {@link Mat} into a {@link BufferedImage}.
   *
   * @param matrix
   *          Mat of type CV_8UC3 or CV_8UC1
   *
   * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
   *
   * @throws SimpleInteractiveSpacesException
   *           the OpenCV Mat type is not supported
   */
  public static BufferedImage matToBufferedImage(Mat matrix) throws SimpleInteractiveSpacesException {
    int cols = matrix.cols();
    int rows = matrix.rows();
    int elemSize = (int) matrix.elemSize();
    byte[] data = new byte[cols * rows * elemSize];
    int type;
    matrix.get(0, 0, data);
    switch (matrix.channels()) {
      case 1:
        type = BufferedImage.TYPE_BYTE_GRAY;
        break;
      case 3:
        type = BufferedImage.TYPE_3BYTE_BGR;
        for (int i = 0; i < data.length; i = i + 3) {
          byte b = data[i];
          data[i] = data[i + 2];
          data[i + 2] = b;
        }
        break;
      default:
        throw new SimpleInteractiveSpacesException("The OpenCV Mat type is not supported");
    }

    BufferedImage image = new BufferedImage(cols, rows, type);
    image.getRaster().setDataElements(0, 0, cols, rows, data);

    return image;
  }

}
