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

package interactivespaces.master.event;

import interactivespaces.activity.ActivityState;
import interactivespaces.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;

/**
 * A support implementation of {@link MasterEventListener} which provides empty methods for all events.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseMasterEventListener implements MasterEventListener {

  @Override
  public void onSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerHeartbeat(ActiveSpaceController controller, long timestamp) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerHeartbeatLost(ActiveSpaceController controller, long timeSinceLastHeartbeat) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerStatusChange(ActiveSpaceController controller, SpaceControllerState state) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerShutdown(ActiveSpaceController controller) {
    // Default is do nothing.
  }

  @Override
  public void onLiveActivityDeploy(ActiveLiveActivity liveActivity, LiveActivityDeploymentResponse result,
      long timestamp) {
    // Default is do nothing.
  }

  @Override
  public void onLiveActivityDelete(ActiveLiveActivity liveActivity, LiveActivityDeleteResponse result) {
    // Default is do nothing.
  }

  @Override
  public void
      onLiveActivityStateChange(ActiveLiveActivity liveActivity, ActivityState oldState, ActivityState newState) {
    // Default is do nothing.
  }
}
