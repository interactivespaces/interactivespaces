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

package interactivespaces.domain.basic;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.PersistedObject;
import interactivespaces.domain.basic.GroupLiveActivity.GroupLiveActivityDependency;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A group of live activities.
 *
 * <p>
 * The live activities are not necessarily on the controllers at this point. It is assumed they will be at some point
 * before they can be activated.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityGroup extends PersistedObject, Serializable {

  /**
   * Get the name of the live activity group.
   *
   * @return the name of the live activity group
   */
  String getName();

  /**
   * Set the name of the live activity group.
   *
   * @param name
   *          the name of the live activity group
   */
  void setName(String name);

  /**
   * Get the description of the live activity group.
   *
   * @return the description of the live activity group
   */
  String getDescription();

  /**
   * Set the description of the live activity group.
   *
   * @param description
   *          the description of the live activity group
   */
  void setDescription(String description);

  /**
   * Get all the current activities in the group.
   *
   * @return a freshly allocated list of the live activities
   */
  List<? extends GroupLiveActivity> getLiveActivities();

  /**
   * Add a new activity to the group.
   *
   * <p>
   * A given activity can only be added once.
   *
   * <p>
   * The activity will be required. See {@link GroupLiveActivityDependency#REQUIRED}.
   *
   * @param liveActivity
   *          the new live activity
   *
   * @return this activity group
   *
   * @throws InteractiveSpacesException
   *           the live activity was already in the group
   */
  LiveActivityGroup addLiveActivity(LiveActivity liveActivity) throws InteractiveSpacesException;

  /**
   * Add a new activity to the group.
   *
   * <p>
   * A given activity can only be added once.
   *
   * @param liveActivity
   *          the new live activity
   * @param dependency
   *          the dependency this group has on this activity
   *
   * @return this live activity group
   *
   * @throws InteractiveSpacesException
   *           the live activity was already in the group
   */
  LiveActivityGroup addLiveActivity(LiveActivity liveActivity, GroupLiveActivityDependency dependency)
      throws InteractiveSpacesException;

  /**
   * Remove a live activity from the group.
   *
   * <p>
   * This does nothing if the live activity isn't part of the group already.
   *
   * @param liveActivity
   *          the live activity to remove
   */
  void removeLiveActivity(LiveActivity liveActivity);

  /**
   * Clear all live activities from the group.
   */
  void clearActivities();

  /**
   * Set the metadata for the live activity group.
   *
   * <p>
   * This removes the old metadata completely.
   *
   * @param metadata
   *          the metadata for the live activity group (can be {@link null}
   */
  void setMetadata(Map<String, Object> metadata);

  /**
   * Get the metadata for the live activity group.
   *
   * @return the live activity group's meta data
   */
  Map<String, Object> getMetadata();
}
