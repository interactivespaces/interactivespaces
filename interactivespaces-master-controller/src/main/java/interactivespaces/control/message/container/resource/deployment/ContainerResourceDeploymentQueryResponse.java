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
 * A response to a resource deployment query.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceDeploymentQueryResponse {

  /**
   * The transaction ID for the response.
   */
  private final String transactionId;

  /**
   * The status of the response.
   */
  private final QueryResponseStatus status;

  /**
   * Construct a response.
   *
   * @param transactionId
   *          ID of the transaction
   * @param status
   *          status for the response
   */
  public ContainerResourceDeploymentQueryResponse(String transactionId, QueryResponseStatus status) {
    this.transactionId = transactionId;
    this.status = status;
  }

  /**
   * Get the ID of the transaction.
   *
   * @return the ID of the transaction
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Get the status for the response.
   *
   * @return the status for the response
   */
  public QueryResponseStatus getStatus() {
    return status;
  }

  /**
   * The status of the response.
   *
   * @author Keith M. Hughes
   */
  public enum QueryResponseStatus {

    /**
     * The specific query is satisfied.
     */
    SPECIFIC_QUERY_SATISFIED,

    /**
     * The specific query is not satisfied.
     */
    SPECIFIC_QUERY_NOT_SATISFIED,

    /**
     * The general query response.
     */
    GENERAL_QUERY_RESPONSE
  }
}
