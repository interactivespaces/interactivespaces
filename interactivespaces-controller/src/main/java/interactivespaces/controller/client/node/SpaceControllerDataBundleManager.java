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

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.common.ResourceRepositoryUploadChannel;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.web.HttpClientHttpContentCopier;
import interactivespaces.util.web.HttpContentCopier;
import org.apache.commons.logging.Log;

import java.io.File;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * Controller-side manager for copying data bundles between master and
 * controller.
 *
 * @author Trevor Pering
 */
public class SpaceControllerDataBundleManager implements ControllerDataBundleManager {

  /**
   * Constant identifying the data bundle entry for the controller data.
   */
  public static final String CONTROLLER_DATA_BUNDLE_ENTRY = "controller";

  /**
   * Path prefix to use for activity data bundle sections.
   */
  public static final String ACTIVITY_DATA_BUNDLE_PREFIX = "activity/";

  /**
   * The content copier used for actually copying bundles.
   */
  private final HttpContentCopier contentCopier;

  /**
   * The FileZipper to use for managing zip files.
   */
  private final FileSupport fileSupport;

  /**
   * The space controller managed by this manager.
   */
  private SpaceControllerControl spaceController;

  /**
   * The activity storage manager.
   */
  private ActivityStorageManager activityStorageManager;

  /**
   * Create an instance using a default content copier.
   */
  public SpaceControllerDataBundleManager() {
    this(new HttpClientHttpContentCopier(), FileSupportImpl.INSTANCE);
  }

  /**
   * Create an instance using the supplied content copier and file zipper.
   *
   * @param contentCopier
   *          the content copier to use fot his manager
   * @param fileSupport
   *          interface for file support utilities
   */
  public SpaceControllerDataBundleManager(HttpContentCopier contentCopier, FileSupport fileSupport) {
    this.contentCopier = contentCopier;
    this.fileSupport = fileSupport;
  }

  @Override
  public void startup() {
    contentCopier.startup();
  }

  @Override
  public void shutdown() {
    contentCopier.shutdown();
  }

  @Override
  public void captureControllerDataBundle(String destinationUri) {
    File dataBundle = createDataBundle();
    try {
      Map<String, String> parameters = Maps.newHashMap();
      parameters.put("uuid", getControllerUuid());
      String channel = ResourceRepositoryUploadChannel.DATA_BUNDLE_UPLOAD.getChannelId();
      contentCopier.copyTo(destinationUri, dataBundle, channel, parameters);
    } finally {
      dataBundle.delete();
    }
  }

  @Override
  public void restoreControllerDataBundle(String sourceUri) {
    File dataBundle = generateDataBundleTempFile();
    try {
      contentCopier.copy(sourceUri, dataBundle);
      extractFullDataBundle(dataBundle);
    } finally {
      dataBundle.delete();
    }
  }

