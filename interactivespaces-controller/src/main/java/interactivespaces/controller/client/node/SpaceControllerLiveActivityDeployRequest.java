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

import interactivespaces.resource.Version;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Request for a live activity deployment on a space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerLiveActivityDeployRequest {

  /**
   * UUID for the live activity.
   */
  private final String uuid;

  /**
   * URI for the activity source.
   */
  private final String activitySourceUri;

  /**
   * Identifying name of the live activity.
   */
  private final String identifyingName;

  /**
   * Version of the live activity.
   */
  private final Version version;

  /**
   * The dependencies for this deployment.
   */
  private final Set<SpaceControllerContainerDependency> dependencies = Sets.newHashSet();

  /**
   * Construct a deployment request.
   *
   * @param uuid
   *          UUID of the activity
   * @param activitySourceUri
   *          URI of where to get the activity
   * @param identifyingName
   *          the identifying name of the activity
   * @param version
   *          the version of the activity
   */
  public SpaceControllerLiveActivityDeployRequest(String uuid, String activitySourceUri, String identifyingName,
      Version version) {
    this.uuid = uuid;
    this.activitySourceUri = activitySourceUri;
    this.identifyingName = identifyingName;
    this.version = version;
  }

  /**
   * @return the activitySourceUri
   */
  public String getActivitySourceUri() {
    return activitySourceUri;
  }

  /**
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * @return the identifyingName
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Get the version.
   *
   * @return the version
   */
  public Version getVersion() {
    return version;
  }

  /**
   * Add a new dependency to the request.
   *
   * @param dependency
   *          the dependency to add
   */
  public void addDependency(SpaceControllerContainerDependency dependency) {
    dependencies.add(dependency);
  }

  /**
   * @return the dependencies
   */
  public Set<SpaceControllerContainerDependency> getDependencies() {
    return dependencies;
  }
}
