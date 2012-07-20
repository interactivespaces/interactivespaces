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

package org.ros.osgi.deployment.master;

import java.util.List;

/**
 * Deploys features to a remote system.
 *
 * @author Keith M. Hughes
 */
public interface DeploymentServer {
	
	/**
	 * Start up the deployment server.
	 */
	void startup();
	
	/**
	 * Shut down the deployment server.
	 */
	void shutdown();
	
	/**
	 * Deploy a set of features to a node.
	 * 
	 * @param node The node to receive the features.
	 * @param features The features  to be deployed to the node.
	 */
	void deployFeature(String node, List<String> features);
}
