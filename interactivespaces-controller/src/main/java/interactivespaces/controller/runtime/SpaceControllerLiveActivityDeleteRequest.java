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

package interactivespaces.controller.runtime;

/**
 * Request for a deletion of a live activity from a space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerLiveActivityDeleteRequest {

  /**
   * UUID of the live activity to be deleted.
   */
  private String uuid;

  /**
   * Identifying name of the live activity to delete.
   */
  private String identifyingName;

  /**
   * Version of the live activity to delete.
   */
  private String version;

  /**
   * Construct the request.
   *
   * @param uuid
   *          UUID of the live activity
   * @param identifyingName
   *          identifying name of the live activity
   * @param version
   *          version of the live activity
   */
  public SpaceControllerLiveActivityDeleteRequest(String uuid, String identifyingName, String version) {
    this.uuid = uuid;
    this.identifyingName = identifyingName;
    this.version = version;
  }

  /**
   * Get the UUID of the live activity.
   *
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Get the identifying name of the live activity.
   *
   * @return the identifying name
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Get the version of the activity.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }
}
