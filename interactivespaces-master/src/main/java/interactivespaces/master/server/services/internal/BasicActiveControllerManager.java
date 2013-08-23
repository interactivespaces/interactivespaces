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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import interactivespaces.activity.ActivityState;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivityGroup;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActivityDeploymentManager;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.master.server.services.SpaceControllerListenerHelper;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link ActiveControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicActiveControllerManager implements InternalActiveControllerManager,
    RemoteSpaceControllerClientListener {

  /**
   * All active controllers keyed by their controller's UUID.
   */
  private Map<String, ActiveSpaceController> activeSpaceControllers = Maps.newHashMap();

  /**
   * All active activities keyed by their live activity's UUID.
   */
  private Map<String, ActiveLiveActivity> activeActivities = Maps.newHashMap();

  /**
   * Active live activities mapped by the ID of the controller which contains
   * the live activity.
   */
  private Multimap<String, ActiveLiveActivity> activeActivitiesByController = HashMultimap.create();

  /**
   * All active activity groups keyed by their activity group's ID.
   */
  private Map<String, ActiveLiveActivityGroup> activeActivityGroups = Maps.newHashMap();

  /**
   * Listeners for events in the manager.
   */
  private SpaceControllerListenerHelper controllerListeners = new SpaceControllerListenerHelper();

  /**
   * The client for interacting with a controller remotely.
   */
  private RemoteControllerClient remoteControllerClient;

  /**
   * Manager for handling activity deployments.
   */
  private ActivityDeploymentManager activityDeploymentManager;

  /**
   * The spaces environment being run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public void connectController(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Connecting to controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    acontroller.setState(SpaceControllerState.CONNECT_ATTEMPT);
    remoteControllerClient.connect(acontroller);
  }

  @Override
  public void disconnectController(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Disconnecting from controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteControllerClient.disconnect(acontroller);
    acontroller.setState(SpaceControllerState.UNKNOWN);
  }

  @Override
  public void restartController(SpaceController controller) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void statusController(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Requesting status from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    if (!SpaceControllerState.UNKNOWN.equals(acontroller.getState())) {
      remoteControllerClient.requestStatus(acontroller);
    }
  }

  @Override
  public void forceStatusController(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Forcing status request from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteControllerClient.requestStatus(acontroller);
  }

  @Override
  public void captureControllerDataBundle(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("capturing data for controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteControllerClient.captureControllerDataBundle(acontroller);
  }

  @Override
  public void restoreControllerDataBundle(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Restoring data for controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteControllerClient.restoreControllerDataBundle(acontroller);
  }

  @Override
  public void shutdownController(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Shutting down controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteControllerClient.requestShutdown(acontroller);

    // TODO(keith): Yuck!
    remoteControllerClient.getRemoteControllerClientListeners().signalSpaceControllerStatusChange(
        controller.getUuid(), SpaceControllerState.UNKNOWN);

    cleanLiveActivityStateModels(controller);
  }

  @Override
  public void shutdownAllActivities(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Shutting down all apps on controller %s", controller.getHostId()));

    // The async results will signal all active apps.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteControllerClient.shutdownAllActivities(acontroller);

    cleanLiveActivityStateModels(controller);
  }

  /**
   * Clear all active live activity state models for the given controller
   *
   * @param controller
   *          the controller which has the activities
   */
  private void cleanLiveActivityStateModels(SpaceController controller) {
    synchronized (activeActivitiesByController) {
      for (ActiveLiveActivity activeLiveActivity : activeActivitiesByController.get(controller
          .getId())) {
        activeLiveActivity.clearRunningStateModel();
      }
    }
  }

  @Override
  public void deployLiveActivity(LiveActivity activity) {
    deployActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deployActiveActivity(ActiveLiveActivity activeLiveActivity) {
    if (spaceEnvironment.getLog().isInfoEnabled()) {
      LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
      spaceEnvironment.getLog().info(
          String.format("Deploying live activity %s to controller %s", liveActivity.getUuid(),
              liveActivity.getController().getHostId()));
    }

    synchronized (activeLiveActivity) {
      activeLiveActivity.setDeployState(ActivityState.DEPLOY_ATTEMPT);
    }

    activityDeploymentManager.deployLiveActivity(activeLiveActivity);
  }

  @Override
  public void deleteLiveActivity(LiveActivity activity) {
    deleteActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deleteActiveActivity(ActiveLiveActivity activeLiveActivity) {
    if (spaceEnvironment.getLog().isInfoEnabled()) {
      LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
      spaceEnvironment.getLog().info(
          String.format("Deleting live activity %s from controller %s", liveActivity.getUuid(),
              liveActivity.getController().getHostId()));
    }

    synchronized (activeLiveActivity) {
      activeLiveActivity.setDeployState(ActivityState.DELETE_ATTEMPT);
    }

    activityDeploymentManager.deleteLiveActivity(activeLiveActivity);
  }

  @Override
  public void configureLiveActivity(LiveActivity activity) {
    configureActiveLiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void configureActiveLiveActivity(ActiveLiveActivity active) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity %s configuration", active.getLiveActivity().getUuid()));

    synchronized (active) {
      remoteControllerClient.fullConfigureLiveActivity(active);
    }
  }

  @Override
  public void startupLiveActivity(LiveActivity activity) {
    startupActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void startupActiveActivity(ActiveLiveActivity liveActivity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity %s startup", liveActivity.getLiveActivity().getUuid()));

    liveActivity.startup();
  }

  @Override
  public void activateLiveActivity(LiveActivity activity) {
    activateActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void activateActiveActivity(ActiveLiveActivity liveActivity) {
    spaceEnvironment.getLog().info(
        String
            .format("Requesting activity %s activation", liveActivity.getLiveActivity().getUuid()));

    liveActivity.activate();
  }

  @Override
  public void deactivateLiveActivity(LiveActivity activity) {
    deactivateActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deactivateActiveActivity(ActiveLiveActivity liveActivity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity %s deactivation", liveActivity.getLiveActivity()
            .getUuid()));

    liveActivity.deactivate();
  }

  @Override
  public void shutdownLiveActivity(LiveActivity activity) {
    shutdownActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void shutdownActiveActivity(ActiveLiveActivity liveActivity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity %s shutdown", liveActivity.getLiveActivity().getUuid()));

    liveActivity.shutdown();
  }

  @Override
  public void statusLiveActivity(LiveActivity activity) {
    statusActiveLiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void statusActiveLiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity %s status", activeLiveActivity.getLiveActivity()
            .getUuid()));

    activeLiveActivity.status();
  }

  @Override
  public void deployLiveActivityGroup(LiveActivityGroup activeLiveActivityGroup) {
    deployActiveLiveActivityGroup(getActiveLiveActivityGroup(activeLiveActivityGroup));
  }

  @Override
  public void deployActiveLiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup) {
    deployActiveLiveActivityGroupChecked(activeLiveActivityGroup, null);
  }

  @Override
  public void deployActiveLiveActivityGroupChecked(ActiveLiveActivityGroup activeActivityGroup,
      Set<ActiveLiveActivity> deployedLiveActivities) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity group %s deployment", activeActivityGroup
            .getActivityGroup().getId()));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
      LiveActivity activity = groupActivity.getActivity();
      try {
        attemptDeployActiveActivityFromGroup(getActiveLiveActivity(activity),
            deployedLiveActivities);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format(
                "Error while deploying live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
      }
    }
  }

  /**
   * Attempt to deploy a particular activity from a group.
   *
   * <p>
   * The activity will only be deployed if it isn't already.
   *
   * @param liveActivity
   *          the live activity to deploy
   * @param deployedLiveActivities
   *          the live activities that have been deployed already (can be
   *          {@link null})
   */
  private void attemptDeployActiveActivityFromGroup(ActiveLiveActivity liveActivity,
      Set<ActiveLiveActivity> deployedLiveActivities) {
    // Only want a deploy if the activity isn't already deployed from this
    // round or if there is no tracking set.
    if (deployedLiveActivities == null) {
      deployActiveActivity(liveActivity);
    } else if (!deployedLiveActivities.contains(liveActivity)) {
      deployActiveActivity(liveActivity);
      deployedLiveActivities.add(liveActivity);
    }
  }

  @Override
  public void configureLiveActivityGroup(LiveActivityGroup activeLiveActivityGroup) {
    configureActiveActivityGroup(getActiveLiveActivityGroup(activeLiveActivityGroup));
  }

  @Override
  public void configureActiveActivityGroup(ActiveLiveActivityGroup activeActivityGroup) {
    configureActiveLiveActivityGroupChecked(activeActivityGroup, null);
  }

  @Override
  public void configureActiveLiveActivityGroupChecked(ActiveLiveActivityGroup activeActivityGroup,
      Set<ActiveLiveActivity> configuredLiveActivities) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity group %s configure", activeActivityGroup
            .getActivityGroup().getId()));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
      LiveActivity activity = groupActivity.getActivity();
      try {
        attemptConfigureActiveActivityFromGroup(getActiveLiveActivity(activity),
            configuredLiveActivities);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format(
                "Error while configuring live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
      }
    }
  }

  /**
   * Attempt to configure a particular activity from a group.
   *
   * <p>
   * The activity will only be configured if it isn't already.
   *
   * @param liveActivity
   *          the live activity to configure
   * @param configuredLiveActivities
   *          the live activities that have been configured already (can be
   *          {@link null})
   */
  private void attemptConfigureActiveActivityFromGroup(ActiveLiveActivity liveActivity,
      Set<ActiveLiveActivity> configuredLiveActivities) {
    // Only want a deploy if the activity isn't already deployed from this
    // round or if there is no tracking set.
    if (configuredLiveActivities == null) {
      configureActiveLiveActivity(liveActivity);
    } else if (!configuredLiveActivities.contains(liveActivity)) {
      configureActiveLiveActivity(liveActivity);
      configuredLiveActivities.add(liveActivity);
    }
  }

  @Override
  public void startupLiveActivityGroup(LiveActivityGroup activeLiveActivityGroup) {
    startupActiveActivityGroup(getActiveLiveActivityGroup(activeLiveActivityGroup));
  }

  @Override
  public void startupActiveActivityGroup(ActiveLiveActivityGroup activeActivityGroup) {
    String groupId = activeActivityGroup.getActivityGroup().getId();
    spaceEnvironment.getLog().info(String.format("Requesting activity group %s startup", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog()
            .info(
                String.format("Starting up live activity %s from group %s", activity.getUuid(),
                    groupId));
      }

      try {
        getActiveLiveActivity(activity).startupFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format(
                "Error while starting up live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
      }
    }
  }

  @Override
  public void activateLiveActivityGroup(LiveActivityGroup activeLiveActivityGroup) {
    activateActiveActivityGroup(getActiveLiveActivityGroup(activeLiveActivityGroup));
  }

  @Override
  public void activateActiveActivityGroup(ActiveLiveActivityGroup activeActivityGroup) {
    String groupId = activeActivityGroup.getActivityGroup().getId();
    spaceEnvironment.getLog().info(
        String.format("requesting activity group %s activation", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog()
            .info(
                String.format("Activating live activity %s from group %s", activity.getUuid(),
                    groupId));
      }

      try {
        getActiveLiveActivity(activity).activateFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format(
                "Error while activating live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
      }
    }
  }

  @Override
  public void deactivateLiveActivityGroup(LiveActivityGroup activeLiveActivityGroup) {
    deactivateActiveActivityGroup(getActiveLiveActivityGroup(activeLiveActivityGroup));
  }

  @Override
  public void deactivateActiveActivityGroup(ActiveLiveActivityGroup activeActivityGroup) {
    String groupId = activeActivityGroup.getActivityGroup().getId();
    spaceEnvironment.getLog().info(
        String.format("Requesting activity group %s deactivation", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Deactivating live activity %s from group %s", activity.getUuid(),
                groupId));
      }

      try {
        getActiveLiveActivity(activity).deactivateFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format(
                "Error while deactivating live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
      }
    }
  }

  @Override
  public void shutdownLiveActivityGroup(LiveActivityGroup activeLiveActivityGroup) {
    shutdownActiveActivityGroup(getActiveLiveActivityGroup(activeLiveActivityGroup));
  }

  @Override
  public void shutdownActiveActivityGroup(ActiveLiveActivityGroup activeActivityGroup) {
    String groupId = activeActivityGroup.getActivityGroup().getId();
    spaceEnvironment.getLog().info(String.format("Requesting activity group %s shutdown", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Shut down live activity %s from group %s", activity.getUuid(), groupId));
      }

      try {
        getActiveLiveActivity(activity).shutdownFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format(
                "Error while shutting down live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
      }
    }
  }

  @Override
  public ActiveSpaceController getActiveSpaceController(SpaceController controller) {
    String uuid = controller.getUuid();
    synchronized (activeSpaceControllers) {
      ActiveSpaceController activeController = activeSpaceControllers.get(uuid);
      if (activeController == null) {
        // Active controller doesn't exist yet.
        activeController =
            new ActiveSpaceController(controller, spaceEnvironment.getTimeProvider());
        activeSpaceControllers.put(controller.getUuid(), activeController);
      } else {
        activeController.updateController(controller);
      }

      return activeController;
    }
  }

  @Override
  public List<ActiveSpaceController> getActiveSpaceControllers(List<SpaceController> controllers) {
    List<ActiveSpaceController> results = Lists.newArrayList();

    synchronized (activeSpaceControllers) {
      for (SpaceController controller : controllers) {
        results.add(getActiveSpaceController(controller));
      }
    }

    return results;
  }

  @Override
  public ActiveLiveActivityGroup getActiveLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    synchronized (activeActivityGroups) {
      ActiveLiveActivityGroup lag = activeActivityGroups.get(liveActivityGroup.getId());
      if (lag == null) {
        lag = new ActiveLiveActivityGroup(liveActivityGroup);
        activeActivityGroups.put(liveActivityGroup.getId(), lag);
      } else {
        lag.updateLiveActivityGroup(liveActivityGroup);
      }

      return lag;
    }
  }

  @Override
  public ActiveLiveActivity getActiveLiveActivity(LiveActivity activity) {
    String uuid = activity.getUuid();
    synchronized (activeActivities) {
      ActiveLiveActivity active = activeActivities.get(uuid);
      if (active == null) {
        // Active activity doesn't exist yet.
        SpaceController controller = activity.getController();
        active =
            new ActiveLiveActivity(getActiveSpaceController(controller), activity,
                remoteControllerClient, spaceEnvironment.getTimeProvider());
        activeActivities.put(activity.getUuid(), active);
        activeActivitiesByController.put(controller.getId(), active);
      } else {
        active.updateLiveActivity(activity);
      }

      return active;
    }
  }

  @Override
  public List<ActiveLiveActivity> getActiveLiveActivities(List<LiveActivity> iactivities) {
    List<ActiveLiveActivity> activeLiveActivities = Lists.newArrayList();

    synchronized (activeActivities) {
      for (LiveActivity iactivity : iactivities) {
        activeLiveActivities.add(getActiveLiveActivity(iactivity));
      }
    }

    return activeLiveActivities;
  }

  /**
   * Get the active controller for the given UUID in a thread-friendly way.
   *
   * @param uuid
   *          the UUID of the controller
   *
   * @return the active controller associated with the uuid, or {@code null} if
   *         none
   */
  ActiveSpaceController getActiveControllerByUuid(String uuid) {
    synchronized (activeSpaceControllers) {
      return activeSpaceControllers.get(uuid);
    }
  }

  /**
   * Get the active activity for the given UUID in a thread-friendly way.
   *
   * @param uuid
   *          the UUID of the activity
   *
   * @return the active activity associated with the UUID or {@code null} if
   *         none
   */
  ActiveLiveActivity getActiveActivityByUuid(String uuid) {
    synchronized (activeActivities) {
      return activeActivities.get(uuid);
    }
  }

  @Override
  public void onSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    controllerListeners.signalSpaceControllerConnectAttempted(controller);
  }

  @Override
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    controllerListeners.signalSpaceControllerDisconnectAttempted(controller);
  }

  @Override
  public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
    ActiveSpaceController controller = getActiveControllerByUuid(uuid);
    if (controller != null) {
      controller.setState(SpaceControllerState.RUNNING);
      if (spaceEnvironment.getLog().isDebugEnabled()) {
        spaceEnvironment.getLog().debug(
            String.format("Got heartbeat from %s (%s)", controller.getController().getName(),
                controller.getController().getUuid()));
      }

      controllerListeners.signalSpaceControllerHeartbeat(uuid, timestamp);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Heartbeat from unknown controller with UUID %s", uuid));
    }
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    ActiveSpaceController controller = getActiveControllerByUuid(uuid);
    if (controller != null) {
      controller.setState(state);
      if (spaceEnvironment.getLog().isDebugEnabled()) {
        spaceEnvironment.getLog().debug(
            String.format("Got space controller status update %s (%s) to %s", controller
                .getController().getName(), controller.getController().getUuid(), state));
      }

      controllerListeners.signalSpaceControllerStatusChange(uuid, state);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Status change for unknown controller with UUID %s", uuid));
    }
  }

  @Override
  public void onLiveActivityInstall(String uuid, LiveActivityInstallResult result) {
    ActiveLiveActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      switch (result) {
        case SUCCESS:
          // If not running, should update the status as there may be an
          // error or something that is currently being shown.
          active.setDeployState(ActivityState.READY);

          spaceEnvironment.getLog().info(
              String.format("Live activity %s deployed successfully", uuid));

          break;
        case FAIL:
          active.setDeployState(ActivityState.DEPLOY_FAILURE);

          spaceEnvironment.getLog().info(String.format("Live activity %s deployment failed", uuid));
      }

      controllerListeners.signalActivityInstall(uuid, result, spaceEnvironment.getTimeProvider()
          .getCurrentTime());
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onLiveActivityDelete(String uuid, LiveActivityDeleteResult result) {
    ActiveLiveActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      switch (result) {
        case SUCCESS:
          // If not running, should update the status as there may be an
          // error or something that is currently being shown.
          active.setDeployState(ActivityState.UNKNOWN);
          active.setRuntimeState(ActivityState.UNKNOWN, null);
          active.getLiveActivity().setLastDeployDate(null);

          spaceEnvironment.getLog().info(
              String.format("Live activity %s deleted successfully", uuid));

          break;

        case DOESNT_EXIST:
          active.setDeployState(ActivityState.DOESNT_EXIST);
          active.setRuntimeState(ActivityState.DOESNT_EXIST, null);

          spaceEnvironment.getLog().info(
              String.format(
                  "Live activity %s deletion attempt failed because it isn't on the controller",
                  uuid));

          break;

        default:
          spaceEnvironment.getLog().info(String.format("Live activity %s delete failed", uuid));
      }

      controllerListeners.signalActivityDelete(uuid, result, spaceEnvironment.getTimeProvider()
          .getCurrentTime());
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onLiveActivityStateChange(String uuid, ActivityState newState, String newStateDetail) {
    ActiveLiveActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      ActivityState oldState;
      synchronized (active) {
        oldState = active.getRuntimeState();

        active.setRuntimeState(newState, newStateDetail);

        if (newState.equals(ActivityState.READY)) {
          active.clearRunningStateModel();
        }
      }

      controllerListeners.signalLiveActivityStateChange(uuid, oldState, newState);
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  /**
   * Log that an unknown activity gave its status.
   *
   * @param uuid
   *          UUID of the unknown activity
   */
  private void logUnknownLiveActivity(String uuid) {
    spaceEnvironment.getLog().warn(
        String.format("Got activity status update for unknown activity %s", uuid));
  }

  @Override
  public void addControllerListener(SpaceControllerListener listener) {
    controllerListeners.addListener(listener);
  }

  @Override
  public void removeControllerListener(SpaceControllerListener listener) {
    controllerListeners.removeListener(listener);
  }

  /**
   * @param remoteControllerClient
   *          the remoteControllerClient to set
   */
  public void setRemoteControllerClient(RemoteControllerClient remoteControllerClient) {
    this.remoteControllerClient = remoteControllerClient;

    remoteControllerClient.addRemoteSpaceControllerClientListener(this);
  }

  /**
   * @param activityDeploymentManager
   *          the activityDeploymentManager to set
   */
  public void setActivityDeploymentManager(ActivityDeploymentManager activityDeploymentManager) {
    this.activityDeploymentManager = activityDeploymentManager;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
