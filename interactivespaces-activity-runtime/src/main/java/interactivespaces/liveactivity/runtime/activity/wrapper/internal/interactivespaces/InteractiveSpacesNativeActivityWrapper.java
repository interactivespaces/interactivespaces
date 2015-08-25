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

package interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.activity.wrapper.BaseActivityWrapper;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;

import org.osgi.framework.Bundle;

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
   * The bundle for the activity.
   */
  private Bundle activityBundle;

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
    File executableFile = getActivityExecutable(activityFilesystem, configuration);
    activityBundle = bundleLoader.loadLiveActivityBundle(liveActivity, executableFile);

    String className = configuration.getRequiredPropertyString(CONFIGURATION_APPLICATION_JAVA_CLASS);

    try {
      Class<?> activityClass = activityBundle.loadClass(className);

      return (Activity) activityClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw SimpleInteractiveSpacesException.newFormattedException("Could not find the activity Java class %s",
          className);
    } catch (IllegalAccessException e) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Activity class %s must have a public no-argument constructor", className), e);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not create activity class %s", className), e);
    }
  }

  @Override
  public synchronized void done() {
    if (activityBundle != null) {
      bundleLoader.dismissLiveActivityBundle(liveActivity);
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
