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
 * Status of a deletion of a live activity from a space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerLiveActivityDeleteResponse {

  /**
   * The deletion was successful.
   */
  public static final int STATUS_SUCCESS = 0;

  /**
   * The deletion was a failure.
   */
  public static final int STATUS_FAILURE = 1;

  /**
   * UUID of the live activity deleted.
   */
  private String uuid;

  /**
   * Status of the deletion.
   */
  private int status;

  /**
   * The time the activity was deleted.
   */
  private long timeDeleted;

  /**
   * Construct the status.
   *
   * @param uuid
   *          UUID of the live activity
   * @param status
   *          status of the deletion
   * @param timeDeleted
   *          the time the live activity was deleted
   */
  public SpaceControllerLiveActivityDeleteResponse(String uuid, int status, long timeDeleted) {
    this.uuid = uuid;
    this.status = status;
    this.timeDeleted = timeDeleted;
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
   * Get the status of the deletion.
   *
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * Get the time of the deletion.
   *
   * @return the time deleted
   */
  public long getTimeDeleted() {
    return timeDeleted;
  }
}
