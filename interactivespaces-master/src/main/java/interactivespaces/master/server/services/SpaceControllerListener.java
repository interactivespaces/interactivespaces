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
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;
import interactivespaces.master.server.services.internal.LiveActivityInstallResult;

/**
 * A listener for events from controllers which are controlled by a
 * {@link ActiveControllerManager}.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerListener {

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
   * @param uuid
   *          the UUID of the controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  void onSpaceControllerHeartbeat(String uuid, long timestamp);

  /**
   * The controller status has been updated.
   *
   * @param uuid
   *          the UUID of the space controller
   * @param state
   *          the new state
   */
  void onSpaceControllerStatusChange(String uuid, SpaceControllerState state);

  /**
   * An activity has been deployed.
   *
   * @param uuid
   *          uuid of the activity
   * @param result
   *          result of the installation attempt
   * @param timestamp
   *          timestamp of the event
   */
  void onLiveActivityInstall(String uuid, LiveActivityInstallResult result, long timestamp);

  /**
   * An activity has been deleted.
   *
   * @param uuid
   *          uuid of the activity
   * @param result
   *          result from the deletion attempt
   * @param timestamp
   *          timestamp of the event
   */
  void onLiveActivityDelete(String uuid, LiveActivityDeleteResult result, long timestamp);

  /**
   * A live activity has had a state change.
   *
   * @param activity
   *          the live activity whose state has changed
   * @param oldState
   *          the old state the activity had
   * @param newState
   *          the new state
   */
  void onLiveActivityStateChange(String uuid, ActivityState oldState, ActivityState newState);
}
