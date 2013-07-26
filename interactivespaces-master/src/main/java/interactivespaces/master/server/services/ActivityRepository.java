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

package interactivespaces.master.server.services;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;

import java.util.List;

/**
 * A repository for activities domain objects.
 *
 * @author Keith M. Hughes
 */
public interface ActivityRepository {

  /**
   * Create a new activity.
   *
   * @return The new activity instance. It will not be saved in the repository.
   */
  Activity newActivity();

  /**
   * Create a new activity dependency.
   *
   * @return The new activity dependency. It will not be saved in the
   *         repository.
   */
  ActivityDependency newActivityDependency();

  /**
   * Create a new activity configuration.
   *
   * @return The new activity configuration. It will not be saved in the
   *         repository.
   */
  ActivityConfiguration newActivityConfiguration();

  /**
   * Create a new activity configuration parameter.
   *
   * @return The new activity configuration parameter. It will not be saved in
   *         the repository.
   */
  ConfigurationParameter newConfigurationParameter();

  /**
   * Get all activities in the repository.
   *
   * @return Get all activities in the repository.
   */
  List<Activity> getAllActivities();

  /**
   * Get all activities in the repository that match the filter.
   *
   * @param filter
   *          the filter
   *
   * @return all activities in the repository matching the filter
   */
  List<Activity> getActivities(FilterExpression filter);

  /**
   * Get a activity by its ID.
   *
   * @param id
   *          The ID of the desired activity.
   *
   * @return The activity with the given ID or {@code null} if no such activity.
   */
  Activity getActivityById(String id);

  /**
   * Get a activity by its identifying name and version.
   *
   * @param identifyingName
   *          The identifying name of the desired activity.
   * @param version
   *          The version of the desired activity.
   *
   * @return The activity with the given ID or {@code null} if no such activity.
   */
  Activity getActivityByNameAndVersion(String identifyingName, String version);

  /**
   * Save a activity in the repository.
   *
   * <p>
   * Is used both to save a new activity into the repository for the first time
   * or to update edits to the activity.
   *
   * @param activity
   *          The activity to save.
   *
   * @return The persisted activity. use this one going forward.
   */
  Activity saveActivity(Activity activity);

  /**
   * Delete a activity in the repository.
   *
   * @param activity
   *          The activity to save.
   *
   * @return
   */
  void deleteActivity(Activity activity);

  /**
   * Create a new installed activity.
   *
   * <p>
   * The installed activity will be assigned a UUID.
   *
   * @return The new installed activity instance. It will not be saved in the
   *         repository.
   */
  LiveActivity newLiveActivity();

  /**
   * Get all activities in the repository.
   *
   * @return Get all activities in the repository.
   */
  List<LiveActivity> getAllLiveActivities();

  /**
   * Get all live activities in the repository that match the filter.
   *
   * @param filter
   *          the filter
   *
   * @return all live activities in the repository matching the filter
   */
  List<LiveActivity> getLiveActivities(FilterExpression filter);

  /**
   * Get a activity by its ID.
   *
   * @param id
   *          The ID of the desired activity.
   *
   * @return The activity with the given ID or {@code null} if no such activity.
   */
  LiveActivity getLiveActivityById(String id);

  /**
   * Get all live activities on a given controller.
   *
   * @param controller
   *          the controller to check
   *
   * @return all live activities on the controller
   */
  List<LiveActivity> getLiveActivitiesByController(SpaceController controller);

  /**
   * Get the count of all live activities on a given controller.
   *
   * @param controller
   *          the controller to check
   *
   * @return the count of all live activities on the controller
   */
  long getCountLiveActivitiesByController(SpaceController controller);

  /**
   * Get all live activities based on a given activity.
   *
   * @param activity
   *          the activity to check
   *
   * @return all live activities which use the activity
   */
  List<LiveActivity> getLiveActivitiesByActivity(Activity activity);

  /**
   * Get the count of all live activities based on a given activity.
   *
   * @param activity
   *          the activity to check
   *
   * @return the count of all live activities which use the activity
   */
  long getCountLiveActivitiesByActivity(Activity activity);

  /**
   * Get a activity by its UUID.
   *
   * @param uuid
   *          The UUID of the desired activity.
   *
   * @return The activity with the given UUID or {@code null} if no such
   *         activity.
   */
  LiveActivity getLiveActivityByUuid(String uuid);

