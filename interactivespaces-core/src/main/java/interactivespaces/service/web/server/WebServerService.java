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

import interactivespaces.service.SupportedService;
import interactivespaces.util.web.MimeResolver;

import org.apache.commons.logging.Log;

/**
 * A service for obtaining web servers.
 *
 * @author Keith M. Hughes
 */
public interface WebServerService extends SupportedService {

  /**
   * name for the service.
   */
  String SERVICE_NAME = "web.server";

  /**
   * Create a new server.
   *
   * @param serverName
   *          name of the server
   * @param port
   *          port the server should be on
   * @param log
   *          logger to be used with the server
   *
   * @return the web server
   */
  WebServer newWebServer(String serverName, int port, Log log);

  /**
   * Create a new server.
   *
   * <p>
   * The server name and port will have to be set before the server can be started.
   *
   * @param log
   *          logger to be used with the server
   *
   * @return the web server
   */
  WebServer newWebServer(Log log);

  /**
   * Get the web server.
   *
   * @param serverName
   *          name of the web server to get
   *
   * @return The server with the associated name, or {@code null} if no such server
   */
  WebServer getWebServer(String serverName);

  /**
   * Shut down the server with the specified name. The server is then removed from the collection of servers controlled
   * by this service.
   *
   * <p>
   * Do nothing if there is no server with the given name.
   *
   * @param serverName
   *          name of the server to shut down
   */
  void shutdownServer(String serverName);

  /**
   * Get the default HTTP MIME resolver to use.
   *
   * @param <T>
   *          the type of the MIME resolver
   *
   * @return the default HTTP MIME resolver, can be {@code null}
   */
  <T extends MimeResolver> T getDefaultHttpMimeResolver();

  /**
   * Set the default HTTP MIME resolver to use.
   *
   * @param resolver
   *          the default HTTP MIME resolver, can be {@code null}
   */
  void setDefaultHttpMimeResolver(MimeResolver resolver);
}
