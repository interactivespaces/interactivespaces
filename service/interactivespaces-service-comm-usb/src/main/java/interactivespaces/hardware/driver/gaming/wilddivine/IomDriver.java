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

package interactivespaces.hardware.driver.gaming.wilddivine;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.hardware.driver.DriverSupport;
import interactivespaces.service.comm.usb.UsbCommunicationEndpoint;
import interactivespaces.service.comm.usb.UsbCommunicationEndpointService;
import interactivespaces.service.comm.usb.internal.libusb4j.Usb4JavaUsbCommunicationEndpointService;
import interactivespaces.util.ByteUtils;

/**
 * A driver for the Wild Divine Iom biofeedback sensor.
 * 
 * @author Keith M. Hughes
 */
public class IomDriver extends DriverSupport {

	public static void main(String[] args) {
		Usb4JavaUsbCommunicationEndpointService service = new Usb4JavaUsbCommunicationEndpointService();
		service.startup();

		UsbCommunicationEndpoint endpoint = service.newEndpoint("14fa", "0001");
		if (endpoint != null) {
			IomDriver driver = new IomDriver(endpoint);
			driver.startup();
		}
	}
	
	/**
	 * Create a new driver.
	 * 
	 * @param service
	 *            the USB comm endpoint service
	 * 
	 * @return the driver, which will not be started up
	 * 
	 * @throws InteractiveSpacesException
	 *             if no Iom is plugged in
	 */
	public static IomDriver newDriver(UsbCommunicationEndpointService service) {
		UsbCommunicationEndpoint endpoint = service.newEndpoint("14fa", "0001");
		if (endpoint != null) {
			return new IomDriver(endpoint);
		} else {
			throw new InteractiveSpacesException(
					"Cannot locate an Iom device on the USB bus");
		}
	}

	public static final byte CHARACTER_TAG_BEGIN = 0x3c;
	public static final byte CHARACTER_TAG_END = 0x3e;
	public static final byte CHARACTER_SLASH_TAG = 0x5c;
	public static final byte CHARACTER_RAW_START = 0x52;

	/**
	 * Communication endpoint for talking to sensor.
	 */
	private UsbCommunicationEndpoint endpoint;

	/**
	 * The buffer for reading content into.
	 * 
	 * <p>
	 * The size of this buffer is the report size for the device.
	 */
	private byte[] readBuffer;

	/**
	 * The size of the read buffer, in bytes.
	 */
	private int readBufferSize;

	/**
	 * The current position in the read buffer.
	 */
	private int curReadPosition;

	/**
	 * How many bytes in the current packet are good?
	 */
	private int curPacketSize;

	public IomDriver(UsbCommunicationEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void startup() {
		// The IOM reader is second endpoint for this port.
		endpoint.setEndpointIndex(1);
		
		endpoint.startup();

		readBuffer = endpoint.newBuffer();
		readBufferSize = endpoint.getReadBufferSize();

		// Having no idea where the message stream might be, just read
		// until the end of the current message in progress.
		readUntilEndOfMessage();

		readSensorMessage();
	}

	@Override
	public void shutdown() {
		endpoint.shutdown();
	}

	private void readSensorMessage() {
		while (true) {
			System.out.println("Reading from character " + curReadPosition);
			byte b = readByte();
			if (b != CHARACTER_TAG_BEGIN) {
				System.out
						.println("Did not find open tag begin in expected place");
				return;
			}

			System.out.println("Found message begin");

			b = readByte();
			if (b == CHARACTER_RAW_START) {
				// Skip over AW>
				readByte();
				readByte();
				b = readByte();
				System.out.println(Integer.toHexString(b) + " "
						+ Integer.toHexString(CHARACTER_TAG_END));
				if (b != CHARACTER_TAG_END) {
					System.out
							.println("Did not find open tag end in expected place");
					return;
				}

				readByte();
				readByte();
				readByte();
				readByte();
				readByte(); // Just a space, can be ignored
				readByte();
				readByte();
				readByte();
				readByte();
				b = readByte();
				if (b != CHARACTER_TAG_BEGIN) {
					System.out
							.println("Did not find close tag begin in expected place");
					return;
				}

				// Just consume the rest
				readUntilEndOfMessage();
				break;
			} else {
				System.out.println("Have message "
						+ new String(new byte[] { b }));
				// Ignore any other messages
				readUntilEndOfMessage();
			}
		}
	}

	/**
	 * Read and throw away bytes until the end of the current message.
	 */
	private void readUntilEndOfMessage() {
		// Read to the end of the current message
		while (true) {
			// First look for the end of a tag
			while (readByte() != CHARACTER_TAG_END)
				;

			byte b = readByte();
			if (b == 0x0a) {
				b = readByte();
				if (b == 0x0d) {
					System.out.println("Found end of message ");
					break;
				}
			}
		}
	}

	/**
	 * Read a byte from the endpoint
	 * 
	 * @return the byte read
	 * 
	 * @throws InteractiveSpacesException
	 *             some sort of read error has happened
	 */
	private byte readByte() {
		try {
			if (curReadPosition >= curPacketSize) {
				int length = endpoint.readReportSync(readBuffer);

				curPacketSize = (readBuffer[0] & 0xff) + 1;
				System.out.println("CurPacketSize " + curPacketSize);

				// Want to start at position 1 because first byte is
				// ignored.
				curReadPosition = 1;

				System.out.println(ByteUtils.toHexString(readBuffer));
				byte[] b = new byte[curPacketSize - 1];
				System.arraycopy(readBuffer, 1, b, 0, curPacketSize - 1);
				System.out.println(new String(b));
			}

			return readBuffer[curReadPosition++];
		} catch (Exception e) {
			throw new InteractiveSpacesException("Could not read USB device", e);
		}
	}

}
