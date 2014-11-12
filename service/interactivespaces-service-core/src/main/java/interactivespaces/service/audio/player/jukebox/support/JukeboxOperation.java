/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.service.audio.player.jukebox.support;

import interactivespaces.service.audio.player.FilePlayableAudioTrack;

/**
 * An operation happening within the jukebox.
 *
 * @author Keith M. Hughes
 */
public interface JukeboxOperation {

  /**
   * Start the operation.
   */
  void start();

  /**
   * Pause the operation.
   */
  void pause();

  /**
   * Stop the operation.
   */
  void stop();

  /**
   * Handle a track stopping playing.
   *
   * @param track
   *          the track which has stopped
   */
  void handleTrackStop(FilePlayableAudioTrack track);
}
