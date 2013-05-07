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

import interactivespaces.activity.ActivityFilesystem;

import java.io.File;
import java.util.List;

/**
 * A storage manager for activities on the controller.
 *
 * @author Keith M. Hughes
 */
public interface ActivityStorageManager {

	/**
	 * Start the storage manager up.
	 */
	void startup();

	/**
	 * Shut the storage manager down.
	 */
	void shutdown();
	
	/**
	 * Get the UUIDs of all installed live activities.
	 * 
	 * @return the UUIDs of all installed live activities
	 */
	List<String> getAllInstalledActivityUuids();

	/**
	 * Get where the activity is stored.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 * 
	 * @return root folder of the activity.
	 */
	File getBaseActivityLocation(String uuid);

	/**
	 * Get the filesystem for the activity.
	 * 
	 * <p>
	 * Will create the file system if it doesn't exist.
	 * 
	 * @param uuid
	 *            uuid of the activity
	 * 
	 * @return the filesystem
	 */
	ActivityFilesystem getActivityFilesystem(String uuid);

	/**
	 * Delete an activity installation.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 */
	void removeActivityLocation(String uuid);
	
	/**
	 * Clean out the tmp data directory for the activity.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 */
	void cleanTmpActivityDataDirectory(String uuid);
	
	/**
	 * Clean out the permanent data directory for the activity.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 */
	void cleanPermanentActivityDataDirectory(String uuid);
}
