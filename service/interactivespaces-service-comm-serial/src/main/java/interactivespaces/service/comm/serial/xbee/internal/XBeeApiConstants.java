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
 * Constants for working with the XBee API
 *
 * @author Keith M. Hughes
 */
public class XBeeApiConstants {

	/**
	 * The value of the start byte for an XBee API frame.
	 */
	public static final int FRAME_START_BYTE = 0x7e;

	/**
	 * XBee API frame type for sending a local AT command for immediate execution.
	 */
	public static final int FRAME_TYPE_AT_LOCAL_SEND_IMMEDIATE = 0x08;

	/**
	 * XBee API frame type for sending a local AT command for queued execution.
	 */
	public static final int FRAME_TYPE_AT_LOCAL_SEND_QUEUED = 0x09;

	/**
	 * XBee API frame type for a response for a local AT command.
	 */
	public static final int FRAME_TYPE_AT_LOCAL_RESPONSE = 0x88;

	/**
	 * XBee API frame type for sending a remote AT command.
	 */
	public static final int FRAME_TYPE_AT_REMOTE_SEND = 0x17;

	/**
	 * XBee API frame type for a response for a remote AT command.
	 */
	public static final int FRAME_TYPE_AT_REMOTE_RESPONSE = 0x97;

	/**
	 * XBee API frame type for sending a TX command.
	 */
	public static final int FRAME_TYPE_TX_REQUEST = 0x10;

	/**
	 * XBee API frame type for a response for a TX command.
	 */
	public static final int FRAME_TYPE_TX_RESPONSE = 0x8b;

	/**
	 * XBee API frame type for receiving an RX packet.
	 */
	public static final int FRAME_TYPE_RX_RECEIVE = 0x90;

	/**
	 * XBee API frame type for receiving an RX I/O packet.
	 */
	public static final int FRAME_TYPE_RX_IO = 0x92;
	
	/**
	 * The AT command SL (lower order word of address).
	 */
	public static final int[] AT_COMMAND_SL = new int[] { 0x53, 0x4c };
}
