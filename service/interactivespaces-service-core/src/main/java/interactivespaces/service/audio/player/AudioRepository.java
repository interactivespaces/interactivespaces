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

import interactivespaces.util.resource.ManagedResource;

import java.util.Collection;

/**
 * Repository of all music.
 *
 * @author Keith M. Hughes
 */
public interface AudioRepository extends ManagedResource {

  /**
   * Get information about a track from its ID.
   *
   * @param id
   *          the id of the track
   *
   * @return the information about the track, or {@code null} if there is no
   *         track with the ID.
   */
  AudioTrack getTrackData(String id);

  /**
   * Get all tracks in the repository.
   *
   * @return all tracks in the repository
   */
  Collection<PlayableAudioTrack> getAllPlayableTracks();

  /**
   * Get the track from its ID.
   *
   * @param id
   *          the id of the track
   *
   * @return the track, or {@code null} if there is no track with the ID
   */
  PlayableAudioTrack getPlayableTrack(String id);
}
