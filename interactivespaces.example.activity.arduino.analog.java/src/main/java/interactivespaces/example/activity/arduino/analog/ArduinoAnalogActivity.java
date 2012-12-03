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

/**
 * A simple Interactive Spaces Java-based activity which
 * communicates with an Arduino sketch which reads an
 * analog port and sends its value over a serial connection.
 * 
 * <p>
 * The Arduino sends the values as raw bytes, not a string.
 * The high order byte is transmitted first. The serial line
 * is set for 9600 baud.
 * 
 * @author Keith M. Hughes
 */
public class ArduinoAnalogActivity extends BaseActivity {

    @Override
    public void onActivitySetup() {
        getLog().info("Activity interactivespaces.example.activity.arduino.analog.java setup");
    }

	@Override
	public void onActivityStartup() {
		getLog().info("Activity interactivespaces.example.activity.arduino.analog.java startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Activity interactivespaces.example.activity.arduino.analog.java activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Activity interactivespaces.example.activity.arduino.analog.java deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Activity interactivespaces.example.activity.arduino.analog.java shutdown");
	}

    @Override
    public void onActivityCleanup() {
        getLog().info("Activity interactivespaces.example.activity.arduino.analog.java cleanup");
    }
}
