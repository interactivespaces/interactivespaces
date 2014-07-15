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
   * Configuration parameter to specify if startup order of bundles should be
   * logged.
   */
  public static final String CONFIG_PROPERTY_STARTUP_LOGGING = "interactivespaces.logging.container.startup";

  /**
   * Configuration parameter value to specify if startup order of bundles should
   * be logged.
   */
  public static final String CONFIG_PROPERTY_VALUE_STARTUP_LOGGING = "true";

  /**
   * The argument for saying the container should run with no shell access.
   */
  public static final String ARGS_NOSHELL = "--noshell";

  /**
   * The default OSGi startup level for bundles.
   */
  public static final int STARTUP_LEVEL_DEFAULT = 1;

  /**
   * The OSGi startup level for bundles which should start after most bundles
   * but not the ones which really require everything running.
   */
  public static final int STARTUP_LEVEL_PENULTIMATE = 4;

  /**
   * The OSGi startup level for bundles which should start after everything else
   * is started.
   */
  public static final int STARTUP_LEVEL_LAST = 5;

  /**
   * External packages loaded from the Interactive Spaces system folder.
   */
  public static final String[] PACKAGES_SYSTEM_EXTERNAL = new String[] { "org.apache.commons.logging; version=1.1.1",
      "org.apache.commons.logging.impl; version=1.1.1", "javax.transaction; version=1.1.0",
      "javax.transaction.xa; version=1.1.0", "javax.transaction", "javax.transaction.xa" };

  /**
   * Packages loaded from the Interactive Spaces system folder that are part of
   * Interactive Spaces.
   */
  public static final String[] PACKAGES_SYSTEM_INTERACTIVESPACES = new String[] {
      "interactivespaces.system.core.logging", "interactivespaces.system.core.configuration",
      "interactivespaces.system.core.container" };

  /**
   * The Jar Manifest property that gives the Interactive Spaces version.
   */
  public static final String MANIFEST_PROPERTY_INTERACTIVESPACES_VERSION = "Bundle-Version";

  /**
   * The folder where Interactive Spaces will cache OSGi plugins.
   */
  public static final String FOLDER_PLUGINS_CACHE = "plugins-cache";

  /**
   * Where the OSGi framework launcher can be found.
   */
  public static final String OSGI_FRAMEWORK_LAUNCH_FRAMEWORK_FACTORY =
      "META-INF/services/org.osgi.framework.launch.FrameworkFactory";

  /**
   * The OSGI framework which has been started.
   */
  private Framework framework;

  /**
   * All bundles installed.
   */
  private final Set<Bundle> bundles = new HashSet<Bundle>();

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
    // TODO(keith): Better command line processing
    needShell = !args.contains(ARGS_NOSHELL);

    baseInstallFolder = new File(".").getAbsoluteFile().getParentFile();

    initialBundles = new ArrayList<File>();

    getBootstrapBundleJars(new File(baseInstallFolder, ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP));

    // If no bundle JAR files are in the directory, then exit.
    if (initialBundles.isEmpty()) {
      loggingProvider.getLog().warn("No bootstrap bundles to install.");
    } else {
      setupShutdownHandler();

      setupExceptionHandler();

      try {
        loadStartupFolder();

        createCoreServices(args);

        File environmentFolder = new File(baseInstallFolder, ContainerFilesystemLayout.FOLDER_CONFIG_ENVIRONMENT);
        ExtensionsReader extensionsReader = new ExtensionsReader(loggingProvider.getLog());
        extensionsReader.processExtensionFiles(environmentFolder);

        createFramework(extensionsReader);

        registerCoreServices();

        loadClasses(extensionsReader.getLoadclasses());

        framework.start();

        startBundles(initialBundles);
        frameworkStartLevel.setStartLevel(STARTUP_LEVEL_LAST);

        framework.waitForStop(0);
        System.exit(0);
      } catch (Throwable ex) {
        loggingProvider.getLog().error("Error starting framework", ex);

        System.exit(1);
      }
    }
  }

  /**
   * Load the contents of the startup folder, which contains additional
   * resources for the container.
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
   * Set up the default exception handler.
   */
  private void setupExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        loggingProvider.getLog().error(String.format("Caught uncaught exception from thread %s", t), e);
      }
    });
  }

  /**
   * Create the core services to the base bundle which are platform dependent.
   *
   * @param args
   *          the list of command line arguments
   */
  public void createCoreServices(List<String> args) {
    loggingProvider = new Log4jLoggingProvider();
    loggingProvider.configure(baseInstallFolder);

    configurationProvider = new FileConfigurationProvider(baseInstallFolder);
    configurationProvider.load();

    containerCustomizerProvider = new SimpleContainerCustomizerProvider(args, true);
  }

  /**
   * Register all bootstrap core services with the container.
   */
  public void registerCoreServices() {
    rootBundleContext.registerService(LoggingProvider.class.getName(), loggingProvider, null);
    rootBundleContext.registerService(ConfigurationProvider.class.getName(), configurationProvider, null);
    rootBundleContext.registerService(ContainerCustomizerProvider.class.getName(), containerCustomizerProvider, null);
  }

  /**
   * Start all bundles.
   *
   * @param jars
   *          the jars to start as OSGi bundles
   *
   * @throws BundleException
   *           something happened while starting bundles that could not be
   *           recovered from
   */
  protected void startBundles(List<File> jars) throws BundleException {
    for (File bundleFile : jars) {
      String bundleUri = bundleFile.getAbsoluteFile().toURI().toString();

      try {
        Bundle bundle = rootBundleContext.installBundle(bundleUri);

        String symbolicName = bundle.getSymbolicName();
        if (symbolicName != null) {
          int startLevel = STARTUP_LEVEL_DEFAULT;
          if (symbolicName.equals("interactivespaces.master.webapp")) {
            startLevel = STARTUP_LEVEL_LAST;
          } else if (symbolicName.equals("interactivespaces.master")) {
            startLevel = STARTUP_LEVEL_PENULTIMATE;
          }

          if (startLevel != STARTUP_LEVEL_DEFAULT) {
            bundle.adapt(BundleStartLevel.class).setStartLevel(startLevel);
          }

          bundles.add(bundle);
        } else {
          logBadBundle(bundleUri);
        }
      } catch (Exception e) {
        logBadBundle(bundleUri);
      }
    }

    // Start all installed non-fragment bundles.
    for (final Bundle bundle : bundles) {
      if (!isFragment(bundle)) {
        // TODO(keith): See if way to start up shell from property
        // since we may want it for remote access.
        String symbolicName = bundle.getSymbolicName();
        if (symbolicName.equals("org.apache.felix.gogo.shell") && !needShell) {
          continue;
        }

        startBundle(bundle);
      }
    }
  }

  /**
   * Log that we had a bad bundle.
   *
   * @param bundleUri
   *          URI for the bundle
   */
  private void logBadBundle(String bundleUri) {
    loggingProvider.getLog().error(
        String.format("Bundle %s is not an OSGi bundle, skipping during Interactive Spaces startup", bundleUri));
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
    Map<String, String> m = new HashMap<String, String>();

    m.put(Constants.FRAMEWORK_STORAGE_CLEAN, "onFirstInit");

    String delegations = getClassloaderDelegations();
    if (delegations != null) {
      loggingProvider.getLog().info(String.format("Delegations %s", delegations));
      m.put(Constants.FRAMEWORK_BOOTDELEGATION, delegations);
    }

    List<String> extraPackages = new ArrayList<String>();
    for (String pckage : PACKAGES_SYSTEM_EXTERNAL) {
      extraPackages.add(pckage);
    }
    for (String pckage : PACKAGES_SYSTEM_INTERACTIVESPACES) {
      extraPackages.add(pckage);
    }

    extraPackages.addAll(extensionsReader.getPackages());

    loadLibraries(extensionsReader.getLoadlibraries());

    StringBuilder packages = new StringBuilder();
    String separator = "";
    for (String extraPackage : extraPackages) {
      packages.append(separator).append(extraPackage);
      separator = ", ";
    }

    m.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, packages.toString());

    m.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_BASE_INSTALL_DIR, baseInstallFolder.getAbsolutePath());

    m.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_VERSION, getInteractiveSpacesVersion());

    m.putAll(configurationProvider.getInitialConfiguration());

    File file = new File(baseInstallFolder, FOLDER_PLUGINS_CACHE);
    m.put(Constants.FRAMEWORK_STORAGE, file.getCanonicalPath());

    framework = getFrameworkFactory().newFramework(m);
    frameworkStartLevel = framework.adapt(FrameworkStartLevel.class);

    framework.init();
    rootBundleContext = framework.getBundleContext();

    if (CONFIG_PROPERTY_VALUE_STARTUP_LOGGING.equals(m.get(CONFIG_PROPERTY_STARTUP_LOGGING))) {
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
   * Load a collection of libraries.
   *
   * @param libraries
   *          the libraries to load
   */
  private void loadLibraries(List<String> libraries) {
    for (String library : libraries) {
      loggingProvider.getLog().info(String.format("Loading system library %s", library));
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
      loggingProvider.getLog().info(String.format("Loading class %s", className));
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
   * Set up a shutdown hook which will stop the framework when the VM shuts
   * down.
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
   * Simple method to parse META-INF/services file for framework factory.
   * Currently, it assumes the first non-commented line is the class nodeName of
   * the framework factory implementation.
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
          if (!s.isEmpty() && s.charAt(0) != '#') {
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
   * Get the list of packages which must be delegated to the boot classloader.
   *
   * <p>
   * The bootloader loads all classes in the java install. This covers things
   * like the javax classes which are not automatically exposed through the OSGi
   * bundle classloaders.
   *
   * @return a properly formated string for the delegation classpath, or
   *         {@code null} if there are no packages to be delegated
   */
  private String getClassloaderDelegations() {
    File delegation = new File(baseInstallFolder, "lib/system/java/delegations.conf");
    if (delegation.exists()) {

      StringBuilder builder = new StringBuilder();
      String separator = "";

      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(delegation));

        String line;
        while ((line = reader.readLine()) != null) {
          if (!line.trim().isEmpty()) {
            builder.append(separator).append(line);
            separator = ",";
          }
        }

        return builder.toString();
      } catch (Exception e) {
        e.printStackTrace();
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

    return null;
  }

  /**
   * Get the Interactive Spaces version from the JAR manifest.
   *
   * @return The interactive spaces version
   */
  private String getInteractiveSpacesVersion() {
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
