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

package interactivespaces.master.server.services;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;

/**
 * A support implementation of {@link SpaceControllerListener} which provides
 * empty methods for all events.
 *
 * @author Keith M. Hughes
 */
public abstract class SpaceControllerListenerSupport implements SpaceControllerListener {

  @Override
  public void onSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
    // Default is do nothing.
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    // Default is do nothing.
  }

  @Override
  public void onLiveActivityInstall(String uuid, LiveActivityDeploymentResponse result, long timestamp) {
    // Default is do nothing.
  }

  @Override
  public void onLiveActivityDelete(String uuid, LiveActivityDeleteResult result, long timestamp) {
    // Default is do nothing.
  }

  @Override
  public void
      onLiveActivityStateChange(String uuid, ActivityState oldState, ActivityState newState) {
    // Default is do nothing.
  }
}
