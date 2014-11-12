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

package interactivespaces.service.audio.player.jukebox.support;

import interactivespaces.service.audio.player.FilePlayableAudioTrack;
import interactivespaces.service.audio.player.jukebox.AudioJukebox;

/**
 * Additional functionality for an {@link AudioJukebox} for service objects,
 * such as jukebox operations.
 *
 * @author Keith M. Hughes
 */
public interface InternalAudioJukebox extends AudioJukebox {

  /**
   * Notify that the operation has completed.
   */
  void notifyOperationComplete();

  /**
   * Play the supplied track.
   *
   * @param track
   *          the track to play
   */
  void playTrack(FilePlayableAudioTrack track);

  /**
   * Stop playing the current track, if any.
   */
  void stopPlayingCurrentTrack();
}
