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
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.RosMasterController;
import org.ros.osgi.master.core.RosMasterControllerFactory;
import org.ros.osgi.master.core.RosMasterControllerListener;
import org.ros.osgi.master.core.internal.StandardRosMasterControllerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A ROS context for the Interactive Spaces Master.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterRosContext implements MasterRosContext {

  /**
   * The timeout default for the registration of the Interactive Spaces Master ROS node with the ROS master.
   */
  public static final int ROS_MASTER_REGISTRATION_TIMEOUT_DEFAULT = 10000;

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
   * Logger for this context.
   */
  private Log log;

  /**
   * The startup latch used for startup of the ROS node for the Interactive Spaces master.
   */
  private CountDownLatch startupLatch;

  /**
   * The timeout for waiting for a OS master registration for the Interactive Spaces Master ROS node.
   */
  private int rosMasterRegistrationTimeout = ROS_MASTER_REGISTRATION_TIMEOUT_DEFAULT;

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The factory for creating ROS Master Controller instances.
   */
  private RosMasterControllerFactory rosMasterControllerFactory;

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
   * Construct a new ROS context.
   */
  public StandardMasterRosContext() {
    this(new StandardRosMasterControllerFactory());
  }

  /**
   * Construct a new ROS context.
   *
   * @param rosMasterControllerFactory
   *          the factory for creating ROS Master Controller instances
   */
  @VisibleForTesting
  StandardMasterRosContext(RosMasterControllerFactory rosMasterControllerFactory) {
    this.rosMasterControllerFactory = rosMasterControllerFactory;
  }

  @Override
  public void startup() {
    log.info("Starting up the Interactive Spaces Master ROS context");

    startupLatch = new CountDownLatch(1);

    if (CONFIGURATION_VALUE_MASTER_ENABLE_TRUE.equals(spaceEnvironment.getSystemConfiguration().getPropertyString(
        CONFIGURATION_NAME_ROS_MASTER_ENABLE, CONFIGURATION_DEFAULT_ROS_MASTER_ENABLE))) {
      startupRosMasterController();
    } else {
      connectToRosMaster();
    }

    try {
      if (!startupLatch.await(rosMasterRegistrationTimeout, TimeUnit.MILLISECONDS)) {
        log.error(String.format(
            "Could not register the Interactive Spaces Master with the ROS Master within %d milliseconds",
            rosMasterRegistrationTimeout));
      }
    } catch (InterruptedException e) {
      SimpleInteractiveSpacesException.throwFormattedException("ROS Master Context Startup interrupted");
    }
  }

  @Override
  public void shutdown() {
    if (masterNode != null) {
      masterNode.shutdown();
    }

    if (rosMasterController != null) {
      rosMasterController.shutdown();
    }
  }

  /**
   * Get the ROS node for the Interactive Spaces Master.
   *
   * @return the ROS node for the Interactive Spaces Master
   */
  @Override
  public ConnectedNode getMasterNode() {
    if (masterNode != null) {
      return masterNode;
    } else {
      throw SimpleInteractiveSpacesException
          .newFormattedException("The Interactive Spaces Master is not connected to a ROS Master");
    }
  }

  @Override
  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }

  /**
   * Start the ROS master.
   */
  private void startupRosMasterController() {
    rosMasterController = rosMasterControllerFactory.newInternalController();
    rosMasterController.setRosEnvironment(rosEnvironment);

    rosMasterController.addListener(new RosMasterControllerListener() {
      @Override
      public void onRosMasterStartup() {
        connectToRosMaster();
      }

      @Override
      public void onRosMasterShutdown() {
        // Don't care
      }
    });

    rosMasterController.startup();
  }

  /**
   * Connect to the ROS master..
   */
  private void connectToRosMaster() {
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
    log.info(String.format("Got ROS node complete shutdown for Interactive Spaces master node %s",
        masterNode.getName()));

    if (rosMasterController != null) {
      rosMasterController.shutdown();
    }
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
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
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
