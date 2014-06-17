/*
 * Copyright (C) 2014 Google Inc.
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


/**
 * A listener for {@link AudioTrackPlayer} events.
 *
 * @author Keith M. Hughes
 */
public interface AudioTrackPlayerListener {

  /**
   * A track is being started.
   *
   * @param player
   *          the player which will play the track
   * @param track
   *          the track being started
   */
  void onAudioTrackStart(AudioTrackPlayer player, PlayableAudioTrack track);

  /**
   * A track is done.
   *
   * @param player
   *          the player which completed playing the track
   * @param track
   *          the track being completed
   */
  void onAudioTrackStop(AudioTrackPlayer player, PlayableAudioTrack track);
}
