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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.CoreController;
import org.ros.osgi.master.core.CoreControllerListener;

/**
 * Support class that gives some convenience methods for the {@link CoreController}.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseCoreController implements CoreController {

	/**
	 * The ROS Environment this master is to run in.
	 */
	protected RosEnvironment rosEnvironment;
	
	/**
	 * All listeners for Core events.
	 */
	private List<CoreControllerListener> listeners = new CopyOnWriteArrayList<CoreControllerListener>();
	
	/**
	 * {@code true} if the core is started.
	 */
	protected boolean started;

	@Override
	public void addListener(CoreControllerListener listener) {
		// Will be missing the started message otherwise.
		if (started) {
			listener.onCoreStartup();
		}

		listeners.add(listener);
	}

	@Override
	public void removeListener(CoreControllerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Tell all listeners the master has started.
	 */
	protected void signalCoreStartup() {
		for (CoreControllerListener listener : listeners) {
			listener.onCoreStartup();
		}
	}

	/**
	 * Tell all listeners the master has shut doiwn.
	 */
	protected void signalCoreShutdown() {
		for (CoreControllerListener listener : listeners) {
			listener.onCoreShutdown();
		}
	}

	/**
	 * Set the Ros Environment the server should run in.
	 * 
	 * @param rosEnvironment
	 */
	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	/**
	 * Remove the Ros Environment the server should run in.
	 * 
	 * @param rosEnvironment
	 */
	public void unsetRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = null;
	}
}