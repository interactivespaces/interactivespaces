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

package interactivespaces.activity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A collection of {@link ActivityListener} instances and the ability to signal
 * all of them.
 * 
 * <p>
 * All listeners are called, even if they throw an exception.
 * 
 * 
 * @author Keith M. Hughes
 */
public class ActivityListenerCollection {

	/**
	 * The activity this collection is holding listeners for.
	 */
	private Activity activity;

	/**
	 * The listeners in the collection.
	 */
	private List<ActivityListener> listeners = new CopyOnWriteArrayList<ActivityListener>();

	/**
	 * 
	 * @param activity
	 *            the activity the listeners are collected for
	 */
	public ActivityListenerCollection(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Add a new listener to the collection.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addListener(ActivityListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener to the collection.
	 * 
	 * <p>
	 * Nothing happens if the listener was never registered.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeListener(ActivityListener listener) {
		listeners.remove(listener);
	}

	/**
	 * There has been a change in the status of an activity.
	 * 
	 * @param activity
	 *            the activity which has changed its status
	 * @param oldStatus
	 *            the previous status of the activity
	 * @param newStatus
	 *            the new status of the activity
	 */
	public void signalActivityStatusChange(ActivityStatus oldStatus,
			ActivityStatus newStatus) {
		for (ActivityListener listener : listeners) {
			try {
				listener.onActivityStatusChange(activity, oldStatus, newStatus);
			} catch (Exception e) {
				activity.getLog().error(
						"Error during signalling activity status change", e);
			}
		}
	}
}
