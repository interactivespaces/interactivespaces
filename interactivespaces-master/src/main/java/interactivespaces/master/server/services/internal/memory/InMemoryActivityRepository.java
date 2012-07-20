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

package interactivespaces.master.server.services.internal.memory;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.domain.basic.pojo.SimpleActivityConfiguration;
import interactivespaces.domain.basic.pojo.SimpleActivityDependency;
import interactivespaces.domain.basic.pojo.SimpleConfigurationParameter;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleLiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.server.services.ActivityRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.util.UnsupportedException;

import com.google.common.collect.Lists;

/**
 * All activities installed anywhere in the the environment.
 * 
 * @author Keith M. Hughes
 */
public class InMemoryActivityRepository implements ActivityRepository {

	/**
	 * A map of activities keyed by their ID.
	 */
	private Map<String, Activity> activitiesById = new HashMap<String, Activity>();

	/**
	 * A map of installed activities keyed by their ID.
	 */
	private Map<String, LiveActivity> liveActivitiesById = new HashMap<String, LiveActivity>();

	/**
	 * A map of installed activities keyed by their UUID.
	 */
	private Map<String, LiveActivity> liveActivitiesByUuid = new HashMap<String, LiveActivity>();

	/**
	 * A map of activity groups keyed by their ID.
	 */
	private Map<String, LiveActivityGroup> activityGroupsById = new HashMap<String, LiveActivityGroup>();

	@Override
	public Activity newActivity() {
		return new SimpleActivity();
	}

	@Override
	public ActivityDependency newActivityDependency() {
		return new SimpleActivityDependency();
	}

	@Override
	public ActivityConfiguration newActivityConfiguration() {
		return new SimpleActivityConfiguration();
	}

	@Override
	public ConfigurationParameter newConfigurationParameter() {
		return new SimpleConfigurationParameter();
	}

	@Override
	public List<Activity> getAllActivities() {
		synchronized (activitiesById) {
			return new ArrayList<Activity>(activitiesById.values());
		}
	}

	@Override
	public List<Activity> getActivities(FilterExpression filter) {
		List<Activity> result = Lists.newArrayList();
		List<Activity> toBeFiltered;

		synchronized (activitiesById) {
			toBeFiltered = new ArrayList<Activity>(activitiesById.values());
		}

		for (Activity activity : toBeFiltered) {
			if (filter.accept(activity)) {
				result.add(activity);
			}
		}

		return result;
	}

	@Override
	public Activity getActivityById(String id) {
		synchronized (activitiesById) {
			return activitiesById.get(id);
		}
	}

	@Override
	public Activity getActivityByNameAndVersion(String identifyingName,
			String version) {
		synchronized (activitiesById) {
			for (Activity activity : activitiesById.values()) {
				if (activity.getIdentifyingName().equals(identifyingName)
						&& activity.getVersion().equals(version))
					return activity;
			}
			return null;
		}
	}

	@Override
	public Activity saveActivity(Activity activity) {
		synchronized (activitiesById) {
			activitiesById.put(activity.getId(), activity);
		}

		return activity;
	}

	@Override
	public void deleteActivity(Activity activity) {
		synchronized (activitiesById) {
			activitiesById.remove(activity.getId());
		}
	}

	@Override
	public LiveActivity newLiveActivity() {
		return new SimpleLiveActivity();
	}

	@Override
	public List<LiveActivity> getAllLiveActivities() {
		synchronized (liveActivitiesById) {
			return new ArrayList<LiveActivity>(liveActivitiesById.values());
		}
	}

	@Override
	public List<LiveActivity> getLiveActivities(FilterExpression filter) {
		List<LiveActivity> result = Lists.newArrayList();
		List<LiveActivity> toBeFiltered;

		synchronized (liveActivitiesById) {
			toBeFiltered = new ArrayList<LiveActivity>(
					liveActivitiesById.values());
		}

		for (LiveActivity activity : toBeFiltered) {
			if (filter.accept(activity)) {
				result.add(activity);
			}
		}

		return result;
	}

	@Override
	public LiveActivity getLiveActivityById(String id) {
		synchronized (liveActivitiesById) {
			return liveActivitiesById.get(id);
		}
	}

