/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.master.api.master.internal;

import interactivespaces.container.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.container.control.message.activity.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.api.messages.MasterApiMessages;
import interactivespaces.master.event.MasterEventManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * test the {@link StandardMasterApiActivityManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiActivityManagerTest {
  private StandardMasterApiActivityManager masterApiActivityManager;
  private ActivityRepository activityRepository;
  private SpaceControllerRepository spaceControllerRepository;
  private MasterEventManager masterEventManager;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private Log log;

  @Before
  public void setup() {

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    log = Mockito.mock(Log.class);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    activityRepository = Mockito.mock(ActivityRepository.class);
    spaceControllerRepository = Mockito.mock(SpaceControllerRepository.class);

    masterEventManager = Mockito.mock(MasterEventManager.class);

    masterApiActivityManager = new StandardMasterApiActivityManager();
    masterApiActivityManager.setActivityRepository(activityRepository);
    masterApiActivityManager.setSpaceControllerRepository(spaceControllerRepository);
    masterApiActivityManager.setMasterEventManager(masterEventManager);
  }

  /**
   * Test starting up and shutting down the manager.
   */
  @Test
  public void testManagerLifecycle() {
    masterApiActivityManager.startup();
    Mockito.verify(masterEventManager).addListener(masterApiActivityManager.getMasterEventListener());

    masterApiActivityManager.shutdown();
    Mockito.verify(masterEventManager).removeListener(masterApiActivityManager.getMasterEventListener());
  }

  /**
   * Test handling a master event for a live activity install.
   */
  @Test
  public void testActivityInstallSuccess() {
    String uuid = "foo";
    LiveActivityDeploymentResponse result =
        new LiveActivityDeploymentResponse("trans", uuid, ActivityDeployStatus.SUCCESS, null, 1111);
    long timestamp = 1234567;

    LiveActivity liveActivity = Mockito.mock(LiveActivity.class);
    Mockito.when(liveActivity.getUuid()).thenReturn(uuid);
    ActiveLiveActivity activeLiveActivity = new ActiveLiveActivity(null, liveActivity, null, null);

    Mockito.when(activityRepository.getLiveActivityByUuid(uuid)).thenReturn(liveActivity);

    masterApiActivityManager.getMasterEventListener().onLiveActivityDeploy(activeLiveActivity, result, timestamp);

    Mockito.verify(liveActivity).setLastDeployDate(new Date(timestamp));
    Mockito.verify(activityRepository).saveLiveActivity(liveActivity);
  }

  /**
   * Test the successful edit of a live activity.
   */
  @Test
  public void testLiveActivityEdit() {
    String liveActivityId = "liveActivity";
    LiveActivity liveActivity = Mockito.mock(LiveActivity.class);
    Mockito.when(activityRepository.getLiveActivityByTypedId(liveActivityId)).thenReturn(liveActivity);

    String activityId = "activity";
    Activity activity = Mockito.mock(Activity.class);
    Mockito.when(activityRepository.getActivityById(activityId)).thenReturn(activity);

    String spaceControllerId = "spaceController";
    SpaceController spaceController = Mockito.mock(SpaceController.class);
    Mockito.when(spaceControllerRepository.getSpaceControllerById(spaceControllerId)).thenReturn(spaceController);

    Map<String, Object> args = new HashMap<>();

    args.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivityId);
    String name = "name";
    args.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, name);
    String description = "description";
    args.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION, description);
    args.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ACTIVITY_ID, activityId);
    args.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_ID, spaceControllerId);

    masterApiActivityManager.editLiveActivity(args);

    Mockito.verify(liveActivity).setName(name);
    Mockito.verify(liveActivity).setDescription(description);
    Mockito.verify(liveActivity).setActivity(activity);
    Mockito.verify(liveActivity).setController(spaceController);
    Mockito.verify(activityRepository).saveLiveActivity(liveActivity);
  }
}
