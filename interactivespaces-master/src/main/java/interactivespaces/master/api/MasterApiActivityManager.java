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

package interactivespaces.master.api;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.util.resource.ManagedResource;

import java.io.InputStream;
import java.util.Map;

/**
 * A Master API manager for working with activities.
 *
 * <p>
 * This is mostly for crud operations. To work with activities on controllers,
 * see {@link MasterApiControllerManager}.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiActivityManager extends ManagedResource {

  /**
   * Message key for non-existent activities.
   */
  String MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN = "space.domain.activity.unknown";

  /**
   * Message key for non-existent live activities.
   */
  String MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN = "space.domain.liveactivity.unknown";

  /**
   * Message key for non-existent live activity groups.
   */
  String MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN = "space.domain.liveactivitygroup.unknown";

  /**
   * Save an activity.
   *
   * <p>
   * Includes saving the activity file in the activity repository.
   *
   * @param activity
   *          the activity
   * @param activityStream
   *          the input stream containing the contents of the activity
   *
   * @return the updated activity
   */
  Activity saveActivity(SimpleActivity activity, InputStream activityStream);

  /**
   * Get all activities that meet a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return the master API message for all activities that meet the filter
   */
  Map<String, Object> getActivitiesByFilter(String filter);

  /**
   * Get basic information about an activity.
   *
   * @param activity
   *          the activity
   *
   * @return a Master API coded object giving the basic information
   */
  Map<String, Object> getBasicActivityApiData(Activity activity);

  /**
   * Delete an activity from the activity repository.
   *
   * <p>
   * Does nothing if there is no activity with the given ID.
   *
   * @param id
   *          ID of the activity.
   *
   * @return result of deleting activity
   */
  Map<String, Object> deleteActivity(String id);

  /**
   * Modify a activity's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in data</li>
   * </ul>
   *
   * @param id
   *          ID of the activity
   * @param metadataCommand
   *          the modification command
   *
   * @return a JSON response object
   */
  Map<String, Object> updateActivityMetadata(String id, Map<String, Object> metadataCommand);

  /**
   * Get all live activities that meet a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return the master API message for all live activities that meet the filter
   */
  Map<String, Object> getLiveActivitiesByFilter(String filter);

  /**
   * Get the view of a live activity.
   *
   * @param id
   *          ID for the live activity
   *
   * @return the master API message for the live activity view
   */
  Map<String, Object> getLiveActivityView(String id);

  /**
   * Delete an installed activity from the activity repository.
   *
   * <p>
   * Does nothing if there is no live activity with the given ID.
   *
   * @param id
   *          ID of the live activity
   *
   * @return API response to deletion
   */
  Map<String, Object> deleteLiveActivity(String id);

  /**
   * Get the configuration of a live activity.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the configuration
   */
  Map<String, Object> getLiveActivityConfiguration(String id);

  /**
   * Configure a live activity.
   *
   * @param id
   *          ID of the live activity
   * @param map
   *          the new configuration
   *
   * @return API response
   */
  Map<String, Object> configureLiveActivity(String id, Map<String, String> map);

  /**
   * Get basic information about a space controller.
   *
   * @param controller
   *          the space controller
   *
   * @return a Master API Response coded object giving the basic information
   */
  Map<String, Object> getBasicSpaceControllerApiData(SpaceController controller);

  /**
   * Get the Master API response data for a live activity.
   *
   * @param activity
   *          the live activity to get data from
   * @param activityData
   *          the map where the data will be store
   */
  void getLiveActivityViewApiData(LiveActivity activity, Map<String, Object> activityData);

  /**
   * Add in all data needed for the Master API response of the live activity.
   *
   * @param activity
   *          the live activity
   * @param data
   *          the JSON map to add the data into
   */
  void getLiveActivityStatusApiData(LiveActivity activity, Map<String, Object> data);

  /**
   * Get the view of a live activity group.
   *
   * @param id
   *          ID of the live activity group
   *
   * @return the Master API view of the group
   */
  Map<String, Object> getLiveActivityGroupView(String id);

  /**
   * Get the Master API response data describing a live activity group.
   *
   * @param liveActivityGroup
   *          the live activity group
   *
   * @return the API Response data describing the group
   */
  Map<String, Object> getLiveActivityGroupApiData(LiveActivityGroup liveActivityGroup);

  /**
   * Get all live activity groups that meet a filter.
   *
   * @param filter
   *          the filter for the group, can be {@code null}
   *
   * @return the master API message for all groups
   */
  Map<String, Object> getLiveActivityGroupsByFilter(String filter);

  /**
   * Modify a live activity's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in data</li>
   * </ul>
   *
   * @param id
   *          ID of the live activity
   * @param metadataCommand
   *          the modification command
   *
   * @return a JSON response object
   */
  Map<String, Object> updateLiveActivityMetadata(String id, Map<String, Object> metadataCommand);

  /**
   * Delete an activity group from the activity repository.
   *
   * <p>
   * Does nothing if there is no activity group with the given ID.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return API response
   */
  Map<String, Object> deleteActivityGroup(String id);

  /**
   * Modify a live activity group's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in data</li>
   * </ul>
   *
   * @param id
   *          ID of the live activity
   * @param metadataCommand
   *          the modification command
   *
   * @return a JSON response object
   */
  Map<String, Object> updateLiveActivityGroupMetadata(String id, Map<String, Object> metadataCommand);
}
