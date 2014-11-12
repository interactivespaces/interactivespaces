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

import org.apache.commons.logging.Log;

/**
 * A {@link JukeboxOperation} for playing a track.
 *
 * @author Keith M. Hughes
 */
public class PlayTrackJukeboxOperation extends BaseJukeboxOperation {

  /**
   * The track to be played.
   */
  private FilePlayableAudioTrack track;

  /**
   * Construct the jukebox operation.
   *
   * @param audioJukebox
   *          the jukebox running this operation
   * @param track
   *          the track to play
   * @param log
   *          the logger to use
   */
  public PlayTrackJukeboxOperation(InternalAudioJukebox audioJukebox, FilePlayableAudioTrack track,
       Log log) {
    super(audioJukebox, log);

    this.track = track;
  }

  @Override
  public synchronized void start() {
    audioJukebox.playTrack(track);
  }

  @Override
  public synchronized void pause() {
    log.warn("Currently no way to pause playing");
  }

  @Override
  public synchronized void stop() {
    audioJukebox.stopPlayingCurrentTrack();
  }

  @Override
  public synchronized void handleTrackStop(FilePlayableAudioTrack track) {
    audioJukebox.notifyOperationComplete();
  }
}
