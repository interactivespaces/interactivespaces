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

import interactivespaces.service.comm.serial.xbee.TxRequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeAddress16;
import interactivespaces.service.comm.serial.xbee.XBeeAddress64;

/**
 * XBee frame for sending a transmit request to a remote radio.
 * 
 * <p>
 * This allows arbitrary data to be sent.
 * 
 * @author Keith M. Hughes
 */
public class TxRequestXBeeFrameImpl extends BaseRequestXBeeFrame implements TxRequestXBeeFrame {

	/**
	 * Construct an XBee Remote transmit request with fully specified address.
	 * 
	 * @param address64
	 *            the 64 bit destination address
	 * @param address16
	 *            the 16 bit destination address
	 * @param frameNumber
	 *            the frame number, if {@code 0} no response is sent
	 * @param broadcastRadius
	 *            the maximum number of hops to deliver the data if a broadcast
	 *            transmission, if {@code 0} will be the maximum hops value
	 * @param options
	 *            options for the frame
	 */
	public TxRequestXBeeFrameImpl(XBeeAddress64 address64,
			XBeeAddress16 address16, int frameNumber, int broadcastRadius,
			int options) {
		super(XBeeApiConstants.FRAME_TYPE_TX_REQUEST);

		add(frameNumber);

		address64.write(this);
		address16.write(this);

		add(broadcastRadius);
		add(options);
	}

	/**
	 * Construct an Remote transmit XBee request if the 16 bit address isn't
	 * known.
	 * 
	 * @param address64
	 *            the 64 bit destination address
	 * @param frameNumber
	 *            the frame number, if {@code 0} no response is sent
	 * @param broadcastRadius
	 *            the maximum number of hops to deliver the data if a broadcast
	 *            transmission, if {@code 0} will be the maximum hops value
	 * @param options
	 *            options for the frame
	 */
	public TxRequestXBeeFrameImpl(XBeeAddress64 address64, int frameNumber,
			int broadcastRadius, int options) {
		this(address64, XBeeAddress16Impl.BROADCAST_ADDRESS, frameNumber,
				broadcastRadius, options);
	}
}
