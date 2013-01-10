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

package interactivespaces.controller.client.node.ros;

import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.client.node.SpaceControllerActivityInstallationManager;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Date;

import org.ros.message.interactivespaces_msgs.LiveActivityDeleteRequest;
import org.ros.message.interactivespaces_msgs.LiveActivityDeleteStatus;
import org.ros.message.interactivespaces_msgs.LiveActivityDeployRequest;
import org.ros.message.interactivespaces_msgs.LiveActivityDeployStatus;

/**
 * The controller side of the installation manager for Interactive Spaces live
 * activities.
 * 
 * @author Keith M. Hughes
 */
public class RosSpaceControllerActivityInstallationManager implements
		SpaceControllerActivityInstallationManager {

	/**
	 * The Spaces environment being used.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Manages deployment of activities.
	 */
	private ActivityInstallationManager activityInstallationManager;

	public RosSpaceControllerActivityInstallationManager(
			ActivityInstallationManager activityInstallationManager,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.activityInstallationManager = activityInstallationManager;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public void startup() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public LiveActivityDeployStatus handleDeploymentRequest(
			LiveActivityDeployRequest request) {
		String activityUri = request.activity_source_uri;
		String uuid = request.uuid;

		// This will be usually set to the current possible error status.

		int status = -1;
		boolean success = true;

		Date installedDate = null;

		try {
			status = LiveActivityDeployStatus.STATUS_FAILURE_COPY;
			activityInstallationManager.copyActivity(uuid, activityUri);

			status = LiveActivityDeployStatus.STATUS_FAILURE_UNPACK;
			installedDate = activityInstallationManager.installActivity(uuid,
					request.identifying_name, request.version);

			status = LiveActivityDeployStatus.STATUS_SUCCESS;
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					String.format("Could not install live activity %s", uuid),
					e);

			success = false;
		} finally {
			activityInstallationManager.removePackedActivity(uuid);
		}

		return createDeployResult(request, status, success, installedDate);
	}

	/**
	 * Create the status for the deployment
	 * 
	 * @param request
	 *            the deployment request
	 * @param status
	 *            final status
	 * @param success
	 *            if the deployment was successful
	 * @param installedDate
	 *            date to mark it if successful
	 * @return an appropriately filled out deployment status
	 */
	private LiveActivityDeployStatus createDeployResult(
			LiveActivityDeployRequest request, int status, boolean success,
			Date installedDate) {
		LiveActivityDeployStatus result = new LiveActivityDeployStatus();

		result.uuid = request.uuid;
		result.status = status;

		if (installedDate != null) {
			result.time_deployed = installedDate.getTime();
		}

		return result;
	}

	@Override
	public LiveActivityDeleteStatus handleDeleteRequest(
			LiveActivityDeleteRequest request) {
		boolean success = false;

		try {
			success = activityInstallationManager.removeActivity(request.uuid);
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					String.format("Could not delete live activity %s",
							request.uuid), e);
		}

		return createDeleteResponse(request, success);
	}

	/**
	 * Create the delete response to the request.
	 * 
	 * @param request
	 *            the deletion request
	 * @param success
	 *            {@code true} if the activity was deleted
	 * 
	 * @return the response to be sent back
	 */
	public LiveActivityDeleteStatus createDeleteResponse(
			LiveActivityDeleteRequest request, boolean success) {
		LiveActivityDeleteStatus status = new LiveActivityDeleteStatus();
		status.uuid = request.uuid;
		status.time_deleted = spaceEnvironment.getTimeProvider()
				.getCurrentTime();
		status.status = success ? LiveActivityDeleteStatus.STATUS_SUCCESS
				: LiveActivityDeleteStatus.STATUS_FAILURE;
		return status;
	}
}
