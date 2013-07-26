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

package interactivespaces.master.ui.internal.web;

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.ui.UiLiveActivity;

import java.util.List;

/**
 * UI information about a live activity group which is part of a space.
 *
 * @author Keith M. Hughes
 */
public class UiSpaceLiveActivityGroup {

  /**
   * The live activity group represented.
   */
  private LiveActivityGroup liveActivityGroup;

  /**
   * The list of all live activities for a given group.
   */
  private List<UiLiveActivity> liveActivities;

  public UiSpaceLiveActivityGroup(LiveActivityGroup liveActivityGroup,
      List<UiLiveActivity> liveActivities) {
    this.liveActivityGroup = liveActivityGroup;
    this.liveActivities = liveActivities;
  }

  /**
   * @return the liveActivityGroup
   */
  public LiveActivityGroup getLiveActivityGroup() {
    return liveActivityGroup;
  }

  /**
   * @return the liveActivities
   */
  public List<UiLiveActivity> getLiveActivities() {
    return liveActivities;
  }
}
