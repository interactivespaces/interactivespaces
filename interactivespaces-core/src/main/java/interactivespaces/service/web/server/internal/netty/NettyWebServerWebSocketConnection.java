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

package interactivespaces.service.web.server.internal.netty;

import interactivespaces.service.web.WebSocketConnection;
import interactivespaces.service.web.WebSocketHandler;
import interactivespaces.service.web.server.WebResourceAccessManager;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;
import interactivespaces.util.data.json.JsonMapper;

import org.apache.commons.logging.Log;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * A {@link WebSocketConnection} for a Netty web socket server.
 *
 * @author Keith M. Hughes
 */
public class NettyWebServerWebSocketConnection implements WebSocketConnection {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER;

  static {
    MAPPER = new JsonMapper();
  }

  /**
   * The channel for this web socket handler.
   */
  private Channel channel;

  /**
   * Handshaker for the web socket.
   */
  private WebSocketServerHandshaker handshaker;

  /**
   * Business logic handler for the web socket call.
   */
  private WebSocketHandler handler;

  /**
   * Logger for this handler.
   */
  private Log log;

  /**
   * The id for the user who initiated this socket connection.
   */
  private String user;

  /**
   * The access manager for web resources.
   */
  private WebResourceAccessManager accessManager;

  /**
   * All continuation data seen to date.
   */
  private StringBuilder continuationFrameData = new StringBuilder();

  /**
   * Construct a new connection.
   *
   * @param channel
   *          the channel to the client socket
   * @param user
   *          the user ID for the user using the socket
   * @param handshaker
   *          the websocket handshaker
   * @param handlerFactory
   *          the factory for creating web socket handlers
   * @param accessManager
   *          the access manager for web resources
   * @param log
   *          the logger for the connection
   */
  public NettyWebServerWebSocketConnection(Channel channel, String user, WebSocketServerHandshaker handshaker,
      WebServerWebSocketHandlerFactory handlerFactory, WebResourceAccessManager accessManager, Log log) {
    this.channel = channel;
    this.handshaker = handshaker;
    this.accessManager = accessManager;
    this.log = log;
    this.user = user;
    handler = handlerFactory.newWebSocketHandler(this);
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  /**
   * Handle a web socket request coming into the server.
   *
   * @param ctx
   *          the netty channel context
   * @param frame
   *          the web socket frame that has come in
   */
  public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    if (frame instanceof CloseWebSocketFrame) {
      handleCloseFrame(ctx, (CloseWebSocketFrame) frame);
    } else if (frame instanceof PingWebSocketFrame) {
      handlePingFrame(ctx, (PingWebSocketFrame) frame);
    } else if (frame instanceof TextWebSocketFrame) {
      handleTextFrameData((TextWebSocketFrame) frame);
    } else if (frame instanceof ContinuationWebSocketFrame) {
      handleContinuationFrameData((ContinuationWebSocketFrame) frame);
    } else {
      log.warn(String.format("Could not process web socket frame. %s frame types not supported", frame.getClass()
          .getName()));
    }
  }

  /**
   * Handle a close frame.
   *
   * @param ctx
   *          the channel handler context
   * @param frame
   *          the close frame
   */
  private void handleCloseFrame(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
    handshaker.close(ctx.getChannel(), frame);
  }

  /**
   * Handle a ping frame.
   *
   * @param ctx
   *          the channel handler context
   * @param frame
   *          the ping frame
   */
  private void handlePingFrame(ChannelHandlerContext ctx, PingWebSocketFrame frame) {
    ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
  }

  /**
   * Handle text frame data.
   *
   * @param frame
   *          the text frame
   */
  private void handleTextFrameData(TextWebSocketFrame frame) {
    String text = frame.getText();

    if (frame.isFinalFragment()) {
      if (isProcessingFragments()) {
        log.warn("Improper web socket communication, received a final text frame when in process"
            + " with continuation frames. Dropping continuation data.");
        continuationFrameData.setLength(0);
      }

      handleTextData(text);
    } else {
      // Text frames not labeled as final are the first frame received when there is a continuation frame.
      continuationFrameData.setLength(0);
      continuationFrameData.append(text);
    }
  }

  /**
   * Is the handler processing fragmented packets?
   *
   * @return {@code true} if processing fragments
   */
  private boolean isProcessingFragments() {
    return continuationFrameData.length() != 0;
  }

  /**
   * Handle continuation frame data.
   *
   * @param frame
   *          the continuation frame
   */
  private void handleContinuationFrameData(ContinuationWebSocketFrame frame) {
    // All data coming in from the first text frame that was marked non final.
    continuationFrameData.append(frame.getText());
    if (frame.isFinalFragment()) {
      handleTextData(continuationFrameData.toString());
      continuationFrameData.setLength(0);
    }
  }

  /**
   * Handle the complete text from a frame.
   *
   * <p>
   * This includes concatenated continuation frame data.
   *
   * @param textData
   *          the complete data of the message
   */
  private void handleTextData(String textData) {
    if (accessManager != null) {
      if (!accessManager.allowWebsocketCall(getUser(), textData)) {
        return;
      }
    }
    try {
      handler.onReceive(MAPPER.parseObject(textData));
    } catch (Exception e) {
      log.error("Could not process web socket frame", e);
    }
  }

  @Override
  public void writeDataAsJson(Object data) {
    try {
      channel.write(new TextWebSocketFrame(MAPPER.toString(data)));
    } catch (Exception e) {
      log.error("Could not write JSON object on web socket", e);
    }
  }

  @Override
  public void writeDataAsString(String data) {
    try {
      channel.write(new TextWebSocketFrame(data));
    } catch (Exception e) {
      log.error("Could not write string data on web socket", e);
    }
  }

  @Override
  public void shutdown() {
    // The netty server handler should signal that the channel has been
    // closed.
    channel.close();
  }

  @Override
  public String getUser() {
    return user;
  }

  /**
   * Get the handler this endpoint is using.
   *
   * @return the handler
   */
  public WebSocketHandler getHandler() {
    return handler;
  }
}
