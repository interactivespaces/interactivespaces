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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.component.web.WebBrowserActivityComponent;
import interactivespaces.activity.component.web.WebServerActivityComponent;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory.MultipleConnectionWebSocketHandler;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * An {@link Activity} which starts up a web server with websocket handler and a
 * web browser. The activity can then easily respond to web socket messages.
 * 
 * <p>
 * Only 1 web socket client is currently supported.
 * 
 * <p>
 * This activity is easily scriptable.
 * 
 * @author Keith M. Hughes
 */
public class BaseWebActivity extends BaseWebServerActivity implements
		MultipleConnectionWebSocketHandler {

	/**
	 * Web socket handler for the connection to the browser.
	 */
	private MultipleConnectionWebServerWebSocketHandlerFactory webSocketFactory;

	@Override
	public void commonActivitySetup() {
		addActivityComponent(
				new WebServerActivityComponent());

		addActivityComponent(new WebBrowserActivityComponent());
		
		WebServerActivityComponent webServer = getComponent(WebServerActivityComponent.COMPONENT_NAME);
		webSocketFactory = new MultipleConnectionWebServerWebSocketHandlerFactory(this,
				getLog());
		webServer.setWebSocketHandlerFactory(webSocketFactory);
	}

	/**
	 * Convert a map to a JSON string.
	 * 
	 * @param chaName
	 *            the name of the output channel to send the message on
	 * @param message
	 *            the message to send
	 */
	public String jsonStringify(Map<String, Object> map) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.writeValueAsString(map);
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not serialize JSON object as string", e);
		}
	}

	/**
	 * Parse a JSON string and return the map.
	 * 
	 * @param data
	 *            the JSON string
	 * @return the map for the string
	 */
	public Map<String, Object> jsonParse(String data) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) mapper.readValue(
					data, Map.class);
			return map;
		} catch (Exception e) {
			throw new InteractiveSpacesException("Could not parse JSON string",
					e);
		}
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
	 *            ID for the web socket connection
	 */
	public void onNewWebSocketConnection(String connectionId) {
		// Default is nothing to do
	}

	/**
	 * Web socket closed.
	 * 
	 * @param connectionId
	 *            ID for the web socket connection
	 */
	public void onWebSocketClose(String connectionId) {
		// Default is nothing to do.
	}

	/**
	 * Received a web socket call.
	 * 
	 * @param connectionId
	 *            ID for the web socket connection
	 * @param data
	 *            The data from the web socket call.
	 */
	public void onWebSocketReceive(String connectionId, Object data) {
		// Default is to do nothing.
	}

	/**
	 * Send a JSON result to the web socket
	 * 
	 * @param connectionId
	 *            ID for the web socket connection
	 * @param data
	 *            the data to send
	 */
	public void sendWebSocketJson(String connectionId, Object data) {
		webSocketFactory.sendJson(connectionId, data);
	}

	/**
	 * Send a JSON result to all web socket connections.
	 * 
	 * @param data
	 *            the data to send
	 */
	public void sendAllWebSocketJson(Object data) {
		webSocketFactory.sendJson(data);
	}

	/**
	 * Send a string to the web socket
	 * 
	 * @param connectionId
	 *            ID for the web socket connection
	 * @param data
	 *            the data to send
	 */
	public void sendWebSocketString(String connectionId, String data) {
		webSocketFactory.sendString(connectionId, data);
	}

	/**
	 * Send a string to all web socket connections.
	 * 
	 * @param data
	 *            the data to send
	 */
	public void sendAllWebSocketString(String data) {
		webSocketFactory.sendString(data);
	}
	
	@Override
	public void handleNewWebSocketConnection(String connectionId) {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onNewWebSocketConnection(connectionId);
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	@Override
	public void handleWebSocketReceive(String connectionId, Object data) {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onWebSocketReceive(connectionId, data);
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	@Override
	public void handleWebSocketClose(String connectionId) {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onWebSocketClose(connectionId);
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}
}
