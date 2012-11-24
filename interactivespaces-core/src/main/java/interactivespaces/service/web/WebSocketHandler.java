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

package interactivespaces.service.web;

/**
 * Handle web socket requests.
 *
 * @author Keith M. Hughes
 */
public interface WebSocketHandler {

	/**
	 * The connection has just been made to the remote endpoint.
	 */
	void onConnect();
	
	/**
	 * The connection to the remote endpoint has been closed.
	 */
	void onClose();
	
	/**
	 * Data has been received from the remote endpoint. Process it.
	 * 
	 * @param data The data to process.
	 */
	void onReceive(Object data);
}
