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

package interactivespaces.controller.client.node.ros;

import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.client.node.ActiveControllerActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Maps;

/**
 * Watches activityStates and handles changes to their state, reports errors,
 * and handles restart policies.
 * 
 * <p>
 * It is assumed that this activity watcher and its operations run in one and
 * only 1 thread.
 * 
 * @author Keith M. Hughes
 */
public class SpaceControllerActivityWatcher {

	/**
	 * All activityStates being watched by the watcher.
	 */
	private Map<ActiveControllerActivity, ActivityWatcherActivityState> activityStates = Maps
			.newHashMap();

	/**
	 * Activities watched by the watcher.
	 */
	private List<ActiveControllerActivity> activities = new CopyOnWriteArrayList<ActiveControllerActivity>();

	/**
	 * Listeners for events from the watcher.
	 */
	private List<SpaceControllerActivityWatcherListener> listeners = new CopyOnWriteArrayList<SpaceControllerActivityWatcherListener>();

	/**
	 * The space environment
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	public SpaceControllerActivityWatcher(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * Add a listener from the watcher.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addListener(SpaceControllerActivityWatcherListener listener) {
		listeners.add(listener);

	}

	/**
	 * Remove a listener from the watcher.
	 * 
	 * <p>
	 * This does nothing if the listener was never added.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeListener(SpaceControllerActivityWatcherListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Start watching an activity.
	 * 
	 * @param activity
	 *            the activity to watch
	 */
	public void watchActivity(ActiveControllerActivity activity) {
		activities.add(activity);
	}

	/**
	 * Scan all activities.
	 */
	public void scan() {
		try {
			// Scan all activityStates from the controller and signal any status
			// changes.
			for (ActiveControllerActivity activity : activities) {
				// No need to synchronize. If something bad happens we'll
				// catch it the next time around.
				handleActivity(activity);
			}
		} catch (Throwable e) {
			spaceEnvironment.getLog().error(
					"Error during activity watcher update", e);
		}
	}

	/**
	 * See what happens with a given activity.
	 * 
	 * @param activity
	 *            the activity to be processed
	 */
	private void handleActivity(ActiveControllerActivity activity) {
		ActivityStatus newStatus = activity.getActivityStatus();

		// If first time watcher is seeing this, just record its current state.
		boolean isNew = false;
		ActivityWatcherActivityState watcherState = activityStates
				.get(activity);
		if (watcherState == null) {
			watcherState = new ActivityWatcherActivityState(activity, newStatus);
			activityStates.put(activity, watcherState);
			isNew = true;
		}

		// If state hasn't changed, don't care.
		ActivityStatus oldStatus = watcherState.getLastKnownStatus();
		if (!isNew && oldStatus.getState().equals(newStatus.getState())) {
			return;
		}

		if (newStatus.getState().isRunning()) {
			watcherState.setLastKnownStatus(newStatus);
		} else {
			activityStates.remove(activity);
			activities.remove(activity);
		}

		if (newStatus.getState().isError()) {
			signalActivityError(activity, oldStatus, newStatus);
		} else {
			signalActivityStatusChange(activity, oldStatus, newStatus);
		}
	}

	/**
	 * An activity has some sort of state change.
	 * 
	 * @param activity
	 *            The activity which has changed to a non-error state
	 * @param previousStatus
	 *            the previous status for the activity
	 * @param currentStatus
	 *            the current status for the activity
	 */
	private void signalActivityStatusChange(ActiveControllerActivity activity,
			ActivityStatus previousStatus, ActivityStatus currentStatus) {
		// Don't care if a listener gets added or removed between any of the
		// changes
		System.out.format("%s changed from %s to %s\n", activity.getUuid(), previousStatus, currentStatus);
		for (SpaceControllerActivityWatcherListener listener : listeners) {
			listener.onActivityStatusChange(activity, previousStatus,
					currentStatus);
		}
	}

	/**
	 * An activity has some sort of error.
	 * 
	 * @param activity
	 *            the activity which has an error
	 * @param previousStatus
	 *            the previous status for the activity
	 * @param currentStatus
	 *            the current status for the activity
	 */
	private void signalActivityError(ActiveControllerActivity activity,
			ActivityStatus previousStatus, ActivityStatus currentStatus) {
		// Don't care if a listener gets added or removed between any of the
		// changes
		for (SpaceControllerActivityWatcherListener listener : listeners) {
			listener.onActivityError(activity, previousStatus, currentStatus);
		}
	}

	/**
	 * Information about an activity as seen by the activity activityWatcher.
	 * 
	 * @author Keith M. Hughes
	 */
	public static class ActivityWatcherActivityState {
		/**
		 * The activity being watched.
		 */
		private ActiveControllerActivity activity;

		/**
		 * Last known status running in activity.
		 */
		private ActivityStatus lastKnownStatus;

		public ActivityWatcherActivityState(ActiveControllerActivity activity,
				ActivityStatus lastKnownStatus) {
			this.activity = activity;
			this.lastKnownStatus = lastKnownStatus;
		}

		/**
		 * @return the activity
		 */
		public ActiveControllerActivity getActivity() {
			return activity;
		}

		/**
		 * @return the lastKnownStatus
		 */
		public ActivityStatus getLastKnownStatus() {
			return lastKnownStatus;
		}

		/**
		 * @param lastKnownStatus
		 *            the last known status to set
		 */
		public void setLastKnownStatus(ActivityStatus lastKnownStatus) {
			this.lastKnownStatus = lastKnownStatus;
		}
	}
}
