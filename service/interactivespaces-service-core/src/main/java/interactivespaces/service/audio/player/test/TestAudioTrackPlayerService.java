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

package interactivespaces.service.audio.player.test;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.audio.player.AudioTrackPlayer;
import interactivespaces.service.audio.player.AudioTrackPlayerService;

import org.apache.commons.logging.Log;

/**
 * An {@link AudioTrackPlayerService} for {@link TestAudioTrackPlayer}
 * instances.
 *
 * @author Keith M. Hughes
 */
public class TestAudioTrackPlayerService extends BaseSupportedService implements AudioTrackPlayerService {

  /**
   * A test track length, in milliseconds.
   */
  private static final long TEST_TRACK_LENGTH_DEFAULT = 2000;

  /**
   * The current test track length, in milliseconds.
   */
  private long testTrackLength = TEST_TRACK_LENGTH_DEFAULT;

  @Override
  public String getName() {
    return AudioTrackPlayerService.SERVICE_NAME;
  }

  @Override
  public AudioTrackPlayer newTrackPlayer(Log log) {
    return new TestAudioTrackPlayer(testTrackLength, getSpaceEnvironment().getExecutorService(), log);
  }

  /**
   * Set the test length for tracks.
   *
   * @param testTrackLength
   *          the test track length, in milliseconds
   */
  public void setTestTrackLength(long testTrackLength) {
    this.testTrackLength = testTrackLength;
  }
}
