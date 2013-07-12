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

package interactivespaces.service.comm.chat;

import interactivespaces.comm.CommunicationEndpoint;

/**
 * A connection to a chat service.
 *
 * @author Keith M. Hughes
 */
public interface ChatConnection extends CommunicationEndpoint {

  /**
   * Get the user of the connection.
   *
   * @return the user the service is using
   */
  String getUser();

  /**
   * Send a message to the other end.
   *
   * @param to
   *          who to send the message to
   * @param message
   *          the message to send
   */
  void sendMessage(String to, String message);

  /**
   * Add a new listener.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(ChatConnectionListener listener);

  /**
   * Remove a listener.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(ChatConnectionListener listener);
}
