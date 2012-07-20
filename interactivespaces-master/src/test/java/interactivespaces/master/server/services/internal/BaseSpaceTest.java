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

package interactivespaces.master.server.services.internal;

import static org.junit.Assert.assertEquals;
import interactivespaces.activity.ActivityState;
import interactivespaces.domain.basic.GroupLiveActivity.GroupLiveActivityDependency;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleLiveActivityGroup;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.domain.space.pojo.SimpleSpace;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpace;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.time.LocalTimeProvider;
import interactivespaces.time.TimeProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.mockito.Mockito;

import com.google.common.collect.Lists;

/**
 * A base test class for tests wanting to check out space operations.
 * 
 * @author Keith M. Hughes
 */
public class BaseSpaceTest {
	protected SpaceController controller;

	protected Map<Integer, String> ids;
	protected Map<String, LiveActivity> installedAppsByUuid = new HashMap<String, LiveActivity>();
	protected Random random;

	protected TimeProvider timeProvider;

	protected RemoteControllerClient remoteControllerClient;

	/**
	 * 
	 */
	protected void baseSetup() {
		random = new Random(System.currentTimeMillis());
		ids = new HashMap<Integer, String>();

		timeProvider = new LocalTimeProvider();

		controller = new SimpleSpaceController();
		controller.setUuid("123-45-6789");

		installedAppsByUuid.clear();

		remoteControllerClient = Mockito.mock(RemoteControllerClient.class);
	}

	/**
	 * Create a space with a name given by an ID from the random ID list.
	 * 
	 * @param idNum
	 *            The ID from the random ID list to use.
	 * 
	 * @return A space with just a name set.
	 */
	protected Space space(int idNum) {
		Space space = new SimpleSpace();
		space.setName(getId(idNum));
		return space;
	}

	/**
	 * Create a space with a name given by an ID from the random ID list. Also
	 * set the given activity group.
	 * 
	 * @param idNum
	 *            The ID from the random ID list to use.
	 * @param activityGroups
	 *            the activity groups to attach
	 * 
	 * @return A space with just a name set.
	 */
	protected Space space(int idNum, LiveActivityGroup... activityGroups) {
		Space space = space(idNum);

		for (LiveActivityGroup activityGroup : activityGroups) {
			space.addActivityGroup(activityGroup);
		}

		return space;
	}

	/**
	 * Create an activity group from a collection of apps.
	 * 
	 * @param apps
	 * @return
	 */
	protected LiveActivityGroup liveActivityGroup(LiveActivity... apps) {
		return liveActivityGroup(GroupLiveActivityDependency.REQUIRED, apps);
	}

	protected LiveActivityGroup liveActivityGroup(
			GroupLiveActivityDependency dependency, LiveActivity... apps) {
		SimpleLiveActivityGroup activityGroup = new SimpleLiveActivityGroup();
		for (LiveActivity iapp : apps)
			activityGroup.addActivity(iapp, dependency);

		return activityGroup;
	}

	/**
	 * Create a live activity group from a collection of IDs for the live
	 * activities.
	 * 
	 * @param liveActivityIds
	 *            the live activity IDs
	 * 
	 * @return a live activity group containing the requested live activities
	 */
	protected LiveActivityGroup liveActivityGroup(int... liveActivityIds) {
		return liveActivityGroup(GroupLiveActivityDependency.REQUIRED,
				liveActivityIds);
	}

	/**
	 * Create a live activity group from a collection of the live activities.
	 * 
	 * @param liveActivities
	 *            the live activities
	 * 
	 * @return a live activity group containing the requested live activities
	 */
	protected LiveActivityGroup liveActivityGroup(
			List<LiveActivity> liveActivities) {
		return liveActivityGroup(GroupLiveActivityDependency.REQUIRED,
				liveActivities);
	}

	protected LiveActivityGroup liveActivityGroup(
			GroupLiveActivityDependency dependency, int... appIds) {
		LiveActivityGroup activityGroup = new SimpleLiveActivityGroup();
		for (int id : appIds) {
			activityGroup.addActivity(liveActivity(id), dependency);
		}
		return activityGroup;
	}

