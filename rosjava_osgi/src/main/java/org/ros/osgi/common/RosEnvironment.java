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

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.ros.master.uri.MasterUriProvider;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.node.NodeMain;

/**
 * A ROS environment.
 * 
 * <p>
 * This contains the node configuration to use and access to the environments
 * thread pools.
 * 
 * @author Keith M. Hughes
 */
public interface RosEnvironment {

	/**
	 * Name the ROS node will have as its base name.
	 */
	public static final String PROPERTY_ROS_NODE_NAME = "org.ros.node.name";

	/**
	 * The host this node is running on. Can either be a resolvable domain name
	 * or an IP address.
	 */
	public static final String PROPERTY_ROS_HOST = "org.ros.host";

	/**
	 * URI of the ROS master.
	 */
	public static final String PROPERTY_ROS_MASTER_URI = "org.ros.master.uri";

	/**
	 * Name the ROS node will have as its base name.
	 */
	public static final String PROPERTY_ROS_NETWORK_TYPE = "org.ros.network.type";

	/**
	 * Configuration property giving the ROS container type.
	 */
	public static final String CONFIGURATION_ROS_CONTAINER_TYPE = "org.ros.container.type";

	/**
	 * Configuration property value for the master ROS container type.
	 */
	public static final String CONFIGURATION_ROS_CONTAINER_TYPE_MASTER = "master";

	/**
	 * Configuration property value for the node ROS container type.
	 */
	public static final String CONFIGURATION_ROS_CONTAINER_TYPE_NODE = "node";

	/**
	 * Configuration property giving the ROS network type, e.g. prod, dev,
	 * local.
	 */
	public static final String CONFIGURATION_ROS_NETWORK_TYPE = "org.ros.network.type";

	/**
	 * Get the node configuration to be used as a public node for this
	 * environment.
	 * 
	 * @return
	 */
	NodeConfiguration getPublicNodeConfiguration();

	/**
	 * Get a public node configuration where names will be resolved via the node
	 * name returned by {@link #getNodeName()}.
	 * 
	 * @return
	 */
	NodeConfiguration getPublicNodeConfigurationWithNodeName();

	/**
	 * Get a public node configuration where names will be resolved via the node
	 * name returned by {@link #getNodeName()} plus the additional subname.
	 * 
	 * <p>
	 * For example, if {@link #getNodeName()} returns {@code /foobar}, the node
	 * name will be rooted at {@code /foobar/subname}.
	 * 
	 * @param subname
	 *            The next level of node name.
	 * 
	 * @return
	 */
	NodeConfiguration getPublicNodeConfigurationWithNodeName(String subname);

	/**
	 * Get the node configuration to be used as a private node for this
	 * environment.
	 * 
	 * @return
	 */
	NodeConfiguration getPrivateNodeConfiguration();

	/**
	 * Create a node from the given node configuration.
	 * 
	 * @param configuration
	 *            the configuration describing the node
	 * 
	 * @return a newly created node.
	 */
	Node newNode(NodeConfiguration configuration);

	/**
	 * Create a node from the given node configuration with the given set of
	 * node listeners.
	 * 
	 * @param configuration
	 *            the configuration describing the node
	 * @param listeners
	 *            the listeners to add to the node on creation
	 * 
	 * @return a newly created node.
	 */
	Node newNode(NodeConfiguration nodeConfiguration,
			Collection<NodeListener> listeners);

	/**
	 * Run a node in the environment.
	 * 
	 * @param node
	 *            The node to run
	 * @param configuration
	 *            Configuration to run the node in.
	 */
	void executeNodeMain(NodeMain node, NodeConfiguration configuration);

	/**
	 * Get the name of the node for this environment.
	 * 
	 * @return
	 */
	String getNodeName();

	/**
	 * Get the master URI.
	 * 
	 * <p>
	 * All {@link NodeConfiguration} instances returned by this instance will
	 * have this master URI.
	 * 
	 * @return The master URI.
	 */
	URI getMasterUri();

	/**
	 * Get the host ROS is running on.
	 * 
	 * <p>
	 * All {@link NodeConfiguration} instances returned by this instance will
	 * have this host.
	 * 
	 * @return The host.
	 */
	String getHost();

	/**
	 * Get the executor service being used by the container.
	 * 
	 * @return
	 */
	ScheduledExecutorService getExecutorService();

	/**
	 * Get the container logger.
	 * 
	 * @return
	 */
	Log getLog();

	/**
	 * Get the value of a property from the ROS environment.
	 * 
	 * @param property
	 *            the name of the property
	 * 
	 * @return the value if the property if it exists, or {@code null}
	 */
	String getProperty(String property);

	/**
	 * Set the value of a property in the ROS environment.
	 * 
	 * @param property
	 *            the name of the property
	 * 
	 * @param value
	 *            the value if the property
	 */
	void setProperty(String property, String value);

	/**
	 * Is this environment a master environment?
	 * 
	 * @return
	 */
	boolean isMaster();

	/**
	 * Set the master URI provider the environment will use.
	 * 
	 * @param masterUriProvider
	 */
	void setMasterUriProvider(MasterUriProvider masterUriProvider);

	/**
	 * Get the network type for the ROS graph.
	 * 
	 * <p>
	 * This allows distinguishing between ROS masters, e.g. localdev, prod,
	 * fredbot.
	 * 
	 * @return
	 */
	String getNetworkType();
}
