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

import interactivespaces.activity.component.web.WebServerActivityComponent;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory.MultipleConnectionWebSocketHandler;
import interactivespaces.service.web.server.HttpFileUpload;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.util.data.json.JsonBuilder;
import interactivespaces.util.data.json.JsonMapper;

import java.io.File;
import java.util.Map;

/**
 * A web server activity which allows for ROS routes.
 *
 * @author Keith M. Hughes
 */
public class BaseRoutableRosWebServerActivity extends BaseRoutableRosActivity implements
    MultipleConnectionWebSocketHandler, HttpFileUploadListener {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER;

  static {
    MAPPER = new JsonMapper();
  }

  /**
   * Web socket handler for the connection to the browser.
   */
  private MultipleConnectionWebServerWebSocketHandlerFactory webSocketFactory;

  /**
   * The web server component.
   */
  private WebServerActivityComponent webServerComponent;

  @Override
  public void commonActivitySetup() {
    super.commonActivitySetup();

    webSocketFactory = new MultipleConnectionWebServerWebSocketHandlerFactory(this, getLog());

    webServerComponent = addActivityComponent(new WebServerActivityComponent());
    webServerComponent.setWebSocketHandlerFactory(webSocketFactory);
    webServerComponent.setHttpFileUploadListener(this);
  }

  /**
   * Convert a map to a JSON string.
   *
   * @param map
   *          the map to convert
   */
  public String jsonStringify(Map<String, Object> map) {
    return MAPPER.toString(map);
  }

  /**
   * Parse a JSON string and return the map.
   *
   * @param data
   *          the JSON string
   *
   * @return the map for the string
   */
  public Map<String, Object> jsonParse(String data) {
    return MAPPER.parseObject(data);
  }

  /**
   * Add static content for the web server to serve.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param baseDir
   *          the base directory where the content will be found
   */
  public void addStaticContent(String uriPrefix, File baseDir) {
    webServerComponent.addStaticContent(uriPrefix, baseDir);
  }

  /**
   * Is the web socket connected to anything?
   *
   * @return {@code true} if the web socket is connected.
   */
  public boolean isWebSocketConnected() {
    return webSocketFactory.areWebSocketsConnected();
  }

  /**
   * A new web socket connection has been made.
   *
   * @param connectionId
   *          ID for the web socket connection
   */
  public void onNewWebSocketConnection(String connectionId) {
    // Default is nothing to do
  }

  /**
   * Web socket closed.
   *
   * @param connectionId
   *          ID for the web socket connection
   */
  public void onWebSocketClose(String connectionId) {
    // Default is nothing to do.
  }

  /**
   * Received a web socket call.
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          The data from the web socket call.
   */
  public void onWebSocketReceive(String connectionId, Object data) {
    // Default is to do nothing.
  }

  /**
   * Send a JSON result to the web socket
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          the data to send
   */
  public void sendWebSocketJson(String connectionId, Object data) {
    webSocketFactory.sendJson(connectionId, data);
  }

  /**
   * Send a {@link JsonBuilder} result to the web socket
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          the data to send
   */
  public void sendWebSocketJsonBuilder(String connectionId, JsonBuilder data) {
    sendWebSocketJson(connectionId, data.build());
  }

  /**
   * Send a JSON result to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  public void sendAllWebSocketJson(Object data) {
    webSocketFactory.sendJson(data);
  }

  /**
   * Send a {@link JsonBuilder} result to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  public void sendAllWebSocketJsonBuilder(JsonBuilder data) {
    sendAllWebSocketJson(data.build());
  }

  /**
   * Send a string to the web socket
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          the data to send
   */
  public void sendWebSocketString(String connectionId, String data) {
    webSocketFactory.sendString(connectionId, data);
  }

  /**
   * Send a string to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  public void sendAllWebSocketString(String data) {
    webSocketFactory.sendString(data);
  }

  @Override
  public void handleNewWebSocketConnection(String connectionId) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onNewWebSocketConnection(connectionId);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void handleWebSocketClose(String connectionId) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onWebSocketClose(connectionId);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void handleWebSocketReceive(String connectionId, Object data) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onWebSocketReceive(connectionId, data);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void handleHttpFileUpload(HttpFileUpload fileUpload) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onHttpFileUpload(fileUpload);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * A file upload has happened.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param fileUpload
   *          the file upload
   */
  public void onHttpFileUpload(HttpFileUpload fileUpload) {
    // The default is do nothing.
  }

  /**
   * Get the web server for the activity.
   *
   * @return the web server
   */
  public WebServer getWebServer() {
    return webServerComponent.getWebServer();
  }
}
