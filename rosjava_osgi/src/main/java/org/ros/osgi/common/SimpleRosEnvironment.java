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

package org.ros.osgi.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.ros.concurrent.DefaultScheduledExecutorService;
import org.ros.exception.RosRuntimeException;
import org.ros.master.uri.MasterUriProvider;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeFactory;
import org.ros.node.NodeListener;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A very basic ROS environment.
 *
 * @author Keith M. Hughes
 */
public class SimpleRosEnvironment implements RosEnvironment {

  /**
   * Node runner for this environment.
   */
  private NodeMainExecutor nodeRunner;

  /**
   * Executor service used for running the ROS threads.
   */
  private ScheduledExecutorService executorService;

  /**
   * {@code true} is this instance owns the executor service.
   */
  private boolean ownExecutorService;

  /**
   * Host that this environment is running on.
   */
  private String host = "localhost";

  /**
   * Host the master is running on.
   */
  private URI masterUri = NodeConfiguration.DEFAULT_MASTER_URI;

  /**
   * Overall name for the node.
   */
  private String nodeName;

  /**
   * The network type for the ROS graph.
   *
   * <p>
   * This allows distinguishing between ROS Masters, e.g. localdev, prod,
   * fredbot.
   */
  private String networkType;

  /**
   * The container logger.
   */
  private Log log;

  /**
   * The properties associated with this environment.
   */
  private final Map<String, String> properties = Maps.newHashMap();

  /**
   * {@code true} if this is an environment for masters, false otherwise.
   */
  private boolean master;

  /**
   * A provider for ROS Master URIs.
   *
   * <p>
   * This can be null.
   */
  private MasterUriProvider masterUriProvider;

  /**
   * The node factory for creating ROS nodes.
   */
  private NodeFactory nodeFactory;

  /**
   * Start up the ROS environment.
   */
  public void startup() {
    // Get the URI of the master.
    String masterUri = getProperty(CONFIGURATION_ROS_MASTER_URI);
    if (masterUri != null) {
      try {
        setMasterUri(new URI(masterUri));
      } catch (URISyntaxException e) {
        throw new RosRuntimeException("Cannot start ros environment. Illegal master URI: "
            + masterUri, e);
      }
    }

    // Set the host running the node.
    String host = getProperty(CONFIGURATION_ROS_HOST);
    if (host != null)
      setHost(host);

    // Set the name of the node.
    String nodeName = getProperty(CONFIGURATION_ROS_NODE_NAME);
    if (nodeName != null)
      setNodeName(nodeName);

    String networkType = getProperty(CONFIGURATION_ROS_NETWORK_TYPE);
    if (networkType != null)
      setNetworkType(networkType);

    if (executorService == null) {
      executorService = new DefaultScheduledExecutorService();
      ownExecutorService = true;
    }

    nodeRunner = DefaultNodeMainExecutor.newDefault(executorService);

    nodeFactory = new DefaultNodeFactory(executorService);
  }

  /**
   * Shut the ROS environment down.
   */
  public void shutdown() {
    nodeRunner.shutdown();

    if (ownExecutorService) {
      executorService.shutdown();
    }
  }

  @Override
  public ConnectedNode newNode(NodeConfiguration configuration) {
    // TODO(ROS): Part of ROS update
    // return nodeFactory.newNode(configuration, true);
    return newNode(configuration, null);
  }

  @Override
  public ConnectedNode newNode(NodeConfiguration configuration, Collection<NodeListener> listeners) {
    // TODO(ROS): Part of ROS update
    // return nodeFactory.newNode(configuration, listeners, true);
    final AtomicReference<ConnectedNode> node = new AtomicReference<ConnectedNode>();

    final CountDownLatch registeredLatch = new CountDownLatch(1);

    NodeListener registrationListener = new NodeListener() {

      @Override
      public void onStart(ConnectedNode connectedNode) {
        node.set(connectedNode);
        registeredLatch.countDown();
      }

      @Override
      public void onShutdownComplete(Node node) {
        log.info(String.format("Shut down complete for node %s", node.getName()));
      }

      @Override
      public void onShutdown(Node node) {
        // Nothing to do
      }

      @Override
      public void onError(Node node, Throwable throwable) {
        log.error(String.format("Error for node %s", node.getName()), throwable);
      }
    };

    List<NodeListener> fullListeners = Lists.newArrayList(registrationListener);
    if (listeners != null) {
      fullListeners.addAll(listeners);
    }

    Node bareNode = nodeFactory.newNode(configuration, fullListeners);

    try {
      if (registeredLatch.await(10000, TimeUnit.MILLISECONDS)) {

        return node.get();
      } else {
        throw new RuntimeException(String.format("No registration connection made for ROS node %s",
            bareNode.getName()));
      }
    } catch (InterruptedException e) {
      return null;
    }
  }

