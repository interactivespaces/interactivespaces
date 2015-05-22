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

package interactivespaces.liveactivity.runtime.standalone.development;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.component.route.MessageRouterActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.evaluation.SimpleExpressionEvaluatorFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRuntimeComponentFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRuntimeListener;
import interactivespaces.liveactivity.runtime.LiveActivityStatusPublisher;
import interactivespaces.liveactivity.runtime.SimpleLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.StandardLiveActivityRuntime;
import interactivespaces.liveactivity.runtime.alert.LoggingAlertStatusManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.liveactivity.runtime.logging.InteractiveSpacesEnvironmentLiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.logging.LiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import interactivespaces.liveactivity.runtime.monitor.internal.StandardRemoteLiveActivityRuntimeMonitorService;
import interactivespaces.liveactivity.runtime.standalone.StandaloneActivityInstallationManager;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLiveActivityInformation;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLiveActivityInformationCollection;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLiveActivityStorageManager;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLocalLiveActivityRepository;
import interactivespaces.liveactivity.runtime.standalone.messaging.StandaloneMessageRouter;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.concurrency.SimpleSequentialEventQueue;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.resource.ManagedResource;
import interactivespaces.util.resource.ManagedResources;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.Future;

/**
 * A standalone runner for activities that takes the activities from a development environment.
 *
 * @author Trevor Pering
 * @author Keith M. Hughes
 */
public class DevelopmentStandaloneLiveActivityRuntime implements ManagedResource {

  /**
   * The filename for project files.
   */
  private static final String PROJECT_FILENAME = "project.xml";

  /**
   * The path relative to an activity project folder where development information is kept.
   */
  public static final String ACTIVITY_PATH_DEVELOPMENT = "dev";

  /**
   * The path where the build staging directory is.
   *
   * TODO(keith): See how much workbench info could be placed in a common library and move this in there.
   */
  public static final String ACTIVITY_PATH_BUILD_STAGING = "build/staging";

  /**
   * Config parameter for the instance suffix.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_STANDALONE_INSTANCE =
      "interactivespaces.standalone.instance";

  /**
   * Config parameter for whether this run is for a single activity or a group. {@code true} if this is a single
   * activity run.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_SINGLE =
      "interactivespaces.standalone.activity.single";

  /**
   * Config parameter for whether this run is for a single activity or a group.
   */
  public static final boolean CONFIGURATION_DEFAULT_INTERACTIVESPACES_STANDALONE_ACTIVITY_SINGLE = false;

  /**
   * Config parameter for activity runtime.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_RUNTIME =
      "interactivespaces.standalone.activity.runtime";

  /**
   * Config parameter for activity source.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_SOURCE =
      "interactivespaces.standalone.activity.source";

  /**
   * Config parameter for activity config.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_CONFIG =
      "interactivespaces.standalone.activity.config";

  /**
   * Config parameter for router type.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_STANDALONE_ROUTER_TYPE =
      "interactivespaces.standalone.router.type";

  /**
   * Mode value for standalone controller mode.
   */
  public static final String CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE = "standalone";

