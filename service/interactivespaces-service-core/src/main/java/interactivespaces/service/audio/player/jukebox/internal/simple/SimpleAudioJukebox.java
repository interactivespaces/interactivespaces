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

package interactivespaces.service.audio.player.jukebox.internal.simple;

import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.BaseAudioTrackPlayerListener;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.jukebox.AudioJukeboxListener;
import interactivespaces.service.audio.player.jukebox.support.BaseJukeboxOperation;
import interactivespaces.service.audio.player.jukebox.support.InternalAudioJukebox;
import interactivespaces.service.audio.player.jukebox.support.PlayTrackJukeboxOperation;
import interactivespaces.service.audio.player.jukebox.support.ShuffleJukeboxOperation;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * a very basic audio jukebox.
 *
 * @author Keith M. Hughes
 */
public class SimpleAudioJukebox implements InternalAudioJukebox {

  /**
   * The music repository this is a jukebox for.
   */
  private final AudioRepository musicRepository;

  /**
   * The factory for track players.
   */
  private final AudioTrackPlayer trackPlayer;

  /**
   * Current operation the jukebox is doing. Can be {@code null} if nothing is
   * happening.
   */
  private BaseJukeboxOperation currentOperation;

  /**
   * Logger for the jukebox.
   */
  private final Log log;

  /**
   * Listeners for jukebox events.
   */
  private final List<AudioJukeboxListener> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * Construct a new jukebox.
   *
   * @param musicRepository
   *          the repository the jukebox will play from
   * @param trackPlayer
   *          the player for audio tracks
   * @param log
   *          the logger to use
   */
  public SimpleAudioJukebox(AudioRepository musicRepository, AudioTrackPlayer trackPlayer, Log log) {
    this.musicRepository = musicRepository;
    this.trackPlayer = trackPlayer;
    this.log = log;

    trackPlayer.addListener(new BaseAudioTrackPlayerListener() {

      @Override
      public void onAudioTrackStop(AudioTrackPlayer player, PlayableAudioTrack track) {
        handleAudioTrackStop(track);
      }

      @Override
      public void onAudioTrackStart(AudioTrackPlayer player, PlayableAudioTrack track) {
        handleAudioTrackStart(track);
      }
    });
  }

  @Override
  public void addListener(AudioJukeboxListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(AudioJukeboxListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void startup() {
    trackPlayer.startup();
  }

  @Override
  public void shutdown() {
    shutdownCurrentOperation();
    trackPlayer.shutdown();
  }

  @Override
  public void startPlayTrackOperation(String id) {
    log.info(String.format("Beginning track play of %s", id));

    PlayableAudioTrack ptrack = musicRepository.getPlayableTrack(id);
    if (ptrack != null) {
      startNewOperation(new PlayTrackJukeboxOperation(this, ptrack, log));
    } else {
      log.warn(String.format("Unable to find track %s", id));
    }
  }

  @Override
  public void startShuffleTrackOperation() {
    log.info("Beginning shuffle play");

    startNewOperation(new ShuffleJukeboxOperation(this, musicRepository, log));
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
  private void startNewOperation(BaseJukeboxOperation newOperation) {
    shutdownCurrentOperation();
    currentOperation = newOperation;
    newOperation.start();
  }

  @Override
  public void playTrack(PlayableAudioTrack track) {
    trackPlayer.start(track);
  }

  @Override
  public void stopPlayingCurrentTrack() {
    trackPlayer.stop();
  }

  /**
   * The track player has started playing a track. Notify everyone who needs to
   * know.
   *
   * @param track
   *          the track
   */
  private void handleAudioTrackStart(PlayableAudioTrack track) {
    for (AudioJukeboxListener listener : listeners) {
      try {
        listener.onJukeboxTrackStart(this, track);
      } catch (Exception e) {
        log.error("Exception while processing track stop message", e);
      }
    }
  }

  /**
   * The track player has stopped playing a track. Notify everyone who needs to
   * know.
   *
   * @param track
   *          the track
   */
  private void handleAudioTrackStop(PlayableAudioTrack track) {
    for (AudioJukeboxListener listener : listeners) {
      try {
        listener.onJukeboxTrackStop(this, track);
      } catch (Exception e) {
        log.error("Exception while processing track stop message", e);
      }
    }

    currentOperation.handleTrackStop(track);
  }

  @Override
  public void notifyOperationComplete() {
    for (AudioJukeboxListener listener : listeners) {
      try {
        listener.onJukeboxOperationComplete(this);
      } catch (Exception e) {
        log.error("Exception while processing jukebox operation complete message", e);
      }
    }
  }
}
