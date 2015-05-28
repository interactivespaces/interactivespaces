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

import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.event.MasterEventManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

/**
 * test the {@link StandardMasterApiActivityManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiActivityManagerTest {
  private StandardMasterApiActivityManager masterApiActivityManager;
  private ActivityRepository activityRepository;
  private MasterEventManager masterEventManager;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private Log log;

  @Before
  public void setup() {

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    log = Mockito.mock(Log.class);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    activityRepository = Mockito.mock(ActivityRepository.class);

    masterEventManager = Mockito.mock(MasterEventManager.class);

    masterApiActivityManager = new StandardMasterApiActivityManager();
    masterApiActivityManager.setActivityRepository(activityRepository);
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
    LiveActivityDeploymentResponse result = new LiveActivityDeploymentResponse("trans", uuid, ActivityDeployStatus.STATUS_SUCCESS, 1111);
    long timestamp = 1234567;

    LiveActivity liveActivity = Mockito.mock(LiveActivity.class);
    Mockito.when(liveActivity.getUuid()).thenReturn(uuid);
    ActiveLiveActivity activeLiveActivity = new ActiveLiveActivity(null, liveActivity, null, null);

    Mockito.when(activityRepository.getLiveActivityByUuid(uuid)).thenReturn(liveActivity);

    masterApiActivityManager.getMasterEventListener().onLiveActivityDeploy(activeLiveActivity, result, timestamp);

    Mockito.verify(liveActivity).setLastDeployDate(new Date(timestamp));
    Mockito.verify(activityRepository).saveLiveActivity(liveActivity);
  }
}
