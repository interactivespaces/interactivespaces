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

package interactivespaces.system;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.resource.ManagedResource;

import java.io.File;

/**
 * A base class for InteractiveSpacesFilesystem subclasses that validates directories.
 *
 * @author Keith M. Hughes
 * @author Max Rebuschatis
 */
public class BasicInteractiveSpacesFilesystem implements InteractiveSpacesFilesystem, ManagedResource {
  /**
   * The container subdirectory for data.
   */
  public static final String DIRECTORY_DATA = "data";

  /**
   * The container subdirectory for libraries.
   */
  public static final String DIRECTORY_LIB = "lib";

  /**
   * The container subdirectory for core bootstrap.
   */
  public static final String DIRECTORY_SYSTEM_BOOTSTRAP = "bootstrap";

  /**
   * The container subdirectory for user library bootstrap.
   */
  public static final String DIRECTORY_USER_BOOTSTRAP = "startup";

  /**
   * The container subdirectory for logs.
   */
  public static final String DIRECTORY_LOGS = "logs";

  /**
   * The container subdirectory for tmp data.
   */
  public static final String DIRECTORY_TMP = "tmp";

  /**
   * The base Interactive Spaces install directory. This is the install for a
   * master or a controller.
   */
  private final File baseInstallDirectory;

  /**
   * The temporary directory to be used by the install.
   */
  private final File tempDirectory;

  /**
   * The data directory to be used by the install.
   */
  private final File dataDirectory;

  /**
   * Where libraries will be stored.
   */
  private final File libraryDirectory;

  /**
   * Where the system bootstrap is.
   */
  private final File systemBootstrapDirectory;

  /**
   * Where the user library bootstrap is.
   */
  private final File userBootstrapDirectory;

  /**
   * Where the system logs are kept.
   */
  private final File logsDirectory;

  /**
   * Create a new BasicInteractiveSpacesFilesystem.
   *
   * @param baseInstallDirectory
   *          the base directory where Interactive Spaces is installed
   * @param baseRuntimeDirectory
   *          the directory where runtime data should be stored
   */
  protected BasicInteractiveSpacesFilesystem(File baseInstallDirectory, File baseRuntimeDirectory) {
    this.baseInstallDirectory = baseInstallDirectory;
    String absolutePath = baseInstallDirectory.getAbsolutePath();
    if (absolutePath.endsWith(".")) {
      baseInstallDirectory = baseInstallDirectory.getParentFile();
    }

    if (baseRuntimeDirectory == null) {
      baseRuntimeDirectory = baseInstallDirectory;
    }

    logsDirectory = new File(baseRuntimeDirectory, DIRECTORY_LOGS);
    systemBootstrapDirectory = new File(baseInstallDirectory, DIRECTORY_SYSTEM_BOOTSTRAP);
    userBootstrapDirectory = new File(baseInstallDirectory, DIRECTORY_USER_BOOTSTRAP);
    libraryDirectory = new File(baseInstallDirectory, DIRECTORY_LIB);
    dataDirectory = new File(baseRuntimeDirectory, DIRECTORY_DATA);
    tempDirectory = new File(baseRuntimeDirectory, DIRECTORY_TMP);
  }

  /**
   * Create a new BasicInteractiveSpacesFilesystem.
   *
   * @param baseInstallDirectory
   *          the base directory where Interactive Spaces is installed.
   */
  public BasicInteractiveSpacesFilesystem(File baseInstallDirectory) {
    this(baseInstallDirectory, null);
  }

  @Override
  public File getInstallDirectory() {
    return baseInstallDirectory;
  }

  @Override
  public File getSystemBootstrapDirectory() {
    return systemBootstrapDirectory;
  }

  @Override
  public File getUserBootstrapDirectory() {
    return userBootstrapDirectory;
  }

  @Override
  public File getLogsDirectory() {
    return logsDirectory;
  }

  @Override
  public File getTempDirectory() {
    return tempDirectory;
  }

  @Override
  public File getTempDirectory(String subdir) {
    File tmpDir = new File(tempDirectory, subdir);
    checkWriteableDirectory(tmpDir, "temporary");

    return tmpDir;
  }

  @Override
  public File getDataDirectory() {
    return dataDirectory;
  }

  @Override
  public File getDataDirectory(String subdir) {
    File dataDir = new File(dataDirectory, subdir);
    checkWriteableDirectory(dataDir, DIRECTORY_DATA);

    return dataDir;
  }

  @Override
  public File getLibraryDirectory() {
    return libraryDirectory;
  }

  @Override
  public File getLibraryDirectory(String subdir) {
    File tmpDir = new File(libraryDirectory, subdir);
    checkReadableDirectory(tmpDir, "library");

    return tmpDir;
  }

  @Override
  public void startup() {
    checkWriteableDirectory(systemBootstrapDirectory, DIRECTORY_SYSTEM_BOOTSTRAP);
    checkWriteableDirectory(userBootstrapDirectory, DIRECTORY_USER_BOOTSTRAP);
    checkReadableDirectory(logsDirectory, DIRECTORY_LOGS);
    checkReadableDirectory(libraryDirectory, DIRECTORY_LIB);
    checkWriteableDirectory(dataDirectory, DIRECTORY_DATA);
    checkWriteableDirectory(tempDirectory, DIRECTORY_TMP);
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  /**
   * Check to see if have writable directory. It will be created if it doesn't
   * exist.
   *
   * @param dir
   *          the directory to create
   * @param type
   *          the type of directory
   */
  private void checkWriteableDirectory(File dir, String type) {
    if (dir.exists()) {
      if (dir.isDirectory()) {
        if (!dir.canWrite()) {
          throw new SimpleInteractiveSpacesException(String.format("The %s directory %s is not writeable", type,
              dir.getAbsolutePath()));
        }
        if (!dir.canRead()) {
          throw new SimpleInteractiveSpacesException(String.format("The %s directory %s is not readable", type,
              dir.getAbsolutePath()));
        }
      } else {
        throw new SimpleInteractiveSpacesException(String.format("The %s directory %s is not a directory", type,
            dir.getAbsolutePath()));
      }
    } else {
      if (!dir.mkdirs()) {
        throw new SimpleInteractiveSpacesException(String.format("Unable to create the %s directory %s", type,
            dir.getAbsolutePath()));
      }
    }
  }

  /**
   * Check to see if have writable directory. It will be created if it doesn't
   * exist.
   *
   * @param dir
   *          the directory to create
   * @param type
   *          the type of directory
   */
  private void checkReadableDirectory(File dir, String type) {
    if (dir.exists()) {
      if (dir.isDirectory()) {
        if (!dir.canRead()) {
          throw new SimpleInteractiveSpacesException(String.format("The %s directory %s is not readable", type,
              dir.getAbsolutePath()));
        }
      } else {
        throw new SimpleInteractiveSpacesException(String.format("The %s directory %s is not a directory", type,
            dir.getAbsolutePath()));
      }
    } else {
      throw new SimpleInteractiveSpacesException(String.format("The %s directory %s does not exist", type,
          dir.getAbsolutePath()));
    }
  }
}
