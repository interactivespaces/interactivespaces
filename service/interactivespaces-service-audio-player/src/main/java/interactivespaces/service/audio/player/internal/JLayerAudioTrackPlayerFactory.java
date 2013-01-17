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

package interactivespaces.service.audio.player.internal;

import org.apache.commons.logging.Log;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;
import interactivespaces.service.audio.player.PlayableAudioTrack;

/**
 * An audio track player factory for JLayer players.
 *
 * @author Keith M. Hughes
 */
public class JLayerAudioTrackPlayerFactory implements AudioTrackPlayerFactory {

	@Override
	public AudioTrackPlayer newTrackPlayer(PlayableAudioTrack track,
			Configuration configuration, Log log) {
		return new JLayerAudioTrackPlayer(configuration, track, log);
	}
}
