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

import interactivespaces.InteractiveSpacesExceptionUtils;
import interactivespaces.activity.ActivityState;
import interactivespaces.container.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.container.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.master.event.MasterEventManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivityGroup;
import interactivespaces.master.server.services.ActiveSpace;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.RemoteSpaceControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link ActiveSpaceControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardActiveSpaceControllerManager implements InternalActiveSpaceControllerManager,
    RemoteSpaceControllerClientListener {

  /**
   * All active controllers keyed by their controller's UUID.
   */
  private final Map<String, ActiveSpaceController> activeSpaceControllers = Maps.newHashMap();

  /**
   * All active activities keyed by their live activity's UUID.
   */
  private final Map<String, ActiveLiveActivity> activeLiveActivities = Maps.newHashMap();

  /**
   * Active live activities mapped by the ID of the controller which contains the live activity.
   */
  private final Multimap<String, ActiveLiveActivity> activeLiveActivitiesByController = HashMultimap.create();

  /**
   * All active activity groups keyed by their activity group's ID.
   */
  private final Map<String, ActiveLiveActivityGroup> activeLiveActivityGroups = Maps.newHashMap();

  /**
   * All active spaces keyed by their space's ID.
   */
  private final Map<String, ActiveSpace> activeSpaces = Maps.newHashMap();

  /**
   * Listeners for events in the manager.
   */
  private MasterEventManager masterEventManager;

  /**
   * The client for interacting with a controller remotely.
   */
  private RemoteSpaceControllerClient remoteSpaceControllerClient;

  /**
   * Manager for handling activity deployments.
   */
  private RemoteActivityDeploymentManager remoteActivityDeploymentManager;

  /**
   * The spaces environment being run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public void connectSpaceController(SpaceController spaceController) {
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    spaceEnvironment.getExtendedLog().formatInfo("Connecting to space controller %s",
        activeSpaceController.getDisplayName());

    remoteSpaceControllerClient.connectToSpaceController(activeSpaceController);
  }

  @Override
  public void disconnectSpaceController(SpaceController spaceController, boolean fromError) {
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    spaceEnvironment.getExtendedLog().formatInfo("Disconnecting from space controller %s",
        activeSpaceController.getDisplayName());

    remoteSpaceControllerClient.disconnectFromSpaceController(activeSpaceController);

    if (fromError) {
      updateSpaceControllerErrorState(activeSpaceController);
    } else {
      activeSpaceController.setState(SpaceControllerState.UNKNOWN);
    }
  }

  /**
   * Update the error state of a space controller.
   *
   * @param activeSpaceController
   *          the space controller
   */
  private void updateSpaceControllerErrorState(ActiveSpaceController activeSpaceController) {
    SpaceControllerState curState = activeSpaceController.getState();

    if (curState.isError()) {
      return;
    }

    if (curState == SpaceControllerState.CONNECT_ATTEMPT) {
      activeSpaceController.setState(SpaceControllerState.CONNECT_FAILURE);
    } else if (curState == SpaceControllerState.RUNNING) {
      activeSpaceController.setState(SpaceControllerState.CONNECTION_LOST);
    } else {
      spaceEnvironment.getExtendedLog().formatWarn(
          "Attempting to change space controller state in disconnect,"
              + " but the state %s does not make sense for an error", curState);
    }
  }

  @Override
  public void restartSpaceController(SpaceController spaceController) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void statusSpaceController(SpaceController spaceController) {
    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Requesting status from space controller %s",
        activeSpaceController.getDisplayName());

    if (!SpaceControllerState.UNKNOWN.equals(activeSpaceController.getState())) {
      requestSpaceControllerStatus(activeSpaceController);
    }
  }

  @Override
  public void forceStatusSpaceController(SpaceController spaceController) {
    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Forcing status request from space controller %s",
        activeSpaceController.getDisplayName());

    requestSpaceControllerStatus(activeSpaceController);
  }

  /**
   * Request the status from a space controller.
   *
   * @param activeSpaceController
   *          the active wrapper for the space controller
   */
  private void requestSpaceControllerStatus(ActiveSpaceController activeSpaceController) {
    try {
      remoteSpaceControllerClient.requestSpaceControllerStatus(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not get space controller status: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void configureSpaceController(SpaceController spaceController) {
    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Setting configuration of space controller %s",
        activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.configureSpaceController(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not configure space controller: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void cleanSpaceControllerTempData(SpaceController spaceController) {
    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog()
        .formatInfo("Requesting space controller temp data clean from space controller %s",
            activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.cleanSpaceControllerTempData(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not clean space controller temp data: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void cleanSpaceControllerPermanentData(SpaceController spaceController) {

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo(
        "Requesting space controller permanent data clean from space controller %s",
        activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.cleanSpaceControllerPermanentData(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not clean space controller permanent data: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void cleanSpaceControllerActivitiesTempData(SpaceController spaceController) {

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo(
        "Requesting all live activity temp data clean from space controller %s",
        activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.cleanSpaceControllerActivitiesTempData(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e,
          "Could not clean space controller live activity temp data: %s", activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void cleanSpaceControllerActivitiesPermanentData(SpaceController spaceController) {

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo(
        "Requesting all activities permanent data clean from space controller %s",
        activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.cleanSpaceControllerActivitiesPermanentData(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e,
          "Could not clean space controller live activity permanentdata : %s", activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void captureSpaceControllerDataBundle(SpaceController spaceController) {
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Capturing data bundle for space controller %s",
        activeSpaceController.getDisplayName());

    activeSpaceController.setDataBundleState(DataBundleState.CAPTURE_REQUESTED);
    try {
      remoteSpaceControllerClient.captureSpaceControllerDataBundle(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not capture space controller's data bundle: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void restoreSpaceControllerDataBundle(SpaceController spaceController) {

    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Restoring data bundle for space controller %s",
        activeSpaceController.getDisplayName());

    activeSpaceController.setDataBundleState(DataBundleState.RESTORE_REQUESTED);
    try {
      remoteSpaceControllerClient.restoreSpaceControllerDataBundle(activeSpaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not restore space controller data bundle: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void shutdownSpaceController(SpaceController spaceController) {
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Shutting down space controller %s",
        activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.requestSpaceControllerShutdown(activeSpaceController);

      // TODO(keith): Yuck!
      remoteSpaceControllerClient.getRemoteControllerClientListeners().signalSpaceControllerStatusChange(
          spaceController.getUuid(), SpaceControllerState.UNKNOWN);

      cleanLiveActivityStateModels(spaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e, "Could not shut down space controller: %s",
          activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  @Override
  public void shutdownAllActivities(SpaceController spaceController) {
    // The async results will signal all active apps.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);

    spaceEnvironment.getExtendedLog().formatInfo("Shutting down all apps on space controller %s",
        activeSpaceController.getDisplayName());

    try {
      remoteSpaceControllerClient.shutdownSpacecontrollerAllActivities(activeSpaceController);

      cleanLiveActivityStateModels(spaceController);
    } catch (Throwable e) {
      spaceEnvironment.getExtendedLog().formatError(e,
          "Could not shut down all activities on space controller: %s", activeSpaceController.getDisplayName());

      // Communication failures from space controllers are handled by other event mechanisms.
    }
  }

  /**
   * Clear all active live activity state models for the given space controller.
   *
   * @param spaceController
   *          the space controller
   */
  private void cleanLiveActivityStateModels(SpaceController spaceController) {
    synchronized (activeLiveActivitiesByController) {
      for (ActiveLiveActivity activeLiveActivity : activeLiveActivitiesByController.get(spaceController.getId())) {
        activeLiveActivity.clearRunningStateModel();
      }
    }
  }

  @Override
  public void deployLiveActivity(LiveActivity activity) {
    deployActiveLiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deployActiveLiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Deploying live activity to space controller %s: %s",
        activeLiveActivity.getActiveController().getDisplayName(), activeLiveActivity.getDisplayName());

    try {
      activeLiveActivity.setDeployState(ActivityState.DEPLOY_ATTEMPT);

      remoteActivityDeploymentManager.deployLiveActivity(activeLiveActivity);
    } catch (Throwable e) {
      activeLiveActivity.setDeployState(ActivityState.DEPLOY_FAILURE,
          InteractiveSpacesExceptionUtils.getExceptionDetail(e));

      spaceEnvironment.getExtendedLog().formatError(e, "could not deploy live activity to space controller %s: %s",
          activeLiveActivity.getActiveController().getDisplayName(), activeLiveActivity.getDisplayName());
    }
  }

  @Override
  public void deleteLiveActivity(LiveActivity activity) {
    deleteActiveLiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deleteActiveLiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Deleting live activity from space controller %s: %s",
        activeLiveActivity.getActiveController().getDisplayName(), activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      activeLiveActivity.setDeployState(ActivityState.DELETE_ATTEMPT);

      try {
        remoteActivityDeploymentManager.deleteLiveActivity(activeLiveActivity);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Could not delete live activity from space controller: %s", activeLiveActivity.getDisplayName());

        // Actual state is unknown since communication failed.
        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void configureLiveActivity(LiveActivity activity) {
    configureActiveLiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void configureActiveLiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting live activity configuration: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        remoteSpaceControllerClient.fullConfigureLiveActivity(activeLiveActivity);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e, "A live activity has failed to configure: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void startupLiveActivity(LiveActivity activity) {
    startupActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void startupActiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting live activity startup: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        activeLiveActivity.startup();
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e, "A live activity has failed to star up: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void activateLiveActivity(LiveActivity activity) {
    activateActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void activateActiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting live activity activation: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        activeLiveActivity.activate();
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError("A live activity has failed to activate: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void deactivateLiveActivity(LiveActivity activity) {
    deactivateActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deactivateActiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting live activity deactivation: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        activeLiveActivity.deactivate();
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e, "A live activity has failed to deactivate: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void shutdownLiveActivity(LiveActivity activity) {
    shutdownActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void shutdownActiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting live activity shutdown: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        activeLiveActivity.shutdown();
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e, "A live activity has failed to shut down: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void statusLiveActivity(LiveActivity activity) {
    statusActiveLiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void statusActiveLiveActivity(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting live activity status: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        activeLiveActivity.status();
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError("A live activity has failed to activate: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void cleanLiveActivityPermanentData(LiveActivity activity) {
    cleanLiveActivityPermanentData(getActiveLiveActivity(activity));
  }

  @Override
  public void cleanLiveActivityPermanentData(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting permanent data clean for live activity: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        remoteSpaceControllerClient.cleanLiveActivityPermanentData(activeLiveActivity);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e, "Could not clean live activity permanent data: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
  }

  @Override
  public void cleanLiveActivityTempData(LiveActivity activity) {
    cleanLiveActivityTempData(getActiveLiveActivity(activity));
  }

  @Override
  public void cleanLiveActivityTempData(ActiveLiveActivity activeLiveActivity) {
    spaceEnvironment.getExtendedLog().formatInfo("Requesting temp data clean for live activity: %s",
        activeLiveActivity.getDisplayName());

    synchronized (activeLiveActivity) {
      try {
        remoteSpaceControllerClient.cleanLiveActivityTempData(activeLiveActivity);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError("Could not clean live activity temp data: %s",
            activeLiveActivity.getDisplayName());

        updateLiveActivityStateForCommunicationFailure(activeLiveActivity, e);
      }
    }
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
    spaceEnvironment.getExtendedLog().formatInfo("Requesting activity group %s deployment",
        activeActivityGroup.getActivityGroup().getId());

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(groupActivity.getActivity());
      try {
        attemptDeployActiveActivityFromGroup(activeLiveActivity, deployedLiveActivities);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Error while deploying live activity %s as part of live activity group %s",
            activeLiveActivity.getDisplayName(), activeActivityGroup.getActivityGroup().getId());
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
   *          the live activities that have been deployed already (can be {@link null})
   */
  private void attemptDeployActiveActivityFromGroup(ActiveLiveActivity liveActivity,
      Set<ActiveLiveActivity> deployedLiveActivities) {
    // Only want a deploy if the activity isn't already deployed from this
    // round or if there is no tracking set.
    if (deployedLiveActivities == null) {
      deployActiveLiveActivity(liveActivity);
    } else if (!deployedLiveActivities.contains(liveActivity)) {
      deployActiveLiveActivity(liveActivity);
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
    spaceEnvironment.getExtendedLog().formatInfo("Requesting activity group %s configure",
        activeActivityGroup.getActivityGroup().getId());

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(groupActivity.getActivity());
      try {
        attemptConfigureActiveActivityFromGroup(activeLiveActivity, configuredLiveActivities);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Error while configuring live activity %s as part of live activity group %s",
            activeLiveActivity.getDisplayName(), activeActivityGroup.getActivityGroup().getId());
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
   *          the live activities that have been configured already (can be {@link null})
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
    spaceEnvironment.getExtendedLog().formatInfo("Requesting activity group %s startup", groupId);

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(groupActivity.getActivity());

      spaceEnvironment.getExtendedLog().formatInfo("Starting up live activity %s from group %s",
          activeLiveActivity.getDisplayName(), groupId);

      try {
        activeLiveActivity.startupFromLiveActivityGroup(activeActivityGroup);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Error while starting up live activity %s as part of live activity group %s",
            activeLiveActivity.getDisplayName(), activeActivityGroup.getActivityGroup().getId());
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
    spaceEnvironment.getExtendedLog().formatInfo("requesting activity group %s activation", groupId);

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(groupActivity.getActivity());

      spaceEnvironment.getExtendedLog().formatInfo("Activating live activity %s from group %s",
          activeLiveActivity.getDisplayName(), groupId);

      try {
        activeLiveActivity.activateFromLiveActivityGroup(activeActivityGroup);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Error while activating live activity %s as part of live activity group %s",
            activeLiveActivity.getDisplayName(), groupId);
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
    spaceEnvironment.getExtendedLog().formatInfo("Requesting activity group %s deactivation", groupId);

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(groupActivity.getActivity());

      spaceEnvironment.getExtendedLog().formatInfo("Deactivating live activity %s from group %s",
          activeLiveActivity.getDisplayName(), groupId);

      try {
        activeLiveActivity.deactivateFromLiveActivityGroup(activeActivityGroup);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Error while deactivating live activity %s as part of live activity group %s",
            activeLiveActivity.getDisplayName(), groupId);
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
    spaceEnvironment.getExtendedLog().formatInfo("Requesting activity group %s shutdown", groupId);

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(groupActivity.getActivity());

      spaceEnvironment.getExtendedLog().formatInfo("Shut down live activity %s from group %s",
          activeLiveActivity.getDisplayName(), groupId);

      try {
        activeLiveActivity.shutdownFromLiveActivityGroup(activeActivityGroup);
      } catch (Throwable e) {
        spaceEnvironment.getExtendedLog().formatError(e,
            "Error while shutting down live activity %s as part of live activity group %s",
            activeLiveActivity.getDisplayName(), groupId);
      }
    }
  }

  /**
   * Get the active space associated with a give space.
   *
   * @param space
   *          the space the active space is wanted for
   *
   * @return the active space for the space
   */
  public ActiveSpace getActiveSpace(Space space) {
    synchronized (activeSpaces) {
      ActiveSpace aspace = activeSpaces.get(space.getId());

      if (aspace == null) {
        aspace = new ActiveSpace(space);
        activeSpaces.put(space.getId(), aspace);
      } else {
        aspace.updateSpace(space);
      }

      return aspace;
    }
  }

  @Override
  public void deploySpace(Space space) {
    final Set<ActiveLiveActivity> deployedActivities = Sets.newHashSet();

    SpaceWalker walker = new SpaceWalker() {
      @Override
      protected void doVisit(Space space) {
        for (LiveActivityGroup activityGroup : space.getActivityGroups()) {
          deployActiveLiveActivityGroupChecked(getActiveLiveActivityGroup(activityGroup), deployedActivities);
        }
      }
    };

    walker.walk(space);
  }

  @Override
  public void configureSpace(Space space) {
    final Set<ActiveLiveActivity> configuredActivities = Sets.newHashSet();

    SpaceWalker walker = new SpaceWalker() {
      @Override
      protected void doVisit(Space space) {
        for (LiveActivityGroup activityGroup : space.getActivityGroups()) {
          configureActiveLiveActivityGroupChecked(getActiveLiveActivityGroup(activityGroup), configuredActivities);
        }
      }
    };

    walker.walk(space);
  }

  @Override
  public void startupSpace(Space space) {
    SpaceWalker walker = new SpaceWalker() {
      @Override
      protected void doVisit(Space aspace) {
        for (LiveActivityGroup activityGroup : aspace.getActivityGroups()) {
          startupActiveActivityGroup(getActiveLiveActivityGroup(activityGroup));
        }
      }
    };

    walker.walk(space);
  }

  @Override
  public void shutdownSpace(Space space) {
    SpaceWalker walker = new SpaceWalker() {
      @Override
      protected void doVisit(Space space) {
        for (LiveActivityGroup activityGroup : space.getActivityGroups()) {
          shutdownActiveActivityGroup(getActiveLiveActivityGroup(activityGroup));
        }
      }
    };

    walker.walk(space);
  }

  @Override
  public void activateSpace(Space space) {
    SpaceWalker walker = new SpaceWalker() {
      @Override
      protected void doVisit(Space space) {
        for (LiveActivityGroup activityGroup : space.getActivityGroups()) {
          activateActiveActivityGroup(getActiveLiveActivityGroup(activityGroup));
        }
      }
    };

    walker.walk(space);
  }

  @Override
  public void deactivateSpace(Space space) {
    SpaceWalker walker = new SpaceWalker() {
      @Override
      protected void doVisit(Space space) {
        for (LiveActivityGroup activityGroup : space.getActivityGroups()) {
          deactivateActiveActivityGroup(getActiveLiveActivityGroup(activityGroup));
        }
      }
    };

    walker.walk(space);
  }

  @Override
  public ActiveSpaceController getActiveSpaceController(SpaceController spaceController) {
    String uuid = spaceController.getUuid();
    synchronized (activeSpaceControllers) {
      ActiveSpaceController activeController = activeSpaceControllers.get(uuid);
      if (activeController == null) {
        // Active space controller doesn't exist yet.
        activeController = new ActiveSpaceController(spaceController, spaceEnvironment.getTimeProvider());
        activeSpaceControllers.put(spaceController.getUuid(), activeController);
      } else {
        activeController.updateController(spaceController);
      }

      return activeController;
    }
  }

  @Override
  public List<ActiveSpaceController> getActiveSpaceControllers(List<SpaceController> controllers) {
    List<ActiveSpaceController> results = Lists.newArrayList();

    synchronized (activeSpaceControllers) {
      for (SpaceController spaceController : controllers) {
        results.add(getActiveSpaceController(spaceController));
      }
    }

    return results;
  }

  @Override
  public ActiveLiveActivityGroup getActiveLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    synchronized (activeLiveActivityGroups) {
      ActiveLiveActivityGroup activeLiveActivityGroup = activeLiveActivityGroups.get(liveActivityGroup.getId());
      if (activeLiveActivityGroup == null) {
        activeLiveActivityGroup = new ActiveLiveActivityGroup(liveActivityGroup);
        activeLiveActivityGroups.put(activeLiveActivityGroup.getActivityGroup().getId(), activeLiveActivityGroup);
      } else {
        activeLiveActivityGroup.updateLiveActivityGroup(liveActivityGroup);
      }

      return activeLiveActivityGroup;
    }
  }

  @Override
  public ActiveLiveActivity getActiveLiveActivity(LiveActivity activity) {
    String uuid = activity.getUuid();
    synchronized (activeLiveActivities) {
      ActiveLiveActivity active = activeLiveActivities.get(uuid);
      if (active == null) {
        // Active activity doesn't exist yet.
        SpaceController spaceController = activity.getController();
        active =
            new ActiveLiveActivity(getActiveSpaceController(spaceController), activity, remoteSpaceControllerClient,
                spaceEnvironment.getTimeProvider());
        activeLiveActivities.put(activity.getUuid(), active);
        activeLiveActivitiesByController.put(spaceController.getId(), active);
      } else {
        active.updateLiveActivity(activity);
      }

      return active;
    }
  }

  @Override
  public List<ActiveLiveActivity> getActiveLiveActivities(List<LiveActivity> iactivities) {
    List<ActiveLiveActivity> activeLiveActivities = Lists.newArrayList();

    synchronized (activeLiveActivities) {
      for (LiveActivity iactivity : iactivities) {
        activeLiveActivities.add(getActiveLiveActivity(iactivity));
      }
    }

    return activeLiveActivities;
  }

  /**
   * Get the active space controller for the given UUID in a thread-friendly way.
   *
   * @param uuid
   *          the UUID of the space controller
   *
   * @return the active space controller associated with the uuid, or {@code null} if none
   */
  @VisibleForTesting
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
   * @return the active activity associated with the UUID or {@code null} if none
   */
  ActiveLiveActivity getActiveActivityByUuid(String uuid) {
    synchronized (activeLiveActivities) {
      return activeLiveActivities.get(uuid);
    }
  }

  @Override
  public void onSpaceControllerConnectAttempted(ActiveSpaceController spaceController) {
    masterEventManager.signalSpaceControllerConnectAttempted(spaceController);
  }

  @Override
  public void onSpaceControllerConnectFailed(ActiveSpaceController spaceController, long timeToWait) {
    masterEventManager.signalSpaceControllerConnectFailed(spaceController, timeToWait);
  }

  @Override
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController spaceController) {
    masterEventManager.signalSpaceControllerDisconnectAttempted(spaceController);
  }

  @Override
  public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      spaceController.setState(SpaceControllerState.RUNNING);
      if (spaceEnvironment.getLog().isDebugEnabled()) {
        spaceEnvironment.getExtendedLog().formatDebug("Got heartbeat from %s", spaceController.getDisplayName());
      }

      masterEventManager.signalSpaceControllerHeartbeat(spaceController, timestamp);
    } else {
      spaceEnvironment.getExtendedLog().formatWarn("Heartbeat from unknown space controller with UUID %s", uuid);
    }
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      spaceController.setState(state);

      spaceEnvironment.getExtendedLog().formatDebug("Got space controller status update %s to %s",
          spaceController.getDisplayName(), state);

      masterEventManager.signalSpaceControllerStatusChange(spaceController, state);
    } else {
      spaceEnvironment.getExtendedLog().formatWarn("Status change for unknown controller with UUID %s", uuid);
    }
  }

  @Override
  public void onLiveActivityDeployment(String uuid, LiveActivityDeploymentResponse result) {
    ActiveLiveActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      if (result.getStatus().isSuccess()) {

        // If not running, should update the status as there may be an
        // error or something that is currently being shown.
        active.setDeployState(ActivityState.READY);

        spaceEnvironment.getExtendedLog().formatInfo("Live activity deployed successfully: %s",
            active.getDisplayName());
      } else {
        String deployStatusDetail =
            String.format("%s: %s", result.getStatus().getDescription(), result.getStatusDetail());
        active.setDeployState(ActivityState.DEPLOY_FAILURE, deployStatusDetail);

        spaceEnvironment.getExtendedLog().formatError("Live activity %s deployment failed: %s",
            active.getDisplayName(), deployStatusDetail);
      }

      masterEventManager.signalLiveActivityDeploy(active, result, spaceEnvironment.getTimeProvider().getCurrentTime());
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onLiveActivityDelete(String uuid, LiveActivityDeleteResponse result) {
    ActiveLiveActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      switch (result.getStatus()) {
        case SUCCESS:
          // If not running, should update the status as there may be an
          // error or something that is currently being shown.
          active.setDeployState(ActivityState.UNKNOWN);
          active.setRuntimeState(ActivityState.UNKNOWN, null);
          active.getLiveActivity().setLastDeployDate(null);

          spaceEnvironment.getExtendedLog().formatInfo("Live activity deleted successfully: %s",
              active.getDisplayName());

          break;

        case DOESNT_EXIST:
          active.setDeployState(ActivityState.DOESNT_EXIST);
          active.setRuntimeState(ActivityState.DOESNT_EXIST, null);

          spaceEnvironment.getExtendedLog().formatWarn(
              "Live activity deletion attempt failed because it isn't on the controller: %s", active.getDisplayName());

          break;

        default:
          spaceEnvironment.getExtendedLog().formatError("Live activity delete failed: %s", active.getDisplayName(),
              result.getStatusDetail());
      }

      masterEventManager.signalLiveActivityDelete(active, result);
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onLiveActivityRuntimeStateChange(String uuid, ActivityState newState, String newStateDetail) {
    ActiveLiveActivity activeLiveActivity = getActiveActivityByUuid(uuid);
    if (activeLiveActivity != null) {
      spaceEnvironment.getExtendedLog().formatInfo("Remote live activity has reported state %s: %s", newState,
          activeLiveActivity.getDisplayName());

      updateLiveActivityRuntimeState(activeLiveActivity, newState, newStateDetail);
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  /**
   * Update the live activity state due to a communication failure.
   *
   * @param activeLiveActivity
   *          the live activity
   * @param e
   *          the exception that signaled the failure
   */
  private void updateLiveActivityStateForCommunicationFailure(ActiveLiveActivity activeLiveActivity, Throwable e) {
    updateLiveActivityRuntimeState(activeLiveActivity, ActivityState.UNKNOWN,
        InteractiveSpacesExceptionUtils.getExceptionDetail(e));
  }

  /**
   * Update the runtime state of a live activity.
   *
   * @param activeLiveActivity
   *          the live activity
   * @param newState
   *          the new state
   * @param newStateDetail
   *          the detail of the new state
   */
  private void updateLiveActivityRuntimeState(ActiveLiveActivity activeLiveActivity, ActivityState newState,
      String newStateDetail) {
    ActivityState oldState;
    synchronized (activeLiveActivity) {
      oldState = activeLiveActivity.getRuntimeState();

      activeLiveActivity.setRuntimeState(newState, newStateDetail);

      if (!newState.isRunning()) {
        activeLiveActivity.clearRunningStateModel();
      }
    }

    masterEventManager.signalLiveActivityRuntmeStateChange(activeLiveActivity, oldState, newState);
  }

  @Override
  public void onDataBundleStateChange(String uuid, DataBundleState state) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      spaceController.setDataBundleState(state);
    } else {
      spaceEnvironment.getExtendedLog().formatWarn(
          "Data bundle state change update from unknown controller with UUID %s", uuid);
    }
  }

  @Override
  public void onSpaceControllerShutdown(String uuid) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      masterEventManager.signalSpaceControllerShutdown(spaceController);
    } else {
      spaceEnvironment.getExtendedLog().formatWarn("Shutdown from unknown controller with UUID %s", uuid);
    }
  }

  /**
   * Log that an unknown activity gave its status.
   *
   * @param uuid
   *          UUID of the unknown activity
   */
  private void logUnknownLiveActivity(String uuid) {
    spaceEnvironment.getExtendedLog().formatWarn("Got activity status update for unknown activity %s", uuid);
  }

  /**
   * Set the master event manager.
   *
   * @param masterEventManager
   *          the master event manager
   */
  public void setMasterEventManager(MasterEventManager masterEventManager) {
    this.masterEventManager = masterEventManager;
  }

  /**
   * Set the remote space controller client.
   *
   * @param remoteSpaceControllerClient
   *          the remote space controller client to use
   */
  public void setRemoteSpaceControllerClient(RemoteSpaceControllerClient remoteSpaceControllerClient) {
    this.remoteSpaceControllerClient = remoteSpaceControllerClient;

    remoteSpaceControllerClient.addRemoteSpaceControllerClientListener(this);
  }

  /**
   * Set the remote activity deployment manager to use.
   *
   * @param remoteActivityDeploymentManager
   *          the remote activity deployment manager
   */
  public void setRemoteActivityDeploymentManager(RemoteActivityDeploymentManager remoteActivityDeploymentManager) {
    this.remoteActivityDeploymentManager = remoteActivityDeploymentManager;
  }

  /**
   * Set the space environment to use.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
