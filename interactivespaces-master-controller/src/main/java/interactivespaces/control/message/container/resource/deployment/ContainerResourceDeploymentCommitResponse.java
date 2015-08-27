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

package interactivespaces.control.message.container.resource.deployment;

/**
 * Response from a container deployment request.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceDeploymentCommitResponse {

  /**
   * The transaction ID for the deployment request.
   */
  private final String transactionId;

  /**
   * The status of the commit.
   */
  private final ContainerResourceDeploymentCommitStatus status;

  /**
   * The detail on the response. Can be {@code null}.
   */
  private final String detail;

  /**
   * Construct a new response.
   *
   * @param transactionId
   *          transaction ID for the commit
   * @param status
   *          status of the response
   * @param detail
   *          detail of the status, can be {@code null}
   */
  public ContainerResourceDeploymentCommitResponse(String transactionId,
      ContainerResourceDeploymentCommitStatus status, String detail) {
    this.transactionId = transactionId;
    this.status = status;
    this.detail = detail;
  }

  /**
   * Get the transaction ID for the deployment transaction.
   *
   * @return the transaction ID for the deployment transaction
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Get the status of the response.
   *
   * @return the status of the response
   */
  public ContainerResourceDeploymentCommitStatus getStatus() {
    return status;
  }

  /**
   * Get the detail of the status, if any.
   *
   * @return the detail, can be {@code null}
   */
  public String getDetail() {
    return detail;
  }

  /**
   * The status of the commit.
   *
   * @author Keith M. Hughes
   */
  public enum ContainerResourceDeploymentCommitStatus {
    /**
     * The commit was a success.
     */
    SUCCESS,

    /**
     * The commit was a failure.
     */
    FAILURE,
  }
}
