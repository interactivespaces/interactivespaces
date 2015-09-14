/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.master.server.services;

import interactivespaces.domain.basic.LiveActivityGroup;

/**
 * A {@link LiveActivityGroup} which is active in the system.
 *
 * @author Keith M. Hughes
 */
public class ActiveLiveActivityGroup {

  /**
   * The activity group this represents.
   */
  private LiveActivityGroup activityGroup;

  /**
   * ID of the activity group.
   */
  private String id;

  public ActiveLiveActivityGroup(LiveActivityGroup activityGroup) {
    this.activityGroup = activityGroup;
    this.id = activityGroup.getId();
  }

  /**
   * @return the activityGroup
   */
  public LiveActivityGroup getActivityGroup() {
    return activityGroup;
  }

  /**
   * Update the live activity group object contained within.
   *
   * <p>
   * This allows this object access to merged data.
   *
   * @param activityGroup
   *          the potentially updated live activity group entity
   */
  public void updateLiveActivityGroup(LiveActivityGroup activityGroup) {
    this.activityGroup = activityGroup;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ActiveLiveActivityGroup other = (ActiveLiveActivityGroup) obj;
    return id.equals(other.id);
  }
}
