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

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;

import org.apache.commons.logging.Log;

/**
 * A {@link AudioTrackPlayerFactory} which gives track players which are natively
 * run.
 * 
 * @author Keith M. Hughes
 */
public class NativeAudioTrackPlayerFactory implements AudioTrackPlayerFactory {
	/**
	 * The factory for native activity runners.
	 */
	private NativeActivityRunnerFactory runnerFactory;

	/**
	 * The log to use.
	 */
	private Log log;

	public NativeAudioTrackPlayerFactory(NativeActivityRunnerFactory runnerFactory,
			Log log) {
		this.runnerFactory = runnerFactory;
		this.log = log;
	}

	@Override
	public AudioTrackPlayer newTrackPlayer(PlayableAudioTrack ptrack,
			Configuration configuration, Log log) {
		return new NativeAudioTrackPlayer(configuration, runnerFactory, ptrack, log);
	}
}
