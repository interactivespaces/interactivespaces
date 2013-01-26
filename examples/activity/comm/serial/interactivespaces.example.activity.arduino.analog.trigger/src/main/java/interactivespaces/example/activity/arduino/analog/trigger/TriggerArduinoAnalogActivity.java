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

package interactivespaces.example.activity.arduino.analog.trigger;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.event.trigger.SimpleThresholdTrigger;
import interactivespaces.event.trigger.Trigger;
import interactivespaces.event.trigger.TriggerEventType;
import interactivespaces.event.trigger.TriggerListener;
import interactivespaces.event.trigger.TriggerState;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.util.concurrency.CancellableLoop;
import interactivespaces.util.resource.ManagedResourceWithTask;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * An Interactive Spaces Java-based activity which communicates with an Arduino
 * sketch which reads an analog port and sends its value over a serial
 * connection. Once the value of the analog signal gets beyond a certain
 * threshold, a trigger will be set.
 * 
 * <p>
 * The Arduino sends the values as raw bytes, not a string. The high order byte
 * is transmitted first. The serial line is set for 9600 baud.
 * 
 * <p>
 * This example is a little complicated. It uses a serial connection to talk to
 * the arduino. It requires a loop which runs independently to read values from
 * the arduino. The values from the arduino are sent to a threshold trigger
 * which, once a given threshold is met, will trigger. When triggered, the
 * activity will send a message out on a route that it has triggered.
 * 
 * <p>
 * The assumption for the analog signal is that a trigger is wanted when the
 * value from the analog port is larger than the threshold. If you want to
 * trigger below a certain value you can always check for a falling edge event
 * type.
 * 
 * @author Keith M. Hughes
 */
public class TriggerArduinoAnalogActivity extends BaseRoutableRosActivity {

	/**
	 * The name of the config property for obtaining the serial port.
	 */
	public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT = "space.hardware.serial.port";

	/**
	 * The name of the config property that gives the threshold value for
	 * triggering.
	 */
	public static final String CONFIGURATION_PROPERTY_TRIGGER_THRESHOLD = "activity.example.arduino.analog.trigger.threshold";

	/**
	 * The name of the config property that gives how much fuzz is allowed for
	 * the trigger threshold.
	 */
	public static final String CONFIGURATION_PROPERTY_TRIGGER_FUZZ = "activity.example.arduino.analog.trigger.fuzz";

	/**
	 * Size of the message, in bytes, we want from the arduino.
	 */
	private static final int MESSAGE_LENGTH = 2;

	/**
	 * The source for serial communication endpoints.
	 */
	private SerialCommunicationEndpointService communicationEndpointService;

	/**
	 * The communication endpoint for the arduino.
	 */
	private SerialCommunicationEndpoint arduinoEndpoint;

	/**
	 * A threshold trigger to detect when the trigger has gotten to a certain
	 * value.
	 */
	private SimpleThresholdTrigger trigger;

	@Override
	public void onActivitySetup() {
		communicationEndpointService = getSpaceEnvironment()
				.getServiceRegistry().getRequiredService(
						SerialCommunicationEndpointService.SERVICE_NAME);
		getLog().info(
				String.format("Serial ports available: %s",
						communicationEndpointService.getSerialPorts()));

		String portName = getConfiguration().getRequiredPropertyString(
				CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

		arduinoEndpoint = communicationEndpointService
				.newSerialEndpoint(portName);

		addManagedResource(new ManagedResourceWithTask(arduinoEndpoint,
				new CancellableLoop() {
					byte[] buffer = new byte[MESSAGE_LENGTH];

					@Override
					protected void loop() throws InterruptedException {
						readStream(buffer);
					}

					@Override
					protected void handleException(Exception e) {
						getLog().error("Exception while reading serial port", e);
					}
				}, getSpaceEnvironment()));

		trigger = new SimpleThresholdTrigger();
		trigger.addListener(new TriggerListener() {

			@Override
			public void onTrigger(Trigger trigger, TriggerState newState,
					TriggerEventType type) {
				getLog().info(
						String.format(
								"The trigger has gone to %s with type %s",
								newState, type));

				if (TriggerState.TRIGGERED.equals(newState)) {
					broadcastTriggeredMessage();
				}
			}
		});

		int threshold = getConfiguration().getPropertyInteger(
				CONFIGURATION_PROPERTY_TRIGGER_THRESHOLD, 220);
		int fuzz = getConfiguration().getPropertyInteger(
				CONFIGURATION_PROPERTY_TRIGGER_FUZZ, 20);
		trigger.setThreshold(threshold).setHysteresis(fuzz);
	}

	@Override
	public void onActivityActivate() {
		// Reset the trigger when the activity is deactivated.
		trigger.reset();
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

				// Production code should probably have this as a debug rather
				// and an info, then have logging at ERROR.
				//
				// For now will write logs of data to log since an example.
				getLog().info(String.format("Analog value is %d\n", val));

				// The new value from the Arduino is known. Tell the trigger
				// that the value has changed,
				trigger.update(val);
			}
		}
	}

	/**
	 * Broadcast a triggered message.
	 */
	private void broadcastTriggeredMessage() {
		getLog().info("Arduino analog value has triggered");

		Map<String, Object> message = Maps.newHashMap();
		message.put("message", "Do not stand so close to me!");
		sendOutputJson("triggermessage", message);
	}
}
