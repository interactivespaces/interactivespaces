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

package interactivespaces.controller.client.node.internal;

import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.client.node.SpaceControllerActivityInstallationManager;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeleteRequest;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeleteStatus;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeployRequest;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeployStatus;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces_msgs.LiveActivityDeleteStatus;

import java.util.Date;

/**
 * The controller side of the installation manager for Interactive Spaces live
 * activities.
 * 
 * @author Keith M. Hughes
 */
public class SimpleSpaceControllerActivityInstallationManager implements
		SpaceControllerActivityInstallationManager {

	/**
	 * The Spaces environment being used.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Manages deployment of activities.
	 */
	private ActivityInstallationManager activityInstallationManager;

	public SimpleSpaceControllerActivityInstallationManager(
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
	public SpaceControllerLiveActivityDeployStatus handleDeploymentRequest(
			SpaceControllerLiveActivityDeployRequest request) {
		String activityUri = request.getActivitySourceUri();
		String uuid = request.getUuid();

		// This will be usually set to the current possible error status.

		int status = -1;
		boolean success = true;

		Date installedDate = null;

		try {
			status = SpaceControllerLiveActivityDeployStatus.STATUS_FAILURE_COPY;
			activityInstallationManager.copyActivity(uuid, activityUri);

			status = SpaceControllerLiveActivityDeployStatus.STATUS_FAILURE_UNPACK;
			installedDate = activityInstallationManager.installActivity(uuid,
					request.getIdentifyingName(), request.getVersion());

			status = SpaceControllerLiveActivityDeployStatus.STATUS_SUCCESS;
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
	private SpaceControllerLiveActivityDeployStatus createDeployResult(
			SpaceControllerLiveActivityDeployRequest request, int status,
			boolean success, Date installedDate) {

		long timeDeployed = 0;
		if (installedDate != null) {
			timeDeployed = installedDate.getTime();
		}

		return new SpaceControllerLiveActivityDeployStatus(request.getUuid(),
				status, timeDeployed);
	}

	@Override
	public SpaceControllerLiveActivityDeleteStatus handleDeleteRequest(
			SpaceControllerLiveActivityDeleteRequest request) {
		boolean success = false;

		try {
			success = activityInstallationManager.removeActivity(request
					.getUuid());
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					String.format("Could not delete live activity %s",
							request.getUuid()), e);
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
	public SpaceControllerLiveActivityDeleteStatus createDeleteResponse(
			SpaceControllerLiveActivityDeleteRequest request, boolean success) {
		return new SpaceControllerLiveActivityDeleteStatus(request.getUuid(),
				success ? LiveActivityDeleteStatus.STATUS_SUCCESS
						: LiveActivityDeleteStatus.STATUS_FAILURE,
				spaceEnvironment.getTimeProvider().getCurrentTime());
	}
}
