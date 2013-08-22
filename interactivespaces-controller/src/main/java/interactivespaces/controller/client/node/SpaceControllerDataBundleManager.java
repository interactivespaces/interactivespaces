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
import interactivespaces.InteractiveSpacesException;
import interactivespaces.common.ResourceRepositoryUploadChannel;
import interactivespaces.controller.SpaceController;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.web.HttpClientHttpContentCopier;
import interactivespaces.util.web.HttpContentCopier;

import java.io.File;
import java.util.Map;

/**
 * Controller-side manager for copying data bundles between master and controller.
 *
 * @author Trevor Pering
 */
public class SpaceControllerDataBundleManager implements ControllerDataBundleManager {

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
  private SpaceController spaceController;

  /**
   * Create an instance using a default content copier.
   */
  public SpaceControllerDataBundleManager() {
    this(new HttpClientHttpContentCopier(), new FileSupportImpl());
  }

  /**
   * Create an instance using the supplied content copier and file zipper.
   *
   * @param contentCopier
   *            the content copier to use fot his manager
   * @param fileSupport
   *            interface for file support utilities
   */
  public SpaceControllerDataBundleManager(HttpContentCopier contentCopier,
      FileSupport fileSupport) {
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
      extractDataBundle(dataBundle);
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
    File contentDirectory = getControllerDataContentDirectory();
    try {
      fileSupport.zip(dataBundle, contentDirectory);
    } catch (Exception e) {
      dataBundle.delete();
      throw new InteractiveSpacesException("While zipping " +
          contentDirectory.getAbsolutePath(), e);
    }
    return dataBundle;
  }

  /**
   * Extract the given data bundle file for this controller.
   *
   * @param dataBundle
   *           data bundle file to extract
   */
  private void extractDataBundle(File dataBundle) {
    File incomingContentDirectory = generateDataBundleTempFile();
    File outgoingContentDirectory = generateDataBundleTempFile();
    File dataContentDirectory = getControllerDataContentDirectory();

    // This function executes in three stages, with three separate try/catch blocks in an
    // attempt to cleanly handle error conditions depending on when they happen.

    try {
      fileSupport.unzip(dataBundle, incomingContentDirectory);
    } catch (Exception e) {
      fileSupport.delete(incomingContentDirectory);
      throw new InteractiveSpacesException("Extracting data bundle " + dataBundle.getName(), e);
    }

    try {
      dataContentDirectory.renameTo(outgoingContentDirectory);
      incomingContentDirectory.renameTo(dataContentDirectory);
    } catch (Exception e) {
      if (outgoingContentDirectory.exists() && !dataContentDirectory.exists()) {
        outgoingContentDirectory.renameTo(dataContentDirectory);
      }
      fileSupport.delete(incomingContentDirectory);
      throw new InteractiveSpacesException("Renaming content directories for " +
              dataContentDirectory.getAbsolutePath(), e);
    }

    try {
      fileSupport.delete(outgoingContentDirectory);
    } catch (Exception e) {
      throw new InteractiveSpacesException("Deleting old content directory " +
          outgoingContentDirectory.getAbsolutePath(), e);
    }
  }

  @Override
  public void setSpaceController(SpaceController spaceController) {
    this.spaceController = spaceController;
  }

  /**
   * Get the assigned controller UUID.
   *
   * @return
   *    uuid of the associated controller
   */
  private String getControllerUuid() {
    return spaceController.getControllerInfo().getUuid();
  }

  /**
   * Get a temporary file location, guaranteed not to exist.
   *
   * @return
   *           temp file for this bundle
   */
  private File generateDataBundleTempFile() {
    File tempFile;
    do {
      String filename = String.format("temp-%x", Math.round(Math.random() * Integer.MAX_VALUE));
      tempFile = new File(
          spaceController.getSpaceEnvironment().getFilesystem().getTempDirectory("dataBundle"),
          filename);
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
}
