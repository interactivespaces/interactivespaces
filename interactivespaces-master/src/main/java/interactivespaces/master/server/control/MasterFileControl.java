/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.master.server.control;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.io.directorywatcher.DirectoryWatcher;
import interactivespaces.util.io.directorywatcher.DirectoryWatcherListener;
import interactivespaces.util.io.directorywatcher.SimpleDirectoryWatcher;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Handle control of the master by using the filesystem.
 * 
 * <p>
 * These command work by looking at the {@link #FOLDER_RUN_CONTROL} folder in the
 * master directory. Any files with the names given are immediately deleted
 * and then the name of the file is executed as a command.
 * 
 * @author Keith M. Hughes
 */
public class MasterFileControl implements DirectoryWatcherListener {

	/**
	 * The subfolder of the container installation being watched for system
	 * control.
	 */
	private static final String FOLDER_RUN_CONTROL = "run/control";

	/**
	 * The command for shutting the entire container down.
	 */
	public static final String COMMAND_SHUTDOWN = "shutdown";

	/**
	 * The space environment to run in.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Full system control of the container.
	 */
	private InteractiveSpacesSystemControl spaceSystemControl;

	/**
	 * The directory watcher watching the directory for control files.
	 */
	private DirectoryWatcher watcher;

	/**
	 * Start up the controller;
	 */
	public void startup() {
		File controlDirectory = new File(spaceEnvironment.getFilesystem()
				.getInstallDirectory(), FOLDER_RUN_CONTROL);
		watcher = new SimpleDirectoryWatcher(true);
		watcher.addDirectory(controlDirectory);
		watcher.addDirectoryWatcherListener(this);

		watcher.startup(spaceEnvironment, 10, TimeUnit.SECONDS);

		spaceEnvironment.getLog().info(
				"File control of master started");
	}

	/**
	 * Shut the control down.
	 */
	public void shutdown() {
		if (watcher != null) {
			watcher.shutdown();
			watcher = null;
		}
	}

	@Override
	public void onFileAdded(File file) {
		// Immediately delete file
		file.delete();

		handleCommand(file.getName());
	}

	@Override
	public void onFileRemoved(File file) {
		// Don't care.
	}

	/**
	 * Handle the command coming in.
	 * 
	 * @param command
	 *            the command to be executed
	 */
	void handleCommand(String command) {
		if (COMMAND_SHUTDOWN.equalsIgnoreCase(command)) {
			spaceSystemControl.shutdown();
		} else {
			spaceEnvironment.getLog().warn(
					String.format(
							"Unknown command to master file control %s",
							command));
		}
	}

	/**
	 * @param spaceEnvironment the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * @param spaceSystemControl the spaceSystemControl to set
	 */
	public void setSpaceSystemControl(
			InteractiveSpacesSystemControl spaceSystemControl) {
		this.spaceSystemControl = spaceSystemControl;
	}
}
