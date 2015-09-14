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

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;

/**
 * A basic implementation of a {@link ActivityFilesystem}.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityFilesystem implements InternalLiveActivityFilesystem {

  /**
   * The subdirectory off of the base install directory for the activity install.
   */
  public static final String SUBDIRECTORY_INSTALL = "install";

  /**
   * The subdirectory off of the base install directory where the activity's logs are stored.
   */
  public static final String SUBDIRECTORY_LOG = "log";

  /**
   * The subdirectory off of the base install directory where the activity's permanent data is stored.
   */
  public static final String SUBDIRECTORY_DATA_PERMANENT = "data";

  /**
   * The subdirectory off of the base install directory where the activity's temporary data is stored.
   */
  public static final String SUBDIRECTORY_DATA_TEMPORARY = "tmp";

  /**
   * The subdirectory off of the base install directory where the activity's internal Interactive Spaces data is stored.
   */
  public static final String SUBDIRECTORY_INTERNAL = "internal";

  /**
   * Where the activity is installed.
   */
  private final File installDirectory;

  /**
   * Where log files are stored.
   */
  private final File logDirectory;

  /**
   * Where permanent data files can be stored.
   */
  private final File permanentDataDirectory;

  /**
   * Where temporary files can be stored.
   */
  private final File tempDataDirectory;

  /**
   * Where internal Interactive Spaces files can be stored.
   */
  private final File internalDirectory;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a filesystem.
   *
   * <p>
   * The entire filesystem will be rooted at the base install directory.
   *
   * @param baseInstallationDirectory
   *          the base install directory of the filesystem
   */
  public SimpleLiveActivityFilesystem(File baseInstallationDirectory) {
    this(new File(baseInstallationDirectory, SUBDIRECTORY_INSTALL), new File(baseInstallationDirectory,
        SUBDIRECTORY_LOG), new File(baseInstallationDirectory, SUBDIRECTORY_DATA_PERMANENT), new File(
        baseInstallationDirectory, SUBDIRECTORY_DATA_TEMPORARY), new File(baseInstallationDirectory,
        SUBDIRECTORY_INTERNAL));
  }

  /**
   * Construct a new filesystem.
   *
   * @param installDirectory
   *          the directory where the activity is installed
   * @param logDirectory
   *          the log directory for the activity
   * @param permanentDataDirectory
   *          the permanent data directory for the activity
   * @param tempDataDirectory
   *          the temporary data directory for the activity
   * @param internalDirectory
   *          the internal data directory for the activity
   */
  public SimpleLiveActivityFilesystem(File installDirectory, File logDirectory, File permanentDataDirectory,
      File tempDataDirectory, File internalDirectory) {
    this.installDirectory = installDirectory;
    this.logDirectory = logDirectory;
    this.permanentDataDirectory = permanentDataDirectory;
    this.tempDataDirectory = tempDataDirectory;
    this.internalDirectory = internalDirectory;
  }

  @Override
  public File getInstallDirectory() {
    return installDirectory;
  }

  @Override
  public File getInstallFile(String relative) {
    return new File(installDirectory, relative);
  }

  @Override
  public File getLogDirectory() {
    return logDirectory;
  }

  @Override
  public File getPermanentDataDirectory() {
    return permanentDataDirectory;
  }

  @Override
  public File getPermanentDataFile(String relative) {
    return new File(permanentDataDirectory, relative);
  }

  @Override
  public void cleanPermanentDataDirectory() {
    fileSupport.deleteDirectoryContents(getPermanentDataDirectory());
  }

  @Override
  public File getTempDataDirectory() {
    return tempDataDirectory;
  }

  @Override
  public File getTempDataFile(String relative) {
    return new File(tempDataDirectory, relative);
  }

  @Override
  public void cleanTempDataDirectory() {
    fileSupport.deleteDirectoryContents(getTempDataDirectory());
  }

  @Override
  public File getInternalDirectory() {
    return internalDirectory;
  }

  @Override
  public File getInternalFile(String relative) {
    return new File(internalDirectory, relative);
  }

  /**
   * Make sure that all directories exist. If not, they will be created.
   */
  public void ensureDirectories() {
    fileSupport.directoryExists(installDirectory, "Create activity installation directory");
    fileSupport.directoryExists(logDirectory, "Creating activity log directory");
    fileSupport.directoryExists(permanentDataDirectory, "Creating activity permanent data directory");
    fileSupport.directoryExists(tempDataDirectory, "Creating activity temporary data directory");
    fileSupport.directoryExists(internalDirectory, "Creating activity internal Interactive Spaces directory");
  }
}
