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

import interactivespaces.interaction.detection.DetectionEventListener;
import interactivespaces.service.image.video.BaseVideoFrameListener;
import interactivespaces.util.geometry.Rectangle2;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

/**
 * A video frame listener that applies a cascade classifier to the video frame
 * and sends out detection events to registered listeners.
 *
 * @author Keith M. Hughes
 */
public class OpenCvCascadeClassifierVideoFrameProcessor extends BaseVideoFrameListener<Mat> {

  /**
   * Color to use for drawing detection rectangles.
   */
  public static final Scalar FACE_RECTANGLE_COLOR = new Scalar(0, 255, 0);

  /**
   * The classifier to use.
   */
  private final CascadeClassifier classifier;

  /**
   * The detection listeners.
   */
  private final List<DetectionEventListener<OpenCvVideoLoop, Rectangle2>> listeners = Lists.newArrayList();

  /**
   * Logger for the listener.
   */
  private final Log log;

  /**
   * Construct a listener with the given classifier.
   *
   * @param classifier
   *          the classifier to use
   * @param log
   *          the logger for the listener
   */
  public OpenCvCascadeClassifierVideoFrameProcessor(CascadeClassifier classifier, Log log) {
    this.classifier = classifier;
    this.log = log;
  }

  /**
   * Add a new detection event listener.
   *
   * @param listener
   *          the listener to add
   */
  public void addDetectionEventListener(DetectionEventListener<OpenCvVideoLoop, Rectangle2> listener) {
    listeners.add(listener);
  }

  /**
   * Remove a detection event listener.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  public void removeDetectionEventListener(DetectionEventListener<OpenCvVideoLoop, Rectangle2> listener) {
    listeners.remove(listener);
  }

  @Override
  public Mat onNewVideoFrame(Mat frame) {
    MatOfRect classifierDetections = new MatOfRect();
    classifier.detectMultiScale(frame, classifierDetections);

    List<Rectangle2> eventData = Lists.newArrayList();

    for (Rect detectionRect : classifierDetections.toArray()) {
      Point topLeft = detectionRect.tl();
      Point bottomRight = detectionRect.br();

      eventData.add(new Rectangle2(topLeft.x, bottomRight.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.x));

      // TODO(keith): Detemine if we write or not.
      Core.rectangle(frame, topLeft, bottomRight, FACE_RECTANGLE_COLOR);
    }

    sendDetectionEvents(eventData);

    // Pass the same frame on.
    return frame;
  }

  /**
   * Send the detection events to the listeners.
   *
   * @param eventData
   *          the detection event data
   */
  private void sendDetectionEvents(List<Rectangle2> eventData) {
    for (DetectionEventListener<OpenCvVideoLoop, Rectangle2> listener : listeners) {
      try {
        listener.onNewDetectionEvent(null, eventData);
      } catch (Exception e) {
        log.error("Error when sending detection event from classifier", e);
      }
    }
  }
}
