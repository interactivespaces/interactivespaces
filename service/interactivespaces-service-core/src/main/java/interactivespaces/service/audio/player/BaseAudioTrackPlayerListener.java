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
 * A base {@link AudioTrackPlayerListener} with blank methods for all callbacks.
 *
 * @author Keith M. Hughes
 */
public class BaseAudioTrackPlayerListener implements AudioTrackPlayerListener {

  @Override
  public void onAudioTrackStart(AudioTrackPlayer player, FilePlayableAudioTrack track) {
    // Default is do nothing
  }

  @Override
  public void onAudioTrackStop(AudioTrackPlayer player, FilePlayableAudioTrack track) {
    // Default is do nothing
  }
}
