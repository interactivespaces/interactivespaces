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

package interactivespaces.master.server.services.internal;

import static org.mockito.Mockito.when;

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActiveLiveActivityGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * tests for a {@link BasicActiveSpaceManager}.
 *
 * @author Keith M. Hughes
 */
public class ActiveSpaceManagerTest extends BaseSpaceTest {

  private BasicActiveSpaceManager activeSpaceManager;

  private InternalActiveControllerManager activeControllerManager;

  @Before
  public void setup() {
    baseSetup();

    activeControllerManager = Mockito.mock(InternalActiveControllerManager.class);

    activeSpaceManager = getActiveSpaceManager();
    activeSpaceManager.setActiveControllerManager(activeControllerManager);
  }

  /**
   * Make sure all elements of the space tree deploy.
   */
  @Test
  public void testEntireSpaceDeployment() {
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            space(3).addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.deploySpace(spaceTree);

    Mockito.verify(activeControllerManager, Mockito.times(1)).deployActiveLiveActivityGroupChecked(
        Mockito.eq(agroup1), Mockito.anySet());
    Mockito.verify(activeControllerManager, Mockito.times(1)).deployActiveLiveActivityGroupChecked(
        Mockito.eq(agroup2), Mockito.anySet());
    Mockito.verify(activeControllerManager, Mockito.times(1)).deployActiveLiveActivityGroupChecked(
        Mockito.eq(agroup3), Mockito.anySet());
  }

  /**
   * Don't deploy the entire tree, just a subtree.
   */
  @Test
  public void testSubSpaceDeploy() {
    Space subspace;
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(
            space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            subspace =
                space(3)
                    .addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.deploySpace(subspace);

    Mockito.verify(activeControllerManager, Mockito.never()).deployActiveLiveActivityGroupChecked(
        Mockito.eq(agroup1), Mockito.anySet());
    Mockito.verify(activeControllerManager, Mockito.never()).deployActiveLiveActivityGroupChecked(
        Mockito.eq(agroup2), Mockito.anySet());
    Mockito.verify(activeControllerManager, Mockito.times(1)).deployActiveLiveActivityGroupChecked(
        Mockito.eq(agroup3), Mockito.anySet());
  }

  /**
   * Make sure all elements of the space tree start up.
   */
  @Test
  public void testEntireSpaceStartup() {
    LiveActivityGroup group1, group2, group3;
    Space spaceTree =
        space(0).addSpaces(space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            space(3).addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.startupSpace(spaceTree);

    Mockito.verify(activeControllerManager, Mockito.times(1)).startupActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.times(1)).startupActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1)).startupActiveActivityGroup(agroup3);
  }

  /**
   * Don't start the entire tree up, just a subtree.
   */
  @Test
  public void testSubSpaceStartup() {
    Space subspace;
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(
            space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            subspace =
                space(3)
                    .addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.startupSpace(subspace);

    Mockito.verify(activeControllerManager, Mockito.never()).startupActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.never()).startupActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1)).startupActiveActivityGroup(agroup3);
  }

  /**
   * Make sure all elements of the space tree shutdown.
   */
  @Test
  public void testEntireSpaceShutdown() {
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            space(3).addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.shutdownSpace(spaceTree);

    Mockito.verify(activeControllerManager, Mockito.times(1)).shutdownActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.times(1)).shutdownActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1)).shutdownActiveActivityGroup(agroup3);
  }

  /**
   * Don't shutdown the entire tree, just a subtree. The entire tree will have
   * been started.
   */
  @Test
  public void testSubSpaceShutdown() {
    Space subspace;
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(
            space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            subspace =
                space(3)
                    .addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.shutdownSpace(subspace);

    // These apps were not in the subtree shut down.
    Mockito.verify(activeControllerManager, Mockito.never()).shutdownActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.never()).shutdownActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1)).shutdownActiveActivityGroup(agroup3);
  }

  /**
   * Make sure all elements of the space tree activate.
   */
  @Test
  public void activateEntireSpace() {
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            space(3).addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.activateSpace(spaceTree);

    Mockito.verify(activeControllerManager, Mockito.times(1)).activateActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.times(1)).activateActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1)).activateActiveActivityGroup(agroup3);
  }

  /**
   * Don't activate the entire tree up, just a subtree.
   */
  @Test
  public void activateSubSpace() {
    Space subspace;
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(
            space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            subspace =
                space(3)
                    .addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.activateSpace(subspace);

    Mockito.verify(activeControllerManager, Mockito.never()).activateActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.never()).activateActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1)).activateActiveActivityGroup(agroup3);
  }

  /**
   * Make sure all elements of the space tree deactivate.
   */
  @Test
  public void testEntireSpaceDeactivate() {
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            space(3).addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.deactivateSpace(spaceTree);

    Mockito.verify(activeControllerManager, Mockito.times(1))
        .deactivateActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.times(1))
        .deactivateActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1))
        .deactivateActiveActivityGroup(agroup3);
  }

  /**
   * Don't deactivate the entire tree, just a subtree. The entire tree will have
   * been started.
   */
  @Test
  public void testSubSpaceDeactivation() {
    Space subspace;
    LiveActivityGroup group1, group2, group3;

    Space spaceTree =
        space(0).addSpaces(
            space(1, group1 = liveActivityGroup(13)).addSpaces(space(4), space(5)),
            space(2).addSpaces(space(6, group2 = liveActivityGroup(10, 11))),
            subspace =
                space(3)
                    .addSpaces(space(7), space(8, group3 = liveActivityGroup(12, 10)), space(9)));

    ActiveLiveActivityGroup agroup1 = new ActiveLiveActivityGroup(group1);
    when(activeControllerManager.getActiveLiveActivityGroup(group1)).thenReturn(agroup1);
    ActiveLiveActivityGroup agroup2 = new ActiveLiveActivityGroup(group2);
    when(activeControllerManager.getActiveLiveActivityGroup(group2)).thenReturn(agroup2);
    ActiveLiveActivityGroup agroup3 = new ActiveLiveActivityGroup(group3);
    when(activeControllerManager.getActiveLiveActivityGroup(group3)).thenReturn(agroup3);

    activeSpaceManager.deactivateSpace(subspace);

    // These apps were not in the subtree deactivated.
    Mockito.verify(activeControllerManager, Mockito.never()).deactivateActiveActivityGroup(agroup1);
    Mockito.verify(activeControllerManager, Mockito.never()).deactivateActiveActivityGroup(agroup2);
    Mockito.verify(activeControllerManager, Mockito.times(1))
        .deactivateActiveActivityGroup(agroup3);
  }

  public BasicActiveSpaceManager getActiveSpaceManager() {
    return new BasicActiveSpaceManager();
  }
}
