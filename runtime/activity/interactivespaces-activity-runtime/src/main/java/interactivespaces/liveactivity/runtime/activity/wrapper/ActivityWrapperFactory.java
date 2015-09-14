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

package interactivespaces.liveactivity.runtime.activity.wrapper;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.VersionedResource;

/**
 * A factory for {@link ActivityWrapper} instances of a specific activity type.
 *
 * @author Keith M. Hughes
 */
public interface ActivityWrapperFactory extends VersionedResource {

  /**
   * Get the activity type supported by this factory.
   *
   * @return the activity type of the factory
   */
  String getActivityType();

  /**
   * Create an activity wrapper.
   *
   * @param liveActivity
   *          the live to be run
   * @param activityFilesystem
   *          the filesystem for the activity
   * @param configuration
   *          configuration for the activity
   * @param activityRuntime
   *          the activity runtime
   *
   * @return a new activity wrapper
   */
  ActivityWrapper newActivityWrapper(InstalledLiveActivity liveActivity, ActivityFilesystem activityFilesystem,
      Configuration configuration, ActivityRuntime activityRuntime);
}
