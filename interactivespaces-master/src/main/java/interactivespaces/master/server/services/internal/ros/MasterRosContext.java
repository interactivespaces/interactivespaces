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

import org.apache.commons.logging.Log;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.osgi.common.RosEnvironment;

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
	 * Node main for this client.
	 */
	private NodeMain nodeMain;

	/**
	 * Node for this client.
	 */
	private Node node;
	
	/**
	 * Logger for this node.
	 */
	private Log log;

	public void startup() {
		log.error("Starting up master ROS context");
		
		final NodeConfiguration nodeConfiguration = rosEnvironment
				.getPublicNodeConfigurationWithNodeName();
		nodeConfiguration.setNodeName("interactivespaces/master");
		
		node = rosEnvironment.newNode(nodeConfiguration);
	}

	public void shutdown() {
		if (node != null) {
			node.shutdown();
		}
	}
	
	public Node getNode() {
		return node;
	}
	
	public RosEnvironment getRosEnvironment() {
		return rosEnvironment;
	}
	
	private void nodeStartup(Node node) {
		this.node = node;
	}

	/**
	 * @param rosEnvironment the rosEnvironment to set
	 */
	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(Log log) {
		this.log = log;
	}
}
