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
import java.util.Map;

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
  String WEBSOCKET_URI_PREFIX_DEFAULT = "websocket";

  /**
   * Add in a new static content handler to the server.
   *
   * <p>
   * See {@link #addStaticContentHandler(String, File, Map)}, the content header
   * map value will be {@code null}.
   *
   * @param uriPrefix
   *          URI prefix for the content
   * @param baseDir
   *          the base directory for the content
   */
  void addStaticContentHandler(String uriPrefix, File baseDir);

  /**
   * Add in a new static content handler to the server.
   *
   * <p>
   * Content handlers are attempted in the order added. The first prefix which
   * matches will be run.
   *
   * @param uriPrefix
   *          URI prefix for the content
   * @param baseDir
   *          the base directory for the content
   * @param extraHttpContentHeaders
   *          extra content headers to add to every response, can be {@code null}
   */
  void addStaticContentHandler(String uriPrefix, File baseDir, Map<String, String> extraHttpContentHeaders);

  /**
   * Add in a new static content handler to the server.
   *
   * <p>
   * Content handlers are attempted in the order added. The first prefix which
   * matches will be run.
   *
   * @param uriPrefix
   *          URI prefix for the content
   * @param baseDir
   *          the base directory for the content
   * @param extraHttpContentHeaders
   *          extra content headers to add to every response, can be {@code null}
   * @param fallbackHandler
   *          dynamic content handler to use if requested file is missing, can be {@code null}
   */
  void addStaticContentHandler(String uriPrefix, File baseDir, Map<String, String> extraHttpContentHeaders,
      HttpDynamicRequestHandler fallbackHandler);

  /**
   * Add in a new dynamic content handler to the server.
   *
   * <p>
   * See
   * {@link #addDynamicContentHandler(String, HttpDynamicRequestHandler, Map)},
   * the content header map value will be {@code null}.
   *
   * @param uriPrefix
   *          URI prefix for the content
   * @param usePath
   *          {@code true} if the path will be used for processing requests
   * @param handler
   *          dynamic request handler
   */
  void addDynamicContentHandler(String uriPrefix, boolean usePath, HttpDynamicRequestHandler handler);

  /**
   * Add in a new dynamic content handler to the server.
   *
   * <p>
   * Content handlers are attempted in the order added. The first prefix which
   * matches will be run.
   *
   * @param uriPrefix
   *          URI prefix for the content
   * @param usePath
   *          {@code true} if the path will be used for processing requests
   * @param handler
   *          dynamic request handler
   * @param extraHttpContentHeaders
   *          extra HTTP content headers to add to all responses to the handler,
   *          can be {@code null}
   */
  void addDynamicContentHandler(String uriPrefix, boolean usePath, HttpDynamicRequestHandler handler,
      Map<String, String> extraHttpContentHeaders);

  /**
   * Set the factory for creating web socket handlers.
   *
   * @param webSocketUriPrefix
   *          uri prefix for websocket handler
   * @param webSocketHandlerFactory
   *          the factory to use (can be {@code null} if don't want to handle
   *          web socket calls)
   */
  void setWebSocketHandlerFactory(String webSocketUriPrefix, WebServerWebSocketHandlerFactory webSocketHandlerFactory);

  /**
   * Set the listener for file uploads.
   *
   * @param listener
   *          the listener to use
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

  /**
   * Add an HTTP content header that will go out with every HTTP response.
   *
   * @param name
   *          name of the header
   * @param value
   *          value of the header
   */
  void addContentHeader(String name, String value);

  /**
   * Add an HTTP content header that will go out with every HTTP response.
   *
   * @param headers
   *          the headers to add, the key is the header name, value is the
   *          header value
   */
  void addContentHeaders(Map<String, String> headers);

  /**
   * Set the AuthProvider to use with this server, if no auth provider is set on
   * a server, it should not attempt any kind of access control. Setting the
   * auth provider to null should disable authorization checking on a server.
   *
   * @param authProvider
   *          the authentication provider
   */
  void setAuthProvider(HttpAuthProvider authProvider);

  /**
   * Set the access manager for this webserver. The access manager will only be
   * used if the auth provider is set.
   *
   * @param accessManager
   *          the access manager
   */
  void setAccessManager(WebResourceAccessManager accessManager);
}
