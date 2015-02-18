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

package interactivespaces.service.audio.player.internal.binary;

import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.FilePlayableAudioTrack;
import interactivespaces.service.audio.player.support.BaseAudioTrackPlayer;
import interactivespaces.util.process.NativeApplicationRunner;
import interactivespaces.util.process.NativeApplicationRunnerFactory;

import org.apache.commons.logging.Log;

import java.text.MessageFormat;

/**
 * A {@link AudioTrackPlayer} which uses a {@link NativeActivityRunner}.
 *
 * @author Keith M. Hughes
 */
public class NativeAudioTrackPlayer extends BaseAudioTrackPlayer {

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
  private NativeApplicationRunnerFactory runnerFactory;

  /**
   * The activity runner running the activity.
   */
  private NativeApplicationRunner runner;

  /**
   * Construct a new native player.
   *
   * @param runnerFactory
   *          factory for runners
   * @param log
   *          logger to use
   */
  public NativeAudioTrackPlayer(NativeApplicationRunnerFactory runnerFactory, Log log) {
    super(log);
    this.runnerFactory = runnerFactory;
  }

  @Override
  public void shutdown() {
    stop();
  }

  @Override
  public synchronized void start(FilePlayableAudioTrack track) {
    // TODO(keith): Fix once application runners have callbacks to not have all
    // the threading crap here.
    if (runner == null) {
      runner = runnerFactory.newPlatformNativeApplicationRunner(log);

      // TODO(keith): This needs to get the OS, or it needs to be wrapped
      // so that can have something else check for OS
      runner.setExecutablePath(configuration.getRequiredPropertyString(CONFIGURATION_PROPERTY_MUSIC_PLAYER_EXECUTABLE));

      runner.parseCommandArguments(MessageFormat.format(configuration
          .getRequiredPropertyString(CONFIGURATION_PROPERTY_MUSIC_PLAYER_EXECUTABLE_FLAGS), track.getFile()
          .getAbsolutePath()));

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
