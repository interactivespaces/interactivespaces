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

package interactivespaces.controller.client.master;

import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.util.resource.ManagedResource;

/**
 * Install Interactive Spaces activities to the remote location.
 *
 * @author Keith M. Hughes
 */
public interface RemoteActivityDeploymentManager extends ManagedResource {

  /**
   * Deploy an activity to its space controller.
   *
   * @param activity
   *          the activity to be deployed
   */
  void deployLiveActivity(ActiveLiveActivity activity);

  /**
   * Got a deployment result.
   *
   * @param status
   *          the status of the result
   */
  void handleLiveDeployResult(LiveActivityDeploymentResponse status);

  /**
   * Handle a resource deployment query response.
   *
   * <p>
   * This response may not be part of an activity deployment.
   *
   * @param response
   *          the query result
   *
   * @return {@code true} if the response was part of an activity deployment and
   *         has been handled
   */
  boolean handleResourceDeploymentQueryResponse(ContainerResourceDeploymentQueryResponse response);

  /**
   * Handle a resource deployment commit response.
   *
   * <p>
   * This response may not be part of an activity deployment.
   *
   * @param response
   *          the commit response
   *
   * @return {@code true} if the response was part of an activity deployment and
   *         has been handled
   */
  boolean handleResourceDeploymentCommitResponse(ContainerResourceDeploymentCommitResponse response);

  /**
   * Delete an activity from its space controller.
   *
   * @param activity
   *          the activity to be deleted
   */
  void deleteLiveActivity(ActiveLiveActivity activity);
}
