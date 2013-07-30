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
   * The Interactive Spaces environment we are running under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  public PythonActivityScriptFactory(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void initialize() {
    File pythonCachedir = spaceEnvironment.getFilesystem().getTempDirectory("python");
    File bootstrapDir = spaceEnvironment.getFilesystem().getBootstrapDirectory();

    Properties props = new Properties(System.getProperties());

    props.setProperty("python.cachedir", pythonCachedir.getAbsolutePath());
    props.setProperty("python.packages.directories", bootstrapDir.getAbsolutePath());

    addSystemPythonPath(props);

    PySystemState.initialize(props, null, null, PythonActivityScriptFactory.class.getClassLoader());
    // PythonInterpreter.initialize(System.getProperties(), props,
    // new String[0]);

    // PySystemState state = new PySystemState();

    // interp = new PythonInterpreter(/* null, state */);
  }

  /**
   * @param props
   */
  protected void addSystemPythonPath(Properties props) {
    // Get all readable dirs in Interactive Spaces system python library
    File systemPythonLibDirectory = spaceEnvironment.getFilesystem().getLibraryDirectory("python");

    if (systemPythonLibDirectory.exists() && systemPythonLibDirectory.canRead()) {
      StringBuilder pythonPath = new StringBuilder();

      File siteLibraries = new File(systemPythonLibDirectory, "site");
      File[] contents = siteLibraries.listFiles();
      if (contents != null) {
        for (File siteLibrary : contents) {
          if (siteLibrary.isDirectory() && siteLibrary.canRead()) {
            pythonPath.append(siteLibrary.getAbsolutePath()).append(':');
          }
        }
      }

      String pylibPath = new File(systemPythonLibDirectory, "PyLib").getAbsolutePath();
      pythonPath.append(new File(systemPythonLibDirectory, "release").getAbsolutePath())
          .append(':').append(pylibPath);

      PySystemState.prefix = Py.newString(pylibPath);

      props.setProperty("python.path", pythonPath.toString());
    }
  }

  @Override
  public ActivityScriptWrapper getActivity(String objectName, ScriptSource scriptSource,
      ActivityFilesystem activityFilesystem, Configuration configuration) {
    return new PythonActivityScriptWrapper(objectName, scriptSource, activityFilesystem,
        configuration);
  }
}
