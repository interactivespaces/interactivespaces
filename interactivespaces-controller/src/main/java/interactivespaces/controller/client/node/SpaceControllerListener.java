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

import interactivespaces.controller.client.node.SpaceControllerActivityStatuses.ControllerActivityActivateStatus;
import interactivespaces.controller.client.node.SpaceControllerActivityStatuses.ControllerActivityDeactivateStatus;
import interactivespaces.controller.client.node.SpaceControllerActivityStatuses.ControllerActivityDeployStatus;
import interactivespaces.controller.client.node.SpaceControllerActivityStatuses.ControllerActivityShutdownStatus;
import interactivespaces.controller.client.node.SpaceControllerActivityStatuses.ControllerActivityStartStatus;

/**
 * A listener for events from an Interactive Spaces controller.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerListener {

  /**
   * An activity was deployed.
   *
   * @param uuid
   *          UUID of the activity
   * @param status
   *          status of the deploy
   */
  void activityDeploy(String uuid, ControllerActivityDeployStatus status);

  /**
   * An activity started up.
   *
   * @param uuid
   *          UUID of the activity
   * @param status
   *          status of the start
   */
  void activityStart(String uuid, ControllerActivityStartStatus status);

  /**
   * An activity shut down.
   *
   * @param uuid
   *          UUID of the activity
   * @param status
   *          status of the shutdown
   */
  void activityShutdown(String uuid, ControllerActivityShutdownStatus status);

  /**
   * An activity activated.
   *
   * @param uuid
   *          UUID of the activity
   * @param status
   *          status of the activation
   */
  void activityActivate(String uuid, ControllerActivityActivateStatus status);

  /**
   * An activity deactivated.
   *
   * @param uuid
   *          UUID of the activity.
   * @param status
   *          status of the deactivation
   */
  void activityDeactivate(String uuid, ControllerActivityDeactivateStatus status);
}
