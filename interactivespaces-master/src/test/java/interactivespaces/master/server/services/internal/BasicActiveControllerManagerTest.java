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
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

/**
 * Tests for the {@link BasicActiveControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicActiveControllerManagerTest {

  private BasicActiveControllerManager controllerManager;
  private SpaceControllerListener listener;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private TimeProvider timeProvider;
  private RemoteControllerClient remoteControllerClient;
  private final long timestamp = 4321;
  private Log log;

  @Before
  public void setup() {

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    timeProvider = Mockito.mock(TimeProvider.class);
    Mockito.when(timeProvider.getCurrentTime()).thenReturn(timestamp);

    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    log = Mockito.mock(Log.class);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    listener = Mockito.mock(SpaceControllerListener.class);

    remoteControllerClient = Mockito.mock(RemoteControllerClient.class);

    controllerManager = new BasicActiveControllerManager();
    controllerManager.setSpaceEnvironment(spaceEnvironment);
    controllerManager.setRemoteControllerClient(remoteControllerClient);

    controllerManager.addControllerListener(listener);
  }

  /**
   * Test handling the onActivityInstall message handler during success.
   */
  @Test
  public void testActivityInstallSuccess() {
    String activityUuid = "activity";
    LiveActivityDeploymentResponse result =
        new LiveActivityDeploymentResponse(null, activityUuid, ActivityDeployStatus.STATUS_SUCCESS, timestamp);

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    controllerManager.getActiveSpaceController(controller);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setUuid(activityUuid);
    activity.setController(controller);
    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    controllerManager.onLiveActivityDeployment(activityUuid, result);

    Mockito.verify(listener).onLiveActivityInstall(activityUuid, result, timestamp);
    assertEquals(null, active.getRuntimeState());
    assertEquals(ActivityState.READY, active.getDeployState());
  }

  /**
   * Test handling the onActivityInstall message handler during failure.
   */
  @Test
  public void testActivityInstallFailure() {
    String activityUuid = "activity";
    LiveActivityDeploymentResponse result =
        new LiveActivityDeploymentResponse(null, activityUuid, ActivityDeployStatus.STATUS_FAILURE_COPY, timestamp);

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    controllerManager.getActiveSpaceController(controller);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setUuid(activityUuid);
    activity.setController(controller);
    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    controllerManager.onLiveActivityDeployment(activityUuid, result);

    Mockito.verify(listener).onLiveActivityInstall(activityUuid, result, timestamp);
    assertEquals(null, active.getRuntimeState());
    assertEquals(ActivityState.DEPLOY_FAILURE, active.getDeployState());
  }

  /**
   * Test handling the onActivityDelete message handler during success.
   */
  @Test
  public void testActivityDeleteSucccess() {
    String activityUuid = "activity";
    LiveActivityDeleteResult result = LiveActivityDeleteResult.SUCCESS;

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    controllerManager.getActiveSpaceController(controller);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setUuid(activityUuid);
    activity.setController(controller);
    Date lastDeployDate = new Date();
    activity.setLastDeployDate(lastDeployDate);
    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    controllerManager.onLiveActivityDelete(activityUuid, result);

    Mockito.verify(listener).onLiveActivityDelete(activityUuid, result, timestamp);
    assertEquals(ActivityState.UNKNOWN, active.getRuntimeState());
    assertEquals(ActivityState.UNKNOWN, active.getDeployState());
    assertEquals(null, activity.getLastDeployDate());
  }

  /**
   * Test handling the onActivityDelete message handler during success.
   */
  @Test
  public void testActivityDeleteFailure() {
    String activityUuid = "activity";
    LiveActivityDeleteResult result = LiveActivityDeleteResult.FAIL;

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    controllerManager.getActiveSpaceController(controller);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setUuid(activityUuid);
    activity.setController(controller);
    Date lastDeployDate = new Date();
    activity.setLastDeployDate(lastDeployDate);
    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    controllerManager.onLiveActivityDelete(activityUuid, result);

    Mockito.verify(listener).onLiveActivityDelete(activityUuid, result, timestamp);
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
    LiveActivityDeleteResult result = LiveActivityDeleteResult.DOESNT_EXIST;

    String spaceUuid = "space";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(spaceUuid);
    controllerManager.getActiveSpaceController(controller);

    LiveActivity activity = new SimpleLiveActivity();
    activity.setUuid(activityUuid);
    activity.setController(controller);
    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);
    active.setDeployState(null);
    active.setRuntimeState(null, null);

    controllerManager.onLiveActivityDelete(activityUuid, result);

    Mockito.verify(listener).onLiveActivityDelete(activityUuid, result, timestamp);
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

    controllerManager.cleanControllerPermanentData(controller);

    ActiveSpaceController acontroller = controllerManager.getActiveControllerByUuid(controllerUuid);
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

    controllerManager.cleanControllerActivitiesPermanentData(controller);

    ActiveSpaceController acontroller = controllerManager.getActiveControllerByUuid(controllerUuid);
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

    controllerManager.cleanControllerTempData(controller);

    ActiveSpaceController acontroller = controllerManager.getActiveControllerByUuid(controllerUuid);
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

    controllerManager.cleanControllerActivitiesTempData(controller);

    ActiveSpaceController acontroller = controllerManager.getActiveControllerByUuid(controllerUuid);
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

    controllerManager.cleanLiveActivityPermanentData(activity);

    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);

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

    controllerManager.cleanLiveActivityTempData(activity);

    ActiveLiveActivity active = controllerManager.getActiveLiveActivity(activity);

    Mockito.verify(remoteControllerClient).cleanActivityTempData(active);
  }
}
