/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.comm.serial.bluetooth;

import interactivespaces.InteractiveSpacesException;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.bluetooth.L2CAPConnection;
import javax.microedition.io.Connector;

/**
 * A driver for the Wii remote.
 * 
 * @author Keith M. Hughes
 */
public class WiiRemoteDriver {

	private static final byte COMMAND_LIGHT = 0x11;

	private static final byte[] FULL_COMMAND_LIGHT_0 = { 0x52, COMMAND_LIGHT,
			0x10 };
	private static final byte[] FULL_COMMAND_LIGHT_1 = { 0x52, COMMAND_LIGHT,
			0x20 };
	private static final byte[] FULL_COMMAND_LIGHT_2 = { 0x52, COMMAND_LIGHT,
			0x40 };
	private static final byte[] FULL_COMMAND_LIGHT_3 = { 0x52, COMMAND_LIGHT,
			(byte) 0x80 };

	private static final byte[][] FULL_COMMAND_LIGHTS = { FULL_COMMAND_LIGHT_0,
			FULL_COMMAND_LIGHT_1, FULL_COMMAND_LIGHT_2, FULL_COMMAND_LIGHT_3 };

	private static byte COMMAND_READ_CALIBRATION = 0x17;

	private static byte[] FULL_COMMAND_READ_CALIBRATION = new byte[] { 0x52,
			COMMAND_READ_CALIBRATION, 0x00, 0x00, 0x00, 0x16, 0x00, 0x08 };

	/**
	 * The Bluetooth address for the remote.
	 */
	private String address;

	private L2CAPConnection receiveConnection = null;

	private L2CAPConnection sendConnection = null;
	
	/**
	 * The reader future.
	 */
	private Future<?> readerFuture;

	/**
	 * Start the driver up.
	 * 
	 * @param executorService
	 */
	public void startup(ScheduledExecutorService executorService) {
		try {
			address = "8C56C5D8C5A4";
			receiveConnection = (L2CAPConnection) Connector.open("btl2cap://"
					+ address + ":" + "13", Connector.READ, true);
			sendConnection = (L2CAPConnection) Connector.open("btl2cap://"
					+ address + ":" + "11", Connector.WRITE, true);
		} catch (Exception e) {
			throw new InteractiveSpacesException("Unable to connect to device",
					e);
		}
		
		readerFuture = executorService.submit(new RemoteReader());

		readCalibration();
	}

	/**
	 * Shut the driver down.
	 */
	public void shutdown() {
		if (readerFuture != null) {
			readerFuture.cancel(true);
			readerFuture = null;
		}
		
		if (receiveConnection != null) {
			try {
				receiveConnection.close();
				receiveConnection = null;
			} catch (IOException e) {
				// Don't care.
			}
		}

		if (sendConnection != null) {
			try {
				sendConnection.close();
				sendConnection = null;
			} catch (IOException e) {
				// Don't care.
			}
		}
	}

	/**
	 * Set a light on.
	 * 
	 * @param light
	 *            the light to turn on
	 */
	public void setLight(int light) {
		if (light >= 0 && light <= 3) {
			try {
				sendConnection.send(FULL_COMMAND_LIGHTS[light]);
			} catch (Exception e) {
				throw new InteractiveSpacesException(
						"Error sending light command", e);
			}
		}
	}

	/**
	 * Read the calibration from the remote
	 */
	private void readCalibration() {
		try {
			sendConnection.send(FULL_COMMAND_READ_CALIBRATION);
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Error sending read calibration command", e);
		}
	}
	
	private class RemoteReader implements Runnable {
		
		/**
		 * The read buffer
		 */
		private byte[] buffer = new byte[21];
		
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					receiveConnection.receive(buffer);
					
					System.out.println("Got read data " + Integer.toHexString(buffer[1]));
					
					switch(buffer[1]) {
					case 0x21:
						System.out.println("Got calibration response");
						break;
					case 0x30:
						int button = (buffer[2] << 8) | buffer[3];
						System.out.println("Got button " + button);
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
