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

package interactivespaces.workbench.project.group;

import com.google.common.collect.Lists;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.TemplateFile;
import interactivespaces.workbench.project.TemplateVar;

import java.io.File;
import java.util.List;

/**
 * A group of projects.
 *
 * @author Trevor Pering
 */
public class ProjectGroup {

  /**
   * List of projects in this group.
   */
  private final List<Project> projectList = Lists.newArrayList();

  /**
   * Base directory for the output files when generating this project group.
   */
  private File baseDirectory;

  /**
   * Source specification file for this project group.
   */
  private File specificationSource;

  /**
   * Get the project list.
   *
   * @return list of projects in this group
   */
  public List<Project> getProjectList() {
    return projectList;
  }

  /**
   * Add a project to the group.
   *
   * @param project
   *          project to add
   */
  public void addProject(Project project) {
    projectList.add(project);
  }

  /**
   * @return base directory for this group.
   */
  public File getBaseDirectory() {
    return baseDirectory;
  }

  /**
   * Set the base directory for this group.
   *
   * @param baseDirectory
   *          base directory for this group
   */
  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * @return specification source file for this project group
   */
  public File getSpecificationSource() {
    return specificationSource;
  }

  /**
   * Set the specification source for this group.
   *
   * @param specDirectory
   *          directory to set for the specification
   */
  public void setSpecificationSource(File specDirectory) {
    this.specificationSource = specDirectory;
  }

}
