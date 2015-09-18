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

package interactivespaces.activity;

/**
 * A listener for events on an activity.
 *
 * @author Keith M. Hughes
 */
public interface ActivityListener {

  /**
   * There has been a change in the status of an activity.
   *
   * @param activity
   *          the activity that has changed its status
   * @param oldStatus
   *          the previous status of the activity
   * @param newStatus
   *          the new status of the activity
   */
  void onActivityStatusChange(Activity activity, ActivityStatus oldStatus, ActivityStatus newStatus);
}