  /**
   * Create a data bundle for this controller.
   *
   * @return the created data bundle file
   */
  private File createDataBundle() {
    File dataBundle = generateDataBundleTempFile();
    ZipOutputStream zipOutputStream = null;
    try {
      zipOutputStream = fileSupport.createZipOutputStream(dataBundle);
      addDataBundleSection(zipOutputStream, CONTROLLER_DATA_BUNDLE_ENTRY, getControllerDataContentDirectory());
      for (InstalledLiveActivity activity : spaceController.getAllInstalledLiveActivities()) {
        addDataBundleSection(zipOutputStream, ACTIVITY_DATA_BUNDLE_PREFIX + activity.getUuid(),
            getActivityDataContentDirectory(activity));
      }
      zipOutputStream.close();
    } catch (Exception e) {
      dataBundle.delete();

      throw new InteractiveSpacesException(
          String.format("Error while zipping data bundle %s", dataBundle.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(zipOutputStream);
    }

    return dataBundle;
  }

  /**
   * Add a data bundle section to the supplied output stream.
   *
   * @param zipOutputStream
   *          stream to which to add the component data bundle
   * @param subsection
   *          subsection in the zip stream to add it to
   * @param contentDirectory
   *          the data bundle directory to add
   */
  private void addDataBundleSection(ZipOutputStream zipOutputStream, String subsection, File contentDirectory) {
    if (contentDirectory.exists()) {
      getLog().info(String.format("Adding data bundle content source %s from directory %s",
          subsection, contentDirectory.getAbsolutePath()));
      File relFile = new File(".");
      fileSupport.addFileToZipStream(zipOutputStream, contentDirectory, relFile, subsection);
    } else {
      getLog().warn(String.format("Skipping non-existent data directory %s", contentDirectory.getAbsolutePath()));
    }
  }

  /**
   * Extract the given data bundle file for this controller, including both controller and activity portions.
   *
   * @param dataBundle
   *          data bundle file to extract
   */
  private void extractFullDataBundle(File dataBundle) {
    File incomingDirectory = generateDataBundleTempFile();

    try {
      fileSupport.unzip(dataBundle, incomingDirectory);

      extractDataBundleSection(incomingDirectory, CONTROLLER_DATA_BUNDLE_ENTRY, getControllerDataContentDirectory());

      for (InstalledLiveActivity activity : spaceController.getAllInstalledLiveActivities()) {
        extractDataBundleSection(incomingDirectory, ACTIVITY_DATA_BUNDLE_PREFIX + activity.getUuid(),
            getActivityDataContentDirectory(activity));
      }

    } catch (Exception e) {
      throw new InteractiveSpacesException("Extracting data bundle " + dataBundle.getName(), e);
    } finally {
      fileSupport.delete(incomingDirectory);
    }
  }

  /**
   * Extract a data bundle section from the compete bundle.
   *
   * @param incomingDirectory
   *          directory containing the extracted master data bundle
   * @param subsection
   *          subsection to extract
   * @param targetDirectory
   *          target location for the extracted bundle
   */
  private void extractDataBundleSection(File incomingDirectory, String subsection, File targetDirectory) {
    File sourceDirectory = new File(incomingDirectory, subsection);
    File backupDirectory = generateDataBundleTempFile();

    try {
      if (targetDirectory.exists() && !fileSupport.rename(targetDirectory, backupDirectory)) {
        throw new SimpleInteractiveSpacesException("Could not move existing directory to backup");
      }
      if (!sourceDirectory.exists()) {
        getLog().warn(String.format("No source directory found for %s, skipping", sourceDirectory));
      } else {
        getLog().info(String.format("Extracting source %s to %s, backup %s",
            sourceDirectory, targetDirectory, backupDirectory));
        if (!fileSupport.rename(sourceDirectory, targetDirectory)) {
          throw new SimpleInteractiveSpacesException("Could not move incoming directory to target");
        }
      }
      fileSupport.delete(backupDirectory);
    } catch (Exception e) {
      if (backupDirectory.exists() && !targetDirectory.exists()) {
        backupDirectory.renameTo(targetDirectory);
      }
      fileSupport.delete(sourceDirectory);
      throw new InteractiveSpacesException(
          "Renaming content directories for " + targetDirectory.getAbsolutePath(), e);
    }
  }

  @Override
  public void setSpaceController(SpaceControllerControl spaceController) {
    this.spaceController = spaceController;
  }

  @Override
  public void setActivityStorageManager(ActivityStorageManager activityStorageManager) {
    this.activityStorageManager = activityStorageManager;
  }

  /**
   * Get the assigned controller UUID.
   *
   * @return uuid of the associated controller
   */
  private String getControllerUuid() {
    return spaceController.getControllerInfo().getUuid();
  }

  /**
   * Get a temporary file location, guaranteed not to exist.
   *
   * @return temp file for this bundle
   */
  private File generateDataBundleTempFile() {
    File tempRoot = spaceController.getSpaceEnvironment().getFilesystem().getTempDirectory("dataBundle");
    fileSupport.directoryExists(tempRoot);
    File tempFile;
    do {
      String filename = String.format("temp-%x", Math.round(Math.random() * Integer.MAX_VALUE));
      tempFile = new File(tempRoot, filename);
    } while (tempFile.exists());
    return tempFile;
  }

  /**
   * Get the content directory for this controller.
   *
   * @return the content directory for the controller
   */
  private File getControllerDataContentDirectory() {
    return spaceController.getSpaceEnvironment().getFilesystem().getDataDirectory();
  }

  /**
   * Get the content directory for an activity.
   *
   * @param activity
   *          activity to query
   *
   * @return the content directory for the activity
   */
  private File getActivityDataContentDirectory(InstalledLiveActivity activity) {
    return activityStorageManager.getActivityFilesystem(activity.getUuid()).getPermanentDataDirectory();
  }

  /**
   * @return logger to use
   */
  private Log getLog() {
    return spaceController.getSpaceEnvironment().getLog();
  }
}
