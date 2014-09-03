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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.impl.ros.BaseRosActivity;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrack;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.internal.ScanningFileAudioRepository;
import interactivespaces.service.audio.player.internal.jlayer.JLayerAudioTrackPlayer;
import interactivespaces.service.audio.player.jukebox.AudioJukebox;
import interactivespaces.service.audio.player.jukebox.AudioJukeboxListener;
import interactivespaces.service.audio.player.jukebox.internal.simple.SimpleAudioJukebox;
import interactivespaces.util.ros.RosPublishers;
import interactivespaces.util.ros.RosSubscribers;

import interactivespaces_msgs.MusicJukeboxAnnounce;
import interactivespaces_msgs.MusicJukeboxControl;
import org.python.google.common.collect.Lists;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;

import java.io.File;
import java.util.List;

/**
 * The Music Jukebox activity.
 *
 * @author Keith M. Hughes
 */
public class MusicJukeboxActivity extends BaseRosActivity implements AudioJukeboxListener {

  /**
   * The configuration parameter name for the base folders for obtaining music.
   */
  public static final String CONFIGURATION_MUSIC_JUKEBOX_MUSIC_REPOSITORY_BASE = "space.music.repository.base";

  /**
   * ROS topic name for jukebox control.
   */
  public static final String CONFIGURATION_MUSIC_JUKEBOX_CONTROL_ROS_TOPIC_NAME =
      "music.jukebox.control.ros.topic.name";

  /**
   * ROS topic name for announcing what is currently playing.
   */
  public static final String CONFIGURATION_MUSIC_JUKEBOX_ANNOUNCE_ROS_TOPIC_NAME =
      "music.jukebox.announce.ros.topic.name";

  /**
   * The music repository this is a jukebox for.
   */
  private ScanningFileAudioRepository musicRepository;

  /**
   * ROS subscribers for the jukebox control messages.
   */
  private RosSubscribers<MusicJukeboxControl> jukeboxControlSubscribers;

  /**
   * ROS subscribers for the jukebox announcement messages.
   */
  private RosPublishers<MusicJukeboxAnnounce> jukeboxAnnouncePublishers;

  /**
   * The jukebox to use for playing.
   */
  private AudioJukebox jukebox;

  /**
   * Message factory for creating messages.
   */
  private MessageFactory rosMessageFactory;

  @Override
  public void onActivityStartup() {
    getLog().info("Music jukebox starting!");

    Configuration configuration = getConfiguration();

    setupRosTopics(configuration);
    startMusicRepository(configuration);

    AudioTrackPlayer trackPlayer = new JLayerAudioTrackPlayer(getSpaceEnvironment().getExecutorService(), getLog());
    addManagedResource(trackPlayer);

    jukebox = new SimpleAudioJukebox(musicRepository, trackPlayer, getLog());
    jukebox.addListener(this);

    addManagedResource(jukebox);

    getLog().info("Music jukebox ready to spin the tunes!");
  }

  @Override
  public void onActivityCleanup() {
    if (jukeboxAnnouncePublishers != null) {
      jukeboxAnnouncePublishers.shutdown();
      jukeboxAnnouncePublishers = null;
    }

    if (jukeboxControlSubscribers != null) {
      jukeboxControlSubscribers.shutdown();
      jukeboxControlSubscribers = null;
    }
  }

  @Override
  public void onActivityActivate() {
    jukebox.startShuffleTrackOperation();
  }

  @Override
  public void onActivityDeactivate() {
    jukebox.shutdownCurrentOperation();
  }

