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

package interactivespaces.workbench.project.builder;

import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectTaskContext;

/**
 * A builder for Interactive Spaces projects.
 *
 * @param <T>
 *          the type of the project
 *
 * @author Keith M. Hughes
 */
public interface ProjectBuilder<T extends Project> {

  /**
   * Configuration property defining the project home directory.
   */
  String CONFIGURATION_PROPERTY_PROJECT_HOME = "project.home";

  /**
   * Subdirectory of build folder which contains the staged activity.
   */
  String BUILD_STAGING_DIRECTORY = "staging";

  /**
   * Build the given project.
   *
   * @param project
   *          the project to build
   * @param context
   *          the context to use when building the activity
   *
   * @return {@code true} if a successful build
   */
  boolean build(T project, ProjectTaskContext context);
}
