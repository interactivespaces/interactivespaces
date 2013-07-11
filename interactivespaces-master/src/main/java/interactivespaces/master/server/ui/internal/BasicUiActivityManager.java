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
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.master.server.services.SpaceControllerListenerSupport;
import interactivespaces.master.server.services.internal.LiveActivityInstallResult;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.MetadataJsonSupport;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.resource.repository.ActivityRepositoryManager;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Simple activity manager for UIs.
 * 
 * @author Keith M. Hughes
 */
public class BasicUiActivityManager implements UiActivityManager {

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
	private ActiveControllerManager activeControllerManager;

	private SpaceControllerListener controllerListener;

	/**
	 * The Interactive Spaces environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		controllerListener = new SpaceControllerListenerSupport() {
			@Override
			public void onLiveActivityInstall(String uuid, LiveActivityInstallResult result,
					long timestamp) {
				if (result == LiveActivityInstallResult.SUCCESS) {
					updateLiveActivityDeploymentTime(uuid, timestamp);
				}
			}
		};

		activeControllerManager.addControllerListener(controllerListener);
	}

	@Override
	public void shutdown() {
		activeControllerManager.removeControllerListener(controllerListener);
	}

	@Override
	public Activity saveActivity(SimpleActivity activity,
			InputStream activityFile) {

		Activity finalActivity = activityRepositoryManager
				.addActivity(activityFile);

		return finalActivity;
	}

	@Override
	public void deleteActivity(String id) {
		Activity activity = activityRepository.getActivityById(id);
		if (activity != null) {
			activityRepository.deleteActivity(activity);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Activity with ID %s not found", id));
		}
	}

	@Override
	public Map<String, Object> updateActivityMetadata(String id,
			Map<String, Object> metadataCommand) {
		try {
			Activity activity = activityRepository.getActivityById(id);
			if (activity == null) {
				return JsonSupport
						.getFailureJsonResponse(MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN);
			}

			String command = (String) metadataCommand
					.get(JsonSupport.JSON_PARAMETER_COMMAND);

			if (MetadataJsonSupport.JSON_COMMAND_METADATA_REPLACE
					.equals(command)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> replacement = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				activity.setMetadata(replacement);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_MODIFY
					.equals(command)) {
				Map<String, Object> metadata = activity.getMetadata();

				@SuppressWarnings("unchecked")
				Map<String, Object> modifications = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (Entry<String, Object> entry : modifications.entrySet()) {
					metadata.put(entry.getKey(), entry.getValue());
				}

				activity.setMetadata(metadata);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_DELETE
					.equals(command)) {
				Map<String, Object> metadata = activity.getMetadata();

				@SuppressWarnings("unchecked")
				List<String> modifications = (List<String>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (String entry : modifications) {
					metadata.remove(entry);
				}

				activity.setMetadata(metadata);
			} else {
				return JsonSupport
						.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_COMMAND_UNKNOWN);
			}

			activityRepository.saveActivity(activity);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					"Could not modify activity metadata", e);

			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_FAILURE);
		}
	}

	@Override
	public void deleteLiveActivity(String id) {
		LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
		if (liveActivity != null) {
			activityRepository.deleteLiveActivity(liveActivity);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Live Activity with ID %s not found", id));
		}
	}

	@Override
	public Map<String, String> getLiveActivityConfiguration(String id) {
		LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
		if (liveActivity != null) {
			Map<String, String> map = Maps.newHashMap();

			ActivityConfiguration config = liveActivity.getConfiguration();
			if (config != null) {
				for (ConfigurationParameter parameter : config.getParameters()) {
					map.put(parameter.getName(), parameter.getValue());
				}
			}

			return map;
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Live Activity with ID %s not found", id));
		}
	}

	@Override
	public void configureLiveActivity(String id, Map<String, String> map) {
		LiveActivity liveActivity = activityRepository.getLiveActivityById(id);
		if (liveActivity != null) {
			if (saveConfiguration(liveActivity, map)) {
				activityRepository.saveLiveActivity(liveActivity);
			}
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Live Activity with ID %s not found", id));
		}
	}

	/**
	 * Get the new configuration into the live activity.
	 * 
	 * @param liveactivity
	 *            the live activity being configured
	 * @param map
	 *            the map representing the new configuration
	 * 
	 * @return {@code true} if there was a change to the configuration
	 */
	private boolean saveConfiguration(LiveActivity liveactivity,
			Map<String, String> map) {
		ActivityConfiguration configuration = liveactivity.getConfiguration();
		if (configuration != null) {
			return mergeParameters(map, configuration);
		} else {
			// No configuration. If nothing in submission, nothing has changed.
			// Otherwise add everything.
			if (map.isEmpty())
				return false;

			createLiveActivityNewConfiguration(liveactivity, map);

			return true;
		}
	}