  /**
   * The file filter for finding directories.
   */
  private static final FileFilter DIRECTORY_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };

  /**
   * The collection of information about the live activities to run.
   */
  private StandaloneLiveActivityInformationCollection liveActivityInformation =
      new StandaloneLiveActivityInformationCollection();

  /**
   * The component factory for the live activity runtime.
   */
  private LiveActivityRuntimeComponentFactory runtimeComponentFactory;

  /**
   * The internal managed resources for the runner.
   */
  private ManagedResources managedResources;

  /**
   * File support instance to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Instance suffix for handling multiple instances run in the same directory.
   */
  private String instanceSuffix;

  /**
   * {@code true} (default) if the standalone router should be used.
   */
  private boolean useStandaloneRouter = true;

  /**
   * Path name for trace filters, if any.
   */
  private String traceFilterPath;

  /**
   * Path name for trace playback file.
   */
  private String tracePlaybackPath;

  /**
   * Path name for trace check file.
   */
  private String traceCheckPath;

  /**
   * Path name for send path.
   */
  private String traceSendPath;

  /**
   * Message router to use for this standalone activity.
   */
  private StandaloneMessageRouter cecRouter;

  /**
   * Space environment.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The live activity runtime for this runner.
   */
  private StandardLiveActivityRuntime liveActivityRuntime;

  /**
   * The live activity runtime listener.
   */
  private LiveActivityRuntimeListener liveActivityRuntimeListener;

  /**
   * Publisher for live activity statuses.
   */
  private LiveActivityStatusPublisher liveActivityStatusPublisher = new LiveActivityStatusPublisher() {

    @Override
    public void publishActivityStatus(String uuid, ActivityStatus status) {
      onPublishActivityStatus(uuid, status);
    }
  };

  /**
   * The future for running the activities.
   */
  private Future<?> playerFuture;

  /**
   * The system control for the runtime.
   */
  private InteractiveSpacesSystemControl systemControl;

  /**
   * Construct a new standalone runtime.
   *
   * @param runtimeComponentFactory
   *          component factory for the live activity runtime
   * @param spaceEnvironment
   *          space environment to use for this instance
   * @param liveActivityRuntimeListener
   *          a live activity runner listener to add to the runner
   * @param systemControl
   *          the system control for the container
   */
  public DevelopmentStandaloneLiveActivityRuntime(LiveActivityRuntimeComponentFactory runtimeComponentFactory,
      InteractiveSpacesEnvironment spaceEnvironment, LiveActivityRuntimeListener liveActivityRuntimeListener,
      InteractiveSpacesSystemControl systemControl) {
    this.runtimeComponentFactory = runtimeComponentFactory;
    this.spaceEnvironment = spaceEnvironment;
    this.liveActivityRuntimeListener = liveActivityRuntimeListener;
    this.systemControl = systemControl;

    managedResources = new ManagedResources(spaceEnvironment.getLog());
  }

  @Override
  public void startup() {
    Configuration configuration = spaceEnvironment.getSystemConfiguration();

    boolean isSingleActivity =
        configuration.getPropertyBoolean(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_SINGLE,
            CONFIGURATION_DEFAULT_INTERACTIVESPACES_STANDALONE_ACTIVITY_SINGLE);

    String instanceSuffixValue =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_INSTANCE, "");
    setInstanceSuffix(instanceSuffixValue);

    File activityRuntimeFolder = null;
    String activityRuntimeFolderPath =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_RUNTIME);
    if (activityRuntimeFolderPath != null) {
      getLog().info("activityRuntimePath is " + activityRuntimeFolderPath);
      activityRuntimeFolder = fileSupport.newFile(activityRuntimeFolderPath);
    }

    String activitySourceFolderPath =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_SOURCE);
    getLog().info("activitySourcePath is " + activitySourceFolderPath);
    File activitySourceFolder = fileSupport.newFile(activitySourceFolderPath);

    String standaloneRouterType =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ROUTER_TYPE);
    if (standaloneRouterType != null) {
      getLog().info("configuring to use router type " + standaloneRouterType);
      setUseStandaloneRouter(CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE.equals(standaloneRouterType));
    }

    if (isSingleActivity) {
      prepareForSingleActivityRun(activityRuntimeFolder, activitySourceFolder);
    } else {
      prepareForMultipleActivityRun(activityRuntimeFolder, activitySourceFolder);
    }

    SequentialEventQueue eventQueue = new SimpleSequentialEventQueue(spaceEnvironment, spaceEnvironment.getLog());
    managedResources.addResource(eventQueue);

    LoggingAlertStatusManager alertStatusManager = new LoggingAlertStatusManager(spaceEnvironment.getLog());
    managedResources.addResource(alertStatusManager);

    StandaloneLiveActivityStorageManager liveActivityStorageManager =
        new StandaloneLiveActivityStorageManager(liveActivityInformation);
    managedResources.addResource(liveActivityStorageManager);

    StandaloneLocalLiveActivityRepository liveActivityRepository =
        new StandaloneLocalLiveActivityRepository(liveActivityInformation, spaceEnvironment);
    managedResources.addResource(liveActivityRepository);

    ActivityInstallationManager activityInstallationManager = new StandaloneActivityInstallationManager();
    managedResources.addResource(activityInstallationManager);

    // TODO(keith): Consider placing in runtime component factory.
    ExpressionEvaluatorFactory expressionEvaluatorFactory = new SimpleExpressionEvaluatorFactory();

    DevelopmentPropertyFileLiveActivityConfigurationManager configurationManager =
        new DevelopmentPropertyFileLiveActivityConfigurationManager(expressionEvaluatorFactory, spaceEnvironment);

    LiveActivityLogFactory activityLogFactory =
        new InteractiveSpacesEnvironmentLiveActivityLogFactory(spaceEnvironment);

    RemoteLiveActivityRuntimeMonitorService runtimeDebugService =
        new StandardRemoteLiveActivityRuntimeMonitorService();

    liveActivityRuntime =
        new StandardLiveActivityRuntime(runtimeComponentFactory, liveActivityRepository, activityInstallationManager,
            activityLogFactory, configurationManager, liveActivityStorageManager, alertStatusManager, eventQueue,
            runtimeDebugService, spaceEnvironment);
    liveActivityRuntime.setLiveActivityStatusPublisher(liveActivityStatusPublisher);
    liveActivityRuntime.addRuntimeListener(liveActivityRuntimeListener);
    managedResources.addResource(liveActivityRuntime);

    managedResources.startupResources();

    playerFuture = spaceEnvironment.getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        play();
      }
    });
  }

  @Override
  public void shutdown() {
    if (playerFuture != null) {
      playerFuture.cancel(true);
    }

    managedResources.shutdownResourcesAndClear();
  }

  /**
   * Start the run.
   */
  private void play() {
    try {
      prepareFilesystem();
      prepareRuntime();
      startupActivities();

      startPlayback();
    } catch (Throwable e) {
      getLog().error("Error while running the standalone runner", e);

      systemControl.shutdown();
    }
  }

  /**
   * Prepare for the run of a single activity.
   *
   * @param activityRuntimeFolder
   *          the runtime folder to use
   * @param activityRootFolder
   *          the root folder of the activity
   */
  private void prepareForSingleActivityRun(File activityRuntimeFolder, File activityRootFolder) {
    compileLiveActivityInformation(createActivityUuid(activityRootFolder), activityRootFolder, activityRuntimeFolder);
  }

  /**
   * Prepare for a multiple activity run.
   *
   * @param rootFolder
   *          the root folder for the activities
   * @param suppliedRuntimeFolder
   *          the runtime folder supplied to the runner
   */
  private void prepareForMultipleActivityRun(File rootFolder, File suppliedRuntimeFolder) {
    List<File> foldersToUse = Lists.newArrayList();
    scanForProjectFolders(rootFolder, foldersToUse);

    for (File projectFolder : foldersToUse) {
      String uuid = createActivityUuid(projectFolder);
      File baseActivityRuntimeFolder = fileSupport.newFile(suppliedRuntimeFolder, uuid);
      compileLiveActivityInformation(uuid, projectFolder, baseActivityRuntimeFolder);
    }
  }

  /**
   * Scan for project folders.
   *
   * @param folder
   *          the root folder to scan from
   * @param foldersToUse
   *          the list of folders to use
   */
  private void scanForProjectFolders(File folder, List<File> foldersToUse) {
    if (isProjectFolder(folder)) {
      foldersToUse.add(folder);
    } else {
      File[] subfolders = folder.listFiles(DIRECTORY_FILE_FILTER);
      if (subfolders != null) {
        for (File subfolder : subfolders) {
          scanForProjectFolders(subfolder, foldersToUse);
        }
      }
    }
  }

  /**
   * Is the supplied folder a project folder?
   *
   * @param folder
   *          the folder to test
   *
   * @return {@code true} if the folder is a project folder
   */
  public boolean isProjectFolder(File folder) {
    return fileSupport.exists(fileSupport.newFile(folder, PROJECT_FILENAME));
  }

  /**
   * Create a UUID for a project.
   *
   * @param projectFolder
   *          the project folder
   *
   * @return the UUID
   */
  private String createActivityUuid(File projectFolder) {
    return projectFolder.getName();
  }

  /**
   * Fill in the rest of the information object.
   *
   * @param uuid
   *          the UUID for the activity
   * @param projectFolder
   *          the project folder
   * @param baseActivityRuntimeFolder
   *          the base folder for the activity runtime
   */
  private void compileLiveActivityInformation(String uuid, File projectFolder, File baseActivityRuntimeFolder) {
    StandaloneLiveActivityInformation info = new StandaloneLiveActivityInformation(uuid, projectFolder);

    liveActivityInformation.addInformation(info);

    info.setBaseRuntimeActivityDirectory(baseActivityRuntimeFolder);

    File installDirectory = fileSupport.newFile(info.getBaseSourceDirectory(), ACTIVITY_PATH_BUILD_STAGING);

    File activityLogDirectory =
        fileSupport.newFile(baseActivityRuntimeFolder, "activity-" + SimpleLiveActivityFilesystem.SUBDIRECTORY_LOG);
    File permanentDataDirectory =
        fileSupport.newFile(baseActivityRuntimeFolder, SimpleLiveActivityFilesystem.SUBDIRECTORY_DATA_PERMANENT);
    File tempDataDirectory =
        fileSupport.newFile(baseActivityRuntimeFolder, SimpleLiveActivityFilesystem.SUBDIRECTORY_DATA_TEMPORARY);
    File internalDirectory = fileSupport.newFile(projectFolder, ACTIVITY_PATH_DEVELOPMENT);
    SimpleLiveActivityFilesystem filesystem =
        new SimpleLiveActivityFilesystem(installDirectory, activityLogDirectory, permanentDataDirectory,
            tempDataDirectory, internalDirectory);
    filesystem.ensureDirectories();

    info.setActivityFilesystem(filesystem);
  }

  /**
   * Prepare the runtime.
   */
  public void prepareRuntime() {
    if (useStandaloneRouter) {
      cecRouter = new StandaloneMessageRouter(this);
      spaceEnvironment.setValue(MessageRouterActivityComponent.class.getName(), cecRouter);
    }
  }

  /**
   * Set the instance suffix for this instance.
   *
   * @param instanceSuffix
   *          instance suffix to use
   */
  public void setInstanceSuffix(String instanceSuffix) {
    this.instanceSuffix = (instanceSuffix != null && !instanceSuffix.trim().isEmpty()) ? instanceSuffix.trim() : null;
  }

  /**
   * Controls the router to use.
   *
   * @param useStandaloneRouter
   *          {@code true} if the standalone router should be used
   */
  public void setUseStandaloneRouter(boolean useStandaloneRouter) {
    this.useStandaloneRouter = useStandaloneRouter;
  }

  /**
   * Set the trace send path.
   *
   * @param traceSendPath
   *          trace send path
   */
  public void setTraceSendPath(String traceSendPath) {
    this.traceSendPath = traceSendPath;
  }

  /**
   * Set the trace check path.
   *
   * @param traceCheckPath
   *          trace check path
   */
  public void setTraceCheckPath(String traceCheckPath) {
    this.traceCheckPath = traceCheckPath;
  }

  /**
   * Set the trace playback path.
   *
   * @param tracePlaybackPath
   *          trace playback path
   */
  public void setTracePlaybackPath(String tracePlaybackPath) {
    this.tracePlaybackPath = tracePlaybackPath;
  }

  /**
   * Set the trace filter path.
   *
   * @param traceFilterPath
   *          trace filter path
   */
  public void setTraceFilterPath(String traceFilterPath) {
    this.traceFilterPath = traceFilterPath;
  }

  /**
   * Prepare the filesystem for use. Makes sure necessary directories exist.
   */
  public void prepareFilesystem() {
    // Nothing to do right now
  }

  /**
   * Start any trace playback.
   */
  public void startPlayback() {
    if (tracePlaybackPath != null) {
      cecRouter.playback(tracePlaybackPath, false);
    }

    if (traceSendPath != null) {
      cecRouter.playback(traceSendPath, true);
    }
  }

  /**
   * Startup all activities.
   */
  public void startupActivities() {
    if (traceCheckPath != null) {
      cecRouter.checkStart(traceCheckPath);
    }

    if (traceFilterPath != null) {
      cecRouter.setTraceFilter(traceFilterPath);
    }

    for (InstalledLiveActivity activity : liveActivityRuntime.getAllInstalledLiveActivities()) {
      liveActivityRuntime.activateLiveActivity(activity.getUuid());
    }
  }

  /**
   * An activity status update has happened.
   *
   * @param uuid
   *          uuid of activity
   * @param status
   *          status of the activity
   */
  private void onPublishActivityStatus(String uuid, ActivityStatus status) {
    spaceEnvironment.getLog().info(String.format("Activity status for activity %s is now %s", uuid, status));

    ActivityState state = status.getState();
    if (!state.isRunning() && !state.isTransitional()) {
      systemControl.shutdown();
    }
  }

  /**
   * Signal completion, either due to an error or successful verification of all messages.
   *
   * @param success
   *          {@code true} if completion is due to successful message verification
   */
  public void signalCompletion(boolean success) {
    // Need to do this in another thread because we don't know the context the error is occurring in.
    // Specifically, the web-server gets unhappy if you try to exit from an io-worker thread.
    new Thread(new ExitHelper(success)).start();
  }

  /**
   * Handle an error by the activity.
   *
   * @param msg
   *          message to include with the error
   * @param throwable
   *          exception that caused the error
   */
  public synchronized void handleError(String msg, Throwable throwable) {
    getLog().error(msg, throwable);
  }

  /**
   * Helper runner class for making a clean exit.
   */
  class ExitHelper implements Runnable {
    /**
     * Save the success code, we need it when exiting.
     */
    private boolean success;

    /**
     * Exit helper.
     *
     * @param success
     *          success state of the system
     */
    public ExitHelper(boolean success) {
      this.success = success;
    }

    /**
     * Actually shut down the system.
     */
    @Override
    public void run() {
      try {
        liveActivityRuntime.shutdownAllActivities();
      } catch (Exception e) {
        getLog().error("Error encountered during shutdown", e);
        success = false;
      }

      int returnCode = success ? 0 : 1;
      getLog().error("Exiting with result code " + returnCode);
      System.exit(returnCode);
    }
  }

  /**
   * @return logger to use
   */
  private Log getLog() {
    return spaceEnvironment.getLog();
  }

  /**
   * Get the live activity runtime for the standalone runner.
   *
   * @return the live activity runtime
   */
  public StandardLiveActivityRuntime getLiveActivityRuntime() {
    return liveActivityRuntime;
  }
}
