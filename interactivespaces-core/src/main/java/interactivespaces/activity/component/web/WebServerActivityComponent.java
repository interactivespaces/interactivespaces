/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.component.web;

import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import java.io.File;

/**
 * An activity component which creates a web server.
 *
 * @author Keith M. Hughes
 */
public interface WebServerActivityComponent extends ActivityComponent {

  /**
   * Name of the component.
   */
  String COMPONENT_NAME = "web.server";

  /**
   * Descriptive name of the component.
   */
  String COMPONENT_DESCRIPTION = "Web Server";

  /**
   * Get the main URL for web content for the component.
   *
   * @return the main URL for web content
   */
  String getWebContentUrl();

  /**
   * Get the port used by the web server for the component.
   *
   * @return the web server port
   */
  int getWebServerPort();

  /**
   * Get the web server backing the component.
   *
   * @return the web server
   */
  WebServer getWebServer();

  /**
   * Get the path for web content.
   *
   * @return the path for web content
   */
  String getWebContentPath();

  /**
   * Get the base directory for web content in the file system.
   *
   * @return the base directory for web content in the file system
   */
  File getWebContentBaseDir();

  /**
   * Set the URI prefix for websocket connections.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param webSocketUriPrefix
   *          the prefix for web socket connections (can be {@code null})
   *
   * @return the web server component this method was called on
   */
  WebServerActivityComponent setWebSocketUriPrefix(String webSocketUriPrefix);

  /**
   * Set the web socket handler factory for the web server to use.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param webSocketHandlerFactory
   *          the webSocketHandlerFactory to set
   *
   * @return the web server component this method was called on
   */
  WebServerActivityComponent setWebSocketHandlerFactory(WebServerWebSocketHandlerFactory webSocketHandlerFactory);

  /**
   * Set the HTTP file upload listener for the web server to use.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param httpFileUploadListener
   *          the HTTP file upload listener to use (can be {@code null})
   *
   * @return the web server component this method was called on
   */
  WebServerActivityComponent setHttpFileUploadListener(HttpFileUploadListener httpFileUploadListener);

  /**
   * Add static content for the web server to serve.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param baseDir
   *          the base directory where the content will be found
   *
   * @return the web server component this method was called on
   */
  WebServerActivityComponent addStaticContent(String uriPrefix, File baseDir);

  /**
   * Add dynamic content for the web server to serve.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param usePath
   *          {@code true} if the path will be used as part of request
   *          processing
   * @param handler
   *          content handler being added
   *
   * @return the web server component this method was called on
   */
  WebServerActivityComponent addDynamicContent(String uriPrefix, boolean usePath, HttpDynamicRequestHandler handler);
}
