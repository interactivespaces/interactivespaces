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

package interactivespaces.master.event;

import interactivespaces.activity.ActivityState;
import interactivespaces.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;

/**
 * The manager for master events.
 *
 * @author Keith M. Hughes
 */
public interface MasterEventManager {

  /**
   * Add in a new event listener.
   *
   * @param listener
   *          the new listener
   */
  void addListener(MasterEventListener listener);

  /**
   * Remove an event listener.
   *
   * <p>
   * Does nothing if the listener wasn't registered.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(MasterEventListener listener);

  /**
   * Clear all listeners from the manager.
   */
  void removeAllListeners();

  /**
   * Send the live activity state change message to all listeners.
   *
   * @param liveActivity
   *          the activity
   * @param oldState
   *          old state of the remote activity
   * @param newState
   *          new state of the remote activity
   */
  void signalLiveActivityStateChange(ActiveLiveActivity liveActivity, ActivityState oldState, ActivityState newState);

  /**
   * Send the live activity deletion message to all listeners.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          result of the deletion
   */
  void signalLiveActivityDelete(ActiveLiveActivity liveActivity, LiveActivityDeleteResponse result);

  /**
   * Send the on deployment message to all listeners.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          result of the install
   * @param timestamp
   *          timestamp of the deployment
   */
  void
      signalLiveActivityDeploy(ActiveLiveActivity liveActivity, LiveActivityDeploymentResponse result, long timestamp);

  /**
   * Signal that the controller status has been updated.
   *
   * @param controller
   *          the space controller
   * @param state
   *          the new state
   */
  void signalSpaceControllerStatusChange(ActiveSpaceController controller, SpaceControllerState state);

  /**
   * Signal that a space controller status is shutting down.
   *
   * @param controller
   *          the space controller
   */
  void signalSpaceControllerShutdown(ActiveSpaceController controller);

  /**
   * Signal a space controller heartbeat.
   *
   * @param controller
   *          the space controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  void signalSpaceControllerHeartbeat(ActiveSpaceController controller, long timestamp);

  /**
   * Signal that master has lost the heartbeat from a controller.
   *
   * @param controller
   *          the controller
   * @param timeSinceLastHeartbeat
   *          the time since the last heartbeat that triggered the error, in milliseconds
   */
  void signalSpaceControllerHeartbeatLost(ActiveSpaceController controller, long timeSinceLastHeartbeat);

  /**
   * Signal a space controller disconnection attempt.
   *
   * @param controller
   *          the space controller
   */
  void signalSpaceControllerDisconnectAttempted(ActiveSpaceController controller);

  /**
   * Signal a space controller connection attempt.
   *
   * @param controller
   *          the space controller
   */
  void signalSpaceControllerConnectAttempted(ActiveSpaceController controller);
}
