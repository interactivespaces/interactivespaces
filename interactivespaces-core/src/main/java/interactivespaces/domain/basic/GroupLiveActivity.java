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

package interactivespaces.domain.basic;

import java.io.Serializable;

/**
 * A live activity that is part of a {@link LiveActivityGroup}.
 *
 * @author Keith M. Hughes
 */
public interface GroupLiveActivity extends Serializable {

  /**
   * Get the activity group this activity is part of.
   *
   * @return the live activity group
   */
  LiveActivityGroup getActivityGroup();

  /**
   * Set the activity group this activity is part of.
   *
   * @param activityGroup
   *          the live activity group
   */
  void setActivityGroup(LiveActivityGroup activityGroup);

  /**
   * Get the activity this represents.
   *
   * @return the live activity
   */
  LiveActivity getActivity();

  /**
   * Set the live activity this represents.
   *
   * @param activity
   *          the live activity
   */
  void setActivity(LiveActivity activity);

  /**
   * Get the dependency status of the activity in its containing activity group.
   *
   * @return the dependency status
   */
  GroupLiveActivityDependency getDependency();

  /**
   * Set the dependency status of the activity in its containing activity group.
   *
   * @param dependency
   *          the dependency
   */
  void setDependency(GroupLiveActivityDependency dependency);

  /**
   * The status of a live activity dependency in a live activity group.
   *
   * @author Keith M. Hughes
   */
  public enum GroupLiveActivityDependency {

    /**
     * The live activity is required in the group.
     */
    REQUIRED("activity.group.activity.dependency.required"),

    /**
     * The live activity is optional in the group.
     */
    OPTIONAL("activity.group.activity.dependency.optional");

    /**
     * Message ID for the description.
     */
    private String description;

    /**
     * Construct a new enum.
     *
     * @param description
     *          the description
     */
    GroupLiveActivityDependency(String description) {
      this.description = description;
    }

    /**
     * Get the dependency description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }
}
