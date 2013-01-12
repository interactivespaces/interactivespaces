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
 * XBee frame for sending an AT request to a remote radio.
 * 
 * @author Keith M. Hughes
 */
public class AtRemoteRequestXBeeFrame extends RequestXBeeFrame {

	/**
	 * Construct an AT Remote command XBee with fully specified address.
	 * 
	 * @param address64
	 *            the 64 bit destination address
	 * @param address16
	 *            the 16 bit destination address
	 * @param command
	 *            the AT command to send
	 * @param frameNumber
	 *            the frame number, if {@code 0} no response is sent
	 * @param options
	 *            options for the frame
	 */
	public AtRemoteRequestXBeeFrame(XBeeAddress64 address64,
			XBeeAddress16 address16, int[] command, int frameNumber, int options) {
		super(XBeeApiConstants.FRAME_TYPE_AT_REMOTE_SEND);

		add(frameNumber);

		address64.write(this);
		address16.write(this);

		add(options);

		add(command);
	}

	/**
	 * Construct an AT Remote command XBee if the 16 bit address isn't known.
	 * 
	 * @param address64
	 *            the 64 bit destination address
	 * @param command
	 *            the AT command to send
	 * @param frameNumber
	 *            the frame number, if {@code 0} no response is sent
	 * @param options
	 *            options for the frame
	 */
	public AtRemoteRequestXBeeFrame(XBeeAddress64 address64, int[] command,
			int frameNumber, int options) {
		this(address64, XBeeAddress16.BROADCAST_ADDRESS, command, frameNumber,
				options);
	}
}
