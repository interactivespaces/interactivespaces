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
import interactivespaces.container.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.container.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;

/**
 * A listener for events within the master.
 *
 * @author Keith M. Hughes
 */
public interface MasterEventListener {

  /**
   * A space controller connection is being attempted.
   *
   * @param controller
   *          the controller
   */
  void onSpaceControllerConnectAttempted(ActiveSpaceController controller);

  /**
   * A space controller disconnection is being attempted.
   *
   * @param controller
   *          the controller
   */
  void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller);

  /**
   * A controller has sent a heartbeat.
   *
   * @param controller
   *          the controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  void onSpaceControllerHeartbeat(ActiveSpaceController controller, long timestamp);

  /**
   * The master has lost the heartbeat from a controller.
   *
   * @param controller
   *          the controller
   * @param timeSinceLastHeartbeat
   *          the time since te last heartbeat that triggered the error, in milliseconds
   */
  void onSpaceControllerHeartbeatLost(ActiveSpaceController controller, long timeSinceLastHeartbeat);

  /**
   * The controller status has been updated.
   *
   * @param controller
   *          the space controller
   * @param state
   *          the new state
   */
  void onSpaceControllerStatusChange(ActiveSpaceController controller, SpaceControllerState state);

  /**
   * The space controller is shutting down.
   *
   * @param controller
   *          the space controller
   */
  void onSpaceControllerShutdown(ActiveSpaceController controller);

  /**
   * A live activity has been deployed.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          result of the installation attempt
   * @param timestamp
   *          timestamp of the event
   */
  void onLiveActivityDeploy(ActiveLiveActivity liveActivity, LiveActivityDeploymentResponse result, long timestamp);

  /**
   * A live activity has been deleted.
   *
   * @param liveActivity
   *          the activity
   * @param result
   *          result from the deletion attempt
   */
  void onLiveActivityDelete(ActiveLiveActivity liveActivity, LiveActivityDeleteResponse result);

  /**
   * A live activity has had a state change.
   *
   * @param liveActivity
   *          the live activity
   * @param oldState
   *          the old state the activity had
   * @param newState
   *          the new state
   */
  void onLiveActivityStateChange(ActiveLiveActivity liveActivity, ActivityState oldState, ActivityState newState);
}
