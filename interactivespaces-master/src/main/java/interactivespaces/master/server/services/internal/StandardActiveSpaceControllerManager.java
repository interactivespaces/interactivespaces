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
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
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
   * The spaces being managed.
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
    spaceEnvironment.getLog().info(String.format("Connecting to controller %s", spaceController.getHostId()));

    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    activeSpaceController.setState(SpaceControllerState.CONNECT_ATTEMPT);
    remoteSpaceControllerClient.connect(activeSpaceController);
  }

  @Override
  public void disconnectSpaceController(SpaceController spaceController) {
    spaceEnvironment.getLog().info(String.format("Disconnecting from controller %s", spaceController.getHostId()));

    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.disconnect(activeSpaceController);
    activeSpaceController.setState(SpaceControllerState.UNKNOWN);
  }

  @Override
  public void restartSpaceController(SpaceController spaceController) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void statusSpaceController(SpaceController spaceController) {
    spaceEnvironment.getLog().info(String.format("Requesting status from controller %s", spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    if (!SpaceControllerState.UNKNOWN.equals(activeSpaceController.getState())) {
      remoteSpaceControllerClient.requestStatus(activeSpaceController);
    }
  }

  @Override
  public void forceStatusSpaceController(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Forcing status request from controller %s", spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.requestStatus(activeSpaceController);
  }

  @Override
  public void configureSpaceController(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Setting configuration of space controller %s", spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.configureSpaceController(activeSpaceController);
  }

  @Override
  public void cleanSpaceControllerTempData(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Requesting space controller temp data clean from space controller %s",
            spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.cleanControllerTempData(activeSpaceController);
  }

  @Override
  public void cleanSpaceControllerPermanentData(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Requesting space controller permanent data clean from space controller %s",
            spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.cleanControllerPermanentData(activeSpaceController);
  }

  @Override
  public void cleanSpaceControllerActivitiesTempData(SpaceController spaceController) {
    spaceEnvironment.getLog()
        .info(
            String.format("Requesting all activity temp data clean from space controller %s",
                spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.cleanControllerActivitiesTempData(activeSpaceController);
  }

  @Override
  public void cleanSpaceControllerActivitiesPermanentData(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Requesting all activities permanent data clean from space controller %s",
            spaceController.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.cleanControllerActivitiesPermanentData(activeSpaceController);
  }

  @Override
  public void captureSpaceControllerDataBundle(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Capturing data bundle for space controller %s", spaceController.getHostId()));

    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    activeSpaceController.setDataBundleState(DataBundleState.CAPTURE_REQUESTED);
    remoteSpaceControllerClient.captureControllerDataBundle(activeSpaceController);
  }

  @Override
  public void restoreSpaceControllerDataBundle(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Restoring data bundle for space controller %s", spaceController.getHostId()));

    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    activeSpaceController.setDataBundleState(DataBundleState.RESTORE_REQUESTED);
    remoteSpaceControllerClient.restoreControllerDataBundle(activeSpaceController);
  }

  @Override
  public void shutdownSpaceController(SpaceController spaceController) {
    spaceEnvironment.getLog().info(String.format("Shutting down space controller %s", spaceController.getHostId()));

    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.requestShutdown(activeSpaceController);

    // TODO(keith): Yuck!
    remoteSpaceControllerClient.getRemoteControllerClientListeners().signalSpaceControllerStatusChange(
        spaceController.getUuid(), SpaceControllerState.UNKNOWN);

    cleanLiveActivityStateModels(spaceController);
  }

  @Override
  public void shutdownAllActivities(SpaceController spaceController) {
    spaceEnvironment.getLog().info(
        String.format("Shutting down all apps on space controller %s", spaceController.getHostId()));

    // The async results will signal all active apps.
    ActiveSpaceController activeSpaceController = getActiveSpaceController(spaceController);
    remoteSpaceControllerClient.shutdownAllActivities(activeSpaceController);

    cleanLiveActivityStateModels(spaceController);
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
    deployActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deployActiveActivity(ActiveLiveActivity activeLiveActivity) {
    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    try {
      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Deploying live activity %s to space controller %s", liveActivity.getUuid(), liveActivity
                .getController().getHostId()));
      }

      activeLiveActivity.setDeployState(ActivityState.DEPLOY_ATTEMPT);

      remoteActivityDeploymentManager.deployLiveActivity(activeLiveActivity);
    } catch (Exception e) {
      activeLiveActivity.setDeployState(ActivityState.DEPLOY_FAILURE, e.getMessage());
      spaceEnvironment.getLog().error(
          String.format("could not deploy live activity %s to space controller %s", liveActivity.getUuid(),
              liveActivity.getController().getHostId()), e);
    }
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
          String.format("Deleting live activity %s from space controller %s", liveActivity.getUuid(), liveActivity
              .getController().getHostId()));
    }

    activeLiveActivity.setDeployState(ActivityState.DELETE_ATTEMPT);

    remoteActivityDeploymentManager.deleteLiveActivity(activeLiveActivity);
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
      remoteSpaceControllerClient.fullConfigureLiveActivity(active);
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
        String.format("Requesting activity %s activation", liveActivity.getLiveActivity().getUuid()));

    liveActivity.activate();
  }

  @Override
  public void deactivateLiveActivity(LiveActivity activity) {
    deactivateActiveActivity(getActiveLiveActivity(activity));
  }

  @Override
  public void deactivateActiveActivity(ActiveLiveActivity liveActivity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting activity %s deactivation", liveActivity.getLiveActivity().getUuid()));

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
        String.format("Requesting activity %s status", activeLiveActivity.getLiveActivity().getUuid()));

    activeLiveActivity.status();
  }

  @Override
  public void cleanLiveActivityPermanentData(LiveActivity activity) {
    cleanLiveActivityPermanentData(getActiveLiveActivity(activity));
  }

  @Override
  public void cleanLiveActivityPermanentData(ActiveLiveActivity activity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting permanent data clean for activity %s", activity.getLiveActivity().getUuid()));

    synchronized (activity) {
      remoteSpaceControllerClient.cleanActivityPermanentData(activity);
    }
  }

  @Override
  public void cleanLiveActivityTempData(LiveActivity activity) {
    cleanLiveActivityTempData(getActiveLiveActivity(activity));
  }

  @Override
  public void cleanLiveActivityTempData(ActiveLiveActivity activity) {
    spaceEnvironment.getLog().info(
        String.format("Requesting temp data clean for activity %s", activity.getLiveActivity().getUuid()));

    synchronized (activity) {
      remoteSpaceControllerClient.cleanActivityTempData(activity);
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
    spaceEnvironment.getLog().info(
        String.format("Requesting activity group %s deployment", activeActivityGroup.getActivityGroup().getId()));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      LiveActivity activity = groupActivity.getActivity();
      try {
        attemptDeployActiveActivityFromGroup(getActiveLiveActivity(activity), deployedLiveActivities);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Error while deploying live activity %s as part of live activity group %s",
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
   *          the live activities that have been deployed already (can be {@link null})
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
        String.format("Requesting activity group %s configure", activeActivityGroup.getActivityGroup().getId()));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      LiveActivity activity = groupActivity.getActivity();
      try {
        attemptConfigureActiveActivityFromGroup(getActiveLiveActivity(activity), configuredLiveActivities);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Error while configuring live activity %s as part of live activity group %s",
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
    spaceEnvironment.getLog().info(String.format("Requesting activity group %s startup", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Starting up live activity %s from group %s", activity.getUuid(), groupId));
      }

      try {
        getActiveLiveActivity(activity).startupFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Error while starting up live activity %s as part of live activity group %s",
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
    spaceEnvironment.getLog().info(String.format("requesting activity group %s activation", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Activating live activity %s from group %s", activity.getUuid(), groupId));
      }

      try {
        getActiveLiveActivity(activity).activateFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Error while activating live activity %s as part of live activity group %s",
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
    spaceEnvironment.getLog().info(String.format("Requesting activity group %s deactivation", groupId));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Deactivating live activity %s from group %s", activity.getUuid(), groupId));
      }

      try {
        getActiveLiveActivity(activity).deactivateFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Error while deactivating live activity %s as part of live activity group %s",
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

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getLiveActivities()) {
      LiveActivity activity = groupActivity.getActivity();

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        spaceEnvironment.getLog().info(
            String.format("Shut down live activity %s from group %s", activity.getUuid(), groupId));
      }

      ActiveLiveActivity activeLiveActivity = getActiveLiveActivity(activity);
      try {
        activeLiveActivity.shutdownFromLiveActivityGroup(activeActivityGroup);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Error while shutting down live activity %s as part of live activity group %s",
                activeLiveActivity.getDisplayName(), activeActivityGroup.getActivityGroup().getId()), e);
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
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController spaceController) {
    masterEventManager.signalSpaceControllerDisconnectAttempted(spaceController);
  }

  @Override
  public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      spaceController.setState(SpaceControllerState.RUNNING);
      if (spaceEnvironment.getLog().isDebugEnabled()) {
        spaceEnvironment.getLog().debug(
            String.format("Got heartbeat from %s (%s)", spaceController.getSpaceController().getName(),
                spaceController.getSpaceController().getUuid()));
      }

      masterEventManager.signalSpaceControllerHeartbeat(spaceController, timestamp);
    } else {
      spaceEnvironment.getLog().warn(String.format("Heartbeat from unknown space controller with UUID %s", uuid));
    }
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      spaceController.setState(state);
      if (spaceEnvironment.getLog().isDebugEnabled()) {
        spaceEnvironment.getLog().debug(
            String.format("Got space controller status update %s (%s) to %s", spaceController.getSpaceController()
                .getName(), spaceController.getSpaceController().getUuid(), state));
      }

      masterEventManager.signalSpaceControllerStatusChange(spaceController, state);
    } else {
      spaceEnvironment.getLog().warn(String.format("Status change for unknown controller with UUID %s", uuid));
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

        spaceEnvironment.getLog().info(
            String.format("Live activity deployed successfully: %s", active.getDisplayName()));
      } else {
        active.setDeployState(ActivityState.DEPLOY_FAILURE, result.getStatus().toString());

        spaceEnvironment.getLog().info(
            String.format("Live activity deployment failed %s: %s", result.getStatus(), active.getDisplayName()));
      }

      masterEventManager.signalLiveActivityDeploy(active, result, spaceEnvironment.getTimeProvider().getCurrentTime());
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
              String.format("Live activity deleted successfully: %s", active.getDisplayName()));

          break;

        case DOESNT_EXIST:
          active.setDeployState(ActivityState.DOESNT_EXIST);
          active.setRuntimeState(ActivityState.DOESNT_EXIST, null);

          spaceEnvironment.getLog().info(
              String.format("Live activity deletion attempt failed because it isn't on the controller: %s",
                  active.getDisplayName()));

          break;

        default:
          spaceEnvironment.getLog().info(String.format("Live activity %s delete failed", uuid));
      }

      masterEventManager.signalLiveActivityDelete(active, result, spaceEnvironment.getTimeProvider().getCurrentTime());
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onLiveActivityRuntimeStateChange(String uuid, ActivityState newState, String newStateDetail) {
    ActiveLiveActivity active = getActiveActivityByUuid(uuid);
    if (active != null) {
      spaceEnvironment.getLog().info(
          String.format("Remote live activity has reported state %s: %s", newState, active.getDisplayName()));

      ActivityState oldState;
      synchronized (active) {
        oldState = active.getRuntimeState();

        active.setRuntimeState(newState, newStateDetail);

        if (newState.equals(ActivityState.READY)) {
          active.clearRunningStateModel();
        }
      }

      masterEventManager.signalLiveActivityStateChange(active, oldState, newState);
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onDataBundleStateChange(String uuid, DataBundleState state) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      spaceController.setDataBundleState(state);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Data bundle state change update from unknown controller with UUID %s", uuid));
    }
  }

  @Override
  public void onSpaceControllerShutdown(String uuid) {
    ActiveSpaceController spaceController = getActiveControllerByUuid(uuid);
    if (spaceController != null) {
      masterEventManager.signalSpaceControllerShutdown(spaceController);
    } else {
      spaceEnvironment.getLog().warn(String.format("Shutdown from unknown controller with UUID %s", uuid));
    }
  }

  /**
   * Log that an unknown activity gave its status.
   *
   * @param uuid
   *          UUID of the unknown activity
   */
  private void logUnknownLiveActivity(String uuid) {
    spaceEnvironment.getLog().warn(String.format("Got activity status update for unknown activity %s", uuid));
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
