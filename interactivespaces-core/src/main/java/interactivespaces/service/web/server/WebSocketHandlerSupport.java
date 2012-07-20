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


/**
 * A useful support class for {@link WebSocketHandler} instances
 *
 * @author Keith M. Hughes
 */
public class WebSocketHandlerSupport implements WebSocketHandler {
	/**
	 * The connection to the remote endpoint.
	 */
	protected WebSocketConnection connection;

	public WebSocketHandlerSupport(WebSocketConnection connection) {
		this.connection = connection;
	}

	@Override
	public void onConnect() {
		// Default is nothing.
	}

	@Override
	public void onClose() {
		// Default is nothing.
		
	}

	@Override
	public void onReceive(Object data) {
		// Default is nothing.
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
