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

package interactivespaces.messaging;

import interactivespaces.util.data.json.JsonBuilder;

import java.util.Map;

/**
 * A route message publisher that sends simple objects.
 *
 * @author Keith M. Hughes
 */
public interface MessagePublisher {

  /**
   * Send a message.
   *
   * <p>
   * The message will be serialized properly for the channel.
   *
   * @param message
   *          the message to send
   */
  void sendMessage(Map<String, Object> message);

  /**
   * Send an output message from a {@link JsonBuilder}.
   *
   * @param message
   *          the message to send
   */
  void sendMessage(JsonBuilder message);

  /**
   * Send an output string message.
   *
   * @param message
   *          the message to send
   */
  void sendMessage(String message);
}
