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

/**
 * A response frame for an XBee TX request.
 * 
 * @author Keith M. Hughes
 */
public interface TxResponseXBeeFrame {

	/**
	 * @return the frameId
	 */
	int getFrameId();

	/**
	 * @return the address16
	 */
	XBeeAddress16 getAddress16();

	/**
	 * @return the transmitRetryCount
	 */
	int getTransmitRetryCount();

	/**
	 * @return the deliveryStatus
	 */
	int getDeliveryStatus();

	/**
	 * @return the discoveryStatus
	 */
	int getDiscoveryStatus();

}