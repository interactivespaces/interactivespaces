/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.controller.client.node;

/**
 * The status of a live activity deployment on the controller
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerLiveActivityDeployStatus {

  /**
   * Deployment was a success.
   */
  public static final int STATUS_SUCCESS = 0;

  /**
   * Failed to copy the activity from the repository.
   */
  public static final int STATUS_FAILURE_COPY = 1;

  /**
   * Could not unpack the live activity.
   */
  public static final int STATUS_FAILURE_UNPACK = 2;

  /**
   * UUID of the live activity that was deployed.
   */
  private String uuid;

  /**
   * Status of the deployment.
   */
  private int status;

  /**
   * Time that the activity was deployed.
   */
  private long timeDeployed;

  public SpaceControllerLiveActivityDeployStatus(String uuid, int status, long timeDeployed) {
    this.uuid = uuid;
    this.status = status;
    this.timeDeployed = timeDeployed;
  }

  /**
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * @return the timeDeployed
   */
  public long getTimeDeployed() {
    return timeDeployed;
  }
}
