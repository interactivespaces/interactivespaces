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

/**
 * A listener for chat connection messages.
 *
 * @author Keith M. Hughes
 */
public interface ChatConnectionListener {

  /**
   * A message has come in.
   *
   * @param connection
   *          the connection the message came from
   * @param from
   *          the chat ID of the message sender
   * @param message
   *          the body of the message
   */
  void onMessage(ChatConnection connection, String from, String message);
}
