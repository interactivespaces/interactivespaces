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

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.type.ProjectType;

import com.google.common.collect.Lists;

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

  /**
   * Get the project being built.
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

  /**
   * Get the project type for the build.
   *
   * @param <T>
   *          the actual project type
   *
   * @return the project type
   */
  @SuppressWarnings("unchecked")
  public <T extends ProjectType> T getProjectType() {
    return (T) projectType;
  }

  /**
   * Return a configuration that provides necessary values for building this project.
   *
   * @return configuration for this project build
   */
  public Configuration getProjectConfiguration() {
    SimpleConfiguration workbenchConfig = getWorkbench().getWorkbenchConfig();
    SimpleConfiguration resourceConfig = SimpleConfiguration.newConfiguration();
    resourceConfig.setParent(workbenchConfig);
    resourceConfig.setValue(ProjectBuilder.CONFIGURATION_PROPERTY_PROJECT_HOME,
        project.getBaseDirectory().getAbsolutePath());
    return resourceConfig;
  }

  /**
   * Return the appropriate file path depending on evaluate and default root directory.
   *
   *
   * @param rootDirectory
   *          root directory to use in case of default
   * @param target
   *          target path desired
   *
   * @return appropriate file to use
   */
  public File getProjectTarget(File rootDirectory, String target) {
    String targetPath = getProjectConfiguration().evaluate(target);
    File targetFile = new File(targetPath);
    if (targetFile.isAbsolute()) {
      return targetFile;
    }
    return new File(rootDirectory, targetPath);
  }
}
