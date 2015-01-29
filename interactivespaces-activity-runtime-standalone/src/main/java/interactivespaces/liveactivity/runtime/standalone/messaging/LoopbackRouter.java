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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Simple standalone router that just loops messages back -- useful when no other valid interface is available.
 *
 * @author Trevor Pering
 */
public class LoopbackRouter implements StandaloneRouter {

  /**
   * Internal message queue of messages for loopback.
   */
  private final BlockingQueue<MessageMap> messageQueue = new LinkedBlockingQueue<MessageMap>();

  /**
   * Current state of this component.
   */
  private volatile boolean isRunning;

  /**
   * Basic constructor.
   *
   * @param configuration
   *          configuration of the router
   */
  public LoopbackRouter(Configuration configuration) {
  }

  @Override
  public void startup() {
    isRunning = true;
  }

  @Override
  public void shutdown() {
    isRunning = false;
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public void send(MessageMap messageObject) {
    messageQueue.add(messageObject);
  }

  @Override
  public MessageMap receive() {
    try {
      return messageQueue.take();
    } catch (InterruptedException e) {
      throw new SimpleInteractiveSpacesException("LoopbackRouter interrupted", e);
    }
  }
}
