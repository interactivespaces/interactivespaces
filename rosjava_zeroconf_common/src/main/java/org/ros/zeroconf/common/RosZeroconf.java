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

package org.ros.zeroconf.common;

import java.util.List;

/**
 * Zeroconf configuration for ROS.
 * 
 * @author Keith M. Hughes
 */
public interface RosZeroconf {

	/**
	 * Start the ROS zeroconf services up.
	 */
	void startup();

	/**
	 * Shut the ROS zeroconf services down.
	 */
	void shutdown();

	/**
	 * Add a new listener to the Zeroconf service.
	 * 
	 * @param listener
	 *            the new listener
	 */
	void addListener(RosZeroconfListener listener);

	/**
	 * Remove a listener to the Zeroconf service.
	 * 
	 * <p>
	 * Nothing happens if the listener was never added
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	void removeListener(RosZeroconfListener listener);

	/**
	 * Register a new master with Zeroconf.
	 * 
	 * @param masterInfo
	 */
	void registerMaster(ZeroconfRosMasterInfo masterInfo);

	/**
	 * Unregister a master with Zeroconf.
	 * 
	 * @param masterInfo
	 *            info about the master to unregister
	 */
	void unregisterMaster(ZeroconfRosMasterInfo masterInfo);

	/**
	 * Do a query for all known masters.
	 * 
	 * <p>
	 * This call should be used to pickup all known masters when first starting
	 * to use zeroconf. The listeners for this class are for dynamic
	 * registrations.
	 * 
	 * @return
	 */
	List<ZeroconfRosMasterInfo> getKnownMasters();

	/**
	 * Do a query for all known ROS masters of a given type.
	 * 
	 * <p>
	 * This call should be used to pickup masters when first starting to use
	 * zeroconf. The listeners for this class are for dynamic registrations.
	 * 
	 * @param type
	 *            the type of ROS master, e.g. localdev, prod
	 * 
	 * @return
	 */
	List<ZeroconfRosMasterInfo> getKnownMasters(String type);
}
