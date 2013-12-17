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

import interactivespaces.resource.Version;

/**
 * A request for an activity deployment.
 *
 * @author Keith M. Hughes
 */
public class ActivityDeploymentRequest {

  /**
   * The transaction ID for this request.
   */
  private final String transactionId;

  /**
   * UUID for the activity.
   */
  private final String uuid;

  /**
   * Identifying name for the activity.
   */
  private final String identifyingName;

  /**
   * Version of the activity.
   */
  private final Version version;

  /**
   * URI for getting the activity.
   */
  private final String activitySourceUri;

  /**
   * Construct a deployment request.
   *
   * @param transactionId
   *          ID for the deployment transaction
   * @param uuid
   *          UUID of the live activity
   * @param identifyingName
   *          identifying name of the live activity
   * @param version
   *          version of the activity
   * @param activitySourceUri
   *          URI for obtaining the source
   */
  public ActivityDeploymentRequest(String transactionId, String uuid, String identifyingName, Version version,
      String activitySourceUri) {
    this.transactionId = transactionId;
    this.uuid = uuid;
    this.identifyingName = identifyingName;
    this.version = version;
    this.activitySourceUri = activitySourceUri;
  }

  /**
   * Get the transaction ID for the request.
   *
   * @return the transaction ID
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Get the UUID for the live activity being deployed.
   *
   * @return the UUID for the live activity
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Get the identifying name for the live activity being deployed.
   *
   * @return the identifying name for the live activity
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Get the version of the live activity being deployed.
   *
   * @return the version of the live activity
   */
  public Version getVersion() {
    return version;
  }

  /**
   * Get the URI for obtaining the activity.
   *
   * @return the URI for obtaining the activity
   */
  public String getActivitySourceUri() {
    return activitySourceUri;
  }

  @Override
  public String toString() {
    return "ActivityDeploymentRequest [transactionId=" + transactionId + ", uuid=" + uuid + ", identifyingName="
        + identifyingName + ", version=" + version + ", activitySourceUri=" + activitySourceUri + "]";
  }
}
