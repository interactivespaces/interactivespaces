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

package interactivespaces.controller.client.node;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.domain.InstalledLiveActivity;

/**
 * A factory for {@link ActivityWrapper} instances.
 *
 * @author Keith M. Hughes
 */
public interface ActiveControllerActivityFactory {

  /**
   * Create a runner for a given activity type.
   *
   * @param activityType
   *          the type of activity being created
   * @param liapp
   *          the activity to be run.
   * @param activityFilesystem
   *          the filesystem for the activity
   * @param configuration
   *          configuration for the activity.
   * @param controller
   *          the controller which will run the activity
   *
   * @return A runner for the activity.
   */
  ActiveControllerActivity createActiveLiveActivity(String activityType,
      InstalledLiveActivity liapp, ActivityFilesystem activityFilesystem,
      LiveActivityConfiguration configuration, StandardSpaceController controller);

  /**
   * Create an appropriate runner.
   *
   * <p>
   * The activity type is determined from the {@code configuration} using the
   * {@link #getConfiguredType(Configuration)} method.
   *
   * @param liapp
   *          the activity to be run.
   * @param activityFilesystem
   *          the activity's filesystem
   * @param configuration
   *          configuration for the activity.
   * @param controller
   *          the controller which will run the activity
   *
   * @return A runner for the activity.
   */
  ActiveControllerActivity newActiveActivity(InstalledLiveActivity liapp,
      ActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      StandardSpaceController controller);

  /**
   * Get the activity type of the activity.
   *
   * @param configuration
   *          the configuration of the activity.
   *
   * @return the activity type
   *
   * @throws InteractiveSpacesException
   *           if can't determine the activity type.
   */
  String getConfiguredType(Configuration configuration);

  /**
   * Register an {@link ActivityWrapperFactory}.
   *
   * @param factory
   */
  void registerActivityWrapperFactory(ActivityWrapperFactory factory);

  /**
   * Unregister an {@link ActivityWrapperFactory}.
   *
   * <p>
   * Nothing happens if the factory was never registered.
   *
   * @param factory
   */
  void unregisterActivityWrapperFactory(ActivityWrapperFactory factory);
}
