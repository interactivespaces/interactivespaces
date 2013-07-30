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

import java.io.File;

/**
 * File system for a full Interactive Spaces installation.
 *
 * @author Keith M. Hughes
 */
public interface InteractiveSpacesFilesystem {

  /**
   * Get the installation directory where Interactive Spaces was installed.
   *
   * @return the installation directory
   */
  File getInstallDirectory();

  /**
   * Get the system bootstrap directory for Interactive Spaces.
   *
   * @return the bootstrap directory
   */
  File getBootstrapDirectory();

  /**
   * Get the system log directory for Interactive Spaces.
   *
   * @return the log directory
   */
  File getLogsDirectory();

  /**
   * Get the EInteractive Spaces-wide directory used for writing temporary
   * files.
   *
   * @return the temporary directory
   */
  File getTempDirectory();

/**
	 * Get a temp directory relative to {@link #getTempDirectory()).
	 *
	 * <p>
	 * If the directory doesn't exist, it will be created. If it does exist, it will be checked if it is writeable.
	 *
	 * @param subdir
	 * 		the relative portion of the filename
	 *
	 * @return the requested subdirectory in the temp directory
	 */
  File getTempDirectory(String subdir);

  /**
   * Get the root library directory. This contains libraries for the various
   * supported scripting languages.
   *
   * @return the root library directory
   */
  File getLibraryDirectory();

/**
	 * Get a library directory relative to {@link #getLibraryDirectory()).
	 *
	 * <p>
	 * If the directory doesn't exist, it will be created. If it does exist, it will be checked if it is readable.
	 *
	 * @param subdir
	 * 		the relative portion of the filename
	 * @return
	 */
  File getLibraryDirectory(String subdir);

  /**
   * Get the root Interactive Spaces-wide data directory. This is for activities
   * on the controller sharing data.
   *
   * @return the root data directory
   */
  File getDataDirectory();

/**
	 * Get a directory relative to {@link #getDataDirectory()).
	 *
	 * <p>
	 * If the directory doesn't exist, it will be created. If it does exist, it will be checked if it is writeable.
	 *
	 * @param subdir
	 * 		the relative portion of the filename
	 *
	 * @return the requested subdirectory in the data directory
	 */
  File getDataDirectory(String subdir);
}
