/*
 * Copyright (C) 2012 Google Inc.
 * tracksPlayed
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

package interactivespaces.service.audio.player.jukebox;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import org.apache.commons.logging.Log;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
   * Tracks which have already been played.
   */
  private Collection<PlayableAudioTrack> tracksAlreadyPlayed;

  /**
   * Music repository to get a new track.
   */
  private AudioRepository musicRepository;

  /**
   * The track player for the track
   */
  private AudioTrackPlayer player;

  /**
   * Current track being played.
   */
  private PlayableAudioTrack currentTrack;

  /**
   * The runnable which will check on the track player.
   */
  private Runnable runnable;

  /**
   * Handle for periodic task scanning the player.
   */
  private ScheduledFuture<?> playingFuture;

  public ShuffleJukeboxOperation(Collection<PlayableAudioTrack> tracksAlreadyPlayed,
      Configuration configuration, AudioRepository musicRepository,
      AudioTrackPlayerFactory trackPlayerFactory, ScheduledExecutorService executor,
      AudioJukeboxListener listener, Log log) {
    super(configuration, trackPlayerFactory, executor, listener, log);

    this.tracksAlreadyPlayed = tracksAlreadyPlayed;
    this.musicRepository = musicRepository;
    isRunning = false;

    runnable = new Runnable() {
      @Override
      public void run() {
        checkPlayer();
      }
    };
  }

  @Override
  public synchronized void start() {
    log.info("Starting music jukebox shuffle play");
    if (!isRunning) {
      playingFuture = executor.scheduleAtFixedRate(runnable, 0, 500, TimeUnit.MILLISECONDS);

      isRunning = true;
    }
  }

  @Override
  public void pause() {
    log.warn("Currently no way to pause playing");
  }

  @Override
  public synchronized void stop() {
    log.info("Stopping music jukebox shuffle play " + isRunning);
    if (isRunning) {
      playingFuture.cancel(true);
      if (player != null && player.isPlaying()) {
        player.stop();
        player = null;
      }

      isRunning = false;
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return isRunning;
  }

  /**
   * Check how the player is doing.
   */
  private synchronized void checkPlayer() {
    if (player != null) {
      if (player.isPlaying()) {
        return;
      } else {
        player = null;
        listener.onJukeboxTrackStop(this, currentTrack);
      }
    }

    try {
      currentTrack = musicRepository.getRandomTrack(tracksAlreadyPlayed);
      tracksAlreadyPlayed.add(currentTrack);
      player = trackPlayerFactory.newTrackPlayer(currentTrack, configuration, log);
      player.start(0, 0);

      listener.onJukeboxTrackStart(this, currentTrack);
    } catch (Exception e) {
      log.error(String.format("Could not start up track %s", currentTrack), e);
    }
  }
}
