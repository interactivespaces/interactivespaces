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

import com.google.common.collect.Sets;

import interactivespaces.activity.ActivityState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.time.TimeProvider;

import java.util.Date;
import java.util.Set;

/**
 * A {@link LiveActivity} which is active.
 *
 * <p>
 * This class is not thread safe in and of itself. It is assumed that whoever is
 * calling it is thread safe.
 *
 * @author Keith M. Hughes
 */
public class ActiveLiveActivity {

  /**
   * The client for interacting with a controller remotely.
   */
  private RemoteControllerClient remoteControllerClient;

  /**
   * Controller the active activity is directRunning on.
   */
  private ActiveSpaceController activeController;

  /**
   * The activity which is now active.
   */
  private LiveActivity activity;

  /**
   * The state of the active activity.
   *
   * <p>
   * This is local knowledge about what is true about the remote version of the
   * activity.
   */
  private ActivityState runtimeState = ActivityState.UNKNOWN;

  /**
   * The deploy state of the activity.
   *
   * <p>
   * This is local knowledge about what is true about the remote version of the
   * activity.
   */
  private ActivityState deployState = ActivityState.UNKNOWN;

  /**
   * The last state update. {@code null} means there hasn't been one yet.
   */
  private Long lastStateUpdate;

  /**
   * Is the activity directRunning from a direct startup?
   */
  private boolean directRunning = false;

  /**
   * Is the activity activated from a direct call?
   */
  private boolean directActivated = false;

  /**
   * The time provider.
   */
  private TimeProvider timeProvider;

  /**
   * The groups which have requested live activity to run.
   */
  private Set<ActiveLiveActivityGroup> groupsRunning = Sets.newHashSet();

  /**
   * The groups which have requested live activity to activate.
   */
  private Set<ActiveLiveActivityGroup> groupsActivated = Sets.newHashSet();

  public ActiveLiveActivity(ActiveSpaceController activeController, LiveActivity activity,
      RemoteControllerClient remoteControllerClient, TimeProvider timeProvider) {
    this.activeController = activeController;
    this.activity = activity;
    this.remoteControllerClient = remoteControllerClient;
    this.timeProvider = timeProvider;
  }

  /**
   * @return the activeController
   */
  public ActiveSpaceController getActiveController() {
    return activeController;
  }

  /**
   * @return the live activity
   */
  public LiveActivity getLiveActivity() {
    return activity;
  }

  /**
   * Update the live activity object contained within.
   *
   * <p>
   * This allows this object access to merged data.
   *
   * @param liveActivity
   *          the potentially updated live activity entity
   */
  public void updateLiveActivity(LiveActivity liveActivity) {
    this.activity = liveActivity;
  }

  /**
   * Is the activity running?
   *
   * @return {@code true} if running
   */
  public synchronized boolean isRunning() {
    return directRunning || !groupsRunning.isEmpty();
  }

  /**
   * Is the live activity running from a direct call (not just from a group)?
   *
   * @return {@code true} if the activity was started up directly
   */
  public synchronized boolean isDirectRunning() {
    return directRunning;
  }

  /**
   * Get the number of groups which currently think they started the activity.
   *
   * @return the number of groups
   */
  public synchronized int getNumberLiveActivityGroupRunning() {
    return groupsRunning.size();
  }

  /**
   * Is the activity activated?
   *
   * @return {@code true} if activated
   */
  public synchronized boolean isActivated() {
    return directActivated || !groupsActivated.isEmpty();
  }

  /**
   * Is the live activity activated from a direct call (not just from a group)?
   *
   * @return {@code true} if the activity was activated directly
   */
  public synchronized boolean isDirectActivated() {
    return directActivated;
  }

  /**
   * Get the number of groups which currently think they activated the activity.
   *
   * @return the number of groups
   */
  public synchronized int getNumberLiveActivityGroupActivated() {
    return groupsActivated.size();
  }

  /**
   * Start up the activity directly.
   *
   * <p>
   * Nothing will happen if it is running already.
   */
  public synchronized void startup() {
    attemptRemoteStartup();

    // Make sure it is marked as a direct run.
    directRunning = true;
  }

  /**
   * Shut the activity down directly.
   *
   * <p>
   * Nothing will happen if it isn't running.
   */
  public synchronized void shutdown() {
    attemptRemoteShutdown();

    // Clean everyone out.
    clearRunningStateModel();
  }

  /**
   * Clear the running state model of the activity.
   */
  public synchronized void clearRunningStateModel() {
    directRunning = false;
    groupsRunning.clear();
    directActivated = false;
    groupsActivated.clear();
  }

