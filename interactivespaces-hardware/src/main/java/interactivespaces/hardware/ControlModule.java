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

package interactivespaces.hardware;

import interactivespaces.InteractiveSpacesException;


/**
 * Factory for device controllers from a specific controller.
 *
 * @author Keith M. Hughes
 */
public interface ControlModule {

	/**
	 * Start the connection to all connected devices.
	 * 
	 * @throws InteractiveSpacesException Something bad happened.
	 */
	void start();

	/**
	 * Stop the connection to all connected devices.
	 * 
	 * @throws InteractiveSpacesException Something bad happened.
	 */
	void stop();

	/**
	 * Create a new light.
	 * 
	 * @param position The position of the light in the module.
	 * @param initialState The initial state of the light.
	 * @return
	 */
	Light createLight(int position, boolean initialState);
}