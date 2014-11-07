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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.client.node.InternalActivityFilesystem;
import interactivespaces.controller.domain.InstalledLiveActivity;

/**
 * A factory for {@link BasicLiveActivityRunner} instances.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityRunnerFactory {

  /**
   * Create an active controller activity for a given activity type.
   *
   * @param activityType
   *          the type of activity being created
   * @param installedLiveActivity
   *          the activity to be run
   * @param activityFilesystem
   *          the filesystem for the activity
   * @param configuration
   *          configuration for the activity
   * @param liveActivityRunnerListener
   *          the listener for live activity runner events
   * @param controller
   *          the controller which will run the activity
   *
   * @return the active controller activity instance
   */
  LiveActivityRunner newLiveActivityRunner(String activityType, InstalledLiveActivity installedLiveActivity,
      InternalActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      LiveActivityRunnerListener liveActivityRunnerListener, SpaceController controller);

  /**
   * Create an active controller activity for a given activity type.
   *
   * <p>
   * The activity type is determined from the {@code configuration} using the {@link #getConfiguredType(Configuration)}
   * method.
   *
   *
   * @param installedLiveActivity
   *          the activity to be run
   * @param activityFilesystem
   *          the activity's filesystem
   * @param configuration
   *          configuration for the activity
   * @param liveActivityRunnerListener
   *          the listener for live activity runner events
   * @param controller
   *          the controller which will run the activity
   *
   * @return a runner for the activity
   */
  BasicLiveActivityRunner newLiveActivityRunner(InstalledLiveActivity installedLiveActivity,
      InternalActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      LiveActivityRunnerListener liveActivityRunnerListener, SpaceController controller);

  /**
   * Get the activity type of the activity.
   *
   * @param configuration
   *          the configuration of the activity.
   *
   * @return the activity type
   *
   * @throws InteractiveSpacesException
   *           if can't determine the activity type
   */
  String getConfiguredType(Configuration configuration) throws InteractiveSpacesException;

  /**
   * Register an {@link ActivityWrapperFactory}.
   *
   * @param factory
   *          activity wrapper factory
   */
  void registerActivityWrapperFactory(ActivityWrapperFactory factory);

  /**
   * Unregister an {@link ActivityWrapperFactory}.
   *
   * <p>
   * Nothing happens if the factory was never registered.
   *
   * @param factory
   *          activity wrapper factory
   */
  void unregisterActivityWrapperFactory(ActivityWrapperFactory factory);
}
