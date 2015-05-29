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

import interactivespaces.activity.ActivityState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.event.MasterEventManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.service.web.server.MultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link StandardMasterWebsocketManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterWebsocketManagerTest {
  private StandardMasterWebsocketManager masterWebsocketManager;
  private MasterEventManager masterEventManager;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private TimeProvider timeProvider;

  @Before
  public void setup() {

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    timeProvider = Mockito.mock(TimeProvider.class);
    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    masterEventManager = Mockito.mock(MasterEventManager.class);

    masterWebsocketManager = new StandardMasterWebsocketManager();
    masterWebsocketManager.setSpaceEnvironment(spaceEnvironment);
    masterWebsocketManager.setMasterEventManager(masterEventManager);
  }

  /**
   * Test the manager getting an activity state change from the master event bus.
   */
  @Test
  public void testMasterEventActivityStateChange() {
    MultipleConnectionWebServerWebSocketHandlerFactory websocketHandlerFactory =
        Mockito.mock(MultipleConnectionWebServerWebSocketHandlerFactory.class);
    masterWebsocketManager.setWebSocketHandlerFactory(websocketHandlerFactory);

    String uuid = "foo";

    LiveActivity liveActivity = Mockito.mock(LiveActivity.class);
    Mockito.when(liveActivity.getUuid()).thenReturn(uuid);
    ActiveLiveActivity activeLiveActivity = new ActiveLiveActivity(null, liveActivity, null, null);

    masterWebsocketManager.getMasterEventListener().onLiveActivityStateChange(activeLiveActivity,
        ActivityState.STARTUP_ATTEMPT, ActivityState.RUNNING);

    Mockito.verify(websocketHandlerFactory).sendJson(Mockito.any());
  }
}
