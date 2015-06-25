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

package interactivespaces.controller.runtime.internal;

import interactivespaces.activity.deployment.LiveActivityDeploymentRequest;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.controller.runtime.SpaceControllerActivityInstallationManager;
import interactivespaces.controller.runtime.SpaceControllerLiveActivityDeleteRequest;
import interactivespaces.controller.runtime.SpaceControllerLiveActivityDeleteResponse;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager.RemoveActivityResult;
import interactivespaces.system.InteractiveSpacesEnvironment;

import interactivespaces_msgs.LiveActivityDeleteStatus;

import java.util.Date;

/**
 * The controller side of the installation manager for Interactive Spaces live activities.
 *
 * @author Keith M. Hughes
 */
public class SimpleSpaceControllerActivityInstallationManager implements SpaceControllerActivityInstallationManager {

  /**
   * The Spaces environment being used.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Manages deployment of activities.
   */
  private final ActivityInstallationManager activityInstallationManager;

  /**
   * Construct a controller activity installation manager.
   *
   * @param activityInstallationManager
   *          the installation manager for an activity
   * @param spaceEnvironment
   *          the space environment to run under
   */
  public SimpleSpaceControllerActivityInstallationManager(ActivityInstallationManager activityInstallationManager,
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
  public LiveActivityDeploymentResponse handleDeploymentRequest(LiveActivityDeploymentRequest request) {
    String activityUri = request.getActivitySourceUri();
    String uuid = request.getUuid();

    // This will be usually set to the current possible error status.
    ActivityDeployStatus status = null;
    boolean success = true;

    Date installedDate = null;

    try {
      status = ActivityDeployStatus.STATUS_FAILURE_COPY;
      activityInstallationManager.copyActivity(uuid, activityUri);

      status = ActivityDeployStatus.STATUS_FAILURE_UNPACK;
      installedDate =
          activityInstallationManager.installActivity(uuid, request.getIdentifyingName(), request.getVersion());

      status = ActivityDeployStatus.STATUS_SUCCESS;
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Could not install live activity %s", uuid), e);

      success = false;
    } finally {
      activityInstallationManager.removePackedActivity(uuid);
    }

    return createDeployResult(request, status, success, installedDate);
  }

  /**
   * Create the status for the deployment.
   *
   * @param request
   *          the deployment request
   * @param status
   *          final status
   * @param success
   *          if the deployment was successful
   * @param installedDate
   *          date to mark it if successful
   * @return an appropriately filled out deployment status
   */
  private LiveActivityDeploymentResponse createDeployResult(LiveActivityDeploymentRequest request,
      ActivityDeployStatus status, boolean success, Date installedDate) {

    long timeDeployed = 0;
    if (installedDate != null) {
      timeDeployed = installedDate.getTime();
    }

    return new LiveActivityDeploymentResponse(request.getTransactionId(), request.getUuid(), status, null,
        timeDeployed);
  }

  @Override
  public SpaceControllerLiveActivityDeleteResponse
      handleDeleteRequest(SpaceControllerLiveActivityDeleteRequest request) {
    RemoveActivityResult result = RemoveActivityResult.FAILURE;

    try {
      result = activityInstallationManager.removeActivity(request.getUuid());
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Could not delete live activity %s", request.getUuid()), e);
    }

    return createDeleteResponse(request, result);
  }

  /**
   * Create the delete response to the request.
   *
   * @param request
   *          the deletion request
   * @param result
   *          result of the deletion request
   *
   * @return the response to be sent back
   */
  public SpaceControllerLiveActivityDeleteResponse createDeleteResponse(
      SpaceControllerLiveActivityDeleteRequest request, RemoveActivityResult result) {
    int status;
    switch (result) {
      case SUCCESS:
        status = LiveActivityDeleteStatus.STATUS_SUCCESS;
        break;

      case DOESNT_EXIST:
        status = LiveActivityDeleteStatus.STATUS_DOESNT_EXIST;
        break;

      default:
        status = LiveActivityDeleteStatus.STATUS_FAILURE;
    }

    return new SpaceControllerLiveActivityDeleteResponse(request.getUuid(), status, spaceEnvironment.getTimeProvider()
        .getCurrentTime());
  }
}
