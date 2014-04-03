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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivityGroup;
import interactivespaces.master.server.services.ActiveSpace;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteSpaceControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.master.server.services.SpaceControllerListenerHelper;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link ActiveSpaceControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicActiveSpaceControllerManager implements InternalActiveSpaceControllerManager,
    RemoteSpaceControllerClientListener {

  /**
   * All active controllers keyed by their controller's UUID.
   */
  private final Map<String, ActiveSpaceController> activeSpaceControllers = Maps.newHashMap();

  /**
   * All active activities keyed by their live activity's UUID.
   */
  private final Map<String, ActiveLiveActivity> activeActivities = Maps.newHashMap();

  /**
   * Active live activities mapped by the ID of the controller which contains
   * the live activity.
   */
  private final Multimap<String, ActiveLiveActivity> activeActivitiesByController = HashMultimap.create();

  /**
   * All active activity groups keyed by their activity group's ID.
   */
  private final Map<String, ActiveLiveActivityGroup> activeActivityGroups = Maps.newHashMap();

  /**
   * The spaces being managed.
   */
  private final Map<String, ActiveSpace> activeSpaces = Maps.newHashMap();

  /**
   * Listeners for events in the manager.
   */
  private final SpaceControllerListenerHelper controllerListeners = new SpaceControllerListenerHelper();

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
  public void connectSpaceController(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Connecting to controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    acontroller.setState(SpaceControllerState.CONNECT_ATTEMPT);
    remoteSpaceControllerClient.connect(acontroller);
  }

  @Override
  public void disconnectSpaceController(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Disconnecting from controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.disconnect(acontroller);
    acontroller.setState(SpaceControllerState.UNKNOWN);
  }

  @Override
  public void restartSpaceController(SpaceController controller) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void statusSpaceController(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Requesting status from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    if (!SpaceControllerState.UNKNOWN.equals(acontroller.getState())) {
      remoteSpaceControllerClient.requestStatus(acontroller);
    }
  }

  @Override
  public void forceStatusSpaceController(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Forcing status request from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.requestStatus(acontroller);
  }

  @Override
  public void cleanSpaceControllerTempData(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Requesting controller temp data clean from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.cleanControllerTempData(acontroller);
  }

  @Override
  public void cleanSpaceControllerPermanentData(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Requesting controller permanent data clean from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.cleanControllerPermanentData(acontroller);
  }

  @Override
  public void cleanSpaceControllerActivitiesTempData(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Requesting all activity temp data clean from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.cleanControllerActivitiesTempData(acontroller);
  }

  @Override
  public void cleanSpaceControllerActivitiesPermanentData(SpaceController controller) {
    spaceEnvironment.getLog().info(
        String.format("Requesting all activities permanent data clean from controller %s", controller.getHostId()));

    // To make sure something is listening for the request.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.cleanControllerActivitiesPermanentData(acontroller);
  }

  @Override
  public void captureSpaceControllerDataBundle(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Capturing data bundle for controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    acontroller.setDataBundleState(DataBundleState.CAPTURE_REQUESTED);
    remoteSpaceControllerClient.captureControllerDataBundle(acontroller);
  }

  @Override
  public void restoreSpaceControllerDataBundle(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Restoring data bundle for controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    acontroller.setDataBundleState(DataBundleState.RESTORE_REQUESTED);
    remoteSpaceControllerClient.restoreControllerDataBundle(acontroller);
  }

  @Override
  public void shutdownSpaceController(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Shutting down controller %s", controller.getHostId()));

    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.requestShutdown(acontroller);

    // TODO(keith): Yuck!
    remoteSpaceControllerClient.getRemoteControllerClientListeners().signalSpaceControllerStatusChange(
        controller.getUuid(), SpaceControllerState.UNKNOWN);

    cleanLiveActivityStateModels(controller);
  }

  @Override
  public void shutdownAllActivities(SpaceController controller) {
    spaceEnvironment.getLog().info(String.format("Shutting down all apps on controller %s", controller.getHostId()));

    // The async results will signal all active apps.
    ActiveSpaceController acontroller = getActiveSpaceController(controller);
    remoteSpaceControllerClient.shutdownAllActivities(acontroller);

    cleanLiveActivityStateModels(controller);
  }

  /**
   * Clear all active live activity state models for the given controller.
   *
   * @param controller
   *          the controller which has the activities
   */
  private void cleanLiveActivityStateModels(SpaceController controller) {
    synchronized (activeActivitiesByController) {
      for (ActiveLiveActivity activeLiveActivity : activeActivitiesByController.get(controller.getId())) {
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
            String.format("Deploying live activity %s to controller %s", liveActivity.getUuid(), liveActivity
                .getController().getHostId()));
      }

      activeLiveActivity.setDeployState(ActivityState.DEPLOY_ATTEMPT);

      remoteActivityDeploymentManager.deployLiveActivity(activeLiveActivity);
    } catch (Exception e) {
      activeLiveActivity.setDeployState(ActivityState.DEPLOY_FAILURE, e.getMessage());
      spaceEnvironment.getLog().error(
          String.format("could not deploy live activity %s to controller %s", liveActivity.getUuid(), liveActivity
              .getController().getHostId()), e);
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
          String.format("Deleting live activity %s from controller %s", liveActivity.getUuid(), liveActivity
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

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
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
        String.format("Requesting activity group %s configure", activeActivityGroup.getActivityGroup().getId()));

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
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

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
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

    for (GroupLiveActivity groupActivity : activeActivityGroup.getActivityGroup().getActivities()) {
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
            String.format("Error while shutting down live activity %s as part of live activity group %s",
                activity.getUuid(), activeActivityGroup.getActivityGroup().getId()), e);
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
  public ActiveSpaceController getActiveSpaceController(SpaceController controller) {
    String uuid = controller.getUuid();
    synchronized (activeSpaceControllers) {
      ActiveSpaceController activeController = activeSpaceControllers.get(uuid);
      if (activeController == null) {
        // Active controller doesn't exist yet.
        activeController = new ActiveSpaceController(controller, spaceEnvironment.getTimeProvider());
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
      ActiveLiveActivityGroup activeLiveActivityGroup = activeActivityGroups.get(liveActivityGroup.getId());
      if (activeLiveActivityGroup == null) {
        activeLiveActivityGroup = new ActiveLiveActivityGroup(liveActivityGroup);
        activeActivityGroups.put(activeLiveActivityGroup.getActivityGroup().getId(), activeLiveActivityGroup);
      } else {
        activeLiveActivityGroup.updateLiveActivityGroup(liveActivityGroup);
      }

      return activeLiveActivityGroup;
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
            new ActiveLiveActivity(getActiveSpaceController(controller), activity, remoteSpaceControllerClient,
                spaceEnvironment.getTimeProvider());
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
            String.format("Got heartbeat from %s (%s)", controller.getController().getName(), controller
                .getController().getUuid()));
      }

      controllerListeners.signalSpaceControllerHeartbeat(uuid, timestamp);
    } else {
      spaceEnvironment.getLog().warn(String.format("Heartbeat from unknown controller with UUID %s", uuid));
    }
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    ActiveSpaceController controller = getActiveControllerByUuid(uuid);
    if (controller != null) {
      controller.setState(state);
      if (spaceEnvironment.getLog().isDebugEnabled()) {
        spaceEnvironment.getLog().debug(
            String.format("Got space controller status update %s (%s) to %s", controller.getController().getName(),
                controller.getController().getUuid(), state));
      }

      controllerListeners.signalSpaceControllerStatusChange(uuid, state);
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

        spaceEnvironment.getLog().info(String.format("Live activity %s deployed successfully", uuid));

      } else {
        active.setDeployState(ActivityState.DEPLOY_FAILURE, result.getStatus().toString());

        spaceEnvironment.getLog()
            .info(String.format("Live activity %s deployment failed %s", uuid, result.getStatus()));
      }

      controllerListeners.signalActivityInstall(uuid, result, spaceEnvironment.getTimeProvider().getCurrentTime());
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

          spaceEnvironment.getLog().info(String.format("Live activity %s deleted successfully", uuid));

          break;

        case DOESNT_EXIST:
          active.setDeployState(ActivityState.DOESNT_EXIST);
          active.setRuntimeState(ActivityState.DOESNT_EXIST, null);

          spaceEnvironment.getLog().info(
              String.format("Live activity %s deletion attempt failed because it isn't on the controller", uuid));

          break;

        default:
          spaceEnvironment.getLog().info(String.format("Live activity %s delete failed", uuid));
      }

      controllerListeners.signalActivityDelete(uuid, result, spaceEnvironment.getTimeProvider().getCurrentTime());
    } else {
      logUnknownLiveActivity(uuid);
    }
  }

  @Override
  public void onLiveActivityRuntimeStateChange(String uuid, ActivityState newState, String newStateDetail) {
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

  @Override
  public void onDataBundleStateChange(String uuid, DataBundleState state) {
    ActiveSpaceController controller = getActiveControllerByUuid(uuid);
    if (controller != null) {
      controller.setDataBundleState(state);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Data bundle state change update from unknown controller with UUID %s", uuid));
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

  @Override
  public void addSpaceControllerListener(SpaceControllerListener listener) {
    controllerListeners.addListener(listener);
  }

  @Override
  public void removeSpaceControllerListener(SpaceControllerListener listener) {
    controllerListeners.removeListener(listener);
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
   * @param remoteActivityDeploymentManager
   *          the remote activity deployment manager to use
   */
  public void setRemoteActivityDeploymentManager(RemoteActivityDeploymentManager remoteActivityDeploymentManager) {
    this.remoteActivityDeploymentManager = remoteActivityDeploymentManager;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
