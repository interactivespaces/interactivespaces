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

package interactivespaces.workbench.project.creator;

import com.google.common.collect.Maps;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.activity.type.ProjectType;

import java.io.File;
import java.util.Map;

/**
 * Encapsulates the data necessary to create a project. Used in conjunction with the project specification and
 * project creator to ultimately create a project.
 *
 * @author Trevor Pering
 */
public class ProjectCreationContext implements ProjectContext {

  /**
   * Map of the template data for this project creation.
   */
  private final Map<String, Object> templateData = Maps.newTreeMap();

  /**
   * Description of this specificaiton.
   */
  private final String description;

  /**
   * The project itself.
   */
  private Project project;

  /**
   * The template for the project.
   */
  private File specificationBase;

  /**
   * Base directory where output files should be rooted.
   */
  private File baseDirectory;

  /**
   * Containing workbench.
   */
  private InteractiveSpacesWorkbench workbench;

  /**
   * Create a project creation specification with the given description.
   *
   * @param description
   *          description of this specification
   */
  public ProjectCreationContext(String description) {
    this.description = description;
  }

  @Override
  public Project getProject() {
    return project;
  }

  /**
   * Set the project description for the spec.
   *
   * @param project
   *          the project to set
   */
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Get the project template.
   *
   * @return the template
   */
  public File getSpecificationBase() {
    return specificationBase;
  }

  /**
   * Set the project template.
   *
   * @param specification
   *          the template to set
   */
  public void setSpecificationBase(File specification) {
    this.specificationBase = specification;
  }

  /**
   * @return base directory for creating a project
   */
  public File getBaseDirectory() {
    return baseDirectory;
  }

  /**
   * Set the base directory.
   *
   * @param baseDirectory
   *          base directory where to create the project
   */
  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * Get the template data to use for creation.
   *
   * @return template data to use
   */
  public Map<String, Object> getTemplateData() {
    return templateData;
  }

  /**
   * Add all from a set of template data to this template data.
   *
   * @param addData
   *          data to add
   */
  public void addTemplateData(Map<String, Object> addData) {
    templateData.putAll(addData);
  }

  /**
   * Add a single template data entry to this creation specification.
   *
   * @param key
   *          data key
   * @param value
   *          data value
   */
  public void addTemplateDataEntry(String key, Object value) {
    templateData.put(key, value);
  }

  /**
   * Get the workbench used for creating projects.
   *
   * @return workbench
   */
  public InteractiveSpacesWorkbench getWorkbench() {
    return workbench;
  }

  /**
   * Set the workbench used for creating projects.
   *
   * @param workbench
   *          workbench used
   */
  public void setWorkbench(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  @Override
  public <T extends ProjectType> T getProjectType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public File getProjectTarget(File rootDirectory, String target) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Map<File, File> getResourceSourceMap() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * @return description of this specification
   */
  public String getDescription() {
    return description;
  }
}
