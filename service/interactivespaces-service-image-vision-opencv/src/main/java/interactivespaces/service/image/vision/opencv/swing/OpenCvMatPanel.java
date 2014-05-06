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

package interactivespaces.service.image.vision.opencv.swing;

import interactivespaces.service.image.vision.opencv.MatUtils;

import org.apache.commons.logging.Log;
import org.opencv.core.Mat;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * A Swing JPanel which can display OpenCV Mat images.
 *
 * @author Keith M. Hughes
 */
public class OpenCvMatPanel extends JPanel {

  /**
   * The Swing image to paint.
   */
  private BufferedImage image;

  /**
   * {@code true} if the image should be released after it is drawn.
   */
  private final boolean releaseAfterDraw;

  /**
   * Logger for the panel.
   */
  private final Log log;

  /**
   * Construct a new panel.
   *
   * <p>
   * The image will be released after it is drawn.
   *
   * @param log
   *          logger for the panel
   */
  public OpenCvMatPanel(Log log) {
    this(log, true);
  }

  /**
   * Construct a new panel.
   *
   * @param log
   *          logger for the panel
   * @param releaseAfterDraw
   *          {@code true} if the Mat should be released after it is drawn
   */
  public OpenCvMatPanel(Log log, boolean releaseAfterDraw) {
    this.log = log;
    this.releaseAfterDraw = releaseAfterDraw;
  }

  @Override
  public void paintComponent(Graphics g) {
    if (image != null) {
      g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
    }
  }

  /**
   * Draw an OpenCV image in the panel.
   *
   * @param opencvImage
   *          the image to draw
   */
  public void drawImage(final Mat opencvImage) {
    SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {

      @Override
      protected BufferedImage doInBackground() throws Exception {
        return MatUtils.matToBufferedImage(opencvImage);
      }

      @Override
      protected void done() {
        try {
          image = get();

          repaint();

          if (releaseAfterDraw) {
            opencvImage.release();
          }
        } catch (InterruptedException e) {
          log.info("Swing worker for rendering Mat images interrupted");
        } catch (ExecutionException e) {
          log.error("Error during Swing worker for rendering Mat images", e);
        }
      }
    };

    worker.execute();
  }
}
