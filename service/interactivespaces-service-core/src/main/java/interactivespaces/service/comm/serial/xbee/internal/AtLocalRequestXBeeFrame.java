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
 * XBee frame for sending an AT request to the local radio.
 * 
 * @author Keith M. Hughes
 */
public class AtLocalRequestXBeeFrame extends RequestXBeeFrame {

	/**
	 * Construct an AT Local command XBee
	 * @param command
	 *            the AT command to send
	 * @param frameNumber
	 *            the frame number
	 */
	public AtLocalRequestXBeeFrame(int[] command, int frameNumber) {
		super(XBeeApiConstants.FRAME_TYPE_AT_LOCAL_SEND_IMMEDIATE);

		add(frameNumber);

		add(command);
	}
}
