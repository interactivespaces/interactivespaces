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

	public NettyWebServerWebSocketConnection(Channel channel, String user,
			WebSocketServerHandshaker handshaker,
			WebServerWebSocketHandlerFactory handlerFactory, Log log) {
		this.channel = channel;
		this.handshaker = handshaker;
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
	 *            the netty channel context
	 * @param frame
	 *            the web socket frame that has come in
	 */
	public void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame, WebResourceAccessManager accessManager) {
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
			return;
		} else if (frame instanceof PingWebSocketFrame) {
			ctx.getChannel().write(
					new PongWebSocketFrame(frame.getBinaryData()));
			return;
		} else if (!(frame instanceof TextWebSocketFrame)) {
			log.warn(String
					.format("Could not process web socket frame. %s frame types not supported",
							frame.getClass().getName()));
			return;
		}

		String textData = ((TextWebSocketFrame) frame).getText();
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
