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

package interactivespaces.service.comm.serial.xbee.internal;

/**
 * A response frame for an XBee TX request.
 * 
 * @author Keith M. Hughes
 */
public class TxStatusXBeeFrame {

	/**
	 * ID of the frame for the TX request that gave this response
	 */
	private int frameId;

	/**
	 * The 16 bit address for the remote radio which received the TX packet.
	 */
	private XBeeAddress16 address16;

	/**
	 * The number of application transmission retries that took place.
	 */
	private int transmitRetryCount;

	/**
	 * The delivery status of the TX packet.
	 */
	private int deliveryStatus;
	
	/**
	 * The status for the discovery of the destination radio.
	 */
	private int discoveryStatus;

	public TxStatusXBeeFrame(int frameId, XBeeAddress16 address16,
			int transmitRetryCount, int deliveryStatus, int discoveryStatus) {
		this.frameId = frameId;
		this.address16 = address16;
		this.transmitRetryCount = transmitRetryCount;
		this.deliveryStatus = deliveryStatus;
		this.discoveryStatus = discoveryStatus;
	}

	/**
	 * @return the frameId
	 */
	public int getFrameId() {
		return frameId;
	}

	/**
	 * @return the address16
	 */
	public XBeeAddress16 getAddress16() {
		return address16;
	}

	/**
	 * @return the transmitRetryCount
	 */
	public int getTransmitRetryCount() {
		return transmitRetryCount;
	}

	/**
	 * @return the deliveryStatus
	 */
	public int getDeliveryStatus() {
		return deliveryStatus;
	}

	/**
	 * @return the discoveryStatus
	 */
	public int getDiscoveryStatus() {
		return discoveryStatus;
	}

	@Override
	public String toString() {
		return "TxStatusXBeeFrame [frameId=" + frameId + ", address16="
				+ address16 + ", transmitRetryCount=" + transmitRetryCount
				+ ", deliveryStatus=" + deliveryStatus + ", discoveryStatus="
				+ discoveryStatus + "]";
	}
}
