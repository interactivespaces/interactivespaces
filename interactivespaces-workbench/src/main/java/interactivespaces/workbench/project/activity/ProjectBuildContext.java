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

package interactivespaces.workbench.project.activity;

import com.google.common.collect.Lists;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;

import java.io.File;
import java.util.List;

/**
 * A context for building and packaging activities.
 *
 * @author Keith M. Hughes
 */
public class ProjectBuildContext {

  /**
   * Where things are being built.
   */
  private static final String BUILD_DIRECTORY = "build";

  /**
   * The project being built.
   */
  private Project project;

  /**
   * The workbench the project is being built under.
   */
  private InteractiveSpacesWorkbench workbench;

  /**
   * Files to add to the project.
   */
  private List<File> artifactsToAdd = Lists.newArrayList();

  /**
   * The directory where the project will be built.
   */
  private File buildDirectory;

  public ProjectBuildContext(Project project, InteractiveSpacesWorkbench workbench) {
    this.project = project;
    this.workbench = workbench;

    buildDirectory = new File(project.getBaseDirectory(), BUILD_DIRECTORY);
    if (!buildDirectory.exists()) {
      if (!buildDirectory.mkdirs()) {
        throw new InteractiveSpacesException(String.format(
            "Cannot create activity build directory %s", buildDirectory));
      }
    }
  }

  /**
   * Add a new artifact to go in the file.
   *
   * @param artifact
   */
  public void addArtifact(File artifact) {
    artifactsToAdd.add(artifact);
  }

  /**
   * Get the list of artifacts to add to the project.
   *
   * @return the list of artifacts to add to the project
   */
  public List<File> getArtifactsToAdd() {
    return artifactsToAdd;
  }

  /**
   * Get the project being built
   *
   * @return the project being built
   */
  public Project getProject() {
    return project;
  }

  /**
   * Get the workbench the project is being built under.
   *
   * @return the workbench
   */
  public InteractiveSpacesWorkbench getWorkbench() {
    return workbench;
  }

  /**
   * Get the root build directory.
   *
   * @return the root of the build directory
   */
  public File getBuildDirectory() {
    return buildDirectory;
  }
}
