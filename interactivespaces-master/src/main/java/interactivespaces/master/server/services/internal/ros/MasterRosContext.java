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

package interactivespaces.master.server.services.internal.ros;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.osgi.common.RosEnvironment;

import java.util.concurrent.CountDownLatch;

/**
 * A ROS context for the master.
 *
 * @author Keith M. Hughes
 */
public class MasterRosContext {

  /**
   * The ROS environment the client is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * Node for this client.
   */
  private ConnectedNode node;

  /**
   * Logger for this node.
   */
  private Log log;

  public void startup() {
    log.error("Starting up master ROS context");

    final NodeConfiguration nodeConfiguration =
        rosEnvironment.getPublicNodeConfigurationWithNodeName();
    nodeConfiguration.setNodeName("interactivespaces/master");

    final CountDownLatch registrationLatch = new CountDownLatch(1);

    NodeListener listener = new NodeListener() {

      @Override
      public void onStart(ConnectedNode connectedNode) {
        setConnectedNode(connectedNode);
        registrationLatch.countDown();
      }

      @Override
      public void onShutdownComplete(Node node) {
        log.error(String.format(
            "Got ROS node complete shutdown for Interactive Spaces master node %s", node.getName()));
      }

      @Override
      public void onShutdown(Node node) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onError(Node node, Throwable throwable) {
        log.error(
            String.format("Got ROS node error for Interactive Spaces master node %s",
                node.getName()), throwable);
      }
    };
    rosEnvironment.newNode(nodeConfiguration, Lists.newArrayList(listener));
  }

  public void shutdown() {
    if (node != null) {
      node.shutdown();
    }
  }

  public ConnectedNode getNode() {
    return node;
  }

  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }

  private void setConnectedNode(ConnectedNode node) {
    this.node = node;
  }

  /**
   * @param rosEnvironment
   *          the rosEnvironment to set
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
