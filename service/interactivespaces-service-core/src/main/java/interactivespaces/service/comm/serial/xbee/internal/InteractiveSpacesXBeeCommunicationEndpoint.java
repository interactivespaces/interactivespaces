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
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListener;
import interactivespaces.util.concurrency.CancellableLoop;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;

/**
 * An Interactive Spaces implementation of an XBee communication endpoint.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesXBeeCommunicationEndpoint implements
		XBeeCommunicationEndpoint {

/*	public static void main(String[] args) {

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
				public void onAtLocalXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						AtLocalResponseXBeeFrame response) {
					log1.info(response);
					log1.info(ByteUtils.toHexString(response.getCommandData()));
				}

				@Override
				public void onRxXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						RxResponseXBeeFrame response) {
					log1.info(response);
				}

				@Override
				public void onTxStatusXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						TxStatusXBeeFrame response) {
					log1.info(response);
				}

				@Override
				public void onAtRemoteXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						AtRemoteResponseXBeeFrame response) {
					log1.info(response);
					log1.info(ByteUtils.toHexString(response.getCommandData()));
				}
			});

			endpoint2 = new InteractiveSpacesXBeeCommunicationEndpoint(
					new RxtxSerialCommunicationEndpoint("/dev/ttyUSB1"),
					executor, log2);
			endpoint2.startup();

			endpoint2.addListener(new XBeeResponseListenerSupport() {

				@Override
				public void onAtLocalXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						AtLocalResponseXBeeFrame response) {
					log1.info(response);
					log1.info(ByteUtils.toHexString(response.getCommandData()));
				}

				@Override
				public void onRxXBeeResponse(
						XBeeCommunicationEndpoint endpoint,
						RxResponseXBeeFrame response) {
					log2.info(response);
					log2.info(ByteUtils.toHexString(response.getReceivedData()));
				}
			});

			endpoint2.test();
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
*/
	/**
	 * The communication endpoint for speaking with the XBee.
	 */
	private SerialCommunicationEndpoint commEndpoint;

	/**
	 * reader for the XBee frames.
	 */
	private EscapedXBeeFrameReader reader;

	/**
	 * Future for the reader loop.
	 */
	private Future<?> readerLoopFuture;

	/**
	 * Parser for response frames.
	 */
	private ResponseXBeeFrameHandler frameHandler = new SimpleResponseXBeeFrameHandler();

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

		this.reader = new EscapedXBeeFrameReader(commEndpoint);
	}

	@Override
	public void startup() {
		commEndpoint.startup();

		CancellableLoop readerLoop = new CancellableLoop() {
			@Override
			protected void loop() throws InterruptedException {
				readFrame();
			}
		};

		readerLoopFuture = executorService.submit(readerLoop);
	}

	@Override
	public void shutdown() {
		readerLoopFuture.cancel(true);

		if (commEndpoint != null) {
			commEndpoint.shutdown();
			commEndpoint = null;
		}
	}

	public void test() {
		RequestXBeeFrame content1 = new AtLocalRequestXBeeFrame(
				XBeeApiConstants.AT_COMMAND_AP, 0x7d);
		// content1.add(0x02);

		content1.write(commEndpoint);
		//
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// RequestXBeeFrame content2 = new AtLocalRequestXBeeFrame(
		// XBeeApiConstants.AT_COMMAND_SL, 0x12);
		//
		// content2.write(commEndpoint);
	}

	public void test1() {
		RequestXBeeFrame content = new AtRemoteRequestXBeeFrame(
				new XBeeAddress64("0013a200407bd2e3"),
				XBeeApiConstants.AT_COMMAND_SL, 0x11, 0);

		content.write(commEndpoint);
	}

	public void test2() {
		RequestXBeeFrame content = new TxRequestXBeeFrame(new XBeeAddress64(
				"0013a200407bd2e3"), 0x03, 0, 0);
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
		if (!reader.waitForStartFrame()) {
			log.warn("End of XBee serial stream detected");
			return;
		}

		int length = reader.readPacketLength();

		frameHandler.handle(this, reader, length, listeners, log);

		// Go past checksum
		reader.readByte();
	}
}
