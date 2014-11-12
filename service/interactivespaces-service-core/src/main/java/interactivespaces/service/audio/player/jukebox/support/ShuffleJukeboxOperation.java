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

import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.FilePlayableAudioTrack;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A {@link JukeboxOperation} which shuffles from the repository.
 *
 * @author Keith M. Hughes
 */
public class ShuffleJukeboxOperation extends BaseJukeboxOperation {

  /**
   * The shuffle is running.
   */
  private boolean isRunning;

  /**
   * Audio repository to get a new track.
   */
  private AudioRepository audioRepository;

  /**
   * Current track being played.
   */
  private FilePlayableAudioTrack currentTrack;

  /**
   * The random number generator for shuffling the songs.
   */
  private final Random random = new Random(System.nanoTime());

  /**
   * The tracks to be played.
   */
  private List<String> tracksToPlay;

  /**
   * Construct the jukebox operation.
   *
   * @param audioJukebox
   *          the jukebox running this operation
   * @param audioRepository
   *          the repository for audio
   * @param log
   *          the logger to use
   */
  public ShuffleJukeboxOperation(InternalAudioJukebox audioJukebox, AudioRepository audioRepository, Log log) {
    super(audioJukebox, log);

    this.audioRepository = audioRepository;
    isRunning = false;
  }

  @Override
  public synchronized void start() {
    if (!isRunning) {
      log.info("Starting music jukebox shuffle play");

      tracksToPlay =
          Lists.newArrayList(Iterables.transform(audioRepository.getAllPlayableTracks(),
              new Function<FilePlayableAudioTrack, String>() {
                @Override
                public String apply(FilePlayableAudioTrack input) {
                  return input.getMetadata().getId();
                }
              }));

      Collections.shuffle(tracksToPlay, random);

      isRunning = true;

      startPlayingNewTrack();
    } else {
      log.warn("Trying to starting music jukebox shuffle play when already playing");
    }
  }

  @Override
  public void pause() {
    log.warn("Currently no way to pause playing");
  }

  @Override
  public synchronized void stop() {
    if (isRunning) {
      audioJukebox.stopPlayingCurrentTrack();
      isRunning = false;
    }
  }

  @Override
  public synchronized void handleTrackStop(FilePlayableAudioTrack track) {
    startPlayingNewTrack();
  }

  /**
   * Get a new track to play and start it playing.
   */
  private void startPlayingNewTrack() {
    if (!tracksToPlay.isEmpty()) {
      currentTrack = audioRepository.getPlayableTrack(tracksToPlay.remove(tracksToPlay.size() - 1));
      audioJukebox.playTrack(currentTrack);
    } else {
      audioJukebox.notifyOperationComplete();
    }
  }
}
