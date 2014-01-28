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

package interactivespaces.activity.image.vision.opencv.outline;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.image.video.VideoFrameProcessor;
import interactivespaces.service.image.vision.opencv.OpenCvVideoLoop;
import interactivespaces.service.image.vision.opencv.swing.OpenCvMatPanel;
import interactivespaces.util.ui.swing.JFrameManagedResource;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javax.swing.JFrame;

/**
 * An activity which uses raw OpenCV calls to capture video frames and reduce
 * the images to a series of edges.
 *
 * @author Keith M. Hughes
 */
public class ImageOpenCvVisionOutlineActivity extends BaseActivity implements VideoFrameProcessor<Mat> {

  /**
   * Configuration property for specifying which camera to use.
   */
  public static final String CONFIGURATION_NAME_CAMERA_ID = "camera.id";

  /**
   * Width of the display frame.
   */
  public static final int FRAME_WIDTH = 400;

  /**
   * Height of the display frame.
   */
  public static final int FRAME_HEIGHT = 400;

  /**
   * Filter size for the frame blurring.
   */
  public static final int BLUR_FILTER_SIZE = 7;

  /**
   * Threshold for edge detection to set its value.
   */
  public static final int EDGES_THRESHOLD = 80;

  /**
   * The maximum value when thresholding the processed image.
   */
  public static final int MAXIMUM_THRESHOLD_VALUE = 255;

  /**
   * Kernel size for the laplacian being used for edge detection.
   */
  public static final int LAPLACIAN_KERNEL_SIZE = 5;

  /**
   * ID of the camera to use.
   */
  public static final int CAMERA_ID_DEFAULT = 1;

  /**
   * The panel for writing the processed video into.
   */
  private OpenCvMatPanel panel;

  @Override
  public void onActivitySetup() {
    getLog().info("Raw OpenCV vision outline activity starting!");

    panel = new OpenCvMatPanel(getLog());

    JFrame frame = new JFrame("BasicPanel");
    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    frame.setContentPane(panel);
    frame.setVisible(true);
    addManagedResource(new JFrameManagedResource(frame));

    OpenCvVideoLoop videoLoop =
        new OpenCvVideoLoop(getConfiguration().getPropertyInteger(CONFIGURATION_NAME_CAMERA_ID, CAMERA_ID_DEFAULT),
            getLog());
    videoLoop.addProcessor(this);
    getManagedCommands().submit(videoLoop);
  }

  @Override
  public Mat onNewVideoFrame(Mat frame) {
    Mat processed = new Mat(frame.size(), CvType.CV_8UC3);
    edgeify(frame, processed);

    panel.drawImage(processed);

    return processed;
  }

  /**
   * Detect all the edges in the image and make only the edges visible.
   *
   * @param image
   *          the image to be processed
   * @param dst
   *          the destination image
   */
  private void edgeify(Mat image, Mat dst) {
    Mat gray = new Mat();
    Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY);

    Imgproc.medianBlur(gray, gray, BLUR_FILTER_SIZE);

    Mat edges = new Mat();
    Imgproc.Laplacian(gray, edges, CvType.CV_8U, LAPLACIAN_KERNEL_SIZE, 1, 0);

    Imgproc.threshold(edges, dst, EDGES_THRESHOLD, MAXIMUM_THRESHOLD_VALUE, Imgproc.THRESH_BINARY_INV);
  }
}
