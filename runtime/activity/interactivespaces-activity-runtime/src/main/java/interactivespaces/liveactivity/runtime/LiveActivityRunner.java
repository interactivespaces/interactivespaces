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

package interactivespaces.liveactivity.runtime;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityControl;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;

import java.util.Map;

/**
 * A runner for a single Live Activity.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityRunner extends ActivityControl {

  /**
   * Initial status for an activity.
   */
  ActivityStatus INITIAL_ACTIVITY_STATUS = new ActivityStatus(ActivityState.READY, null);

  /**
   * Get the UUID of the underlying live activity.
   *
   * @return the uuid
   */
  String getUuid();

  /**
   * Update the configuration of the activity.
   *
   * @param update
   *          a map of the configuration update
   */
  void updateConfiguration(Map<String, String> update);

  /**
   * Get the state of the live activity.
   *
   * <p>
   * This will be the last known state of the activity. The activity itself will be sampled.
   *
   * @return the state of the activity
   */
  ActivityStatus sampleActivityStatus();

  /**
   * Get the last known activity status. Does not sample the activity.
   *
   * @return the cached activity status
   */
  ActivityStatus getCachedActivityStatus();

  /**
   * Get the instance of the activity.
   *
   * @return the instance (can be {@code null} if the activity has not been started)
   */
  Activity getInstance();

  /**
   * Set the state of the live activity.
   *
   * @param status
   *          activity status to set
   */
  void setActivityStatus(ActivityStatus status);

  /**
   * Get the activity wrapper for this instance.
   *
   * @return the activity wrapper
   */
  ActivityWrapper getActivityWrapper();

  /**
   * Get the display name for the live activity in the runner.
   *
   * @return the display name
   */
  String getDisplayName();

  /**
   * Mark the live activity runner as stale.
   *
   * <p>
   * "Stale" implies that the live activity underlying the runner has changed and the runner has potentially wrong
   * information.
   */
  void markStale();

  /**
   * Is the live activity runner stale?
   *
   * @return {@code true} if stale
   */
  boolean isStale();
}
