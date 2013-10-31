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

package interactivespaces.master.server.ui.internal;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerMode;
import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.master.server.ui.UiLiveActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * A simple controller manager for UIs.
 *
 * @author Keith M. Hughes
 */
public class BasicUiControllerManager implements UiControllerManager {

  /**
   * Repository for obtaining controller entities.
   */
  private ControllerRepository controllerRepository;

  /**
   * Repository for obtaining activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Handle operations on remote controllers.
   */
  private ActiveControllerManager activeControllerManager;

  /**
   * Interactive Spaces environment being run in.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public UiControllerManager deleteController(String id) {
    controllerRepository.deleteSpaceController(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager connectToAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.connectController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to connect to controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager disconnectFromAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.disconnectController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to disconnect to controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager statusFromAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.statusController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to get the status from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager forceStatusFromAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.forceStatusController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to force the status from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager shutdownControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeControllerManager.shutdownController(controller);
        } catch (Exception e) {
          spaceEnvironment.getLog().error(
              String.format("Unable to shut down controller %s (%s)", controller.getUuid(),
                  controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

        throw new EntityNotFoundInteractiveSpacesException(String.format(
            "Space controller with ID %s not found", id));
      }
    }

    return this;
  }

  @Override
  public UiControllerManager connectToControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeControllerManager.connectController(controller);
        } catch (Exception e) {
          spaceEnvironment.getLog().error(
              String.format("Unable to shut down controller %s (%s)", controller.getUuid(),
                  controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

        throw new EntityNotFoundInteractiveSpacesException(String.format(
            "Space controller with ID %s not found", id));
      }
    }

    return this;
  }

  @Override
  public UiControllerManager disconnectFromControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeControllerManager.disconnectController(controller);
        } catch (Exception e) {
          spaceEnvironment.getLog().error(
              String.format("Unable to disconnect from controller %s (%s)", controller.getUuid(),
                  controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

        throw new EntityNotFoundInteractiveSpacesException(String.format(
            "Space controller with ID %s not found", id));
      }
    }

    return this;
  }

  @Override
  public UiControllerManager shutdownAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.shutdownController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to shut down controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager statusControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        activeControllerManager.statusController(controller);
      } else {
        spaceEnvironment.getLog().error(
            String.format("Attempted status of unknown controller %s", id));

        throw new EntityNotFoundInteractiveSpacesException(String.format(
            "Space controller with ID %s not found", id));
      }
    }

    return this;
  }

  @Override
  public UiControllerManager cleanControllerTempData(String id) {
    activeControllerManager.cleanControllerTempData(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager cleanControllerTempDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerTempData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean temp data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager cleanControllerPermanentData(String id) {
    activeControllerManager.cleanControllerPermanentData(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager cleanControllerPermanentDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerPermanentData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean permanent data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager cleanControllerActivitiesTempData(String id) {
    activeControllerManager.cleanControllerActivitiesTempData(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager cleanControllerActivitiesTempDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerActivitiesTempData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean all temp data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager cleanControllerActivitiesPermanentData(String id) {
    activeControllerManager.cleanControllerActivitiesPermanentData(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager cleanControllerActivitiesPermanentDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerActivitiesPermanentData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean all permanent data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager captureControllerDataBundle(String id) {
    activeControllerManager.captureControllerDataBundle(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager restoreControllerDataBundle(String id) {
    activeControllerManager.restoreControllerDataBundle(getSpaceControllerById(id));

    return this;
  }

  @Override
  public UiControllerManager captureDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.captureControllerDataBundle(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to capture data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }
    return this;
  }

  @Override
  public UiControllerManager restoreDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.restoreControllerDataBundle(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to capture data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }
    return this;
  }

  @Override
  public UiControllerManager shutdownAllActivities(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.shutdownAllActivities(controller);
    } else {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space controller with ID %s not found", id));
    }

    return this;
  }

  @Override
  public UiControllerManager shutdownAllActivitiesAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.shutdownAllActivities(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to shut down all live activities from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager deployAllControllerActivityInstances(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space Controller with ID %s not found", id));
    }

    for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByController(controller)) {
      activeControllerManager.deployLiveActivity(liveActivity);
    }

    return this;
  }

  @Override
  public UiControllerManager deployAllActivityInstances(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Activity with ID %s not found", id));
    }

    for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByActivity(activity)) {
      if (liveActivity.isOutOfDate()) {
        activeControllerManager.deployLiveActivity(liveActivity);
      }
    }

    return this;
  }

  @Override
  public UiControllerManager deleteLiveActivity(String id) {
    activeControllerManager.deleteLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager deployLiveActivity(String id) {
    activeControllerManager.deployLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager configureLiveActivity(String id) {
    activeControllerManager.configureLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager startupLiveActivity(String id) {
    activeControllerManager.startupLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager activateLiveActivity(String id) {
    activeControllerManager.activateLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager deactivateLiveActivity(String id) {
    activeControllerManager.deactivateLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager shutdownLiveActivity(String id) {
    activeControllerManager.shutdownLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager statusLiveActivity(String id) {
    activeControllerManager.statusLiveActivity(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager cleanLiveActivityPermanentData(String id) {
    activeControllerManager.cleanLiveActivityPermanentData(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager cleanLiveActivityTempData(String id) {
    activeControllerManager.cleanLiveActivityTempData(getLiveActivityById(id));

    return this;
  }

  @Override
  public UiControllerManager deployLiveActivityGroup(String id) {
    activeControllerManager.deployLiveActivityGroup(getLiveActivityGroupById(id));

    return this;
  }

  @Override
  public UiControllerManager configureLiveActivityGroup(String id) {
    activeControllerManager.configureLiveActivityGroup(getLiveActivityGroupById(id));

    return this;
  }

  @Override
  public UiControllerManager startupLiveActivityGroup(String id) {
    activeControllerManager.startupLiveActivityGroup(getLiveActivityGroupById(id));

    return this;
  }

  @Override
  public UiControllerManager activateLiveActivityGroup(String id) {
    activeControllerManager.activateLiveActivityGroup(getLiveActivityGroupById(id));

    return this;
  }

  @Override
  public UiControllerManager deactivateLiveActivityGroup(String id) {
    activeControllerManager.deactivateLiveActivityGroup(getLiveActivityGroupById(id));

    return this;
  }

  @Override
  public UiControllerManager shutdownLiveActivityGroup(String id) {
    activeControllerManager.shutdownLiveActivityGroup(getLiveActivityGroupById(id));

    return this;
  }

  @Override
  public UiControllerManager forceShutdownLiveActivitiesLiveActivityGroup(String id) {
    LiveActivityGroup group = getLiveActivityGroupById(id);

    for (GroupLiveActivity gla : group.getActivities()) {
      activeControllerManager.shutdownLiveActivity(gla.getActivity());
    }

    return this;
  }

  @Override
  public UiControllerManager statusLiveActivityGroup(String id) {
    LiveActivityGroup group = getLiveActivityGroupById(id);

    for (GroupLiveActivity gla : group.getActivities()) {
      statusLiveActivity(gla.getActivity().getId());
    }

    return this;
  }

  @Override
  public UiControllerManager liveActivityStatusSpace(String id) {
    Space space = getSpaceById(id);

    Set<String> liveActivityIds = Sets.newHashSet();
    statusSpace(space, liveActivityIds);

    return this;
  }

  /**
   * Request status for all live activities in the space and all subspaces.
   *
   * <p>
   * A given live activity will only be queried once even if in multiple
   * activity groups.
   *
   * @param space
   *          the space to examine
   * @param liveActivityIds
   *          IDs of all live activities which have had their status requested
   *          so far
   */
  private void statusSpace(Space space, Set<String> liveActivityIds) {
    for (LiveActivityGroup group : space.getActivityGroups()) {
      for (GroupLiveActivity gla : group.getActivities()) {
        String id = gla.getActivity().getId();
        if (liveActivityIds.add(id)) {
          statusLiveActivity(id);
        }
      }
    }

    for (Space subspace : space.getSpaces()) {
      statusSpace(subspace, liveActivityIds);
    }
  }

