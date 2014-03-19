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
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.configuration.Configuration;

import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.osgi.common.RosEnvironment;

/**
 * A basic {@link ActivityComponent} that gives ROS functionality.
 *
 * @author Keith M. Hughes
 */
public class BasicRosActivityComponent extends BaseActivityComponent implements RosActivityComponent {

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
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

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

  /* (non-Javadoc)
   * @see interactivespaces.activity.component.ros.RosActivityComponent#getNodeName()
   */
  @Override
  public String getNodeName() {
    return rosNodeName;
  }

  /* (non-Javadoc)
   * @see interactivespaces.activity.component.ros.RosActivityComponent#getNodeConfiguration()
   */
  @Override
  public NodeConfiguration getNodeConfiguration() {
    return nodeConfiguration;
  }

  /* (non-Javadoc)
   * @see interactivespaces.activity.component.ros.RosActivityComponent#getNode()
   */
  @Override
  public ConnectedNode getNode() {
    return node;
  }

  /* (non-Javadoc)
   * @see interactivespaces.activity.component.ros.RosActivityComponent#getRosEnvironment()
   */
  @Override
  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }
}