  /**
   * Set up all ROS topics for the jukebox.
   *
   * @param configuration
   *          the activity configuration
   */
  private void setupRosTopics(Configuration configuration) {
    ConnectedNode node = getMainNode();
    rosMessageFactory = node.getTopicMessageFactory();

    jukeboxControlSubscribers = new RosSubscribers<MusicJukeboxControl>(getLog());
    jukeboxControlSubscribers.addSubscribers(node, "interactivespaces_msgs/MusicJukeboxControl",
        configuration.getRequiredPropertyString(CONFIGURATION_MUSIC_JUKEBOX_CONTROL_ROS_TOPIC_NAME),
        new MessageListener<MusicJukeboxControl>() {
          @Override
          public void onNewMessage(MusicJukeboxControl request) {
            handleNewJukeboxCommand(request);
          }
        });

    jukeboxAnnouncePublishers = new RosPublishers<MusicJukeboxAnnounce>(getLog());
    jukeboxAnnouncePublishers.addPublishers(node, "interactivespaces_msgs/MusicJukeboxAnnounce",
        configuration.getRequiredPropertyString(CONFIGURATION_MUSIC_JUKEBOX_ANNOUNCE_ROS_TOPIC_NAME));
  }

  /**
   * Start up the music repository.
   */
  private void startMusicRepository(Configuration configuration) {
    List<String> baseRepositoryFiles =
        configuration.getPropertyStringList(CONFIGURATION_MUSIC_JUKEBOX_MUSIC_REPOSITORY_BASE, ":");
    if (baseRepositoryFiles != null) {
      List<File> baseRespositories = Lists.newArrayList();
      for (String baseRepositoryFile : baseRepositoryFiles) {

      }
      musicRepository = new ScanningFileAudioRepository();

      musicRepository.setRepositoryBases(baseRespositories);
      addManagedResource(musicRepository);
    } else {
      throw new SimpleInteractiveSpacesException(String.format("No configuration parameter %s",
          CONFIGURATION_MUSIC_JUKEBOX_MUSIC_REPOSITORY_BASE));
    }
  }

  /**
   * Received new jukebox control request.
   *
   * @param request
   *          The new request
   */
  private void handleNewJukeboxCommand(MusicJukeboxControl request) {
    try {
      // TODO(keith): Add track queueing operation
      switch (request.getOperation()) {
        case MusicJukeboxControl.OPERATION_PLAY_TRACK:
          jukebox.startPlayTrackOperation(request.getId());

          break;

        case MusicJukeboxControl.OPERATION_PAUSE:
          getLog().warn("Currently unsupported operation: pause");
          break;

        case MusicJukeboxControl.OPERATION_SHUFFLE:
          jukebox.startShuffleTrackOperation();

          break;

        case MusicJukeboxControl.OPERATION_STOP:
          jukebox.shutdownCurrentOperation();

          break;
        default:
          getLog().error(String.format("Unknown music jukebox command %d", request.getOperation()));
      }
    } catch (Exception e) {
      getLog().error(String.format("Error during music jukebox command %d", request.getOperation()), e);
    }
  }

  @Override
  public void onJukeboxTrackStart(AudioJukebox jukebox, PlayableAudioTrack ptrack) {
    if (getLog().isInfoEnabled()) {
      getLog().info(String.format("Playing %s", ptrack));
    }

    MusicJukeboxAnnounce announce = rosMessageFactory.newFromType(MusicJukeboxAnnounce._TYPE);
    AudioTrack track = ptrack.getTrack();
    announce.setTitle(track.getTitle());
    announce.setArtist(track.getArtist());
    announce.setAlbum(track.getAlbum());

    jukeboxAnnouncePublishers.publishMessage(announce);
  }

  @Override
  public void onJukeboxTrackStop(AudioJukebox jukebox, PlayableAudioTrack ptrack) {
    // Everyone gets told we have completed the track.
    if (getLog().isInfoEnabled()) {
      getLog().info(String.format("Done playing %s", ptrack));
    }

    MusicJukeboxAnnounce announce = rosMessageFactory.newFromType(MusicJukeboxAnnounce._TYPE);
    jukeboxAnnouncePublishers.publishMessage(announce);
  }

  @Override
  public void onJukeboxOperationComplete(AudioJukebox jukebox) {
    getLog().info("Operation completed");
  }

}
