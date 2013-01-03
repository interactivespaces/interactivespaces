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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint.Parity;
import interactivespaces.service.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Driver for a Sensacell.
 * 
 * <p>
 * This class is not thread safe.
 * 
 * @author Keith M. Hughes
 */
public class SensacellDriver {

	/**
	 * The percentage of pixels in a rectangle which have to be active for the
	 * rectangle to be considered active.
	 */
	public static final double ACTIVE_RECTANGLE_PERCENTAGE = .5;

	/**
	 * ASCII character value for a carriage return.
	 */
	public static final byte CARRIAGE_RETURN = 0x0D;

	/**
	 * The default threshold value for an element to be considered active.
	 */
	public static final int SENSACELL_DEFAULT_THRESHOLD = 100;

	/**
	 * The length of time for the sensacell to reset, in milliseconds.
	 */
	public static final int SENSACELL_RESET_PERIOD = 8000;

	/**
	 * The length of time for reseting a sensor, in milliseconds.
	 */
	public static final int SENSACELL_SENSOR_RESET_PERIOD = 1000;

	/**
	 * The ID for referring to the master sensacell module in an array of
	 * modules.
	 */
	public static final byte SENSACELL_MASTER_MODULE = 0x01;

	/**
	 * The read mode setting for the sensor to be read in digital mode.
	 */
	public static final int SENSACELL_MODE_READ_DIGITAL = 0;

	/**
	 * The read mode setting for the sensor to be read in proportional mode.
	 */
	public static final int SENSACELL_MODE_READ_PROPORTIONAL = 1;

	/**
	 * The setting for the sensor to update at a 10 Hz rate.
	 */
	public static final int SENSACELL_SPEED_10HZ = 0;

	/**
	 * The setting for the sensor to update at a 20 Hz rate.
	 */
	public static final int SENSACELL_SPEED_20HZ = 1;

	/**
	 * A normal read for the sensacell.
	 */
	public static final int SENSACELL_NORMAL_READ = 0;

	/**
	 * A latched read for the sensacell.
	 */
	public static final int SENSACELL_LATCHED_READ = 1;

