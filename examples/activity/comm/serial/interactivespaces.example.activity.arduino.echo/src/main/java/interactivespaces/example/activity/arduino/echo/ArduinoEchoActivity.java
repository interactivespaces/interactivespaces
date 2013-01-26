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

package interactivespaces.example.activity.arduino.echo;

import java.util.Random;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.util.concurrency.CancellableLoop;
import interactivespaces.util.resource.ManagedResourceWithTask;

/**
 * An Interactive Spaces Java-based activity which writes bytes on a serial
 * connection and expects those bytes to be written back. The project includes
 * an Arduino sketch which will echo back what it receives on its serial
 * connection.
 * 
 * <p>
 * A random value is sent every time the activity is activated. The value is
 * random because we want to make sure that what was sent is exactly what was
 * received.
 * 
 * @author Keith M. Hughes
 */
public class ArduinoEchoActivity extends BaseActivity {

	/**
	 * The name of the config property for obtaining the serial port.
	 */
	public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT = "space.hardware.serial.port";

	/**
	 * The communication endpoint for the arduino.
	 */
	private SerialCommunicationEndpoint serialEndpoint;

	/**
	 * A random number generator to use so that we can test that what is really
	 * being sent is really being received.
	 */
	private Random random;

	@Override
	public void onActivitySetup() {
		SerialCommunicationEndpointService communicationEndpointService = getSpaceEnvironment()
				.getServiceRegistry().getRequiredService(
						SerialCommunicationEndpointService.SERVICE_NAME);
		getLog().info(
				String.format("Serial ports available: %s",
						communicationEndpointService.getSerialPorts()));

		String portName = getConfiguration().getRequiredPropertyString(
				CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

		serialEndpoint = communicationEndpointService
				.newSerialEndpoint(portName);

		addManagedResource(new ManagedResourceWithTask(serialEndpoint,
				new CancellableLoop() {
					@Override
					protected void loop() throws InterruptedException {
						readStream();
					}

					@Override
					protected void handleException(Exception e) {
						getLog().error("Exception while reading serial port", e);
					}
				}, getSpaceEnvironment()));

		random = new Random(System.currentTimeMillis());
	}

	@Override
	public void onActivityActivate() {
		// Get an 8 but number to send
		int valueToSend = random.nextInt() & 0xff;

		getLog().info(
				String.format("Writing %d to the serial connection",
						valueToSend));

		serialEndpoint.write(valueToSend);
	}

	/**
	 * Attempt to read the serial data from the serial connection.
	 */
	private void readStream() {
		int value = serialEndpoint.read() & 0xff;

		// To keep the example simple, just write the value to a log.
		// A real production piece of code would probably not log every
		// value, or would log at a debug level as otherwise your logs
		// will get full.
		getLog().info(
				String.format("Received value %d from the serial connection",
						value));
	}
}
