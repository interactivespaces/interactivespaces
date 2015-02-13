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

package interactivespaces.controller.activity.wrapper;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.Version;

/**
 * An {@link ActivityWrapperFactory} base class with some potential useful methods.
 *
 * @author Keith M. Hughes
 *
 * @deprecated use {@link interactivespaces.liveactivity.runtime.activity.wrapper.BaseActivityWrapperFactory}.
 */
@Deprecated
public abstract class BaseActivityWrapperFactory extends
    interactivespaces.liveactivity.runtime.activity.wrapper.BaseActivityWrapperFactory implements
    ActivityWrapperFactory {

  /**
   * Construct a factory with a null version.
   */
  public BaseActivityWrapperFactory() {
    this(null);
  }

  /**
   * Construct a factory.
   *
   * @param version
   *          the version supported by the factory
   */
  public BaseActivityWrapperFactory(Version version) {
    super(version);
  }

  @Override
  public interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper newActivityWrapper(
      InstalledLiveActivity liveActivity, ActivityFilesystem activityFilesystem, Configuration configuration,
      ActivityRuntime activityRuntime) {
    return newActivityWrapper((interactivespaces.controller.domain.InstalledLiveActivity) liveActivity,
        activityFilesystem, configuration, (SpaceController) null);
  }

  @Deprecated
  @Override
  public ActivityWrapper newActivityWrapper(interactivespaces.controller.domain.InstalledLiveActivity liveActivity,
      ActivityFilesystem activityFilesystem, Configuration configuration, SpaceController controller) {
    throw new SimpleInteractiveSpacesException(String.format(
        "Activity wrapper class %s does not override a newActivityWrapper method", getClass().getName()));
  }
}
