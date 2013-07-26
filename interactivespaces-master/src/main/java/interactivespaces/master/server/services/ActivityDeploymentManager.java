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

/**
 * Handles deployment of activities to controllers.
 *
 * @author Keith M. Hughes
 */
public interface ActivityDeploymentManager {

  /**
   * Delete the live activity to its controller.
   *
   * @param liveActivity
   *          the activity to deploy
   */
  void deployLiveActivity(ActiveLiveActivity liveActivity);

  /**
   * Delete the live activity from its controller.
   *
   * @param liveActivity
   *          the activity to delete
   */
  void deleteLiveActivity(ActiveLiveActivity liveActivity);
}
