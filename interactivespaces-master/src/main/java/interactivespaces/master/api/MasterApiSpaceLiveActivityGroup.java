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

package interactivespaces.master.api;

import interactivespaces.domain.basic.LiveActivityGroup;

import java.util.List;

/**
 * Master API information about a live activity group which is part of a space.
 *
 * @author Keith M. Hughes
 */
@Deprecated
public class MasterApiSpaceLiveActivityGroup {

  /**
   * The live activity group represented.
   */
  private final LiveActivityGroup liveActivityGroup;

  /**
   * The list of all live activities for a given group.
   */
  private final List<MasterApiLiveActivity> liveActivities;

  /**
   * Construct the master API representation of a live activity group.
   *
   * @param liveActivityGroup
   *          the group
   * @param liveActivities
   *          the live activities in the group
   */
  public MasterApiSpaceLiveActivityGroup(LiveActivityGroup liveActivityGroup, List<MasterApiLiveActivity> liveActivities) {
    this.liveActivityGroup = liveActivityGroup;
    this.liveActivities = liveActivities;
  }

  /**
   * Get the live activity group.
   *
   * @return the live activity group
   */
  public LiveActivityGroup getLiveActivityGroup() {
    return liveActivityGroup;
  }

  /**
   * Get the live activities in the group.
   *
   * @return the live activities in the group
   */
  public List<MasterApiLiveActivity> getLiveActivities() {
    return liveActivities;
  }
}
