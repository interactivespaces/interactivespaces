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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.apache.commons.logging.Log;

/**
 * An audio track player which uses JLayer.
 * 
 * @author Keith M. Hughes
 */
public class JLayerAudioTrackPlayer implements AudioTrackPlayer {

	/**
	 * The configuration to get track player info from.
	 */
	private Configuration configuration;

	/**
	 * The track to be played.
	 */
	private PlayableAudioTrack ptrack;

	/**
	 * The log to use.
	 */
	private Log log;

	/**
	 * The player.
	 */
	private AdvancedPlayer player;

	private FileInputStream istream;
	
	private AtomicBoolean playing = new AtomicBoolean(false);

	public JLayerAudioTrackPlayer(Configuration configuration,
			PlayableAudioTrack ptrack, Log log) {
		this.configuration = configuration;
		this.ptrack = ptrack;
		this.log = log;
	}

	@Override
	public void start(long begin, long duration) {
		File file = ptrack.getFile();
		if (!file.exists()) {
			throw new InteractiveSpacesException((String.format(
					"Cannot find audio file %s", file.getAbsolutePath())));
		}

		try {
			istream = new FileInputStream(file);

			player = new AdvancedPlayer(istream);
			player.setPlayBackListener(new PlaybackListener() {

				@Override
				public void playbackFinished(PlaybackEvent event) {
					playing.set(false);
				}

				@Override
				public void playbackStarted(PlaybackEvent event) {
					playing.set(true);
				} 
				
			});
			
			player.play();
		} catch (Exception e) {
			throw new InteractiveSpacesException((String.format(
					"Cannot create audio player for file %s",
					file.getAbsolutePath())), e);
		}
	}

	@Override
	public void stop() {
		player.stop();
	}

	@Override
	public boolean isPlaying() {
		return playing.get();
	}
}
