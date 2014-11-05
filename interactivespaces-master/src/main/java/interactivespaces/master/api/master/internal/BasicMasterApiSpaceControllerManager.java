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

package interactivespaces.master.api.master.internal;

import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerConfiguration;
import interactivespaces.domain.basic.SpaceControllerMode;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.api.master.MasterApiSpaceControllerManager;
import interactivespaces.master.api.master.MasterApiUtilities;
import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.server.services.internal.DataBundleState;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A simple controller manager for UIs.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiSpaceControllerManager extends BaseMasterApiManager implements
    MasterApiSpaceControllerManager {

  /**
   * Repository for obtaining controller entities.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Repository for obtaining activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Handle operations on remote controllers.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * Master API manager for activity operations.
   */
  private InternalMasterApiActivityManager masterApiActivityManager;

  @Override
  public Map<String, Object> getSpaceControllers(String filter) {
    List<Map<String, Object>> responseData = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<SpaceController> spaceControllers =
          Lists.newArrayList(spaceControllerRepository.getSpaceControllers(filterExpression));
      Collections.sort(spaceControllers, MasterApiUtilities.SPACE_CONTROLLER_BY_NAME_COMPARATOR);
      for (ActiveSpaceController acontroller : activeSpaceControllerManager.getActiveSpaceControllers(spaceControllers)) {
        Map<String, Object> controllerData = Maps.newHashMap();

        SpaceController controller = acontroller.getController();
        getSpaceControllerMasterApiData(controller, controllerData);

        responseData.add(controllerData);
      }

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Attempt to get activity data failed", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> getSpaceControllerAllView() {
    List<Map<String, Object>> data = Lists.newArrayList();

    List<SpaceController> spaceControllers = spaceControllerRepository.getSpaceControllers(null);
    Collections.sort(spaceControllers, MasterApiUtilities.SPACE_CONTROLLER_BY_NAME_COMPARATOR);
    for (ActiveSpaceController acontroller : activeSpaceControllerManager.getActiveSpaceControllers(spaceControllers)) {
      Map<String, Object> controllerData = Maps.newHashMap();

      SpaceController controller = acontroller.getController();
      getSpaceControllerMasterApiData(controller, controllerData);
      getActiveSpaceControllerMasterApiData(acontroller, controllerData);

      data.add(controllerData);
    }

    return MasterApiMessageSupport.getSuccessResponse(data);
  }

  @Override
  public Map<String, Object> getSpaceControllerFullView(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      Map<String, Object> responseData = Maps.newHashMap();

      Map<String, Object> controllerData = Maps.newHashMap();

      getSpaceControllerMasterApiData(controller, controllerData);

      ActiveSpaceController acontroller = activeSpaceControllerManager.getActiveSpaceController(controller);
      getActiveSpaceControllerMasterApiData(acontroller, controllerData);

      responseData.put("spacecontroller", controllerData);

      List<Map<String, Object>> liveActivities =
          masterApiActivityManager.getAllUiLiveActivitiesByController(controller);
      responseData.put("liveactivities", liveActivities);

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> getSpaceControllerView(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      Map<String, Object> controllerData = Maps.newHashMap();

      getSpaceControllerMasterApiData(controller, controllerData);

      return MasterApiMessageSupport.getSuccessResponse(controllerData);
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

//  @Override
//  public Map<String, Object> getSpacecontrollerConfiguration(String id) {
//    SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);
//    if (spaceController != null) {
//      Map<String, String> data = Maps.newHashMap();
//
//      SpaceControllerConfiguration config = spaceController.getConfiguration();
//      if (config != null) {
//        for (ConfigurationParameter parameter : config.getParameters()) {
//          data.put(parameter.getName(), parameter.getValue());
//        }
//      }
//
//      return MasterApiMessageSupport.getSuccessResponse(data);
//    } else {
//      return getNoSuchSpaceControllerResult();
//    }
//  }

//  @Override
//  public Map<String, Object> configureSpaceController(String id, Map<String, String> map) {
//    SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);
//    if (spaceController != null) {
//      if (saveSpaceControllerConfiguration(spaceController, map)) {
//        spaceControllerRepository.saveSpaceController(spaceController);
//      }
//
//      return MasterApiMessageSupport.getSimpleSuccessResponse();
//    } else {
//      return getNoSuchSpaceControllerResult();
//    }
//  }

  /**
   * Get the new configuration into the space controller.
   *
   * @param spaceController
   *          the space controller being configured
   * @param map
   *          the map representing the new configuration
   *
   * @return {@code true} if there was a change to the configuration
   */
//  private boolean saveSpaceControllerConfiguration(SpaceController spaceController, Map<String, String> map) {
//    SpaceControllerConfiguration configuration = spaceController.getConfiguration();
//    if (configuration != null) {
//      return mergeParameters(map, configuration);
//    } else {
//      // No configuration. If nothing in submission, nothing has changed.
//      // Otherwise add everything.
//      if (map.isEmpty()) {
//        return false;
//      }
//
//      newSpaceControllerConfiguration(spaceController, map);
//
//      return true;
//    }
//  }

  /**
   * merge the values in the map with the configuration.
   *
   * @param map
   *          map of new name/value pairs
   * @param configuration
   *          the configuration which may be changed
   *
   * @return {@code true} if there were any parameters changed in the configuration
   */
  private boolean mergeParameters(Map<String, String> map, SpaceControllerConfiguration configuration) {
    boolean changed = false;

    Map<String, ConfigurationParameter> existingMap = configuration.getParameterMap();

    // Delete all items removed
    for (Entry<String, ConfigurationParameter> entry : existingMap.entrySet()) {
      if (!map.containsKey(entry.getKey())) {
        changed = true;

        configuration.removeParameter(entry.getValue());
      }
    }

    // Now everything in the submitted map will be check. if the name exists
    // in the old configuration, we will try and change the value. if the
    // name doesn't exist, add it.
    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = existingMap.get(entry.getKey());
      if (parameter != null) {
        // Existed
        String oldValue = parameter.getValue();
        if (!oldValue.equals(entry.getValue())) {
          changed = true;
          parameter.setValue(entry.getValue());
        }
      } else {
        // Didn't exist
        changed = true;

        parameter = activityRepository.newConfigurationParameter();
        parameter.setName(entry.getKey());
        parameter.setValue(entry.getValue());

        configuration.addParameter(parameter);
      }

    }
    return changed;
  }

  /**
   * Create a new configuration for a space controller.
   *
   * @param spaceController
   *          the space controller
   * @param map
   *          the new configuration
   */
//  private void newSpaceControllerConfiguration(SpaceController spaceController, Map<String, String> map) {
//    SpaceControllerConfiguration configuration = spaceControllerRepository.newSpaceControllerConfiguration();
//    spaceController.setConfiguration(configuration);
//
//    for (Entry<String, String> entry : map.entrySet()) {
//      ConfigurationParameter parameter = spaceControllerRepository.newSpaceControllerConfigurationParameter();
//      parameter.setName(entry.getKey());
//      parameter.setValue(entry.getValue());
//
//      configuration.addParameter(parameter);
//    }
//  }

  @Override
  public Map<String, Object> updateSpaceControllerMetadata(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);
      if (spaceController == null) {
        return getNoSuchSpaceControllerResult();
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA);
        spaceController.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = spaceController.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        spaceController.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = spaceController.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications = (List<String>) metadataCommand.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        spaceController.setMetadata(metadata);
      } else {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_COMMAND_UNKNOWN);
      }

      spaceControllerRepository.saveSpaceController(spaceController);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify space controller metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> deleteSpaceController(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      spaceControllerRepository.deleteSpaceController(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> connectToAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.connectSpaceController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to connect to controller %s (%s)", controller.getUuid(), controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> disconnectFromAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.disconnectSpaceController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to disconnect to controller %s (%s)", controller.getUuid(), controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> statusFromAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.statusSpaceController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to get the status from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> forceStatusFromAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.forceStatusSpaceController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to force the status from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownSpaceControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeSpaceControllerManager.shutdownSpaceController(controller);
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
  public Map<String, Object> connectToSpaceControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeSpaceControllerManager.connectSpaceController(controller);
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
  public Map<String, Object> disconnectFromSpaceControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        try {
          activeSpaceControllerManager.disconnectSpaceController(controller);
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
  public Map<String, Object> shutdownAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.shutdownSpaceController(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to shut down controller %s (%s)", controller.getUuid(), controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> statusSpaceControllers(List<String> ids) {
    for (String id : ids) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
      if (controller != null) {
        activeSpaceControllerManager.statusSpaceController(controller);
      } else {
        spaceEnvironment.getLog().error(String.format("Attempted status of unknown controller %s", id));

        return getNoSuchSpaceControllerResult();
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerTempData(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerTempData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerTempDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerTempData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean temp data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerPermanentData(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerPermanentData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerPermanentDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerPermanentData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean permanent data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesTempData(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerActivitiesTempData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesTempDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerActivitiesTempData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean all temp data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesPermanentData(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerActivitiesPermanentData(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerActivitiesPermanentData(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to clean all permanent data from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> captureSpaceControllerDataBundle(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.captureSpaceControllerDataBundle(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> restoreSpaceControllerDataBundle(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.restoreSpaceControllerDataBundle(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> captureDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.captureSpaceControllerDataBundle(controller);
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
  public Map<String, Object> restoreDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.restoreSpaceControllerDataBundle(controller);
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
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {
      activeSpaceControllerManager.shutdownAllActivities(controller);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResult();
    }
  }

  @Override
  public Map<String, Object> shutdownAllActivitiesAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.shutdownAllActivities(controller);
      } catch (Exception e) {
        spaceEnvironment.getLog().error(
            String.format("Unable to shut down all live activities from controller %s (%s)", controller.getUuid(),
                controller.getName()), e);
      }
    }

    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> deployAllSpaceControllerActivityInstances(String id) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    if (controller != null) {

      for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByController(controller)) {
        activeSpaceControllerManager.deployLiveActivity(liveActivity);
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
          activeSpaceControllerManager.deployLiveActivity(liveActivity);
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
      activeSpaceControllerManager.deleteLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deployLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.deployLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> configureLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.configureLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> startupLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.startupLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> activateLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.activateLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deactivateLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.deactivateLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> shutdownLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.shutdownLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> statusLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.statusLiveActivity(liveActivity);

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
      activeSpaceControllerManager.cleanLiveActivityPermanentData(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> cleanLiveActivityTempData(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activeSpaceControllerManager.cleanLiveActivityTempData(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deployLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.deployLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> configureLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.configureLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> startupLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.startupLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> activateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.activateLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> deactivateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.deactivateLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> shutdownLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.shutdownLiveActivityGroup(group);

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
        activeSpaceControllerManager.shutdownLiveActivity(gla.getActivity());
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
   * A given live activity will only be queried once even if in multiple activity groups.
   *
   * @param space
   *          the space to examine
   * @param liveActivityIds
   *          IDs of all live activities which have had their status requested so far
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
  public Map<String, Object> deploySpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.deploySpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> configureSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.configureSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }

  }

  @Override
  public Map<String, Object> startupSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.startupSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> shutdownSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.shutdownSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> activateSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.activateSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> deactivateSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.deactivateSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> statusSpace(String id) {
    try {
      Space space = activityRepository.getSpaceById(id);
      if (space != null) {

        Map<String, Object> response = generateSpaceStatusApiResponse(space);

        return MasterApiMessageSupport.getSuccessResponse(response);
      } else {
        return getNoSuchSpaceResponse();
      }
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify activity metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  /**
   * Create the Master API status object for a space.
   *
   * <p>
   * This will include all subspaces, live activity groups, and the live activities contained in the groups.
   *
   * @param space
   *          the space to get the status for
   *
   * @return the Master API status object
   */
  private Map<String, Object> generateSpaceStatusApiResponse(Space space) {
    Map<String, Object> data = Maps.newHashMap();

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, space.getId());
    data.put("subspaces", generateSubSpacesStatusesApiResponse(space));
    data.put("liveActivityGroups", generateLiveActivityGroupsStatusesApiResponse(space));

    return data;
  }

  /**
   * Get a list of Master API status objects for all subspaces of a space.
   *
   * @param space
   *          the space containing the subspaces
   *
   * @return a list for all subspace Master API status objects
   */
  private List<Map<String, Object>> generateSubSpacesStatusesApiResponse(Space space) {
    List<Map<String, Object>> subspaces = Lists.newArrayList();

    for (Space subspace : space.getSpaces()) {
      subspaces.add(generateSpaceStatusApiResponse(subspace));
    }

    return subspaces;
  }

  /**
   * Get a list of Master API status objects for all live activity groups in a space.
   *
   * @param space
   *          the space containing the subspaces
   *
   * @return a list for all group Master API status objects
   */
  private List<Map<String, Object>> generateLiveActivityGroupsStatusesApiResponse(Space space) {
    List<Map<String, Object>> groups = Lists.newArrayList();

    for (LiveActivityGroup group : space.getActivityGroups()) {
      groups.add(generateLiveActivityGroupStatusApiResponse(group));
    }
    return groups;
  }

  /**
   * Generate the Master API response data for a live activity group.
   *
   * @param group
   *          the live activity group
   *
   * @return the master API response data
   */
  private Map<String, Object> generateLiveActivityGroupStatusApiResponse(LiveActivityGroup group) {
    Map<String, Object> result = Maps.newHashMap();

    result.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, group.getId());
    result.put("liveactivities", generateLiveActivitiesStatusesApiResponse(group));

    return result;
  }

  /**
   * Get a list of Master API status objects for all live activities in a space.
   *
   * @param group
   *          the group containing the live activities
   *
   * @return a list for all live activity Master API status objects
   */
  private List<Map<String, Object>> generateLiveActivitiesStatusesApiResponse(LiveActivityGroup group) {
    List<Map<String, Object>> activities = Lists.newArrayList();

    for (GroupLiveActivity activity : group.getActivities()) {
      activities.add(generateApiLiveActivityStatus(activity.getActivity()));
    }

    return activities;
  }

  /**
   * Get the Master API status object for the given live activity.
   *
   * @param liveActivity
   *          the live activity
   *
   * @return the Master API status object
   */
  private Map<String, Object> generateApiLiveActivityStatus(LiveActivity liveActivity) {
    ActiveLiveActivity active = activeSpaceControllerManager.getActiveLiveActivity(liveActivity);

    Map<String, Object> response = Maps.newHashMap();

    response.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
    response.put("status", active.getRuntimeState().getDescription());

    return response;
  }

  /**
   * Get all enabled space controllers, which are ones that are not marked disabled or otherwise should not be contacted
   * for normal "all" operations.
   *
   * @return list of enabled space controllers
   */
  private List<SpaceController> getAllEnabledSpaceControllers() {
    List<SpaceController> allControllers = spaceControllerRepository.getAllSpaceControllers();
    List<SpaceController> liveControllers = Lists.newArrayListWithExpectedSize(allControllers.size());
    for (SpaceController controller : allControllers) {
      if (SpaceControllerMode.isControllerEnabled(controller)) {
        liveControllers.add(controller);
      }
    }
    return liveControllers;
  }

  /**
   * Get the Master API data for a controller.
   *
   * @param controller
   *          the space controller
   * @param controllerData
   *          where the data should be stored
   */
  private void getSpaceControllerMasterApiData(SpaceController controller, Map<String, Object> controllerData) {
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, controller.getId());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_UUID, controller.getUuid());
    controllerData.put("name", controller.getName());
    controllerData.put("description", controller.getDescription());
    controllerData.put("metadata", controller.getMetadata());
    controllerData.put("hostId", controller.getHostId());

    SpaceControllerMode mode = controller.getMode();
    if (mode != null) {
      controllerData.put("mode", mode.name());
      controllerData.put("modeDescription", mode.getDescription());
    }
  }

  /**
   * Get the Master API data for an active controller.
   *
   * @param controller
   *          the space controller
   * @param controllerData
   *          where the data should be stored
   */
  private void getActiveSpaceControllerMasterApiData(ActiveSpaceController controller,
      Map<String, Object> controllerData) {
    SpaceControllerState state = controller.getState();
    controllerData.put("state", state);
    controllerData.put("stateDescription", state.getDescription());
    Date lastStateUpdateDate = controller.getLastStateUpdateDate();
    controllerData.put("lastStateUpdateDate", lastStateUpdateDate != null ? lastStateUpdateDate.toString() : null);
    DataBundleState dataBundleState = controller.getDataBundleState();
    controllerData.put("dataBundleState", dataBundleState.name());
    controllerData.put("dataBundleStateDescription", dataBundleState.getDescription());

    Date lastDataBundleStateUpdateDate = controller.getLastDataBundleStateUpdateDate();
    controllerData.put("lastDataBundleStateUpdateDate",
        lastDataBundleStateUpdateDate != null ? lastDataBundleStateUpdateDate.toString() : null);
  }

  /**
   * Get the Master API response for there not being a particular activity.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchActivityResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN);
  }

  /**
   * Get the Master API response for there not being a particular live activity.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchLiveActivityResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN);
  }

  /**
   * Get the Master API response for no such live activity group.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchLiveActivityGroupResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN);
  }

  /**
   * Get a master API response for no such space controller.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchSpaceControllerResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN);
  }

  /**
   * Get a master API response for no such space.
   *
   * @return the API response
   */
  private Map<String, Object> getNoSuchSpaceResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN);
  }

  /**
   * @param spaceControllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
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
  public void setActiveSpaceControllerManager(ActiveSpaceControllerManager activeControllerManager) {
    this.activeSpaceControllerManager = activeControllerManager;
  }

  /**
   * Set the Master API manager for activity operations.
   *
   * @param masterApiActivityManager
   *          the Master API manager for activity operations
   */
  public void setMasterApiActivityManager(InternalMasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }
}
