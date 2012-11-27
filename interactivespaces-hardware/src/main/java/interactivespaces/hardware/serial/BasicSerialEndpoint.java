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

package interactivespaces.hardware.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import interactivespaces.InteractiveSpacesException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A serial endpoint.
 * 
 * <p>
 * This is useful if the serial endpoint should not be considered its own
 * application, but rather owned by another application.
 * 
 * @author Keith M. Hughes
\ */
public class BasicSerialEndpoint implements SerialEndpoint {
	
	/**
	 * Number of msecs to wait for a serial port connection.
	 */
	private static final int TIME_TO_WAIT_FOR_PORT = 10000;

	/**
	 * Map from port names to all information about the port.
	 */
	private Map<String, PortData> portData = new HashMap<String, PortData>();

	/**
	 * Thread pool for this endpoint.
	 */
	private ScheduledExecutorService executor;

	/**
	 * 
	 * @param executor
	 *            the creator of this endpoint is in charge of the lifecycle of
	 *            the executor
	 */
	public BasicSerialEndpoint(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	@Override
	public void startup() {
		// For now nothing to do.
	}

	@Override
	public void shutdown() {
		for (PortData data : portData.values()) {
			closePort(data);
		}
		portData.clear();
	}

	@Override
	public void addPort(String portName) {
		if (portData.containsKey(portName))
			throw new InteractiveSpacesException("Port already open: " + portName);

		try {
			SerialPort port = createSerialPort(portName);

			port.setSerialPortParams(57600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// This is a hack. When the port first opens it can drop some bytes.
			for (int i = 0; i < 50; i++)
				port.getOutputStream().write(0);
			Thread.sleep(1000);
			for (int i = 0; i < 50; i++)
				port.getOutputStream().write(0);

//			getLog().info(
//					"NSerial connectione connected to " + portName);

			// TODO(keith): Get a thread pool in here.
			//Thread thread = new Thread(rs);

			//portData.put(portName, new PortData(node, portName, port, rs,
			//		thread));

			//thread.start();
		} catch (Exception e) {
//			getLog()
//					.error("Could not start up serial port " + portName, e);
		}
	}

	@Override
	public void shutdownPort(String portName) {
		PortData data = portData.get(portName);
		if (data != null) {
			closePort(data);

			portData.remove(portName);
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
			throw new InteractiveSpacesException("Could not find serial port " + portName);
		}

		SerialPort port = null;
		try {
			// Name of the application asking for the port
			// Wait max. 10 sec. to acquire port
			port = (SerialPort) portId.open("interactivespaces",
					TIME_TO_WAIT_FOR_PORT);
		} catch (PortInUseException e) {
			throw new InteractiveSpacesException("Port already in use: " + portName, e);
		}

		return port;
	}

	/**
	 * Shut down a port.
	 * 
	 * <p>
	 * Not removed from the portData map.
	 * 
	 * @param data
	 *            The data about the port to be shut down.
	 */
	private void closePort(PortData data) {
		//data.getEndpoint().shutdown();
		data.getPort().close();
	}

	/**
	 * Data about a port being communicated with.
	 * 
	 * @author Keith M. Hughes
	 * @since Jul 29, 2011
	 */
	private static class PortData {

		/**
		 * The name of the port.
		 */
		private String portName;

		/**
		 * The serial port associated with the portname.
		 */
		private SerialPort port;

		/**
		 * The ros serial endpoint.
		 */
		private Void endpoint;

		/**
		 * Thread port is running in.
		 */
		private Thread thread;

		public PortData(String portName, SerialPort port,
				Void endpoint, Thread thread) {
			this.portName = portName;
			this.port = port;
			this.endpoint = endpoint;
			this.thread = thread;
		}

		/**
		 * @return the portName
		 */
		public String getPortName() {
			return portName;
		}

		/**
		 * @return the port
		 */
		public SerialPort getPort() {
			return port;
		}

		/**
		 * @return the endpoint
		 */
		public Void getEndpoint() {
			return endpoint;
		}
	}
}
