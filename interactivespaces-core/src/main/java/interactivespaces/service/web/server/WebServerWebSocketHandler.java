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

import interactivespaces.service.web.WebSocketHandler;

/**
 * A {@link WebSocketHandler} which can also send data
 *
 * @author Keith M. Hughes
 */
public interface WebServerWebSocketHandler extends WebSocketHandler {
	
	/**
	 * Send data to the remote endpoint.
	 * 
	 * @param data The data to send.
	 */
	void sendJson(Object data);
	
	/**
	 * Send raw data to the remote endpoint.
	 * 
	 * @param data The data to send.
	 */
	void sendString(String data);
}
