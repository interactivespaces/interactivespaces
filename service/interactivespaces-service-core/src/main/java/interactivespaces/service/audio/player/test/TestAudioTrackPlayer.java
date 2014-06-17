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

package interactivespaces.service.audio.player.test;

import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.support.BaseAudioTrackPlayer;
import interactivespaces.util.InteractiveSpacesUtilities;

import org.apache.commons.logging.Log;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A fake {@link AudioTrackPlayer}. It will merely write events on stdout.
 *
 * @author Keith M. Hughes
 */
public class TestAudioTrackPlayer extends BaseAudioTrackPlayer {

  /**
   * The executor service to use.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The number of milliseconds in the "track".
   */
  private final long trackLength;

  /**
   * The playing indicator.
   */
  private final AtomicBoolean playing = new AtomicBoolean();

  /**
   * Construct the player.
   *
   * @param trackLength
   *          the amount of time to sleep for simulated track playback
   * @param executorService
   *          executor service for threads
   * @param log
   *          the logger to use
   */
  public TestAudioTrackPlayer(long trackLength, ScheduledExecutorService executorService, Log log) {
    super(log);
    this.executorService = executorService;
    this.trackLength = trackLength;
  }

  @Override
  public void shutdown() {
    stop();
  }

  @Override
  public void start(final PlayableAudioTrack track) {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        playing.set(true);
        notifyTrackStart(track);

        InteractiveSpacesUtilities.delay(trackLength);

        playing.set(false);
        notifyTrackStop(track);
      }
    });

  }

  @Override
  public void stop() {
    // TODO(keith): Make this stoppable
  }

  @Override
  public boolean isPlaying() {
    return playing.get();
  }
}
