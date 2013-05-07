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

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.util.io.Files;

import java.io.File;

/**
 * A basic implementation of a {@link ActivityFilesystem}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleActivityFilesystem implements ActivityFilesystem {
	/**
	 * The subdirectory off of the base install directory for the activity
	 * install.
	 */
	public static final String SUBDIRECTORY_INSTALL = "install";

	/**
	 * The subdirectory off of the base install directory where the activity's
	 * logs are stored.
	 */
	public static final String SUBDIRECTORY_LOG = "log";

	/**
	 * The subdirectory off of the base install directory where the activity's
	 * permanent data is stored.
	 */
	public static final String SUBDIRECTORY_DATA_PERMANENT = "data";

	/**
	 * The subdirectory off of the base install directory where the activity's
	 * temporary data is stored.
	 */
	public static final String SUBDIRECTORY_DATA_TEMPORARY = "tmp";

	/**
	 * The base installation directory for the activity
	 */
	private File baseInstallationDirectory;

	/**
	 * Where the activity is installed.
	 */
	private File installDirectory;

	/**
	 * Where log files are stored.
	 */
	private File logDirectory;

	/**
	 * Where permanent data files can be stored.
	 */
	private File permanentDataDirectory;

	/**
	 * Where temporary files can be stored.
	 */
	private File tempDataDirectory;

	public SimpleActivityFilesystem(File baseInstallationDirectory) {
		this.baseInstallationDirectory = baseInstallationDirectory;
		
		installDirectory = new File(baseInstallationDirectory, SUBDIRECTORY_INSTALL);
		logDirectory = new File(baseInstallationDirectory, SUBDIRECTORY_LOG);
		permanentDataDirectory = new File(baseInstallationDirectory, SUBDIRECTORY_DATA_PERMANENT);
		tempDataDirectory = new File(baseInstallationDirectory, SUBDIRECTORY_DATA_TEMPORARY);
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
		Files.deleteDirectoryContents(getPermanentDataDirectory());
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
		Files.deleteDirectoryContents(getTempDataDirectory());
	}
}
