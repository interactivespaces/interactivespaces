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

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.MetadataJsonSupport;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiSpaceManager;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A simple space manager for UIs.
 * 
 * @author Keith M. Hughes
 */
public class BasicUiSpaceManager implements UiSpaceManager {

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
	 * The UI manager for activities.
	 */
	private UiActivityManager uiActivityManager;

	/**
	 * The Interactive Spaces environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void deleteSpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activityRepository.deleteSpace(space);
		}
	}

	@Override
	public void deploySpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activeSpaceManager.deploySpace(space);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Space with ID %s not found", id));
		}

	}

	@Override
	public void configureSpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activeSpaceManager.configureSpace(space);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Space with ID %s not found", id));
		}

	}

	/**
	 * Get the JSON view of a space.
	 * 
	 * @param space
	 * 			the space
	 * 
	 * @return the JSON view data for the space
	 */
	public Map<String, Object> getBasicSpaceViewJsonData(Space space) {
		Map<String, Object> spaceData = Maps.newHashMap();

		getBasicSpaceData(space, spaceData);
		return spaceData;
	}

	/**
	 * Add in the basic space data used in API calls.
	 * 
	 * @param space
	 *            the space to get the data from
	 * @param data
	 *            the JSON data being collected
	 */
	public void getBasicSpaceData(Space space, Map<String, Object> data) {
		data.put("id", space.getId());
		data.put("name", space.getName());
		data.put("description", space.getDescription());
		data.put("metadata", space.getMetadata());
	}

	@Override
	public Map<String, Object> getSpaceViewJsonData(Space space) {
		Map<String, Object> data = getBasicSpaceViewJsonData(space);

		addGroupsData(space, data);
		generateJsonSubspacesView(space, data);

		return data;
	}

	/**
	 * Add all data needed for groups.
	 * 
	 * @param space
	 *            the space which contains the groups
	 * @param data
	 *            the JSON result for the space
	 */
	private void addGroupsData(Space space, Map<String, Object> data) {
		List<Map<String, Object>> groupData = Lists.newArrayList();
		data.put("liveActivityGroups", groupData);

		for (LiveActivityGroup group : space.getActivityGroups()) {
			groupData.add(uiActivityManager.getLiveActivityGroupJsonData(group));
		}
	}

	/**
	 * Add all data needed for subspaces.
	 * 
	 * @param space
	 *            the space which contains the subspaces
	 * @param data
	 *            the JSON result for the space
	 */
	private void generateJsonSubspacesView(Space space, Map<String, Object> data) {
		List<Map<String, Object>> subspaceData = Lists.newArrayList();
		data.put("subspaces", subspaceData);

		for (Space subspace : space.getSpaces()) {
			subspaceData.add(getSpaceViewJsonData(subspace));
		}
	}

	@Override
	public Map<String, Object> updateSpaceMetadata(String id,
			Map<String, Object> metadataCommand) {
		try {
			Space space = activityRepository.getSpaceById(id);
			if (space == null) {
				return JsonSupport
						.getFailureJsonResponse(MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN);
			}

			String command = (String) metadataCommand
					.get(JsonSupport.JSON_PARAMETER_COMMAND);

			if (MetadataJsonSupport.JSON_COMMAND_METADATA_REPLACE
					.equals(command)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> replacement = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				space.setMetadata(replacement);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_MODIFY
					.equals(command)) {
				Map<String, Object> metadata = space.getMetadata();

				@SuppressWarnings("unchecked")
				Map<String, Object> modifications = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (Entry<String, Object> entry : modifications.entrySet()) {
					metadata.put(entry.getKey(), entry.getValue());
				}

				space.setMetadata(metadata);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_DELETE
					.equals(command)) {
				Map<String, Object> metadata = space.getMetadata();

				@SuppressWarnings("unchecked")
				List<String> modifications = (List<String>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (String entry : modifications) {
					metadata.remove(entry);
				}

				space.setMetadata(metadata);
			} else {
				return JsonSupport
						.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_COMMAND_UNKNOWN);
			}

			activityRepository.saveSpace(space);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (Exception e) {
			spaceEnvironment.getLog().error("Could not modify space metadata",
					e);

			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_FAILURE);
		}
	}

