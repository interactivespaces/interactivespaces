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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The base for all XBee Frame Writers.
 * 
 * @author Keith M. Hughes
 */
public class RequestXBeeFrame {

	/**
	 * Where bytes are written for the frame
	 */
	private ByteArrayOutputStream bos = new ByteArrayOutputStream();

	/**
	 * The checksum for the frame.
	 */
	private int checksum = 0;
	
	/**
	 * The total number of bytes in the frame counted in the length field of the frame.
	 */
	private int length = 0;

	/**
	 * Construct an XBee Write Frame
	 * 
	 * @param frameType
	 *            the type of frame
	 */
	public RequestXBeeFrame(int frameType) {
		bos.write(XBeeApiConstants.FRAME_START_BYTE);

		// Save room for length
		bos.write(0x00);
		bos.write(0x00);

		add(frameType);
	}

	/**
	 * Add a new byte to the frame.
	 * 
	 * @param b
	 *            the byte to add
	 */
	public void add(int b) {
		bos.write(b);
		checksum += b;
		length++;
	}

	/**
	 * Add a new 16-bit integer to the frame.
	 * 
	 * @param i
	 *            the integer to add
	 */
	public void add16(int i) {
		int b1 = (i >> 8) & 0xff;
		int b2 = i & 0xff;

		bos.write(b1);
		bos.write(b2);
		checksum += b1 + b2;
		length += 2;
	}

	/**
	 * Add an array of bytes to the frame.
	 * 
	 * @param ba
	 *            the bytes to add
	 */
	public void add(byte[] ba) {
		try {
			bos.write(ba);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Error while writing bytes into XBee frame", e);
		}

		for (byte b : ba) {
			checksum += ((int) b & 0xff);
		}
		length += ba.length;
	}

	/**
	 * Add an array of bytes to the frame.
	 * 
	 * <p>
	 * The bottom 8 bits of each integer is added.
	 * 
	 * @param ia
	 *            the bytes to add
	 */
	public void add(int[] ia) {
		for (int i : ia) {
			bos.write(i);
			checksum += i & 0xff;
		}
		length += ia.length;
	}

	/**
	 * Write the frame to the XBee.
	 * 
	 * @param commEndpoint
	 *            the communication endpoint for the XBee
	 */
	public void write(SerialCommunicationEndpoint commEndpoint) {
		checksum &= 0xff;
		checksum = 0xff - checksum;
		bos.write(checksum);

		byte[] bytes = bos.toByteArray();

		// Get proper length into the frame.
		bytes[1] = (byte) ((length >> 8) & 0xff);
		bytes[2] = (byte) (length & 0xff);

		commEndpoint.write(bytes);
		commEndpoint.flush();
	}
}