  @Override
  public List<UiLiveActivity> getAllUiLiveActivities() {
    return getUiLiveActivities(activityRepository.getAllLiveActivities());
  }

  @Override
  public List<UiLiveActivity> getAllUiLiveActivitiesByController(SpaceController controller) {
    return getUiLiveActivities(activityRepository.getLiveActivitiesByController(controller));
  }

  @Override
  public UiLiveActivity getUiLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity != null) {
      return newUiLiveActivity(activity);
    } else {
      return null;
    }
  }

  @Override
  public List<UiLiveActivity> getUiLiveActivities(List<LiveActivity> activities) {
    List<UiLiveActivity> result = Lists.newArrayList();

    if (activities != null) {
      for (LiveActivity activity : activities) {
        result.add(newUiLiveActivity(activity));
      }
    }

    return result;
  }

  /**
   * Create a new {@link UiLiveActivity} with the active part properly filled
   * out for a given live activity, if there is an active version (controller
   * specified).
   *
   * @param activity
   *          the live activity
   *
   * @return the UI live activity
   */
  private UiLiveActivity newUiLiveActivity(LiveActivity activity) {
    ActiveLiveActivity active = null;
    if (activity.getController() != null) {
      active = activeControllerManager.getActiveLiveActivity(activity);
    }

    return new UiLiveActivity(activity, active);
  }

  /**
   * Get a valid SpaceController, or throw an exception if not found.
   *
   * @param id
   *          id of the desired space controller
   *
   * @return space controller object
   *
   * @throws EntityNotFoundInteractiveSpacesException
   *           if the entity is not found
   */
  private SpaceController getSpaceControllerById(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space controller with ID %s not found", id));
    }
    return controller;
  }

  /**
   * Get a valid live activity, or throw an exception if not found.
   *
   * @param id
   *          id of the desired live activity
   *
   * @return live activity object
   *
   * @throws EntityNotFoundInteractiveSpacesException
   *           if the entity is not found
   */
  private LiveActivity getLiveActivityById(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live activity with ID %s not found", id));
    }
    return activity;
  }

  /**
   * Get all enabled space controllers, which are ones that are not marked disabled
   * or otherwise should not be contacted for normal "all" operations.
   *
   * @return list of enabled space controllers
   */
  private List<SpaceController> getAllEnabledSpaceControllers() {
    List<SpaceController> allControllers = controllerRepository.getAllSpaceControllers();
    List<SpaceController> liveControllers = Lists.newArrayListWithExpectedSize(allControllers.size());
    for (SpaceController controller : allControllers) {
      if (SpaceControllerMode.isControllerEnabled(controller)) {
        liveControllers.add(controller);
      }
    }
    return liveControllers;
  }

  /**
   * Get a valid live activity group, or throw an exception if not found.
   *
   * @param id
   *          id of the desired live activity group
   *
   * @return live activity group object
   *
   * @throws EntityNotFoundInteractiveSpacesException
   *           if the entity is not found
   */
  private LiveActivityGroup getLiveActivityGroupById(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live activity group with ID %s not found", id));
    }
    return group;
  }

  /**
   * Get a valid space, or throw an exception if not found.
   *
   * @param id
   *          id of the desired space
   *
   * @return space object
   *
   * @throws EntityNotFoundInteractiveSpacesException
   *           if the entity is not found
   */
  private Space getSpaceById(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space with ID %s not found", id));
    }
    return space;
  }

  /**
   * @param controllerRepository
   *          the controllerRepository to set
   */
  public void setControllerRepository(ControllerRepository controllerRepository) {
    this.controllerRepository = controllerRepository;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param activeControllerManager
   *          the activeControllerManager to set
   */
  public void setActiveControllerManager(ActiveControllerManager activeControllerManager) {
    this.activeControllerManager = activeControllerManager;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
