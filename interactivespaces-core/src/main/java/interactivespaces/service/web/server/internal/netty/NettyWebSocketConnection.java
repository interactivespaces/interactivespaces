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

import interactivespaces.service.web.server.WebSocketConnection;
import interactivespaces.service.web.server.WebSocketHandler;
import interactivespaces.service.web.server.WebSocketHandlerFactory;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * A {@link WebSocketConnection} for Netty.
 * 
 * @author Keith M. Hughes
 */
public class NettyWebSocketConnection implements WebSocketConnection {

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

	public NettyWebSocketConnection(Channel channel,
			WebSocketServerHandshaker handshaker,
			WebSocketHandlerFactory handlerFactory, Log log) {
		this.channel = channel;
		this.handshaker = handshaker;
		this.log = log;
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
			WebSocketFrame frame) {
		// Send the Squared integer back.
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
			return;
		} else if (frame instanceof PingWebSocketFrame) {
			ctx.getChannel().write(
					new PongWebSocketFrame(frame.getBinaryData()));
			return;
		} else if (!(frame instanceof TextWebSocketFrame)) {
			String message = String
					.format("Could not process web socket frame. %s frame types not supported",
							frame.getClass().getName());
			log.error(message);
			throw new UnsupportedOperationException(message);
		}

		String textData = ((TextWebSocketFrame) frame).getText();
		ObjectMapper mapper = new ObjectMapper();
		try {
			handler.onReceive(mapper.readValue(textData, Map.class));
		} catch (Exception e) {
			log.error("Could not process web socket frame", e);
		}
	}

	@Override
	public void writeDataAsJson(Object data) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			channel.write(new TextWebSocketFrame(mapper
					.writeValueAsString(data)));
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
	public void close() {
		// The netty server handler should signal that the channel has been
		// closed.
		channel.close();
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
