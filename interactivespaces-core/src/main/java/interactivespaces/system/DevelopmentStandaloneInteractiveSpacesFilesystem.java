/*
 * Copyright (C) 2015 Google Inc.
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

import java.io.File;

/**
 * A version of a {@link InteractiveSpacesFilesystem} that
 * supports an instance directory separate from the install directory.
 *
 * @author Max Rebuschatis
 */
public class DevelopmentStandaloneInteractiveSpacesFilesystem extends BasicInteractiveSpacesFilesystem {
  /**
   * The name of the Install directory type.
   */
  private static final String DIR_TYPE_NAME_INSTALL = "Install";

  /**
   * The name of the System Bootstrap directory type.
   */
  private static final String DIR_TYPE_NAME_SYSTEM_BOOTSTRAP = "System Bootstrap";

  /**
   * The name of the User Bootstrap directory type.
   */
  private static final String DIR_TYPE_NAME_USER_BOOTSTRAP = "User Bootstrap";

  /**
   * The name of the Library directory type.
   */
  private static final String DIR_TYPE_NAME_LIBRARY = "Library";

  /**
   * Create a new DevelopmentStandaloneInteractiveSpacesFilesystem.
   *
   * @param baseInstallDirectory
   *          the base directory where Interactive Spaces is installed
   * @param baseRuntimeDirectory
   *          the base directory where the activity should store runtime data
   */
  public DevelopmentStandaloneInteractiveSpacesFilesystem(File baseInstallDirectory, File baseRuntimeDirectory) {
    super(baseInstallDirectory, baseRuntimeDirectory);
  }

  /**
   * Throw a SimpleInteractiveSpacesException warning that a directory is unavailable for access.
   *
   * @param directoryType
   *          the directory type that was requested
   * @return the exception
   */
  private SimpleInteractiveSpacesException createDirectoryAccessException(String directoryType) {
    return new SimpleInteractiveSpacesException(
        String.format("Attempt to access the shared %s directory from an activity running in standalone mode",
            directoryType));
  }

  @Override
  public File getInstallDirectory() {
    throw createDirectoryAccessException(DIR_TYPE_NAME_INSTALL);
  }

  @Override
  public File getSystemBootstrapDirectory() {
    throw createDirectoryAccessException(DIR_TYPE_NAME_SYSTEM_BOOTSTRAP);
  }

  @Override
  public File getUserBootstrapDirectory() {
    throw createDirectoryAccessException(DIR_TYPE_NAME_USER_BOOTSTRAP);
  }

  @Override
  public File getLibraryDirectory() {
    throw createDirectoryAccessException(DIR_TYPE_NAME_LIBRARY);
  }

  @Override
  public File getLibraryDirectory(String subdir) {
    throw createDirectoryAccessException(DIR_TYPE_NAME_LIBRARY);
  }
}
