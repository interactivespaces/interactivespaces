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

package interactivespaces.controller.client.node;

import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.SpaceControllerStatus;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;

/**
 * Provide communications between a Space Controller and something controlling
 * it.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerCommunicator {

  /**
   * Notify the master that the controller has started.
   */
  void notifyRemoteMasterServerAboutStartup(SimpleSpaceController controllerInfo);

  /**
   * The controller is starting up.
   *
   * <p>
   * This is a chance for the communicator to do any additional startup
   */
  void onStartup();

  /**
   * The controller is shutting down.
   *
   * <p>
   * This is a chance for the communicator to do any additional shutdown.
   */
  void onShutdown();

  /**
   * Publish the state of an activity on the activity status topic.
   *
   * @param uuid
   *          the UUID of the live activity
   * @param newStatus
   *          the status to be sent
   */
  void publishActivityStatus(String uuid, ActivityStatus newStatus);

  /**
   * Create a {@link SpaceControllerHeartbeat} appropriate for the commuication
   * channel.
   *
   * @return A {@link SpaceControllerHeartbeat}
   */
  SpaceControllerHeartbeat newSpaceControllerHeartbeat();

  /**
   * @param controllerControl
   *          the controllerControl to set
   */
  void setControllerControl(SpaceControllerControl controllerControl);

  /**
   * Publish a controller data status message.
   * @param type
   *          type of transfer
   * @param statusCode
   *          status code (success/failure) for the message
   * @param e
   *          exception for an error, or {@code null} on success
   */
  void publishControllerDataStatus(SpaceControllerDataOperation type,
      SpaceControllerStatus statusCode, Exception e);
}
