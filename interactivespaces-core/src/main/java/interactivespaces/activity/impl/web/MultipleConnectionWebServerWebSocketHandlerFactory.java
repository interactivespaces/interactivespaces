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

import interactivespaces.service.web.WebSocketConnection;
import interactivespaces.service.web.server.WebServerWebSocketHandler;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;
import interactivespaces.service.web.server.WebServerWebSocketHandlerSupport;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * A {@link WebServerWebSocketHandlerFactory} which supports multiple simultaneous web
 * socket connections.
 * 
 * @author Keith M. Hughes
 */
public class MultipleConnectionWebServerWebSocketHandlerFactory implements
		WebServerWebSocketHandlerFactory {

	/**
	 * The client handler.
	 */
	private MultipleConnectionWebSocketHandler clientHandler;

	/**
	 * A map from connect IDs to handlers.
	 */
	private Map<String, MyWebServerWebSocketHandler> handlers = Maps.newConcurrentMap();

	/**
	 * Creator of connection IDs.
	 */
	private AtomicLong connectionIdFactory = new AtomicLong(
			System.currentTimeMillis());

	/**
	 * Log
	 */
	private Log log;

	public MultipleConnectionWebServerWebSocketHandlerFactory(
			MultipleConnectionWebSocketHandler clientHandler, Log log) {
		this.clientHandler = clientHandler;
		this.log = log;
	}

	@Override
	public WebServerWebSocketHandler newWebSocketHandler(WebSocketConnection connection) {
		return new MyWebServerWebSocketHandler(connection);
	}

	/**
	 * Are there any web sockets connected?
	 * 
	 * @return {@code true} if there are any connections
	 */
	public boolean areWebSocketsConnected() {
		return !handlers.isEmpty();
	}

	/**
	 * Send a JSON result to the web socket
	 * 
	 * @param connectionId
	 *            the ID of the connection
	 * @param data
	 *            the data to send
	 */
	public void sendJson(String connectionId, Object data) {
		MyWebServerWebSocketHandler handler = handlers.get(connectionId);
		if (handler != null) {
			handler.sendJson(data);
		} else {
			log.error(String.format("Unknown web socket connection ID %s",
					connectionId));
		}
	}

	/**
	 * Send a JSON result to all web socket connections
	 * 
	 * @param data
	 *            the data to send
	 */
	public void sendJson(Object data) {
		for (MyWebServerWebSocketHandler handler : handlers.values()) {
			handler.sendJson(data);
		}
	}

	/**
	 * Send a string to the web socket.
	 * 
	 * @param connectionId
	 *            the ID of the connection
	 * @param data
	 *            the data to send
	 */
	public void sendString(String connectionId, String data) {
		MyWebServerWebSocketHandler handler = handlers.get(connectionId);
		if (handler != null) {
			handler.sendString(data);
		} else {
			log.error(String.format("Unknown web socket connection ID %s",
					connectionId));
		}
	}

	/**
	 * Send a string to all web socket connections
	 * 
	 * @param data
	 *            the data to send
	 */
	public void sendString(String data) {
		for (MyWebServerWebSocketHandler handler : handlers.values()) {
			handler.sendString(data);
		}
	}

	/**
	 * Create a new connetion ID.
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
		private String connectionId;

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

	/**
	 * An interface for handling events to the web socket handlers.
	 * 
	 * @author Keith M. Hughes
	 */
	public interface MultipleConnectionWebSocketHandler {

		/**
		 * A New web socket connection has come in.
		 * 
		 * @param connectionId
		 *            the ID of the connection
		 */
		void handleNewWebSocketConnection(String connectionId);

		/**
		 * A web socket connection is closing.
		 * 
		 * @param connectionId
		 *            the ID of the connection
		 */
		void handleWebSocketClose(String connectionId);

		/**
		 * Data has been sent to the web socket connection.
		 * 
		 * @param connectionId
		 *            the ID of the connection that received the data
		 */
		void handleWebSocketReceive(String connectionId, Object data);
	}
}
