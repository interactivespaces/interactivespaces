/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.service.audio.player.jukebox.internal.simple;

import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.SimpleAudioTrack;
import interactivespaces.service.audio.player.jukebox.AudioJukebox;
import interactivespaces.service.audio.player.jukebox.AudioJukeboxListener;
import interactivespaces.service.audio.player.support.InMemoryAudioRepository;
import interactivespaces.service.audio.player.test.TestAudioTrackPlayer;
import interactivespaces.system.StandaloneInteractiveSpacesEnvironment;

import com.google.common.collect.Sets;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the {@link SimpleAudioJukebox}.
 *
 * @author Keith M. Hughes
 */
public class SimpleAudioJukeboxTest {

  private StandaloneInteractiveSpacesEnvironment spaceEnvironment;
  private SimpleAudioJukebox jukebox;
  private PlayableAudioTrack ptrack1;
  private PlayableAudioTrack ptrack2;

  @Before
  public void setup() {
    spaceEnvironment = StandaloneInteractiveSpacesEnvironment.newStandaloneInteractiveSpacesEnvironment();

    InMemoryAudioRepository repository = new InMemoryAudioRepository();
    ptrack1 = new PlayableAudioTrack(new SimpleAudioTrack("1", "foo", null, null), null);
    ptrack2 = new PlayableAudioTrack(new SimpleAudioTrack("2", "bar", null, null), null);
    repository.addTrack(ptrack1);
    repository.addTrack(ptrack2);

    TestAudioTrackPlayer trackPlayer =
        new TestAudioTrackPlayer(10, spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());

    jukebox = new SimpleAudioJukebox(repository, trackPlayer, spaceEnvironment.getLog());
  }

  @After
  public void cleanup() {
    jukebox.shutdown();
    spaceEnvironment.shutdown();
  }

  @Test
  public void testShufflePlay() throws Exception {
    final Set<String> stopContent = Sets.newHashSet();
    final Set<String> startContent = Sets.newHashSet();
    final CountDownLatch completed = new CountDownLatch(1);

    jukebox.addListener(new AudioJukeboxListener() {

      @Override
      public void onJukeboxTrackStop(AudioJukebox jukebox, PlayableAudioTrack track) {
        stopContent.add(track.getTrack().getId());
      }

      @Override
      public void onJukeboxTrackStart(AudioJukebox jukebox, PlayableAudioTrack track) {
        startContent.add(track.getTrack().getId());
      }

      @Override
      public void onJukeboxOperationComplete(AudioJukebox jukebox) {
        completed.countDown();
      }
    });
    jukebox.startup();
    jukebox.startShuffleTrackOperation();

    Set<String> expected = Sets.newHashSet(ptrack1.getTrack().getId(), ptrack2.getTrack().getId());

    Assert.assertTrue(completed.await(5, TimeUnit.SECONDS));
    Assert.assertEquals(expected, startContent);
    Assert.assertEquals(expected, stopContent);
  }
}
