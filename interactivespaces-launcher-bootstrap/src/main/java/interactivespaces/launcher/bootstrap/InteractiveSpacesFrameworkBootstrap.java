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

package interactivespaces.launcher.bootstrap;

import interactivespaces.system.core.configuration.ConfigurationProvider;
import interactivespaces.system.core.configuration.CoreConfiguration;
import interactivespaces.system.core.container.ContainerCustomizerProvider;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.system.core.container.InteractiveSpacesStartLevel;
import interactivespaces.system.core.container.SimpleContainerCustomizerProvider;
import interactivespaces.system.core.logging.LoggingProvider;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * The boostrapper for Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesFrameworkBootstrap {

  /**
   * Configuration parameter to specify if startup order of bundles should be logged.
   */
  public static final String CONFIG_PROPERTY_STARTUP_LOGGING = "interactivespaces.logging.container.startup";

  /**
   * Configuration parameter value to specify if startup order of bundles should be logged.
   */
  public static final String CONFIG_PROPERTY_VALUE_STARTUP_LOGGING = "true";

  /**
   * The argument for saying the container should run with no shell access.
   */
  public static final String ARGS_NOSHELL = "--noshell";

  /**
   * Command line argument prefix for specifying a specific runtime path. This should match the value of
   * {@code InteractiveSpacesLauncher.COMMAND_LINE_RUNTIME_PREFIX}, but can't be a shared variable because of package
   * dependency considerations.
   */
  public static final String ARGS_PREFIX_RUNTIME = "--runtime=";

  /**
   * Command line argument prefix for specifying a specific config path. This should match the value of
   * {@code InteractiveSpacesLauncher.COMMAND_LINE_CONFIG_PREFIX}, but can't be a shared variable because of package
   * dependency considerations.
   */
  public static final String ARGS_PREFIX_CONFIG = "--config=";

  /**
   * Command line argument prefix for adding in extra bootstrap folders.
   */
  public static final String ARGS_PREFIX_BOOTSTRAP = "--bootstrap=";

  /**
   * The OSGi wild card for exporting nested packages.
   */
  public static final String NESTED_PACKAGE_WILDCARD = ".*";

  /**
   * Comment character for delegations.conf.
   */
  public static final String DELEGATIONS_CONF_FILE_COMMENT = "#";

  /**
   * The comment character used in files found in meta-inf, at least for OSGi framework factory.
   */
  public static final char META_INF_COMMENT_CHARACTER = '#';

  /**
   * The location of the delegations.conf file relative to the IS install.
   */
  public static final String LOCATION_DELEGATIONS_CONF = "lib/system/java/delegations.conf";

  /**
   * The bundle symbolic name for the OSGi shell being used.
   */
  public static final String BUNDLE_SYMBOLIC_NAME_OSGI_SHELL = "org.apache.felix.gogo.shell";

  /**
   * External packages loaded from the Interactive Spaces system folder that must be exposed for things to work.
   *
   * <p>
   * These packages are critical, IS is crippled without them which is why they are here in the code.
   */
  public static final String[] PACKAGES_SYSTEM_EXTERNAL = new String[] { "org.apache.commons.logging; version=1.1.1",
      "org.apache.commons.logging.impl; version=1.1.1", "javax.transaction; version=1.1.0",
      "javax.transaction.xa; version=1.1.0", "javax.transaction", "javax.transaction.xa" };

  /**
   * The interface describing the container customizer provider.
   */
  public static final Class<ContainerCustomizerProvider> CONTAINER_COSTUMIZER_PROVIDER_INTERFACE =
      ContainerCustomizerProvider.class;

  /**
   * The interface describing the configuration provider.
   */
  public static final Class<ConfigurationProvider> CONFIGURATION_PROVIDER_INTERFACE = ConfigurationProvider.class;

  /**
   * The interface describing the logging provider.
   */
  public static final Class<LoggingProvider> LOGGING_PROVIDER_INTERFACE = LoggingProvider.class;

  /**
   * Packages loaded from the Interactive Spaces system folder that are part of Interactive Spaces.
   */
  public static final String[] PACKAGES_SYSTEM_INTERACTIVESPACES = new String[] {
      LOGGING_PROVIDER_INTERFACE.getPackage().getName(), CONFIGURATION_PROVIDER_INTERFACE.getPackage().getName(),
      CONTAINER_COSTUMIZER_PROVIDER_INTERFACE.getPackage().getName() };

  /**
   * The Jar Manifest property that gives the Interactive Spaces version.
   */
  public static final String MANIFEST_PROPERTY_INTERACTIVESPACES_VERSION = "Bundle-Version";

  /**
   * The folder where Interactive Spaces will cache OSGi plugins. This is relative to the run folder.
   */
  public static final String FOLDER_PLUGINS_CACHE = "plugins-cache";

  /**
   * Where the OSGi framework launcher can be found.
   */
  public static final String OSGI_FRAMEWORK_LAUNCH_FRAMEWORK_FACTORY =
      "META-INF/services/org.osgi.framework.launch.FrameworkFactory";

  /**
   * Bundle manifest header indicating the start level to use.
   */
  public static final String BUNDLE_MANIFEST_START_LEVEL_HEADER = "InteractiveSpaces-StartLevel";

  /**
   * Command line argument prefix string for configuration value definitions.
   */
  public static final String COMMAND_LINE_VALUE_DEFINITION_PREFIX = "-D";

  /**
   * Command line configuration value key/value split specification.
   */
  public static final String COMMAND_LINE_VALUE_DEFINITION_SPLIT = "=";

  /**
   * Environment variable that indicates the home install directory for interactive spaces.
   */
  private static final String INTERACTIVESPACES_HOME_ENVIRONMENT_KEY = "INTERACTIVESPACES_HOME";

  /**
   * Default directory for the home directory relative to install of this component.
   */
  private static final String INTERACTIVESPACES_HOME_DEFAULT_DIR = "..";

  /**
   * The name of the System property for the current operating system.
   */
  private static final String SYSTEM_PROPERTY_OS_NAME = "os.name";

  /**
   * The value of the System property value for the current operating system when it is Linux.
   */
  private static final String SYSTEM_PROPERTY_OS_VALUE_LINUX = "Linux";

  /**
   * The value of the System property value for the current operating system when it is OSX (Mac).
   */
  private static final String SYSTEM_PROPERTY_OS_VALUE_OSX = "Mac OS X";

  /**
   * The beginning of the System property value for the current operating system when it is Windows.
   */
  private static final String SYSTEM_PROPERTY_OS_PREFIX_WINDOWS = "Windows";

  /**
   * The OSGI framework which has been started.
   */
  private Framework framework;

  /**
   * A map of bundle symbolic names to start levels for those bundles.
   */
  private Map<String, InteractiveSpacesStartLevel> bundleStartLevels =
      new HashMap<String, InteractiveSpacesStartLevel>();

  /**
   * Extra folders to be added to the bootstrap from the commandline.
   */
  private final List<File> extraBootstrapFolders = new ArrayList<File>();

  /**
   * All bundles installed.
   */
  private final Set<Bundle> installedBundles = new HashSet<Bundle>();

  /**
   * The initial set of bundles to load.
   */
  private List<File> initialBundles;

  /**
   * Whether or not the OSGi shell is needed.
   */
  private boolean needShell = true;

  /**
   * Logging provider for the container.
   */
  private Log4jLoggingProvider loggingProvider;

  /**
   * The configuration provider for the container.
   */
  private FileConfigurationProvider configurationProvider;

  /**
   * The container customizer provider for the container.
   */
  private SimpleContainerCustomizerProvider containerCustomizerProvider;

  /**
   * The base install folder for Interactive Spaces.
   */
  private File baseInstallFolder;

  /**
   * The root runtime directory for this container. May be the same as the base install folder, but can be independently
   * controlled to allow for multiple runtime instances.
   */
  private File runtimeFolder;

  /**
   * The root config directory for this container.
   */
  private File configFolder;

  /**
   * The OSGi bundle context for the OSGi framework bundle.
   */
  private BundleContext rootBundleContext;

  /**
   * The start level for the OSGi framework.
   */
  private FrameworkStartLevel frameworkStartLevel;

  /**
   * Boot the framework.
   *
   * @param args
   *          the arguments to be passed to the bootstrap
   */
  public void boot(List<String> args) {

    baseInstallFolder = new File(".").getAbsoluteFile().getParentFile();

    // Set default values for various directories.
    runtimeFolder = baseInstallFolder;
    configFolder = new File(baseInstallFolder, ContainerFilesystemLayout.FOLDER_DEFAULT_CONFIG);

    processCommandLineArgs(args);

    initialBundles = new ArrayList<File>();

    initializeBundleStartLevels();

    getBootstrapBundleJars(new File(baseInstallFolder, ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP));

    try {
      setupShutdownHandler();

      setupExceptionHandler();

      loadStartupFolder();
      loadExtraBootstrapFolders();

      createCoreServices(args);

      if (initialBundles.isEmpty()) {
        throw new RuntimeException("No bootstrap bundles to install.");
      }

      File environmentFolder = new File(configFolder, ContainerFilesystemLayout.FOLDER_CONFIG_ENVIRONMENT);
      ExtensionsReader extensionsReader = new ExtensionsReader(loggingProvider.getLog());
      extensionsReader.processExtensionFiles(environmentFolder);

      createFramework(extensionsReader);

      registerCoreServices();

      loadLibraries(extensionsReader.getLoadLibraries());
      loadClasses(extensionsReader.getLoadClasses());

      addContainerPathBundles(extensionsReader.getContainerPath());

      framework.start();

      startBundles();
      frameworkStartLevel.setStartLevel(InteractiveSpacesStartLevel.STARTUP_LEVEL_LAST.getStartLevel());

      framework.waitForStop(0);
      System.exit(0);
    } catch (Throwable ex) {
      if (loggingProvider != null && loggingProvider.getLog() != null) {
        loggingProvider.getLog().error("Error starting framework", ex);
      } else {
        System.err.println("Error starting framework");
        ex.printStackTrace();
      }
      System.exit(1);
    }
  }

  /**
   * Populate the map for bundle start levels.
   */
  private void initializeBundleStartLevels() {
    // TODO(keith): Once Spring is removed, see if this is even necessary.
    bundleStartLevels.put("interactivespaces.master.webapp", InteractiveSpacesStartLevel.STARTUP_LEVEL_LAST);
    bundleStartLevels.put("interactivespaces.master", InteractiveSpacesStartLevel.STARTUP_LEVEL_PENULTIMATE);
  }

  /**
   * Process the command line arguments for this container.
   *
   * @param args
   *          the list of command line arguments
   */
  private void processCommandLineArgs(List<String> args) {
    for (String arg : args) {
      if (arg.equals(ARGS_NOSHELL)) {
        needShell = false;
      } else if (arg.startsWith(ARGS_PREFIX_RUNTIME)) {
        runtimeFolder = new File(arg.substring(ARGS_PREFIX_RUNTIME.length()));
      } else if (arg.startsWith(ARGS_PREFIX_CONFIG)) {
        configFolder = new File(arg.substring(ARGS_PREFIX_CONFIG.length()));
      } else if (arg.startsWith(ARGS_PREFIX_BOOTSTRAP)) {
        extraBootstrapFolders.add(new File(arg.substring(ARGS_PREFIX_BOOTSTRAP.length())));
      }
    }
  }

  /**
   * Load the contents of the startup folder, which contains additional resources for the container.
   *
   * @throws Exception
   *           could not create the startup folder or load it
   */
  private void loadStartupFolder() throws Exception {
    File startupFolder = new File(baseInstallFolder, ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP);
    if (startupFolder.exists()) {
      if (startupFolder.isFile()) {
        throw new Exception(String.format("User bootstrap folder %s is a file not a folder.",
            startupFolder.getAbsolutePath()));
      }
    } else if (!startupFolder.mkdirs()) {
      throw new Exception(String.format("Cannot create user bootstrap folder %s.", startupFolder.getAbsolutePath()));
    }

    getBootstrapBundleJars(startupFolder);
  }

  /**
   * Add in all extra bootstrap folders.
   */
  private void loadExtraBootstrapFolders() {
    for (File extraBootstrapFolder : extraBootstrapFolders) {
      // Only add ones that are directories. This also means they exist.
      if (extraBootstrapFolder.isDirectory()) {
        getBootstrapBundleJars(extraBootstrapFolder);
      }
    }
  }

  /**
   * Set up the default exception handler.
   */
  private void setupExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        loggingProvider.getLog().error(String.format("Caught previously uncaught exception from thread %s", t), e);
      }
    });
  }

  /**
   * Add configuration items that can be automatically determined to the given FileConfigurationProvider.
   *
   * @param configurationProvider
   *          the FileConfigurationProvider to inject key, value pairs of config data
   */
  private void addAutomaticConfiguration(FileConfigurationProvider configurationProvider) {
    // Calculate the proper home directory for this install of interactive spaces.
    String isHomeEnvPath = System.getenv(INTERACTIVESPACES_HOME_ENVIRONMENT_KEY);
    File isHomeDir =
        isHomeEnvPath != null ? new File(isHomeEnvPath) : new File(baseInstallFolder,
            INTERACTIVESPACES_HOME_DEFAULT_DIR);
    configurationProvider.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_HOME, isHomeDir.getAbsolutePath());

    String platformOs = System.getProperty(SYSTEM_PROPERTY_OS_NAME);
    // Convert the Os string into the format Interactive Spaces is expecting.
    if (platformOs.equals(SYSTEM_PROPERTY_OS_VALUE_LINUX)) {
      platformOs = CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_LINUX;
    } else if (platformOs.equals(SYSTEM_PROPERTY_OS_VALUE_OSX)) {
      platformOs = CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_OSX;
    } else if (platformOs.startsWith(SYSTEM_PROPERTY_OS_PREFIX_WINDOWS)) {
      platformOs = CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_WINDOWS;
    } else {
      loggingProvider.getLog().warn(
          String
              .format("Unsupported operating system: %s. Native applications will not function properly.", platformOs));
      platformOs = CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_UNKNOWN;
    }

    configurationProvider.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_PLATFORM_OS, platformOs);
  }

  /**
   * Add command line configuration parameters to the given system configuration.
   *
   * @param configurationProvider
   *          the FileConfigurationProvider which will have the command line configurations added
   * @param args
   *          the command line arguments
   *
   * @return the command line arguments minus the configuration parameters
   */
  private List<String> addCommandLineConfigurationParameters(FileConfigurationProvider configurationProvider,
      List<String> args) {
    List<String> newArgs = new ArrayList<String>();

    for (String arg : args) {
      if (arg.startsWith(COMMAND_LINE_VALUE_DEFINITION_PREFIX)) {
        String[] parts = arg.split(COMMAND_LINE_VALUE_DEFINITION_SPLIT, 2);
        configurationProvider.put(parts[0].substring(COMMAND_LINE_VALUE_DEFINITION_PREFIX.length()),
            parts.length > 1 ? parts[1] : "");
      } else {
        newArgs.add(arg);
      }
    }

    return newArgs;
  }

  /**
   * Create a FileConfigurationProvider, assign it to configurationProvider, inject automatic configuration, and then
   * load configuration from config files.
   *
   * @param args
   *          the command line arguments
   *
   * @return the command line arguments minus configuration parameters
   */
  private List<String> initializeFileConfigurationProvider(List<String> args) {
    configurationProvider = new FileConfigurationProvider(baseInstallFolder, configFolder, loggingProvider.getLog());

    // The order is important, as it determines which source of configuration parameters overrides the other sources.
    // Configuration sources that are invoked later override those that are done sooner. The order is:
    // 1) automatic configuration
    // 2) file configuration
    // 3) command-line configuration
    addAutomaticConfiguration(configurationProvider);
    configurationProvider.load();
    args = addCommandLineConfigurationParameters(configurationProvider, args);

    return args;
  }

  /**
   * Create the core services to the base bundle which are platform dependent.
   *
   * @param args
   *          the list of command line arguments
   */
  private void createCoreServices(List<String> args) {
    loggingProvider = new Log4jLoggingProvider();
    loggingProvider.configure(runtimeFolder, configFolder);

    args = initializeFileConfigurationProvider(args);

    containerCustomizerProvider = new SimpleContainerCustomizerProvider(args, true);
  }

  /**
   * Register all bootstrap core services with the container.
   */
  public void registerCoreServices() {
    rootBundleContext.registerService(LOGGING_PROVIDER_INTERFACE.getName(), loggingProvider, null);
    rootBundleContext.registerService(CONFIGURATION_PROVIDER_INTERFACE.getName(), configurationProvider, null);
    rootBundleContext.registerService(CONTAINER_COSTUMIZER_PROVIDER_INTERFACE.getName(), containerCustomizerProvider,
        null);
  }

  /**
   * Start all bundles.
   *
   * @throws BundleException
   *           something happened while starting bundles that could not be recovered from
   */
  private void startBundles() throws BundleException {
    for (File bundleFile : initialBundles) {
      String bundleUri = bundleFile.getAbsoluteFile().toURI().toString();

      try {
        Bundle bundle = rootBundleContext.installBundle(bundleUri);

        String symbolicName = bundle.getSymbolicName();
        if (symbolicName != null) {
          InteractiveSpacesStartLevel startLevel = bundleStartLevels.get(symbolicName);
          if (startLevel == null) {
            String interactiveSpacesStartLevel = bundle.getHeaders().get(BUNDLE_MANIFEST_START_LEVEL_HEADER);
            if (interactiveSpacesStartLevel != null) {
              startLevel = InteractiveSpacesStartLevel.valueOf(interactiveSpacesStartLevel);
            } else {
              startLevel = InteractiveSpacesStartLevel.STARTUP_LEVEL_DEFAULT;
            }
          }

          if (startLevel != InteractiveSpacesStartLevel.STARTUP_LEVEL_DEFAULT) {
            bundle.adapt(BundleStartLevel.class).setStartLevel(startLevel.getStartLevel());
          }

          installedBundles.add(bundle);
        } else {
          logBadBundle(bundleUri, new Exception("No symbolic name in bundle"));
        }
      } catch (Exception e) {
        logBadBundle(bundleUri, e);
      }
    }

    // Start all installed non-fragment bundles.
    for (Bundle bundle : installedBundles) {
      if (isFragment(bundle)) {
        continue;
      }
      // TODO(keith): See if way to start up shell from property
      // since we may want it for remote access.
      String symbolicName = bundle.getSymbolicName();
      if (symbolicName.equals(BUNDLE_SYMBOLIC_NAME_OSGI_SHELL) && !needShell) {
        continue;
      }

      startBundle(bundle);
    }
  }

  /**
   * Log that we had a bad bundle.
   *
   * @param bundleUri
   *          URI for the bundle
   * @param e
   *          triggering exception
   */
  private void logBadBundle(String bundleUri, Exception e) {
    loggingProvider.getLog().error(
        String.format("Bundle %s is not an OSGi bundle, skipping during Interactive Spaces startup", bundleUri), e);
  }

  /**
   * Start a particular bundle.
   *
   * @param bundle
   *          the bundle to start
   */
  private void startBundle(Bundle bundle) {
    try {
      bundle.start();
    } catch (Exception e) {
      loggingProvider.getLog().error(String.format("Error while starting bundle %s", bundle.getLocation()), e);
    }
  }

  /**
   * Create, configure, and start the OSGi framework instance.
   *
   * @param extensionsReader
   *          the reader for extensions files
   *
   * @throws Exception
   *           unable to create and/or start the framework
   */
  private void createFramework(ExtensionsReader extensionsReader) throws Exception {
    Map<String, String> frameworkConfig = new HashMap<String, String>();

    frameworkConfig.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

    // The bootloader delegation loads all classes in the java install. This covers things like the javax classes which
    // are not automatically exposed through the OSGi bundle classloaders.
    List<String> bootDelegationPackages = new ArrayList<String>();

    // Extra system packages are not found in the boot classloader but need to be exported by the system bundle
    // classloader.
    List<String> extraSystemPackages = new ArrayList<String>();

    bootDelegationPackages.addAll(extensionsReader.getBootPackages());

    processDelegationsFile(bootDelegationPackages, extraSystemPackages);
    configureBootDelegationPackages(frameworkConfig, bootDelegationPackages);

    // Get the initial packages into the extra system packages.
    Collections.addAll(extraSystemPackages, PACKAGES_SYSTEM_EXTERNAL);
    Collections.addAll(extraSystemPackages, PACKAGES_SYSTEM_INTERACTIVESPACES);
    extraSystemPackages.addAll(extensionsReader.getPackages());
    configureExtraSystemPackages(frameworkConfig, extraSystemPackages);

    frameworkConfig.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_BASE_INSTALL_DIR,
        baseInstallFolder.getAbsolutePath());

    frameworkConfig.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_RUNTIME_DIR, runtimeFolder.getAbsolutePath());

    frameworkConfig.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_VERSION, getInteractiveSpacesVersion());

    frameworkConfig.putAll(configurationProvider.getInitialConfiguration());

    File pluginsCacheFolder =
        new File(new File(runtimeFolder, ContainerFilesystemLayout.FOLDER_INTERACTIVESPACES_RUN), FOLDER_PLUGINS_CACHE);
    frameworkConfig.put(Constants.FRAMEWORK_STORAGE, pluginsCacheFolder.getCanonicalPath());

    framework = getFrameworkFactory().newFramework(frameworkConfig);
    frameworkStartLevel = framework.adapt(FrameworkStartLevel.class);

    framework.init();
    rootBundleContext = framework.getBundleContext();

    if (CONFIG_PROPERTY_VALUE_STARTUP_LOGGING.equals(frameworkConfig.get(CONFIG_PROPERTY_STARTUP_LOGGING))) {
      rootBundleContext.addBundleListener(new SynchronousBundleListener() {
        @Override
        public void bundleChanged(BundleEvent event) {
          try {
            if (event.getType() == BundleEvent.STARTED) {
              Bundle bundle = event.getBundle();
              loggingProvider.getLog().info(
                  String.format("Bundle %s:%s started with start level %d", bundle.getSymbolicName(),
                      bundle.getVersion(), bundle.adapt(BundleStartLevel.class).getStartLevel()));
            }
          } catch (Exception e) {
            loggingProvider.getLog().error("Exception while responding to bundle change events", e);
          }
        }
      });
    }
  }

  /**
   * Configure the extra system packages for the framework.
   *
   * @param frameworkConfig
   *          the framework configuration
   * @param extraPackages
   *          the list of packages to be used as extra system packages
   */
  private void configureExtraSystemPackages(Map<String, String> frameworkConfig, List<String> extraPackages) {
    StringBuilder packages = new StringBuilder();
    String separator = "";
    for (String extraPackage : extraPackages) {
      packages.append(separator).append(extraPackage);
      separator = ", ";
    }

    loggingProvider.getLog().debug(String.format("Extra packages: %s", packages));
    frameworkConfig.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, packages.toString());
  }

  /**
   * Configure the extra system packages for the framework.
   *
   * @param frameworkConfig
   *          the framework configuration
   * @param bootPackages
   *          the list of packages to be used as bootloader packages
   */
  private void configureBootDelegationPackages(Map<String, String> frameworkConfig, List<String> bootPackages) {
    if (bootPackages.isEmpty()) {
      return;
    }

    StringBuilder packages = new StringBuilder();
    String separator = "";
    for (String bootPackage : bootPackages) {
      packages.append(separator).append(bootPackage);
      if (!bootPackage.endsWith(NESTED_PACKAGE_WILDCARD)) {
        packages.append(NESTED_PACKAGE_WILDCARD);
      }
      separator = ", ";
    }

    loggingProvider.getLog().debug(String.format("Boot delegations: %s", packages));
    frameworkConfig.put(Constants.FRAMEWORK_BOOTDELEGATION, packages.toString());
  }

  /**
   * Load a collection of libraries.
   *
   * @param libraries
   *          the libraries to load
   */
  private void loadLibraries(List<String> libraries) {
    for (String library : libraries) {
      loggingProvider.getLog().debug(String.format("Loading system library %s", library));
      System.loadLibrary(library);
    }
  }

  /**
   * Load a collection of classes.
   *
   * @param classes
   *          the classes to load
   */
  private void loadClasses(List<String> classes) {
    for (String className : classes) {
      loggingProvider.getLog().debug(String.format("Loading class %s", className));
      try {
        Class<?> clazz = InteractiveSpacesFrameworkBootstrap.class.getClassLoader().loadClass(className);
        Object obj = clazz.newInstance();
        rootBundleContext.registerService(obj.getClass().getName(), obj, null);
      } catch (Exception e) {
        loggingProvider.getLog().error(String.format("Error while creating class %s", className), e);
      }
    }
  }

  /**
   * Add in all container path entries from the extensions files to the initial bundles list as long as the files
   * actually exist.
   *
   * @param containerPath
   *          the elements to be on the container classpath.
   */
  private void addContainerPathBundles(List<String> containerPath) {
    for (String containerBundlePath : containerPath) {
      File bundleFile = new File(containerBundlePath);
      if (bundleFile.isFile()) {
        initialBundles.add(bundleFile);
      } else {
        loggingProvider.getLog().warn(String.format("Container path file %s is not a file", containerBundlePath));
      }
    }
  }

  /**
   * Set up a shutdown hook which will stop the framework when the VM shuts down.
   */
  private void setupShutdownHandler() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          if (framework != null) {
            framework.stop();
            framework.waitForStop(0);
          }
        } catch (Exception ex) {
          loggingProvider.getLog().error("Error stopping framework", ex);
        }
      }
    });
  }

  /**
   * Get all jars from the bootstrap folder.
   *
   * @param folder
   *          the folder containing the bootstrap bundles
   */
  private void getBootstrapBundleJars(File folder) {
    // Look in the specified bundle directory to create a list
    // of all JAR files to install.
    File[] files = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        String filename = name.toLowerCase();
        return filename.endsWith(".jar") || filename.endsWith(".war");
      }
    });

    for (File f : files) {
      initialBundles.add(f.getAbsoluteFile());
    }
  }

  /**
   * Is the bundle a fragment host?
   *
   * @param bundle
   *          the bundle to check
   *
   * @return {@code true} if the bundle is a fragment host
   */
  private boolean isFragment(Bundle bundle) {
    return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
  }

  /**
   * Simple method to parse META-INF/services file for framework factory. Currently, it assumes the first non-commented
   * line is the class nodeName of the framework factory implementation.
   *
   * @return the created <tt>FrameworkFactory</tt> instance
   *
   * @throws Exception
   *           if any errors occur.
   **/
  private FrameworkFactory getFrameworkFactory() throws Exception {
    // using the ServiceLoader to get a factory.
    ClassLoader classLoader = InteractiveSpacesFrameworkBootstrap.class.getClassLoader();
    URL url = classLoader.getResource(OSGI_FRAMEWORK_LAUNCH_FRAMEWORK_FACTORY);
    if (url != null) {
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      try {
        for (String s = br.readLine(); s != null; s = br.readLine()) {
          // Try to load first non-empty, non-commented line.
          s = s.trim();
          if (!s.isEmpty() && s.charAt(0) != META_INF_COMMENT_CHARACTER) {
            return (FrameworkFactory) classLoader.loadClass(s).newInstance();
          }
        }
      } finally {
        if (br != null) {
          br.close();
        }
      }
    }

    throw new Exception("Could not find framework factory.");
  }

  /**
   * Get the list of boot delegation packages and extra system packages from the delegations file.
   *
   * <p>
   * The bootloader loads all classes in the java install. This covers things like the javax classes which are not
   * automatically exposed through the OSGi bundle classloaders.
   *
   * @param bootDelegationPackages
   *          packages to be exported in the boot delegation
   * @param extraSystemPackages
   *          packages to be exported in the exta system classes
   */
  private void processDelegationsFile(List<String> bootDelegationPackages, List<String> extraSystemPackages) {
    File delegationFile = new File(baseInstallFolder, LOCATION_DELEGATIONS_CONF);
    if (!delegationFile.exists()) {
      return;
    }
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(delegationFile));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          if (line.startsWith(DELEGATIONS_CONF_FILE_COMMENT)) {
            continue;
          } else if (line.startsWith(ExtensionsReader.EXTENSION_FILE_KEYWORD_PACKAGE)) {
            extraSystemPackages.add(line.substring(ExtensionsReader.EXTENSION_FILE_KEYWORD_PACKAGE.length()).trim());
          } else if (line.startsWith(ExtensionsReader.EXTENSION_FILE_KEYWORD_PACKAGE_BOOT)) {
            bootDelegationPackages.add(line.substring(ExtensionsReader.EXTENSION_FILE_KEYWORD_PACKAGE_BOOT.length())
                .trim());
          } else {
            // The default is to be a boot delegation package
            bootDelegationPackages.add(line);
          }
        }
      }

    } catch (Exception e) {
      loggingProvider.getLog().error(
          String.format("Error file processing delegations file %s", delegationFile.getAbsolutePath()), e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // Don't care. Closing.
        }
      }
    }
  }

  /**
   * Get the Interactive Spaces version from the JAR manifest.
   *
   * @return The interactive spaces version
   */
  private String getInteractiveSpacesVersion() {
    // This little lovely line gives us the name of the jar that gave the class we are looking at.
    String classContainer = getClass().getProtectionDomain().getCodeSource().getLocation().toString();

    InputStream in = null;
    try {
      URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
      in = manifestUrl.openStream();
      Manifest manifest = new Manifest(in);
      Attributes attributes = manifest.getMainAttributes();

      return attributes.getValue(MANIFEST_PROPERTY_INTERACTIVESPACES_VERSION);
    } catch (IOException ex) {
      return null;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          // Don't care
        }
      }
    }
  }
}
