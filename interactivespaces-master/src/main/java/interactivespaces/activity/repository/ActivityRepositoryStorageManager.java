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

package interactivespaces.activity.repository;

import interactivespaces.domain.basic.Activity;

import java.io.InputStream;


/**
 * A storage manager for the Interactive Spaces activity repository.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityRepositoryStorageManager {
	
	/**
	 * Start the storage manager up.
	 */
	void startup();

	/**
	 * Shut the storage manager down.
	 */
	void shutdown();

	/**
	 * 
	 * Get the base location of the repository.
	 * 
	 * TODO(keith): This must go away, we do not want the HTTP server to have
	 * direct access to the file system, but rather look up an activity by
	 * its identifying name and version. but then we need a web server handler
	 * that can look up file names.
	 * 
	 * @return
	 */
	String getRepositoryBaseLocation();

	/**
	 * Get the name the activity has in the repository.
	 * 
	 * @param activity
	 *            The activity in the repository.
	 * @return The fully qualified name of the activity.
	 */
	String getRepositoryActivityName(Activity activity);

	/**
	 * Add an activity to the repository.
	 * 
	 * @param activity
	 *            The activity to be represented.
	 * @returns True if the repository contains the activity, fals
	 */
	boolean containsActivity(Activity activity);

	/**
	 * Stage an activity.
	 * 
	 * @param activityStream
	 *            A stream of the incoming activity.
	 * 
	 * @return An opaque handle on the activity. Do not make any assumptions
	 *         on this handle, it can change.
	 */
	String stageActivity(InputStream activityStream);

	/**
	 * Remove a staged activity from the manager.
	 * 
	 * @param stageHandle
	 *            The handle which was returned by
	 *            {@link #stageActivity(InputStream)}
	 */
	void removeStagedActivity(String stageHandle);

	/**
	 * Get an {@link InputStream} for the description file in the staged activity.
	 * 
	 * @param stageHandle
	 * 		The handle which was returned by {@link #stageActivity(InputStream)
	 * 
	 * @return		
	 */
	InputStream getStagedActivityDescription(String stageHandle);

	/**
	 * Add an activity to the repository.
	 * 
	 * @param activity
	 *            The activity the stream represents.
	 * 
	 * @param activityStream
	 *            Stream containing the activity.
	 */
	void addActivity(Activity activity, String stageHandle);

}
