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

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.api.MasterApiMessage;
import interactivespaces.master.api.MasterApiMessageSupport;
import interactivespaces.master.api.MasterApiUtilities;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.master.server.services.SpaceControllerListenerSupport;
import interactivespaces.resource.repository.ActivityRepositoryManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Simple Master API manager for activity operations.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiActivityManager extends BaseMasterApiManager implements InternalMasterApiActivityManager {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository server which gives activities.
   */
  private ActivityRepositoryManager activityRepositoryManager;

  /**
   * Manager for activity operations.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * Listener for space controller events.
   */
  private SpaceControllerListener controllerListener;

  @Override
  public void startup() {
    controllerListener = new SpaceControllerListenerSupport() {
      @Override
      public void onLiveActivityInstall(String uuid, LiveActivityDeploymentResponse result, long timestamp) {
        if (result.getStatus() == ActivityDeployStatus.STATUS_SUCCESS) {
          updateLiveActivityDeploymentTime(uuid, timestamp);
        }
      }
    };

    activeSpaceControllerManager.addSpaceControllerListener(controllerListener);
  }

  @Override
  public void shutdown() {
    activeSpaceControllerManager.removeSpaceControllerListener(controllerListener);
  }

  @Override
  public Activity saveActivity(SimpleActivity activity, InputStream activityFile) {

    Activity finalActivity = activityRepositoryManager.addActivity(activityFile);

    return finalActivity;
  }

  @Override
  public Map<String, Object> getActivitiesByFilter(String filter) {
    List<Map<String, Object>> responseData = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<Activity> activities = activityRepository.getActivities(filterExpression);
      Collections.sort(activities, MasterApiUtilities.ACTIVITY_BY_NAME_AND_VERSION_COMPARATOR);
      for (Activity activity : activities) {
        responseData.add(extractBasicActivityApiData(activity));
      }

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Attempt to get activity data failed", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> getActivityView(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      return MasterApiMessageSupport.getSuccessResponse(extractBasicActivityApiData(activity));
    } else {
      return noSuchActivityResult();
    }
  }

  @Override
  public Map<String, Object> getActivityFullView(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      Map<String, Object> fullView = Maps.newHashMap();
      fullView.put("activity", extractBasicActivityApiData(activity));
      fullView.put("liveactivities", extractLiveActivities(activityRepository.getLiveActivitiesByActivity(activity)));

      return MasterApiMessageSupport.getSuccessResponse(fullView);
    } else {
      return noSuchActivityResult();
    }
  }

  /**
   * Get basic information about an activity.
   *
   * @param activity
   *          the activity
   *
   * @return a Master API coded object giving the basic information
   */
  private Map<String, Object> extractBasicActivityApiData(Activity activity) {
    Map<String, Object> data = Maps.newHashMap();

    data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID, activity.getId());
    data.put("identifyingName", activity.getIdentifyingName());
    data.put("version", activity.getVersion());
    data.put("name", activity.getName());
    data.put("description", activity.getDescription());
    data.put("metadata", activity.getMetadata());
    data.put("lastUploadDate", activity.getLastUploadDate());
    data.put("lastStartDate", activity.getLastStartDate());
    data.put("bundleContentHash", activity.getBundleContentHash());

    return data;
  }

  @Override
  public Map<String, Object> deleteActivity(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      activityRepository.deleteActivity(activity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return noSuchActivityResult();
    }
  }

  @Override
  public Map<String, Object> updateActivityMetadata(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      Activity activity = activityRepository.getActivityById(id);
      if (activity == null) {
        return noSuchActivityResult();
      }

      String command = (String) metadataCommand.get(MasterApiMessage.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessage.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        activity.setMetadata(replacement);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        activity.setMetadata(metadata);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        activity.setMetadata(metadata);
      } else {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_COMMAND_UNKNOWN);
      }

      activityRepository.saveActivity(activity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify activity metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> getLiveActivitiesByFilter(String filter) {
    List<Map<String, Object>> responseData = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<LiveActivity> liveActivities = activityRepository.getLiveActivities(filterExpression);
      Collections.sort(liveActivities, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);
      for (LiveActivity activity : liveActivities) {
        Map<String, Object> activityData = Maps.newHashMap();

        extractLiveActivityApiData(activity, activityData);

        responseData.add(activityData);
      }

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Attempt to get live activity data failed", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> getLiveActivityView(String id) {
    LiveActivity liveactivity = activityRepository.getLiveActivityById(id);
    if (liveactivity != null) {
      Map<String, Object> data = Maps.newHashMap();

      extractLiveActivityApiData(liveactivity, data);

      return MasterApiMessageSupport.getSuccessResponse(data);
    } else {
      return noSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> getLiveActivityFullView(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      Map<String, Object> responseData = Maps.newHashMap();

      Map<String, Object> liveActivityData = Maps.newHashMap();
      extractLiveActivityApiData(liveActivity, liveActivityData);

      responseData.put("liveActivity", liveActivityData);

      List<LiveActivityGroup> liveActivityGroups =
          Lists.newArrayList(activityRepository.getLiveActivityGroupsByLiveActivity(liveActivity));
      Collections.sort(liveActivityGroups, MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);
      responseData.put("liveActivityGroups", extractLiveActivityGroups(liveActivityGroups));

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } else {
      return noSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> deleteLiveActivity(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      activityRepository.deleteLiveActivity(liveActivity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return noSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> getLiveActivityConfiguration(String id) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      Map<String, String> data = Maps.newHashMap();

      ActivityConfiguration config = liveActivity.getConfiguration();
      if (config != null) {
        for (ConfigurationParameter parameter : config.getParameters()) {
          data.put(parameter.getName(), parameter.getValue());
        }
      }

      return MasterApiMessageSupport.getSuccessResponse(data);
    } else {
      return noSuchLiveActivityResult();
    }
  }

  @Override
  public Map<String, Object> configureLiveActivity(String id, Map<String, String> map) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
    if (liveActivity != null) {
      if (saveConfiguration(liveActivity, map)) {
        activityRepository.saveLiveActivity(liveActivity);
      }

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return noSuchLiveActivityResult();
    }
  }

  /**
   * Get the new configuration into the live activity.
   *
   * @param liveactivity
   *          the live activity being configured
   * @param map
   *          the map representing the new configuration
   *
   * @return {@code true} if there was a change to the configuration
   */
  private boolean saveConfiguration(LiveActivity liveactivity, Map<String, String> map) {
    ActivityConfiguration configuration = liveactivity.getConfiguration();
    if (configuration != null) {
      return mergeParameters(map, configuration);
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (map.isEmpty()) {
        return false;
      }

      createLiveActivityNewConfiguration(liveactivity, map);

      return true;
    }
  }

  /**
   * merge the values in the map with the configuration.
   *
   * @param map
   *          map of new name/value pairs
   * @param configuration
   *          the configuration which may be changed
   *
   * @return {@code true} if there were any parameters changed in the
   *         configuration
   */
  private boolean mergeParameters(Map<String, String> map, ActivityConfiguration configuration) {
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
   * Create a new configuration for a live activity.
   *
   * @param liveactivity
   *          the live activity
   * @param map
   *          the new configuration
   */
  private void createLiveActivityNewConfiguration(LiveActivity liveactivity, Map<String, String> map) {
    ActivityConfiguration configuration;
    configuration = activityRepository.newActivityConfiguration();
    liveactivity.setConfiguration(configuration);

    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = activityRepository.newConfigurationParameter();
      parameter.setName(entry.getKey());
      parameter.setValue(entry.getValue());

      configuration.addParameter(parameter);
    }
  }

  @Override
  public Map<String, Object> updateMetadataLiveActivity(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      LiveActivity activity = activityRepository.getLiveActivityById(id);
      if (activity == null) {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN);
      }

      String command = (String) metadataCommand.get(MasterApiMessage.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessage.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        activity.setMetadata(replacement);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        activity.setMetadata(metadata);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        activity.setMetadata(metadata);
      } else {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_COMMAND_UNKNOWN);
      }

      activityRepository.saveLiveActivity(activity);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify live activity metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> deleteLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activityRepository.deleteLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return noSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> getBasicSpaceControllerApiData(SpaceController controller) {
    Map<String, Object> data = Maps.newHashMap();

    data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID, controller.getId());
    data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_UUID, controller.getUuid());
    data.put("name", controller.getName());

    return data;
  }

  /**
   * Get the Master API response data for a live activity.
   *
   * @param liveActivity
   *          the live activity to get data from
   * @param data
   *          the map where the data will be stored
   */
  private void extractLiveActivityApiData(LiveActivity liveActivity, Map<String, Object> data) {
    data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
    data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_UUID, liveActivity.getUuid());
    data.put("name", liveActivity.getName());
    data.put("description", liveActivity.getDescription());
    data.put("metadata", liveActivity.getMetadata());
    data.put("outOfDate", liveActivity.isOutOfDate());
    data.put("activity", extractBasicActivityApiData(liveActivity.getActivity()));
    data.put("controller", getBasicSpaceControllerApiData(liveActivity.getController()));
    Date lastDeployDate = liveActivity.getLastDeployDate();
    data.put("lastDeployDate", (lastDeployDate != null) ? lastDeployDate.toString() : null);

    getLiveActivityStatusApiData(liveActivity, data);
  }

  @Override
  public void getLiveActivityStatusApiData(LiveActivity liveActivity, Map<String, Object> data) {
    ActiveLiveActivity active = activeSpaceControllerManager.getActiveLiveActivity(liveActivity);

    Map<String, Object> activeData = Maps.newHashMap();
    data.put("active", activeData);

    ActivityState runtimeState = active.getRuntimeState();
    activeData.put("runtimeState", runtimeState.name());
    activeData.put("runtimeStateDescription", runtimeState.getDescription());
    activeData.put("runtimeStateDetail", active.getRuntimeStateDetail());
    ActivityState deployState = active.getDeployState();
    activeData.put("deployState", deployState.name());
    activeData.put("deployStateDescription", deployState.getDescription());
    activeData.put("directRunning", active.isDirectRunning());
    activeData.put("directActivated", active.isDirectActivated());
    activeData.put("numberLiveActivityGroupRunning", active.getNumberLiveActivityGroupRunning());
    activeData.put("numberLiveActivityGroupActivated", active.getNumberLiveActivityGroupActivated());
    Date lastStateUpdateDate = active.getLastStateUpdateDate();
    activeData.put("lastStateUpdate", (lastStateUpdateDate != null) ? lastStateUpdateDate.toString() : null);
  }

  @Override
  public Map<String, Object> getLiveActivityGroupView(String id) {
    LiveActivityGroup liveActivityGroup = activityRepository.getLiveActivityGroupById(id);
    if (liveActivityGroup != null) {
      return MasterApiMessageSupport.getSuccessResponse(getLiveActivityGroupApiData(liveActivityGroup));
    } else {
      return noSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> getLiveActivityGroupFullView(String id) {
    LiveActivityGroup liveActivityGroup = activityRepository.getLiveActivityGroupById(id);
    if (liveActivityGroup != null) {
      Map<String, Object> responseData = Maps.newHashMap();
      responseData.put("liveactivitygroup", getLiveActivityGroupApiData(liveActivityGroup));

      List<LiveActivity> liveActivities = Lists.newArrayList();
      for (GroupLiveActivity gla : liveActivityGroup.getActivities()) {
        liveActivities.add(gla.getActivity());
      }

      responseData.put("liveactivities", extractLiveActivities(liveActivities));

      List<Space> spaces = Lists.newArrayList(activityRepository.getSpacesByLiveActivityGroup(liveActivityGroup));
      Collections.sort(spaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);
      responseData.put("spaces", getSpaceApiData(spaces));

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } else {
      return noSuchLiveActivityGroupResult();
    }
  }

  @Override
  public Map<String, Object> getLiveActivityGroupsByFilter(String filter) {
    List<Map<String, Object>> responseData = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<LiveActivityGroup> liveActivityGroups = activityRepository.getLiveActivityGroups(filterExpression);
      Collections.sort(liveActivityGroups, MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

      for (LiveActivityGroup group : liveActivityGroups) {
        Map<String, Object> groupData = Maps.newHashMap();

        extractLiveActivityGroup(group, groupData);

        responseData.add(groupData);
      }

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Attempt to get live activity group data failed", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  /**
   * Get the Master API response data describing a live activity group.
   *
   * @param liveActivityGroup
   *          the live activity group
   *
   * @return the API Response data describing the group
   */
  private Map<String, Object> getLiveActivityGroupApiData(LiveActivityGroup liveActivityGroup) {
    Map<String, Object> data = Maps.newHashMap();

    extractLiveActivityGroup(liveActivityGroup, data);

    List<Map<String, Object>> activityData = Lists.newArrayList();
    data.put("liveActivities", activityData);

    List<LiveActivity> liveActivities = Lists.newArrayList();
    for (GroupLiveActivity gactivity : liveActivityGroup.getActivities()) {
      liveActivities.add(gactivity.getActivity());
    }
    Collections.sort(liveActivities, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);

    for (LiveActivity liveActivity : liveActivities) {
      Map<String, Object> liveActivityData = Maps.newHashMap();
      activityData.add(liveActivityData);

      extractLiveActivityApiData(liveActivity, liveActivityData);
    }

    return data;
  }

  @Override
  public List<Map<String, Object>> getAllUiLiveActivitiesByController(SpaceController controller) {
    List<LiveActivity> liveActivitiesByController =
        Lists.newArrayList(activityRepository.getLiveActivitiesByController(controller));
    Collections.sort(liveActivitiesByController, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);

    return extractLiveActivities(liveActivitiesByController);
  }

  /**
   * Extract the live activity data for all given live activities.
   *
   * @param liveActivities
   *          the live activities
   *
   * @return list of the data for all live activities
   */
  private List<Map<String, Object>> extractLiveActivities(List<LiveActivity> liveActivities) {
    List<Map<String, Object>> result = Lists.newArrayList();

    if (liveActivities != null) {
      for (LiveActivity liveActivity : liveActivities) {
        Map<String, Object> data = Maps.newHashMap();
        extractLiveActivityApiData(liveActivity, data);
        result.add(data);
      }
    }

    return result;
  }

  /**
   * Extract the live activity data for all given live activities.
   *
   * @param liveActivityGroups
   *          the live activity groups
   *
   * @return list of the data for all the live activity groups
   */
  private List<Map<String, Object>> extractLiveActivityGroups(List<LiveActivityGroup> liveActivityGroups) {
    List<Map<String, Object>> result = Lists.newArrayList();

    if (liveActivityGroups != null) {
      for (LiveActivityGroup liveActivityGroup : liveActivityGroups) {
        Map<String, Object> data = Maps.newHashMap();
        extractLiveActivityGroup(liveActivityGroup, data);
        result.add(data);
      }
    }

    return result;
  }

  @Override
  public Map<String, Object> getSpacesByFilter(String filter) {
    List<Map<String, Object>> data = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<Space> spaces = activityRepository.getSpaces(filterExpression);
      Collections.sort(spaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);

      for (Space space : spaces) {
        data.add(getBasicSpaceViewApiResponse(space));
      }

      return MasterApiMessageSupport.getSuccessResponse(data);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Attempt to get all space data failed", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> deleteSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activityRepository.deleteSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> updateMetadataSpace(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      Space space = activityRepository.getSpaceById(id);
      if (space == null) {
        return getNoSuchSpaceResponse();
      }

      String command = (String) metadataCommand.get(MasterApiMessage.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessage.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        space.setMetadata(replacement);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = space.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        space.setMetadata(metadata);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = space.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        space.setMetadata(metadata);
      } else {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_COMMAND_UNKNOWN);
      }

      activityRepository.saveSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify space metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  /**
   * Get the basic Master API view of a space.
   *
   * @param space
   *          the space
   *
   * @return the Master API data
   */
  private Map<String, Object> getBasicSpaceViewApiResponse(Space space) {
    Map<String, Object> spaceData = Maps.newHashMap();

    getBasicSpaceApiResponse(space, spaceData);

    return spaceData;
  }

  /**
   * Get the basic space data for a list of spaces.
   *
   * @param spaces
   *          a list of spaces
   *
   * @return a list of the basic space data
   */
  private List<Map<String, Object>> getSpaceApiData(List<Space> spaces) {
    List<Map<String, Object>> data = Lists.newArrayList();

    for (Space space : spaces) {
      data.add(getBasicSpaceViewApiResponse(space));
    }

    return data;
  }

  /**
   * Add in the basic space data used in API calls.
   *
   * @param space
   *          the space to get the data from
   * @param response
   *          the Master API data being collected
   */
  private void getBasicSpaceApiResponse(Space space, Map<String, Object> response) {
    response.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID, space.getId());
    response.put("name", space.getName());
    response.put("description", space.getDescription());
    response.put("metadata", space.getMetadata());
  }

  @Override
  public Map<String, Object> getSpaceView(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      return MasterApiMessageSupport.getSuccessResponse(getSpaceViewApiResponse(space));
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> getSpaceFullView(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      Map<String, Object> responseData = Maps.newHashMap();

      responseData.put("space", getSpaceViewApiResponse(space));

      List<? extends LiveActivityGroup> liveActivityGroups = space.getActivityGroups();
      Collections.sort(liveActivityGroups, MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

      responseData.put("liveActivityGroups", getLiveActivityGroupsMasterApi(liveActivityGroups));

      List<? extends Space> subspaces = space.getSpaces();
      Collections.sort(subspaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);
      responseData.put("subspaces", subspaces);

      List<Space> cspaces = Lists.newArrayList(activityRepository.getSpacesBySubspace(space));
      Collections.sort(cspaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);
      responseData.put("containingSpaces", cspaces);

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> getSpaceLiveActivityGroupView(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      Map<String, Object> responseData = Maps.newHashMap();

      responseData.put("space", getSpaceViewApiResponse(space));

      Set<LiveActivityGroup> liveActivityGroupsSet = Sets.newHashSet();
      collectLiveActivityGroupsForSpace(space, liveActivityGroupsSet);
      List<LiveActivityGroup> liveActivityGroups = Lists.newArrayList(liveActivityGroupsSet);
      Collections.sort(liveActivityGroups, MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

      responseData.put("liveActivityGroups", getLiveActivityGroupsLiveActivitiesMasterApi(liveActivityGroups));

      return MasterApiMessageSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  /**
   * Get the complete space view for a given space.
   *
   * @param space
   *          the space
   *
   * @return the Master API view
   */
  private Map<String, Object> getSpaceViewApiResponse(Space space) {
    Map<String, Object> data = getBasicSpaceViewApiResponse(space);

    addLiveActivityGroupsDataApiResponse(space, data);
    generateSubspacesViewApiResponse(space, data);

    return data;
  }

  /**
   * Add all data needed for groups.
   *
   * @param space
   *          the space which contains the groups
   * @param data
   *          the Master API result for the space
   */
  private void addLiveActivityGroupsDataApiResponse(Space space, Map<String, Object> data) {
    List<Map<String, Object>> groupData = Lists.newArrayList();
    data.put("liveActivityGroups", groupData);

    for (LiveActivityGroup group : space.getActivityGroups()) {
      groupData.add(getLiveActivityGroupApiData(group));
    }
  }

  /**
   * Add all data needed for subspaces.
   *
   * @param space
   *          the space which contains the subspaces
   * @param data
   *          the Master API result for the space
   */
  private void generateSubspacesViewApiResponse(Space space, Map<String, Object> data) {
    List<Map<String, Object>> subspaceData = Lists.newArrayList();
    data.put("subspaces", subspaceData);

    for (Space subspace : space.getSpaces()) {
      subspaceData.add(getSpaceViewApiResponse(subspace));
    }
  }

  /**
   * Collect the live activity groups from this space and all subspaces.
   *
   * @param space
   *          the root space
   * @param liveActivityGroups
   *          the set of all groups seen
   */
  private void collectLiveActivityGroupsForSpace(Space space, Set<LiveActivityGroup> liveActivityGroups) {
    liveActivityGroups.addAll(space.getActivityGroups());

    for (Space subspace : space.getSpaces()) {
      collectLiveActivityGroupsForSpace(subspace, liveActivityGroups);
    }
  }

  /**
   * Get a list of live activity groups master API data.
   *
   * @param groups
   *          list of groups
   *
   * @return the API data being collected
   */
  private List<Map<String, Object>> getLiveActivityGroupsMasterApi(List<? extends LiveActivityGroup> groups) {
    List<Map<String, Object>> response = Lists.newArrayList();

    if (groups != null) {
      for (LiveActivityGroup group : groups) {
        Map<String, Object> groupData = Maps.newHashMap();
        extractLiveActivityGroup(group, groupData);
        response.add(groupData);
      }
    }

    return response;
  }

  /**
   * Get a list of live activity groups master API data.
   *
   * @param groups
   *          list of groups
   *
   * @return the API data being collected
   */
  private List<Map<String, Object>> getLiveActivityGroupsLiveActivitiesMasterApi(
      List<? extends LiveActivityGroup> groups) {
    List<Map<String, Object>> response = Lists.newArrayList();

    if (groups != null) {
      for (LiveActivityGroup group : groups) {
        response.add(getLiveActivityGroupApiData(group));
      }
    }

    return response;
  }

  /**
   * Translate live activity group data into the form needed for the Master API.
   *
   * @param group
   *          the group to get the data from
   * @param data
   *          the API data being collected
   */
  private void extractLiveActivityGroup(LiveActivityGroup group, Map<String, Object> data) {
    data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID, group.getId());
    data.put("name", group.getName());
    data.put("description", group.getDescription());
    data.put("metadata", group.getMetadata());
  }

  @Override
  public Map<String, Object> updateMetadataLiveActivityGroup(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
      if (group == null) {
        return MasterApiMessageSupport
            .getFailureResponse(MasterApiMessage.MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN);
      }

      String command = (String) metadataCommand.get(MasterApiMessage.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessage.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        group.setMetadata(replacement);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = group.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        group.setMetadata(metadata);
      } else if (MasterApiMessage.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = group.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        group.setMetadata(metadata);
      } else {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_COMMAND_UNKNOWN);
      }

      activityRepository.saveLiveActivityGroup(group);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify live activity group metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  /**
   * Update the deployment time of a live activity.
   *
   * @param uuid
   *          UUID of the live activity
   * @param timestamp
   *          timestamp of the time of deployment
   */
  public void updateLiveActivityDeploymentTime(String uuid, long timestamp) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      liveActivity.setLastDeployDate(new Date(timestamp));

      activityRepository.saveLiveActivity(liveActivity);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Attempt to update deployment time for an unknown live activity %s", uuid));
    }
  }

  /**
   * Get the Master API response for no such activity.
   *
   * @return the API response
   */
  private Map<String, Object> noSuchActivityResult() {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN);
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param activityRepositoryManager
   *          the activityRepositoryManager to set
   */
  public void setActivityRepositoryManager(ActivityRepositoryManager activityRepositoryManager) {
    this.activityRepositoryManager = activityRepositoryManager;
  }

  /**
   * @param activeSpaceControllerManager
   *          the activeControllerManager to set
   */
  public void setActiveSpaceControllerManager(ActiveSpaceControllerManager activeSpaceControllerManager) {
    this.activeSpaceControllerManager = activeSpaceControllerManager;
  }
}
