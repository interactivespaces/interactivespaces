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

package interactivespaces.activity.music.jukebox.internal;

import interactivespaces.service.audio.player.PlayableAudioTrack;

/**
 * A listener for {@link JukeboxOperation} events.
 * 
 * @author Keith M. Hughes
 */
public interface JukeboxOperationListener {
	/**
	 * A track is being started.
	 * 
	 * @param operation
	 *            the operation starting to play the track
	 * @param track
	 *            the track being started
	 */
	void onTrackStart(JukeboxOperation operation, PlayableAudioTrack track);

	/**
	 * A track is done.
	 * 
	 * @param operation
	 *            the operation starting to play the track
	 * @param track
	 *            the track being completed
	 */
	void onTrackStop(JukeboxOperation operation, PlayableAudioTrack track);

	/**
	 * This operation is done running.
	 * 
	 * @param operation
	 *            the operation which completed
	 */
	void onOperationComplete(JukeboxOperation operation);
}
