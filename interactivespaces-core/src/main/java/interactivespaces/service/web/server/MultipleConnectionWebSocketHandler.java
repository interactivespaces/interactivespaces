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
 * An interface for handling events to the web socket handlers.
 *
 * @author Keith M. Hughes
 */
public interface MultipleConnectionWebSocketHandler {

  /**
   * A New web socket connection has come in.
   *
   * @param connectionId
   *          the ID of the connection
   */
  void handleNewWebSocketConnection(String connectionId);

  /**
   * A web socket connection is closing.
   *
   * @param connectionId
   *          the ID of the connection
   */
  void handleWebSocketClose(String connectionId);

  /**
   * Data has been sent to the web socket connection.
   *
   * @param connectionId
   *          the ID of the connection that received the data
   */
  void handleWebSocketReceive(String connectionId, Object data);
}
