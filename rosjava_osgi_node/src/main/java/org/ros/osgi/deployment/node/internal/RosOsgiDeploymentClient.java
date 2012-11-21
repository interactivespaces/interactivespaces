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

package org.ros.osgi.deployment.node.internal;

import java.io.File;

import org.osgi.framework.BundleContext;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.deployment.common.DeploymentInstaller;
import org.ros.osgi.deployment.common.DeploymentInstallerFactory;
import org.ros.osgi.deployment.node.DeploymentClient;

/**
 * A deployment client for ROS.
 * 
 * TODO(keith): This is dead in the water
 * 
 * @author Keith M. Hughes
 */
public class RosOsgiDeploymentClient implements DeploymentClient {

	/**
	 * The ROS environment being used.
	 */
	private RosEnvironment rosEnvironment;

	/**
	 * The factory for creating {@link DeploymentInstaller} instances.
	 */
	private DeploymentInstallerFactory deploymentInstallerFactory;

	/**
	 * Place where deployments will go.
	 */
	private File deploymentDirectory;

	public void activate(final BundleContext context) {
		deploymentDirectory = new File("bootstrap");

		deploymentInstallerFactory = new DeploymentInstallerFactory() {

			@Override
			public DeploymentInstaller createInstaller() {
				return new OsgiDeploymentInstaller(context,
						deploymentDirectory, rosEnvironment.getLog());
			}

		};

	}

	public void deactivate(BundleContext context) {
	}

	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	public void clearRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = null;
	}

}
