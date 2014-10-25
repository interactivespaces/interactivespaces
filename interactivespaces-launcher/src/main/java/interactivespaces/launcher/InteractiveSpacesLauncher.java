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

package interactivespaces.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The initial launcher for Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesLauncher {

  /**
   * The name of the class which bootstraps the Interactoive Spaces framework.
   */
  private static final String CLASSNAME_INTERACTIVESPACES_FRAMEWORK_BOOTSTRAP =
      "interactivespaces.launcher.bootstrap.InteractiveSpacesFrameworkBootstrap";

  /**
   * The run folder in the directory where the container is installed.
   */
  private static final String RUN_DIR_PATH = "run";

  /**
   * The name of the PID file for the container.
   */
  private static final String FILENAME_INTERACTIVESPACES_PID = "interactivespaces.pid";

  /**
   * The file extension used for files which give container extensions.
   */
  private static final String EXTENSION_FILE_EXTENSION = ".ext";

  /**
   * The keyword header for a package line on an extensions file.
   */
  public static final String EXTENSION_FILE_PATH_KEYWORD = "path:";

  /**
   * The length of the keyword header for a package line on an extensions file.
   */
  public static final int EXTENSION_FILE_PATH_KEYWORD_LENGTH = EXTENSION_FILE_PATH_KEYWORD.length();

  /**
   * The subdirectory which contains the system files.
   */
  private static final String SPACES_LIB_SYSTEM_JAVA = "lib/system/java";

  /**
   * The subdirectory which contains the environment files relative to the config folder.
   */
  private static final String SPACES_CONFIG_ENVIRONMENT = "environment";

  /**
   * Command line argument prefix for specifying a specific runtime path. This should match the value of
   * {@code InteractiveSpacesFrameworkBootstrap.ARGS_RUNTIME_PREFIX}, but can't be a shared variable because of
   * package dependency considerations.
   */
  private static final String COMMAND_LINE_RUNTIME_PREFIX = "--runtime=";

  /**
   * Command line argument prefix for specifying a specific config path. This should match the value of
   * {@code InteractiveSpacesFrameworkBootstrap.ARGS_CONFIG_PREFIX}, but can't be a shared variable because of
   * package dependency considerations.
   */
  private static final String COMMAND_LINE_CONFIG_PREFIX = "--config=";

  /**
   * The classloader for starting Interactive Spaces.
   */
  private ClassLoader classLoader;

  /**
   * Path to the runtime directory, which is the root for dynamic configuration. Can be set by a command line flag.
   */
  private String runtimePath = ".";

  /**
   * Path to the config directory, which is the root for dynamic configuration. Can be set by a command line flag.
   */
  private String configPath = "config";

  /**
   * The file which gives the process ID for the Interactive Spaces process.
   */
  private File pidFile;

  /**
   * The main method for Interactive Spaces.
   *
   * @param args
   *          the command line arguments
   *
   * @throws Exception
   *           fall down go boom
   */
  public static void main(String[] args) throws Exception {
    InteractiveSpacesLauncher launcher = new InteractiveSpacesLauncher();
    launcher.launch(args);
  }

  /**
   * Launch Interactive Spaces.
   *
   * @param args
   *          the command line arguments
   */
  public void launch(String[] args) {
    // argList needs to be mutable because it is modified by some components downstream.
    List<String> argList = new ArrayList<String>();
    Collections.addAll(argList, args);
    processLauncherCommandArgs(argList);
    if (writePid()) {
      createClassLoader();
      boostrap(argList);
    } else {
      System.err.format("InteractiveSpaces component already running. Lock found on %s\n", pidFile.getAbsolutePath());
    }
  }

  /**
   * Process any command line args for the launcher. This leaves the arguments in-place in case they're
   * needed downstream.
   *
   * @param args
   *          list of arguments
   */
  private void processLauncherCommandArgs(List<String> args) {
    for (String thisArg : args) {
      if (thisArg.startsWith(COMMAND_LINE_RUNTIME_PREFIX)) {
        runtimePath = thisArg.substring(COMMAND_LINE_RUNTIME_PREFIX.length());
        System.out.println("Setting runtime path to " + runtimePath);
      } else if (thisArg.startsWith(COMMAND_LINE_CONFIG_PREFIX)) {
        configPath = thisArg.substring(COMMAND_LINE_CONFIG_PREFIX.length());
        System.out.println("Setting config path to " + configPath);
      }
    }
  }

  /**
   * Create the classloader to use to start the system.
   */
  private void createClassLoader() {
    List<URL> urls = new ArrayList<URL>();

    collectSystemLibClasspath(urls);
    addExtensionsToClasspath(urls);

    classLoader = new URLClassLoader(urls.toArray(new URL[0]));
  }

  /**
   * Get the classpath to be used for starting the system.
   *
   * @param classpath
   *          the classpath list
   */
  private void collectSystemLibClasspath(List<URL> classpath) {
    File systemDirectory = new File(SPACES_LIB_SYSTEM_JAVA);

    File[] files = systemDirectory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith(EXTENSION_FILE_EXTENSION)) {
          continue;
        }

        try {
          URL url = file.toURI().toURL();
          classpath.add(url);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
    }

    if (classpath.isEmpty()) {
      System.err.format("No bootstrap files found in %s", systemDirectory.getAbsolutePath());
    }
  }

  /**
   * Add any extension classpath additions to the classpath.
   *
   * @param classpath
   *          the classpath
   */
  private void addExtensionsToClasspath(List<URL> classpath) {
    File extensionsDirectory = new File(new File(configPath), SPACES_CONFIG_ENVIRONMENT);
    File[] extensionFiles = extensionsDirectory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(EXTENSION_FILE_EXTENSION);
      }
    });

    if (extensionFiles == null) {
      return;
    }

    for (File extensionFile : extensionFiles) {
      processExtensionFile(classpath, extensionFile);
    }

  }

  /**
   * process an extension file.
   *
   * @param urls
   *          the collection of urls to be placed on the classpath at boot
   *
   * @param extensionFile
   *          the extension file to process
   */
  private void processExtensionFile(List<URL> urls, File extensionFile) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(extensionFile));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          int pos = line.indexOf(EXTENSION_FILE_PATH_KEYWORD);
          if (pos == 0 && line.length() > EXTENSION_FILE_PATH_KEYWORD_LENGTH) {
            File path = new File(line.substring(EXTENSION_FILE_PATH_KEYWORD_LENGTH).trim());
            urls.add(path.toURI().toURL());
          }
        }
      }
    } catch (Exception e) {
      System.out.format("Error while processing extensions file %s\n", extensionFile);
      e.printStackTrace();
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
   * Bootstrap the framework.
   *
   * @param argList
   *          the command line arguments
   */
  private void boostrap(List<String> argList) {
    try {
      Class<?> bootstrapClass = classLoader.loadClass(CLASSNAME_INTERACTIVESPACES_FRAMEWORK_BOOTSTRAP);

      Object bootstrapInstance = bootstrapClass.newInstance();

      Method boostrapMethod = bootstrapClass.getMethod("boot", List.class);
      boostrapMethod.invoke(bootstrapInstance, argList);
    } catch (Exception e) {
      System.err.println("Could not create bootstrapper");
      e.printStackTrace(System.err);
    }
  }

  /**
   * Try and write the pid file.
   *
   * @return {@code false} if there was an error creating the pid file
   */
  private boolean writePid() {
    File runDirectory = new File(runtimePath, RUN_DIR_PATH);
    if (!runDirectory.exists()) {
      if (!runDirectory.mkdir()) {
        System.err.format("Could not create run directory %s\n", runDirectory);
        return false;
      }
    }

    pidFile = new File(runDirectory, FILENAME_INTERACTIVESPACES_PID);

    try {
      RandomAccessFile pidRaf = new RandomAccessFile(pidFile, "rw");
      FileLock fl = pidRaf.getChannel().tryLock(0, Long.MAX_VALUE, false);

      if (fl != null) {
        writePidFile(pidRaf);

        pidFile.deleteOnExit();

        return true;
      } else {
        // someone else has the lock
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();

      return false;
    }
  }

  /**
   * Attempt to write into the PID file.
   *
   * @param pidRaf
   *          the file reference
   *
   * @return {@code true} if the write was successful
   */
  private boolean writePidFile(RandomAccessFile pidRaf) {
    try {
      pidRaf.writeChars(Integer.toString(getPid()));

      return true;
    } catch (IOException e) {
      e.printStackTrace();

      return false;
    }
  }

  /**
   * @return PID of node process if available, throws
   *         {@link UnsupportedOperationException} otherwise.
   */
  private int getPid() {
    // Java has no standard way of getting PID. MF.getName()
    // returns '1234@localhost'.
    try {
      String mxName = ManagementFactory.getRuntimeMXBean().getName();
      int idx = mxName.indexOf('@');
      if (idx > 0) {
        try {
          return Integer.parseInt(mxName.substring(0, idx));
        } catch (NumberFormatException e) {
          return 0;
        }
      }
    } catch (NoClassDefFoundError unused) {
      // Android does not support ManagementFactory. Try to get the PID on
      // Android.
      try {
        return (Integer) Class.forName("android.os.Process").getMethod("myPid").invoke(null);
      } catch (Exception unused1) {
        // Ignore this exception and fall through to the
        // UnsupportedOperationException.
      }
    }
    throw new UnsupportedOperationException();
  }

}
