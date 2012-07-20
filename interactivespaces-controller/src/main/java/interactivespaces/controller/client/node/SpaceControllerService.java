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

package interactivespaces.controller.client.node;

/**
 * A service for controlling Interactive Spaces controllers.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerService {
	
	/**
	 * Start up an Interactive Spaces controller with the given ID.
	 * 
	 * @param controllerId ID of the controller to start
	 */
	void startController(String controllerId);

	/**
	 * Shut down an Interactive Spaces controller with the given ID.
	 * 
	 * @param controllerId ID of the controller to shut down
	 */
	void shutdownController(String controllerId);
}
