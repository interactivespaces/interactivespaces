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

package interactivespaces.service.web;

/**
 * A connection connection for a web socket server.
 *
 * @author Keith M. Hughes
 */
public interface WebSocketConnection {

  /**
   * Is the connection still open?
   *
   * @return {@code true} if the connection is open, {@code false} otherwise
   */
  boolean isOpen();

  /**
   * Close the connection.
   */
  void shutdown();

  /**
   * Write data out to the remote endpoint encoded as JSON.
   *
   * @param data
   *          data to write
   */
  void writeDataAsJson(Object data);

  /**
   * Write data out to the remote endpoint encoded as a raw string.
   *
   * @param data
   *          data to write
   */
  void writeDataAsString(String data);

  /**
   * Return the user id of the user who opened this socket connection
   *
   * @return the user id of the user who opened this connection.
   */
  String getUser();
}
