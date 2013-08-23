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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.Files;

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A basic {@link DirectoryWatcher}/
 *
 * @author Keith M. Hughes
 */
public class SimpleDirectoryWatcher implements DirectoryWatcher, Runnable {

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
  private List<DirectoryWatcherListener> listeners = Lists.newArrayList();

  /**
   * The future used for scheduling the scanning.
   */
  private ScheduledFuture<?> scanningFuture;

  /**
   * {@code true} if the directories should be cleaned before they are watched.
   */
  private boolean cleanFirst = false;

  /**
   * {@code true} if the watcher should stop when there is an exception.
   */
  private boolean stopOnException = true;

  /**
   * The logger to use.
   */
  private Log log;

  /**
   * Construct a new SimpleWatcher.
   *
   * @param cleanFirst
   *          {@code true} if added directories are cleaned before they are
   *          watched
   */
  public SimpleDirectoryWatcher() {
    this(false);
  }

  /**
   * Construct a new SimpleWatcher.
   *
   * @param cleanFirst
   *          {@code true} if added directories are cleaned before they are
   *          watched
   */
  public SimpleDirectoryWatcher(boolean cleanFirst) {
    this(cleanFirst, null);
  }

  /**
   * Construct a new SimpleWatcher.
   *
   * @param cleanFirst
   *          {@code true} if added directories are cleaned before they are
   *          watched
   * @param log
   *          the logger to use
   */
  public SimpleDirectoryWatcher(boolean cleanFirst, Log log) {
    this.log = log;
    setCleanFirst(cleanFirst);
  }

  @Override
  public synchronized void startup(InteractiveSpacesEnvironment environment, long period,
      TimeUnit unit) {
    // If no log was set, we will use the space environment log
    if (log == null) {
      log = environment.getLog();
    }
    scanningFuture = environment.getExecutorService().scheduleAtFixedRate(this, 0, period, unit);
  }

  @Override
  public Set<File> startupWithScan(InteractiveSpacesEnvironment environment, long period,
      TimeUnit unit) {
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
        if (directory.canWrite()) {
          if (cleanFirst) {
            Files.deleteDirectoryContents(directory);
          }
        }
        directoriesWatched.add(directory);
      } else {
        throw new IllegalArgumentException(String.format("%s is not readable", directory));
      }
    } else if (directory.exists()) {
      // The file exists, but it isn't a directory.
      //
      // This is checked for separately to handle directories added
      // after the watcher starts running.
      throw new IllegalArgumentException(String.format("%s is not a directory", directory));
    } else {
      // Assume the directory will be added at some point
      directoriesWatched.add(directory);
    }
  }

  @Override
  public synchronized void addDirectoryWatcherListener(DirectoryWatcherListener listener) {
    listeners.add(listener);
  }

  @Override
  public synchronized void removeDirectoryWatcherListener(DirectoryWatcherListener listener) {
    listeners.remove(listener);
  }

  @Override
  public synchronized void scan() {
    Set<File> currentScan = scanAllDirectories();

    findAddedFiles(currentScan);
    findRemovedFiles(currentScan);

    filesLastScanned = currentScan;
  }

  /**
   * Find all files removed since the last scan.
   *
   * @param currentScan
   *          the files from the current scan
   */
  private void findRemovedFiles(Set<File> currentScan) {
    for (File fileFromLast : filesLastScanned) {
      if (!currentScan.contains(fileFromLast)) {
        signalFileRemoved(fileFromLast);
      }
    }
  }

  /**
   * Find all files added since the last scan.
   *
   * @param currentScan
   *          the files from the current scan
   */
  private void findAddedFiles(Set<File> currentScan) {
    for (File fileFromCurrent : currentScan) {
      if (!filesLastScanned.contains(fileFromCurrent)) {
        signalFileAdded(fileFromCurrent);
      }
    }
  }

  /**
   * Scan all directories for the files they contain
   *
   * @return the set of all files which are currently in the folders
   */
  protected Set<File> scanAllDirectories() {
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
   *          the file which has been added
   */
  private void signalFileAdded(File fileAdded) {
    for (DirectoryWatcherListener listener : listeners) {
      try {
        listener.onFileAdded(fileAdded);
      } catch (Exception e) {
        log.error(
            String.format("Exception while signalling file added %s", fileAdded.getAbsolutePath()),
            e);
      }
    }
  }

  /**
   * Signal all listeners that a file has been removed.
   *
   * @param fileRemoved
   *          the file which has been removed
   */
  private void signalFileRemoved(File fileRemoved) {
    for (DirectoryWatcherListener listener : listeners) {
      try {
        listener.onFileRemoved(fileRemoved);
      } catch (Exception e) {
        log.error(
            String.format("Exception while signalling file removed %s",
                fileRemoved.getAbsolutePath()), e);
      }
    }
  }

  @Override
  public void run() {
    try {
      scan();
    } catch (Throwable e) {
      log.error("Exception happened during directory watcher scan", e);

      if (stopOnException) {
        // TODO(keith): Not entirely happy with this. maybe
        // eventually put the future into this instance and shut it
        // down.
        throw new RuntimeException();
      }
    }
  }

  @Override
  public void setCleanFirst(boolean cleanFirst) {
    this.cleanFirst = cleanFirst;
  }

  @Override
  public void setStopOnException(boolean stopOnException) {
    this.stopOnException = stopOnException;
  }
}
