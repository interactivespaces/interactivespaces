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

package interactivespaces.service.comm.serial.bluetooth.jsr82;

import java.io.IOException;
import java.io.InterruptedIOException;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.bluetooth.BluetoothCommunicationEndpoint;

import javax.bluetooth.L2CAPConnection;
import javax.microedition.io.Connector;

/**
 * A bluetooth connection using Java JSR-82
 * 
 * @author Keith M. Hughes
 */
public class Jsr82MultiPortBluetoothCommunicationEndpoint implements
		BluetoothCommunicationEndpoint {

	/**
	 * The Bluetooth address for the connection.
	 */
	private String address;

	/**
	 * The port for the receive connection.
	 */
	private int receivePort;

	/**
	 * The port for the send connection.
	 */
	private int sendPort;

	/**
	 * The connection for receiving.
	 */
	private L2CAPConnection receiveConnection;

	/**
	 * The connection for sending.
	 */
	private L2CAPConnection sendConnection;

	public Jsr82MultiPortBluetoothCommunicationEndpoint(String address,
			int receivePort, int sendPort) {
		this.address = address;
		this.receivePort = receivePort;
		this.sendPort = sendPort;
	}

	@Override
	public void startup() {
		try {
			receiveConnection = (L2CAPConnection) Connector.open("btl2cap://"
					+ address + ":" + Integer.toString(receivePort),
					Connector.READ, true);
			sendConnection = (L2CAPConnection) Connector.open("btl2cap://"
					+ address + ":" + Integer.toString(sendPort),
					Connector.WRITE, true);
		} catch (Exception e) {
			throw new InteractiveSpacesException("Unable to connect to device",
					e);
		}
	}

	/**
	 * Shut the connection down.
	 */
	public void shutdown() {
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

	@Override
	public void write(byte[] data) {
		try {
			sendConnection.send(data);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Error while writing bytes to a bluetooth connection", e);
		}
	}

	@Override
	public int read(byte[] data) {
		try {
			receiveConnection.receive(data);

			return data.length;
		} catch (InterruptedIOException e) {
			// Don't care, being ended
			return -1;
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Error while receiving bytes from a bluetooth connection",
					e);
		}
	}

}
