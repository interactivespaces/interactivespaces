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

import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.internal.rxtx.RxtxSerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.util.concurrency.CancellableLoop;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An nteractive Spaces implementation of an XBee communication endpoint.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesXbeeCommunicationEndpoint implements
		XBeeCommunicationEndpoint {

	public static void main(String[] args) {
		InteractiveSpacesXbeeCommunicationEndpoint endpoint = null;
		try {
			endpoint = new InteractiveSpacesXbeeCommunicationEndpoint();
			endpoint.startup();

			endpoint.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (endpoint != null) {
				endpoint.shutdown();
			}
		}
	}

	/**
	 * The communication endpoint for speaking with the XBee.
	 */
	private SerialCommunicationEndpoint commEndpoint;

	private ScheduledExecutorService executor = Executors
			.newScheduledThreadPool(10);

	private Future<?> readerLoop;

	@Override
	public void startup() {
		commEndpoint = new RxtxSerialCommunicationEndpoint("/dev/ttyUSB0");
		commEndpoint.startup();

		CancellableLoop loop = new CancellableLoop() {
			@Override
			protected void loop() throws InterruptedException {
				readFrame();
			}
		};

		readerLoop = executor.submit(loop);
	}

	@Override
	public void shutdown() {
		readerLoop.cancel(true);
		executor.shutdown();
		if (commEndpoint != null) {
			commEndpoint.shutdown();
			commEndpoint = null;
		}
	}

	public void test() {
		XBeeFrameWriter content = new XBeeFrameWriter(XBeeApiConstants.FRAME_TYPE_AT_LOCAL_SEND_IMMEDIATE);

		content.add(0x11);

		content.add(0x53);
		content.add(0x4c);

		content.write(commEndpoint);
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Read a frame from the connected XBee
	 */
	private void readFrame() {
		// Read until either the frame start byte is read or the end of stream
		// is reached.
		int b;
		while ((b = commEndpoint.read()) != XBeeApiConstants.FRAME_START_BYTE && b != -1)
			;

		int highLength = commEndpoint.read();
		int lowLength = commEndpoint.read();
		int length = (highLength & 0xff) << 8 | (lowLength & 0xff);

		byte[] result = new byte[length];

		int pos = 0;
		int bytesRead = 0;
		int bytesLeft = length;
		while ((bytesRead = commEndpoint.read(result, pos, bytesLeft)) != -1) {

			bytesLeft -= bytesRead;
			pos += bytesRead;
			if (bytesLeft == 0)
				break;
		}

		// Go past checksum
		commEndpoint.read();
		
		switch ((int)(result[0] & 0xff)) {
		case XBeeApiConstants.FRAME_TYPE_AT_LOCAL_RESPONSE:
			System.out.println("AT Local Response");
			break;
		case XBeeApiConstants.FRAME_TYPE_AT_REMOTE_RESPONSE:
			System.out.println("AT Remote Response");
			break;
		case XBeeApiConstants.FRAME_TYPE_TX_RESPONSE:
			System.out.println("TX Response");
			break;
		case XBeeApiConstants.FRAME_TYPE_RX_RECEIVE:
			System.out.println("RX Receive");
			break;
		default:
			System.out.format("Unknown frame type %d\n", result[0]);
		}

		for (int i = 0; i < result.length; i++) {
			System.out.println(Integer.toHexString(result[i] & 0xff));
		}
	}
}
