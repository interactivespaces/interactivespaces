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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ros.osgi.deployment.common.DeploymentInstaller;

/**
 * An OSGi deployment installer.
 * 
 * <p>
 * This should only be used for 1 round of installations.
 * 
 * @author Keith M. Hughes
 */
public class OsgiDeploymentInstaller implements DeploymentInstaller {

	/**
	 * A bundle context to use for installing the bundles.
	 */
	private BundleContext bundleContext;

	/**
	 * The bundles installed
	 */
	private List<Bundle> bundles = new ArrayList<Bundle>();

	/**
	 * Directory where files will be copied.
	 */
	private File deploymentDirectory;
	
	/**
	 * Logger for the instance.
	 */
	private Log log;

	public OsgiDeploymentInstaller(BundleContext bundleContext,
			File deploymentDirectory, Log log) {
		this.bundleContext = bundleContext;
		this.deploymentDirectory = deploymentDirectory;
		this.log = log;
	}

	@Override
	public DeploymentStatus install(String bundleLocation) {
		try {
			// TODO(keith): Need to copy the bundle into the deploymentDirectory and change
			// its location to point there.
			Bundle bundle = bundleContext.installBundle(bundleLocation);

			bundles.add(bundle);

			return DeploymentStatus.OK;
		} catch (BundleException e) {
			log.error(String.format("Cannot load bundle %s", bundleLocation), e);

			return DeploymentStatus.ERROR;
		}
	}

	@Override
	public void finish() {
		for (Bundle bundle : bundles) {
			try {
				bundle.start();
			} catch (BundleException e) {
				log.error(String.format("Cannot start bundle %s", bundle.getSymbolicName()), e);
			}
		}
	}

}
