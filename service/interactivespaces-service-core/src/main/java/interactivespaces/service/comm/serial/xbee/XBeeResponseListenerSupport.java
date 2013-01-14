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

import interactivespaces.service.comm.serial.xbee.internal.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.internal.AtRemoteResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.internal.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.internal.TxStatusXBeeFrame;

/**
 * An {@link XBeeResponseListener} with do nothing methods for all listeners.
 * 
 * <p>
 * This class is meant to be subclassed and the methods you care about
 * overridden.
 * 
 * @author Keith M. Hughes
 */
public class XBeeResponseListenerSupport implements XBeeResponseListener {

	@Override
	public void onAtLocalXBeeResponse(XBeeCommunicationEndpoint endpoint,
			AtLocalResponseXBeeFrame response) {
		// Default is do nothing
	}

	@Override
	public void onAtRemoteXBeeResponse(XBeeCommunicationEndpoint endpoint,
			AtRemoteResponseXBeeFrame response) {
		// Default is do nothing
	}

	@Override
	public void onTxStatusXBeeResponse(XBeeCommunicationEndpoint endpoint,
			TxStatusXBeeFrame response) {
		// Default is do nothing
	}

	@Override
	public void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint,
			RxResponseXBeeFrame response) {
		// Default is do nothing
	}
}
