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

package interactivespaces.service.script.internal.python;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.ScriptSource;
import interactivespaces.service.script.internal.ActivityScriptFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.python.core.Py;
import org.python.core.PySystemState;

import java.io.File;
import java.util.Properties;

/**
 * Create a Python-based {@link Activity}.
 *
 * @author Keith M. Hughes
 */
public class PythonActivityScriptFactory implements ActivityScriptFactory {

  /**
   * Python property for the list of packages directories where the interpreter
   * will find packages.
   */
  public static final String PYTHON_PROPERTY_PACKAGES_DIRECTORIES = "python.packages.directories";

  /**
   * Python property for the Python path.
   */
  public static final String PYTHON_PROPERTY_PYTHON_PATH = "python.path";

  /**
   * Name of the cache directory for the python classes.
   */
  public static final String PYTHON_CACHE_DIRECTORY = "python";

  /**
   * Name of the directory in the container lib folder for Python packages.
   */
  public static final String CONTAINER_LIB_PYTHON = "python";

  /**
   * Name of the directory in the container site specific folder for Python
   * packages.
   */
  public static final String CONTAINER_LIB_PYTHON_SITE = "site";

  /**
   * Name of the directory for the Python system libraries in the python lib
   * folder.
   */
  public static final String CONTAINER_LIB_PYTHON_SYSTEM = "PyLib";

  /**
   * Name of the directory for the Python libraries that supply IS-specific
   * Python code.
   */
  public static final String CONTAINER_LIB_PYTHON_CONTAINER = "release";

  /**
   * Separator for python paths.
   */
  public static final char PYTHON_PATH_SEPARATOR = ':';

  /**
   * The Interactive Spaces environment we are running under.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Construct the factory.
   *
   * @param spaceEnvironment
   *          the space environment for the factory
   */
  public PythonActivityScriptFactory(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void initialize() {
    File pythonCachedir = spaceEnvironment.getFilesystem().getTempDirectory(PYTHON_CACHE_DIRECTORY);
    File bootstrapDir = spaceEnvironment.getFilesystem().getSystemBootstrapDirectory();

    Properties properties = new Properties(System.getProperties());
    properties.setProperty(PySystemState.PYTHON_CACHEDIR, pythonCachedir.getAbsolutePath());
    properties.setProperty(PYTHON_PROPERTY_PACKAGES_DIRECTORIES, bootstrapDir.getAbsolutePath());

    addSystemPythonPath(properties);

    PySystemState.initialize(properties, null, null, PythonActivityScriptFactory.class.getClassLoader());
  }

  /**
   * Add in the python paths from the system.
   *
   * @param properties
   *          the properties for the python interpreter
   */
  protected void addSystemPythonPath(Properties properties) {
    // Get all readable dirs in Interactive Spaces system python library
    File systemPythonLibDirectory = spaceEnvironment.getFilesystem().getLibraryDirectory(CONTAINER_LIB_PYTHON);

    if (systemPythonLibDirectory.exists() && systemPythonLibDirectory.canRead()) {
      StringBuilder pythonPath = new StringBuilder();

      File siteLibraries = new File(systemPythonLibDirectory, CONTAINER_LIB_PYTHON_SITE);
      File[] contents = siteLibraries.listFiles();
      if (contents != null) {
        for (File siteLibrary : contents) {
          if (siteLibrary.isDirectory() && siteLibrary.canRead()) {
            pythonPath.append(siteLibrary.getAbsolutePath()).append(PYTHON_PATH_SEPARATOR);
          }
        }
      }

      String pylibPath = new File(systemPythonLibDirectory, CONTAINER_LIB_PYTHON_SYSTEM).getAbsolutePath();
      pythonPath.append(new File(systemPythonLibDirectory, CONTAINER_LIB_PYTHON_CONTAINER).getAbsolutePath())
          .append(PYTHON_PATH_SEPARATOR).append(pylibPath);

      PySystemState.prefix = Py.newString(pylibPath);

      properties.setProperty(PYTHON_PROPERTY_PYTHON_PATH, pythonPath.toString());
    }
  }

  @Override
  public ActivityScriptWrapper getActivity(String objectName, ScriptSource scriptSource,
      ActivityFilesystem activityFilesystem, Configuration configuration) {
    return new PythonActivityScriptWrapper(objectName, scriptSource, activityFilesystem, configuration);
  }
}
