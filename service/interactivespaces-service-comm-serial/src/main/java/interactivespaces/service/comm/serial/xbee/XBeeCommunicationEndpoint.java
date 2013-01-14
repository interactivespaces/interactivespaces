/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.comm.serial.xbee;

import interactivespaces.comm.CommunicationEndpoint;

/**
 * An XBee communication endpoint.
 * 
 * <p>
 * This communication endpoint assumes the XBee is a Series 2 radio in escaped
 * API mode (AP=2).
 * 
 * @author Keith M. Hughes
 */
public interface XBeeCommunicationEndpoint extends CommunicationEndpoint {

	/**
	 * Add a listener to the endpoint.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	void addListener(XBeeResponseListener listener);

	/**
	 * Remove a listener from the endpoint.
	 * 
	 * <p>
	 * Does nothing if the listener was never added
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	void removeListener(XBeeResponseListener listener);
}
