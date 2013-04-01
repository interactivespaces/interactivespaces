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

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.server.services.MasterAlertManager;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.master.server.services.SpaceControllerListenerSupport;
import interactivespaces.service.alert.AlertService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A basic implementation of a {@link MasterAlertManager}.
 * 
 * @author Keith M. Hughes
 */
public class BasicMasterAlertManager implements MasterAlertManager {

	/**
	 * Default number of milliseconds for space controller failure.
	 */
	public static final int SPACE_CONTROLLER_HEARTBEAT_TIME_DEFAULT = 30000;

	/**
	 * The default number of milliseconds the watcher thread delays between
	 * scans.
	 */
	private static final int WATCHER_DELAY_DEFAULT = 1000;

	/**
	 * The active controller manager to listen to.
	 */
	private ActiveControllerManager activeControllerManager;

	/**
	 * Number of milliseconds after not receiving a heartbeat for a space
	 * controller that we will raise the alarm.
	 */
	private long spaceControllerHeartbeatTime = SPACE_CONTROLLER_HEARTBEAT_TIME_DEFAULT;

	/**
	 * The listener for space controller events.
	 */
	private SpaceControllerListener spaceControllerListener = new SpaceControllerListenerSupport() {

		@Override
		public void onSpaceControllerConnectAttempted(String uuid) {
			handleSpaceControllerConnectAttempted(uuid);
		}

		@Override
		public void onSpaceControllerDisconnectAttempted(String uuid) {
			handleSpaceControllerDisconnectAttempted(uuid);
		}

		@Override
		public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
			handleSpaceControllerHeartbeat(uuid, timestamp);
		}
	};

	/**
	 * Control for the alert manager.
	 */
	private ScheduledFuture<?> alertWatcherControl;

	/**
	 * Number of milliseconds the alert watcher waits before scanning for
	 * activity state.
	 */
	private long alertWatcherDelay = WATCHER_DELAY_DEFAULT;

	/**
	 * A mapping of controller UUIDs to the controller.
	 */
	private Map<String, SpaceControllerAlertWatcher> spaceControllerWatchers = Maps
			.newHashMap();

	/**
	 * The alert service to use.
	 */
	private AlertService alertService;

	/**
	 * The repository for controller entities.
	 */
	private ControllerRepository controllerRepository;

	/**
	 * The space environment to use.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		activeControllerManager.addControllerListener(spaceControllerListener);

		alertWatcherControl = spaceEnvironment.getExecutorService()
				.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						scan();
					}
				}, alertWatcherDelay, alertWatcherDelay, TimeUnit.MILLISECONDS);

		spaceEnvironment.getLog().info("Master alert manager started");
	}

	@Override
	public void shutdown() {
		activeControllerManager
				.removeControllerListener(spaceControllerListener);
		if (alertWatcherControl != null) {
			alertWatcherControl.cancel(true);
			alertWatcherControl = null;
		}
	}

	/**
	 * Scan for alerts.
	 */
	public void scan() {
		long currentTimestamp = spaceEnvironment.getTimeProvider()
				.getCurrentTime();

		for (SpaceControllerAlertWatcher watcher : getSpaceControllerWatchers()) {
			watcher.check(currentTimestamp);
		}
	}

	/**
	 * Handle a connection attempt to a space controller.
	 * 
	 * @param uuid
	 *            uuid of the space controller
	 */
	private void handleSpaceControllerConnectAttempted(String uuid) {
		addSpaceControllerWatcher(uuid);
	}

	/**
	 * Handle a disconnection attempt to a space controller.
	 * 
	 * @param uuid
	 *            uuid of the space controller
	 */
	private void handleSpaceControllerDisconnectAttempted(String uuid) {
		removeSpaceControllerWatcher(uuid);
	}

	/**
	 * Handle a controller heartbeat.
	 * 
	 * @param uuid
	 *            UUID of the controller
	 * @param timestamp
	 *            timestamp of the controller coming in
	 */
	public void handleSpaceControllerHeartbeat(String uuid, long timestamp) {
		SpaceControllerAlertWatcher watcher = getSpaceControllerWatcher(uuid);

		if (watcher != null) {
			watcher.heartbeat(timestamp);
		} else {
			spaceEnvironment
					.getLog()
					.warn(String
							.format("Master alert manager got heartbeat for unknown space controller %s",
									uuid));
		}
	}

	/**
	 * Add the watcher for a specific space controller.
	 * 
	 * <p>
	 * It will be given the timestamp of the current time.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 */
	private void addSpaceControllerWatcher(String uuid) {
		long timestamp = spaceEnvironment.getTimeProvider().getCurrentTime();
		synchronized (spaceControllerWatchers) {
			SpaceControllerAlertWatcher watcher = new SpaceControllerAlertWatcher(
					uuid, timestamp);
			spaceControllerWatchers.put(uuid, watcher);
		}
	}

	/**
	 * Get the watcher for a specific space controller.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 * @param timestamp
	 *            timestamp the call came in
	 * 
	 * @return the watcher
	 */
	public SpaceControllerAlertWatcher getSpaceControllerWatcher(String uuid) {
		synchronized (spaceControllerWatchers) {
			return spaceControllerWatchers.get(uuid);
		}
	}

	/**
	 * Remove the watcher for a specific space controller.
	 * 
	 * <p>
	 * Does nothing if no watcher for the space controller.
	 * 
	 * @param uuid
	 *            the UUID of the space controller
	 */
	public void removeSpaceControllerWatcher(String uuid) {
		synchronized (spaceControllerWatchers) {
			spaceControllerWatchers.remove(uuid);
		}
	}

	/**
	 * Get all space controller watchers currently registered.
	 * 
	 * @return all watchers
	 */
	private List<SpaceControllerAlertWatcher> getSpaceControllerWatchers() {
		synchronized (spaceControllerWatchers) {
			return Lists.newArrayList(spaceControllerWatchers.values());
		}
	}

	/**
	 * Get the space controller listener the manager is using.
	 * 
	 * @return the space controller listener
	 */
	SpaceControllerListener getSpaceControllerListener() {
		return spaceControllerListener;
	}

	/**
	 * @param activeControllerManager
	 *            the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			ActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
	}

	/**
	 * Set the maximum amount of time willing to wait for a controller heartbeat
	 * before complaining.
	 * 
	 * @param spaceControllerHeartbeatTime
	 *            the time to wait in milliseconds
	 */
	public void setSpaceControllerHeartbeatTime(
			long spaceControllerHeartbeatTime) {
		this.spaceControllerHeartbeatTime = spaceControllerHeartbeatTime;
	}

	/**
	 * Get the maximum amount of time willing to wait for a controller heartbeat
	 * before complaining.
	 * 
	 * @return the time to wait in milliseconds
	 */
	public long getSpaceControllerHeartbeatTime() {
		return spaceControllerHeartbeatTime;
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * @param alertService
	 *            the alertService to set
	 */
	public void setAlertService(AlertService alertService) {
		this.alertService = alertService;
	}

	/**
	 * The watcher for an individual space controller.
	 * 
	 * @author Keith M. Hughes
	 */
	public class SpaceControllerAlertWatcher {

		/**
		 * UUID of the controller being watched.
		 */
		private String uuid;

		/**
		 * Last timestamp for a heartbeat.
		 */
		private AtomicLong lastHeartbeatTimestamp;

		/**
		 * {@code true} if an alert has been sent.
		 * 
		 * TODO(keith): make a strategy.
		 */
		private volatile boolean alerted = false;

		public SpaceControllerAlertWatcher(String uuid, long timestamp) {
			this.uuid = uuid;
			lastHeartbeatTimestamp = new AtomicLong(timestamp);
		}

		/**
		 * New heartbeat coming in. Catch it.
		 * 
		 * @param heartbeatTimestamp
		 *            the new heartbeat
		 */
		public void heartbeat(long heartbeatTimestamp) {
			lastHeartbeatTimestamp.set(heartbeatTimestamp);
			alerted = false;
		}

		/**
		 * Check the current timestamp.
		 * 
		 * @param currentTimestamp
		 *            the time stamp to check against
		 */
		public void check(long currentTimestamp) {
			long timeSinceLastHeartbeat = currentTimestamp
					- lastHeartbeatTimestamp.get();
			if (timeSinceLastHeartbeat > spaceControllerHeartbeatTime) {
				handleAlertSpaceControllerTimeout(timeSinceLastHeartbeat);
			}
		}

		/**
		 * A space controller has timed out. Decide what to do.
		 * 
		 * @param timeSinceLastHeartbeat
		 *            number of milliseconds since last heartbeat
		 */
		private void handleAlertSpaceControllerTimeout(
				long timeSinceLastHeartbeat) {
			if (!alerted) {
				alerted = true;

				alertService.raiseAlert(ALERT_TYPE_CONTROLLER_TIMEOUT, uuid,
						createAlertMessage(timeSinceLastHeartbeat));
			}
		}

		/**
		 * Create a message for the alert.
		 * 
		 * @param timeSinceLastHeartbeat
		 *            the amount of time for the scan
		 * 
		 * @return the fully formated message
		 */
		private String createAlertMessage(long timeSinceLastHeartbeat) {
			SpaceController controller = controllerRepository
					.getSpaceControllerByUuid(uuid);

			if (controller != null) {
				String message = "No space controller heartbeat in %d milliseconds\n\n"
						+ "ID: %s\nUUID: %s\nName: %s\nHostId: %s\n";
				return String.format(message, timeSinceLastHeartbeat,
						controller.getId(), uuid, controller.getName(),
						controller.getHostId());
			} else {
				return String
						.format("No space controller heartbeat in %d milliseconds\nUnknown space controller with UUID %s",
								timeSinceLastHeartbeat, uuid);
			}
		}
	}
}
