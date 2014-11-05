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

package interactivespaces.service.web.server;

/**
 * A factory for web socket handlers that support multiple connections.
 *
 * @author Keith M. Hughes
 */
public interface MultipleConnectionWebServerWebSocketHandlerFactory extends WebServerWebSocketHandlerFactory {

  /**
   * Are there any web sockets connected?
   *
   * @return {@code true} if there are any connections
   */
  boolean areWebSocketsConnected();

  /**
   * Send a JSON message to a specific web socket connection.
   *
   * @param connectionId
   *          the ID of the connection
   * @param data
   *          the data to send
   */
  void sendJson(String connectionId, Object data);

  /**
   * Send a JSON message to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  void sendJson(Object data);

  /**
   * Send a string to a specific web socket connection.
   *
   * @param connectionId
   *          the ID of the connection
   * @param data
   *          the data to send
   */
  void sendString(String connectionId, String data);

  /**
   * Send a string to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  void sendString(String data);
}
