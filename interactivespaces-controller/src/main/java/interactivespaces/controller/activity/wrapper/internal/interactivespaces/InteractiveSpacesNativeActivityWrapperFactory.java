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

package interactivespaces.controller.activity.wrapper.internal.interactivespaces;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.BaseActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.util.data.resource.MessageDigestResourceSignature;
import interactivespaces.util.data.resource.ResourceSignature;

import org.osgi.framework.BundleContext;

/**
 * A {@link ActivityWrapperFactory} for Interactive Spaces Native apps.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesNativeActivityWrapperFactory extends BaseActivityWrapperFactory {

  /**
   * The name of the activity type.
   */
  public static final String ACTIVITY_TYPE_NAME = "interactivespaces_native";

  /**
   * The bundle loader to use for loading live activities.
   */
  private final LiveActivityBundleLoader bundleLoader;

  /**
   * The bundle comparer.
   */
  private final ResourceSignature bundleComparer;

  /**
   * Construct a new IS native activity wrapper factory.
   *
   * @param bundleContext
   *          the OSGi bundle context for the factory
   */
  public InteractiveSpacesNativeActivityWrapperFactory(BundleContext bundleContext) {
    this.bundleComparer = new MessageDigestResourceSignature();
    this.bundleLoader = new SimpleLiveActivityBundleLoader(bundleContext, bundleComparer);
  }

  @Override
  public String getActivityType() {
    return ACTIVITY_TYPE_NAME;
  }

  @Override
  public ActivityWrapper newActivityWrapper(InstalledLiveActivity liveActivity, ActivityFilesystem activityFilesystem,
      Configuration configuration, ActivityRuntime activityRuntime) {
    return new InteractiveSpacesNativeActivityWrapper(liveActivity, activityFilesystem, configuration, bundleLoader);
  }
}
