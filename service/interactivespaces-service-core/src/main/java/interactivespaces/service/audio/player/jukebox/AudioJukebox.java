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

import interactivespaces.util.resource.ManagedResource;

/**
 * A jukebox for playing audio.
 *
 * @author Keith M. Hughes
 */
public interface AudioJukebox extends ManagedResource {

  /**
   * Add a listener to the jukebox.
   *
   * @param listener
   *          the listener to use
   */
  void addListener(AudioJukeboxListener listener);

  /**
   * Add a listener to the jukebox.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(AudioJukeboxListener listener);

  /**
   * Start playing a track.
   *
   * @param id
   *          ID of the track to play
   */
  void startPlayTrackOperation(String id);

  /**
   * Start a shuffle operation.
   */
  void startShuffleTrackOperation();

  /**
   * Shutdown and remove the current operation, if there is one.
   */
  void shutdownCurrentOperation();
}
