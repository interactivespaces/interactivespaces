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

package interactivespaces.service.web.server;

import interactivespaces.service.web.WebSocketConnection;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A basic implementation of the
 * {@link MultipleConnectionWebServerWebSocketHandlerFactory}.
 *
 * @author Keith M. Hughes
 */
public class BasicMultipleConnectionWebServerWebSocketHandlerFactory implements
    MultipleConnectionWebServerWebSocketHandlerFactory {

  /**
   * The client handler.
   */
  private final MultipleConnectionWebSocketHandler clientHandler;

  /**
   * A map from connect IDs to handlers.
   */
  private final Map<String, MyWebServerWebSocketHandler> handlers = Maps.newConcurrentMap();

  /**
   * Creator of connection IDs.
   */
  private final AtomicLong connectionIdFactory = new AtomicLong(System.currentTimeMillis());

  /**
   * Log.
   */
  private final Log log;

  /**
   * Construct a basic factory.
   *
   * @param clientHandler
   *          the client handler to use
   * @param log
   *          the logger to use
   */
  public BasicMultipleConnectionWebServerWebSocketHandlerFactory(MultipleConnectionWebSocketHandler clientHandler,
      Log log) {
    this.clientHandler = clientHandler;
    this.log = log;
  }

  @Override
  public WebServerWebSocketHandler newWebSocketHandler(WebSocketConnection connection) {
    return new MyWebServerWebSocketHandler(connection);
  }

  @Override
  public boolean areWebSocketsConnected() {
    return !handlers.isEmpty();
  }

  @Override
  public void sendJson(String connectionId, Object data) {
    MyWebServerWebSocketHandler handler = handlers.get(connectionId);
    if (handler != null) {
      handler.sendJson(data);
    } else {
      log.error(String.format("Unknown web socket connection ID %s", connectionId));
    }
  }

  @Override
  public void sendJson(Object data) {
    for (MyWebServerWebSocketHandler handler : handlers.values()) {
      handler.sendJson(data);
    }
  }

  @Override
  public void sendString(String connectionId, String data) {
    MyWebServerWebSocketHandler handler = handlers.get(connectionId);
    if (handler != null) {
      handler.sendString(data);
    } else {
      log.error(String.format("Unknown web socket connection ID %s", connectionId));
    }
  }

  @Override
  public void sendString(String data) {
    for (MyWebServerWebSocketHandler handler : handlers.values()) {
      handler.sendString(data);
    }
  }

  /**
   * Create a new connection ID.
   *
   * @return the new connection ID
   */
  private String newConnectionId() {
    return Long.toHexString(connectionIdFactory.getAndAdd(1));
  }

  /**
   * Web socket handler for this class.
   *
   * @author Keith M. Hughes
   */
  public class MyWebServerWebSocketHandler extends WebServerWebSocketHandlerSupport {

    /**
     * The ID for the connection.
     */
    private final String connectionId;

    /**
     * Construct a new web socket handler.
     *
     * @param connection
     *          the web socket connection
     */
    public MyWebServerWebSocketHandler(WebSocketConnection connection) {
      super(connection);

      this.connectionId = newConnectionId();
      handlers.put(connectionId, this);
    }

    @Override
    public void onReceive(Object data) {
      clientHandler.handleWebSocketReceive(connectionId, data);
    }

    @Override
    public void onConnect() {
      clientHandler.handleNewWebSocketConnection(connectionId);
    }

    @Override
    public void onClose() {
      handlers.remove(connectionId);

      clientHandler.handleWebSocketClose(connectionId);
    }
  }
}
