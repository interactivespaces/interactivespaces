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

package interactivespaces.service.audio.player.jukebox;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
  private PlayableAudioTrack track;

  /**
   * Number of milliseconds into the begin of the track
   */
  private long begin;

  /**
   * Duration of the play.
   */
  private long duration;

  /**
   * The track player being used to play the track.
   *
   * <p>
   * Will be {@code null} if no track is being played.
   */
  private AudioTrackPlayer player;

  /**
   * The runnable which will check on the track player.
   */
  private Runnable runnable;

  /**
   * Handle for periodic task scanning the player.
   */
  private ScheduledFuture<?> playingFuture;

  public PlayTrackJukeboxOperation(PlayableAudioTrack track, long begin, long duration,
      Configuration configuration, AudioTrackPlayerFactory trackPlayerFactory,
      ScheduledExecutorService executor, AudioJukeboxListener listener, Log log) {
    super(configuration, trackPlayerFactory, executor, listener, log);

    this.track = track;
    this.begin = begin;
    this.duration = duration;

    runnable = new Runnable() {
      @Override
      public void run() {
        checkPlayer();
      }
    };
  }

  @Override
  public void start() {
    synchronized (this) {
      playingFuture = executor.scheduleAtFixedRate(runnable, 0, 500, TimeUnit.MILLISECONDS);

    }

    listener.onJukeboxTrackStart(this, track);
  }

  @Override
  public void pause() {
    log.warn("Currently no way to pause playing");
  }

  @Override
  public synchronized void stop() {
    synchronized (this) {
      if (player != null) {
        playingFuture.cancel(true);

        player.stop();
        player = null;
      }
    }

    listener.onJukeboxTrackStop(this, track);
  }

  @Override
  public synchronized boolean isRunning() {
    return player != null && player.isPlaying();
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
        listener.onJukeboxTrackStop(this, track);
        listener.onJukeboxOperationComplete(this);

        playingFuture.cancel(true);
      }
    } else {
      player = trackPlayerFactory.newTrackPlayer(track, configuration, log);
      player.start(begin, duration);
    }
  }
}
