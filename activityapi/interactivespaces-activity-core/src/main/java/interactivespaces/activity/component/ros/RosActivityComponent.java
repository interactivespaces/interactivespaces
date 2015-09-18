/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.component.ros;

import interactivespaces.activity.component.ActivityComponent;

import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.osgi.common.RosEnvironment;

/**
 * An {@link ActivityComponent} that gives ROS functionality.
 *
 * @author Keith M. Hughes
 */
public interface RosActivityComponent extends ActivityComponent {

  /**
   * Name of the component.
   */
  String COMPONENT_NAME = "comm.ros";

  /**
   * Configuration property for specifying the Interactive Spaces ROS node name
   * for the activity.
   */
  String CONFIGURATION_ACTIVITY_ROS_NODE_NAME = "space.activity.ros.node.name";

  /**
   * Get the ROS node name for the activity.
   *
   * <p>
   * This method can be overwritten by a subclass. Default it to read the
   * configuration property {@link #CONFIGURATION_ACTIVITY_ROS_NODE_NAME}.
   *
   * @return the ROS node name for the activity
   */
  String getNodeName();

  /**
   * Get the ROS node configuration for this activity.
   *
   * @return the ROS node configuration
   */
  NodeConfiguration getNodeConfiguration();

  /**
   * Get the ROS node associated with this component.
   *
   * @return the ROS node associated with this component
   */
  ConnectedNode getNode();

  /**
   * Get the ROS environment being used by this component.
   *
   * @return the ROS environment being used by this component
   */
  RosEnvironment getRosEnvironment();
}
