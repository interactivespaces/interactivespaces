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

import interactivespaces.SimpleInteractiveSpacesException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.RosMasterController;
import org.ros.osgi.master.core.RosMasterControllerListener;
import org.ros.osgi.master.core.internal.RosJavaRosMasterController;

import java.util.concurrent.CountDownLatch;

/**
 * A ROS context for the Interactive Spaces Master.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterRosContext implements MasterRosContext {

  /**
   * The ROS environment the client is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The ROS Master controller to use.
   */
  private RosMasterController rosMasterController;

  /**
   * Node for this client.
   */
  private ConnectedNode masterNode;

  /**
   * Logger for this node.
   */
  private Log log;

  /**
   * The startup latch used for startup.
   */
  private CountDownLatch startupLatch;

  /**
   * The master node listener.
   */
  private NodeListener masterNodeListener = new NodeListener() {
    @Override
    public void onStart(ConnectedNode connectedNode) {
      handleMasterRosNodeStartup(connectedNode);
    }

    @Override
    public void onShutdownComplete(Node node) {
      handleMasterRosNodeCompleteShutdown();
    }

    @Override
    public void onShutdown(Node node) {
      // Nothing to do
    }

    @Override
    public void onError(Node node, Throwable throwable) {
      handleMasterRosNodeError(node, throwable);
    }
  };

  /**
   * Construct a new master context.
   */
  public StandardMasterRosContext() {
    this(new RosJavaRosMasterController());
  }

  /**
   * Construct a new context.
   *
   * @param rosMasterController
   *          the ROS Master controller to use
   */
  @VisibleForTesting
  StandardMasterRosContext(RosMasterController rosMasterController) {
    this.rosMasterController = rosMasterController;
  }

  @Override
  public void startup() {
    log.info("Starting up the Interactive Spaces Master ROS context");

    startupLatch = new CountDownLatch(1);
    rosMasterController.setRosEnvironment(rosEnvironment);

    rosMasterController.addListener(new RosMasterControllerListener() {
      @Override
      public void onRosMasterStartup() {
        handleRosMasterStartup(startupLatch);
      }

      @Override
      public void onRosMasterShutdown() {
        // Don't care
      }
    });

    rosMasterController.startup();

    try {
      startupLatch.await();
    } catch (InterruptedException e) {
      SimpleInteractiveSpacesException.throwFormattedException("ROS Master Context Startup interrupted");
    }
  }

  @Override
  public void shutdown() {
    if (masterNode != null) {
      masterNode.shutdown();
    }
  }

  /**
   * Get the ROS node for the Interactive Spaces Master.
   *
   * @return the ROS node for the Interactive Spaces Master
   */
  @Override
  public ConnectedNode getMasterNode() {
    return masterNode;
  }

  @Override
  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }

  /**
   * Handle the ROS Master starting up.
   *
   * @param startupLatch
   *          the latch for startup completion
   */
  private void handleRosMasterStartup(final CountDownLatch startupLatch) {
    NodeConfiguration nodeConfiguration = rosEnvironment.getPublicNodeConfigurationWithNodeName();
    nodeConfiguration.setNodeName(ROS_NODENAME_INTERACTIVESPACES_MASTER);

    rosEnvironment.newNode(nodeConfiguration, Lists.newArrayList(masterNodeListener));
  }

  /**
   * Handle the startup of the ROS node for the Interactive Spaces Master.
   *
   * @param masterNode
   *          the Interactive Spaces Master's ROS node
   */
  private void handleMasterRosNodeStartup(ConnectedNode masterNode) {
    this.masterNode = masterNode;
    startupLatch.countDown();
  }

  /**
   * Handle any operations after the complete shutdown of the ROS node for the Interactive Spaces Master.
   */
  private void handleMasterRosNodeCompleteShutdown() {
    log.info(String.format("Got ROS node complete shutdown for Interactive Spaces master node %s", masterNode.getName()));
    rosMasterController.shutdown();
  }

  /**
   * Handle an error in the Interactive Spaces Master's ROS node.
   *
   * @param node
   *          the master's ROS node
   * @param throwable
   *          the error
   */
  private void handleMasterRosNodeError(Node node, Throwable throwable) {
    log.error(String.format("Got ROS node error for Interactive Spaces master node %s", node.getName()), throwable);
  }

  /**
   * Set the ROS environment for the context.
   *
   * @param rosEnvironment
   *          the ROS Environment
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * Set the log for the context.
   *
   * @param log
   *          the log
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * Get the Interactive Spaces Master's ROS node listener.
   *
   * @return the Interactive Spaces Master's ROS node listener
   */
  @VisibleForTesting
  NodeListener getMasterNodeListener() {
    return masterNodeListener;
  }
}
