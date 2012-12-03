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

package interactivespaces.comm.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Enumeration;

/**
 * An rxtx serial connection.
 * 
 * @author Keith M. Hughes
 */
public class RxtxSerialConnection {

	/**
	 * The port name to talk to.
	 */
	private String portName = "/dev/ttyUSB0";

	/**
	 * The serial port communicating with the serial endpoint.
	 */
	private SerialPort port;

	// private ROSSerial rosSerial;

	private int serialPortBaudRate;
	
	/**
	 * The space environment being run in.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	public RxtxSerialConnection(InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * Connect to the serial connection
	 */
	public void connect() {
		try {
			port = createSerialPort(portName);

			serialPortBaudRate = 57600;
			port.setSerialPortParams(serialPortBaudRate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			spaceEnvironment.getLog().error("Have serial connection");
		} catch (Exception e) {
			throw new InteractiveSpacesException("Cannot start serial node", e);
		}
	}

	/**
	 * Shut down and release all components of the serial connection.
	 */
	public void close() {
		port.close();
		port = null;
	}

	/**
	 * Get a serial port.
	 * 
	 * @param portName
	 *            name of the port desired
	 * 
	 * @return the requested port
	 * 
	 * @throws InteractiveSpacesException
	 *             the port was not found or was already in use
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
			// Wait max. 10 sec. to acquire port
			port = (SerialPort) portId.open("interactivespaces", 10000);
		} catch (PortInUseException e) {
			throw new InteractiveSpacesException("Port already in use: "
					+ portName, e);
		}

		return port;
	}
}
