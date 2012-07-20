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

package interactivespaces.controller.activity.installation;

import java.util.Date;

/**
 * Manages activity deployment on the controller.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityInstallationManager {

	/**
	 * Start the manager up.
	 */
	void startup();

	/**
	 * Shut the manager down.
	 */
	void shutdown();

	/**
	 * Copy a packed activity to the controller.
	 * 
	 * @param uuid
	 *            UUID of the activity.
	 * @param uri
	 *            URI for a zip file containing the activity.
	 */
	void copyActivity(String uuid, String uri);

	/**
	 * Install the activity to its final location.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 * @param activityIdentifyingName
	 *            identifying name of the activity
	 * @param version
	 *            version of the activity
	 *            
	 * @return the timestamp from when the activity was installed
	 */
	Date installActivity(String uuid, String activityIdentifyingName,
			String version);

	/**
	 * Delete an activity from the file system.
	 * 
	 * @param uuid
	 *            UUID of the activity.
	 */
	void removeActivity(String uuid);

	/**
	 * Delete the packed activity from file system.
	 * 
	 * <p>
	 * Does nothing if there is no activity with the given UUID.
	 * 
	 * @param uuid
	 *            UUID of the activity.
	 */
	void removePackedActivity(String uuid);

	/**
	 * Add in a new activity installation listener.
	 * 
	 * @param listener
	 *            The new listener.
	 */
	void addActivityInstallationListener(
			ActivityInstallationListener listener);

	/**
	 * Remove an activity installation listener.
	 * 
	 * <p>
	 * Does nothing if the listener wasn't there.
	 * 
	 * @param listener
	 *            The listener.
	 */
	void removeActivityInstallationListener(
			ActivityInstallationListener listener);
}
