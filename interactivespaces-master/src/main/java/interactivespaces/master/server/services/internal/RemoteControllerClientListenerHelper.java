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

import interactivespaces.activity.ActivityState;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A helper class for working with {@link RemoteSpaceControllerClientListener}
 * instances.
 * 
 * <p>
 * There will be one per remote controller client.
 * 
 * @author Keith M. Hughes
 */
public class RemoteControllerClientListenerHelper {

	/**
	 * Listeners registered with helper.
	 */
	private List<RemoteSpaceControllerClientListener> listeners = Lists
			.newCopyOnWriteArrayList();

	/**
	 * Add in a new event listener.
	 * 
	 * @param listener
	 *            The new listener.
	 */
	public void addListener(RemoteSpaceControllerClientListener listener) {
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
	public void removeListener(RemoteSpaceControllerClientListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Signal a space controller connecting.
	 * 
	 * @param controller
	 *            the controller being disconnected from
	 */
	public void signalSpaceControllerConnectAttempt(
			ActiveSpaceController controller) {
		for (RemoteSpaceControllerClientListener listener : listeners) {
			listener.onSpaceControllerConnectAttempted(controller);
		}
	}

	/**
	 * Signal a space controller disconnecting.
	 * 
	 * @param controller
	 *            the controller being disconnected from
	 */
	public void signalSpaceControllerDisconnectAttempt(
			ActiveSpaceController controller) {
		for (RemoteSpaceControllerClientListener listener : listeners) {
			listener.onSpaceControllerDisconnectAttempted(controller);
		}
	}

	/**
	 * Signal a space controller heartbeat.
	 * 
	 * @param uuid
	 *            uuid of the controller
	 * 
	 * @param timestamp
	 *            timestamp of the heartbeat
	 */
	public void signalSpaceControllerHeartbeat(
			String uuid, long timestamp) {
		for (RemoteSpaceControllerClientListener listener : listeners) {
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
		for (RemoteSpaceControllerClientListener listener : listeners) {
			listener.onSpaceControllerStatusChange(uuid, state);
		}
	}

	/**
	 * Send the on deployment message to all listeners.
	 * 
	 * @param uuid
	 *            UUID of the activity.
	 * @param result
	 *            the result of the install
	 */
	public void signalActivityInstall(String uuid,
			LiveActivityInstallResult result) {
		for (RemoteSpaceControllerClientListener listener : listeners) {
			listener.onLiveActivityInstall(uuid, result);
		}
	}

	/**
	 * Send the on deletion message to all listeners.
	 * 
	 * @param uuid
	 *            UUID of the activity.
	 * @param result
	 *            result of the deletion
	 */
	public void signalActivityDelete(String uuid,
			LiveActivityDeleteResult result) {
		for (RemoteSpaceControllerClientListener listener : listeners) {
			listener.onLiveActivityDelete(uuid, result);
		}
	}

	/**
	 * Send the activity state change message to all listeners.
	 * 
	 * @param uuid
	 *            UUID of the activity.
	 * @param status
	 *            Deploy status of the remote activity.
	 */
	public void signalActivityStateChange(String uuid, ActivityState state) {
		for (RemoteSpaceControllerClientListener listener : listeners) {
			listener.onLiveActivityStateChange(uuid, state);
		}
	}

	/**
	 * Clear all listeners from the helper.
	 */
	public void clear() {
		listeners.clear();
	}
}
