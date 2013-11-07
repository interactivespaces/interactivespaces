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

package interactivespaces.workbench.project.builder;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.constituent.ProjectConstituent;

import java.io.File;
import java.util.List;

/**
 * A base builder class for common project types.
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public abstract class BaseProjectBuilder implements ProjectBuilder {

  /**
   * Build has begun. Do any specific parts of the build.
   *
   * @param project
   *          the project
   * @param context
   *          the build context
   * @param stagingDirectory
   *          the staging directory where build artifacts go
   *
   * @return {@code true} if build part was successful
   */
  public boolean onBuild(Project project, ProjectBuildContext context, File stagingDirectory) {
    // Default is nothing
    return true;
  }

  /**
   * Process the needed resources for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  protected void processResources(Project project, File stagingDirectory,
      ProjectBuildContext context) {
    if (project.getSources() != null) {
      throw new SimpleInteractiveSpacesException("Project type does not allow sources section");
    }
    processConstituents(project, project.getResources(), stagingDirectory, context);
  }

  /**
   * Process the needed sources for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  protected void processSources(Project project, File stagingDirectory,
      ProjectBuildContext context) {
    if (project.getResources() != null) {
      throw new SimpleInteractiveSpacesException("Project type does not allow resources section");
    }
    processConstituents(project, project.getSources(), stagingDirectory, context);
  }

  /**
   * Process the list of constituents for the project.
   *
   * @param project
   *          the project being built
   * @param constituents
   *          constituents to process
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  private void processConstituents(Project project, List<ProjectConstituent> constituents,
      File stagingDirectory, ProjectBuildContext context) {
    if (constituents == null) {
      return;
    }
    SimpleConfiguration workbenchConfig = context.getWorkbench().getWorkbenchConfig();
    SimpleConfiguration resourceConfig = SimpleConfiguration.newConfiguration();
    resourceConfig.setParent(workbenchConfig);

    resourceConfig.setValue(CONFIGURATION_PROPERTY_PROJECT_HOME, project.getBaseDirectory()
        .getAbsolutePath());

    for (ProjectConstituent constituent : constituents) {
      constituent.processConstituent(project, stagingDirectory, context, resourceConfig);
    }
  }

  /**
   * Get the build destination file.
   *
   * @param project
   *          the project being built
   * @param buildDirectory
   *          where the artifact will be built
   * @param extension
   *          filename extension for generating destination
   *
   * @return the file where the build should be written
   */
  protected File getBuildDestinationFile(Project project, File buildDirectory, String extension) {
    return new File(buildDirectory, project.getIdentifyingName() + "-" + project.getVersion()
        + "." + extension);
  }
}
