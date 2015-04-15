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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.component.route.MessageRouterActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.evaluation.SimpleExpressionEvaluatorFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRuntimeComponentFactory;
import interactivespaces.liveactivity.runtime.LiveActivityStatusPublisher;
import interactivespaces.liveactivity.runtime.SimpleLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.StandardLiveActivityRuntime;
import interactivespaces.liveactivity.runtime.alert.LoggingAlertStatusManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.liveactivity.runtime.logging.InteractiveSpacesEnvironmentLiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.logging.LiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.standalone.StandaloneActivityInstallationManager;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLiveActivityInformation;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLiveActivityInformationCollection;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLiveActivityStorageManager;
import interactivespaces.liveactivity.runtime.standalone.StandaloneLocalLiveActivityRepository;
import interactivespaces.liveactivity.runtime.standalone.messaging.StandaloneMessageRouter;
import interactivespaces.system.InteractiveSpacesEnvironment;
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileLock;
import java.util.List;

/**
 * A standalone runner for activities that takes the activities from a development environment.
 *
 * @author Trevor Pering
 * @author Keith M. Hughes
 */
public class DevelopmentStandaloneActivityRunner implements ManagedResource {

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
   * Number of threads in the thread pool.
   */
  public static final int THREAD_POOL_SIZE = 100;

  /**
   * Config filename to use for activity specific configuration.
   */
  public static final String ACTIVITY_SPECIFIC_CONFIG_FILE_NAME = "standalone.conf";

  /**
   * Config filename to use for activity specific configuration.
   */
  public static final String LOCAL_CONFIG_FILE_NAME = "local.conf";

  /**
   * Config filename for the included activity configuration.
   */
  public static final String ACTIVITY_CONFIG_FILE_NAME = "activity.conf";

  /**
   * Filename to use for a running instance lock.
   */
  private static final String LOCK_FILE_NAME = "instancelock";

  /**
   * Maximum number of standalone instances to allow.
   */
  private static final int MAX_INSTANCE_COUNT = 10;

  /**
   * File support instance to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Instance suffix for handling multiple instances run in the same directory.
   */
  private String instanceSuffix;

  /**
   * {@code true} if this is the primary (first) instance running in the same directory.
   */
  private boolean isPrimaryInstance;

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
   * Mutable local config file for this activity. Has default, but can be set.
   */
  private File localConfigFile = new File(LOCAL_CONFIG_FILE_NAME);

  /**
   * Mutable local config file for this activity. Has default, but can be set.
   */
  private File activityConfigFile = new File(ACTIVITY_SPECIFIC_CONFIG_FILE_NAME);

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
   * Publisher for live activity statuses.
   */
  private LiveActivityStatusPublisher liveActivityStatusPublisher = new LiveActivityStatusPublisher() {

    @Override
    public void publishActivityStatus(String uuid, ActivityStatus status) {
      onPublishActivityStatus(uuid, status);
    }
  };

  /**
   * Create a new activity runner.
   *
   * @param runtimeComponentFactory
   *          component factory for the live activity runtime
   * @param spaceEnvironment
   *          space environment to use for this instance
   */
  public DevelopmentStandaloneActivityRunner(LiveActivityRuntimeComponentFactory runtimeComponentFactory,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.runtimeComponentFactory = runtimeComponentFactory;
    this.spaceEnvironment = spaceEnvironment;

    managedResources = new ManagedResources(spaceEnvironment.getLog());
  }

  @Override
  public void startup() {
    Configuration configuration = spaceEnvironment.getSystemConfiguration();
    addDynamicConfiguration(configuration);

    String instance = configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_INSTANCE, "");
    setInstanceSuffix(instance);