  /**
   * Save a activity in the repository.
   *
   * <p>
   * Is used both to save a new activity into the repository for the first time
   * or to update edits to the activity.
   *
   * @param activity
   *          The activity to save.
   *
   * @return The persisted activity. use this one going forward.
   */
  LiveActivity saveLiveActivity(LiveActivity activity);

  /**
   * Delete an installed activity in the repository.
   *
   * @param activity
   *          The activity to save.
   *
   * @return
   */
  void deleteLiveActivity(LiveActivity activity);

  /**
   * Create a new live activity group.
   *
   * @return
   */
  LiveActivityGroup newLiveActivityGroup();

  /**
   * Get all live activity groups.
   *
   * @return All persisted live activity groups.
   */
  List<LiveActivityGroup> getAllLiveActivityGroups();

  /**
   * Get all live activity groups in the repository that match the filter.
   *
   * @param filter
   *          the filter
   *
   * @return all live activity groups in the repository matching the filter
   */
  List<LiveActivityGroup> getLiveActivityGroups(FilterExpression filter);

  /**
   * Get a live activity group by its ID.
   *
   * @param id
   *          The ID of the desired activity group.
   *
   * @return The activity group with the given ID or {@code null} if no such
   *         activity.
   */
  LiveActivityGroup getLiveActivityGroupById(String id);

  /**
   * Get all live activity groups containing a given live activity.
   *
   * @param liveActivity
   *          the live activity to check
   *
   * @return all live activity groups which use the live activity
   */
  List<LiveActivityGroup> getLiveActivityGroupsByLiveActivity(LiveActivity liveActivity);

  /**
   * Get the count of all live activity groups containing a given live activity.
   *
   * @param liveActivity
   *          the live activity to check
   *
   * @return the count of all live activity groups which use the live activity
   */
  long getCountLiveActivityGroupsByLiveActivity(LiveActivity liveActivity);

  /**
   * Save a activity group in the repository.
   *
   * <p>
   * Is used both to save a new activity group into the repository for the first
   * time or to update edits to the activity group.
   *
   * @param liveActivityGroup
   *          The activity group to save.
   *
   * @return The persisted activity group. use this one going forward.
   */
  LiveActivityGroup saveLiveActivityGroup(LiveActivityGroup liveActivityGroup);

  /**
   * Delete an activity group in the repository.
   *
   * @param liveActivityGroup
   *          The activity group to delete.
   *
   * @return
   */
  void deleteLiveActivityGroup(LiveActivityGroup liveActivityGroup);

  /**
   * Create a new space.
   *
   * @return The new space instance. It will not be saved in the repository.
   */
  Space newSpace();

  /**
   * Get all spaces.
   *
   * @return list of all spaces in the repository
   */
  List<Space> getAllSpaces();

  /**
   * Get all spaces in the repository that match the filter.
   *
   * @param filter
   *          the filter
   *
   * @return all spaces in the repository matching the filter
   */
  List<Space> getSpaces(FilterExpression filter);

  /**
   * Get a space by its ID.
   *
   * @param id
   *          The id of the desired space.
   *
   * @return The space with the given id or {@code null} if no such space.
   */
  Space getSpaceById(String id);

  /**
   * Get all spaces which immediately contain a particular live activity group.
   *
   * @param liveActivityGroup
   *          the live activity group
   *
   * @return all spaces which directly contain the given live activity group
   */
  List<Space> getSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup);

  /**
   * Get the count of all spaces which immediately contain a particular live
   * activity group.
   *
   * @param liveActivityGroup
   *          the live activity group
   *
   * @return the count of all spaces which directly contain the given live
   *         activity group
   */
  long getCountSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup);

  /**
   * Get all spaces which immediately contain a particular subspace.
   *
   * @param subspace
   *          the subspace
   *
   * @return all spaces which directly contain the given space as a subspace
   */
  List<Space> getSpacesBySubspace(Space subspace);

  /**
   * Get a count of all spaces which immediately contain a particular subspace.
   *
   * @param subspace
   *          the subspace
   *
   * @return the count of all spaces which directly contain the given space as a
   *         subspace
   */
  long getCountSpacesBySubspace(Space subspace);

  /**
   * Save a space in the repository.
   *
   * <p>
   * Is used both to save a new space into the repository for the first time or
   * to update edits to the space.
   *
   * @param space
   *          The space to save.
   *
   * @return The persisted space. use this one going forward.
   */
  Space saveSpace(Space space);

  /**
   * Delete a space in the repository.
   *
   * @param space
   *          The space to delete.
   *
   * @return
   */
  void deleteSpace(Space space);
}
