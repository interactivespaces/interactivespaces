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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.interaction.detection.DetectionEventListener;
import interactivespaces.service.image.video.BaseVideoFrameProcessor;
import interactivespaces.service.image.video.CompositeVideoFrameProcessor;
import interactivespaces.service.image.vision.opencv.OpenCvCascadeClassifierVideoFrameProcessor;
import interactivespaces.service.image.vision.opencv.OpenCvVideoLoop;
import interactivespaces.service.image.vision.opencv.swing.OpenCvMatPanel;
import interactivespaces.util.data.json.JsonBuilder;
import interactivespaces.util.geometry.Rectangle2;
import interactivespaces.util.ui.swing.JFrameManagedResource;

import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.util.Collection;

import javax.swing.JFrame;

/**
 * An activity which captures video frames and detects faces in them and sends
 * the result on a route.
 *
 * @author Keith M. Hughes
 */
public class ImageOpenCvVisionFaceDetectActivity extends BaseRoutableRosActivity {

  /**
   * Location in the controller filesystem for obtaining the classifiers.
   */
  public static final String CASCADE_DATA_ROOT = "extras/interactivespaces.service.image.vision/data/haarcascades";

  /**
   * Route channel to write on.
   */
  public static final String ROUTE_CHANNEL = "output1";

  /**
   * The message property giving an array of detected faces.
   */
  public static final String MESSAGE_PROPERTY_FACES = "faces";

  /**
   * The message property giving the x coordinate for a particular face
   * detection.
   */
  public static final String MESSAGE_PROPERTY_FACE_X = "x";

  /**
   * The message property giving the y coordinate for a particular face
   * detection.
   */
  public static final String MESSAGE_PROPERTY_FACE_Y = "y";

  /**
   * The message property giving the width of a particular face detection.
   */
  public static final String MESSAGE_PROPERTY_FACE_WIDTH = "width";

  /**
   * The message property giving the height of a particular face detection.
   */
  public static final String MESSAGE_PROPERTY_FACE_HEIGHT = "height";

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
   * ID of the camera to use.
   */
  public static final int CAMERA_ID_DEFAULT = 0;

  /**
   * The panel for writing the processed video into.
   */
  private OpenCvMatPanel panel;

  /**
   * The face detector to use.
   */
  private CascadeClassifier faceDetector;

  @Override
  public void onActivitySetup() {
    getLog().info("Raw OpenCV vision face detect activity starting!");

    // TODO(keith): Add this to an openCV service for getting OpenCV resources
    faceDetector = getClassifier("haarcascade_frontalface_alt.xml");

    OpenCvCascadeClassifierVideoFrameProcessor cascadeListener =
        new OpenCvCascadeClassifierVideoFrameProcessor(faceDetector, getLog());

    // Need a listener when the face detector detects a face.
    cascadeListener.addDetectionEventListener(new DetectionEventListener<OpenCvVideoLoop, Rectangle2>() {
      @Override
      public void onNewDetectionEvent(OpenCvVideoLoop source, Collection<Rectangle2> eventData) {
        handleNewDetectionEvent(eventData);
      }
    });

    CompositeVideoFrameProcessor<Mat> compositeListener = new CompositeVideoFrameProcessor<Mat>(getLog());
    compositeListener.addComponent(cascadeListener);

    OpenCvVideoLoop videoLoop =
        new OpenCvVideoLoop(getConfiguration().getPropertyInteger(CONFIGURATION_NAME_CAMERA_ID, CAMERA_ID_DEFAULT),
            getLog());
    videoLoop.addProcessor(compositeListener);
    getManagedCommands().submit(videoLoop);

    // TODO(keith): Add config parameter to say if want to see the frame in a
    // Swing panel.
    panel = new OpenCvMatPanel(getLog());

    JFrame frame = new JFrame("Face Detection Panel");
    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    frame.setContentPane(panel);
    frame.setVisible(true);
    addManagedResource(new JFrameManagedResource(frame));
    compositeListener.addComponent(new BaseVideoFrameProcessor<Mat>() {

      @Override
      public Mat onNewVideoFrame(Mat frame) {
        panel.drawImage(frame);

        return frame;
      }
    });
  }

  /**
   * Handle a set of detection events for the faces.
   *
   * @param eventData
   *          the face data
   */
  private void handleNewDetectionEvent(Collection<Rectangle2> eventData) {
    if (isActivated()) {
      JsonBuilder message = new JsonBuilder();

      message.newArray(MESSAGE_PROPERTY_FACES);

      for (Rectangle2 event : eventData) {
        message.newObject();

        message.put(MESSAGE_PROPERTY_FACE_X, event.getX());
        message.put(MESSAGE_PROPERTY_FACE_Y, event.getY());
        message.put(MESSAGE_PROPERTY_FACE_WIDTH, event.getWidth());
        message.put(MESSAGE_PROPERTY_FACE_HEIGHT, event.getHeight());

        message.up();
      }

      getLog().debug(String.format("Faces detected: %s", message));

      sendOutputJsonBuilder(ROUTE_CHANNEL, message);
    }
  }

  /**
   * Get the classifier to use.
   *
   * @param classifierName
   *          the name of the classifier
   *
   * @return the classifier
   */
  private CascadeClassifier getClassifier(String classifierName) {
    File haarCascadesDirectoryPath =
        new File(getSpaceEnvironment().getFilesystem().getInstallDirectory(), CASCADE_DATA_ROOT);

    String classifierPath = new File(haarCascadesDirectoryPath, classifierName).getAbsolutePath();
    CascadeClassifier classifier = new CascadeClassifier(classifierPath);
    if (classifier.empty()) {
      throw new SimpleInteractiveSpacesException(String.format("Cannot find face classification file %s",
          classifierPath));
    }

    return classifier;
  }
}
