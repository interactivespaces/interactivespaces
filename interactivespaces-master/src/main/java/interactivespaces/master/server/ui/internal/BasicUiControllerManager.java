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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.master.server.ui.UiLiveActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;

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
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      controllerRepository.deleteSpaceController(controller);
    } else {
      spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space controller with ID %s not found", id));
    }

    return this;
  }

  @Override
  public UiControllerManager connectToAllControllers() {
    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
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
    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
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
    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
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
    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
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
    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
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
    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
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
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.deleteLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager deployLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.deployLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager configureLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.configureLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager startupLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.startupLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager activateLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.activateLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager deactivateLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.deactivateLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager shutdownLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.shutdownLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager statusLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity with ID %s not found", id));
    }

    activeControllerManager.statusLiveActivity(activity);

    return this;
  }

  @Override
  public UiControllerManager deployLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    activeControllerManager.deployLiveActivityGroup(group);

    return this;
  }

  @Override
  public UiControllerManager configureLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    activeControllerManager.configureLiveActivityGroup(group);

    return this;
  }

  @Override
  public UiControllerManager startupLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    activeControllerManager.startupLiveActivityGroup(group);

    return this;
  }

  @Override
  public UiControllerManager activateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    activeControllerManager.activateLiveActivityGroup(group);

    return this;
  }

  @Override
  public UiControllerManager deactivateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    activeControllerManager.deactivateLiveActivityGroup(group);

    return this;
  }

  @Override
  public UiControllerManager shutdownLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    activeControllerManager.shutdownLiveActivityGroup(group);

    return this;
  }

  @Override
  public UiControllerManager forceShutdownLiveActivitiesLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    for (GroupLiveActivity gla : group.getActivities()) {
      activeControllerManager.shutdownLiveActivity(gla.getActivity());
    }

    return this;
  }

  @Override
  public UiControllerManager statusLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Live Activity Group with ID %s not found", id));
    }

    for (GroupLiveActivity gla : group.getActivities()) {
      statusLiveActivity(gla.getActivity().getId());
    }

    return this;
  }

  @Override
  public UiControllerManager liveActivityStatusSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space with ID %s not found", id));
    }

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
   * out.
   *
   * @param activity
   * @return
   */
  protected UiLiveActivity newUiLiveActivity(LiveActivity activity) {
    ActiveLiveActivity active = null;
    if (activity.getController() != null) {
      active = activeControllerManager.getActiveLiveActivity(activity);
    }

    UiLiveActivity e = new UiLiveActivity(activity, active);
    return e;
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

  /**
   * Get a valid SpaceController, or throw an exception if not found.
   *
   * @param id
   *          id of the desired space controller
   * @return
   *          space controller object
   * @throws
   *          EntityNotFoundInteractiveSpacesException
   */
  private SpaceController getSpaceControllerById(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller == null) {
      throw new EntityNotFoundInteractiveSpacesException(String.format(
          "Space controller with ID %s not found", id));
    }
    return controller;
  }
}
