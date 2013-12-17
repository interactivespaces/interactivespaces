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

import static org.junit.Assert.assertEquals;

import interactivespaces.activity.ActivityState;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

/**
 * Tests for the {@link BasicActiveControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class ActiveControllerManagerTest extends BaseSpaceTest {
  private BasicActiveControllerManager activeControllerManager;

  private RemoteControllerClient remoteControllerClient;

  private RemoteControllerClientListenerHelper remoteControllerListenerHelper;

  private SpaceControllerListener controllerListener;

  private RemoteActivityDeploymentManager remoteActivityDeploymentManager;

  private Log log;

  private InteractiveSpacesEnvironment spaceEnvironment;

  @Before
  public void setup() {
    baseSetup();

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    log = Mockito.mock(Log.class);

    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    remoteActivityDeploymentManager = Mockito.mock(RemoteActivityDeploymentManager.class);

    activeControllerManager = new BasicActiveControllerManager();

    activeControllerManager.setRemoteActivityDeploymentManager(remoteActivityDeploymentManager);

    remoteControllerClient = Mockito.mock(RemoteControllerClient.class);
    activeControllerManager.setRemoteControllerClient(remoteControllerClient);
    activeControllerManager.setSpaceEnvironment(spaceEnvironment);

    controllerListener = Mockito.mock(SpaceControllerListener.class);
    activeControllerManager.addControllerListener(controllerListener);

    remoteControllerListenerHelper = new RemoteControllerClientListenerHelper(log);
    remoteControllerListenerHelper.addListener(activeControllerManager);
  }

  /**
   * Test a live activity going to the successful deploy states.
   */
  @Test
  public void testRemoteDeployStateSuccess() {
    LiveActivity liveActivity = liveActivity(0);
    ActiveLiveActivity activeLiveActivity = activeLiveActivity(liveActivity);

    ActivityState oldState = ActivityState.DEPLOY_ATTEMPT;
    activeLiveActivity.setRuntimeState(oldState, null);
    remoteControllerListenerHelper.signalActivityStateChange(liveActivity.getUuid(),
        ActivityState.READY, null);
    assertEquals(ActivityState.READY, activeLiveActivity.getRuntimeState());
    Mockito.verify(controllerListener, Mockito.times(1)).onLiveActivityStateChange(
        liveActivity.getUuid(), oldState, ActivityState.READY);
  }

  /**
   * Test a live activity going to the failure deploy states.
   */
  @Test
  public void testRemoteDeployStateFailure() {
    LiveActivity liveActivity = liveActivity(0);
    ActiveLiveActivity activeLiveActivity = activeLiveActivity(liveActivity);

    ActivityState startState = ActivityState.DEPLOY_ATTEMPT;
    ActivityState finalState = ActivityState.DEPLOY_FAILURE;
    activeLiveActivity.setRuntimeState(startState, null);
    remoteControllerListenerHelper.signalActivityStateChange(liveActivity.getUuid(), finalState,
        null);
    assertEquals(finalState, activeLiveActivity.getRuntimeState());
    Mockito.verify(controllerListener, Mockito.times(1)).onLiveActivityStateChange(
        liveActivity.getUuid(), startState, finalState);
  }

  /**
   * Test an activity going through the start states.
   */
  // @Test
  // public void testRemoteStartupStates() {
  // LiveActivity activity = activity(0);
  // ActiveLiveActivity activeLiveActivity = activeControllerManager
  // .getActiveActivity(activity);
  //
  // activeLiveActivity.setState(ActivityState.STARTUP_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityStart(activity.getUuid(),
  // RemoteActivityStartStatus.SUCCESS);
  // assertEquals(ActivityState.RUNNING, activeLiveActivity.getState());
  // Mockito.verify(controllerListener, Mockito.times(1)).onActivityStartup(
  // activity);
  //
  // activeLiveActivity.setState(ActivityState.STARTUP_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityStart(activity.getUuid(),
  // RemoteActivityStartStatus.FAILURE);
  // assertEquals(ActivityState.STARTUP_FAILURE,
  // activeLiveActivity.getState());
  // }
  //
  // /**
  // * Test an activity going through the activate states.
  // */
  // @Test
  // public void testRemoteActivateStates() {
  // LiveActivity activity = activity(0);
  // ActiveLiveActivity activeLiveActivity = activeControllerManager
  // .getActiveActivity(activity);
  //
  // activeLiveActivity.setState(ActivityState.ACTIVATE_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityActivate(activity.getUuid(),
  // RemoteActivityActivateStatus.SUCCESS);
  // assertEquals(ActivityState.ACTIVE, activeLiveActivity.getState());
  // Mockito.verify(controllerListener, Mockito.times(1))
  // .onActivityActivate(activity);
  //
  // activeLiveActivity.setState(ActivityState.ACTIVATE_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityActivate(activity.getUuid(),
  // RemoteActivityActivateStatus.FAILURE);
  // assertEquals(ActivityState.ACTIVATE_FAILURE,
  // activeLiveActivity.getState());
  // }
  //
  // /**
  // * Test an activity going through the deactivate states.
  // */
  // @Test
  // public void testRemoteDeactivateStates() {
  // LiveActivity activity = activity(0);
  // ActiveLiveActivity activeLiveActivity = activeControllerManager
  // .getActiveActivity(activity);
  //
  // activeLiveActivity.setState(ActivityState.DEACTIVATE_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityDeactivate(activity.getUuid(),
  // RemoteActivityDeactivateStatus.SUCCESS);
  // assertEquals(ActivityState.RUNNING, activeLiveActivity.getState());
  // Mockito.verify(controllerListener, Mockito.times(1))
  // .onActivityDeactivate(activity);
  //
  // activeLiveActivity.setState(ActivityState.ACTIVATE_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityDeactivate(activity.getUuid(),
  // RemoteActivityDeactivateStatus.FAILURE);
  // assertEquals(ActivityState.DEACTIVATE_FAILURE,
  // activeLiveActivity.getState());
  // }
  //
  // /**
  // * Test an activity going through the shutdown states.
  // */
  // @Test
  // public void testRemoteShutdownStates() {
  // LiveActivity activity = activity(0);
  // ActiveLiveActivity activeLiveActivity = activeControllerManager
  // .getActiveActivity(activity);
  //
  // activeLiveActivity.setState(ActivityState.SHUTDOWN_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityShutdown(activity.getUuid(),
  // RemoteActivityShutdownStatus.SUCCESS);
  // assertEquals(ActivityState.READY, activeLiveActivity.getState());
  // Mockito.verify(controllerListener, Mockito.times(1))
  // .onActivityShutdown(activity);
  //
  // activeLiveActivity.setState(ActivityState.SHUTDOWN_ATTEMPT);
  // remoteControllerListenerHelper.sendOnActivityShutdown(activity.getUuid(),
  // RemoteActivityShutdownStatus.FAILURE);
  // assertEquals(ActivityState.SHUTDOWN_FAILURE,
  // activeLiveActivity.getState());
  // }

  /**
   * Make sure an activity deploys.
   */
  @Test
  public void testActivityDeployment() {
    LiveActivity activity = liveActivity(10);

    activeControllerManager.deployLiveActivity(activity);

    ActiveLiveActivity activeLiveActivity = activeControllerManager.getActiveLiveActivity(activity);
    assertActiveActivityState(activeLiveActivity, false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeLiveActivity);
  }

  /**
   * Make sure an activity deletes.
   */
  @Test
  public void testActivityDeletion() {
    LiveActivity activity = liveActivity(10);

    activeControllerManager.deleteLiveActivity(activity);

    ActiveLiveActivity activeLiveActivity = activeControllerManager.getActiveLiveActivity(activity);
    assertActiveActivityState(activeLiveActivity, false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DELETE_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deleteLiveActivity(
        activeLiveActivity);
  }

  /**
   * Make sure an activity starts up.
   */
  @Test
  public void testActivityStartup() {
    LiveActivity activity = liveActivity(10);

    activeControllerManager.startupLiveActivity(activity);

    ActiveLiveActivity activeLiveActivity = activeControllerManager.getActiveLiveActivity(activity);
    assertActiveActivityState(activeLiveActivity, true, 0, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivity);

  }

  /**
   * Make sure activity shuts down.
   */
  @Test
  public void testActivityShutdown() {
    LiveActivity activity = liveActivity(10);

    activeControllerManager.startupLiveActivity(activity);
    activeControllerManager.shutdownLiveActivity(activity);

    ActiveLiveActivity activeLiveActivity = activeControllerManager.getActiveLiveActivity(activity);
    assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivity);
  }

  /**
   * Make sure all activity activates.
   */
  @Test
  public void testActivityActivate() {
    LiveActivity activity = liveActivity(10);

    activeControllerManager.startupLiveActivity(activity);
    activeControllerManager.activateLiveActivity(activity);

    ActiveLiveActivity activeLiveActivity = activeControllerManager.getActiveLiveActivity(activity);
    assertActiveActivityState(activeLiveActivity, true, 0, true, 0, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivity);
  }

  /**
   * Make sure an activity deactivates.
   */
  @Test
  public void testActivityDeactivate() {
    LiveActivity activity = liveActivity(10);

    activeControllerManager.startupLiveActivity(activity);
    activeControllerManager.activateLiveActivity(activity);
    activeControllerManager.deactivateLiveActivity(activity);

    ActiveLiveActivity activeLiveActivity = activeControllerManager.getActiveLiveActivity(activity);
    assertActiveActivityState(activeLiveActivity, true, 0, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivity);
  }

  /**
   * Make sure all activities in a group deploy.
   */
  @Test
  public void testActivityGroupDeployment() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    activeControllerManager.deployLiveActivityGroup(group1);

    assertActiveActivityState(activeActivities.get(0), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeActivities.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeActivities.get(0));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeActivities.get(1));
  }

  /**
   * Make sure all activities in a group deploy even if one fails.
   */
  @Test
  public void testActivityGroupDeploymentWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteActivityDeploymentManager)
        .deployLiveActivity(activeActivities.get(0));

    activeControllerManager.deployLiveActivityGroup(group1);

    assertActiveActivityState(activeActivities.get(0), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeActivities.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeActivities.get(0));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group start up.
   */
  @Test
  public void testActivityGroupStartup() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);

    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    activeControllerManager.startupLiveActivityGroup(group1);

    assertActiveActivityState(activeActivities.get(0), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeActivities.get(1), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group start up even if one fails.
   */
  @Test
  public void testActivityGroupStartupWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);

    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient)
        .startupActivity(activeActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);

    assertActiveActivityState(activeActivities.get(0), false, 0, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeActivities.get(1), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group shutdown.
   */
  @Test
  public void testActivityGroupShutdown() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.shutdownLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group shutdown with a failure.
   */
  @Test
  public void testActivityGroupShutdownWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient)
        .shutdownActivity(activeLiveActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.shutdownLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group activate.
   */
  @Test
  public void testActivityGroupActivate() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group activate with a failure.
   */
  @Test
  public void testActivityGroupActivateWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient)
        .activateActivity(activeLiveActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 0,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities in the activity group deactivate.
   */
  @Test
  public void testActivityGroupDeactivate() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group1);
    activeControllerManager.deactivateLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities in the activity group deactivate with a failure.
   */
  @Test
  public void testActivityGroupDeactivateWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient)
        .deactivateActivity(activeLiveActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group1);
    activeControllerManager.deactivateLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities in each group deploy, even if duplicated.
   */
  @Test
  public void testMultipleActivityGroupAllDeploymentSeparateCalls() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.deployLiveActivityGroup(group1);
    activeControllerManager.deployLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure deployed twice since two
    // calls.
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(2)).deployLiveActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Make sure all activities in each group deploy only once when call with
   * deploy set.
   */
  @Test
  public void testMultipleActivityGroupAllDeploymentOverlaping() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    Set<ActiveLiveActivity> deployedActivities = Sets.newHashSet();
    activeControllerManager.deployActiveLiveActivityGroupChecked(
        activeControllerManager.getActiveLiveActivityGroup(group1), deployedActivities);
    activeControllerManager.deployActiveLiveActivityGroupChecked(
        activeControllerManager.getActiveLiveActivityGroup(group2), deployedActivities);

    assertEquals(3, deployedActivities.size());

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure deployed once since tracking.
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Make sure all elements of the multiple activity groups start up.
   */
  @Test
  public void testMultipleActivityGroupAllStartup() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only started once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Start up activities in two activity groups. One activity will be in both.
   * Force it to shutdown.
   */
  @Test
  public void testMultipleActivityGroupAllStartupForceShutdown() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    activeControllerManager.shutdownLiveActivity(liveActivity(10));

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities2.get(0));

    // This one should be shutdown
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Make sure all elements of the multiple activity groups shutdown.
   */
  @Test
  public void testMultipleActivityGroupAllShutdown() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    activeControllerManager.shutdownLiveActivityGroup(group1);
    activeControllerManager.shutdownLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0,
        ActivityState.SHUTDOWN_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only stopped once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Multiple activity groups. One activity is in both. Activate one of the
   * groups containing the activity and make sure that unshared activities are
   * activated, but the shared activity is left alone.
   */
  @Test
  public void testMultipleActivityGroupPartialStartup() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0,
        ActivityState.UNKNOWN, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.never()).startupActivity(
        activeLiveActivities1.get(1));

    // This one was in only the started up group.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities2.get(0));

    // This one was in two groups. Make sure was started up.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Make sure all activities of the multiple activity groups activate.
   */
  @Test
  public void testMultipleActivityGroupAllActivate() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    activeControllerManager.activateLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 2,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 2,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only started once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Multiple activity groups are started up. One activity is in both. Activate
   * one of the groups containing the activity and make sure that unshared
   * activities are activated, but the shared activity is left alone.
   */
  @Test
  public void testMultipleActivityGroupPartialActivate() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    activeControllerManager.activateLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0,
        ActivityState.STARTUP_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.never()).activateActivity(
        activeLiveActivities1.get(1));

    // This one was in only the activated group.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities2.get(0));

    // This one was in two groups. Make sure was activated.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Make sure all activities in the multiple activity groups deactivate.
   */
  @Test
  public void testMultipleActivityGroupAllDeactivate() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    activeControllerManager.activateLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group2);

    activeControllerManager.deactivateLiveActivityGroup(group1);
    activeControllerManager.deactivateLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only stopped once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Multiple activity groups are deactivated. One activity is in both. Stop one
   * of the groups containing the activity and make sure that unshared
   * activities are deactivated, but the shared activity is left alone.
   */
  @Test
  public void testMultipleActivityGroupPartialDeactivate() {
    List<LiveActivity> liveActivities1 = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

    List<LiveActivity> liveActivities2 = liveActivities(12, 10);
    List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
    LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.startupLiveActivityGroup(group2);

    activeControllerManager.activateLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group2);

    activeControllerManager.deactivateLiveActivityGroup(group2);

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0,
        ActivityState.DEACTIVATE_ATTEMPT, ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 1,
        ActivityState.ACTIVATE_ATTEMPT, ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.never()).deactivateActivity(
        activeLiveActivities1.get(1));

    // This one was in only the deactivated group.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeLiveActivities2.get(0));

    // This one was in two groups. Make sure not stopped.
    Mockito.verify(remoteControllerClient, Mockito.never()).deactivateActivity(
        activeLiveActivities1.get(0));
  }

  /**
   * Get all active live activities.
   *
   * @param liveActivities
   *          the live activities
   *
   * @return the active live activities
   */
  public ActiveLiveActivity activeLiveActivity(LiveActivity liveActivity) {
    return activeControllerManager.getActiveLiveActivity(liveActivity);
  }

  /**
   * Get all active live activities.
   *
   * @param liveActivities
   *          the live activities
   *
   * @return the active live activities
   */
  public List<ActiveLiveActivity> activeLiveActivities(LiveActivity... liveActivities) {
    List<ActiveLiveActivity> activeActivities = Lists.newArrayList();

    for (LiveActivity liveActivity : liveActivities) {
      activeControllerManager.getActiveLiveActivity(liveActivity);
    }

    return activeActivities;
  }

  /**
   * Get all active live activities.
   *
   * @param liveActivities
   *          the live activities
   *
   * @return the active live activities
   */
  public List<ActiveLiveActivity> activeLiveActivities(List<LiveActivity> liveActivities) {
    List<ActiveLiveActivity> activeActivities = Lists.newArrayList();

    for (LiveActivity liveActivity : liveActivities) {
      activeActivities.add(activeControllerManager.getActiveLiveActivity(liveActivity));
    }

    return activeActivities;
  }
}
