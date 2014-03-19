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

package interactivespaces.activity.impl.web;

import interactivespaces.service.web.server.BasicMultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import org.apache.commons.logging.Log;

/**
 * A {@link WebServerWebSocketHandlerFactory} which supports multiple
 * simultaneous web socket connections.
 *
 * @author Keith M. Hughes
 */
@Deprecated
public class MultipleConnectionWebServerWebSocketHandlerFactory extends
    BasicMultipleConnectionWebServerWebSocketHandlerFactory {

  /**
   * Construct a new factory.
   *
   * @param clientHandler
   *          the client handler for the connections
   * @param log
   *          the logger for issues
   *
   */
  public MultipleConnectionWebServerWebSocketHandlerFactory(MultipleConnectionWebSocketHandler clientHandler, Log log) {
    super(clientHandler, log);
  }

  /**
   * An interface for handling events to the web socket handlers.
   *
   * @author Keith M. Hughes
   */
  @Deprecated
  public interface MultipleConnectionWebSocketHandler extends
      interactivespaces.service.web.server.MultipleConnectionWebSocketHandler {
  }
}
