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
import interactivespaces.system.core.container.SimpleContainerCustomizerProvider;
import interactivespaces.system.core.logging.LoggingProvider;

import org.osgi.service.startlevel.StartLevel;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
   * The Jar Manifest property that gives the Interactive Spaces version.
   */
  private static final String MANIFEST_PROPERTY_INTERACTIVESPACES_VERSION = "Bundle-Version";

  /**
   * The file extension used for files which give container extensions.
   */
  private static final String EXTENSION_FILE_EXTENSION = ".ext";

  /**
   * The keyword header for a package line on an extensions file.
   */
  public static final String EXTENSION_FILE_PACKAGE_KEYWORD = "package:";

  /**
   * The length of the keyword header for a package line on an extensions file.
   */
  public static final int EXTENSION_FILE_PACKAGE_KEYWORD_LENGTH = EXTENSION_FILE_PACKAGE_KEYWORD
      .length();

  /**
   * Where the OSGi framework launcher can be found.
   */
  private static final String OSGI_FRAMEWORK_LAUNCH_FRAMEWORK_FACTORY =
      "META-INF/services/org.osgi.framework.launch.FrameworkFactory";

  /**
   * Subdirectory which will contain the bootstrap bundles.
   */
  public static final String BUNDLE_DIRECTORY_BOOTSTRAP = "bootstrap";

  /**
   * The OSGI framework which has been started.
   */
  private Framework framework;

  /**
   * All bundles installed.
   */
  private Set<Bundle> bundles = new HashSet<Bundle>();

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
   * The base install folder for Interactive Spaces
   */
  private File baseInstallFolder;

  private BundleContext rootBundleContext;

  private StartLevel startLevelService;

  /**
   * Boot the framework.
   */
  public void boot(List<String> args) {
    // TODO(keith): Better command line processing
    needShell = !args.contains("--noshell");

    baseInstallFolder = new File(".");

    getBootstrapBundleJars(BUNDLE_DIRECTORY_BOOTSTRAP);

    // If no bundle JAR files are in the directory, then exit.
    if (initialBundles.isEmpty()) {
      loggingProvider.getLog().warn("No bundles to install.");
    } else {
      setupShutdownHandler();

      try {
        createCoreServices(args);

        List<String> loadclasses = new ArrayList<String>();

        createAndStartFramework(loadclasses);

        registerCoreServices();

        // System.out.println("Got loadclasses " + loadclasses);
        // for (String loadclass : loadclasses) {
        // Class<?> loadclazz =
        // getClass().getClassLoader().loadClass(loadclass);
        // System.out.println("Loaded class " + loadclazz);
        // Method method = loadclazz.getMethod("getProperties");
        // method.invoke(null);
        // }

        startBundles(initialBundles);

        // Wait for framework to stop.
        framework.waitForStop(0);
        System.exit(0);

      } catch (Exception ex) {
        System.err.println("Error starting framework: " + ex);
        ex.printStackTrace();
        System.exit(0);
      }
    }
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

    Set<File> startupBundles = new HashSet<File>();
    startupBundles.addAll(initialBundles);

    containerCustomizerProvider = new SimpleContainerCustomizerProvider(args, startupBundles, true);
  }

  /**
   * Register all bootstrap core services with the container.
   */
  public void registerCoreServices() {
    rootBundleContext.registerService(LoggingProvider.class.getName(), loggingProvider, null);
    rootBundleContext.registerService(ConfigurationProvider.class.getName(), configurationProvider,
        null);
    rootBundleContext.registerService(ContainerCustomizerProvider.class.getName(),
        containerCustomizerProvider, null);
  }

  /**
   * Start all bundles.
   *
   * @param ctxt
   *          the framework bundle context
   * @param bundleList
   *          the list of all bundles
   * @param jars
   *          the jars for the bundles
   * @throws BundleException
   */
  protected void startBundles(List<File> jars) throws BundleException {
    for (File bundleFile : jars) {
      String bundleUri = bundleFile.toURI().toString();

      // TODO(keith): See if way to start up shell from property
      // since we may want it for remote access.
      if (bundleUri.contains("gogo") && !needShell) {
        continue;
      }

      Bundle b = rootBundleContext.installBundle(bundleUri);

      int startLevel = 1;
      String symbolicName = b.getSymbolicName();
      if (symbolicName.equals("interactivespaces.master.webapp")) {
        startLevel = 5;
      } else if (symbolicName.equals("interactivespaces.master")) {
        startLevel = 4;
      }

      if (startLevel != 1) {
        startLevelService.setBundleStartLevel(b, startLevel);
      }

      bundles.add(b);
    }

    // Start all installed non-fragment bundles.
    for (final Bundle bundle : bundles) {
      if (!isFragment(bundle)) {
        // TODO(keith): See if way to start up shell from property
        // since we may want it for remote access.
        if (bundle.getLocation().contains("gogo") && !needShell) {
          continue;
        }

        startBundle(bundle);
      }
    }

    startLevelService.setStartLevel(5);
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
      loggingProvider.getLog().error(
          String.format("Error while starting bundle %s", bundle.getLocation()), e);
    }
  }

  /**
   * Create, configure, and start the OSGi framework instance.
   *
   * @throws IOException
   * @throws Exception
   * @throws BundleException
   */
  private void createAndStartFramework(List<String> loadclasses) throws IOException, Exception,
      BundleException {
    Map<String, String> m = new HashMap<String, String>();

    m.put(Constants.FRAMEWORK_STORAGE_CLEAN, "onFirstInit");

    String delegations = getClassloaderDelegations();
    if (delegations != null) {
      loggingProvider.getLog().info(String.format("Delegations %s", delegations));
      m.put(Constants.FRAMEWORK_BOOTDELEGATION, delegations);
    }

    List<String> extraPackages = new ArrayList<String>();
    extraPackages.add("org.apache.commons.logging; version=1.1.1");
    extraPackages.add("org.apache.commons.logging.impl; version=1.1.1");
    extraPackages.add("interactivespaces.system.core.logging");
    extraPackages.add("interactivespaces.system.core.configuration");
    extraPackages.add("interactivespaces.system.core.container");

    addControllerExtensionsClasspath(extraPackages, loadclasses);

    StringBuilder packages = new StringBuilder();
    String separator = "";
    for (String extraPackage : extraPackages) {
      packages.append(separator).append(extraPackage);
      separator = ", ";
    }

    m.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, packages.toString());

    m.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_BASE_INSTALL_DIR,
        baseInstallFolder.getAbsolutePath());

    m.put(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_VERSION, getInteractiveSpacesVersion());

    m.putAll(configurationProvider.getInitialConfiguration());

    File file = new File(baseInstallFolder, "plugins-cache");
    m.put(Constants.FRAMEWORK_STORAGE, file.getCanonicalPath());

    framework = getFrameworkFactory().newFramework(m);
    framework.start();
    rootBundleContext = framework.getBundleContext();

    for (ServiceReference sr : framework.getServicesInUse()) {
      Object service = rootBundleContext.getService(sr);
      if (StartLevel.class.isAssignableFrom(service.getClass())) {
        startLevelService = (StartLevel) service;
      }
    }
  }

  /**
   * Set up a shutdown hook which will stop the framework when the VM shuts
   * down.
   */
  private void setupShutdownHandler() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
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
   * @param bootstrapFolder
   *          the folder containing the bootstrap bundles
   */
  private void getBootstrapBundleJars(String bootstrapFolder) {
    // Look in the specified bundle directory to create a list
    // of all JAR files to install.
    File[] files = new File(baseInstallFolder, bootstrapFolder).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        String filename = name.toLowerCase();
        return filename.endsWith(".jar") || filename.endsWith(".war");
      }
    });
    initialBundles = new ArrayList<File>();
    for (File f : files) {
      initialBundles.add(f);
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
          s = s.trim();
          // Try to load first non-empty, non-commented line.
          if ((s.length() > 0) && (s.charAt(0) != '#')) {
            return (FrameworkFactory) classLoader.loadClass(s).newInstance();
          }
        }
      } finally {
        if (br != null)
          br.close();
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
          builder.append(separator).append(line);
          separator = ",";
        }

        return builder.toString();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (reader != null)
          try {
            reader.close();
          } catch (IOException e) {
            // Don't care. Closing.
          }
      }
    }

    return null;
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param packages
   *          the list of packages to store the packages in
   * @param loadclasses
   *          The list of classes to have the classloader preload.
   */
  private void addControllerExtensionsClasspath(List<String> packages, List<String> loadclasses) {
    File[] extensionFiles =
        new File(baseInstallFolder, "lib/system/java").listFiles(new FilenameFilter() {

          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith(EXTENSION_FILE_EXTENSION);
          }
        });
    if (extensionFiles == null)
      return;

    for (File extensionFile : extensionFiles) {
      processExtensionFile(packages, loadclasses, extensionFile);
    }

  }

  /**
   * process an extension file.
   *
   * @param packages
   *          the collection of packages described in the extension files
   *
   * @param extensionFile
   *          the extension file to process
   */
  private void processExtensionFile(List<String> packages, List<String> loadclasses,
      File extensionFile) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(extensionFile));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          int pos = line.indexOf(EXTENSION_FILE_PACKAGE_KEYWORD);
          if (pos == 0 && line.length() > EXTENSION_FILE_PACKAGE_KEYWORD_LENGTH) {
            packages.add(line.substring(EXTENSION_FILE_PACKAGE_KEYWORD_LENGTH));
          }

          pos = line.indexOf("loadclass:");
          if (pos == 0 && line.length() > "loadclass:".length()) {
            loadclasses.add(line.substring("loadclass:".length()));
          }
        }
      }
    } catch (Exception e) {
      loggingProvider.getLog().error(
          String.format("Error while processing extensions file %s", extensionFile), e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // Don't care.
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
    String classContainer =
        getClass().getProtectionDomain().getCodeSource().getLocation().toString();

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