	/**
	 * A mapping of digits to their associated hex character.
	 */
	public static final byte[] NUMBER_TO_HEX = { (byte) '0', (byte) '1',
			(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
			(byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

	/**
	 * Name of the serial port to talk to.
	 */
	private String portName;

	/**
	 * Serial communication endpoint to the SensacellDriver.
	 */
	private SerialCommunicationEndpoint cellEndpoint;

	/**
	 * {@true} if the cell has completed setup.
	 */
	private boolean setupComplete;

	/**
	 * ID of the cell being read.
	 */
	private int id;

	/**
	 * {@code true} if the system is resetting.
	 */
	private boolean systemResetting;

	/**
	 * The system time when a system reset started.
	 */
	private long systemResetStartTime;

	/**
	 * {@code true} if the sensor is resetting.
	 */
	private boolean sensorResetting;

	/**
	 * The system time when a sensor reset started.
	 */
	private long sensorResetStartTime;

	/**
	 * {@code true} if a mode change happened.
	 */
	private boolean modeChanged;

	/**
	 * The sensor data from the cell.
	 */
	private int[] sensorData = new int[16];

	/**
	 * The threshold at which a sensor is considered to be active.
	 */
	private int sensorThreshold;

	/**
	 * The system time that the last read was done, in milliseconds.
	 */
	private long lastTimeRead;

	/**
	 * The read mode for the cell.
	 */
	private int readMode;

	/**
	 * The latch mode for the cell.
	 */
	private int latchMode;

	/**
	 * The speed at which the sensor hardware is scanning.
	 */
	private int sensorSpeed;

	/**
	 * The rate at which the cell hardware is updating, and so valid to be read,
	 * in ms.
	 */
	private long updateRate;

	/**
	 * Buffer for storing outgoing commands to the sensacell.
	 */
	private byte[] command = new byte[8];

	/**
	 * Buffer for doing a digital data read.
	 * 
	 * <p>
	 * This data is packed, 1 bit for each of 16 sensors in 4 bytes.
	 */
	private byte[] digitalReadBuffer = new byte[5];

	/**
	 * Buffer for doing a proportional data read.
	 * 
	 * <p>
	 * This is a byte for each of 16 sensors terminated with a carriage return.
	 */
	private byte[] proportionalReadBuffer = new byte[17];

	/**
	 * The listeners for the cell.
	 */
	private List<SensacellListener> listeners = Lists.newArrayList();

	/**
	 * Logger for the cell.
	 */
	private Log log;

	/**
	 * Construct a driver.
	 * 
	 * @param portName
	 *            the port the driver will be on
	 * @param log
	 *            the log to log to
	 */
	public SensacellDriver(String portName, Log log) {
		this.log = log;

		setupComplete = false;
		systemResetting = false;
		sensorResetting = false;
		modeChanged = false;

		id = SENSACELL_MASTER_MODULE;

		readMode = SENSACELL_MODE_READ_DIGITAL;
		latchMode = SENSACELL_NORMAL_READ;

		sensorSpeed = SENSACELL_SPEED_10HZ;
		updateRate = 100;

		setSensorThreshold(SENSACELL_DEFAULT_THRESHOLD);

		Arrays.fill(sensorData, 255);

		lastTimeRead = 0;
	}

	/**
	 * Set up the SensacellDriver.
	 * 
	 * @param spaceEnvironment
	 *            the space environment the driver runs under
	 * 
	 * @return {@code true} if setup was successful
	 */
	public boolean startup(InteractiveSpacesEnvironment spaceEnvironment) {
		SerialCommunicationEndpointService communicationEndpointService = spaceEnvironment
				.getServiceRegistry().getRequiredService(
						SerialCommunicationEndpointService.SERVICE_NAME);

		try {
			cellEndpoint = communicationEndpointService
					.newSerialEndpoint(portName);
			cellEndpoint.setBaud(230400).setDataBits(8).setStopBits(1)
					.setParity(Parity.NONE);
			cellEndpoint.startup();

			setupComplete = true;

			return setupComplete;
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Cannot set up port %s for sensacell", portName), e);
		}
	}

	/**
	 * Shut down the sensacell.
	 */
	public void shutdown() {
		cellEndpoint.shutdown();
		cellEndpoint = null;
	}

	/**
	 * Add a new listener to the cell.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addListener(SensacellListener listener) {
		listeners.add(listener);
	}

	/**
	 * Update the sensacell's state.
	 */
	public void update() {
		// Can't update if setup isn't complete.
		if (!setupComplete) {
			return;
		}

		try {
			// while the system is resetting, sensor data is useless so don't
			// read until the reset period is complete
			if (systemResetting) {
				if ((System.currentTimeMillis() - systemResetStartTime) >= SENSACELL_RESET_PERIOD) {
					systemResetting = false;
					cellEndpoint.flush();

					// restore settings
					sendSensorReadModeCommand(readMode, sensorSpeed, latchMode);
				} else {
					return;
				}
			}

			if (modeChanged) {
				modeChanged = false;
				// read mode was changed so flush the port
				cellEndpoint.flush();
			}

			if (sensorResetting) {
				if ((System.currentTimeMillis() - sensorResetStartTime) >= SENSACELL_SENSOR_RESET_PERIOD) {
					sensorResetting = false;
					cellEndpoint.flush();

					// restore settings (not sure this is necessary, but playing
					// it safe just in case
					sendSensorReadModeCommand(readMode, sensorSpeed, latchMode);
				} else {
					return;
				}
			}

			// the sensor is only updated at a rate of 10Hz or 20Hz (depending
			// on value of sensorSpeed)
			long timeDifference = System.currentTimeMillis() - lastTimeRead;
			if (timeDifference >= updateRate) {
				if (readMode == SENSACELL_MODE_READ_DIGITAL) {
					sendDigitalReadCommand(id);
					readDigitalData();
					notifyListenersOfUpdate();
				} else if (readMode == SENSACELL_MODE_READ_PROPORTIONAL) {
					sendProportionalReadCommand(id);
					readProporptionalData();
					notifyListenersOfUpdate();
				}

				lastTimeRead = System.currentTimeMillis();
			}
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Could not update the sensacell values", e);
		}
	}

	/**
	 * Notify all listeners of an update event.
	 */
	private void notifyListenersOfUpdate() {
		for (SensacellListener listener : listeners) {
			listener.onSensacellUpdate(this);
		}
	}

	/**
	 * Draw the current sensor data to the cell to visualize what is sensed.
	 */
	public void drawToSensacell() {
		try {
			sendGlobalWriteCommand(0x01, 0x00);
			for (int i = 0; i < 16; i++) {
				cellEndpoint.write(sensorData[i]);
				cellEndpoint.write(sensorData[i]);
				cellEndpoint.write(sensorData[i]);
			}
			cellEndpoint.write(CARRIAGE_RETURN);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Cannot draw sensor data to sensacell", e);
		}
	}

	/**
	 * Read the data from the SensacellDriver.
	 * 
	 * @return {@code true} if the data was read correctly, {@code false}
	 *         otherwise.
	 */
	private boolean readDigitalData() throws IOException {
		if (!readSerialData(digitalReadBuffer))
			return false;

		// get reads into more usable 16 part array
		placeInSensorData(hexToInt(digitalReadBuffer[3]), 15);
		placeInSensorData(hexToInt(digitalReadBuffer[2]), 11);
		placeInSensorData(hexToInt(digitalReadBuffer[1]), 7);
		placeInSensorData(hexToInt(digitalReadBuffer[0]), 3);

		return true;
	}

	/**
	 * Unpack a set of data into the stored sensor data.
	 * 
	 * @param value
	 *            the packed data
	 * @param pos
	 *            where the data should be stored in the data array
	 */
	private void placeInSensorData(int value, int pos) {
		for (int i = 0; i < 4; i++, pos--) {
			sensorData[pos] = (value & 0x01);
			value >>= 1;
		}
	}

	/**
	 * read a sequence of proportional data from the SensacellDriver.
	 * 
	 * @return {@code true} if the data was read correctly, {@code false}
	 *         otherwise.
	 */
	private boolean readProporptionalData() throws IOException {
		if (!readSerialData(proportionalReadBuffer))
			return false;

		for (int i = 0; i < 16; i++) {
			sensorData[i] = (int) (hexToInt(proportionalReadBuffer[i]) / 16.0f * 255);
		}

		return true;
	}

	/**
	 * Read serial data from the cell into the buffer
	 * 
	 * @param buffer
	 *            the buffer to store the data in
	 * 
	 * @return {@code true} if of the correct length and properly terminated.
	 * 
	 * @throws IOException
	 */
	private boolean readSerialData(byte[] buffer) throws IOException {
		int offset = 0;
		int toRead = buffer.length;
		while (toRead > 0) {
			int readAmt = cellEndpoint.read(buffer, offset, toRead);
			if (readAmt == -1) {
				log.info("Reached EOF of sensacell stream");
				return false;
			}
			offset += readAmt;
			toRead -= readAmt;
		}

		if (buffer[buffer.length - 1] != CARRIAGE_RETURN) {
			log.warn("No carriage return at end of sensacell packet");
			return false;
		}

		return true;
	}

	/**
	 * Takes a HEX byte and returns the integer it represents.
	 * 
	 * @param hexByte
	 *            the hex byte, in ASCII
	 * 
	 * @return the integer
	 */
	private int hexToInt(byte hexByte) {
		for (int i = 0; i < NUMBER_TO_HEX.length; i++) {
			if (NUMBER_TO_HEX[i] == hexByte)
				return i;
		}

		throw new InteractiveSpacesException(String.format(
				"Illegal hex byte %d", (int) hexByte));
	}

	/**
	 * Set the ID (address) of the sensacell module to be monitoring
	 * 
	 * <p>
	 * This is only necessary if looking at an entire grid.
	 * 
	 * @param id
	 *            ID (0 - 255)
	 */
	public void setId(int id) {
		if (id >= 0 && id <= 255) {
			this.id = id;
		} else {
			this.id = SENSACELL_MASTER_MODULE;
			// to do: or throw invalid id error
		}
	}

	/**
	 * Set the threshold for the {@link #isActive(int, int)} method
	 * 
	 * @param threshold
	 *            the threshold for the isActive method (0 - 255)
	 */
	public void setSensorThreshold(int threshold) {
		if (threshold >= 0 && threshold <= 255) {
			sensorThreshold = threshold;
		} else {
			sensorThreshold = SENSACELL_DEFAULT_THRESHOLD;
		}
	}

	/**
	 * Get the current sensor threshold value.
	 * 
	 * @return the threshold, between 0 and 255
	 */
	public int getSensorThreshold() {
		return sensorThreshold;
	}

	/**
	 * Set the speed (update rate) for the sensor.
	 * 
	 * @param speed
	 *            update rate of the sensor, valid values are:
	 *            {@link #SENSACELL_SPEED_10HZ} and
	 *            {@link #SENSACELL_SPEED_20HZ}.
	 */
	public void setSensorSpeed(int speed) {
		if (speed == SENSACELL_SPEED_10HZ || speed == SENSACELL_SPEED_20HZ) {
			sensorSpeed = speed;

			updateRate = speed == SENSACELL_SPEED_10HZ ? 100 : 50;
			try {
				sendSensorReadModeCommand(readMode, sensorSpeed, latchMode);
			} catch (IOException e) {
				throw new InteractiveSpacesException(
						"Cannot set speed on SensacellDriver", e);
			}
		} else {
			// invalid speed value
		}
	}

	/**
	 * Set the read mode for the sensor
	 * 
	 * @param mode
	 *            read mode of the senso, valid values are
	 *            {@link #SENSACELL_NORMAL_READ} and
	 *            {@link #SENSACELL_LATCHED_READ}
	 */
	public void setReadMode(int mode) {
		if (mode == SENSACELL_NORMAL_READ || mode == SENSACELL_LATCHED_READ) {
			readMode = mode;
			try {
				sendSensorReadModeCommand(readMode, sensorSpeed, latchMode);
			} catch (IOException e) {
				throw new InteractiveSpacesException(
						"Cannot set read mode on sensacell", e);
			}
			modeChanged = true;
		} else {
			// invalid readMode value
		}
	}

	/**
	 * Set the latch mode for the sensor
	 * 
	 * @param latch
	 *            latch mode of the sensor, valid values are
	 *            {@link #SENSACELL_MODE_READ_DIGITAL} and
	 *            {@link #SENSACELL_MODE_READ_PROPORTIONAL}
	 */
	public void setLatchMode(int latch) {
		if (latch == SENSACELL_MODE_READ_DIGITAL
				|| latch == SENSACELL_MODE_READ_PROPORTIONAL) {
			latchMode = latch;
			try {
				sendSensorReadModeCommand(readMode, sensorSpeed, latchMode);
			} catch (IOException e) {
				throw new InteractiveSpacesException(
						"Cannot set latch mode on SensacellDriver", e);
			}
		} else {
			// invalid latch value
		}
	}

	/**
	 * @return the current read mode of the system
	 */
	public int getReadMode() {
		return readMode;
	}

	/**
	 * @return the current sensor speed (update rate) of the system
	 */
	public int getSensorSpeed() {
		return sensorSpeed;
	}

	/**
	 * @return the current latch mode of the system
	 */
	public int getLatchMode() {
		return latchMode;
	}

	/**
	 * @return a vector containing the current sensor values
	 */
	public int[] getReads() {
		// TODO(keith): Return copy?
		return sensorData;
	}

	/**
	 * @return the average of all 16 sensor values
	 */
	public int getAvgRead() {
		int avg = 0;
		for (int i = 0; i < 16; i++)
			avg += sensorData[i];
		avg /= 16;

		return avg;
	}

	/**
	 * Get all sensor values as a string.
	 * 
	 * @return a string containing all 16 sensor values
	 */
	public String getReadsAsString() {
		StringBuilder readString = new StringBuilder();

		readString.append(Integer.toString(sensorData[0]));
		for (int i = 1; i < 16; i++) {
			readString.append(':').append(Integer.toString(sensorData[i]));
		}

		return readString.toString();
	}

	/**
	 * Tests if the area defined by the point (x, y) is active
	 * 
	 * @param x
	 *            x coordinate of the point to test
	 * @param y
	 *            y coordinate of the point to test
	 * @return true if object present, false if no object present (within range
	 *         defined by sensorThreshold)
	 */
	boolean isActive(int x, int y) {
		return sensorData[y * 4 + x] > sensorThreshold;
	}

	/**
	 * Tests if the area defined by the rectangle is active.
	 * 
	 * <p>
	 * 50% of the points must have {@link #isActive(int, int)} returning
	 * {@code true}.
	 * 
	 * @param r
	 *            rectangle defining area to be tested
	 * @return {@code true} if sensing inside the rectangle
	 */
	public boolean isActive(Rectangle r) {
		int numActive = 0;
		for (int x = r.x; x < r.x + r.width; x++) {
			for (int y = r.y; y < r.y + r.height; y++) {
				if (isActive(x, y))
					numActive++;
			}
		}

		return numActive > r.width * r.height * ACTIVE_RECTANGLE_PERCENTAGE;
	}

	/**
	 * Request a read from the specified address.
	 * 
	 * @param address
	 *            The address to be read.
	 * @throws IOException
	 */
	private void sendDigitalReadCommand(int address) throws IOException {
		command[0] = (byte) 'r';
		command[1] = NUMBER_TO_HEX[(address >> 4) & 0x0f];
		command[2] = NUMBER_TO_HEX[address & 0x0f];
		command[3] = CARRIAGE_RETURN;

		cellEndpoint.write(command, 0, 4);
	}

	/**
	 * Request a proportional read from the specified address.
	 * 
	 * @param address
	 *            The address to be read.
	 * @throws IOException
	 */
	private void sendProportionalReadCommand(int address) throws IOException {
		command[0] = (byte) 'p';
		command[1] = NUMBER_TO_HEX[(address >> 4) & 0x0f];
		command[2] = NUMBER_TO_HEX[address & 0x0f];
		command[3] = CARRIAGE_RETURN;

		cellEndpoint.write(command, 0, 4);
	}

	/**
	 * Send the command for setting read modes to the sensacell.
	 * 
	 * @param readMode
	 *            the new read mode
	 * @param speed
	 *            the new speed for updates
	 * @param latch
	 *            whether or not the cell should latch
	 * 
	 * @throws IOException
	 */
	private void sendSensorReadModeCommand(int readMode, int speed, int latch)
			throws IOException {
		int ctrlBits = (latch << 2) | (speed << 1) | readMode;

		command[0] = (byte) '0';
		command[1] = (byte) 'B';
		command[2] = NUMBER_TO_HEX[(ctrlBits >> 4) & 0x0f];
		command[3] = NUMBER_TO_HEX[ctrlBits & 0x0f];
		command[4] = (byte) 'a';
		command[5] = (byte) '0';
		command[6] = (byte) '0';
		command[7] = CARRIAGE_RETURN;

		cellEndpoint.write(command, 0, 8);
	}

	/**
	 * Send the global write command to the sensacell.
	 * 
	 * @param numModules
	 *            the number of modules in the full array
	 * @param address
	 *            which module to send to
	 * 
	 * @throws IOException
	 */
	private void sendGlobalWriteCommand(int numModules, int address)
			throws IOException {
		command[0] = (byte) '0';
		command[1] = (byte) '1';
		command[2] = NUMBER_TO_HEX[(numModules >> 4) & 0x0f];
		command[3] = NUMBER_TO_HEX[numModules & 0x0f];
		command[4] = (byte) 'a';
		command[5] = NUMBER_TO_HEX[(address >> 4) & 0x0f];
		command[6] = NUMBER_TO_HEX[address & 0x0f];
		command[7] = CARRIAGE_RETURN;

		cellEndpoint.write(command, 0, 8);
	}

	/**
	 * Reset and recalibrate all sensacell modules
	 * 
	 * @throws IOException
	 */
	private void sensorReset() throws IOException {
		// reset all modules
		sensorReset(0x00);
	}

	/**
	 * Reset and recalibrate the addressed sensacell module
	 * 
	 * @param address
	 *            address of the sensacell module to be reset
	 * 
	 * @throws IOException
	 */
	private void sensorReset(int address) throws IOException {
		sensorResetting = true;
		sensorResetStartTime = System.currentTimeMillis();

		command[0] = (byte) '0';
		command[1] = (byte) '3';
		command[2] = (byte) '0';
		command[3] = (byte) '0';
		command[4] = (byte) 'a';
		command[5] = NUMBER_TO_HEX[(address >> 4) & 0x0f];
		command[6] = NUMBER_TO_HEX[address & 0x0f];
		command[7] = CARRIAGE_RETURN;

		cellEndpoint.write(command, 0, 8);
	}

	/**
	 * Perform a full system reset. This is essential the same as powering down
	 * and repowering the device.
	 * 
	 * @throws IOException
	 */
	public void systemReset() {
		try {
			command[0] = (byte) '1';
			command[1] = (byte) '3';
			command[2] = (byte) 'E';
			command[3] = (byte) 'A';
			command[4] = (byte) 'a';
			command[5] = (byte) '0';
			command[6] = (byte) '0';
			command[7] = CARRIAGE_RETURN;

			// Give the cell time to set up if first turning on.
			InteractiveSpacesUtilities.delay(5000);

			systemResetting = true;
			systemResetStartTime = System.currentTimeMillis();

			cellEndpoint.write(command, 0, 8);
			cellEndpoint.flush();
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not send system reset command to sensacell", e);
		}
	}

	/**
	 * Turn off all illumintated LEDs on all sensacells
	 * 
	 * @throws IOException
	 */
	public void blackOut() throws IOException {
		// send write command
		sendGlobalWriteCommand(0x01, 0x00);

		Random r = new Random();
		// immediate follow by RGB values
		for (int i = 0; i < 16; i++) {
			cellEndpoint.write(r.nextInt(255));
			cellEndpoint.write(r.nextInt(255));
			cellEndpoint.write(r.nextInt(255));
		}
		cellEndpoint.write(CARRIAGE_RETURN);
		cellEndpoint.flush();

		// saveCurrentState();
	}

	/**
	 * Save the current state of the sensacell
	 * 
	 * @throws IOException
	 */
	private void saveCurrentState() throws IOException {
		// to do: update for all 4 write types
		command[0] = (byte) '1';
		command[1] = (byte) '7';
		command[2] = (byte) '0';
		command[3] = (byte) '0';
		command[4] = (byte) 'a';
		command[5] = (byte) '0';
		command[6] = (byte) '1';
		command[7] = CARRIAGE_RETURN;

		cellEndpoint.write(command, 0, 8);
		cellEndpoint.flush();
	}

	/**
	 * Listener for events from a sensacell.
	 * 
	 * @author Keith M. Hughes
	 */
	public static interface SensacellListener {

		/**
		 * The sensacell has updated.
		 * 
		 * @param cell
		 *            The cell which updated.
		 */
		void onSensacellUpdate(SensacellDriver cell);
	}
}