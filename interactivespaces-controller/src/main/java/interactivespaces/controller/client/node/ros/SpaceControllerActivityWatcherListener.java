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

package interactivespaces.controller.client.node.ros;

import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.client.node.ActiveControllerActivity;

/**
 * Listeners for events from the {@link SpaceControllerActivityWatcher}.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerActivityWatcherListener {

  /**
   * The status of the given activity has changed.
   *
   * <p>
   * The change is not an error.
   *
   * @param activity
   *          The activity whose status has changed.
   * @param oldStatus
   *          The status the activity had before the change
   * @param newStatus
   *          The status of the activity after the change.
   */
  void onWatcherActivityStatusChange(ActiveControllerActivity activity, ActivityStatus oldStatus,
      ActivityStatus newStatus);

  /**
   * An activity has had an error.
   *
   * @param activity
   *          The activity whose status has changed.
   * @param oldStatus
   *          The status the activity had before the change
   * @param newStatus
   *          The status of the activity after the change.
   */
  void onWatcherActivityError(ActiveControllerActivity activity, ActivityStatus oldStatus,
      ActivityStatus newStatus);
}