  @Override
  public void executeNodeMain(NodeMain nodeMain, NodeConfiguration configuration) {
    try {
      nodeRunner.execute(nodeMain, configuration);
    } catch (Exception e) {
      log.error("Could not run node main", e);
    }
  }

  @Override
  public NodeConfiguration getPublicNodeConfiguration() {
    if (masterUriProvider != null) {
      NodeConfiguration conf = NodeConfiguration.newPublic(host, masterUriProvider.getMasterUri());
      conf.setLog(log);
      return conf;
    } else if (masterUri != null) {
      NodeConfiguration conf = NodeConfiguration.newPublic(host, masterUri);
      conf.setLog(log);
      return conf;
    } else {
      throw new RosRuntimeException("No ROS Master URI available");
    }
  }

  @Override
  public NodeConfiguration getPublicNodeConfigurationWithNodeName() {
    NodeConfiguration configuration = getPublicNodeConfiguration();
    configuration.setParentResolver(new NameResolver(GraphName.of(getNodeName()),
        new HashMap<GraphName, GraphName>()));

    return configuration;
  }

  @Override
  public NodeConfiguration getPublicNodeConfigurationWithNodeName(String subname) {
    NodeConfiguration configuration = getPublicNodeConfiguration();
    configuration.setParentResolver(new NameResolver(GraphName.of(getNodeName() + "/" + subname),
        new HashMap<GraphName, GraphName>()));

    return configuration;
  }

  @Override
  public NodeConfiguration getPrivateNodeConfiguration() {
    NodeConfiguration configuration = NodeConfiguration.newPrivate(masterUri);
    configuration.setLog(log);

    return configuration;
  }

  @Override
  public String getNodeName() {
    return nodeName;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public URI getMasterUri() {
    if (masterUriProvider != null) {
      return masterUriProvider.getMasterUri();
    } else {
      return masterUri;
    }
  }

  @Override
  public String getNetworkType() {
    return networkType;
  }

  @Override
  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public Log getLog() {
    return log;
  }

  /**
   * Set the root name of the node.
   *
   * @param nodeName
   *          The root name of the node.
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  /**
   * Set the URI of the ROS Master.
   *
   * @param masterUri
   *          The URI of the ROS Master.
   */
  public void setMasterUri(URI masterUri) {
    this.masterUri = masterUri;
  }

  /**
   * Set the host the environment is running on. This determines what network
   * interface is bound to.
   *
   * @param host
   *          The host.
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * The network type for the ROS graph.
   *
   * <p>
   * This allows distinguishing between ROS Masters, e.g. localdev, prod,
   * fredbot.
   *
   * @param networkType
   */
  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  /**
   * @param executorService
   *          the executorService to set
   */
  public void setExecutorService(ScheduledExecutorService executorService) {
    this.executorService = executorService;
    this.ownExecutorService = false;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }

  @Override
  public String getProperty(String property) {
    return properties.get(property);
  }

  @Override
  public void setProperty(String property, String value) {
    properties.put(property, value);
  }

  @Override
  public void setProperties(Map<String, String> properties) {
    properties.putAll(properties);
  }

  @Override
  public void setMasterUriProvider(MasterUriProvider masterUriProvider) {
    this.masterUriProvider = masterUriProvider;
  }

  @Override
  public boolean isMaster() {
    return master;
  }

  /**
   * Set whether this is a master environment or not.
   *
   * @param master
   *          {@code true} if it is a master environment.
   */
  public void setMaster(boolean master) {
    this.master = master;
  }
}
