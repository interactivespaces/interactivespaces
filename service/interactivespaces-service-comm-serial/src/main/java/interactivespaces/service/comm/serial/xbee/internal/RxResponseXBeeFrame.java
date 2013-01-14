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
 * A response frame for an XBee RX.
 * 
 * @author Keith M. Hughes
 */
public class RxResponseXBeeFrame {

	/**
	 * The 64 bit address for the remote radio
	 */
	private XBeeAddress64 address64;

	/**
	 * The 16 bit address for the remote radio
	 */
	private XBeeAddress16 address16;

	/**
	 * The receive options.
	 */
	private int receiveOptions;

	/**
	 * The data received from the remote radio.
	 */
	private byte[] receivedData;

	public RxResponseXBeeFrame(XBeeAddress64 address64,
			XBeeAddress16 address16, int receiveOptions, byte[] receivedData) {
		this.address64 = address64;
		this.address16 = address16;
		this.receiveOptions = receiveOptions;
		this.receivedData = receivedData;
	}

	/**
	 * Get the 64 bit address of the sender of the RX frame.
	 * 
	 * @return the sender's 64 bit address
	 */
	public XBeeAddress64 getAddress64() {
		return address64;
	}

	/**
	 * Get the 16 bit address of the sender of the RX frame.
	 * 
	 * @return the sender's 16 bit address
	 */
	public XBeeAddress16 getAddress16() {
		return address16;
	}

	/**
	 * @return the receiveOptions
	 */
	public int getReceiveOptions() {
		return receiveOptions;
	}

	/**
	 * @return the receivedData
	 */
	public byte[] getReceivedData() {
		return receivedData;
	}

	@Override
	public String toString() {
		return "RxResponseXBeeFrame [address64=" + address64 + ", address16="
				+ address16 + ", receiveOptions=" + receiveOptions + "]";
	}
}
