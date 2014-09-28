package interactivespaces.controller.standalone;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityControllerStartupType;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.activity.component.route.MessageRouterActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.ConfigurationStorageManager;
import interactivespaces.configuration.SimplePropertyFileConfigurationStorageManager;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.configuration.SimpleLiveActivityConfiguration;
import interactivespaces.controller.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.web.WebActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivity;
import interactivespaces.controller.client.node.InternalActivityFilesystem;
import interactivespaces.controller.client.node.SimpleActivityFilesystem;
import interactivespaces.controller.client.node.internal.SimpleActiveControllerActivityFactory;
import interactivespaces.controller.domain.ActivityInstallationStatus;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.controller.standalone.messaging.StandaloneMessageRouter;
import interactivespaces.controller.standalone.stubs.StandaloneSpaceController;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.evaluation.ExpressionEvaluator;
import interactivespaces.evaluation.SimpleExpressionEvaluator;
import interactivespaces.resource.Version;
import interactivespaces.system.BasicInteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.LocalTimeProvider;
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
import java.util.concurrent.Executors;

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
  private static final String ACTIVITY_SPECIFIC_CONFIG_FILE_NAME = "standalone.conf";

  /**
   * Config filename to use for activity specific configuration.
   */
  private static final String LOCAL_CONFIG_FILE_NAME = "local.conf";

  /**
   * Config filename to use for standalone activities.
   */
  private static final String STANDALONE_CONFIG_FILE_NAME = "standalone.conf";

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
  private final File activityRootDir;

  /**
   * Space controller instance.
   */
  private StandaloneSpaceController controller;

  /**
   * Configuration.
   */
  private LiveActivityConfiguration liveActivityConfiguration;

  /**
   * Active activity under test.
   */
  private ActiveControllerActivity activeActivity;

  /**
   * Activity filesystem for activity.
   */
  private InternalActivityFilesystem activityFilesystem;

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
   * Create a new activity runner.
   *
   * @param activityRootDir
   *          The root directory that contains this activity.
   */
  public StandaloneActivityRunner(File activityRootDir) {
    this.activityRootDir = activityRootDir;
  }

  /**
   * Create a new instance of a standalone space environment.
   *
   * @param configuration
   *          configuration to use for space environment
   *
   * @return the space environment
   */
  public static InteractiveSpacesEnvironment createSpaceEnvironment(Configuration configuration) {
    File baseInstallDir = new File(".");
   // RosOsgiInteractiveSpacesEnvironment environment = new RosOsgiInteractiveSpacesEnvironment();

    //Log4jLoggingProvider loggingProvider = new Log4jLoggingProvider();
    //loggingProvider.configure(baseInstallDir);
//    environment.setLoggingProvider(null);
//    environment.setSystemConfiguration(configuration);
//    environment.setExecutorService(Executors.newScheduledThreadPool(THREAD_POOL_SIZE));
//    environment.setTimeProvider(new LocalTimeProvider());
//    environment.setFilesystem(new BasicInteractiveSpacesFilesystem(baseInstallDir));

    return null; //environment;
  }

  /**
   * Check that the given argument is valid.
   *
   * @param args
   *          argument array
   * @param index
   *          index of needed argument
   * @param message
   *          message to include if there is a problem
   *
   * @return the argument string
   */
  private static String checkArg(String[] args, int index, String message) {
    if (index >= args.length) {
      throw new InteractiveSpacesException("Expecting argument for " + message);
    }
    return args[index];
  }

  /**
   * Get the instance suffix to use for this instance. The suffix returned depends on the number
   * of already running instances.
   *
   * @param rootDir
   *          the root directory that holds the instance locks
   *
   * @return suffix to use for directories and files
   */
  private static String getInstanceSuffix(File rootDir) {
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
   * @param configDir
   *          directory for configuration files
   * @param fileName
   *          the base configuration file name
   * @param instanceSuffix
   *          the suffix to use for this instance
   *
   * @return file to use for local activity configuration
   */
  private static File getInstanceConfigFile(File configDir, String fileName, String instanceSuffix) {
    int breakIndex = fileName.lastIndexOf('.');
    String localConfigFileName = fileName.substring(0, breakIndex)
        + instanceSuffix + fileName.substring(breakIndex);
    return new File(configDir, localConfigFileName);
  }

  /**
   * Main function for running a standalone activity.
   *
   * @param args
   *          command line arguments
   */
  public static void main(String[] args) {
    int argIndex = 0;
    File defaultRunDir = new File(args[argIndex++]);

    String instanceSuffix = getInstanceSuffix(defaultRunDir);

    boolean isPrimaryInstance = instanceSuffix.length() == 0;
    File actualRunDir = isPrimaryInstance ? defaultRunDir : new File(defaultRunDir, "instance" + instanceSuffix);
    FileSupportImpl.INSTANCE.directoryExists(actualRunDir);

    StandaloneActivityRunner standaloneActivityRunner = new StandaloneActivityRunner(actualRunDir);

    while (argIndex < args.length) {
      String cmd = args[argIndex++];
      if (cmd.equals("-p")) {
        String playbackPath = checkArg(args, argIndex++, "trace playback path");
        standaloneActivityRunner.setTracePlaybackPath(playbackPath);
      } else if (cmd.equals("-t")) {
        String checkPath = checkArg(args, argIndex++, "trace check path");
        standaloneActivityRunner.setTraceCheckPath(checkPath);
      } else if (cmd.equals("-s")) {
        String sendPath = checkArg(args, argIndex++, "trace send path");
        standaloneActivityRunner.setTraceSendPath(sendPath);
      } else if (cmd.equals("-c")) {
        File localConfigDir = new File(checkArg(args, argIndex++, "local config dir"));
        standaloneActivityRunner.setLocalConfigFile(
            getInstanceConfigFile(localConfigDir, LOCAL_CONFIG_FILE_NAME, instanceSuffix));
        standaloneActivityRunner.setActivityConfigFile(
            getInstanceConfigFile(localConfigDir, ACTIVITY_SPECIFIC_CONFIG_FILE_NAME, instanceSuffix));
      } else if (cmd.equals("-f")) {
        String traceFilterPath = checkArg(args, argIndex++, "trace filter path");
        standaloneActivityRunner.setTraceFilterPath(traceFilterPath);
      } else {
        throw new SimpleInteractiveSpacesException("Unrecognized argument: " + cmd);
      }
    }

    standaloneActivityRunner.prepareFilesystem(isPrimaryInstance);
    standaloneActivityRunner.makeController();
    standaloneActivityRunner.createActivity();
    standaloneActivityRunner.reportStatus();
    standaloneActivityRunner.startupActivity();
    standaloneActivityRunner.reportStatus();
    standaloneActivityRunner.activateActivity();
    standaloneActivityRunner.reportStatus();
    standaloneActivityRunner.startPlayback();
  }

  /**
   * Actually create the activity.
   */
  public void createActivity() {
    SimpleActiveControllerActivityFactory controllerActivityFactory = getControllerActivityFactory();

    getLog().info("Creating activity " + activityRootDir.getAbsolutePath());
    activeActivity = controllerActivityFactory.newActiveActivity(
        getLiveActivity(), activityFilesystem, liveActivityConfiguration, controller);
  }

  /**
   * Get an activity factor for creating a new activity.
   *
   * @return controller activity factory
   */
  private SimpleActiveControllerActivityFactory getControllerActivityFactory() {
    SimpleActiveControllerActivityFactory controllerActivityFactory = new SimpleActiveControllerActivityFactory();
    controllerActivityFactory.registerActivityWrapperFactory(new NativeActivityWrapperFactory());
    controllerActivityFactory.registerActivityWrapperFactory(new WebActivityWrapperFactory());
    controllerActivityFactory.registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());
    return controllerActivityFactory;
  }

  /**
   * Get a live activity configuration.
   *
   * @return live activity configuration
   */
  private LiveActivityConfiguration getLiveActivityConfiguration() {
    ExpressionEvaluator expressionEvaluator = new SimpleExpressionEvaluator();

    ConfigurationStorageManager systemConfigurationManager = new SimplePropertyFileConfigurationStorageManager(true,
        new File(STANDALONE_CONFIG_FILE_NAME), expressionEvaluator);
    systemConfigurationManager.load();
    addDynamicConfiguration(systemConfigurationManager.getConfiguration());

    ConfigurationStorageManager baseConfigurationManager = new SimplePropertyFileConfigurationStorageManager(true,
        activityFilesystem.getInstallFile(ACTIVITY_CONFIG_FILE_NAME), expressionEvaluator);
    baseConfigurationManager.load();

    ConfigurationStorageManager installedConfigurationManager = new SimplePropertyFileConfigurationStorageManager(false,
        activityConfigFile, expressionEvaluator);
    installedConfigurationManager.load();

    ConfigurationStorageManager localConfigurationManager = new SimplePropertyFileConfigurationStorageManager(false,
        localConfigFile, expressionEvaluator);
    localConfigurationManager.load();
    Map<String, String> collapsedMap = localConfigurationManager.getConfiguration().getCollapsedMap();
    installedConfigurationManager.update((Map) collapsedMap);

    LiveActivityConfiguration configuration = new SimpleLiveActivityConfiguration(baseConfigurationManager,
        installedConfigurationManager, expressionEvaluator, systemConfigurationManager.getConfiguration());
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
  private void makeController() {
    if (controller != null) {
      throw new InteractiveSpacesException("controller already defined");
    }
    activityFilesystem = new SimpleActivityFilesystem(activityRootDir);

    liveActivityConfiguration = getLiveActivityConfiguration();

    InteractiveSpacesEnvironment spaceEnvironment = createSpaceEnvironment(liveActivityConfiguration);
    SimpleNativeActivityRunnerFactory simpleNativeActivityRunnerFactory =
        new SimpleNativeActivityRunnerFactory(spaceEnvironment);

    controller = new StandaloneSpaceController(spaceEnvironment, simpleNativeActivityRunnerFactory);

    controller.startup();

    cecRouter = new StandaloneMessageRouter(this);
    controller.getSpaceEnvironment().setValue(MessageRouterActivityComponent.class.getName(), cecRouter);
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
      throw new InteractiveSpacesException(
          "Could not read activity file description from " + activityFile.getAbsolutePath(), e);
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
    liveActivity.setControllerStartupType(ActivityControllerStartupType.READY);

    return liveActivity;
  }

  /**
   * Report the current status of the activity.
   */
  public void reportStatus() {
    getLog().info("Activity status: " + activeActivity.getActivityStatus());
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
    this.localConfigFile = localConfigFile;
  }

  /**
   * Set the activity config file.
   *
   * @param activityConfigFile
   *          activity config file
   */
  public void setActivityConfigFile(File activityConfigFile) {
    this.activityConfigFile = activityConfigFile;
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
   *
   * @param isPrimaryInstance
   *          indicates if this should be prepared for the primary (1st) instance
   */
  public void prepareFilesystem(boolean isPrimaryInstance) {
    fileSupport.directoryExists(new File(activityRootDir, "tmp"));
    fileSupport.directoryExists(new File(activityRootDir, "log"));
    if (!isPrimaryInstance) {
      // No standard way to do this until Java 7 -- so hack for linux.
      File targetInstallLink = new File(activityRootDir, "install");
      try {
        String command = "/bin/ln -sf ../install " + targetInstallLink.getAbsolutePath();
        System.out.println("Executing: " + command); // Can't use log yet, not defined.
        Runtime.getRuntime().exec(command);
      } catch (IOException e) {
        throw new InteractiveSpacesException("Could not execute link command", e);
      }

      // When running multiple instances, require explicit local config files to
      // make it more obvious what they should be.
      if (!activityConfigFile.exists()) {
        throw new InteractiveSpacesException("Missing local config file " + activityConfigFile.getAbsolutePath());
      }
    }
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
    if (!expectedState.equals(activeActivity.getActivityState())) {
      throw new InteractiveSpacesException("Activity in improper state, expecting " + expectedState.toString());
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
