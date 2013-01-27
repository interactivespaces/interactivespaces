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

import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.AtRemoteResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.TxStatusXBeeFrame;

import org.apache.commons.logging.Log;

/**
 * A parser for XBee response frames.
 * 
 * @author Keith M. Hughes
 */
public interface XBeeResponseFrameParser {

	/**
	 * Parse an AT Local response.
	 * 
	 * @param reader
	 *            the frame reader
	 * @param bytesLeft
	 *            the number of bytes left in the packet
	 * @param log
	 *            the logger for issues
	 * 
	 * @return the response
	 */
	AtLocalResponseXBeeFrame parseAtLocalResponse(
			EscapedXBeeFrameReader reader, int bytesLeft, Log log);

	/**
	 * Parse an AT Remote response.
	 * 
	 * @param reader
	 *            the frame reader
	 * @param bytesLeft
	 *            the number of bytes left in the packet
	 * @param log
	 *            the logger for issues
	 */
	AtRemoteResponseXBeeFrame parseAtRemoteResponse(
			EscapedXBeeFrameReader reader, int bytesLeft, Log log);

	/**
	 * Parse an Transmit response.
	 * 
	 * @param reader
	 *            the frame reader
	 * @param bytesLeft
	 *            the number of bytes left in the packet
	 * @param log
	 *            the logger for issues
	 * 
	 * @return the response frame
	 */
	TxStatusXBeeFrame parseTxStatus(EscapedXBeeFrameReader reader,
			int bytesLeft, Log log);

	/**
	 * Parse an Receive response.
	 * 
	 * @param reader
	 *            the frame reader
	 * @param bytesLeft
	 *            the number of bytes left in the packet
	 * @param log
	 *            the logger for issues
	 */
	RxResponseXBeeFrame parseReceiveResponse(EscapedXBeeFrameReader reader,
			int bytesLeft, Log log);
}