  /**
   * Activate the activity directly.
   *
   * <p>
   * Nothing will happen if it is running already.
   */
  public synchronized void activate() {
    attemptRemoteActivation();

    // Make sure it is marked as a direct run.
    directActivated = true;
    directRunning = true;
  }

  /**
   * Deactivate the activity directly.
   *
   * <p>
   * Nothing will happen if it is not activated.
   */
  public synchronized void deactivate() {
    attemptRemoteDeactivation();

    // Clean everyone out.
    directActivated = false;
    groupsActivated.clear();
  }

  /**
   * Request the status of the remote activity.
   *
   * <p>
   * Nothing will happen if it is not activated.
   */
  public synchronized void status() {
    remoteControllerClient.statusActivity(this);
  }

  /**
   * Attempt a startup from a live activity group.
   *
   * @param group
   *          the live activity group
   */
  public synchronized void startupFromLiveActivityGroup(ActiveLiveActivityGroup group) {
    if (!isRunning()) {
      attemptRemoteStartup();
    }

    // OK to add if was already there
    groupsRunning.add(group);
  }

  /**
   * Attempt a shutdown from a live activity group.
   *
   * @param group
   *          the live activity group
   */
  public synchronized void shutdownFromLiveActivityGroup(ActiveLiveActivityGroup group) {
    if (groupsRunning.remove(group)) {
      if (!directRunning && groupsRunning.isEmpty()) {
        attemptRemoteShutdown();

        clearRunningStateModel();
      }
    }
  }

  /**
   * Attempt an activation from a live activity group.
   *
   * <p>
   * Throws an exception if the activity isn't running
   *
   * @param group
   *          the live activity group
   */
  public synchronized void activateFromLiveActivityGroup(ActiveLiveActivityGroup group) {
    if (!isActivated()) {
      attemptRemoteActivation();
    }

    // OK to add if was already there
    groupsActivated.add(group);
    groupsRunning.add(group);
  }

  /**
   * Attempt a deactivation from a live activity group.
   *
   * @param group
   *          the live activity group
   */
  public synchronized void deactivateFromLiveActivityGroup(ActiveLiveActivityGroup group) {
    if (groupsActivated.remove(group)) {
      if (!directActivated && groupsActivated.isEmpty()) {
        attemptRemoteDeactivation();
      }
    }
  }

  /**
   * Get the current local knowledge of the activity state.
   *
   * @return the state
   */
  public ActivityState getRuntimeState() {
    return runtimeState;
  }

  /**
   * Set the current local knowledge of the activity state.
   *
   * @param runtimeState
   *          the state to set
   */
  public void setRuntimeState(ActivityState runtimeState) {
    this.runtimeState = runtimeState;

    lastStateUpdate = timeProvider.getCurrentTime();
  }

  /**
   * Get the time of the last state update.
   *
   * @return The time in milliseconds from the epoch of the last status update.
   *         {@code null} means there hasn't been one yet.
   */
  public Long getLastStateUpdate() {
    return lastStateUpdate;
  }

  /**
   * Get the time of the last state update as a date.
   *
   * @return The time in milliseconds from the epoch of the last status update.
   *         {@code null} means there hasn't been one yet.
   */
  public Date getLastStateUpdateDate() {
    if (lastStateUpdate != null) {
      return new Date(lastStateUpdate);
    } else {
      return null;
    }
  }

  /**
   * Get the current local knowledge of the activity deploy state.
   *
   * @return the deploy state
   */
  public ActivityState getDeployState() {
    return deployState;
  }

  /**
   * Set the current local knowledge of the activity deploy state.
   *
   * @param deployState
   *          the state to set
   */
  public void setDeployState(ActivityState deployState) {
    this.deployState = deployState;
  }

  /**
   * Attempt a remote startup.
   */
  private void attemptRemoteStartup() {
    setRuntimeState(ActivityState.STARTUP_ATTEMPT);
    remoteControllerClient.startupActivity(this);
  }

  /**
   * Attempt a remote activation.
   */
  private void attemptRemoteActivation() {
    setRuntimeState(ActivityState.ACTIVATE_ATTEMPT);
    remoteControllerClient.activateActivity(this);
  }

  /**
   * Attempt a remote deactivation.
   */
  private void attemptRemoteDeactivation() {
    setRuntimeState(ActivityState.DEACTIVATE_ATTEMPT);
    remoteControllerClient.deactivateActivity(this);
  }

  /**
   * Attempt a remote shutdown.
   */
  private void attemptRemoteShutdown() {
    setRuntimeState(ActivityState.SHUTDOWN_ATTEMPT);
    remoteControllerClient.shutdownActivity(this);
  }
}
