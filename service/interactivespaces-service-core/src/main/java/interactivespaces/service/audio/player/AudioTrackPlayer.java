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

package interactivespaces.service.audio.player;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.resource.ManagedResource;

/**
 * Something which can play audio tracks.
 *
 * @author Keith M. Hughes
 */
public interface AudioTrackPlayer extends ManagedResource {

  /**
   * Add a listener to the jukebox.
   *
   * @param listener
   *          the listener to use
   */
  void addListener(AudioTrackPlayerListener listener);

  /**
   * Add a listener to the jukebox.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(AudioTrackPlayerListener listener);

  /**
   * Playing a track.
   *
   * @param track
   *          the audio track to play
   *
   * @throws InteractiveSpacesException
   *           the player was playing a track already or the track could not be
   *           found
   */
  void start(PlayableAudioTrack track) throws InteractiveSpacesException;

  /**
   * Stop playing the track if one is playing.
   */
  void stop();

  /**
   * Is the track playing?
   *
   * @return {@code true} if the track is playing
   */
  boolean isPlaying();
}
