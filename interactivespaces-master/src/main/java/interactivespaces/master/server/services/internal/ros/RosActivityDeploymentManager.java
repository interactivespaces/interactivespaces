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

import interactivespaces.controller.client.master.RemoteActivityInstallationManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityDeploymentManager;

import org.ros.osgi.deployment.master.DeploymentServer;

/**
 * An {@link ActivityDeploymentManager} which includes the ROS deployment
 * server.
 * 
 * @author Keith M. Hughes
 */
public class RosActivityDeploymentManager implements ActivityDeploymentManager {

	/**
	 * Deployer for activities.
	 */
	private RemoteActivityInstallationManager activityDeployer;

	/**
	 * The ROS OSGI deployment manager.
	 */
	private DeploymentServer rosDeploymentManager;

	@Override
	public void deployLiveActivity(ActiveLiveActivity activity) {
		activityDeployer.deployActivity(activity);
	}

	@Override
	public void deleteLiveActivity(ActiveLiveActivity activity) {
		activityDeployer.deleteActivity(activity);
	}

	/**
	 * @param activityDeployer
	 *            the activityDeployer to set
	 */
	public void setActivityDeployer(
			RemoteActivityInstallationManager activityDeployer) {
		this.activityDeployer = activityDeployer;
	}

	/**
	 * @param rosDeploymentManager
	 *            the rosDeploymentManager to set
	 */
	public void setRosDeploymentManager(DeploymentServer rosDeploymentManager) {
		this.rosDeploymentManager = rosDeploymentManager;
	}
}
