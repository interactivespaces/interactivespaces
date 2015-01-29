/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone.messaging;

import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;
import interactivespaces.util.resource.ManagedResource;

/**
 *
 * @author Trevor Pering
 */
public interface StandaloneRouter extends ManagedResource {

  /**
   * @return {@code true} if the component is currently running.
   */
  boolean isRunning();

  /**
   * Send a message. Will modify the outgoing message with medium-specific information.
   *
   * @param messageObject
   *          message to send.
   */
  void send(MessageMap messageObject);

  /**
   * Receive a multicast message.
   *
   * @return received message
   */
  MessageMap receive();
}
