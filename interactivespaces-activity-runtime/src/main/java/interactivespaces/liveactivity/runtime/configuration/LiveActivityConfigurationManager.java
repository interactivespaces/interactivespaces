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

package interactivespaces.liveactivity.runtime.configuration;

import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;

/**
 * Manage configurations for Interactive Spaces activities.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityConfigurationManager {

  /**
   * The activity type for the base activity.
   */
  String CONFIG_TYPE_BASE_ACTIVITY = "activity";

  /**
   * Get a configuration.
   *
   * @param liveActivity
   *          the live activity
   * @param activityFilesystem
   *          filesystem for the activity
   *
   * @return the installation specific configuration
   */
  LiveActivityConfiguration newLiveActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem);
}
