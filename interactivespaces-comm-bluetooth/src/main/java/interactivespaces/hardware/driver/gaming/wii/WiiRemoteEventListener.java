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

package interactivespaces.hardware.driver.gaming.wii;

/**
 * A listener for Wii Remote events
 * 
 * @author Keith M. Hughes
 */
public interface WiiRemoteEventListener {
	
	/**
	 * No button is pushed
	 */
	public static final int VALUE_BUTTON_NONE = 0;
	
	/**
	 * The 1 button is pushed
	 */
	public static final int VALUE_BUTTON_1 = 2;
	
	/**
	 * The 2 button is pushed
	 */
	public static final int VALUE_BUTTON_2 = 1;
	
	/**
	 * The Home button is pushed
	 */
	public static final int VALUE_BUTTON_HOME = 128;
	
	/**
	 * The - button is pushed
	 */
	public static final int VALUE_BUTTON_MINUS = 16;
	
	/**
	 * The + is pushed
	 */
	public static final int VALUE_BUTTON_PLUS = 4096;
	
	/**
	 * The A button is pushed
	 */
	public static final int VALUE_BUTTON_A = 8;
	
	/**
	 * The button on the bottom of the remote is pushed
	 */
	public static final int VALUE_BUTTON_BOTTOM = 4;
	
	/**
	 * The left arrow button is pushed
	 */
	public static final int VALUE_BUTTON_LEFT = 256;
	
	/**
	 * The right arrow button is pushed
	 */
	public static final int VALUE_BUTTON_RIGHT = 512;
	
	/**
	 * The down arrow button is pushed
	 */
	public static final int VALUE_BUTTON_DOWN = 1024;
	
	/**
	 * The up arrow button is pushed
	 */
	public static final int VALUE_BUTTON_UP = 2048;

	/**
	 * A button event has happened.
	 * 
	 * <p>
	 * Button values are ORed together.
	 * 
	 * @param button
	 *            the button which had the event
	 */
	void onWiiRemoteButtonEvent(int button);

	/**
	 * A button and accelerometer event has happened.
	 * 
	 * <p>
	 * Button values are ORed together.
	 * 
	 * @param button
	 *            the current button push
	 * @param x
	 *            the x component of the accelerometer
	 * @param y
	 *            the y component of the accelerometer
	 * @param z
	 *            the z component of the accelerometer
	 */
	void onWiiRemoteButtonAccelerometerEvent(int button, double x, double y,
			double z);
}
