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

import interactivespaces.InteractiveSpacesException;
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
	 * direct access to the file system, but rather look up an activity by its
	 * identifying name and version. but then we need a web server handler that
	 * can look up file names.
	 * 
	 * @return the base location of the repository
	 */
	String getRepositoryBaseLocation();

	/**
	 * Get the name the activity has in the repository.
	 * 
	 * @param activity
	 *            the activity in the repository
	 * 
	 * @return the fully qualified name of the activity
	 */
	String getRepositoryActivityName(Activity activity);

	/**
	 * Does the repository contain an activity?
	 * 
	 * @param activity
	 *            the activity to be checked
	 * 
	 * @return {@code true} if the repository contains the activity
	 */
	boolean containsActivity(Activity activity);

	/**
	 * Stage an activity.
	 * 
	 * @param activityStream
	 *            a stream of the incoming activity
	 * 
	 * @return an opaque handle on the activity, do not make any assumptions on
	 *         this handle, it can change
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
	 * Get an {@link InputStream} for the description file in the staged
	 * activity.
	 * 
	 * @param stageHandle
	 *            the handle which was returned by
	 *            {@link #stageActivity(InputStream)}
	 * 
	 * @return the input stream for the description file for the requested
	 *         staged activity
	 * 
	 * @throws InteractiveSpacesException
	 *             if the stage handle is invalid or the activity contains no
	 *             description file
	 */
	InputStream getStagedActivityDescription(String stageHandle);

	/**
	 * Add an activity to the repository.
	 * 
	 * @param activity
	 *            the activity the stream represents
	 * @param activityStream
	 *            stream containing the activity
	 */
	void addActivity(Activity activity, String stageHandle);

}
