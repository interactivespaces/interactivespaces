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

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.music.PlayableTrack;
import interactivespaces.service.music.TrackPlayer;
import interactivespaces.service.music.TrackPlayerFactory;

import org.apache.commons.logging.Log;

/**
 * A {@link TrackPlayerFactory} which gives track players which are natively
 * run.
 * 
 * @author Keith M. Hughes
 */
public class NativeTrackPlayerFactory implements TrackPlayerFactory {
	/**
	 * The factory for native activity runners.
	 */
	private NativeActivityRunnerFactory runnerFactory;

	/**
	 * The log to use.
	 */
	private Log log;

	public NativeTrackPlayerFactory(NativeActivityRunnerFactory runnerFactory,
			Log log) {
		this.runnerFactory = runnerFactory;
		this.log = log;
	}

	@Override
	public TrackPlayer newTrackPlayer(PlayableTrack ptrack,
			Configuration configuration, Log log) {
		return new NativeTrackPlayer(configuration, runnerFactory, ptrack, log);
	}
}
