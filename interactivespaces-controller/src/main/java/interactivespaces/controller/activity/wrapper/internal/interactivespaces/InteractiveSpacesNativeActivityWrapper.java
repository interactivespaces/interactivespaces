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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.activity.wrapper.BaseActivityWrapper;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;

import java.io.File;

/**
 * A {@link ActivityWrapper} which works with an OSGi container.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesNativeActivityWrapper extends BaseActivityWrapper {

  /**
   * Configuration property giving the Java class.
   */
  public static final String CONFIGURATION_APPLICATION_JAVA_CLASS = "space.activity.java.class";

  /**
   * The installed live activity that this is the wrapper for.
   */
  private InstalledLiveActivity liveActivity;

  /**
   * The file system for the activity.
   */
  private ActivityFilesystem activityFilesystem;

  /**
   * The configuration for the live activity.
   */
  private Configuration configuration;

  /**
   * The bundle loader for the live activity bundles.
   */
  private LiveActivityBundleLoader bundleLoader;

  /**
   * The context we use for loading in a bundle.
   */
  private NativeInteractiveSpacesLiveActivityOsgiBundle bundle;

  /**
   * Construct a new wrapper.
   *
   * @param liveActivity
   *          the live activity to wrap
   * @param activityFilesystem
   *          the file system for the live activity
   * @param configuration
   *          the configuration for the activity
   * @param bundleLoader
   *          the bundle loader to be used for loading the live activity's bundle
   */
  public InteractiveSpacesNativeActivityWrapper(InstalledLiveActivity liveActivity,
      ActivityFilesystem activityFilesystem, Configuration configuration, LiveActivityBundleLoader bundleLoader) {
    this.liveActivity = liveActivity;
    this.activityFilesystem = activityFilesystem;
    this.configuration = configuration;
    this.bundleLoader = bundleLoader;
  }

  @Override
  public synchronized Activity newInstance() {
    File executable = getActivityExecutable(activityFilesystem, configuration);

    String className = configuration.getRequiredPropertyString(CONFIGURATION_APPLICATION_JAVA_CLASS);
    Class<?> activityClass =
        bundleLoader
            .getBundleClass(executable, liveActivity.getIdentifyingName(), liveActivity.getVersion(), className);

    try {
      return (Activity) activityClass.newInstance();
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not create activity class %s", className), e);
    }
  }

  /**
   * Get a file to the activity executable.
   *
   * @param activityFilesystem
   *          the activity's filesystem
   * @param configuration
   *          configuration for the activity
   *
   * @return File containing the executable.
   */
  private File getActivityExecutable(ActivityFilesystem activityFilesystem, Configuration configuration) {
    return new File(activityFilesystem.getInstallDirectory(),
        configuration.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE));
  }
}