	/**
	 * merge the values in the map with the configuration.
	 * 
	 * @param map
	 *            map of new name/value pairs
	 * @param configuration
	 *            the configuration which may be changed
	 * 
	 * @return {@true} if there were any parameters changed in the configuration
	 */
	private boolean mergeParameters(Map<String, String> map,
			ActivityConfiguration configuration) {
		boolean changed = false;

		Map<String, ConfigurationParameter> existingMap = configuration
				.getParameterMap();

		// Delete all items removed
		for (Entry<String, ConfigurationParameter> entry : existingMap
				.entrySet()) {
			if (!map.containsKey(entry.getKey())) {
				changed = true;

				configuration.removeParameter(entry.getValue());
			}
		}

		// Now everything in the submitted map will be check. if the name exists
		// in the old configuration, we will try and change the value. if the
		// name
		// doesn't exist, add it.
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
	 *            the live activity
	 * @param map
	 *            the new configuration
	 */
	private void createLiveActivityNewConfiguration(LiveActivity liveactivity,
			Map<String, String> map) {
		ActivityConfiguration configuration;
		configuration = activityRepository.newActivityConfiguration();
		liveactivity.setConfiguration(configuration);

		for (Entry<String, String> entry : map.entrySet()) {
			ConfigurationParameter parameter = activityRepository
					.newConfigurationParameter();
			parameter.setName(entry.getKey());
			parameter.setValue(entry.getValue());

			configuration.addParameter(parameter);
		}
	}

	@Override
	public Map<String, Object> updateLiveActivityMetadata(String id,
			Map<String, Object> metadataCommand) {
		try {
			LiveActivity activity = activityRepository.getLiveActivityById(id);
			if (activity == null) {
				return JsonSupport
						.getFailureJsonResponse(MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN);
			}

			String command = (String) metadataCommand
					.get(JsonSupport.JSON_PARAMETER_COMMAND);

			if (MetadataJsonSupport.JSON_COMMAND_METADATA_REPLACE
					.equals(command)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> replacement = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				activity.setMetadata(replacement);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_MODIFY
					.equals(command)) {
				Map<String, Object> metadata = activity.getMetadata();

				@SuppressWarnings("unchecked")
				Map<String, Object> modifications = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (Entry<String, Object> entry : modifications.entrySet()) {
					metadata.put(entry.getKey(), entry.getValue());
				}

				activity.setMetadata(metadata);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_DELETE
					.equals(command)) {
				Map<String, Object> metadata = activity.getMetadata();

				@SuppressWarnings("unchecked")
				List<String> modifications = (List<String>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (String entry : modifications) {
					metadata.remove(entry);
				}

				activity.setMetadata(metadata);
			} else {
				return JsonSupport
						.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_COMMAND_UNKNOWN);
			}

			activityRepository.saveLiveActivity(activity);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					"Could not modify live activity metadata", e);

			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_FAILURE);
		}
	}

	@Override
	public void deleteActivityGroup(String id) {
		LiveActivityGroup group = activityRepository
				.getLiveActivityGroupById(id);
		if (group != null) {
			activityRepository.deleteLiveActivityGroup(group);
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Live Activity Group with ID %s not found", id));
		}
	}

	@Override
	public Map<String, Object> getBasicActivityJsonData(Activity activity) {
		Map<String, Object> data = Maps.newHashMap();

		data.put("identifyingName", activity.getIdentifyingName());
		data.put("version", activity.getVersion());
		data.put("metadata", activity.getMetadata());

		return data;
	}

	@Override
	public Map<String, Object> getBasicSpaceControllerJsonData(
			SpaceController controller) {
		Map<String, Object> data = Maps.newHashMap();

		data.put("id", controller.getId());
		data.put("uuid", controller.getUuid());
		data.put("name", controller.getName());

		return data;
	}

	@Override
	public void getLiveActivityViewJsonData(LiveActivity activity,
			Map<String, Object> activityData) {
		activityData.put("id", activity.getId());
		activityData.put("uuid", activity.getUuid());
		activityData.put("name", activity.getName());
		activityData.put("description", activity.getDescription());
		activityData.put("metadata", activity.getMetadata());
		activityData.put("activity",
				getBasicActivityJsonData(activity.getActivity()));
		activityData.put("controller",
				getBasicSpaceControllerJsonData(activity.getController()));

		getLiveActivityStatusJsonData(activity, activityData);
	}

	@Override
	public void getLiveActivityStatusJsonData(LiveActivity activity,
			Map<String, Object> data) {
		ActiveLiveActivity active = activeControllerManager
				.getActiveLiveActivity(activity);
		String runtimeState = active.getRuntimeState().getDescription();
		data.put("status", runtimeState);
		String deployState = active.getDeployState().getDescription();
		data.put("deployStatus", deployState);
	}

	@Override
	public Map<String, Object> getLiveActivityGroupJsonData(
			LiveActivityGroup liveActivityGroup) {
		Map<String, Object> data = Maps.newHashMap();

		getBasicLiveActivityGroupJsonData(liveActivityGroup, data);

		List<Map<String, Object>> activityData = Lists.newArrayList();
		data.put("liveActivities", activityData);

		for (GroupLiveActivity gactivity : liveActivityGroup.getActivities()) {
			LiveActivity activity = gactivity.getActivity();

			Map<String, Object> adata = Maps.newHashMap();
			activityData.add(adata);

			getLiveActivityViewJsonData(activity, adata);
		}
		return data;
	}

	@Override
	public void getBasicLiveActivityGroupJsonData(LiveActivityGroup group,
			Map<String, Object> data) {
		data.put("id", group.getId());
		data.put("name", group.getName());
		data.put("description", group.getDescription());
		data.put("metadata", group.getMetadata());
	}

	@Override
	public Map<String, Object> updateLiveActivityGroupMetadata(String id,
			Map<String, Object> metadataCommand) {
		try {
			LiveActivityGroup group = activityRepository
					.getLiveActivityGroupById(id);
			if (group == null) {
				return JsonSupport
						.getFailureJsonResponse(MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN);
			}

			String command = (String) metadataCommand
					.get(JsonSupport.JSON_PARAMETER_COMMAND);

			if (MetadataJsonSupport.JSON_COMMAND_METADATA_REPLACE
					.equals(command)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> replacement = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				group.setMetadata(replacement);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_MODIFY
					.equals(command)) {
				Map<String, Object> metadata = group.getMetadata();

				@SuppressWarnings("unchecked")
				Map<String, Object> modifications = (Map<String, Object>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (Entry<String, Object> entry : modifications.entrySet()) {
					metadata.put(entry.getKey(), entry.getValue());
				}

				group.setMetadata(metadata);
			} else if (MetadataJsonSupport.JSON_COMMAND_METADATA_DELETE
					.equals(command)) {
				Map<String, Object> metadata = group.getMetadata();

				@SuppressWarnings("unchecked")
				List<String> modifications = (List<String>) metadataCommand
						.get(JsonSupport.JSON_PARAMETER_DATA);
				for (String entry : modifications) {
					metadata.remove(entry);
				}

				group.setMetadata(metadata);
			} else {
				return JsonSupport
						.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_COMMAND_UNKNOWN);
			}

			activityRepository.saveLiveActivityGroup(group);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					"Could not modify live activity group metadata", e);

			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_FAILURE);
		}
	}

	/**
	 * Update the deployment time of a live activity.
	 * 
	 * @param uuid
	 *            UUID of the live activity
	 * @param timestamp
	 *            timestamp of the time of deployment
	 */
	public void updateLiveActivityDeploymentTime(String uuid, long timestamp) {
		LiveActivity liveActivity = activityRepository
				.getLiveActivityByUuid(uuid);
		if (liveActivity != null) {
			liveActivity.setLastDeployDate(new Date(timestamp));

			activityRepository.saveLiveActivity(liveActivity);
		} else {
			spaceEnvironment
					.getLog()
					.warn(String
							.format("Attempt to update deployment time for an unknown live activity %s",
									uuid));
		}
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param activityRepositoryManager
	 *            the activityRepositoryManager to set
	 */
	public void setActivityRepositoryManager(
			ActivityRepositoryManager activityRepositoryManager) {
		this.activityRepositoryManager = activityRepositoryManager;
	}

	/**
	 * @param activeControllerManager
	 *            the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			ActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
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
