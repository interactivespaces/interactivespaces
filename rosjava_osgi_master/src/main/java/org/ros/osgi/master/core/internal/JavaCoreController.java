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

package org.ros.osgi.master.core.internal;

import java.net.URI;

import org.ros.RosCore;
import org.ros.node.NodeConfiguration;

/**
 * Bring up a pure Java ROS Master.
 * 
 * @author Keith M. Hughes
 */
public class JavaCoreController extends BaseCoreController {

	/**
	 * The master.
	 */
	private RosCore master;

	@Override
	public void startup() {
		try {
			NodeConfiguration configuration = rosEnvironment
					.getPublicNodeConfiguration();
			URI masterUri = configuration.getMasterUri();
			master = RosCore.newPublic(masterUri.getHost(),
					masterUri.getPort(), rosEnvironment.getExecutorService());
			master.start();

			master.awaitStart();

			signalCoreStartup();

			started = true;

		} catch (InterruptedException e) {
			// TODO(keith): Decide what to do about exception.
			rosEnvironment.getLog().error("Could not start up master", e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		started = false;

		master.shutdown();
		master = null;
	}
}
