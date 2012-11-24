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
import interactivespaces.service.web.WebSocketHandlerSupport;

/**
 * A support class which implements do nothing default methods for {@link WebServerWebSocketHandler}
 * classes.
 *
 * @author Keith M. Hughes
 */
public class WebServerWebSocketHandlerSupport extends WebSocketHandlerSupport implements WebServerWebSocketHandler {

	/**
	 * The connection to the remote endpoint.
	 */
	protected WebSocketConnection connection;

	public WebServerWebSocketHandlerSupport(WebSocketConnection connection) {
		this.connection = connection;
	}

	@Override
	public void sendJson(Object data) {
		connection.writeDataAsJson(data);
	}

	@Override
	public void sendString(String data) {
		connection.writeDataAsString(data);
	}
}
