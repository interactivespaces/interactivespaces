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

import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.event.BaseMasterEventListener;
import interactivespaces.master.event.MasterEventListener;
import interactivespaces.master.event.MasterEventManager;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.MasterAlertManager;
import interactivespaces.service.alert.AlertService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A basic implementation of a {@link MasterAlertManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterAlertManager implements MasterAlertManager {

  /**
   * The delay for handing a controller shut down event in milliseconds.
   */
  public static final int CONTROLLER_SHUTDOWN_EVENT_HANDLING_DELAY = 500;

  /**
   * Default number of milliseconds for space controller failure.
   */
  public static final int SPACE_CONTROLLER_HEARTBEAT_TIME_DEFAULT = 30000;

  /**
   * The default number of milliseconds the watcher thread delays between scans.
   */
  private static final int WATCHER_DELAY_DEFAULT = 1000;

  /**
   * Number of milliseconds after not receiving a heartbeat for a space controller that we will raise the alarm.
   */
  private long spaceControllerHeartbeatTime = SPACE_CONTROLLER_HEARTBEAT_TIME_DEFAULT;

  /**
   * The listener for space controller events.
   */
  private final MasterEventListener masterEventListener = new BaseMasterEventListener() {
    @Override
    public void onSpaceControllerConnectAttempted(ActiveSpaceController controller) {
      handleSpaceControllerConnectAttempted(controller);
    }

    @Override
    public void onSpaceControllerConnectFailed(ActiveSpaceController controller, long waitedTime) {
      handleSpaceControllerConnectFailed(controller, waitedTime);
    }

    @Override
    public void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
      handleSpaceControllerDisconnectAttempted(controller);
    }

    @Override
    public void onSpaceControllerShutdown(ActiveSpaceController controller) {
      handleSpaceControllerShutdown(controller);
    }

    @Override
    public void onSpaceControllerHeartbeat(ActiveSpaceController controller, long timestamp) {
      handleSpaceControllerHeartbeat(controller, timestamp);
    }

    @Override
    public void onSpaceControllerHeartbeatLost(ActiveSpaceController controller, long timeSinceLastHeartbeat) {
      handleSpaceControllerHeartbeatLost(controller, timeSinceLastHeartbeat);
    }
  };

  /**
   * Control for the alert manager.
   */
  private ScheduledFuture<?> alertWatcherControl;

  /**
   * Number of milliseconds the alert watcher waits before scanning for activity state.
   */
  private final long alertWatcherDelay = WATCHER_DELAY_DEFAULT;

  /**
   * A mapping of controller UUIDs to the controller.
   */
  private final Map<String, SpaceControllerAlertWatcher> spaceControllerWatchers = Maps.newHashMap();

  /**
   * The alert service to use.
   */
  private AlertService alertService;

  /**
   * The event manager for the master.
   */
  private MasterEventManager masterEventManager;

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The active space controller manager.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  @Override
  public void startup() {
    masterEventManager.addListener(masterEventListener);

    alertWatcherControl = spaceEnvironment.getExecutorService().scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        scan();
      }
    }, alertWatcherDelay, alertWatcherDelay, TimeUnit.MILLISECONDS);

    spaceEnvironment.getLog().info("Master alert manager started");
  }

  @Override
  public void shutdown() {
    masterEventManager.removeListener(masterEventListener);

    if (alertWatcherControl != null) {
      alertWatcherControl.cancel(true);
      alertWatcherControl = null;
    }
  }

  /**
   * Scan for alerts.
   */
  public void scan() {
    long currentTimestamp = spaceEnvironment.getTimeProvider().getCurrentTime();

    for (SpaceControllerAlertWatcher watcher : getSpaceControllerWatchers()) {
      watcher.check(currentTimestamp);
    }
  }

  /**
   * Handle a connection attempt to a space controller.
   *
   * @param activeSpaceController
   *          the space controller
   */
  private void handleSpaceControllerConnectAttempted(ActiveSpaceController activeSpaceController) {
    addSpaceControllerWatcher(activeSpaceController);
  }

  /**
   * Handle a connection failure to a space controller.
   *
   * @param activeSpaceController
   *          the space controller
   * @param waitedTime
   *          the time waited for the space controller connection, in milliseconds
   */
  private void handleSpaceControllerConnectFailed(ActiveSpaceController activeSpaceController, long waitedTime) {
    disconnectAndRaiseAlert(activeSpaceController, waitedTime);
  }

  /**
   * Handle a disconnection attempt to a space controller.
   *
   * @param activeSpaceController
   *          the space controller
   */
  private void handleSpaceControllerDisconnectAttempted(ActiveSpaceController activeSpaceController) {
    removeSpaceControllerWatcher(activeSpaceController);
  }

  /**
   * Handle a space controller shutdown event.
   *
   * @param activeSpaceController
   *          the space controller
   */
  private void handleSpaceControllerShutdown(final ActiveSpaceController activeSpaceController) {
    // Have to go into future so not happening in comm threads.
    // TODO(keith): Eventually handle with callback on shutdown events for controller comms.
    spaceEnvironment.getExecutorService().schedule(new Runnable() {
      @Override
      public void run() {
        processSpaceControllerShutdown(activeSpaceController);
      }
    }, CONTROLLER_SHUTDOWN_EVENT_HANDLING_DELAY, TimeUnit.MILLISECONDS);
  }

  /**
   * Process the latter parts of a space controller shutdown.
   *
   * @param activeSpaceController
   *          the space controller being shut down
   */
  private void processSpaceControllerShutdown(ActiveSpaceController activeSpaceController) {
    spaceEnvironment.getLog().info(
        String.format("Space controller signalled shutdown: %s", activeSpaceController.getDisplayName()));

    // Remove heatbeat tester
    removeSpaceControllerWatcher(activeSpaceController);

    // Shutdown comms to the controller.
    disconnectSpaceController(activeSpaceController, false);

    // State is now unknown
    activeSpaceController.setState(SpaceControllerState.UNKNOWN);
  }

  /**
   * Handle a lost space controller heartbeat.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          the amount of time the heartbeat has been lost
   */
  private void handleSpaceControllerHeartbeatLost(ActiveSpaceController activeSpaceController,
      long timeSinceLastHeartbeat) {
    spaceEnvironment.getLog().warn(
        String.format("Lost heartbeat for space controller for %d msec: %s", timeSinceLastHeartbeat,
            activeSpaceController.getDisplayName()));

    disconnectAndRaiseAlert(activeSpaceController, timeSinceLastHeartbeat);
  }

  /**
   * Disconnect from a space controller that has lost its connection and raise an alert.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          the time since the last connection acknowledgement
   */
  private void disconnectAndRaiseAlert(final ActiveSpaceController activeSpaceController,
      final long timeSinceLastHeartbeat) {
    disconnectSpaceController(activeSpaceController, true);

    // TODO: Move alerting into an alerting system separate from a business logic manager that decides that an alert is
    // needed

    // Shouldn't block the thread with alert which could take time depending on what is being contacted, e.g. email
    // server.
    spaceEnvironment.getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        raiseHeartbeatLostAlert(activeSpaceController, timeSinceLastHeartbeat);
      }
    });
  }

  /**
   * Raise the heartbeat alert.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          how late the heartbeat is, in milliseconds
   */
  private void raiseHeartbeatLostAlert(ActiveSpaceController activeSpaceController, long timeSinceLastHeartbeat) {
    try {
      alertService.raiseAlert(ALERT_TYPE_CONTROLLER_TIMEOUT, activeSpaceController.getSpaceController().getUuid(),
          createAlertMessage(activeSpaceController, timeSinceLastHeartbeat));
    } catch (Throwable e) {
      spaceEnvironment.getLog().error(
          String.format("Lost heartbeat alert for space controller: %s", activeSpaceController.getDisplayName()), e);
    }
  }

  /**
   * Disconnect the master from a space controller.
   *
   * @param activeSpaceController
   *          the space controller to be disconnected from
   * @param fromError
   *          {@code true} if the disconnect comes from an error
   */
  private void disconnectSpaceController(ActiveSpaceController activeSpaceController, boolean fromError) {
    try {
      activeSpaceControllerManager.disconnectSpaceController(activeSpaceController.getSpaceController(), fromError);
    } catch (Throwable e) {
      spaceEnvironment.getLog().error(
          String.format("Lost heartbeat disconnect for space controller: %s", activeSpaceController.getDisplayName()),
          e);
    }
  }

  /**
   * Create a message for the alert.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          the amount of time for the scan
   *
   * @return the fully formatted message
   */
  private String createAlertMessage(ActiveSpaceController activeSpaceController, long timeSinceLastHeartbeat) {
    SpaceController controller = activeSpaceController.getSpaceController();

    String message =
        "No space controller heartbeat in %d milliseconds\n\n" + "ID: %s\nUUID: %s\nName: %s\nHostId: %s\n";
    return String.format(message, timeSinceLastHeartbeat, controller.getId(), controller.getUuid(),
        controller.getName(), controller.getHostId());
  }

  /**
   * Handle a controller heartbeat.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timestamp
   *          timestamp of the controller coming in
   */
  public void handleSpaceControllerHeartbeat(ActiveSpaceController activeSpaceController, long timestamp) {
    SpaceControllerAlertWatcher watcher =
        getSpaceControllerWatcher(activeSpaceController.getSpaceController().getUuid());

    if (watcher != null) {
      watcher.heartbeat(timestamp);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Master alert manager got heartbeat for unknown space controller %s", activeSpaceController
              .getSpaceController().getUuid()));
    }
  }

  /**
   * Add the watcher for a specific space controller.
   *
   * <p>
   * It will be given the timestamp of the current time.
   *
   * @param activeSpaceController
   *          the space controller
   */
  private void addSpaceControllerWatcher(ActiveSpaceController activeSpaceController) {
    long timestamp = spaceEnvironment.getTimeProvider().getCurrentTime();
    synchronized (spaceControllerWatchers) {
      SpaceControllerAlertWatcher watcher = new SpaceControllerAlertWatcher(activeSpaceController, timestamp);
      spaceControllerWatchers.put(activeSpaceController.getSpaceController().getUuid(), watcher);
    }
  }

  /**
   * Get the watcher for a specific space controller.
   *
   * @param uuid
   *          the UUID of the space controller
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
   * @param activeSpaceController
   *          the space controller
   */
  public void removeSpaceControllerWatcher(ActiveSpaceController activeSpaceController) {
    synchronized (spaceControllerWatchers) {
      spaceControllerWatchers.remove(activeSpaceController.getSpaceController().getUuid());
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
   * Get the master event listener the manager is using.
   *
   * @return the master event listener
   */
  @VisibleForTesting
  MasterEventListener getMasterEventListener() {
    return masterEventListener;
  }

  /**
   * Set the maximum amount of time willing to wait for a controller heartbeat before complaining.
   *
   * @param spaceControllerHeartbeatTime
   *          the time to wait in milliseconds
   */
  public void setSpaceControllerHeartbeatTime(long spaceControllerHeartbeatTime) {
    this.spaceControllerHeartbeatTime = spaceControllerHeartbeatTime;
  }

  /**
   * Get the maximum amount of time willing to wait for a controller heartbeat before complaining.
   *
   * @return the time to wait in milliseconds
   */
  public long getSpaceControllerHeartbeatTime() {
    return spaceControllerHeartbeatTime;
  }

  /**
   * Set the master event listener.
   *
   * @param masterEventManager
   *          the master event listener
   */
  public void setMasterEventManager(MasterEventManager masterEventManager) {
    this.masterEventManager = masterEventManager;
  }

  /**
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Set the alert service.
   *
   * @param alertService
   *          the alert service
   */
  public void setAlertService(AlertService alertService) {
    this.alertService = alertService;
  }

  /**
   * Set the active space controller manager.
   *
   * @param activeSpaceControllerManager
   *          the active space controller manager
   */
  public void setActiveSpaceControllerManager(ActiveSpaceControllerManager activeSpaceControllerManager) {
    this.activeSpaceControllerManager = activeSpaceControllerManager;
  }

  /**
   * The watcher for an individual space controller.
   *
   * @author Keith M. Hughes
   */
  public class SpaceControllerAlertWatcher {

    /**
     * The active space controller.
     */
    private final ActiveSpaceController activeSpaceController;

    /**
     * {@code true} if an alert has been sent.
     *
     * TODO(keith): make a strategy.
     */
    private volatile boolean alerted = false;

    /**
     * Construct a new alert watcher.
     *
     * @param activeSpaceController
     *          the space controller
     * @param timestamp
     *          timestamp for the alert
     */
    public SpaceControllerAlertWatcher(ActiveSpaceController activeSpaceController, long timestamp) {
      this.activeSpaceController = activeSpaceController;
      activeSpaceController.setHeartbeatTime(timestamp);
    }

    /**
     * New heartbeat coming in. Catch it.
     *
     * @param heartbeatTimestamp
     *          the new heartbeat
     */
    public void heartbeat(long heartbeatTimestamp) {
      activeSpaceController.setHeartbeatTime(heartbeatTimestamp);
      alerted = false;
    }

    /**
     * Check the current timestamp.
     *
     * @param currentTimestamp
     *          the time stamp to check against
     */
    public void check(long currentTimestamp) {
      Long timeSinceLastHeartbeat = activeSpaceController.timeSinceLastHeartbeat(currentTimestamp);
      if (timeSinceLastHeartbeat != null && timeSinceLastHeartbeat > spaceControllerHeartbeatTime && !alerted) {
        alerted = true;

        masterEventManager.signalSpaceControllerHeartbeatLost(activeSpaceController, timeSinceLastHeartbeat);
      }
    }
  }
}
