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

package interactivespaces.controller.common.ros;

import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;

/**
 * A collection of constants for working with Interactive Spaces space
 * controllers.
 *
 * @author Keith M. Hughes
 */
public class RosSpaceControllerConstants {

  /**
   * Topic name for controller requests.
   */
  public static final String CONTROLLER_REQUEST_TOPIC_NAME = "interactivespaces/controller/request";

  /**
   * Topic message type for controller requests.
   */
  public static final String CONTROLLER_REQUEST_MESSAGE_TYPE = ControllerRequest._TYPE;

  /**
   * Topic name for controller status updates.
   */
  public static final String CONTROLLER_STATUS_TOPIC_NAME = "interactivespaces/controller/status";

  /**
   * Topic message type for controller status updates.
   */
  public static final String CONTROLLER_STATUS_MESSAGE_TYPE = ControllerStatus._TYPE;
}
