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

import interactivespaces.InteractiveSpacesException;

import java.io.File;

/**
 * The basic {@link InteractiveSpacesFilesystem}.
 * 
 * @author Keith M. Hughes
 */
public class BasicInteractiveSpacesFilesystem implements
		InteractiveSpacesFilesystem {

	/**
	 * The base Interactive Spaces install directory. This is the install for a
	 * master or a controller.
	 */
	private File baseInstallDirectory;

	/**
	 * The temporary directory to be used by the install.
	 */
	private File tempDirectory;

	/**
	 * The data directory to be used by the install.
	 */
	private File dataDirectory;

	/**
	 * Where libraries will be stored.
	 */
	private File libraryDirectory;

	/**
	 * Where the bootstrap is.
	 */
	private File bootstrapDirectory;

	/**
	 * Where the system logs are kept.
	 */
	private File logsDirectory;

	/**
	 * 
	 * @param baseInstallDir
	 * 		The base directory where Interactive Spaces is installed.
	 */
	public BasicInteractiveSpacesFilesystem(File baseInstallDir) {
		/**
		 * Need to check this is always right for all OSes.
		 */
		this.baseInstallDirectory = baseInstallDir;
		String absolutePath = baseInstallDirectory.getAbsolutePath();
		if (absolutePath.endsWith(".")) {
			baseInstallDirectory = baseInstallDirectory.getParentFile();
		}

		tempDirectory = new File(baseInstallDirectory, "tmp");
		logsDirectory = new File(baseInstallDirectory, "logs");
		bootstrapDirectory = new File(baseInstallDirectory, "bootstrap");
		libraryDirectory = new File(baseInstallDirectory, "lib");
		dataDirectory = new File(baseInstallDirectory, "data");
	}

	@Override
	public File getInstallDirectory() {
		return baseInstallDirectory;
	}

	@Override
	public File getBootstrapDirectory() {
		return bootstrapDirectory;
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
		checkWriteableDirectory(dataDir, "data");

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

	/**
	 * Start up the file system.
	 * 
	 * <p>
	 * This will check if various directories are in fact directories, ones
	 * which need to be writable will be checked that they are writeable, etc.
	 */
	public void startup() {
		checkReadableDirectory(bootstrapDirectory, "bootstrap");
		checkReadableDirectory(logsDirectory, "logs");
		checkReadableDirectory(libraryDirectory, "lib");
		checkWriteableDirectory(dataDirectory, "data");
		checkWriteableDirectory(tempDirectory, "temp");
	}

	/**
	 * Check to see if have writable directory. It will be created if it doesn't
	 * exist.
	 * 
	 * @param dir
	 *            the directory to create
	 * @param type
	 *            the type of directory
	 */
	private void checkWriteableDirectory(File dir, String type) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				if (!dir.canWrite()) {
					throw new InteractiveSpacesException(String.format(
							"The %s directory %s is not writeable", type,
							dir.getAbsolutePath()));
				}
				if (!dir.canRead()) {
					throw new InteractiveSpacesException(String.format(
							"The %s directory %s is not readable", type,
							dir.getAbsolutePath()));
				}
			} else {
				throw new InteractiveSpacesException(String.format(
						"The %s directory %s is not a directory", type,
						dir.getAbsolutePath()));
			}
		} else {
			if (!dir.mkdirs()) {
				throw new InteractiveSpacesException(String.format(
						"Unable to create the %s directory %s", type,
						dir.getAbsolutePath()));
			}
		}
	}

	/**
	 * Check to see if have writable directory. It will be created if it doesn't
	 * exist.
	 * 
	 * @param dir
	 *            the directory to create
	 * @param type
	 *            the type of directory
	 */
	private void checkReadableDirectory(File dir, String type) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				if (!dir.canRead()) {
					throw new InteractiveSpacesException(String.format(
							"The %s directory %s is not readable", type,
							dir.getAbsolutePath()));
				}
			} else {
				throw new InteractiveSpacesException(String.format(
						"The %s directory %s is not a directory", type,
						dir.getAbsolutePath()));
			}
		} else {
			throw new InteractiveSpacesException(String.format(
					"The %s directory %s does not exist", type,
					dir.getAbsolutePath()));
		}
	}
}
