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

package interactivespaces.liveactivity.runtime.standalone;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityRuntimeStartupType;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.component.CoreExistingActivityComponentFactory;
import interactivespaces.activity.component.route.MessageRouterActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimplePropertyFileSingleConfigurationStorageManager;
import interactivespaces.configuration.SingleConfigurationStorageManager;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.evaluation.ExpressionEvaluator;
import interactivespaces.evaluation.SimpleExpressionEvaluator;
import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.LiveActivityRunner;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerListener;
import interactivespaces.liveactivity.runtime.SimpleLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.configuration.StandardLiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.domain.ActivityInstallationStatus;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.liveactivity.runtime.standalone.messaging.StandaloneMessageRouter;
import interactivespaces.liveactivity.runtime.standalone.stubs.StandaloneSpaceController;
import interactivespaces.resource.Version;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.io.Closeables;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.Map;

/**
 * Standalone activity runner class.
 *
 * @author Trevor Pering
 */
public class StandaloneActivityRunner {

  /**
   * Mask to use for creating a random UUID.
   */
  public static final int RANDOM_UUID_MAX = 0x10000000;

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
   * Root directory for the individual activity.
   */
  private File activityRootDir;

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
   * Space controller instance.
   */
  private StandaloneSpaceController controller;

  /**
   * Factory for native activity runners.
   */
  private NativeActivityRunnerFactory nativeActivityRunnerFactory;

  /**
   * Active activity under test.
   */
  private LiveActivityRunner activeActivity;

  /**
   * Activity filesystem for activity.
   */
  private InternalLiveActivityFilesystem activityFilesystem;

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
   * The controller activity factory.
   */
  private final LiveActivityRunnerFactory liveActivityRunnerFactory;

  /**
   * Space environment.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The activity component factory.
   */
  private ActivityComponentFactory activityComponentFactory;