	@Override
	public void startupSpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activeSpaceManager.startupSpace(space);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Space with ID %s not found", id));
		}
	}

	@Override
	public void shutdownSpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activeSpaceManager.shutdownSpace(space);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Space with ID %s not found", id));
		}
	}

	@Override
	public void activateSpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activeSpaceManager.activateSpace(space);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Space with ID %s not found", id));
		}
	}

	@Override
	public void deactivateSpace(String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			activeSpaceManager.deactivateSpace(space);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Space with ID %s not found", id));
		}
	}

	@Override
	public Map<String, Object> getJsonSpaceStatus(String id) {
		try {
			Space space = activityRepository.getSpaceById(id);
			if (space == null) {
				return JsonSupport
						.getFailureJsonResponse(MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN);
			}

			Map<String, Object> response = generateJsonSpaceStatus(space);

			return JsonSupport.getSuccessJsonResponse(response);
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					"Could not modify activity metadata", e);

			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_FAILURE);
		}
	}

	/**
	 * Create the JSON status object for a space.
	 * 
	 * <p>
	 * This will include all subspaces, live activity groups, and the live
	 * activities contained in the groups.
	 * 
	 * @param space
	 *            the space to get the status for
	 * 
	 * @return the JSON status object
	 */
	private Map<String, Object> generateJsonSpaceStatus(Space space) {
		Map<String, Object> data = Maps.newHashMap();

		data.put("id", space.getId());
		data.put("subspaces", generateJsonSubSpacesStatuses(space));
		data.put("liveActivityGroups",
				generateJsonLiveActivityGroupsStatuses(space));

		return data;
	}

	/**
	 * Get a list of JSON status objects for all subspaces of a space.
	 * 
	 * @param space
	 *            the space containing the subspaces
	 * 
	 * @return a list for all subspace JSON status objects
	 */
	private List<Map<String, Object>> generateJsonSubSpacesStatuses(Space space) {
		List<Map<String, Object>> subspaces = Lists.newArrayList();

		for (Space subspace : space.getSpaces()) {
			subspaces.add(generateJsonSpaceStatus(subspace));
		}

		return subspaces;
	}

	/**
	 * Get a list of JSON status objects for all live activity groups in a
	 * space.
	 * 
	 * @param space
	 *            the space containing the subspaces
	 * 
	 * @return a list for all group JSON status objects
	 */
	private List<Map<String, Object>> generateJsonLiveActivityGroupsStatuses(
			Space space) {
		List<Map<String, Object>> groups = Lists.newArrayList();

		for (LiveActivityGroup group : space.getActivityGroups()) {
			groups.add(generateJsonLiveActivityGroupStatus(group));
		}
		return groups;
	}

	private Map<String, Object> generateJsonLiveActivityGroupStatus(
			LiveActivityGroup group) {
		Map<String, Object> result = Maps.newHashMap();
		
		result.put("id", group.getId());
		result.put("liveactivities", generateJsonLiveActivitiesStatuses(group));
		
		return result;
	}

	

	/**
	 * Get a list of JSON status objects for all live activities in a
	 * space.
	 * 
	 * @param group
	 *            the group containing the live activities
	 * 
	 * @return a list for all live activity JSON status objects
	 */
	private List<Map<String, Object>> generateJsonLiveActivitiesStatuses(LiveActivityGroup group) {
		List<Map<String, Object>> activities = Lists.newArrayList();
		
		for (GroupLiveActivity activity : group.getActivities()) {
			activities.add(generateJsonLiveActivityStatus(activity.getActivity()));
		}
		
		return activities;
	}

	/**
	 * Get the JSON status object for the given live activity
	 * 
	 * @param activity
	 * 		the live activity
	 * 
	 * @return the JSON status object
	 */
	private Map<String, Object> generateJsonLiveActivityStatus(
			LiveActivity activity) {
		ActiveLiveActivity active = activeControllerManager.getActiveLiveActivity(activity);
		
		Map<String, Object> response = Maps.newHashMap();
		
		response.put("id", activity.getId());
		response.put("status", active.getRuntimeState().getDescription());
		
		return response;
	}

	/**
	 * 
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param activeSpaceManager
	 *            the activeSpaceManager to set
	 */
	public void setActiveSpaceManager(ActiveSpaceManager activeSpaceManager) {
		this.activeSpaceManager = activeSpaceManager;
	}

	/**
	 * @param activeControllerManager the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			ActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
	}

	/**
	 * @param uiActivityManager the uiActivityManager to set
	 */
	public void setUiActivityManager(UiActivityManager uiActivityManager) {
		this.uiActivityManager = uiActivityManager;
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
