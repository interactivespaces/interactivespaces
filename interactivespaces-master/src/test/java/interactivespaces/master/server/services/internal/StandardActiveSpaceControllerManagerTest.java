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

package interactivespaces.master.server.services.internal;

import static org.junit.Assert.assertEquals;

import interactivespaces.activity.ActivityState;
import interactivespaces.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.control.message.activity.LiveActivityDeleteResponse.LiveActivityDeleteStatus;
import interactivespaces.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.control.message.activity.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.master.event.MasterEventManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteSpaceControllerClient;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Tests for the {@link StandardActiveSpaceControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardActiveSpaceControllerManagerTest extends BaseSpaceTest {

  private StandardActiveSpaceControllerManager activeControllerManager;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private TimeProvider timeProvider;
  private RemoteSpaceControllerClient remoteControllerClient;
  private MasterEventManager masterEventManager;
  private RemoteActivityDeploymentManager remoteActivityDeploymentManager;

  private final long timestamp = 4321;
  private Log log;

  @Before
  public void setup() {
    baseSetup();

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    timeProvider = Mockito.mock(TimeProvider.class);
    Mockito.when(timeProvider.getCurrentTime()).thenReturn(timestamp);

    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    log = Mockito.mock(Log.class);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    remoteControllerClient = Mockito.mock(RemoteSpaceControllerClient.class);

    remoteActivityDeploymentManager = Mockito.mock(RemoteActivityDeploymentManager.class);

    masterEventManager = Mockito.mock(MasterEventManager.class);

    activeControllerManager = new StandardActiveSpaceControllerManager();
    activeControllerManager.setSpaceEnvironment(spaceEnvironment);
    activeControllerManager.setRemoteSpaceControllerClient(remoteControllerClient);
    activeControllerManager.setRemoteActivityDeploymentManager(remoteActivityDeploymentManager);
    activeControllerManager.setMasterEventManager(masterEventManager);
  }

  /**
   * Test handling the onActivityDeploy message handler during success.
   */
  @Test
  public void testActivityDeploySuccess() {
    String activityUuid = "activity";
    LiveActivityDeploymentResponse result =
        new LiveActivityDeploymentResponse(null, activityUuid, ActivityDeployStatus.STATUS_SUCCESS, null, timestamp);

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    activeControllerManager.getActiveSpaceController(controller);

    Activity activity = new SimpleActivity();
    activity.setIdentifyingName("test");
    activity.setVersion("1.2.3");

    LiveActivity liveActivity = new SimpleLiveActivity();
    liveActivity.setUuid(activityUuid);
    liveActivity.setController(controller);
    liveActivity.setActivity(activity);

    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(liveActivity);
    active.setDeployState(ActivityState.DEPLOY_ATTEMPT);
    active.setRuntimeState(null, null);

    activeControllerManager.onLiveActivityDeployment(activityUuid, result);

    Mockito.verify(masterEventManager).signalLiveActivityDeploy(active, result, timestamp);
    assertEquals(null, active.getRuntimeState());
    assertEquals(ActivityState.READY, active.getDeployState());
  }

  /**
   * Test handling the onActivityDeploy message handler during failure.
   */
  @Test
  public void testActivityDeployFailure() {
    String activityUuid = "activity";
    LiveActivityDeploymentResponse result =
        new LiveActivityDeploymentResponse(null, activityUuid, ActivityDeployStatus.STATUS_FAILURE_COPY, "bad",
            timestamp);

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    activeControllerManager.getActiveSpaceController(controller);

    Activity activity = new SimpleActivity();
    activity.setIdentifyingName("test");
    activity.setVersion("1.2.3");

    LiveActivity liveActivity = new SimpleLiveActivity();
    liveActivity.setUuid(activityUuid);
    liveActivity.setController(controller);
    liveActivity.setActivity(activity);

    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(liveActivity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    activeControllerManager.onLiveActivityDeployment(activityUuid, result);

    Mockito.verify(masterEventManager).signalLiveActivityDeploy(active, result, timestamp);
    assertEquals(null, active.getRuntimeState());
    assertEquals(ActivityState.DEPLOY_FAILURE, active.getDeployState());
  }

  /**
   * Test handling the onActivityDelete message handler during success.
   */
  @Test
  public void testActivityDeleteSucccess() {
    String activityUuid = "activity";
    LiveActivityDeleteStatus deleteStatus = LiveActivityDeleteStatus.SUCCESS;

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    activeControllerManager.getActiveSpaceController(controller);

    Activity activity = new SimpleActivity();
    activity.setIdentifyingName("test");
    activity.setVersion("1.2.3");

    LiveActivity liveActivity = new SimpleLiveActivity();
    liveActivity.setUuid(activityUuid);
    liveActivity.setController(controller);
    liveActivity.setActivity(activity);

    Date lastDeployDate = new Date();
    liveActivity.setLastDeployDate(lastDeployDate);
    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(liveActivity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    LiveActivityDeleteResponse deleteResponse =
        new LiveActivityDeleteResponse(activityUuid, deleteStatus, 10000, null);

    activeControllerManager.onLiveActivityDelete(activityUuid, deleteResponse);

    Mockito.verify(masterEventManager).signalLiveActivityDelete(active, deleteResponse);
    assertEquals(ActivityState.UNKNOWN, active.getRuntimeState());
    assertEquals(ActivityState.UNKNOWN, active.getDeployState());
    assertEquals(null, liveActivity.getLastDeployDate());
  }

  /**
   * Test handling the onActivityDelete message handler during success.
   */
  @Test
  public void testActivityDeleteFailure() {
    String activityUuid = "activity";
    LiveActivityDeleteStatus deleteStatus = LiveActivityDeleteStatus.FAILURE;

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    activeControllerManager.getActiveSpaceController(controller);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setUuid(activityUuid);
    activity.setController(controller);
    Date lastDeployDate = new Date();
    activity.setLastDeployDate(lastDeployDate);
    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(activity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    LiveActivityDeleteResponse deleteResponse =
        new LiveActivityDeleteResponse(activityUuid, deleteStatus, 10000, null);

    activeControllerManager.onLiveActivityDelete(activityUuid, deleteResponse);

    Mockito.verify(masterEventManager).signalLiveActivityDelete(active, deleteResponse);
    assertEquals(null, active.getRuntimeState());
    assertEquals(null, active.getDeployState());
    assertEquals(lastDeployDate, activity.getLastDeployDate());

  }

  /**
   * Test handling the onActivityDelete message handler during not exist.
   */
  @Test
  public void testActivityDeleteNotExist() {
    String activityUuid = "activity";
    LiveActivityDeleteStatus deleteStatus = LiveActivityDeleteStatus.DOESNT_EXIST;

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    activeControllerManager.getActiveSpaceController(controller);

    Activity activity = new SimpleActivity();
    activity.setIdentifyingName("test");
    activity.setVersion("1.2.3");

    LiveActivity liveActivity = new SimpleLiveActivity();
    liveActivity.setUuid(activityUuid);
    liveActivity.setController(controller);
    liveActivity.setActivity(activity);

    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(liveActivity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    LiveActivityDeleteResponse deleteResponse =
        new LiveActivityDeleteResponse(activityUuid, deleteStatus, 10000, null);

    activeControllerManager.onLiveActivityDelete(activityUuid, deleteResponse);

    Mockito.verify(masterEventManager).signalLiveActivityDelete(active, deleteResponse);
    assertEquals(ActivityState.DOESNT_EXIST, active.getRuntimeState());
    assertEquals(ActivityState.DOESNT_EXIST, active.getDeployState());
  }

  /**
   * Test cleaning a controller's permanent data.
   */
  @Test
  public void testCleanControllerPermanantData() {
    String controllerUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(controllerUuid);

    activeControllerManager.cleanSpaceControllerPermanentData(controller);

    ActiveSpaceController acontroller = activeControllerManager.getActiveControllerByUuid(controllerUuid);
    Mockito.verify(remoteControllerClient).cleanControllerPermanentData(acontroller);
  }

  /**
   * Test cleaning all permanent data for a controller and all live activities.
   */
  @Test
  public void testCleanControllerPermanantDataAll() {
    String controllerUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(controllerUuid);

    activeControllerManager.cleanSpaceControllerActivitiesPermanentData(controller);

    ActiveSpaceController acontroller = activeControllerManager.getActiveControllerByUuid(controllerUuid);
    Mockito.verify(remoteControllerClient).cleanControllerActivitiesPermanentData(acontroller);
  }

  /**
   * Test cleaning a controller's temp data.
   */
  @Test
  public void testCleanControllerTempData() {
    String controllerUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(controllerUuid);

    activeControllerManager.cleanSpaceControllerTempData(controller);

    ActiveSpaceController acontroller = activeControllerManager.getActiveControllerByUuid(controllerUuid);
    Mockito.verify(remoteControllerClient).cleanControllerTempData(acontroller);
  }

  /**
   * Test cleaning temp data for a controller and all live activities.
   */
  @Test
  public void testCleanControllerTempDataAll() {
    String controllerUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(controllerUuid);

    activeControllerManager.cleanSpaceControllerActivitiesTempData(controller);

    ActiveSpaceController acontroller = activeControllerManager.getActiveControllerByUuid(controllerUuid);
    Mockito.verify(remoteControllerClient).cleanControllerActivitiesTempData(acontroller);
  }

  /**
   * Test cleaning the permanent data for an activity.
   */
  @Test
  public void testActivityPermanentClean() {
    String controllerUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(controllerUuid);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setController(controller);

    activeControllerManager.cleanLiveActivityPermanentData(activity);

    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(activity);

    Mockito.verify(remoteControllerClient).cleanActivityPermanentData(active);
  }

  /**
   * Test cleaning the temp data for an activity.
   */
  @Test
  public void testActivityTempClean() {
    String controllerUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(controllerUuid);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setController(controller);

    activeControllerManager.cleanLiveActivityTempData(activity);

    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(activity);

    Mockito.verify(remoteControllerClient).cleanActivityTempData(active);
  }

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

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeLiveActivity);
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

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deleteLiveActivity(activeLiveActivity);
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
    assertActiveActivityState(activeLiveActivity, false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);

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
    assertActiveActivityState(activeLiveActivity, true, 0, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

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

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeActivities.get(0));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeActivities.get(1));
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
        ActivityState.DEPLOY_FAILURE);
    assertActiveActivityState(activeActivities.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeActivities.get(0));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeActivities.get(1));
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

    assertActiveActivityState(activeActivities.get(0), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeActivities.get(1), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group start up even if one fails.
   */
  @Test
  public void testActivityGroupStartupWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);

    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient).startupActivity(activeActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);

    assertActiveActivityState(activeActivities.get(0), false, 0, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeActivities.get(1), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeActivities.get(1));
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

    assertActiveActivityState(activeLiveActivities.get(0), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group shutdown with a failure.
   */
  @Test
  public void testActivityGroupShutdownWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient).shutdownActivity(activeLiveActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.shutdownLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities.get(1));
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

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities.get(1));
  }

  /**
   * Make sure all activities of the activity group activate with a failure.
   */
  @Test
  public void testActivityGroupActivateWithFail() {
    List<LiveActivity> liveActivities = liveActivities(10, 11);
    List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
    LiveActivityGroup group1 = liveActivityGroup(liveActivities);

    Mockito.doThrow(new RuntimeException()).when(remoteControllerClient).activateActivity(activeLiveActivities.get(0));

    activeControllerManager.startupLiveActivityGroup(group1);
    activeControllerManager.activateLiveActivityGroup(group1);

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 0, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities.get(1));
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

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities.get(1));
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

    assertActiveActivityState(activeLiveActivities.get(0), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities.get(1), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities.get(0));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities.get(1));
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure deployed twice since two
    // calls.
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(2)).deployLiveActivity(activeLiveActivities1.get(0));
  }

  /**
   * Make sure all activities in each group deploy only once when call with deploy set.
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.DEPLOY_ATTEMPT);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure deployed once since tracking.
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(activeLiveActivities1.get(0));
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only started once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities1.get(0));
  }

  /**
   * Start up activities in two activity groups. One activity will be in both. Force it to shutdown.
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities2.get(0));

    // This one should be shutdown
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities1.get(0));
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 0, false, 0, ActivityState.SHUTDOWN_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only stopped once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(activeLiveActivities1.get(0));
  }

  /**
   * Multiple activity groups. One activity is in both. Activate one of the groups containing the activity and make sure
   * that unshared activities are activated, but the shared activity is left alone.
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 0, false, 0, ActivityState.UNKNOWN,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.never()).startupActivity(activeLiveActivities1.get(1));

    // This one was in only the started up group.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities2.get(0));

    // This one was in two groups. Make sure was started up.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(activeLiveActivities1.get(0));
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 2, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 2, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only started once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities1.get(0));
  }

  /**
   * Multiple activity groups are started up. One activity is in both. Activate one of the groups containing the
   * activity and make sure that unshared activities are activated, but the shared activity is left alone.
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0, ActivityState.STARTUP_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.never()).activateActivity(activeLiveActivities1.get(1));

    // This one was in only the activated group.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities2.get(0));

    // This one was in two groups. Make sure was activated.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(activeLiveActivities1.get(0));
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities1.get(1));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities2.get(0));

    // This one was in two subtrees. Make sure only stopped once.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities1.get(0));
  }

  /**
   * Multiple activity groups are deactivated. One activity is in both. Stop one of the groups containing the activity
   * and make sure that unshared activities are deactivated, but the shared activity is left alone.
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

    assertActiveActivityState(activeLiveActivities1.get(0), false, 2, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities1.get(1), false, 1, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    assertActiveActivityState(activeLiveActivities2.get(0), false, 1, false, 0, ActivityState.DEACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);
    assertActiveActivityState(activeLiveActivities2.get(1), false, 2, false, 1, ActivityState.ACTIVATE_ATTEMPT,
        ActivityState.UNKNOWN);

    Mockito.verify(remoteControllerClient, Mockito.never()).deactivateActivity(activeLiveActivities1.get(1));

    // This one was in only the deactivated group.
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(activeLiveActivities2.get(0));

    // This one was in two groups. Make sure not stopped.
    Mockito.verify(remoteControllerClient, Mockito.never()).deactivateActivity(activeLiveActivities1.get(0));
  }

  /**
   * Make sure all elements of the space tree deploy.
   */
  @Test
  public void testEntireSpaceDeploy() {
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                space(9)));

    activeControllerManager.deploySpace(spaceTree);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Don't deploy the entire tree, just a subtree.
   */
  @Test
  public void testSubSpaceDeploy() {
    Space subspace;
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            subspace =
                space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                    space(9)));

    activeControllerManager.deploySpace(subspace);

    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.times(1)).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity12));

    Mockito.verify(remoteActivityDeploymentManager, Mockito.never()).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteActivityDeploymentManager, Mockito.never()).deployLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Make sure all elements of the space tree configure.
   */
  @Test
  public void testEntireSpaceConfigure() {
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                space(9)));

    activeControllerManager.configureSpace(spaceTree);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Don't configure the entire tree, just a subtree.
   */
  @Test
  public void testSubSpaceConfigure() {
    Space subspace;
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            subspace =
                space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                    space(9)));

    activeControllerManager.configureSpace(subspace);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.never()).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.never()).fullConfigureLiveActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Make sure all elements of the space tree start up.
   */
  @Test
  public void testEntireSpaceStartup() {
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                space(9)));

    activeControllerManager.startupSpace(spaceTree);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Don't start the entire tree up, just a subtree.
   */
  @Test
  public void testSubSpaceStartup() {
    Space subspace;
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            subspace =
                space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                    space(9)));

    activeControllerManager.startupSpace(subspace);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.never()).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.never()).startupActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Make sure all elements of the space tree shutdown.
   */
  @Test
  public void testEntireSpaceShutdown() {
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                space(9)));

    // Since space operations act on groups, we need to start before we can shut down
    activeControllerManager.startupSpace(spaceTree);

    activeControllerManager.shutdownSpace(spaceTree);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Don't shutdown the entire tree, just a subtree. The entire tree will have been started.
   */
  @Test
  public void testSubSpaceShutdown() {
    Space subspace;
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            subspace =
                space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                    space(9)));

    // Since space operations act on groups, we need to start before we can shut down
    activeControllerManager.startupSpace(subspace);

    activeControllerManager.shutdownSpace(subspace);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.never()).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.never()).shutdownActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Make sure all elements of the space tree activate.
   */
  @Test
  public void activateEntireSpace() {
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                space(9)));

    activeControllerManager.activateSpace(spaceTree);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Don't activate the entire tree up, just a subtree.
   */
  @Test
  public void activateSubSpace() {
    Space subspace;
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            subspace =
                space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                    space(9)));

    activeControllerManager.activateSpace(subspace);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.never()).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.never()).activateActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Make sure all elements of the space tree deactivate.
   */
  @Test
  public void testEntireSpaceDeactivate() {
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                space(9)));

    // Since space operations act on groups, we need to activate before we can deactivate
    activeControllerManager.activateSpace(spaceTree);

    activeControllerManager.deactivateSpace(spaceTree);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
  }

  /**
   * Don't deactivate the entire tree, just a subtree. The entire tree will have been started.
   */
  @Test
  public void testSubSpaceDeactivation() {
    Space subspace;
    LiveActivity activity10, activity11, activity12, activity13;

    Space spaceTree =
        space(0).addSpaces(
            space(1, liveActivityGroup(activity13 = liveActivity(13))).addSpaces(space(4), space(5)),
            space(2).addSpaces(
                space(6, liveActivityGroup(activity10 = liveActivity(10), activity11 = liveActivity(11)))),
            subspace =
                space(3).addSpaces(space(7), space(8, liveActivityGroup(activity12 = liveActivity(12), activity10)),
                    space(9)));

    // Since space operations act on groups, we need to activate before we can deactivate
    activeControllerManager.activateSpace(subspace);

    activeControllerManager.deactivateSpace(subspace);

    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity10));
    Mockito.verify(remoteControllerClient, Mockito.never()).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity11));
    Mockito.verify(remoteControllerClient, Mockito.times(1)).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity12));
    Mockito.verify(remoteControllerClient, Mockito.never()).deactivateActivity(
        activeControllerManager.getActiveLiveActivity(activity13));
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
