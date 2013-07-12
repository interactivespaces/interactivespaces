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

package interactivespaces.service.audio.player.jukebox;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * a very basic audio jukebox.
 *
 * @author Keith M. Hughes
 */
public class BasicAudioJukebox implements AudioJukebox {

  /**
   * The music repository this is a jukebox for.
   */
  private AudioRepository musicRepository;

  /**
   * The factory for track players.
   */
  private AudioTrackPlayerFactory trackPlayerFactory;

  /**
   * A set of all tracks played since this jukebox was started.
   */
  private Set<PlayableAudioTrack> tracksAlreadyPlayed;

  /**
   * Current operation the jukebox is doing. Can be {@code null} if nothing is
   * happening.
   */
  private JukeboxOperation currentOperation;

  /**
   * Threads for the jukebox.
   */
  private ScheduledExecutorService executorService;

  /**
   * configuration for the jukebox.
   */
  private Configuration configuration;

  /**
   * Logger for the jukebox.
   */
  private Log log;

  /**
   * Listener for jukebox events.
   */
  private AudioJukeboxListener listener;

  public BasicAudioJukebox(AudioRepository musicRepository,
      AudioTrackPlayerFactory trackPlayerFactory, ScheduledExecutorService executorService,
      Configuration configuration, Log log) {
    this.musicRepository = musicRepository;
    this.trackPlayerFactory = trackPlayerFactory;
    this.executorService = executorService;
    this.configuration = configuration;
    this.log = log;
  }

  @Override
  public void setListener(AudioJukeboxListener listener) {
    this.listener = listener;
  }

  @Override
  public void startup() {
    tracksAlreadyPlayed = Sets.newHashSet();
  }

  @Override
  public void shutdown() {
    shutdownCurrentOperation();
  }

  @Override
  public void startPlayTrackOperation(String id, long begin, long duration) {
    log.info(String.format("Beginning track play of %s at %d:%d", id, begin, duration));

    PlayableAudioTrack ptrack = musicRepository.getPlayableTrack(id);
    if (ptrack != null) {
      startNewOperation(new PlayTrackJukeboxOperation(ptrack, begin, duration, configuration,
          trackPlayerFactory, executorService, listener, log));
    } else {
      log.warn(String.format("Unable to find track %s", id));
    }
  }

  @Override
  public void startShuffleTrackOperation() {
    log.info("Beginning shuffle play");

    startNewOperation(new ShuffleJukeboxOperation(tracksAlreadyPlayed, configuration,
        musicRepository, trackPlayerFactory, executorService, listener, log));
  }

  @Override
  public void shutdownCurrentOperation() {
    if (currentOperation != null) {
      currentOperation.stop();

      currentOperation = null;
    }
  }

  /**
   * Start up a new operation.
   *
   * <p>
   * If there is an old one, it will be stopped.
   *
   * @param newOperation
   *          the new operation to run
   */
  private void startNewOperation(JukeboxOperation newOperation) {
    shutdownCurrentOperation();
    currentOperation = newOperation;
    newOperation.start();
  }
}
