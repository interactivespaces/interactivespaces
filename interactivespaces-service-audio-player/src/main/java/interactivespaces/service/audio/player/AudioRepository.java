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

import interactivespaces.configuration.Configuration;

import java.util.Collection;

/**
 * Repository of all music.
 * 
 * @author Keith M. Hughes
 */
public interface AudioRepository {

	/**
	 * Start the repository up.
	 */
	void startup();

	/**
	 * Shut the repository down.
	 */
	void shutdown();

	/**
	 * Set the configuration for the repository to use.
	 * 
	 * @param configuration
	 */
	void setConfiguration(Configuration configuration);

	/**
	 * Get information about a track from its ID.
	 * 
	 * @param id
	 *            the id of the track
	 * 
	 * @return the information about the track, or {@code null} if there is no
	 *         track with the ID.
	 */
	AudioTrack getTrackData(String id);

	/**
	 * Get information about a track from its ID.
	 * 
	 * @param id
	 *            the id of the track
	 * 
	 * @return the information about the track, or {@code null} if there is no
	 *         track with the ID.
	 */
	PlayableAudioTrack getPlayableTrack(String id);

	/**
	 * Get a random track to play.
	 * 
	 * <p>
	 * The returned track will not be found in the tracksPlayed and will not be
	 * added to the collection. If all tracks in the repository have been played,
	 * the collection will be cleared and all tracks become possible.
	 * 
	 * @param tracksPlayed
	 *            tracks to ignore
	 * 
	 * @return a new track to play
	 */
	PlayableAudioTrack getRandomTrack(Collection<PlayableAudioTrack> tracksPlayed);
}
