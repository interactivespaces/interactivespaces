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
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.Version;

import java.io.File;

/**
 * An {@link ActivityWrapperFactory} base class with some potential useful methods.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivityWrapperFactory implements ActivityWrapperFactory {

  /**
   * Version of the factory.
   */
  private final Version version;

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
    this.version = version;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  /**
   * Get the activity's executable file.
   *
   * @param activity
   *          activity containing the executable
   * @param activityFilesystem
   *          the activity's filesystem
   * @param configuration
   *          configuration for the activity
   *
   * @return file containing the executable
   */
  public File getActivityExecutable(InstalledLiveActivity activity, ActivityFilesystem activityFilesystem,
      Configuration configuration) {
    return new File(activityFilesystem.getInstallDirectory(),
        configuration.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE));
  }

  /**
   * Get a file to the activity executable with a given extension.
   *
   * @param activity
   *          live activity containing the executable
   * @param activityFilesystem
   *          the activity's filesystem
   * @param configuration
   *          configuration for the activity
   * @param extension
   *          a file extension to be placed at the end of the name (should not contain dot)
   *
   * @return File containing the executable.
   */
  public File getActivityExecutable(InstalledLiveActivity activity, ActivityFilesystem activityFilesystem,
      Configuration configuration, String extension) {
    return new File(activityFilesystem.getInstallDirectory(),
        configuration.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE) + "."
            + extension);
  }
}
