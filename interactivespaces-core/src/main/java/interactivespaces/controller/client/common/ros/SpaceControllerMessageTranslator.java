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

package interactivespaces.controller.client.common.ros;

import com.google.common.collect.Maps;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;

import interactivespaces_msgs.ControllerActivityStatus;

import java.util.Map;

/**
 * Translation from various types of ROS controller messages between masters and
 * controllers.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerMessageTranslator {

  /**
   * Map from the {@link ControllerActivityStatus} statuses to
   * {@link ActivityStatus}.
   */
  private static Map<Integer, ActivityState> CONTROLLER_STATUS_TO_APPLICATION_STATUS = Maps
      .newHashMap();

  /**
   * Map from the {@link ControllerActivityStatus} statuses to
   * {@link ActivityStatus}.
   */
  private static Map<Integer, ActivityState> APPLICATION_STATUS_TO_CONTROLLER_STATUS = Maps
      .newHashMap();

  static {
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_UNKNOWN,
        ActivityState.UNKNOWN);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_READY,
        ActivityState.READY);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_RUNNING,
        ActivityState.RUNNING);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_ACTIVE,
        ActivityState.ACTIVE);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_STARTUP_FAILURE,
        ActivityState.STARTUP_FAILURE);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_ACTIVATE_FAILURE,
        ActivityState.ACTIVATE_FAILURE);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_DEACTIVATE_FAILURE,
        ActivityState.DEACTIVATE_FAILURE);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_SHUTDOWN_FAILURE,
        ActivityState.SHUTDOWN_FAILURE);
    CONTROLLER_STATUS_TO_APPLICATION_STATUS.put(ControllerActivityStatus.STATUS_CRASH,
        ActivityState.CRASHED);
  }
}
