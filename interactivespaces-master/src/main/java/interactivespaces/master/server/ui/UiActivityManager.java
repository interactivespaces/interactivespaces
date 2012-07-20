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

package interactivespaces.master.server.ui;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;

import java.io.InputStream;
import java.util.Map;

/**
 * A manager for working with activities for UIs.
 * 
 * <p>
 * This is mostly for crud operations. To work with activities on controllers,
 * see {@link UiControllerManager}.
 * 
 * @author Keith M. Hughes
 */
public interface UiActivityManager {

	/**
	 * Message key for non-existent activities.
	 */
	public static final String MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN = "space.domain.activity.unknown";

	/**
	 * Message key for non-existent live activities.
	 */
	public static final String MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN = "space.domain.liveactivity.unknown";

	/**
	 * Message key for non-existent live activity groups.
	 */
	public static final String MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN = "space.domain.liveactivitygroup.unknown";

	/**
	 * Start the manager up.
	 */
	void startup();

	/**
	 * Shut the manager down.
	 */
	void shutdown();
	
	/**
	 * Save an activity.
	 * 
	 * <p>
	 * Includes saving the activity file in the activity repository.
	 * 
	 * @param activity
	 *            The data for the activity.
	 * @param activityFile
	 *            Input stream containing the contents of the activity.
	 */
	Activity saveActivity(SimpleActivity activity, InputStream activityFile);

	/**
	 * Delete an activity from the activity repository.
	 * 
	 * <p>
	 * Does nothing if there is no activity with the given ID.
	 * 
	 * @param id
	 *            ID of the activity.
	 */
	void deleteActivity(String id);

	/**
	 * Modify a activity's metadata.
	 * 
	 * <p>
	 * The command map contains a field called command. This field will be one
	 * of
	 * 
	 * <ul>
	 * <li>replace - data contains a map, replace the entire metadata map with
	 * the map</li>
	 * <li>modify - data contains a map, replace just the fields found in the
	 * map with the values found in the map</li>
	 * <li>delete - data contains a list of keys, remove all keys found in data</li>
	 * </ul>
	 * 
	 * @param id
	 *            ID of the activity
	 * @param metadataCommand
	 *            the modification command
	 * 
	 * @return a JSON response object
	 */
	Map<String, Object> updateActivityMetadata(String id,
			Map<String, Object> metadataCommand);

	/**
	 * Delete an installed activity from the activity repository.
	 * 
	 * <p>
	 * Does nothing if there is no installed activity with the given ID.
	 * 
	 * @param id
	 *            ID of the installed activity.
	 */
	void deleteLiveActivity(String id);

	/**
	 * Get the configuration of a live activity.
	 * 
	 * @param id
	 *            ID of the live activity
	 * 
	 * @return the configuration
	 */
	Map<String, String> getLiveActivityConfiguration(String id);

	/**
	 * Configure a live activity.
	 * 
	 * @param id
	 *            ID of the live activity
	 * @param map
	 *            the new configuration
	 */
	void configureLiveActivity(String id, Map<String, String> map);

	/**
	 * Get basic information about an activity.
	 * 
	 * @param activity
	 *            the activity
	 * 
	 * @return a JSON coded object giving the basic information
	 */
	Map<String, Object> getBasicActivityJsonData(Activity activity);

	/**
	 * Get basic information about a space controller.
	 * 
	 * @param controller
	 *            the space controller
	 * 
	 * @return a JSON coded object giving the basic information
	 */
	Map<String, Object> getBasicSpaceControllerJsonData(
			SpaceController controller);

	/**
	 * Get the JSON data for a live activity.
	 * 
	 * @param activity
	 *            the live activity to get data from
	 * @param activityData
	 *            the map where the data will be store
	 */
	void getLiveActivityViewJsonData(LiveActivity activity,
			Map<String, Object> activityData);

	/**
	 * Add in all data needed for the JSON status of the live activity
	 * 
	 * @param activity
	 *            the live activity
	 * @param data
	 *            the JSON map to add the data into
	 */
	void getLiveActivityStatusJsonData(LiveActivity activity,
			Map<String, Object> data);

	/**
	 * Get the JSON data describing a live activity group.
	 * 
	 * @param liveActivityGroup
	 *            the live activity group
	 * 
	 * @return the JSON data describing the group
	 */
	Map<String, Object> getLiveActivityGroupJsonData(
			LiveActivityGroup liveActivityGroup);

	/**
	 * Add in the basic group data used in API calls.
	 * 
	 * @param group
	 *            the group to get the data from
	 * @param data
	 *            the JSON data being collected
	 */
	void getBasicLiveActivityGroupJsonData(LiveActivityGroup group,
			Map<String, Object> data);

	/**
	 * Modify a live activity's metadata.
	 * 
	 * <p>
	 * The command map contains a field called command. This field will be one
	 * of
	 * 
	 * <ul>
	 * <li>replace - data contains a map, replace the entire metadata map with
	 * the map</li>
	 * <li>modify - data contains a map, replace just the fields found in the
	 * map with the values found in the map</li>
	 * <li>delete - data contains a list of keys, remove all keys found in data</li>
	 * </ul>
	 * 
	 * @param id
	 *            ID of the live activity
	 * @param metadataCommand
	 *            the modification command
	 * 
	 * @return a JSON response object
	 */
	Map<String, Object> updateLiveActivityMetadata(String id,
			Map<String, Object> metadataCommand);

	/**
	 * Delete an activity group from the activity repository.
	 * 
	 * <p>
	 * Does nothing if there is no activity group with the given ID.
	 * 
	 * @param id
	 *            ID of the activity group.
	 */
	void deleteActivityGroup(String id);

	/**
	 * Modify a live activity group's metadata.
	 * 
	 * <p>
	 * The command map contains a field called command. This field will be one
	 * of
	 * 
	 * <ul>
	 * <li>replace - data contains a map, replace the entire metadata map with
	 * the map</li>
	 * <li>modify - data contains a map, replace just the fields found in the
	 * map with the values found in the map</li>
	 * <li>delete - data contains a list of keys, remove all keys found in data</li>
	 * </ul>
	 * 
	 * @param id
	 *            ID of the live activity
	 * @param metadataCommand
	 *            the modification command
	 * 
	 * @return a JSON response object
	 */
	Map<String, Object> updateLiveActivityGroupMetadata(String id,
			Map<String, Object> metadataCommand);
}
