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

package interactivespaces.service.web.client;

import interactivespaces.service.SupportedService;
import interactivespaces.service.web.WebSocketHandler;

import org.apache.commons.logging.Log;

/**
 * A service for obtaining web socket client instances.
 *
 * @author Keith M. Hughes
 */
public interface WebSocketClientService extends SupportedService {

  /**
   * Service name for the service.
   */
  public static final String SERVICE_NAME = "web.client.websocket";

  /**
   * Create a new server.
   *
   * @param uri
   *          the uri to connect to
   * @param handler
   *          the handler for web socket events
   * @param log
   *          logger to be used with the client
   *
   * @return The web server.
   */
  WebSocketClient newWebSocketClient(String uri, WebSocketHandler handler, Log log);
}
