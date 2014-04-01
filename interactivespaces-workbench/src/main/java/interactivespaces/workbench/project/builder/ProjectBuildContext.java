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

import com.google.common.collect.Maps;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.activity.type.ProjectType;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A context for building and packaging activities.
 *
 * @author Keith M. Hughes
 */
public class ProjectBuildContext implements ProjectContext {

  /**
   * Where things are being built.
   */
  private static final String BUILD_DIRECTORY = "build";

  /**
   * Static file support instance.
   */
  private static final FileSupport FILE_SUPPORT = FileSupportImpl.INSTANCE;

  /**
   * The project being built.
   */
  private final Project project;

  /**
   * The workbench the project is being built under.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Files to add to the project.
   */
  private final List<File> artifactsToAdd = Lists.newArrayList();

  /**
   * The directory where the project will be built.
   */
  private final File buildDirectory;

  /**
   * The project type of the project.
   */
  private final ProjectType projectType;

  /**
   * Collection of dest to src file mappings, constructed during the build process.
   */
  private final Map<File, File> sourceMap = Maps.newHashMap();

  /**
   * Construct a new build context.
   *
   * @param projectType
   *          the type of the project
   * @param project
   *          project object
   * @param workbench
   *          workbench instance
   */
  public ProjectBuildContext(ProjectType projectType, Project project,
      InteractiveSpacesWorkbench workbench) {
    this.projectType = projectType;
    this.project = project;
    this.workbench = workbench;

    buildDirectory = new File(project.getBaseDirectory(), BUILD_DIRECTORY);
    FILE_SUPPORT.directoryExists(buildDirectory);

    prepareProjectConfiguration();
  }

  /**
   * Add a new artifact to go in the file.
   *
   * @param artifact
   *          artifact to add to context
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

  @Override
  public Project getProject() {
    return project;
  }

  @Override
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

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ProjectType> T getProjectType() {
    return (T) projectType;
  }

  /**
   * Add anything needed to the project configuration.
   */
  private void prepareProjectConfiguration() {
    project.getConfiguration().setValue(ProjectBuilder.CONFIGURATION_PROPERTY_PROJECT_HOME,
        project.getBaseDirectory().getAbsolutePath());
  }

  @Override
  public File getProjectTarget(File rootDirectory, String target) {
    String targetPath = project.getConfiguration().evaluate(target);
    File targetFile = new File(targetPath);
    if (targetFile.isAbsolute()) {
      return targetFile;
    }
    return new File(rootDirectory, targetPath);
  }

  @Override
  public Map<File, File> getResourceSourceMap() {
    return sourceMap;
  }
}
