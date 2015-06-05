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

package interactivespaces.activity.deployment;

/**
 * The response of a live activity deployment on the controller.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityDeploymentResponse {

  /**
   * Transaction ID for the deployment.
   */
  private String transactionId;

  /**
   * UUID of the live activity that was deployed.
   */
  private final String uuid;

  /**
   * Status of the deployment.
   */
  private final ActivityDeployStatus status;

  /**
   * Time that the activity was deployed.
   */
  private final long timeDeployed;

  /**
   * Construct a response.
   *
   * @param transactionId
   *          transaction ID for the response
   * @param uuid
   *          UUID of the live activity
   * @param status
   *          status of the deployment
   * @param timeDeployed
   *          time the activity was deployed
   */
  public LiveActivityDeploymentResponse(String transactionId, String uuid, ActivityDeployStatus status,
      long timeDeployed) {
    this.transactionId = transactionId;
    this.uuid = uuid;
    this.status = status;
    this.timeDeployed = timeDeployed;
  }

  /**
   * Get the transaction ID for the deployment.
   *
   * @return the transaction ID for the deployment
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Set the transaction ID for the deployment.
   *
   * @param transactionId
   *          the transaction ID for the deployment
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get the UUID for the live activity which was deployed.
   *
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Get the status of the deployment.
   *
   * @return the status
   */
  public ActivityDeployStatus getStatus() {
    return status;
  }

  /**
   * Get the time of the deployment, according to the remote system.
   *
   * @return the time of the deployement
   */
  public long getTimeDeployed() {
    return timeDeployed;
  }

  /**
   * State of the deployment.
   *
   * @author Keith M. Hughes
   */
  public enum ActivityDeployStatus {
    /**
     * Deployment was a success.
     */
    STATUS_SUCCESS(true),

    /**
     * Failed to copy the activity from the repository.
     */
    STATUS_FAILURE_COPY(false),

    /**
     * Could not unpack the live activity.
     */
    STATUS_FAILURE_UNPACK(false),

    /**
     * The dependencies could not be committed for the deployment.
     */
    STATUS_FAILURE_DEPENDENCIES_NOT_COMMITTED(false);

    /**
     * {@code true} if this is a success message.
     */
    private final boolean success;

    /**
     * Construct a status.
     *
     * @param success
     *          {@code true} if this is a success state
     */
    private ActivityDeployStatus(boolean success) {
      this.success = success;
    }

    /**
     * Is this a success state?
     *
     * @return {@code true} if this is a success state
     */
    public boolean isSuccess() {
      return success;
    }
  }
}
