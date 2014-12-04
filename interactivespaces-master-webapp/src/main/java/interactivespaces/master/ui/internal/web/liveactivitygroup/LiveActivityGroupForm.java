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

package interactivespaces.master.ui.internal.web.liveactivitygroup;

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.GroupLiveActivity.GroupLiveActivityDependency;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.pojo.SimpleLiveActivityGroup;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.UiUtilities;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * A simple form for activity groups.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityGroupForm implements Serializable {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = -4185306452314355537L;

  /**
   * The activity group data.
   */
  private SimpleLiveActivityGroup liveActivityGroup = new SimpleLiveActivityGroup();

  /**
   * The IDs for live activities.
   */
  private List<String> liveActivityIds;

  /**
   * Get the live activity group from the form.
   *
   * @return the live activity group
   */
  public SimpleLiveActivityGroup getLiveActivityGroup() {
    return liveActivityGroup;
  }

  /**
   * Set the live activity group from the form.
   *
   * @param liveActivityGroup
   *          the live activity group
   */
  public void setActivityGroup(SimpleLiveActivityGroup liveActivityGroup) {
    this.liveActivityGroup = liveActivityGroup;
  }

  /**
   * Get the live activity IDs.
   *
   * @return the live activity IDs
   */
  public List<String> getLiveActivityIds() {
    return liveActivityIds;
  }

  /**
   * Set the live activity IDs.
   *
   * @param liveActivityIds
   *          the live activity IDs
   */
  public void setLiveActivityIds(List<String> liveActivityIds) {
    this.liveActivityIds = liveActivityIds;
  }

  /**
   * Copy the contents of the form into the supplied group.
   *
   * @param destinationGroup
   *          the live activity group that the contents should be copied into
   * @param activityRepository
   *          repository for activity entities
   */
  public void saveLiveActivityGroup(LiveActivityGroup destinationGroup, ActivityRepository activityRepository) {
    destinationGroup.setName(liveActivityGroup.getName());
    destinationGroup.setDescription(liveActivityGroup.getDescription());

    destinationGroup.clearActivities();
    if (liveActivityIds != null && !liveActivityIds.contains(UiUtilities.MULTIPLE_SELECT_NONE)) {
      for (String liveActivityId : liveActivityIds) {
        LiveActivity activity = activityRepository.getLiveActivityById(liveActivityId);
        if (activity != null) {
          destinationGroup.addLiveActivity(activity, GroupLiveActivityDependency.REQUIRED);
        }
      }
    }
  }

  /**
   * Copy the contents of the supplied group into the form.
   *
   * @param sourceGroup
   *          the live activity group that the contents should be copied from
   */
  public void copyLiveActivityGroup(LiveActivityGroup sourceGroup) {
    liveActivityGroup.setName(sourceGroup.getName());
    liveActivityGroup.setDescription(sourceGroup.getDescription());

    liveActivityIds = Lists.newArrayList();
    for (GroupLiveActivity activity : sourceGroup.getLiveActivities()) {
      liveActivityIds.add(activity.getActivity().getId());
    }
  }
}
