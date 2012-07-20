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

package interactivespaces.controller.client.master;

import interactivespaces.domain.basic.LiveActivity;

/**
 * Install Interactive Spaces activities to the remote location.
 * 
 * @author Keith M. Hughes
 */
public interface RemoteActivityDeployer {
	
	/**
	 * Start the deployer up.
	 */
	void startup();

	/**
	 * Shut the deployer down.
	 */
	void shutdown();

	/**
	 * Deploy an activity to
	 * 
	 * @param activity
	 */
	void deployActivity(LiveActivity activity);

	/**
	 * Add a listener to the installer.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	void addListener(RemoteActivityDeployerListener listener);

	/**
	 * Remove a listener to the installer.
	 * 
	 * <p>
	 * Does nothing if the listener was never added.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	void removeListener(RemoteActivityDeployerListener listener);
}
