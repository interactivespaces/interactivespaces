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

package interactivespaces.example.activity.arduino.analog;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.comm.serial.SerialCommunicationEndpointFactory;
import interactivespaces.comm.serial.rxtx.RxtxSerialCommunicationEndpointFactory;

import java.util.concurrent.Future;

/**
 * A simple Interactive Spaces Java-based activity which communicates with an
 * Arduino sketch which reads an analog port and sends its value over a serial
 * connection.
 * 
 * <p>
 * The Arduino sends the values as raw bytes, not a string. The high order byte
 * is transmitted first. The serial line is set for 9600 baud.
 * 
 * @author Keith M. Hughes
 */
public class ArduinoAnalogActivity extends BaseActivity {

	private static final int MESSAGE_LENGTH = 2;

	/**
	 * The name of the config property for obtaining the serial port.
	 */
	public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT = "space.hardware.serial.port";

	/**
	 * The source for serial communication endpoints.
	 */
	private SerialCommunicationEndpointFactory communicationEndpointFactory;

	/**
	 * The communication endpoint fot the arduino.
	 */
	private SerialCommunicationEndpoint arduinoEndpoint;

	/**
	 * The thread reading the stream.
	 */
	private Future<?> arduinoReader;

	@Override
	public void onActivitySetup() {
		getLog().info(
				"Activity interactivespaces.example.activity.arduino.analog.java setup");

		// TODO(keith): Get this as a service
		communicationEndpointFactory = new RxtxSerialCommunicationEndpointFactory();
		getLog().info(
				String.format("Serial ports available: %s",
						communicationEndpointFactory.getSerialPorts()));
	}

	@Override
	public void onActivityStartup() {
		getLog().info(
				"Activity interactivespaces.example.activity.arduino.analog.java startup");
		String portName = getConfiguration().getRequiredPropertyString(
				CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

		arduinoEndpoint = communicationEndpointFactory
				.newSerialEndpoint(portName);
		arduinoEndpoint.connect();

		arduinoReader = getSpaceEnvironment().getExecutorService().submit(
				new Runnable() {
					public void run() {
						byte[] buffer = new byte[MESSAGE_LENGTH];
						try {
							while (!Thread.interrupted()) {
								readStream(buffer);
							}
						} catch (Exception e) {
							getLog().error("Exception while reading serial port", e);
						}
					}
				});

	}

	@Override
	public void onActivityCleanup() {
		getLog().info(
				"Activity interactivespaces.example.activity.arduino.analog.java cleanup");

		if (arduinoReader != null) {
			arduinoReader.cancel(true);
		}

		if (arduinoEndpoint != null) {
			arduinoEndpoint.shutdown();
		}
	}

	/**
	 * Attempt to read the serial data from the arduino.
	 * 
	 * @param buffer
	 *            the buffer for storing bytes read
	 */
	private void readStream(byte[] buffer) {
		if (arduinoEndpoint.available() >= MESSAGE_LENGTH) {
			arduinoEndpoint.read(buffer);

			// If activated, process the bytes. But they must be read
			// regardless.
			if (isActivated()) {
				int val = ((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff);

				getLog().info(String.format("Analog value is %d\n", val));
			}
		}
	}
}
