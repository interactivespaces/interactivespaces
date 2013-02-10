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

package interactivespaces.time;

/**
 * Get the time.
 * 
 * @author Keith M. Hughes
 */
public interface TimeProvider {
	
	/**
	 * Start up the time provider.
	 */
	void startup();
	
	/**
	 * Shut down the time provider.
	 */
	void shutdown();

	/**
	 * Get the current time.
	 * 
	 * @return the difference in milliseconds between the current time and
	 *         midnight, Jan 1, 1970
	 */
	long getCurrentTime();
}
