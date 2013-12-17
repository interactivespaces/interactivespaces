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

package interactivespaces.container.resource.deployment;

import interactivespaces.resource.ResourceDependency;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * A request for resource deployments to the container.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceDeploymentQueryRequest {

  /**
   * ID for the transaction for the deployment request.
   */
  private final String transactionId;

  /**
   * The queries.
   */
  private final Set<ResourceDependency> queries = Sets.newHashSet();

  /**
   * Construct a new query.
   *
   * @param transactionId
   *          transaction ID for the query
   */
  public ContainerResourceDeploymentQueryRequest(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get the transaction ID for the query.
   *
   * @return the transaction ID
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Add in a new query.
   *
   * @param query
   *          the query to add
   */
  public void addQuery(ResourceDependency query) {
    queries.add(query);
  }

  /**
   * Get the queries.
   *
   * @return the queries
   */
  public Set<ResourceDependency> getQueries() {
    return queries;
  }
}
