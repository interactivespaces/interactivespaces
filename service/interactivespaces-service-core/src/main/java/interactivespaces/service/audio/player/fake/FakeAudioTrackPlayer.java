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

package interactivespaces.service.audio.player.fake;

import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import org.apache.commons.logging.Log;

/**
 * A fake {@link AudioTrackPlayer}. It will merely write events on stdout.
 * 
 * @author Keith M. Hughes
 */
public class FakeAudioTrackPlayer implements AudioTrackPlayer {
	
	/**
	 * The track being played.
	 */
	private PlayableAudioTrack track;

	/**
	 * The number of milliseconds in the "track".
	 */
	private long trackLength;

	/**
	 * When the track started to play
	 */
	private Long playStart;

	/**
	 * Log to write info out on.
	 */
	private Log log;

	public FakeAudioTrackPlayer(PlayableAudioTrack track, long trackLength, Log log) {
		this.track = track;
		this.trackLength = trackLength;
		this.log = log;
	}

	@Override
	public void start(long start, long duration) {
		playStart = System.currentTimeMillis();

		log.info(String.format("Playing track %s starting at %d for %d msecs",
				track, playStart, trackLength));
	}

	@Override
	public void stop() {
		playStart = null;

		log.info(String.format("Done playing track %s", track));
	}

	@Override
	public boolean isPlaying() {
		if (playStart != null) {
			long played = System.currentTimeMillis() - playStart;
			log.info(String.format("Playing track %s at %d of %d msecs",
					track, played, trackLength));
			
			return played < trackLength;
		} else {
			return false;
		}
	}

}
