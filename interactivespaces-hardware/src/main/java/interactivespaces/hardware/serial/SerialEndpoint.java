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

package interactivespaces.hardware.serial;

import interactivespaces.InteractiveSpacesException;

/**
 * An endpoint for serial communication.
 *
 * @author Keith M. Hughes
 */
public interface SerialEndpoint {

	/**
	 * Start the endpoint up.
	 */
	void startup();

	/**
	 * Shut down all open serial ports managed by this endpoint.
	 * 
	 * <p>
	 * The node is left running.
	 */
	void shutdown();

	/**
	 * Add a new port on the given node.
	 * 
	 * @param node The node which will publish the subscribers and publishers.
	 * @param portName Name of the port to open. This will be OS dependent.
	 * 
	 * @throws InteractiveSpacesException Either the port is already open or something bad happened while
	 * openning it.
	 */
	void addPort(String portName);

	/**
	 * Shut down a specific port.
	 * 
	 * <p>
	 * Does nothing if port isn't open.
	 * 
	 * @param portName name of the port to shut down.
	 */
	void shutdownPort(String portName);

}