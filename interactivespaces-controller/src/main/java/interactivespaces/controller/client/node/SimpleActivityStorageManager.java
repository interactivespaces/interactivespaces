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
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * A basic implementation of an {@link ActivityStorageManager}.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivityStorageManager implements ActivityStorageManager {

  /**
   * Configuration property giving the location of the base activity
   * installation directory.
   */
  public static final String CONTROLLER_APPLICATION_INSTALLATION_DIRECTORY_PROPERTY =
      "interactivespaces.controller.activity.installation.directory";

  /**
   * The default folder for installing activities.
   */
  private static final String CONTROLLER_APPLICATIONS_INSTALLATION_DEFAULT = "controller/activities/installed";

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
  public SimpleActivityStorageManager(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    activityBaseDirectory =
        new File(spaceEnvironment.getFilesystem().getInstallDirectory(), systemConfiguration.getPropertyString(
            CONTROLLER_APPLICATION_INSTALLATION_DIRECTORY_PROPERTY, CONTROLLER_APPLICATIONS_INSTALLATION_DEFAULT));
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
  public ActivityFilesystem getActivityFilesystem(String uuid) {
    File baseLocation = getBaseActivityLocation(uuid);

    fileSupport.directoryExists(baseLocation, "Creating activity base location");

    ActivityFilesystem activityFilesystem = new SimpleActivityFilesystem(baseLocation);
    createFilesystemComponent(activityFilesystem.getInstallDirectory(), "Create activity installation directory");
    createFilesystemComponent(activityFilesystem.getLogDirectory(), "Creating activity log directory");
    createFilesystemComponent(activityFilesystem.getPermanentDataDirectory(),
        "Creating activity permanent data directory");
    createFilesystemComponent(activityFilesystem.getTempDataDirectory(), "Creating activity temporary data directory");

    return activityFilesystem;
  }

  /**
   * Create a component of the activity's file system.
   *
   * @param component
   *          the component to create
   * @param message
   *          the message saying what component is being created
   *
   * @throws InteractiveSpacesException
   *           if cannot create the component.
   */
  private void createFilesystemComponent(File component, String message) {
    fileSupport.directoryExists(component, message);
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
    cleanDataDirectory(uuid, SimpleActivityFilesystem.SUBDIRECTORY_DATA_TEMPORARY);
  }

  @Override
  public void cleanPermanentActivityDataDirectory(String uuid) {
    cleanDataDirectory(uuid, SimpleActivityFilesystem.SUBDIRECTORY_DATA_PERMANENT);
  }

  /**
   * Clean a particular data directory for an activity.
   *
   * @param uuid
   *          the UUID of the activity
   * @param dataDirectory
   *          the name of the data directory
   */
  public void cleanDataDirectory(String uuid, String dataDirectory) {
    File baseLocation = getBaseActivityLocation(uuid);
    if (baseLocation.exists()) {
      File tmpDirectory = new File(baseLocation, dataDirectory);
      if (tmpDirectory.exists()) {
        fileSupport.deleteDirectoryContents(tmpDirectory);
      }
    }
  }
}
