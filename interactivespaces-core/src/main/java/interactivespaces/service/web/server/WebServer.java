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

import interactivespaces.util.resource.ManagedResource;

import java.io.File;

/**
 * A web server for Interactive Spaces activities.
 * 
 * @author Keith M. Hughes
 */
public interface WebServer extends ManagedResource {

	/**
	 * The default for the web socket URI prefix. Does not include the forward
	 * slash.
	 */
	public static final String WEBSOCKET_URI_PREFIX_DEFAULT = "websocket";

	/**
	 * Start the server running.
	 */
	void startup();

	/**
	 * Shut the server down.
	 */
	void shutdown();

	/**
	 * Add in a new static content handler to the server.
	 * 
	 * <p>
	 * Content handlers are attempted in the order added. The first prefix which
	 * matches will be run.
	 * 
	 * @param uriPrefix
	 *            URI prefix for the content.
	 * @param baseDir
	 *            The base directory for the content.
	 */
	void addStaticContentHandler(String uriPrefix, File baseDir);

	/**
	 * Add in a new dynamic content handler to the server.
	 * 
	 * <p>
	 * Content handlers are attempted in the order added. The first prefix which
	 * matches will be run.
	 * 
	 * @param uriPrefix
	 *            URI prefix for the content
	 * @param handler
	 *            dynamic request handler
	 */
	void addDynamicContentHandler(String uriPrefix, HttpDynamicRequestHandler handler);

	/**
	 * Set the factory for creating web socket handlers.
	 * 
	 * @param webSocketUriPrefix
	 *            uri prefix for websocket handler
	 * @param webSocketHandlerFactory
	 *            the factory to use (can be {@code null} if don't want to
	 *            handle web socket calls)
	 */
	void setWebSocketHandlerFactory(String webSocketUriPrefix,
			WebServerWebSocketHandlerFactory webSocketHandlerFactory);

	/**
	 * Set the listener for file uploads.
	 * 
	 * @param listener
	 *            the listener to use
	 */
	void setHttpFileUploadListener(HttpFileUploadListener listener);

	/**
	 * Get the name given to the server.
	 * 
	 * <p>
	 * This is not the server's hostname.
	 * 
	 * @return the server name
	 */
	String getServerName();

	/**
	 * Get the port the server is listening on.
	 * 
	 * @return the port
	 */
	int getPort();
}