	@Override
	public LiveActivity getLiveActivityByUuid(String uuid) {
		synchronized (liveActivitiesById) {
			return liveActivitiesByUuid.get(uuid);
		}
	}

	@Override
	public List<LiveActivity> getLiveActivitiesByController(
			SpaceController controller) {
		List<LiveActivity> results = Lists.newArrayList();

		synchronized (liveActivitiesById) {
			for (LiveActivity iactivity : liveActivitiesById.values()) {
				if (iactivity.getController() == controller) {
					results.add(iactivity);
				}
			}
		}

		return results;
	}

	@Override
	public List<LiveActivity> getLiveActivitiesByActivity(Activity activity) {
		List<LiveActivity> results = Lists.newArrayList();

		synchronized (liveActivitiesById) {
			for (LiveActivity iactivity : liveActivitiesById.values()) {
				if (iactivity.getActivity() == activity) {
					results.add(iactivity);
				}
			}
		}

		return results;
	}

	@Override
	public LiveActivity saveLiveActivity(LiveActivity activity) {
		synchronized (liveActivitiesById) {
			liveActivitiesById.put(activity.getId(), activity);
			liveActivitiesByUuid.put(activity.getUuid(), activity);
		}

		return activity;
	}

	@Override
	public void deleteLiveActivity(LiveActivity activity) {
		synchronized (liveActivitiesById) {
			liveActivitiesById.remove(activity.getId());
			liveActivitiesByUuid.remove(activity.getUuid());
		}
	}

	@Override
	public LiveActivityGroup newLiveActivityGroup() {
		return new SimpleLiveActivityGroup();
	}

	@Override
	public List<LiveActivityGroup> getAllLiveActivityGroups() {
		synchronized (activityGroupsById) {
			return new ArrayList<LiveActivityGroup>(activityGroupsById.values());
		}
	}

	@Override
	public List<LiveActivityGroup> getLiveActivityGroups(FilterExpression filter) {
		List<LiveActivityGroup> result = Lists.newArrayList();
		List<LiveActivityGroup> toBeFiltered;

		synchronized (activitiesById) {
			toBeFiltered = new ArrayList<LiveActivityGroup>(
					activityGroupsById.values());
		}

		for (LiveActivityGroup group : toBeFiltered) {
			if (filter.accept(group)) {
				result.add(group);
			}
		}

		return result;
	}

	@Override
	public LiveActivityGroup getLiveActivityGroupById(String id) {
		synchronized (activityGroupsById) {
			return activityGroupsById.get(id);
		}
	}

	@Override
	public List<LiveActivityGroup> getLiveActivityGroupsByLiveActivity(
			LiveActivity liveActivity) {
		// TODO(keith): Implement this.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public LiveActivityGroup saveLiveActivityGroup(
			LiveActivityGroup activityGroup) {
		synchronized (activityGroupsById) {
			activityGroupsById.put(activityGroup.getId(), activityGroup);
		}

		return activityGroup;
	}

	@Override
	public void deleteLiveActivityGroup(LiveActivityGroup activityGroup) {
		synchronized (activityGroupsById) {
			activityGroupsById.remove(activityGroup.getId());
		}
	}

	@Override
	public long getCountLiveActivitiesByController(SpaceController controller) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public long getCountLiveActivitiesByActivity(Activity activity) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public long getCountLiveActivityGroupsByLiveActivity(
			LiveActivity liveActivity) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public Space newSpace() {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public List<Space> getAllSpaces() {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public List<Space> getSpaces(FilterExpression filter) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public Space getSpaceById(String id) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public List<Space> getSpacesByLiveActivityGroup(
			LiveActivityGroup liveActivityGroup) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public List<Space> getSpacesBySubspace(Space subspace) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public long getCountSpacesBySubspace(Space subspace) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public Space saveSpace(Space space) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public void deleteSpace(Space space) {
		throw new UnsupportedException("Not currently supported");
	}

	@Override
	public long getCountSpacesByLiveActivityGroup(
			LiveActivityGroup liveActivityGroup) {
		throw new UnsupportedException("Not currently supported");
	}
}
