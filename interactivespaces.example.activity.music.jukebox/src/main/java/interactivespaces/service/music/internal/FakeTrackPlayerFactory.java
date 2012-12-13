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

package interactivespaces.service.music.internal;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.music.PlayableTrack;
import interactivespaces.service.music.TrackPlayer;
import interactivespaces.service.music.TrackPlayerFactory;

import org.apache.commons.logging.Log;

/**
 * A {@link TrackPlayerfactory} for {@link FakeTrackPlayer} instances.
 * 
 * @author Keith M. Hughes
 */
public class FakeTrackPlayerFactory implements TrackPlayerFactory {
	/**
	 * A fake track length, in milliseconds.
	 */
	private static final int FAKE_TRACK_LENGTH = 2000;

	@Override
	public TrackPlayer newTrackPlayer(PlayableTrack track,
			Configuration configuration, Log log) {
		return new FakeTrackPlayer(track, FAKE_TRACK_LENGTH, log);
	}
}
