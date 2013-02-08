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

package interactivespaces.util.io.directorywatcher;

import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A basic {@link BatchDirectoryWatcher}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleBatchDirectoryWatcher implements BatchDirectoryWatcher,
		Runnable {

	/**
	 * The directories being watched.
	 */
	private List<File> directoriesWatched = Lists.newArrayList();

	/**
	 * The files seen on the last round.
	 */
	private Set<File> filesLastScanned = Sets.newHashSet();

	/**
	 * The listeners.
	 */
	private List<BatchDirectoryWatcherListener> listeners = Lists
			.newArrayList();

	/**
	 * The future used for scheduling the scanning.
	 */
	private ScheduledFuture<?> scanningFuture;

	@Override
	public synchronized void startup(InteractiveSpacesEnvironment environment,
			long period, TimeUnit unit) {
		scanningFuture = environment.getExecutorService().scheduleAtFixedRate(
				this, 0, period, unit);
	}

	@Override
	public Set<File> startupWithScan(InteractiveSpacesEnvironment environment,
			long period, TimeUnit unit) {
		filesLastScanned = scanAllDirectories();
		
		startup(environment, period, unit);
		
		return Sets.newHashSet(filesLastScanned);
	}

	@Override
	public synchronized void shutdown() {
		if (scanningFuture != null) {
			scanningFuture.cancel(false);
			scanningFuture = null;
		}
	}

	@Override
	public synchronized void addDirectory(File directory) {
		if (directory.isDirectory()) {
			if (directory.canRead()) {
				directoriesWatched.add(directory);
			} else {
				throw new IllegalArgumentException(String.format(
						"%s is not readable", directory));
			}
		} else if (directory.exists()) {
			// The file exists, but it isn't a directory.
			//
			// This is checked for separately to handle directories added
			// after the watcher starts running.
			throw new IllegalArgumentException(String.format(
					"%s is not a directory", directory));
		} else {
			// It doesn't exist yet. Assume it eventually will.
			directoriesWatched.add(directory);
		}
	}

	@Override
	public synchronized void addBatchDirectoryWatcherListener(
			BatchDirectoryWatcherListener listener) {
		listeners.add(listener);
	}

	@Override
	public synchronized void removeBatchDirectoryWatcherListener(
			BatchDirectoryWatcherListener listener) {
		listeners.remove(listener);
	}

	@Override
	public synchronized void scan() {
		Set<File> currentScan = scanAllDirectories();

		findAddedFiles(currentScan);

		filesLastScanned = currentScan;
	}

	/**
	 * Find all files added since the last scan.
	 * 
	 * @param currentScan
	 *            the files from the current scan
	 */
	private void findAddedFiles(Set<File> currentScan) {
		Set<File> filesAdded = Sets.newHashSet();
		for (File fileFromCurrent : currentScan) {
			if (!filesLastScanned.contains(fileFromCurrent)) {
				filesAdded.add(fileFromCurrent);
			}
		}
		signalFileAdded(filesAdded);
	}

	/**
	 * Scan all directories for the files they contain
	 * 
	 * @return the set of all files which are currently in the folders
	 */
	private Set<File> scanAllDirectories() {
		Set<File> currentScan = Sets.newHashSet();
		for (File directory : directoriesWatched) {
			if (directory.exists() && directory.isDirectory()) {
				for (File file : directory.listFiles()) {
					currentScan.add(file);
				}
			}
		}

		return currentScan;
	}

	/**
	 * Signal all listeners that a file has been added.
	 * 
	 * @param fileAdded
	 *            the file which has been added
	 */
	private void signalFileAdded(Set<File> filesAdded) {
		for (BatchDirectoryWatcherListener listener : listeners) {
			listener.onFilesAdded(filesAdded);
		}
	}

	@Override
	public void run() {
		scan();
	}
}
