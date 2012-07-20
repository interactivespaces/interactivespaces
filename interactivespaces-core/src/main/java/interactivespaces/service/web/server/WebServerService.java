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

package interactivespaces.service.web.server;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;

/**
 * A service for obtaining web servers.
 * 
 * @author Keith M. Hughes
 */
public interface WebServerService {

	/**
	 * Start the service up.
	 */
	void startup();

	/**
	 * Shut the service down.
	 * 
	 * <p>
	 * All running servers will also be shut down.
	 */
	void shutdown();

	/**
	 * Create a new server.
	 * 
	 * @param serverName
	 *            Name of the server.
	 * @param port
	 *            Port the server should be on.
	 * @param threadPool
	 *            Thread pool for the server.
	 * @param log
	 *            Logger to be used with the server.
	 * 
	 * @return The web server.
	 */
	WebServer newWebServer(String serverName, int port,
			ScheduledExecutorService threadPool, Log log);

	/**
	 * Get the web server.
	 * 
	 * @param serverName
	 *            name of the web server to get.
	 * 
	 * @return The server with the associated name, or null if no such server.
	 */
	WebServer getWebServer(String serverName);

	/**
	 * Shut down the server with the specified name. The server is then removed
	 * from the collection of servers controlled by this service.
	 * 
	 * <p>
	 * Do nothing if there is no server with the given name.
	 * 
	 * @param serverName
	 *            name of the server to shut down.
	 */
	void shutdownServer(String serverName);

}