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
 * Status of a deletion of a live activity from a space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerLiveActivityDeleteStatus {

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

  public SpaceControllerLiveActivityDeleteStatus(String uuid, int status, long timeDeleted) {
    this.uuid = uuid;
    this.status = status;
    this.timeDeleted = timeDeleted;
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
   * @return the timeDeleted
   */
  public long getTimeDeleted() {
    return timeDeleted;
  }
}
