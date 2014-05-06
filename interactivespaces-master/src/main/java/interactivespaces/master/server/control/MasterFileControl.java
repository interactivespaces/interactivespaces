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

import interactivespaces.domain.basic.Activity;
import interactivespaces.master.api.MasterApiAutomationManager;
import interactivespaces.master.api.MasterApiSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.resource.repository.ActivityRepositoryManager;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.io.directorywatcher.DirectoryWatcher;
import interactivespaces.util.io.directorywatcher.DirectoryWatcherListener;
import interactivespaces.util.io.directorywatcher.SimpleDirectoryWatcher;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handle control of the master by using the filesystem.
 *
 * <p>
 * These command work by looking at the {@link #FOLDER_RUN_CONTROL} folder in
 * the master directory. Any files with the names given are immediately deleted
 * and then the name of the file is executed as a command.
 *
 * @author Keith M. Hughes
 */
public class MasterFileControl implements DirectoryWatcherListener {

  /**
   * How often the watched directories should be scanned. In seconds.
   */
  public static final int DIRECTORY_SCAN_TIME = 10;

  /**
   * The subfolder of the container installation being watched for system
   * control.
   */
  public static final String FOLDER_RUN_CONTROL = "run/control";

  /**
   * The command for shutting the entire container down.
   */
  public static final String COMMAND_SHUTDOWN = "shutdown";

  /**
   * The command for forcing the calculation of all activity hashes.
   */
  public static final String COMMAND_CALCULATE_ACTIVITY_HASHES = "calculate-activity-hashes";

  /**
   * The command for shutting down all space controllers.
   */
  public static final String COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL = "space-controllers-shutdown-all";

  /**
   * The command for shutting down all activities on all space controllers.
   */
  public static final String COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL_ACTIVITIES =
      "space-controllers-shutdown-all-activities";

  /**
   * The command for starting up a live activity group.
   */
  public static final String COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_STARTUP = "live-activity-group-startup-";

  /**
   * The command for activating up a live activity group.
   */
  public static final String COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_ACTIVATE = "live-activity-group-activate-";

  /**
   * The command for shutting down a live activity group.
   */
  public static final String COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_SHUTDOWN = "live-activity-group-shutdown-";

  /**
   * The command for activating up a live activity group.
   */
  public static final String COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_DEACTIVATE = "live-activity-group-deactivate-";

  /**
   * The command for starting up a space.
   */
  public static final String COMMAND_PREFIX_SPACE_STARTUP = "space-startup-";

  /**
   * The command for shutting down a space.
   */
  public static final String COMMAND_PREFIX_SPACE_SHUTDOWN = "space-shutdown-";

  /**
   * The command for activating up a space.
   */
  public static final String COMMAND_PREFIX_SPACE_ACTIVATE = "space-activate-";

  /**
   * The command for deactivating up a space.
   */
  public static final String COMMAND_PREFIX_SPACE_DEACTIVATE = "space-deactivate-";

  /**
   * The command for running a script.
   */
  public static final String COMMAND_PREFIX_SCRIPT_RUN = "script-run-";

  /**
   * The space environment to run in.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Full system control of the container.
   */
  private InteractiveSpacesSystemControl spaceSystemControl;

  /**
   * Master API manager for control of space controllers.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * Master API manager for automation control, e.g. running scripts.
   */
  private MasterApiAutomationManager masterApiAutomationManager;

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository manager for activities.
   */
  private ActivityRepositoryManager activityRepositoryManager;

  /**
   * The directory watcher watching the directory for control files.
   */
  private DirectoryWatcher watcher;

  /**
   * Start up the controller.
   */
  public void startup() {
    File controlDirectory = new File(spaceEnvironment.getFilesystem().getInstallDirectory(), FOLDER_RUN_CONTROL);
    watcher = new SimpleDirectoryWatcher(true);
    watcher.addDirectory(controlDirectory);
    watcher.addDirectoryWatcherListener(this);

    // The trains must keep running
    watcher.setStopOnException(false);

    watcher.startup(spaceEnvironment, DIRECTORY_SCAN_TIME, TimeUnit.SECONDS);

    spaceEnvironment.getLog().info(
        String.format("File control of master started, watching %s", controlDirectory.getAbsolutePath()));
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
   *          the command to be executed
   */
  void handleCommand(String command) {
    spaceEnvironment.getLog().info(String.format("Master file control received command %s", command));

    try {
      if (COMMAND_SHUTDOWN.equalsIgnoreCase(command)) {
        spaceSystemControl.shutdown();
      } else if (COMMAND_CALCULATE_ACTIVITY_HASHES.equalsIgnoreCase(command)) {
        calculateActivityHashes();
      } else if (COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL.equalsIgnoreCase(command)) {
        masterApiSpaceControllerManager.shutdownAllSpaceControllers();
      } else if (COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL_ACTIVITIES.equalsIgnoreCase(command)) {
        masterApiSpaceControllerManager.shutdownAllActivitiesAllSpaceControllers();
      } else if (command.startsWith(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_STARTUP)) {
        String id = command.substring(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_STARTUP.length());

        masterApiSpaceControllerManager.startupLiveActivityGroup(id);
      } else if (command.startsWith(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_SHUTDOWN)) {
        String id = command.substring(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_SHUTDOWN.length());

        masterApiSpaceControllerManager.shutdownLiveActivityGroup(id);
      } else if (command.startsWith(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_ACTIVATE)) {
        String id = command.substring(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_ACTIVATE.length());

        masterApiSpaceControllerManager.activateLiveActivityGroup(id);
      } else if (command.startsWith(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_DEACTIVATE)) {
        String id = command.substring(COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_DEACTIVATE.length());

        masterApiSpaceControllerManager.deactivateLiveActivityGroup(id);
      } else if (command.startsWith(COMMAND_PREFIX_SPACE_STARTUP)) {
        String id = command.substring(COMMAND_PREFIX_SPACE_STARTUP.length());

        masterApiSpaceControllerManager.startupSpace(id);
      } else if (command.startsWith(COMMAND_PREFIX_SPACE_SHUTDOWN)) {
        String id = command.substring(COMMAND_PREFIX_SPACE_SHUTDOWN.length());

        masterApiSpaceControllerManager.shutdownSpace(id);
      } else if (command.startsWith(COMMAND_PREFIX_SPACE_ACTIVATE)) {
        String id = command.substring(COMMAND_PREFIX_SPACE_ACTIVATE.length());

        masterApiSpaceControllerManager.activateSpace(id);
      } else if (command.startsWith(COMMAND_PREFIX_SPACE_DEACTIVATE)) {
        String id = command.substring(COMMAND_PREFIX_SPACE_DEACTIVATE.length());

        masterApiSpaceControllerManager.deactivateSpace(id);
      } else if (command.startsWith(COMMAND_PREFIX_SCRIPT_RUN)) {
        String id = command.substring(COMMAND_PREFIX_SCRIPT_RUN.length());

        masterApiAutomationManager.runScript(id);
      } else {
        spaceEnvironment.getLog().warn(String.format("Unknown command to master file control %s", command));
      }
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Exception while executing master file control %s", command), e);
    }
  }

  /**
   * Calculate and update the hashes for all current activities. This is
   * necessary for working with legacy systems that have already uploaded
   * activities.
   *
   * TODO(peringknife): Remove this after all centers have been upgraded.
   */
  private void calculateActivityHashes() {
    List<Activity> activities = activityRepository.getAllActivities();
    for (Activity activity : activities) {
      activityRepositoryManager.calculateBundleContentHash(activity);
      activityRepository.saveActivity(activity);
    }
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * @param spaceSystemControl
   *          the spaceSystemControl to set
   */
  public void setSpaceSystemControl(InteractiveSpacesSystemControl spaceSystemControl) {
    this.spaceSystemControl = spaceSystemControl;
  }

  /**
   * @param masterApiControllerManager
   *          the uiControllerManager to set
   */
  public void setMasterApiSpaceControllerManager(MasterApiSpaceControllerManager masterApiControllerManager) {
    this.masterApiSpaceControllerManager = masterApiControllerManager;
  }

  /**
   * @param masterApiAutomationManager
   *          the uiAutomationManager to set
   */
  public void setMasterApiAutomationManager(MasterApiAutomationManager masterApiAutomationManager) {
    this.masterApiAutomationManager = masterApiAutomationManager;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param activityRepositoryManager
   *          the activityRepositoryManager to set
   */
  public void setActivityRepositoryManager(ActivityRepositoryManager activityRepositoryManager) {
    this.activityRepositoryManager = activityRepositoryManager;
  }
}
