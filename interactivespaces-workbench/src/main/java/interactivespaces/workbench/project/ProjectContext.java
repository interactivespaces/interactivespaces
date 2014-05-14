/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.workbench.project;

import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.type.ProjectType;

import java.io.File;
import java.util.Map;

/**
 * Interface for managing a project context, used for creating or building.
 *
 * @author Trevor Pering
 */
public interface ProjectContext {

  /**
   * Get the project being built.
   *
   * @param <T>
   *          type of the project
   *
   * @return the project being built
   */
  <T extends Project> T getProject();

  /**
   * Get the workbench the project is being built under.
   *
   * @return the workbench
   */
  InteractiveSpacesWorkbench getWorkbench();

  /**
   * Get the project type for the project.
   *
   * @param <T>
   *          project type class
   *
   * @return project type for the context
   */
  <T extends ProjectType> T getProjectType();

  /**
   * Return the appropriate file path depending on evaluate and default root
   * directory.
   *
   * @param rootDirectory
   *          root directory to use in case of default
   * @param target
   *          target path desired
   *
   * @return appropriate file to use
   */
  File getProjectTarget(File rootDirectory, String target);

  /**
   * The resource source map is a map that can be used at runtime to link
   * project files back to their original source, for enabling live editing of
   * javascript or other resources. This map is constructed during the build
   * process (by adding to the map), and then can be written to a file or other
   * construct as part of the resulting build.
   *
   * @return mutable resource source map, stored as {dest, source} key/value
   *         pairs
   */
  Map<File, File> getResourceSourceMap();
}
