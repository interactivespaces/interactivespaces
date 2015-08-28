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

package interactivespaces.container.control.message.activity;

/**
 * The response for an attempt to delete a live activity.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityDeleteResponse {

  /**
   * UUID of the live activity deleted.
   */
  private String uuid;

  /**
   * Status of the deletion.
   */
  private LiveActivityDeleteStatus status;

  /**
   * The time the activity was deleted.
   */
  private long timeDeleted;

  /**
   * The details of the result, can be {@code null}.
   */
  private String statusDetail;

  /**
   * Construct the status.
   *
   * @param uuid
   *          UUID of the live activity
   * @param status
   *          status of the deletion
   * @param timeDeleted
   *          the time the live activity was deleted
   * @param statusDetail
   *          the details of the status, can be {@code null}
   */
  public LiveActivityDeleteResponse(String uuid, LiveActivityDeleteStatus status, long timeDeleted, String statusDetail) {
    this.uuid = uuid;
    this.status = status;
    this.timeDeleted = timeDeleted;
    this.statusDetail = statusDetail;
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
  public LiveActivityDeleteStatus getStatus() {
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

  /**
   * Get the status detail of the deletion.
   *
   * @return the detail, can be {@code null}
   */
  public String getStatusDetail() {
    return statusDetail;
  }

  /**
   * The status of the deletion.
   *
   * @author Keith M. Hughes
   */
  public enum LiveActivityDeleteStatus {
    /**
     * The deletion was successful.
     */
    SUCCESS,

    /**
     * The deletion was a failure.
     */
    FAILURE,

    /**
     * The live activity does not exist.
     */
    DOESNT_EXIST
  }
}
