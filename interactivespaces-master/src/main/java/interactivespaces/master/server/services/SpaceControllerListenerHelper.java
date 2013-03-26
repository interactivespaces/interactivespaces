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

import interactivespaces.activity.ActivityState;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;
import interactivespaces.master.server.services.internal.LiveActivityInstallResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A helper for messages to {@link SpaceControllerListener} instances.
 * 
 * @author Keith M. Hughes
 */
public class SpaceControllerListenerHelper {

	/**
	 * Listeners registered with helper.
	 */
	private List<SpaceControllerListener> listeners = new CopyOnWriteArrayList<SpaceControllerListener>();

	/**
	 * Add in a new event listener.
	 * 
	 * @param listener
	 *            The new listener.
	 */
	public void addListener(SpaceControllerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove an event listener.
	 * 
	 * <p>
	 * Does nothing if the listener wasn't registered.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeListener(SpaceControllerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Signal a space controller connection attempt.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 */
	public void signalSpaceControllerConnectAttempted(String uuid) {
		for (SpaceControllerListener listener : listeners) {
			listener.onSpaceControllerConnectAttempted(uuid);
		}
	}

	/**
	 * Signal a space controller disconnection attempt.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 */
	public void signalSpaceControllerDisconnectAttempted(String uuid) {
		for (SpaceControllerListener listener : listeners) {
			listener.onSpaceControllerDisconnectAttempted(uuid);
		}
	}

	/**
	 * Signal a space controller heartbeat.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 * 
	 * @param timestamp
	 *            timestamp of the heartbeat
	 */
	public void signalSpaceControllerHeartbeat(String uuid, long timestamp) {
		for (SpaceControllerListener listener : listeners) {
			listener.onSpaceControllerHeartbeat(uuid, timestamp);
		}
	}

	/**
	 * Signal that the controller status has been updated.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 * @param state
	 *            the new state
	 */
	public void signalSpaceControllerStatusChange(String uuid,
			SpaceControllerState state) {
		for (SpaceControllerListener listener : listeners) {
			listener.onSpaceControllerStatusChange(uuid, state);
		}
	}

	/**
	 * Send the on deployment message to all listeners.
	 * 
	 * @param uuid
	 *            UUID of the live activity
	 * @param result
	 *            result of the install
	 * @param timestamp
	 *            timestamp of the deployment
	 */
	public void signalActivityInstall(String uuid,
			LiveActivityInstallResult result, long timestamp) {
		for (SpaceControllerListener listener : listeners) {
			listener.onLiveActivityInstall(uuid, result, timestamp);
		}
	}

	/**
	 * Send the on deletion message to all listeners.
	 * 
	 * @param uuid
	 *            UUID of the live activity
	 * @param result
	 *            result of the deletion
	 * @param timestamp
	 *            timestamp of the deletion
	 */
	public void signalActivityDelete(String uuid,
			LiveActivityDeleteResult result, long timestamp) {
		for (SpaceControllerListener listener : listeners) {
			listener.onLiveActivityDelete(uuid, result, timestamp);
		}
	}

	/**
	 * Send the live activity state change message to all listeners.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 * @param oldState
	 *            old state of the remote activity
	 * @param newState
	 *            new state of the remote activity
	 */
	public void signalLiveActivityStateChange(String uuid,
			ActivityState oldState, ActivityState newState) {
		for (SpaceControllerListener listener : listeners) {
			listener.onLiveActivityStateChange(uuid, oldState, newState);
		}
	}

	/**
	 * Clear all listeners from the helper.
	 */
	public void clear() {
		listeners.clear();
	}
}
