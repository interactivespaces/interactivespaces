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

package interactivespaces.master.api.internal;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerMode;
import interactivespaces.domain.space.Space;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiControllerManager;
import interactivespaces.master.api.MasterApiLiveActivity;
import interactivespaces.master.api.MasterApiMessageSupport;
import interactivespaces.master.api.MasterApiSpaceManager;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple controller manager for UIs.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiControllerManager extends BaseMasterApiManager implements MasterApiControllerManager {

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
   * Master API manager for activity operations.
   */
  private MasterApiActivityManager masterApiActivityManager;

  @Override
  public Map<String, Object> getSpaceControllerAllView() {
    List<Map<String, Object>> data = Lists.newArrayList();

    for (ActiveSpaceController acontroller : activeControllerManager.getActiveSpaceControllers(controllerRepository
        .getAllSpaceControllers())) {
      Map<String, Object> controllerData = Maps.newHashMap();

      SpaceController controller = acontroller.getController();
      getSpaceControllerData(controller, controllerData);

      data.add(controllerData);
    }

    return MasterApiMessageSupport.getSuccessResponse(data);
  }

  @Override
  public Map<String, Object> getSpaceControllerView(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      Map<String, Object> controllerData = Maps.newHashMap();

      getSpaceControllerData(controller, controllerData);

      return MasterApiMessageSupport.getSuccessResponse(controllerData);
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> deleteController(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      controllerRepository.deleteSpaceController(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> connectToAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.connectController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to connect to controller %s (%s)", controller.getUuid(), controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> disconnectFromAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.disconnectController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to disconnect to controller %s (%s)", controller.getUuid(), controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> statusFromAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.statusController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to get the status from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> forceStatusFromAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.forceStatusController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to force the status from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeControllerManager.shutdownController(controller);
        } catch (Exception e) {
          spaceEnvironment.getLog().error(
              String.format("Unable to shut down controller %s (%s)", controller.getUuid(), controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

        return getNoSuchSpaceControllerResult();
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> connectToControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeControllerManager.connectController(controller);
        } catch (Exception e) {
          spaceEnvironment.getLog().error(
              String.format("Unable to shut down controller %s (%s)", controller.getUuid(), controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

        return getNoSuchSpaceControllerResult();
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> disconnectFromControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeControllerManager.disconnectController(controller);
        } catch (Exception e) {
          spaceEnvironment.getLog()
              .error(
                  String.format("Unable to disconnect from controller %s (%s)", controller.getUuid(),
                      controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", id));

        return getNoSuchSpaceControllerResult();
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.shutdownController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to shut down controller %s (%s)", controller.getUuid(), controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> statusControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = controllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        activeControllerManager.statusController(controller);
      } else {
        spaceEnvironment.getLog().error(String.format("Attempted status of unknown controller %s", id));

        return getNoSuchSpaceControllerResult();
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanControllerTempData(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.cleanControllerTempData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanControllerTempDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerTempData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean temp data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanControllerPermanentData(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.cleanControllerPermanentData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanControllerPermanentDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerPermanentData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean permanent data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanControllerActivitiesTempData(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.cleanControllerActivitiesTempData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanControllerActivitiesTempDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerActivitiesTempData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean all temp data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanControllerActivitiesPermanentData(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.cleanControllerActivitiesPermanentData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanControllerActivitiesPermanentDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.cleanControllerActivitiesPermanentData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean all permanent data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> captureControllerDataBundle(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.captureControllerDataBundle(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> restoreControllerDataBundle(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.restoreControllerDataBundle(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> captureDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.captureControllerDataBundle(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog()
            .error(
                String.format("Unable to capture data from controller %s (%s)", controller.getUuid(),
                    controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> restoreDataAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.restoreControllerDataBundle(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog()
            .error(
                String.format("Unable to capture data from controller %s (%s)", controller.getUuid(),
                    controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownAllActivities(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeControllerManager.shutdownAllActivities(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> shutdownAllActivitiesAllControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeControllerManager.shutdownAllActivities(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to shut down all live activities from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> deployAllControllerActivityInstances(String id) {
    SpaceController controller = controllerRepository.getSpaceControllerById(id);
    if (controller != null) {

      for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByController(controller)) {
        activeControllerManager.deployLiveActivity(liveActivity);
      }

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> deployAllActivityInstances(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {

      for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByActivity(activity)) {
        if (liveActivity.isOutOfDate()) {
          activeControllerManager.deployLiveActivity(liveActivity);
        }
      }

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchActivityResult();
    }
  }

  @Override
  public Map<String, Object> deleteLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.deleteLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deployLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.deployLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> configureLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.configureLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> startupLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.startupLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> activateLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.activateLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deactivateLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.deactivateLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> shutdownLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.shutdownLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> statusLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.statusLiveActivity(liveActivity);

      Map<String, Object> statusData = Maps.newHashMap();

      masterApiActivityManager.getLiveActivityStatusApiData(liveActivity, statusData);

      return MasterApiMessageSupport.getSuccessResponse(statusData);
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> cleanLiveActivityPermanentData(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.cleanLiveActivityPermanentData(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> cleanLiveActivityTempData(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeControllerManager.cleanLiveActivityTempData(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deployLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeControllerManager.deployLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> configureLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeControllerManager.configureLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> startupLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeControllerManager.startupLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> activateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeControllerManager.activateLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> deactivateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeControllerManager.deactivateLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> shutdownLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeControllerManager.shutdownLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> forceShutdownLiveActivitiesLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {

      for (GroupLiveActivity gla : group.getActivities()) {
        activeControllerManager.shutdownLiveActivity(gla.getActivity());
      }

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> statusLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {

      for (GroupLiveActivity gla : group.getActivities()) {
        statusLiveActivity(gla.getActivity().getId());
      }

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> liveActivityStatusSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space == null) {

      Set<String> liveActivityIds = Sets.newHashSet();
      statusSpace(space, liveActivityIds);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResult();
    }
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
  public List<MasterApiLiveActivity> getAllUiLiveActivities() {
    return getUiLiveActivities(activityRepository.getAllLiveActivities());
  }

  @Override
  public List<MasterApiLiveActivity> getAllUiLiveActivitiesByController(SpaceController controller) {
    return getUiLiveActivities(activityRepository.getLiveActivitiesByController(controller));
  }

  @Override
  public MasterApiLiveActivity getUiLiveActivity(String id) {
    LiveActivity activity = activityRepository.getLiveActivityById(id);
    if (activity != null) {
      return newUiLiveActivity(activity);
    } else {
      return null;
    }
  }

  @Override
  public List<MasterApiLiveActivity> getUiLiveActivities(List<LiveActivity> activities) {
    List<MasterApiLiveActivity> result = Lists.newArrayList();

    if (activities != null) {
      for (LiveActivity activity : activities) {
        result.add(newUiLiveActivity(activity));
      }
    }

    return result;
  }

  /**
   * Create a new {@link MasterApiLiveActivity} with the active part properly
   * filled out for a given live activity, if there is an active version
   * (controller specified).
   *
   * @param activity
   *          the live activity
   *
   * @return the UI live activity
   */
  private MasterApiLiveActivity newUiLiveActivity(LiveActivity activity) {
    ActiveLiveActivity active = null;
    if (activity.getController() != null) {
      active = activeControllerManager.getActiveLiveActivity(activity);
    }

    return new MasterApiLiveActivity(activity, active);
  }

  /**
   * Get all enabled space controllers, which are ones that are not marked
   * disabled or otherwise should not be contacted for normal "all" operations.
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
   * Get the JSON data for a controller.
   *
   * @param controller
   *          the space controller
   * @param controllerData
   *          where the data should be stored
   */
  private void getSpaceControllerData(SpaceController controller, Map<String, Object> controllerData) {
    controllerData.put("id", controller.getId());
    controllerData.put("uuid", controller.getUuid());
    controllerData.put("name", controller.getName());
    controllerData.put("description", controller.getDescription());
    controllerData.put("metadata", controller.getMetadata());
  }

  /**
   * Get the Master API response for there not being a particular activity.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchActivityResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiActivityManager.MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN);
  }

  /**
   * Get the Master API response for there not being a particular live activity.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchLiveActivityResult() {
    return MasterApiMessageSupport
        .getFailureResponse(MasterApiActivityManager.MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN);
  }

  /**
   * Get the Master API response for no such live activity group.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchLiveActivityGroupResult() {
    return MasterApiMessageSupport
        .getFailureResponse(MasterApiActivityManager.MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN);
  }

  /**
   * Get a master API response for no such space controller.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchSpaceControllerResult() {
    return MasterApiMessageSupport
        .getFailureResponse(MasterApiControllerManager.MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN);
  }

  /**
   * Get a master API response for no such space.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchSpaceResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiSpaceManager.MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN);
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
   * Set the active controller manager.
   *
   * @param activeControllerManager
   *          the active controller manager
   */
  public void setActiveControllerManager(ActiveControllerManager activeControllerManager) {
    this.activeControllerManager = activeControllerManager;
  }

  /**
   * Set the Master API manager for activity operations.
   *
   * @param masterApiActivityManager
   *          the Master API manager for activity operations
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }
}
