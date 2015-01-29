/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone.stubs;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.activity.wrapper.BaseActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces.InteractiveSpacesNativeActivityWrapper;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces.LiveActivityBundleLoader;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.standalone.startup.StandaloneLiveActivityBundleLoader;

/**
 * A very simple {@link interactivespaces.controller.activity.wrapper.ActivityWrapperFactory}
 * for Interactive Spaces Native apps.
 *
 * @author Trevor Pering
 */
public class StandaloneNativeActivityWrapperFactory extends BaseActivityWrapperFactory {

  /**
   * The bundle loader to use for loading live activities.
   */
  private final LiveActivityBundleLoader bundleLoader;

  /**
   * Construct a new IS native activity wrapper factory.
   *
   */
  public StandaloneNativeActivityWrapperFactory() {
    this.bundleLoader = new StandaloneLiveActivityBundleLoader();
  }

  @Override
  public String getActivityType() {
    return "interactivespaces_native";
  }

  @Override
  public ActivityWrapper newActivityWrapper(InstalledLiveActivity liveActivity, ActivityFilesystem activityFilesystem,
      Configuration configuration, ActivityRuntime activityRuntime) {
    return new InteractiveSpacesNativeActivityWrapper(liveActivity, activityFilesystem, configuration, bundleLoader);
  }
}
