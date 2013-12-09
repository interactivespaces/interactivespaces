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

import interactivespaces.service.image.video.VideoLoop;
import interactivespaces.util.InteractiveSpacesUtilities;
import interactivespaces.util.concurrency.CancellableLoop;

import org.apache.commons.logging.Log;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

/**
 * A {@link CancellableLoop} which grabs video frames using OpenCV and processes
 * them.
 *
 * @author Keith M. Hughes
 */
public class OpenCvVideoLoop extends VideoLoop<Mat> {

  /**
   * Time for the camera to initialize, in milliseconds.
   */
  public static final long CAMERA_INITIALIZATION_TIME = 1000;

  /**
   * The IS of the camera to use.
   */
  private final int cameraId;

  /**
   * The image being captured.
   */
  private Mat frame;

  /**
   * The video capture device.
   */
  private VideoCapture capture;

  /**
   * Logger for the loop.
   */
  private final Log log;

  /**
   * Construct a video loop.
   *
   * @param cameraId
   *          ID for the camera to use
   * @param log
   *          logger for the loop
   */
  public OpenCvVideoLoop(int cameraId, Log log) {
    this.cameraId = cameraId;
    this.log = log;
  }

  @Override
  protected void setup() {
    frame = new Mat();
    capture = new VideoCapture(cameraId);

    // OpenCV sometimes needs a bit of time for the camera to fully initialize.
    InteractiveSpacesUtilities.delay(CAMERA_INITIALIZATION_TIME);
  }

  @Override
  protected void loop() throws InterruptedException {
    capture.grab();
    capture.retrieve(frame);
    if (frame.empty()) {
      log.warn("No image");
      return;
    }

    notifyListenersNewVideoFrame(frame);
  }

  @Override
  protected void cleanup() {
    capture.release();
  }

  @Override
  protected void handleException(Exception e) {
    log.error("Error during video loop", e);
  }
}
