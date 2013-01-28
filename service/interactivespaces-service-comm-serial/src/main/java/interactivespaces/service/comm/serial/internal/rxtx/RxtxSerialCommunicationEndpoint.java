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

package interactivespaces.service.comm.serial.internal.rxtx;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Enumeration;

import com.google.common.collect.Maps;

/**
 * A serial endpoint.
 * 
 * @author Keith M. Hughes
 */
public class RxtxSerialCommunicationEndpoint implements
		SerialCommunicationEndpoint {

	/**
	 * The map of parity enums to their {@link SerialPort} equivalents.
	 */
	private static final EnumMap<Parity, Integer> SERIAL_PORT_PARITY_VALUES = Maps
			.newEnumMap(Parity.class);

	static {
		SERIAL_PORT_PARITY_VALUES.put(Parity.NONE, SerialPort.PARITY_NONE);
		SERIAL_PORT_PARITY_VALUES.put(Parity.ODD, SerialPort.PARITY_ODD);
		SERIAL_PORT_PARITY_VALUES.put(Parity.EVEN, SerialPort.PARITY_EVEN);
		SERIAL_PORT_PARITY_VALUES.put(Parity.MARK, SerialPort.PARITY_MARK);
		SERIAL_PORT_PARITY_VALUES.put(Parity.SPACE, SerialPort.PARITY_SPACE);
	}
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
	 * Baud rate for the serial connection
	 */
	private int baud = 9600;

	/**
	 * The number of data bits.
	 */
	private int dataBits = SerialPort.DATABITS_8;

	/**
	 * The number of stop bits.
	 */
	private int stopBits = SerialPort.STOPBITS_1;

	/**
	 * The parity of the connection.
	 */
	private Parity parity = Parity.NONE;

	/**
	 * @param portName
	 *            the name of the port to connect to
	 */
	public RxtxSerialCommunicationEndpoint(String portName) {
		this.portName = portName;
	}

	@Override
	public void startup() {
		try {
			port = createSerialPort(portName);

			port.setSerialPortParams(baud, dataBits, stopBits,
					SERIAL_PORT_PARITY_VALUES.get(parity));
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
	public int available() {
		try {
			return port.getInputStream().available();
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					String.format(
							"Unable to get number of available bytes from serial port %s input stream with rxtx",
							portName), e);
		}
	}

	@Override
	public int read() {
		try {
			return port.getInputStream().read();
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to read serial port %s output stream with rxtx",
					portName), e);
		}
	}

	@Override
	public int read(byte[] buffer) {
		try {
			return port.getInputStream().read(buffer);
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to read serial port %s output stream with rxtx",
					portName), e);
		}
	}

	@Override
	public int read(byte[] buffer, int offset, int length) {
		try {
			return port.getInputStream().read(buffer, offset, length);
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to read serial port %s output stream with rxtx",
					portName), e);
		}
	}

	@Override
	public void flush() {
		try {
			port.getOutputStream().flush();
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to flush serial port %s output stream with rxtx",
					portName), e);
		}
	}

	@Override
	public void write(int b) {
		try {
			port.getOutputStream().write(b);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					String.format(
							"Unable to write byte serial port %s output stream with rxtx",
							portName), e);
		}
	}

	@Override
	public void write(byte b[]) {
		try {
			port.getOutputStream().write(b);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					String.format(
							"Unable to write bytes to serial port %s output stream with rxtx",
							portName), e);
		}
	}

	@Override
	public void write(byte b[], int offset, int length) {
		try {
			port.getOutputStream().write(b, offset, length);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					String.format(
							"Unable to write bytes to serial port %s output stream with rxtx",
							portName), e);
		}
	}

	@Override
	public SerialCommunicationEndpoint setBaud(int baud) {
		this.baud = baud;

		return this;
	}

	@Override
	public SerialCommunicationEndpoint setDataBits(int dataBits) {
		this.dataBits = dataBits;

		return this;
	}

	@Override
	public SerialCommunicationEndpoint setStopBits(int stopBits) {
		this.stopBits = stopBits;

		return this;
	}

	@Override
	public SerialCommunicationEndpoint setParity(Parity parity) {
		this.parity = parity;

		return this;
	}

	/**
	 * Get a serial port.
	 * 
	 * @param portName
	 *            the name of the requested port
	 * 
	 * @return the port
	 * 
	 * @throws InteractiveSpacesException
	 *             the port wasn't found or some other error happened
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
