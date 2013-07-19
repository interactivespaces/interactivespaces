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

package interactivespaces.activity.component.ros;

import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.configuration.Configuration;

import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.osgi.common.RosEnvironment;

/**
 * An {@link ActivityComponent} that gives ROS functionality.
 *
 * @author Keith M. Hughes
 */
public class RosActivityComponent extends BaseActivityComponent {

  /**
   * Name of the component.
   */
  public static final String COMPONENT_NAME = "comm.ros";

  /**
   * Configuration property for specifying the Interactive Spaces ROS node name
   * for the activity.
   */
  public static final String CONFIGURATION_ACTIVITY_ROS_NODE_NAME = "space.activity.ros.node.name";

  /**
   * ROS Environment the activity is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The mainNode configuration for this app.
   */
  private NodeConfiguration nodeConfiguration;

  /**
   * The node for this component.
   */
  private ConnectedNode node;

  /**
   * Node name for the ROS node.
   */
  private String rosNodeName;

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public void configureComponent(Configuration configuration,
      ActivityComponentContext componentContext) {
    super.configureComponent(configuration, componentContext);

    rosEnvironment =
        componentContext.getActivity().getSpaceEnvironment().getValue("environment.ros");

    rosNodeName = configuration.getRequiredPropertyString(CONFIGURATION_ACTIVITY_ROS_NODE_NAME);

    nodeConfiguration = rosEnvironment.getPublicNodeConfigurationWithNodeName();
    nodeConfiguration.setLog(componentContext.getActivity().getLog());
    nodeConfiguration.setNodeName(rosNodeName);
  }

  @Override
  public void startupComponent() {
    node = rosEnvironment.newNode(nodeConfiguration);

    getComponentContext()
        .getActivity()
        .getLog()
        .info(
            String.format("Start up ROS activity component with ROS node name %s", node.getName()));
  }

  @Override
  public void shutdownComponent() {
    if (node != null) {
      node.shutdown();
      node = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    return node != null;
  }

  /**
   * Get the ROS node name for the activity.
   *
   * <p>
   * This method can be overwritten by a subclass. Default it to read the
   * configuration property {@link #CONFIGURATION_ACTIVITY_ROS_NODE_NAME}.
   *
   * @return The ROS node name for the activity.
   */
  public String getNodeName() {
    return rosNodeName;
  }

  /**
   * Get the node configuration for this activity.
   *
   * @return
   */
  public NodeConfiguration getNodeConfiguration() {
    return nodeConfiguration;
  }

  /**
   * Get the ROS node associated with this component.
   *
   * @return the ROS node associated with this component
   */
  public ConnectedNode getNode() {
    return node;
  }

  /**
   * Get the ROS environment being used by this component.
   *
   * @return the ROS environment being used by this component
   */
  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }
}