  /**
   * Create a new activity runner.
   *
   * @param spaceEnvironment
   *          space environment to use for this instance
   * @param liveActivityRunnerFactory
   *          live activity factory to use
   * @param nativeActivityRunnerFactory
   *          the factory for native activity runners
   */
  public StandaloneActivityRunner(InteractiveSpacesEnvironment spaceEnvironment,
      LiveActivityRunnerFactory liveActivityRunnerFactory, NativeActivityRunnerFactory nativeActivityRunnerFactory) {
    this.liveActivityRunnerFactory = liveActivityRunnerFactory;
    this.spaceEnvironment = spaceEnvironment;
    this.nativeActivityRunnerFactory = nativeActivityRunnerFactory;

    activityComponentFactory = new CoreExistingActivityComponentFactory();
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
   * Actually create the activity.
   */
  public void createActivity() {
    getLog().info("Creating activity " + activityRootDir.getAbsolutePath());
    LiveActivityConfiguration liveActivityConfiguration = getLiveActivityConfiguration();
    LiveActivityRunnerListener liveActivityRunnerListener = null;

    // TODO(keith): Change once have live activity runtime integration.
    // activeActivity =
    // liveActivityRunnerFactory.newLiveActivityRunner(getLiveActivity(), activityFilesystem,
    // liveActivityConfiguration, liveActivityRunnerListener, new BaseActivityRuntime(nativeActivityRunnerFactory,
    // activityComponentFactory, spaceEnvironment));
  }

  /**
   * Get a live activity configuration.
   *
   * @return live activity configuration
   */
  private LiveActivityConfiguration getLiveActivityConfiguration() {
    ExpressionEvaluator expressionEvaluator = new SimpleExpressionEvaluator();

    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    addDynamicConfiguration(systemConfiguration);

    SingleConfigurationStorageManager baseConfigurationManager =
        new SimplePropertyFileSingleConfigurationStorageManager(true,
            activityFilesystem.getInstallFile(ACTIVITY_CONFIG_FILE_NAME), expressionEvaluator);
    baseConfigurationManager.load();

    SingleConfigurationStorageManager installedConfigurationManager =
        new SimplePropertyFileSingleConfigurationStorageManager(false, activityConfigFile, expressionEvaluator);
    installedConfigurationManager.load();

    SingleConfigurationStorageManager localConfigurationManager =
        new SimplePropertyFileSingleConfigurationStorageManager(false, localConfigFile, expressionEvaluator);
    localConfigurationManager.load();
    Map<String, String> collapsedMap = localConfigurationManager.getConfiguration().getCollapsedMap();
    installedConfigurationManager.update(collapsedMap);

    LiveActivityConfiguration configuration =
        new StandardLiveActivityConfiguration(baseConfigurationManager, installedConfigurationManager,
            expressionEvaluator, systemConfiguration);
    expressionEvaluator.setEvaluationEnvironment(configuration);
    return configuration;
  }

  /**
   * Add dynamic configuration parameters to this configuration.
   *
   * @param configuration
   *          the configuration to dynamically update
   */
  private void addDynamicConfiguration(Configuration configuration) {
    String platformOs = SystemUtils.IS_OS_LINUX ? "linux" : "osx";
    configuration.setValue("interactivespaces.platform.os", platformOs);

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
   * Make the simple controller instance.
   */
  public void makeController() {
    if (controller != null) {
      throw new InteractiveSpacesException("controller already defined");
    }
    activityFilesystem = new SimpleLiveActivityFilesystem(activityRootDir);

    SimpleNativeActivityRunnerFactory simpleNativeActivityRunnerFactory =
        new SimpleNativeActivityRunnerFactory(spaceEnvironment);

    controller = new StandaloneSpaceController(spaceEnvironment, simpleNativeActivityRunnerFactory);

    controller.startup();

    if (useStandaloneRouter) {
      cecRouter = new StandaloneMessageRouter(this);
      controller.getSpaceEnvironment().setValue(MessageRouterActivityComponent.class.getName(), cecRouter);
    }
  }

  /**
   * Get a live activity instance.
   *
   * @return installed live activity instance
   */
  private InstalledLiveActivity getLiveActivity() {
    final InstalledLiveActivity liveActivity = new SimpleInstalledLiveActivity();

    Date installedDate = new Date(controller.getSpaceEnvironment().getTimeProvider().getCurrentTime());

    File activityFile = activityFilesystem.getInstallFile("activity.xml");

    InputStream activityDescriptionStream = null;
    Version version;
    String identifyingName;
    try {
      activityDescriptionStream = new FileInputStream(activityFile);
      ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
      ActivityDescription activityDescription = reader.readDescription(activityDescriptionStream);
      version = Version.parseVersion(activityDescription.getVersion());
      identifyingName = activityDescription.getIdentifyingName();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not read activity file description from "
          + activityFile.getAbsolutePath(), e);
    } finally {
      Closeables.closeQuietly(activityDescriptionStream);
    }

    liveActivity.setUuid(Long.toHexString((long) (Math.random() * RANDOM_UUID_MAX)));
    liveActivity.setBaseInstallationLocation(activityFilesystem.getInstallDirectory().getAbsolutePath());
    liveActivity.setIdentifyingName(identifyingName);
    liveActivity.setVersion(version);
    liveActivity.setLastDeployedDate(installedDate);
    liveActivity.setLastActivityState(ActivityState.READY);
    liveActivity.setInstallationStatus(ActivityInstallationStatus.OK);
    liveActivity.setRuntimeStartupType(ActivityRuntimeStartupType.READY);

    return liveActivity;
  }

  /**
   * Report the current status of the activity.
   */
  public void reportStatus() {
    getLog().info("Activity status: " + activeActivity.getCachedActivityStatus());
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
    FileSupportImpl.INSTANCE.directoryExists(actualRootDir);
    this.activityRootDir = actualRootDir;
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
    fileSupport.directoryExists(new File(activityRootDir, "tmp"));
    fileSupport.directoryExists(new File(activityRootDir, "log"));

    if (!isPrimaryInstance) {
      // No standard way to do this until Java 7 -- so hack for linux.
      // TODO(peringknife): Move this to the activity runner wrapper, since it now knows about the instance.
      File targetInstallLink = new File(activityRootDir, "install");
      try {
        String command = "/bin/ln -sf ../install " + targetInstallLink.getAbsolutePath();
        spaceEnvironment.getLog().info("Executing: " + command);
        Runtime.getRuntime().exec(command);
      } catch (IOException e) {
        throw new InteractiveSpacesException("Could not execute link command", e);
      }

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
    checkActivityState(ActivityState.READY);

    if (traceCheckPath != null) {
      cecRouter.checkStart(traceCheckPath);
    }

    if (traceFilterPath != null) {
      cecRouter.setTraceFilter(traceFilterPath);
    }

    activeActivity.startup();
  }

  /**
   * Activate the activity.
   */
  public void activateActivity() {
    checkActivityState(ActivityState.RUNNING);
    activeActivity.activate();
  }

  /**
   * Check that the activity state is as expected.
   *
   * @param expectedState
   *          the expected state
   */
  private void checkActivityState(ActivityState expectedState) {
    ActivityState state = activeActivity.sampleActivityStatus().getState();
    if (!expectedState.equals(state)) {
      throw new InteractiveSpacesException(String.format("Activity in improper state, was %s expecting %s", state,
          expectedState));
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
        activeActivity.shutdown();
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
    return controller.getSpaceEnvironment().getLog();
  }
}
