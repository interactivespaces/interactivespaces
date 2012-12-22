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

package interactivespaces.activity.gestural.sensacell;

import interactivespaces.activity.gestural.sensacell.SensacellDriver.SensacellListener;
import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.configuration.Configuration;
import interactivespaces.event.trigger.SimpleTriggerPoint;
import interactivespaces.event.trigger.Trigger;
import interactivespaces.event.trigger.TriggerEventType;
import interactivespaces.event.trigger.TriggerListener;
import interactivespaces.event.trigger.TriggerState;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

/**
 * An Interactive Spaces Activity for reading a sensacell and updating a
 * Gestural topic.
 * 
 * @author Keith M. Hughes
 */
public class SensacellGesturalActivity extends BaseRoutableRosActivity {

	/**
	 * Configuration property giving which element of the array for left
	 * gestures.
	 */
	public static final String CONFIGURATION_SENSACELL_GESTURE_SENSOR_LEFT = "sensacell_gestural.gestural.sensor.left";

	/**
	 * Configuration property giving which element of the array for right
	 * gestures.
	 */
	public static final String CONFIGURATION_SENSACELL_GESTURE_SENSOR_RIGHT = "sensacell_gestural.gestural.sensor.right";

	/**
	 * Configuration property giving which serial port to use for the sensacell.
	 */
	public static final String CONFIGURATION_SENSACELL_SENSACELL_PORT = "sensacell_gestural.sensacell.port";

	/**
	 * Configuration property giving the sensor threshold for the sensacell.
	 */
	public static final String CONFIGURATION_SENSACELL_SENSACELL_THRESHOLD = "sensacell_gestural.sensacell.threshold";

	/**
	 * Configuration property giving the sensor hysteresis for the sensacell.
	 */
	public static final String CONFIGURATION_SENSACELL_SENSACELL_HYSTERESIS = "sensacell_gestural.sensacell.hysteresis";

	/**
	 * Configuration property giving the sensor update rate for the sensacell.
	 */
	public static final String CONFIGURATION_SENSACELL_SENSACELL_UPDATE = "sensacell_gestural.gestural.sensor.update";

	/**
	 * The default threshold for detecting something on the sensacell.
	 */
	public static final int DEFAULT_SENSACELL_THRESHOLD = 110;

	/**
	 * The default hysteresis value for reading the sensacell.
	 */
	public static final int DEFAULT_SENSACELL_HYSTERESIS = 20;

	/**
	 * The default sensacell update rate value for reading the sensacell. In
	 * updates per second.
	 */
	public static final double DEFAULT_SENSACELL_UPDATE = 30.0;

	/**
	 * The index of the default element on the sensacell for left gestures.
	 */
	public static final int DEFAULT_LEFT_SENSOR = 8;

	/**
	 * The index of the default element on the sensacell for right gestures.
	 */
	public static final int DEFAULT_RIGHT_SENSOR = 11;

	/**
	 * The sensacell being sensed.
	 */
	private SensacellDriver sensacell;

	/**
	 * The serial port the sensacell will sit on.
	 */
	private String sensacellSerialPort;

	/**
	 * The sensor in the sensacell to use for left gestures.
	 */
	private int leftSensor = DEFAULT_LEFT_SENSOR;

	/**
	 * The sensor in the sensacell to use for right gestures.
	 */
	private int rightSensor = DEFAULT_RIGHT_SENSOR;

	/**
	 * {@code true} if the left sensor is on, {@code false} otherwise.
	 */
	private boolean leftSensorOn;

	/**
	 * {@code true} if the right sensor is on, {@code false} otherwise.
	 */
	private boolean rightSensorOn;

	/**
	 * Trigger for the left sensor.
	 */
	private SimpleTriggerPoint leftTrigger;

	/**
	 * Trigger for the right sensor.
	 */
	private SimpleTriggerPoint rightTrigger;

	/**
	 * Threshold for triggering a sensor.
	 */
	private int sensorThreshold;

	/**
	 * Hysteresis for triggering a sensor.
	 */
	private int sensorHysteresis;

	/**
	 * The update loop for this app.
	 */
	private Future<?> updater;

	/**
	 * How often the SensacellDriver should be updated. Is updates per second.
	 */
	private double sensorUpdateRate = DEFAULT_SENSACELL_UPDATE;

	public SensacellGesturalActivity() {
	}

