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

package interactivespaces.launcher.bootstrap;

import org.apache.commons.logging.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A reader for extensions files.
 *
 * @author Keith M. Hughes
 */
public class ExtensionsReader {

  /**
   * The file extension used for files which give container extensions.
   */
  public static final String EXTENSION_FILE_EXTENSION = ".ext";

  /**
   * The initial chaacter for comments in extension files.
   */
  public static final String EXTENSION_FILE_COMMENT = "#";

  /**
   * The keyword header for a package line in an extensions file.
   */
  public static final String EXTENSION_FILE_KEYWORD_PACKAGE = "package:";

  /**
   * The keyword header for a boot package line in an extensions file.
   */
  public static final String EXTENSION_FILE_KEYWORD_PACKAGE_BOOT = "bootpackage:";

  /**
   * The keyword header for a loadclass line in an extensions file.
   */
  public static final String EXTENSION_FILE_KEYWORD_LOADCLASS = "loadclass:";

  /**
   * The keyword header for a loadlibrary line in an extensions file.
   */
  public static final String EXTENSION_FILE_KEYWORD_LOADLIBRARY = "loadlibrary:";

  /**
   * The keyword header for a containerpath line in an extensions file.
   */
  public static final String EXTENSION_FILE_KEYWORD_CONTAINER_PATH = "containerpath:";

  /**
   * The list of packages from the files.
   */
  private final List<String> packages = new ArrayList<String>();

  /**
   * The list of packages from the files.
   */
  private final List<String> bootPackages = new ArrayList<String>();

  /**
   * The list of classes to be preloaded.
   */
  private final List<String> loadClasses = new ArrayList<String>();

  /**
   * The list of libraries to be loaded.
   */
  private final List<String> loadLibraries = new ArrayList<String>();

  /**
   * The list of items to add onto the container path.
   */
  private final List<String> containerPath = new ArrayList<String>();

  /**
   * Logger for the reader.
   */
  private final Log log;

  /**
   * Construct a new extensions reader.
   *
   * @param log
   *          logger for the reader
   */
  public ExtensionsReader(Log log) {
    this.log = log;
  }

  /**
   * Get the list of Java packages from all processed extension files.
   *
   * @return the list of Java packages
   */
  public List<String> getPackages() {
    return packages;
  }

  /**
   * Get the list of Java packages from all processed extension files that should appear on the boot delegation path.
   *
   * @return the list of Java packages
   */
  public List<String> getBootPackages() {
    return bootPackages;
  }

  /**
   * Get the list of all loadclass values from all processed filed.
   *
   * @return the list of all loadclass values
   */
  public List<String> getLoadClasses() {
    return loadClasses;
  }

  /**
   * Get the list of all loadlibrary values from all processed filed.
   *
   * @return the list of all loadlibrary values
   */
  public List<String> getLoadLibraries() {
    return loadLibraries;
  }

  /**
   * Get the list of all containerpath values from all processed filed.
   *
   * @return the list of all containerpath values
   */
  public List<String> getContainerPath() {
    return containerPath;
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param extensionsFolder
   *          the folder which contains the extension files
   */
  public void processExtensionFiles(File extensionsFolder) {
    File[] extensionFiles = extensionsFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(EXTENSION_FILE_EXTENSION);
      }
    });
    if (extensionFiles == null || extensionFiles.length == 0) {
      return;
    }

    for (File extensionFile : extensionFiles) {
      processExtensionFile(extensionFile);
    }

    log.debug(String.format("Extensions have added the following Java packages: %s", packages));
    log.debug(String.format("Extensions have added the following classes to be loaded automatically: %s", loadClasses));
    log.debug(String.format("Extensions have loaded the following native libraries: %s ", loadLibraries));
    log.debug(String.format("Extensions have loaded the following container path items: %s ", containerPath));
  }

  /**
   * Process an extension file.
   *
   * @param extensionFile
   *          the extension file to process
   */
  private void processExtensionFile(File extensionFile) {
    log.debug(String.format("Loading extension file %s", extensionFile.getAbsolutePath()));
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(extensionFile));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith(EXTENSION_FILE_COMMENT)) {
          boolean processed =
              processLine(line, EXTENSION_FILE_KEYWORD_PACKAGE, packages)
                  || processLine(line, EXTENSION_FILE_KEYWORD_PACKAGE_BOOT, bootPackages)
                  || processLine(line, EXTENSION_FILE_KEYWORD_LOADCLASS, loadClasses)
                  || processLine(line, EXTENSION_FILE_KEYWORD_LOADLIBRARY, loadLibraries)
                  || processLine(line, EXTENSION_FILE_KEYWORD_CONTAINER_PATH, containerPath);
        }
      }
    } catch (Exception e) {
      log.error(String.format("Error while processing extensions file %s", extensionFile), e);
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
   * Process a line and see if it is a particular keyword.
   *
   * @param line
   *          the line being processed
   * @param keyword
   *          the keyword being checked for
   * @param collection
   *          which collection should receive the value if there is a value for the given keyword
   *
   * @return {@code true} if the line was for the keyword and a value was placed in the collection
   */
  private boolean processLine(String line, String keyword, List<String> collection) {
    if (line.startsWith(keyword)) {
      collection.add(line.substring(keyword.length()).trim());

      return true;
    }

    return false;
  }
}