	/**
	 * Create a live activity group from the supplied list of live activities
	 * all with the given dependency.
	 * 
	 * @param dependency
	 *            the group dependency each live activity should have
	 * @param liveActivities
	 *            the list of live activities
	 * 
	 * @return the live activity group containing the live activities
	 */
	protected LiveActivityGroup liveActivityGroup(
			GroupLiveActivityDependency dependency,
			List<LiveActivity> liveActivities) {
		LiveActivityGroup activityGroup = new SimpleLiveActivityGroup();
		for (LiveActivity liveActivity : liveActivities) {
			activityGroup.addActivity(liveActivity, dependency);
		}

		return activityGroup;
	}

	/**
	 * Get a live activity with a given ID.
	 * 
	 * <p>
	 * A live activity will be created if the ID hasn't been allocated yet.
	 * 
	 * @param id
	 *            ID for the live activity
	 * 
	 * @return the live activity with the requested ID
	 */
	protected LiveActivity liveActivity(int idNum) {
		String uuid = getId(idNum);
		LiveActivity app = installedAppsByUuid.get(uuid);
		if (app == null) {
			app = new SimpleLiveActivity().setUuid(uuid);
			app.setController(controller);
			installedAppsByUuid.put(uuid, app);
		}

		return app;
	}

	/**
	 * Get a list of live activities with the given IDs.
	 * 
	 * <p>
	 * A live activity will be created if the ID hasn't been allocated yet.
	 * 
	 * @param ids
	 *            IDs for the live activities
	 * 
	 * @return the live activity with the requested ID
	 */
	protected List<LiveActivity> liveActivities(int... ids) {
		List<LiveActivity> result = Lists.newArrayList();
		for (int id : ids) {
			result.add(liveActivity(id));
		}

		return result;
	}

	protected ActiveSpace activeSpace(int idNum) {
		return new ActiveSpace(space(idNum));
	}

	/**
	 * Get an app with a given ID.
	 * 
	 * <p>
	 * An app will be created if the ID hasn't been allocated yet.
	 * 
	 * @param id
	 * @return
	 */
	protected ActiveLiveActivity activeLiveActivity(int idNum) {
		LiveActivity iapp = liveActivity(idNum);
		return new ActiveLiveActivity(new ActiveSpaceController(
				iapp.getController(), timeProvider), iapp,
				remoteControllerClient, timeProvider);
	}

	private String getId(int idNum) {
		String value = ids.get(idNum);
		if (value == null) {
			while (ids.values().contains(
					value = Double.toString(random.nextDouble())))
				;

			ids.put(idNum, value);
		}

		return value;
	}

	protected List<String> getIdList(int... idNums) {
		List<String> result = new ArrayList<String>(idNums.length);

		for (int idNum : idNums) {
			result.add(getId(idNum));
		}

		return result;
	}

	/**
	 * Check the expected active live activity state
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to check
	 * @param expectedDirectRunning TODO
	 * @param expectedGroupRunningCount
	 *            how many groups should be running the live activity
	 * @param expectedDirectActivated TODO
	 * @param expectedGroupActivatedCount
	 *            how many groups should have activated the live activity
	 * @param expectedState
	 *            the expected state of the activity
	 */
	protected void assertActiveActivityState(
			ActiveLiveActivity activeLiveActivity,
			boolean expectedDirectRunning, int expectedGroupRunningCount,
			boolean expectedDirectActivated, int expectedGroupActivatedCount, ActivityState expectedState) {
		assertEquals("Direct running not equal",
				expectedDirectRunning,
				activeLiveActivity.isDirectRunning());
		assertEquals("Group running counts not equal",
				expectedGroupRunningCount,
				activeLiveActivity.getNumberLiveActivityGroupRunning());
		assertEquals("Direct activation not equal",
				expectedDirectActivated,
				activeLiveActivity.isDirectActivated());
		assertEquals("Group activated counts not equal",
				expectedGroupActivatedCount,
				activeLiveActivity.getNumberLiveActivityGroupActivated());
		assertEquals("Activity states not equal", expectedState,
				activeLiveActivity.getState());
	}
}