	@Override
	public void onActivityStartup() {
		handleConfiguration();

		sensacell = new SensacellDriver(sensacellSerialPort, getLog());

		// listen for EDataAvailable event from sensacell instance
		sensacell.addListener(new SensacellListener() {
			@Override
			public void onSensacellUpdate(SensacellDriver cell) {
				processData(cell);
			}
		});

		leftTrigger = new SimpleTriggerPoint();
		leftTrigger.addListener(new TriggerListener() {
			@Override
			public void onTrigger(Trigger src, TriggerState newState,
					TriggerEventType event) {
				onLeftChange(event);
			}
		});

		rightTrigger = new SimpleTriggerPoint();
		rightTrigger.addListener(new TriggerListener() {
			@Override
			public void onTrigger(Trigger src, TriggerState newState,
					TriggerEventType event) {
				onRightChange(event);
			}
		});

		sensacell.startup(getSpaceEnvironment());
		sensacell.setReadMode(SensacellDriver.SENSACELL_MODE_READ_PROPORTIONAL);
		sensacell.setSensorSpeed(SensacellDriver.SENSACELL_SPEED_20HZ);

		updater = getSpaceEnvironment()
				.getExecutorService()
				.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						update();
					}
				}, 0, (long) (1000.0 / sensorUpdateRate), TimeUnit.MILLISECONDS);
	}

	@Override
	public void onActivityCleanup() {
		updater.cancel(true);

		sensacell.shutdown();
	}

	/**
	 * Handle configuration of the activity.
	 */
	private void handleConfiguration() {
		Configuration configuration = getConfiguration();

		sensacellSerialPort = configuration
				.getRequiredPropertyString(CONFIGURATION_SENSACELL_SENSACELL_PORT);
		sensorThreshold = configuration.getPropertyInteger(
				CONFIGURATION_SENSACELL_SENSACELL_THRESHOLD,
				DEFAULT_SENSACELL_THRESHOLD);
		sensorHysteresis = configuration.getPropertyInteger(
				CONFIGURATION_SENSACELL_SENSACELL_HYSTERESIS,
				DEFAULT_SENSACELL_HYSTERESIS);
		leftSensor = configuration.getPropertyInteger(
				CONFIGURATION_SENSACELL_GESTURE_SENSOR_LEFT,
				DEFAULT_LEFT_SENSOR);
		rightSensor = configuration.getPropertyInteger(
				CONFIGURATION_SENSACELL_GESTURE_SENSOR_RIGHT,
				DEFAULT_RIGHT_SENSOR);

		leftTrigger.setThreshold(sensorThreshold).setHysteresis(
				sensorHysteresis);
		rightTrigger.setThreshold(sensorThreshold).setHysteresis(
				sensorHysteresis);

	}

	/**
	 * Update the activity state.
	 */
	private void update() {
		// Pretty simple at the moment.
		sensacell.update();
	}

	/**
	 * Called when sensacell updates.
	 * 
	 * @param cell
	 *            the sensacell
	 */
	public void processData(SensacellDriver cell) {

		// sensor layout
		//
		// 0 1 2 3
		// 4 5 6 7
		// 8 9 10 11
		// 12 13 14 15

		int[] data = cell.getReads();
		leftTrigger.update(data[leftSensor]);
		rightTrigger.update(data[rightSensor]);

	}

	/**
	 * Listener when left trigger point triggers.
	 * 
	 * @param event
	 */
	public void onLeftChange(TriggerEventType event) {
		if (event == TriggerEventType.RISING) {
			leftSensorOn = true;
			sendGestureData();
		} else if (event == TriggerEventType.FALLING) {
			leftSensorOn = false;
		}
	}

	/**
	 * Listener when right trigger point triggers.
	 * 
	 * @param event
	 */
	void onRightChange(TriggerEventType event) {
		if (event == TriggerEventType.RISING) {
			rightSensorOn = true;
			sendGestureData();
		} else if (event == TriggerEventType.FALLING) {
			rightSensorOn = false;
		}
	}

	/**
	 * Broadcast gesture data.
	 */
	private void sendGestureData() {
		if (isActivated()) {
			int leftValue = 0;
			int rightValue = 0;

			if (leftSensorOn) {
				leftValue = 1;
			} else if (rightSensorOn) {
				rightValue = 1;
			}

			Map<String, Object> message = Maps.newHashMap();
			message.put("left", leftValue);
			message.put("right", rightValue);

			sendOutputJson("gestureOutput", message);
		}
	}

	/**
	 * Get the index of the left sensor on the sensacell.
	 * 
	 * @return
	 */
	public int getLeftSensor() {
		return leftSensor;
	}

	/**
	 * Set which cell is the left sensor.
	 * 
	 * @param index
	 *            index of the left sensor
	 */
	public void setLeftSensor(int index) {
		leftSensor = index;
	}

	/**
	 * Get the index of the right sensor on the sensacell.
	 * 
	 * @return
	 */
	public int getRightSensor() {
		return rightSensor;
	}

	/**
	 * Set which cell is the right sensor.
	 * 
	 * @param index
	 *            index of the left sensor
	 */
	public void setRightSensor(int index) {
		rightSensor = index;
	}
}