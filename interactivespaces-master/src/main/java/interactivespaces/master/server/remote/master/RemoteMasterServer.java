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

package interactivespaces.master.server.remote.master;

/**
 * A remote master server.
 * 
 * <p>
 * This server listens for networking events such as controller startup, and
 * intra-master communication for failover.
 * 
 * @author Keith M. Hughes
 */
public interface RemoteMasterServer {

	/**
	 * Start the server up.
	 */
	void startup();

	/**
	 * Shut the server down.
	 */
	void shutdown();

	/**
	 * Add a new listener to the server.
	 * 
	 * @param listener
	 *            the new listener
	 */
	void addListener(RemoteMasterServerListener listener);

	/**
	 * Remove a listener from the server.
	 * 
	 * <p>
	 * Does nothing if the listener was never registered.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	void removeListener(RemoteMasterServerListener listener);
}