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

package org.ros.osgi.deployment.common;

import java.util.HashMap;

import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.osgi.common.OsgiConstants;

/**
 * Various useful methods for ROS OSGi deployment code.
 *
 * @author Keith M. Hughes
 */
public class RosOsgiDeploymentSupport {
	
	/**
	 * Set up a node configuration with a parent name resolver that will refer to the
	 * deployment nodes for a given remote node.
	 * 
	 * @param configuration The configuration being modified.
	 * @param remoteNodeName	The node name of the remote node.
	 */
	public static void setNamespaceForDeployment(NodeConfiguration configuration, String remoteNodeName) {
		configuration.setParentResolver(new NameResolver(
				GraphName.of(remoteNodeName + "/" + OsgiConstants.DEPLOYMENT_ROOT_NAME), 
				new HashMap<GraphName,GraphName>()));
	}
}
