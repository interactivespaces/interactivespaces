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

import interactivespaces.activity.ActivityState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivityGroup;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteSpaceControllerClient;
import interactivespaces.time.TimeProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * A series of tests for {@link ActiveLiveActivity} instances
 *
 * @author Keith M. Hughes
 */
public class ActiveLiveActivityTest extends BaseSpaceTest {

  private RemoteSpaceControllerClient remoteControllerClient;

  private ActiveSpaceController activeSpaceController;

  private TimeProvider timeProvider;

  private LiveActivity liveActivity;

  private LiveActivityGroup liveActivityGroup1;

  private LiveActivityGroup liveActivityGroup2;

  private ActiveLiveActivity activeLiveActivity;

  private ActiveLiveActivityGroup activeLiveActivityGroup1;

  private ActiveLiveActivityGroup activeLiveActivityGroup2;

  @Before
  public void setup() {
    baseSetup();

    remoteControllerClient = Mockito.mock(RemoteSpaceControllerClient.class);

    activeSpaceController = Mockito.mock(ActiveSpaceController.class);

    timeProvider = Mockito.mock(TimeProvider.class);

    liveActivity = liveActivity(12);

    // Need two groups with the same activity to test overlaps.
    liveActivityGroup1 = liveActivityGroup(12);
    activeLiveActivityGroup1 = new ActiveLiveActivityGroup(liveActivityGroup1);
    liveActivityGroup2 = liveActivityGroup(12);
    activeLiveActivityGroup2 = new ActiveLiveActivityGroup(liveActivityGroup2);

    activeLiveActivity =
        new ActiveLiveActivity(activeSpaceController, liveActivity, remoteControllerClient,
            timeProvider);
  }

  /**
   * Startup the live activity
   */
  @Test
  public void directActivityStartup() {
    activeLiveActivity.startup();

    assertActiveActivityState(activeLiveActivity, true, 0, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivity);
  }

  /**
   * Startup the live activity, then shut it down.
   */
  @Test
  public void directActivityStartupAndShutdown() {
    activeLiveActivity.startup();
    activeLiveActivity.shutdown();

    assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivity);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivity);
  }

  /**
   * directly activate the live activity with no startup.
   */
  @Test
  public void directActivityActivate() {
    activeLiveActivity.activate();

    assertActiveActivityState(activeLiveActivity, true, 0, true, 0, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivity);
  }

  /**
   * activate the live activity, then shut it down.
   */
  @Test
  public void directActivityActivateAndShutdown() {
    activeLiveActivity.activate();
    activeLiveActivity.shutdown();

    assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivity);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivity);
  }

  /**
   * Startup the live activity from a group.
   */
  @Test
  public void groupActivityStartup() {
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup1);

    assertActiveActivityState(activeLiveActivity, false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivity);
  }

  /**
   * Startup the live activity from two groups.
   */
  @Test
  public void groupTwoActivityStartup() {
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup2);

    assertActiveActivityState(activeLiveActivity, false, 2, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivity);
  }

  /**
   * Startup the live activity from two groups and shutdown from 1.
   */
  @Test
  public void groupTwoActivityStartupAndOneShutdown() {
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup2);
    activeLiveActivity.shutdownFromLiveActivityGroup(activeLiveActivityGroup1);

    assertActiveActivityState(activeLiveActivity, false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(0)).shutdownActivity(activeLiveActivity);
  }

  /**
   * Startup the live activity from two groups and shutdown from both.
   */
  @Test
  public void groupTwoActivityStartupAndTwoShutdown() {
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.startupFromLiveActivityGroup(activeLiveActivityGroup2);
    activeLiveActivity.shutdownFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.shutdownFromLiveActivityGroup(activeLiveActivityGroup2);

    assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivity);
  }

  /**
   * Activate the live activity from a group without a startup.
   */
  @Test
  public void groupActivityActivate() {
    activeLiveActivity.activateFromLiveActivityGroup(activeLiveActivityGroup1);

    assertActiveActivityState(activeLiveActivity, false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(0)).startupActivity(activeLiveActivity);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivity);
  }

  /**
   * Activate the live activity from two groups without a startup, then
   * deactivate from 1.
   */
  @Test
  public void groupTwoActivityActivateAndOneDeactivate() {
    activeLiveActivity.activateFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.activateFromLiveActivityGroup(activeLiveActivityGroup2);
    activeLiveActivity.deactivateFromLiveActivityGroup(activeLiveActivityGroup1);

    assertActiveActivityState(activeLiveActivity, false, 2, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(0)).deactivateActivity(activeLiveActivity);
  }

  /**
   * Activate the live activity from two groups without a startup, then
   * deactivate from both.
   */
  @Test
  public void groupTwoActivityActivateAndTwoDeactivate() {
    activeLiveActivity.activateFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.activateFromLiveActivityGroup(activeLiveActivityGroup2);
    activeLiveActivity.deactivateFromLiveActivityGroup(activeLiveActivityGroup1);
    activeLiveActivity.deactivateFromLiveActivityGroup(activeLiveActivityGroup2);

    assertActiveActivityState(activeLiveActivity, false, 2, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivity);
  }

  /**
   * Activate the live activity from a group, then shut it down from a group.
   */
  @Test
  public void groupActivityActivateAndShutdown() {
    activeLiveActivity.activateFromLiveActivityGroup(activeLiveActivityGroup1);

    assertActiveActivityState(activeLiveActivity, false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    activeLiveActivity.shutdownFromLiveActivityGroup(activeLiveActivityGroup1);

    assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
  }

}
