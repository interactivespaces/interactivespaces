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
	 * XBee API frame type for sending a local AT command for immediate
	 * execution.
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
	 * XBee API frame type for a status for a TX command.
	 */
	public static final int FRAME_TYPE_TX_STATUS = 0x8b;

	/**
	 * XBee API frame type for receiving an RX packet.
	 */
	public static final int FRAME_TYPE_RX_RECEIVE = 0x90;

	/**
	 * XBee API frame type for receiving an RX I/O packet.
	 */
	public static final int FRAME_TYPE_RX_IO = 0x92;

	/**
	 * The AT command AC (commit all pending changes).
	 */
	public static final int[] AT_COMMAND_AC = new int[] { 0x41, 0x43 };

	/**
	 * The AT command AP (escape mode for radio).
	 */
	public static final int[] AT_COMMAND_AP = new int[] { 0x41, 0x50 };

	/**
	 * The AT command SH (higher order word of 64 bit address).
	 */
	public static final int[] AT_COMMAND_SH = new int[] { 0x53, 0x48 };

	/**
	 * The AT command SL (lower order word of 64 bit address).
	 */
	public static final int[] AT_COMMAND_SL = new int[] { 0x53, 0x4c };

	/**
	 * Disable ACK for remote AT commands.
	 */
	public static final int AT_REMOTE_COMMAND_OPTIONS_DISABLE_ACK = 0x01;

	/**
	 * Immediately apply changes for remote AT commands.
	 * 
	 * <p>
	 * If this is not set, an AC command is necessary to apply the stored
	 * changes.
	 */
	public static final int AT_REMOTE_COMMAND_OPTIONS_APPLY_CHANGE = 0x02;

	/**
	 * Use extended transmission timeout for remote AT commands.
	 */
	public static final int AT_REMOTE_COMMAND_OPTIONS_EXTENDED_TIMEOUT = 0x40;

	/**
	 * Command status value if the AT local or remote command was successful.
	 */
	public static final int AT_COMMAND_STATUS_SUCCESS = 0x00;

	/**
	 * Command status value if the AT local or remote command was an error.
	 */
	public static final int AT_COMMAND_STATUS_ERROR = 0x01;

	/**
	 * Command status value if the AT local or remote command was not a legal
	 * command.
	 */
	public static final int AT_COMMAND_STATUS_INVALID_COMMAND = 0x02;

	/**
	 * Command status value if the AT local or remote command contained an
	 * illegal parameter.
	 */
	public static final int AT_COMMAND_STATUS_INVALID_PARAMETER = 0x03;

	/**
	 * Command status value if the AT local or remote command has a transmission
	 * failure.
	 */
	public static final int AT_COMMAND_STATUS_TRANSMIT_FAILURE = 0x04;

	/**
	 * TX request option for disabling acknowlegment.
	 */
	public static final int TX_REQUEST_OPTION_DISABLE_ACK = 0x01;

	/**
	 * TX request option for enabling APS encryption, if EE=1.
	 */
	public static final int TX_REQUEST_OPTION_ENABLE_APS_ENCRYPTION = 0x20;

	/**
	 * TX request option for using an extended transmission timeout for this
	 * destination.
	 */
	public static final int TX_REQUEST_OPTION_EXTENDED_TRANSMISSION_TIMEOUT = 0x40;

	/**
	 * TX delivery status for delivery success.
	 */
	public static final int TX_RESPONSE_DELIVERY_STATUS_DELIVERY_SUCCESS = 0x00;

	/**
	 * TX delivery status for MAC ACK failure.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_MAC_ACK_FAILURE = 0x01;

	/**
	 * TX delivery status for CCA failure.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_CCA_FAILURE = 0x02;

	/**
	 * TX delivery status for invalid destination endpoint.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_INVALID_DESTINATION_ENDPOINT = 0x15;

	/**
	 * TX delivery status for network ACK failure.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_NETWORK_ACK_FAILURE = 0x21;

	/**
	 * TX delivery status for not joined to network..
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_NOT_JOINED_NETWORK = 0x22;

	/**
	 * TX delivery status for self addressed.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_SELF_ADDRESSED = 0x23;

	/**
	 * TX delivery status for address not found.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_ADDRESS_NOT_FOUND = 0x24;

	/**
	 * TX delivery status for route not found.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_ROUTE_NOT_FOUND = 0x25;

	/**
	 * TX delivery status for broadcast source failed to hear a neighbor relay
	 * the message.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_BROADCAST_SOURCE_FAIL_RELAY = 0x26;

	/**
	 * TX delivery status for invalid binding table index.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_INVALID_BINDING_INDEX = 0x2b;

	/**
	 * TX delivery status for resource error, such as lack of buffers, timers,
	 * etc.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_RESOURCE_ERROR_1 = 0x2c;

	/**
	 * TX delivery status for attempted broadcast with APS transmission.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_BROADCAST_APS = 0x2d;

	/**
	 * TX delivery status for attempted unicast with APS transmission, but EE=0.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_UNICAST_APS = 0x2e;

	/**
	 * TX delivery status for resource error, such as lack of buffers, timers,
	 * etc.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_RESOURCE_ERROR_2 = 0x32;

	/**
	 * TX delivery status for data payload too large.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_PAYLOAD_TOO_LARGE = 0x74;

	/**
	 * TX delivery status for indirect message unrequested.
	 */
	public static final int TX_STATUS_DELIVERY_STATUS_INDIRECT_UNREQUESTED = 0x75;

	/**
	 * TX discovery status for no overhead.
	 */
	public static final int TX_STATUS_DISCOVERY_STATUS_NO_OVERHEAD = 0x00;

	/**
	 * TX discovery status for address discovery.
	 */
	public static final int TX_STATUS_DISCOVERY_STATUS_ADDRESS_DISCOVERY = 0x01;

	/**
	 * TX discovery status for route discovery.
	 */
	public static final int TX_STATUS_DISCOVERY_STATUS_ROUTE_DISCOVERY = 0x02;

	/**
	 * TX discovery status for address and route discovery.
	 */
	public static final int TX_STATUS_DISCOVERY_STATUS_ADDRESS_ROUTE_DISCOVERY = 0x03;

	/**
	 * TX discovery status for extended timeout discovery.
	 */
	public static final int TX_STATUS_DISCOVERY_STATUS_EXTENDED_TIMEOUT = 0x04;

	/**
	 * RX receive option for packet acknowledged.
	 */
	public static final int RX_RECEIVE_OPTION_PACKET_ACK = 0x01;

	/**
	 * RX receive option for packet was a broadcast packet.
	 */
	public static final int RX_RECEIVE_OPTION_PACKET_BORADCAST = 0x02;

	/**
	 * RX receive option for packet was encrypted with APS.
	 */
	public static final int RX_RECEIVE_OPTION_ENCRYPTED_APS = 0x20;

	/**
	 * RX receive option for packet was sent from an end device (if known).
	 */
	public static final int RX_RECEIVE_OPTION_END_DEVICE = 0x40;

	/**
	 * The escape byte for the XBee frame.
	 */
	public static final int ESCAPE_BYTE = 0x7d;

	/**
	 * The value used for escaping and unescaping escaped bytes.
	 */
	public static final int ESCAPE_BYTE_VALUE = 0x20;

	/**
	 * Is the given value to be escaped?
	 * 
	 * @param value
	 *            the value being checked
	 * 
	 * @return {@code true} if the value should be escaped
	 */
	public static boolean isEscaped(int value) {
		// These are the only bytes which must be escaped.
		return value == FRAME_START_BYTE || value == ESCAPE_BYTE
				|| value == 0x11 || value == 0x13;
	}
}
