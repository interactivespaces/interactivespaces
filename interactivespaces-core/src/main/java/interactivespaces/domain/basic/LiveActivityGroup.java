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
 * A group of activities installed in a space.
 * 
 * <p>
 * The activities are not necessarily on the controllers at this point. It is
 * assumed they will be at some point before they can be activated.
 * 
 * @author Keith M. Hughes
 */
public interface LiveActivityGroup extends PersistedObject, Serializable {
	
	/**
	 * Get the name of the activity group.
	 * 
	 * @return The name of the activity group.
	 */
	String getName();

	/**
	 * Set the name of the activity group.
	 * 
	 * @param name
	 *            The name of the activity group.
	 */
	void setName(String name);

	/**
	 * Get the description of the activity group.
	 * 
	 * @return The description of the activity group.
	 */
	String getDescription();

	/**
	 * Set the description of the activity group.
	 * 
	 * @param description
	 *            The description of the activity group.
	 */
	void setDescription(String description);

	/**
	 * Get all the current activities in the group.
	 * 
	 * @return A freshly allocated list of the activities.
	 */
	List<? extends GroupLiveActivity> getActivities();

	/**
	 * Add a new activity to the group.
	 * 
	 * <p>
	 * A given activity can only be added once.
	 * 
	 * <p>
	 * The activity will be required. See
	 * {@link GroupLiveActivityDependency#REQUIRED}.
	 * 
	 * @param activity
	 *            The new activity.
	 * 
	 * @returns This activity group.
	 * 
	 * @throws InteractiveSpacesException
	 *             The activity was already in the group.
	 */
	LiveActivityGroup addActivity(LiveActivity activity);

	/**
	 * Add a new activity to the group.
	 * 
	 * <p>
	 * A given activity can only be added once.
	 * 
	 * @param activity
	 *            The new activity.
	 * @param dependency
	 *            The dependency this group has on this activity.
	 * 
	 * @returns This activity group.
	 * 
	 * @throws InteractiveSpacesException
	 *             The activity was already in the group.
	 */
	LiveActivityGroup addActivity(LiveActivity activity,
			GroupLiveActivityDependency dependency);

	/**
	 * Remove an activity from the group.
	 * 
	 * <p>
	 * This does nothing if the activity isn't part of the group already.
	 * 
	 * @param activity
	 *            The activity to remove.
	 */
	void removeActivity(LiveActivity activity);

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
	 *            the metadata for the live activity group (can be {@link null}
	 */
	void setMetadata(Map<String, Object> metadata);

	/**
	 * Get the metadata for the live activity group.
	 * 
	 * @return the live activity group's meta data
	 */
	Map<String, Object> getMetadata();
}
