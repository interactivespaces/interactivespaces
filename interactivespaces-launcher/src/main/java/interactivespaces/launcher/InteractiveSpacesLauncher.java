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
import java.util.List;

/**
 * An OSGI launcher for Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesLauncher {

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
  private static final String SPACES_LIB_JAVA_SYSTEM = "lib/system/java";

  /**
   * The classloader for starting Interactive Spaces
   */
  private ClassLoader classLoader;

  /**
   * The file which gives the process ID for the IS process.
   */
  private File pidFile;

  /**
   * The main method for IS.
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
   * Launch Interactive Spaces
   */
  public void launch(String[] args) {
    if (writePid()) {
      createClassLoader();
      boostrap(args);
    } else {
      System.err.format(
          "InteractiveSpaces component already running. Lock found on %s\n",
          pidFile.getAbsolutePath());
    }
  }

  /**
   * Create the classloader to use to start the system.
   */
  private void createClassLoader() {
    File systemDirectory = new File(SPACES_LIB_JAVA_SYSTEM);

    List<URL> urls = collectSystemLibClasspath(systemDirectory);
    addExtensionsToClasspath(systemDirectory, urls);

    classLoader = new URLClassLoader(urls.toArray(new URL[0]));
  }

  /**
   * Get the classpath to be used for starting the system.
   *
   * @return
   */
  private List<URL> collectSystemLibClasspath(File systemDirectory) {
    List<URL> urls = new ArrayList<URL>();
    File[] files = systemDirectory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith(EXTENSION_FILE_EXTENSION)) {
          continue;
        }

        try {
          URL url = file.toURI().toURL();
          urls.add(url);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
    }

    if (urls.isEmpty()) {
      System.err.format("No bootstrap files found in %s", systemDirectory.getAbsolutePath());
    }

    return urls;
  }

  /**
   * If the {@link #SYSTEMJARS_CONF} exists in the system directory, extend the
   * classpath.
   *
   * @param systemDirectory
   *          the system directory
   * @param urls
   *          the collection of URLs for the classpath.
   */
  private void addExtensionsToClasspath(File systemDirectory, List<URL> urls) {
    File[] extensionFiles = systemDirectory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(EXTENSION_FILE_EXTENSION);
      }
    });
    if (extensionFiles == null)
      return;

    for (File extensionFile : extensionFiles) {
      processExtensionFile(urls, extensionFile);
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
            urls.add(new File(line.substring(EXTENSION_FILE_PATH_KEYWORD_LENGTH)).toURI().toURL());
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
   */
  private void boostrap(String[] args) {
    try {
      Class<?> bootstrapClass =
          classLoader
              .loadClass("interactivespaces.launcher.bootstrap.InteractiveSpacesFrameworkBootstrap");

      Object bootstrapInstance = bootstrapClass.newInstance();

      List<String> argList = new ArrayList<String>();
      for (String arg : args) {
        argList.add(arg);
      }

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
   * @return {@code true} if a pid file didn't previously exist and one couldn't
   *         be written.
   */
  private boolean writePid() {
    File runDirectory = new File("run");
    if (!runDirectory.exists()) {
      if (!runDirectory.mkdir()) {
        System.err.format("Could not create run directory %s\n", runDirectory);
        return false;
      }
    }

    pidFile = new File(runDirectory, "interactivespaces.pid");

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
