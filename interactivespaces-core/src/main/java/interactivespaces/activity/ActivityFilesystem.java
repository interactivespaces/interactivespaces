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

package interactivespaces.activity;

import java.io.File;

/**
 * An installed activity's file system.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityFilesystem {

	/**
	 * Get the directory where the contents of the deployed activity are
	 * installed.
	 * 
	 * @return The directory where the activity is installed.
	 */
	File getInstallDirectory();

	/**
	 * Get a file in the installation directory.
	 * 
	 * @param relative
	 *            the pathname of the file relative to the activity install
	 *            directory
	 * 
	 * @return the file
	 */
	File getInstallFile(String relative);

	/**
	 * Get the directory where the activity can permanently store data.
	 * 
	 * <p>
	 * This directory will be removed if the activity is uninstalled, but
	 * otherwise will stay around.
	 * 
	 * @return The directory where the activity can permanently store data.
	 */
	File getPermanentDataDirectory();

	/**
	 * Get a file in the permanent data directory.
	 * 
	 * <p>
	 * This file will be removed if the activity is uninstalled, but
	 * otherwise will stay around.
	 * 
	 * @param relative
	 *            path relative to the permanent data directory for the file
	 * 
	 * @return the file
	 */
	File getPermanentDataFile(String relative);

	/**
	 * Get the directory where the activity can temporarily store data.
	 * 
	 * <p>
	 * This directory is temporary and could be removed at any time. The
	 * activity should assume it will not disappear while the activity is
	 * running.
	 * 
	 * @return The directory where the activity can temporarily store data.
	 */
	File getTempDataDirectory();

	/**
	 * Get a file for temporarily storing data.
	 * 
	 * <p>
	 * This file is temporary and could be removed at any time. The
	 * activity should assume it will not disappear while the activity is
	 * running.
	 * 
	 * @param relative
	 *            path relative to the temporary data directory for the file
	 * 
	 * @return The directory where the activity can temporarily store data.
	 */
	File getTempDataFile(String relative);

	/**
	 * Get the directory where the activity stores logs.
	 * 
	 * @return The directory where the activity stores logs.
	 */
	File getLogDirectory();
}
