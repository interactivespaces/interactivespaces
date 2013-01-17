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

package interactivespaces.activity.music.jukebox;

import interactivespaces.activity.impl.ros.BaseRosActivity;
import interactivespaces.activity.music.jukebox.internal.JukeboxOperation;
import interactivespaces.activity.music.jukebox.internal.JukeboxOperationListener;
import interactivespaces.activity.music.jukebox.internal.PlayTrackJukeboxOperation;
import interactivespaces.activity.music.jukebox.internal.ShuffleJukeboxOperation;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrack;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.internal.JLayerAudioTrackPlayerFactory;
import interactivespaces.service.audio.player.internal.ScanningFileAudioRepository;
import interactivespaces.util.ros.RosPublishers;
import interactivespaces.util.ros.RosSubscribers;

import java.util.Set;

import org.ros.message.MessageListener;
import org.ros.message.interactivespaces_msgs.MusicJukeboxAnnounce;
import org.ros.message.interactivespaces_msgs.MusicJukeboxControl;
import org.ros.node.Node;

import com.google.common.collect.Sets;

/**
 * The Music Jukebox activity.
 * 
 * @author Keith M. Hughes
 */
public class MusicJukeboxActivity extends BaseRosActivity implements
		JukeboxOperationListener {

	public static final String CONFIGURATION_MUSIC_JUKEBOX_CONTROL_ROS_TOPIC_NAME = "music.jukebox.control.ros.topic.name";

	public static final String CONFIGURATION_MUSIC_JUKEBOX_ANNOUNCE_ROS_TOPIC_NAME = "music.jukebox.announce.ros.topic.name";

	/**
	 * The music repository this is a jukebox for.
	 */
	private AudioRepository musicRepository;

	/**
	 * ROS subscribers for the jukebox control messages.
	 */
	private RosSubscribers<MusicJukeboxControl> jukeboxControlSubscribers;

	/**
	 * ROS subscribers for the jukebox announcement messages.
	 */
	private RosPublishers<MusicJukeboxAnnounce> jukeboxAnnouncePublishers;

	/**
	 * A set of all tracks played since this jukebox was started.
	 */
	private Set<PlayableAudioTrack> tracksAlreadyPlayed;

	/**
	 * Current operation the jukebox is doing. Can be {@code null} if nothing is
	 * happening.
	 */
	private JukeboxOperation currentOperation;

	/**
	 * The factory for track players.
	 */
	private AudioTrackPlayerFactory trackPlayerFactory;

	@Override
	public void onActivityStartup() {
		getLog().info("Music jukebox starting!");

		try {
			Configuration configuration = getConfiguration();

			tracksAlreadyPlayed = Sets.newHashSet();

			// TODO(keith): Get from a service repository.
			trackPlayerFactory = new JLayerAudioTrackPlayerFactory();

			setupRosTopics(configuration);
			startMusicRepository(configuration);

			getLog().info("Music jukebox ready to spin the tunes!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityCleanup() {
		shutdownCurrentOperation();

		if (jukeboxAnnouncePublishers != null) {
			jukeboxAnnouncePublishers.shutdown();
			jukeboxAnnouncePublishers = null;
		}

		if (jukeboxControlSubscribers != null) {
			jukeboxControlSubscribers.shutdown();
			jukeboxControlSubscribers = null;
		}

		if (musicRepository != null) {
			musicRepository.shutdown();
			musicRepository = null;
		}
	}

	@Override
	public void onActivityActivate() {
		startShuffleTrackOperation();
	}

	@Override
	public void onActivityDeactivate() {
		shutdownCurrentOperation();
	}

	/**
	 * Set up all ROS topics for the jukebox.
	 * 
	 * @param configuration
	 *            the activity configuration
	 */
	private void setupRosTopics(Configuration configuration) {
		Node node = getMainNode();
		jukeboxControlSubscribers = new RosSubscribers<MusicJukeboxControl>(
				getLog());
		jukeboxControlSubscribers
				.addSubscribers(
						node,
						"interactivespaces_msgs/MusicJukeboxControl",
						configuration
								.getRequiredPropertyString(CONFIGURATION_MUSIC_JUKEBOX_CONTROL_ROS_TOPIC_NAME),
						new MessageListener<MusicJukeboxControl>() {
							@Override
							public void onNewMessage(MusicJukeboxControl request) {
								handleNewJukeboxCommand(request);
							}
						});

		jukeboxAnnouncePublishers = new RosPublishers<MusicJukeboxAnnounce>(
				getLog());
		jukeboxAnnouncePublishers
				.addPublishers(
						node,
						"interactivespaces_msgs/MusicJukeboxAnnounce",
						configuration
								.getRequiredPropertyString(CONFIGURATION_MUSIC_JUKEBOX_ANNOUNCE_ROS_TOPIC_NAME));
	}

	/**
	 * Start up the music repository.
	 */
	private void startMusicRepository(Configuration configuration) {
		musicRepository = new ScanningFileAudioRepository();

		musicRepository.setConfiguration(configuration);
		musicRepository.startup();
	}

	/**
	 * Received new jukebox control request.
	 * 
	 * @param request
	 *            The new request
	 */
	private void handleNewJukeboxCommand(MusicJukeboxControl request) {
		try {
			// TODO(keith): Add track queueing operation
			switch (request.operation) {
			case MusicJukeboxControl.OPERATION_PLAY_TRACK:
				startPlayTrackOperation(request.id, request.begin,
						request.duration);

				break;

			case MusicJukeboxControl.OPERATION_PAUSE:
				getLog().warn("Currently unsupported operation: pause");
				break;

			case MusicJukeboxControl.OPERATION_SHUFFLE:
				startShuffleTrackOperation();

				break;

			case MusicJukeboxControl.OPERATION_STOP:
				shutdownCurrentOperation();

				break;
			default:
				getLog().error(
						String.format("Unknown music jukebox command %d",
								request.operation));
			}
		} catch (Exception e) {
			getLog().error(
					String.format("Error during music jukebox command %d",
							request.operation), e);
		}
	}

	/**
	 * Start playing a track.
	 * 
	 * @param id
	 * @param begin
	 * @param duration
	 */
	private void startPlayTrackOperation(String id, long begin, long duration) {
		getLog().info(
				String.format("Beginning track play of %s at %d:%d", id, begin,
						duration));

		PlayableAudioTrack ptrack = musicRepository.getPlayableTrack(id);
		if (ptrack != null) {
			startNewOperation(new PlayTrackJukeboxOperation(ptrack, begin,
					duration, getConfiguration(), trackPlayerFactory,
					getController().getSpaceEnvironment().getExecutorService(),
					this, getLog()));
		} else {
			getLog().warn(String.format("Unable to find track %s", id));
		}
	}

	/**
	 * Start a shuffle operation.
	 */
	private void startShuffleTrackOperation() {
		getLog().info("Beginning shuffle play");

		startNewOperation(new ShuffleJukeboxOperation(tracksAlreadyPlayed,
				getConfiguration(), musicRepository, trackPlayerFactory,
				getController().getSpaceEnvironment().getExecutorService(),
				this, getLog()));
	}

	/**
	 * Start up a new operation.
	 * 
	 * <p>
	 * If there is an old one, it will be stopped.
	 * 
	 * @param newOperation
	 *            the new operation to run
	 */
	private void startNewOperation(JukeboxOperation newOperation) {
		shutdownCurrentOperation();
		currentOperation = newOperation;
		newOperation.start();
	}

	/**
	 * Shutdown and remove the current operation, if there is one.
	 */
	private void shutdownCurrentOperation() {
		if (currentOperation != null) {
			currentOperation.stop();

			currentOperation = null;
		}
	}

	@Override
	public void onTrackStart(JukeboxOperation operation, PlayableAudioTrack ptrack) {
		if (getLog().isInfoEnabled()) {
			getLog().info(String.format("Playing %s", ptrack));
		}

		MusicJukeboxAnnounce announce = new MusicJukeboxAnnounce();
		AudioTrack track = ptrack.getTrack();
		announce.title = track.getTitle();
		announce.artist = track.getArtist();
		announce.album = track.getAlbum();

		jukeboxAnnouncePublishers.publishMessage(announce);
	}

	@Override
	public void onTrackStop(JukeboxOperation operation, PlayableAudioTrack ptrack) {
		// Everyone gets told we have completed the track.
		if (getLog().isInfoEnabled()) {
			getLog().info(String.format("Done playing %s", ptrack));
		}

		MusicJukeboxAnnounce announce = new MusicJukeboxAnnounce();
		jukeboxAnnouncePublishers.publishMessage(announce);
	}

	@Override
	public void onOperationComplete(JukeboxOperation operation) {
		getLog().info("Operation completed");

		currentOperation = null;
	}

}
