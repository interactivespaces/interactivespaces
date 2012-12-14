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

package interactivespaces.service.audio.player.internal;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;

import org.apache.commons.logging.Log;

/**
 * A {@link TrackPlayerfactory} for {@link FakeAudioTrackPlayer} instances.
 * 
 * @author Keith M. Hughes
 */
public class FakeAudioTrackPlayerFactory implements AudioTrackPlayerFactory {
	/**
	 * A fake track length, in milliseconds.
	 */
	private static final int FAKE_TRACK_LENGTH = 2000;

	@Override
	public AudioTrackPlayer newTrackPlayer(PlayableAudioTrack track,
			Configuration configuration, Log log) {
		return new FakeAudioTrackPlayer(track, FAKE_TRACK_LENGTH, log);
	}
}
