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
import interactivespaces.service.comm.serial.xbee.XBeeResponseListener;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import interactivespaces.util.concurrency.CancellableLoop;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;

/**
 * An nteractive Spaces implementation of an XBee communication endpoint.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesXBeeCommunicationEndpoint implements
		XBeeCommunicationEndpoint {

	public static void main(String[] args) {

		ScheduledExecutorService executor = Executors
				.newScheduledThreadPool(10);

		final Log log1 = new Jdk14Logger("endpoint1");
		final Log log2 = new Jdk14Logger("endpoint2");

		InteractiveSpacesXBeeCommunicationEndpoint endpoint1 = null;
		InteractiveSpacesXBeeCommunicationEndpoint endpoint2 = null;
		try {
			endpoint1 = new InteractiveSpacesXBeeCommunicationEndpoint(
					new RxtxSerialCommunicationEndpoint("/dev/ttyUSB0"),
					executor, log1);
			endpoint1.startup();
			endpoint1.addListener(new XBeeResponseListenerSupport() {
				
				@Override
				public void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint,
						RxResponseXBeeFrame response) {
					log1.info(response);
				}

				@Override
				public void onTxResponseXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						TxResponseXBeeFrame response) {
					log1.info(response);
				}
			});

			endpoint2 = new InteractiveSpacesXBeeCommunicationEndpoint(
					new RxtxSerialCommunicationEndpoint("/dev/ttyUSB1"),
					executor, log2);
			endpoint2.startup();
			
			endpoint2.addListener(new XBeeResponseListenerSupport() {
				
				@Override
				public void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint,
						RxResponseXBeeFrame response) {
					log2.info(response);
				}
				
				@Override
				public void onAtRemoteXBeeResponse(XBeeCommunicationEndpoint endpoint,
						AtRemoteResponseXBeeFrame response) {
					log2.info(response);
				}
			});

			endpoint1.test2();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (endpoint1 != null) {
				endpoint1.shutdown();
			}
			
			if (endpoint2 != null) {
				endpoint2.shutdown();
			}

			executor.shutdown();
		}
	}

	/**
	 * The communication endpoint for speaking with the XBee.
	 */
	private SerialCommunicationEndpoint commEndpoint;

	/**
	 * Future for the reader loop.
	 */
	private Future<?> readerLoop;

	/**
	 * Parser for response frames.
	 */
	private ResponseXBeeFrameHandler frameHandler = new EscapedResponseXBeeFrameHandler();

	/**
	 * The listeners for the endpoint.
	 */
	private List<XBeeResponseListener> listeners = new CopyOnWriteArrayList<XBeeResponseListener>();

	/**
	 * The executor service for running the reader loop.
	 */
	private ScheduledExecutorService executorService;

	/**
	 * Log for the endpoint.
	 */
	private Log log;

	public InteractiveSpacesXBeeCommunicationEndpoint(
			SerialCommunicationEndpoint commEndpoint,
			ScheduledExecutorService executorService, Log log) {
		this.commEndpoint = commEndpoint;
		this.executorService = executorService;
		this.log = log;
	}

	@Override
	public void startup() {
		commEndpoint.startup();

		CancellableLoop loop = new CancellableLoop() {
			@Override
			protected void loop() throws InterruptedException {
				readFrame();
			}
		};

		readerLoop = executorService.submit(loop);
	}

	@Override
	public void shutdown() {
		readerLoop.cancel(true);

		if (commEndpoint != null) {
			commEndpoint.shutdown();
			commEndpoint = null;
		}
	}

	public void test() {
		RequestXBeeFrame content = new AtLocalRequestXBeeFrame(
				XBeeApiConstants.AT_COMMAND_SL, 0x11);

		content.write(commEndpoint);
	}

	public void test1() {
		RequestXBeeFrame content = new AtRemoteRequestXBeeFrame(
				new XBeeAddress64(0x00, 0x13, 0xa2, 0x00, 0x40, 0x7b, 0xd2,
						0xe3), XBeeApiConstants.AT_COMMAND_SL, 0x11, 0);

		content.write(commEndpoint);
	}

	public void test2() {
		RequestXBeeFrame content = new TxRequestXBeeFrame(new XBeeAddress64(
				0x00, 0x13, 0xa2, 0x00, 0x40, 0x7b, 0xd2, 0xe3), 0x11, 0, 0);
		content.add16(1234);

		content.write(commEndpoint);
	}

	@Override
	public void addListener(XBeeResponseListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(XBeeResponseListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Read a frame from the connected XBee
	 */
	private void readFrame() {
		// Read until either the frame start byte is read or the end of stream
		// is reached.
		int b;
		while ((b = commEndpoint.read()) != XBeeApiConstants.FRAME_START_BYTE
				&& b != -1)
			;

		int highLength = commEndpoint.read();
		int lowLength = commEndpoint.read();
		int length = (highLength & 0xff) << 8 | (lowLength & 0xff);

		byte[] frame = new byte[length];

		int pos = 0;
		int bytesRead = 0;
		int bytesLeft = length;
		while ((bytesRead = commEndpoint.read(frame, pos, bytesLeft)) != -1) {

			bytesLeft -= bytesRead;
			pos += bytesRead;
			if (bytesLeft == 0)
				break;
		}

		// Go past checksum
		commEndpoint.read();

		frameHandler.handle(this, frame, listeners, log);
	}
}
