/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone.messaging;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;

import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * A runner that plays back messages into the message router.
 *
 * @author Trevor Pering
 */
class PlaybackRunner implements Runnable {

  /**
   * Default time to initially wait for completion.
   */
  public static final int DEFAULT_FINISH_DELAY_MS = 1000;

  /**
   * File containing playback messages.
   */
  private final File playbackFile;

  /**
   * Reader for processing messages.
   */
  private final BufferedReader playbackReader;

  /**
   * {@code true} if messages should be played back on a route.
   */
  private final boolean onRoute;

  /**
   * Message router to use for handling messages.
   */
  private final StandaloneMessageRouter standaloneMessageRouter;

  /**
   * Create a new playback runner.
   *
   * @param standaloneMessageRouter
   *          router to use for playback
   * @param playbackFile
   *          File containing messages
   * @param onRoute
   *          {@code true} if messages should be played back on a route
   */
  public PlaybackRunner(StandaloneMessageRouter standaloneMessageRouter, File playbackFile, boolean onRoute) {
    this.standaloneMessageRouter = standaloneMessageRouter;
    this.playbackFile = playbackFile;
    this.onRoute = onRoute;
    try {
      playbackReader = new BufferedReader(new FileReader(playbackFile));
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not create playback runner", e);
    }
  }

  @Override
  public void run() {
    try {
      String line;
      while ((line = playbackReader.readLine()) != null) {
        try {
          line = line.trim();
          if (line.startsWith("#") || line.isEmpty()) {
            continue;
          }
          MessageMap messageObject = MessageMap.fromString(line);
          Object timeDelay = messageObject.get(StandaloneMessageRouter.TIME_DELAY_KEY);
          if (!(timeDelay instanceof Integer)) {
            throw new SimpleInteractiveSpacesException(String.format("Message must have an Integer field '%s': %s",
                StandaloneMessageRouter.TIME_DELAY_KEY, messageObject));
          }
          long deltaTime = (Integer) timeDelay;
          standaloneMessageRouter.getLog().debug(String.format("Playback next message in %dms...", deltaTime));
          Thread.sleep(deltaTime);
          standaloneMessageRouter.getLog().debug("Playback message " + line);
          standaloneMessageRouter.processMessage(messageObject, onRoute);
        } catch (Exception e) {
          standaloneMessageRouter.handleError("While processing playback record", e);
        }
      }
      playbackReader.close();

      // Let last messages clear system.
      long finishDelayMs = DEFAULT_FINISH_DELAY_MS;
      while (finishDelayMs > 0) {
        standaloneMessageRouter.getLog().info(
            String.format("Done with playback, waiting %sms for completion.", finishDelayMs));
        Thread.sleep(finishDelayMs);
        finishDelayMs = standaloneMessageRouter.getFinishDelta();
      }

      standaloneMessageRouter.verifyFinished();
    } catch (Exception e) {
      standaloneMessageRouter.handleError("While processing playback file " + playbackFile.getAbsolutePath(), e);
    }
  }

  /**
   * Stop the playback runner.
   */
  public void stop() {
    Closeables.closeQuietly(playbackReader);
  }
}
