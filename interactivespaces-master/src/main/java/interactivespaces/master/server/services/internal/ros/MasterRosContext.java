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

package interactivespaces.master.server.services.internal.ros;

import interactivespaces.util.resource.ManagedResource;

import org.ros.node.ConnectedNode;
import org.ros.osgi.common.RosEnvironment;

/**
 * The ROS context for the Interactive Spaces Master.
 *
 * @author Keith M. Hughes
 */
public interface MasterRosContext extends ManagedResource {

  /**
   * Configuration property name for having the Interactive Spaces master create it's own ROS master.
   */
  String CONFIGURATION_NAME_ROS_MASTER_ENABLE = "interactivespaces.master.ros.master.enable";

  /**
   * Configuration property value for {@link #CONFIGURATION_NAME_ROS_MASTER_ENABLE} for having Interactive Spaces create
   * it's own ROS master.
   */
  String CONFIGURATION_VALUE_MASTER_ENABLE_TRUE = "true";

  /**
   * Configuration property value for {@link #CONFIGURATION_NAME_ROS_MASTER_ENABLE} for having Interactive Spaces not
   * create it's own ROS master.
   */
  String CONFIGURATION_VALUE_MASTER_ENABLE_FALSE = "false";

  /**
   * Configuration property name for having Interactive Spaces create it's own ROS master.
   */
  String CONFIGURATION_DEFAULT_ROS_MASTER_ENABLE = CONFIGURATION_VALUE_MASTER_ENABLE_TRUE;

  /**
   * The ROS node name for the master.
   */
  String ROS_NODENAME_INTERACTIVESPACES_MASTER = "interactivespaces/master";

  /**
   * Get the ROS environment for the context.
   *
   * @return the ROS environment for the context
   */
  RosEnvironment getRosEnvironment();

  /**
   * Get the ROS node for the master.
   *
   * @return the ROS node for the master
   */
  ConnectedNode getMasterNode();
}
