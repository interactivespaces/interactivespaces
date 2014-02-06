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

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiMessageSupport;
import interactivespaces.master.api.MasterApiSpaceManager;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceManager;
import interactivespaces.master.server.services.ActivityRepository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple space manager for the Master API.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiSpaceManager extends BaseMasterApiManager implements MasterApiSpaceManager {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Handle operations on remote controllers.
   */
  private ActiveSpaceManager activeSpaceManager;

  /**
   * The controller manager for active items.
   */
  private ActiveControllerManager activeControllerManager;

  /**
   * The Master API manager for activities.
   */
  private MasterApiActivityManager uiActivityManager;

  @Override
  public Map<String, Object> getSpacesByFilter(String filter) {
    List<Map<String, Object>> data = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      for (Space space : activityRepository.getSpaces(filterExpression)) {
        data.add(getBasicSpaceViewApiResponse(space));
      }

      return MasterApiMessageSupport.getSuccessResponse(data);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Attempt to get all space data failed", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_CALL_FAILURE);
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
  public Map<String, Object> deploySpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceManager.deploySpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> configureSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceManager.configureSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }

  }

  /**
   * Get the Master API view of a space.
   *
   * @param space
   *          the space
   *
   * @return the Master API view data for the space
   */
  @Override
  public Map<String, Object> getBasicSpaceViewApiResponse(Space space) {
    Map<String, Object> spaceData = Maps.newHashMap();

    getBasicSpaceApiResponse(space, spaceData);
    return spaceData;
  }

  /**
   * Add in the basic space data used in API calls.
   *
   * @param space
   *          the space to get the data from
   * @param response
   *          the Master API data being collected
   */
  @Override
  public void getBasicSpaceApiResponse(Space space, Map<String, Object> response) {
    response.put("id", space.getId());
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

    addGroupsDataApiResponse(space, data);
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
  private void addGroupsDataApiResponse(Space space, Map<String, Object> data) {
    List<Map<String, Object>> groupData = Lists.newArrayList();
    data.put("liveActivityGroups", groupData);

    for (LiveActivityGroup group : space.getActivityGroups()) {
      groupData.add(uiActivityManager.getLiveActivityGroupApiData(group));
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

  @Override
  public Map<String, Object> updateSpaceMetadata(String id, Map<String, Object> metadataCommand) {
    try {
      Space space = activityRepository.getSpaceById(id);
      if (space == null) {
        return getNoSuchSpaceResponse();
      }

      String command = (String) metadataCommand.get(MasterApiMessageSupport.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessageSupport.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand.get(MasterApiMessageSupport.MASTER_API_PARAMETER_DATA);
        space.setMetadata(replacement);
      } else if (MasterApiMessageSupport.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = space.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand.get(MasterApiMessageSupport.MASTER_API_PARAMETER_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        space.setMetadata(metadata);
      } else if (MasterApiMessageSupport.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = space.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(MasterApiMessageSupport.MASTER_API_PARAMETER_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        space.setMetadata(metadata);
      } else {
        return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_COMMAND_UNKNOWN);
      }

      activityRepository.saveSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not modify space metadata", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  @Override
  public Map<String, Object> startupSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceManager.startupSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> shutdownSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceManager.shutdownSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> activateSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceManager.activateSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> deactivateSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceManager.deactivateSpace(space);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse();
    }
  }

  @Override
  public Map<String, Object> getSpaceStatus(String id) {
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

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_CALL_FAILURE);
    }
  }

  /**
   * Create the Master API status object for a space.
   *
   * <p>
   * This will include all subspaces, live activity groups, and the live
   * activities contained in the groups.
   *
   * @param space
   *          the space to get the status for
   *
   * @return the Master API status object
   */
  private Map<String, Object> generateSpaceStatusApiResponse(Space space) {
    Map<String, Object> data = Maps.newHashMap();

    data.put("id", space.getId());
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
   * Get a list of Master API status objects for all live activity groups in a
   * space.
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

    result.put("id", group.getId());
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
    ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(liveActivity);

    Map<String, Object> response = Maps.newHashMap();

    response.put("id", liveActivity.getId());
    response.put("status", active.getRuntimeState().getDescription());

    return response;
  }

  /**
   * Get a no such space API response.
   *
   * @return a no such space API response
   */
  private Map<String, Object> getNoSuchSpaceResponse() {
    return MasterApiMessageSupport.getFailureResponse(MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN);
  }

  /**
   *
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param activeSpaceManager
   *          the activeSpaceManager to set
   */
  public void setActiveSpaceManager(ActiveSpaceManager activeSpaceManager) {
    this.activeSpaceManager = activeSpaceManager;
  }

  /**
   * @param activeControllerManager
   *          the activeControllerManager to set
   */
  public void setActiveControllerManager(ActiveControllerManager activeControllerManager) {
    this.activeControllerManager = activeControllerManager;
  }

  /**
   * @param uiActivityManager
   *          the uiActivityManager to set
   */
  public void setUiActivityManager(MasterApiActivityManager uiActivityManager) {
    this.uiActivityManager = uiActivityManager;
  }
}