    String activityRuntimePath =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_RUNTIME);
    getLog().info("activityRuntimePath is " + activityRuntimePath);
    setActivityRuntimeDir(new File(activityRuntimePath));

    String activitySourcePath =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_SOURCE);
    getLog().info("activitySourcePath is " + activitySourcePath);

    String activityConfigPath =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ACTIVITY_CONFIG);
    getLog().info("activityConfigPath is " + activityConfigPath);

    String standaloneRouterType =
        configuration.getPropertyString(CONFIGURATION_INTERACTIVESPACES_STANDALONE_ROUTER_TYPE);
    if (standaloneRouterType != null) {
      getLog().info("configuring to use router type " + standaloneRouterType);
      setUseStandaloneRouter("standalone".equals(standaloneRouterType));
    }

    setActivityConfigFile(new File(activityConfigPath,
        DevelopmentStandaloneActivityRunner.ACTIVITY_SPECIFIC_CONFIG_FILE_NAME));
    setLocalConfigFile(new File(activityConfigPath, DevelopmentStandaloneActivityRunner.LOCAL_CONFIG_FILE_NAME));

    List<File> foldersToUse = Lists.newArrayList();
    File rootFolder = new File(activitySourcePath);
    scanForProjects(rootFolder, foldersToUse);
    prepareLiveActivityInformation(rootFolder, foldersToUse);

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

    liveActivityRuntime =
        new StandardLiveActivityRuntime(runtimeComponentFactory, liveActivityRepository, activityInstallationManager,
            activityLogFactory, configurationManager, liveActivityStorageManager, alertStatusManager, eventQueue,
            spaceEnvironment);
    liveActivityRuntime.setLiveActivityStatusPublisher(liveActivityStatusPublisher);
    managedResources.addResource(liveActivityRuntime);

    managedResources.startupResources();

    play();
  }

  @Override
  public void shutdown() {
    managedResources.shutdownResourcesAndClear();
  }

  /**
   * Start the run.
   */
  public void play() {
    prepareFilesystem();
    prepareRuntime();
    startupActivity();

    startPlayback();
  }

  /**
   * Scan for project folders.
   *
   * @param folder
   *          the root folder to scan from
   * @param foldersToUse
   *          the list of folders to use
   */
  private void scanForProjects(File folder, List<File> foldersToUse) {
    if (isProjectFolder(folder)) {
      foldersToUse.add(folder);
    } else {
      File[] subfolders = folder.listFiles(DIRECTORY_FILE_FILTER);
      if (subfolders != null) {
        for (File subfolder : subfolders) {
          scanForProjects(subfolder, foldersToUse);
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
    return fileSupport.exists(fileSupport.newFile(folder, "project.xml"));
  }

  /**
   * Prepare the activity information from the project files.
   *
   * @param rootFolder
   *          the root folder the runner was started in
   * @param foldersToUse
   *          the folders to use for activities
   */
  private void prepareLiveActivityInformation(File rootFolder, List<File> foldersToUse) {
    // No matter what, the runtime folder will be in the project.
    File runtimeFolder = fileSupport.newFile(rootFolder, "run");
    fileSupport.directoryExists(runtimeFolder);

    if (foldersToUse.size() > 1) {
      for (File projectFolder : foldersToUse) {
        String uuid = createActivityUuid(projectFolder);
        File baseActivityRuntimeFolder = fileSupport.newFile(runtimeFolder, uuid);
        compileLiveActivityInformation(uuid, projectFolder, baseActivityRuntimeFolder);
      }
    } else {
      File projectFolder = foldersToUse.get(0);

      compileLiveActivityInformation(createActivityUuid(projectFolder), projectFolder, runtimeFolder);
    }
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

    File logDirectory = fileSupport.newFile(baseActivityRuntimeFolder, SimpleLiveActivityFilesystem.SUBDIRECTORY_LOG);
    File permanentDataDirectory =
        fileSupport.newFile(baseActivityRuntimeFolder, SimpleLiveActivityFilesystem.SUBDIRECTORY_DATA_PERMANENT);
    File tempDataDirectory =
        fileSupport.newFile(baseActivityRuntimeFolder, SimpleLiveActivityFilesystem.SUBDIRECTORY_DATA_TEMPORARY);
    File internalDirectory = fileSupport.newFile(projectFolder, ACTIVITY_PATH_DEVELOPMENT);
    SimpleLiveActivityFilesystem filesystem =
        new SimpleLiveActivityFilesystem(installDirectory, logDirectory, permanentDataDirectory, tempDataDirectory,
            internalDirectory);
    filesystem.ensureDirectories();

    info.setActivityFilesystem(filesystem);
  }

  /**
   * Get the instance suffix to use for this instance. The suffix returned depends on the number of already running
   * instances.
   *
   * @param rootDir
   *          the root directory that holds the instance locks
   *
   * @return suffix to use for directories and files
   */
  private String findInstanceSuffix(File rootDir) {
    if (instanceSuffix != null) {
      return instanceSuffix;
    }

    int instance = 0;
    while (instance < MAX_INSTANCE_COUNT) {
      String suffix = instance > 0 ? ("-" + instance) : "";
      File lockFile = new File(rootDir, LOCK_FILE_NAME + suffix);
      try {
        RandomAccessFile pidRaf = new RandomAccessFile(lockFile, "rw");
        FileLock fileLock = pidRaf.getChannel().tryLock(0, Long.MAX_VALUE, false);
        if (fileLock != null) {
          return suffix;
        }
      } catch (IOException e) {
        // Do nothing, increment and try again.
      }
      instance++;
    }
    throw new InteractiveSpacesException("Could not lock run file after " + MAX_INSTANCE_COUNT + " tries");
  }

  /**
   * Get the configuration file to use for local activity configuration.
   *
   * @param configFile
   *          base configuration file
   *
   * @return file to use for local activity configuration
   */
  private File getInstanceConfigFile(File configFile) {
    String rootPath = configFile.getPath();
    int breakIndex = rootPath.lastIndexOf('.');
    String instanceName = rootPath.substring(0, breakIndex) + instanceSuffix + rootPath.substring(breakIndex);
    return new File(instanceName);
  }

  /**
   * Add dynamic configuration parameters to this configuration.
   *
   * @param configuration
   *          the configuration to dynamically update
   */
  private void addDynamicConfiguration(Configuration configuration) {
    try {
      String hostname = InetAddress.getLocalHost().getHostName();
      configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_HOSTNAME, hostname);
      configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_HOSTID, hostname);
      String hostAddress = InetAddress.getByName(hostname).getHostAddress();
      configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_HOST_ADDRESS, hostAddress);
    } catch (UnknownHostException e) {
      throw new InteractiveSpacesException("Could not determine hostname", e);
    }
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
    this.instanceSuffix = instanceSuffix;
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
   * Set the activity runtime dir.
   *
   * @param activityRuntimeDir
   *          directory to use for activity runtime files
   */
  public void setActivityRuntimeDir(File activityRuntimeDir) {
    instanceSuffix = findInstanceSuffix(activityRuntimeDir);
    isPrimaryInstance = instanceSuffix.length() == 0;
    File actualRootDir =
        isPrimaryInstance ? activityRuntimeDir : new File(activityRuntimeDir, "instance" + instanceSuffix);
    fileSupport.directoryExists(actualRootDir);
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
   * Set the local config file.
   *
   * @param localConfigFile
   *          local config file
   */
  public void setLocalConfigFile(File localConfigFile) {
    this.localConfigFile = getInstanceConfigFile(localConfigFile);
  }

  /**
   * Set the activity config file.
   *
   * @param activityConfigFile
   *          activity config file
   */
  public void setActivityConfigFile(File activityConfigFile) {
    this.activityConfigFile = getInstanceConfigFile(activityConfigFile);
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
    if (!isPrimaryInstance) {
      // When running multiple instances, require explicit local config files to
      // make it more obvious what they should be.
      if (!activityConfigFile.exists()) {
        throw new InteractiveSpacesException("Missing activity config file " + activityConfigFile.getAbsolutePath());
      }
    }

    spaceEnvironment.getLog().info("Using activity config file " + activityConfigFile.getAbsolutePath());
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
   * Startup the activity.
   */
  public void startupActivity() {
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
    // TODO(keith): If any signal crash, should shut runner down.

    spaceEnvironment.getLog().info(String.format("Activity status for activity %s is now %s", uuid, status));
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
