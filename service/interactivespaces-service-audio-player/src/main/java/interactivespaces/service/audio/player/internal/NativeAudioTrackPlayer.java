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

import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.text.MessageFormat;
import java.util.Map;

/**
 * A {@link AudioTrackPlayer} which uses a {@link NativeActivityRunner}.
 *
 * @author Keith M. Hughes
 */
public class NativeAudioTrackPlayer implements AudioTrackPlayer {

  /**
   * The executable flags for running the audio player.
   */
  public static final String CONFIGURATION_PROPERTY_MUSIC_PLAYER_EXECUTABLE_FLAGS =
      "space.service.music.player.flags.linux";

  /**
   * The executable pathname for the audio player.
   */
  public static final String CONFIGURATION_PROPERTY_MUSIC_PLAYER_EXECUTABLE =
      "space.service.music.player.executable.linux";

  /**
   * The configuration to get track player info from.
   */
  private Configuration configuration;

  /**
   * The activity runner factory for running the activity.
   */
  private NativeActivityRunnerFactory runnerFactory;

  /**
   * The activity runner running the activity.
   */
  private NativeActivityRunner runner;

  /**
   * The track to be played.
   */
  private PlayableAudioTrack ptrack;

  /**
   * The log to use.
   */
  private Log log;

  /**
   * Construct a player.
   *
   * @param configuration
   *          the configuration for the player
   * @param runnerFactory
   *          the factory for application runners
   * @param ptrack
   *          the track to play
   * @param log
   *          the logger to use
   */
  public NativeAudioTrackPlayer(Configuration configuration, NativeActivityRunnerFactory runnerFactory,
      PlayableAudioTrack ptrack, Log log) {
    this.configuration = configuration;
    this.runnerFactory = runnerFactory;
    this.ptrack = ptrack;
    this.log = log;
  }

  @Override
  public synchronized void start(long begin, long duration) {
    if (runner == null) {
      runner = runnerFactory.newPlatformNativeActivityRunner(log);
      Map<String, Object> appConfig = Maps.newHashMap();
      // TODO(keith): This needs to get the OS, or it needs to be wrapped
      // so that can have
      // something else check for OS
      appConfig.put(NativeActivityRunner.EXECUTABLE_PATHNAME,
          configuration.getRequiredPropertyString(CONFIGURATION_PROPERTY_MUSIC_PLAYER_EXECUTABLE));

      String commandFlags =
          MessageFormat.format(configuration
              .getRequiredPropertyString(CONFIGURATION_PROPERTY_MUSIC_PLAYER_EXECUTABLE_FLAGS), ptrack.getFile()
              .getAbsolutePath());

      appConfig.put(NativeActivityRunner.EXECUTABLE_FLAGS, commandFlags);

      runner.configure(appConfig);
      runner.startup();
    }
  }

  @Override
  public synchronized void stop() {
    if (runner != null) {
      runner.shutdown();
      runner = null;
    }
  }

  @Override
  public synchronized boolean isPlaying() {
    return runner.isRunning();
  }
}
