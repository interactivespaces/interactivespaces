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

package interactivespaces.service.audio.player.internal.jlayer;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.audio.player.FilePlayableAudioTrack;
import interactivespaces.service.audio.player.support.BaseAudioTrackPlayer;

import com.google.common.io.Closeables;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An audio track player which uses JLayer.
 *
 * @author Keith M. Hughes
 */
public class JLayerAudioTrackPlayer extends BaseAudioTrackPlayer {

  /**
   * The player.
   */
  private AdvancedPlayer player;

  /**
   * {@code true} if playing.
   */
  private AtomicBoolean playing = new AtomicBoolean(false);

  /**
   * Executor service for getting threads to play in.
   */
  private ScheduledExecutorService executorService;

  /**
   * Construct a new player.
   *
   * @param executorService
   *          the executor service to use
   * @param log
   *          the logger to use
   */
  public JLayerAudioTrackPlayer(ScheduledExecutorService executorService, Log log) {
    super(log);
    this.executorService = executorService;
  }

  @Override
  public void shutdown() {
    stop();
  }

  @Override
  public synchronized void start(final FilePlayableAudioTrack track) {
    if (playing.get()) {
      throw new SimpleInteractiveSpacesException((String.format(
          "Cannot start playing audio file %s: Already playing a track", track.getFile().getAbsolutePath())));
    }

    File file = track.getFile();
    if (!file.exists()) {
      throw new SimpleInteractiveSpacesException((String.format("Cannot find audio file %s", file.getAbsolutePath())));
    }

    try {
      final FileInputStream trackStream = new FileInputStream(file);

      player = new AdvancedPlayer(trackStream);
      player.setPlayBackListener(new PlaybackListener() {

        @Override
        public void playbackFinished(PlaybackEvent event) {
          playing.set(false);

          notifyTrackStop(track);
        }

        @Override
        public void playbackStarted(PlaybackEvent event) {
          playing.set(true);

          notifyTrackStart(track);
        }
      });

      // The JLayer play() method runs entirely in the calling thread,
      // which means it blocks until the song is complete. So run in its own
      // thread.
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          try {
            player.play();
          } catch (JavaLayerException e) {
            log.error("JLayer player failed during MP3 playback", e);
          } finally {
            Closeables.closeQuietly(trackStream);
          }
        }
      });
    } catch (Exception e) {
      throw new InteractiveSpacesException((String.format("Cannot create audio player for file %s",
          file.getAbsolutePath())), e);
    }
  }

  @Override
  public synchronized void stop() {
    if (player != null) {
      try {
        player.stop();
      } catch (Exception e) {
        log.error("Error while stopping audio track playback", e);
      }
      player = null;
    }
  }

  @Override
  public boolean isPlaying() {
    return playing.get();
  }
}
