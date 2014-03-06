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

import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.constituent.ProjectConstituent;

import java.io.File;
import java.util.List;

/**
 * A base builder class for common project types.
 *
 * @param <T>
 *          the type of the project
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public abstract class BaseProjectBuilder<T extends Project> implements ProjectBuilder<T> {

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
  public boolean onBuild(T project, ProjectBuildContext context, File stagingDirectory) {
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
  protected void processResources(Project project, File stagingDirectory, ProjectBuildContext context) {
    processConstituents(project, project.getResources(), stagingDirectory, context);
  }

  /**
   * Process the extra constituents for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  protected void processExtraConstituents(Project project, File stagingDirectory, ProjectBuildContext context) {
    processConstituents(project, project.getExtraConstituents(), stagingDirectory, context);
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
  protected void processSources(Project project, File stagingDirectory, ProjectBuildContext context) {
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
  private void processConstituents(Project project, List<ProjectConstituent> constituents, File stagingDirectory,
      ProjectBuildContext context) {
    if (constituents == null) {
      return;
    }

    for (ProjectConstituent constituent : constituents) {
      constituent.processConstituent(project, stagingDirectory, context);
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
    return new File(buildDirectory, getProjectArtifactFilename(project, extension));
  }

  /**
   * Get the name of the project artifact.
   *
   * @param project
   *          the project
   * @param extension
   *          the extension to be used for the artifact
   *
   * @return the filename for the project artifact
   */
  private String getProjectArtifactFilename(Project project, String extension) {
    return project.getIdentifyingName() + "-" + project.getVersion() + "." + extension;
  }
}
