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

import interactivespaces.configuration.Configuration;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * A basic implementation of an {@link LiveActivityStorageManager}.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityStorageManager implements LiveActivityStorageManager {

  /**
   * Configuration property giving the location of the base activity
   * installation directory.
   */
  public static final String CONFIGURATION_CONTROLLER_APPLICATION_INSTALLATION_DIRECTORY =
      "interactivespaces.controller.activity.installation.directory";

  /**
   * The default folder for installing activities.
   */
  public static final String CONFIGURATION_DEFAULT_CONTROLLER_APPLICATIONS_INSTALLATION_DIRECTORY =
      "controller/activities/installed";

  /**
   * Base directory where activities are stored.
   */
  private File activityBaseDirectory;

  /**
   * The Interactive Spaces environment.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct an activity storage manager.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public SimpleLiveActivityStorageManager(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    activityBaseDirectory =
        new File(spaceEnvironment.getFilesystem().getInstallDirectory(), systemConfiguration.getPropertyString(
            CONFIGURATION_CONTROLLER_APPLICATION_INSTALLATION_DIRECTORY,
            CONFIGURATION_DEFAULT_CONTROLLER_APPLICATIONS_INSTALLATION_DIRECTORY));
  }

  @Override
  public void shutdown() {
    // Nothing to do.
  }

  @Override
  public List<String> getAllInstalledActivityUuids() {
    List<String> results = Lists.newArrayList();
    File[] installedActivityDirectories = activityBaseDirectory.listFiles();
    if (installedActivityDirectories != null) {
      for (File file : installedActivityDirectories) {
        if (file.isDirectory()) {
          results.add(file.getName());
        }
      }
    }

    return results;
  }

  @Override
  public File getBaseActivityLocation(String uuid) {
    return new File(activityBaseDirectory, uuid);
  }

  @Override
  public InternalLiveActivityFilesystem getActivityFilesystem(String uuid) {
    File baseLocation = getBaseActivityLocation(uuid);

    fileSupport.directoryExists(baseLocation, "Creating activity base location");

    SimpleLiveActivityFilesystem activityFilesystem = new SimpleLiveActivityFilesystem(baseLocation);
    activityFilesystem.ensureDirectories();

    return activityFilesystem;
  }

  @Override
  public void removeActivityLocation(String uuid) {
    File baseLocation = getBaseActivityLocation(uuid);

    if (baseLocation.exists()) {
      fileSupport.delete(baseLocation);
    }
  }

  @Override
  public void cleanTmpActivityDataDirectory(String uuid) {
    cleanDataDirectory(uuid, SimpleLiveActivityFilesystem.SUBDIRECTORY_DATA_TEMPORARY);
  }

  @Override
  public void cleanPermanentActivityDataDirectory(String uuid) {
    cleanDataDirectory(uuid, SimpleLiveActivityFilesystem.SUBDIRECTORY_DATA_PERMANENT);
  }

  /**
   * Clean a particular data directory for an activity.
   *
   * @param uuid
   *          the UUID of the activity
   * @param dataDirectory
   *          the name of the data directory
   */
  private void cleanDataDirectory(String uuid, String dataDirectory) {
    File baseLocation = getBaseActivityLocation(uuid);
    if (baseLocation.exists()) {
      File tmpDirectory = new File(baseLocation, dataDirectory);
      if (tmpDirectory.exists()) {
        fileSupport.deleteDirectoryContents(tmpDirectory);
      }
    }
  }
}
