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

package interactivespaces.comm.serial.rxtx;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.comm.serial.SerialCommunicationEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * A serial endpoint.
 * 
 * @author Keith M. Hughes
 */
public class RxtxSerialCommunicationEndpoint implements
		SerialCommunicationEndpoint {

	/**
	 * Number of msecs to wait for a serial port connection.
	 */
	private static final int TIME_TO_WAIT_FOR_PORT = 10000;

	/**
	 * Name of the port this endpoint is for.
	 */
	private String portName;

	/**
	 * The serial communication port.
	 */
	private SerialPort port;

	/**
	 * @param portName
	 *            the name of the port to connect to
	 */
	public RxtxSerialCommunicationEndpoint(String portName) {
		this.portName = portName;
	}

	@Override
	public void connect() {

		try {
			port = createSerialPort(portName);

			port.setSerialPortParams(9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to connect to serial port %s with rxtx", portName),
					e);
		}
	}

	@Override
	public void shutdown() {
		port.close();
	}

	@Override
	public InputStream getInputStream() {
		try {
			return port.getInputStream();
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to get serial port %s input stream with rxtx", portName),
					e);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			return port.getOutputStream();
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to get serial port %s output stream with rxtx", portName),
					e);
		}
	}

	/**
	 * Get a serial port.
	 * 
	 * @param portName
	 * @return
	 */
	private SerialPort createSerialPort(String portName) {
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier
				.getPortIdentifiers();

		CommPortIdentifier portId = null; // will be set if port found

		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = portIdentifiers.nextElement();
			if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& pid.getName().equals(portName)) {
				portId = pid;
				break;
			}
		}
		if (portId == null) {
			throw new InteractiveSpacesException("Could not find serial port "
					+ portName);
		}

		SerialPort port = null;
		try {
			// Name of the application asking for the port
			// Wait max. 10 sec. to acquire port
			port = (SerialPort) portId.open("interactivespaces",
					TIME_TO_WAIT_FOR_PORT);
		} catch (PortInUseException e) {
			throw new InteractiveSpacesException("Port already in use: "
					+ portName, e);
		}

		return port;
	}
}
