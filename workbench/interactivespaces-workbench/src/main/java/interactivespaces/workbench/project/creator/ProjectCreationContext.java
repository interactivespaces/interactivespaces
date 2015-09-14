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

import interactivespaces.util.io.FileCollector;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.group.GroupProjectTemplateSpecification;
import interactivespaces.workbench.tasks.WorkbenchTaskContext;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.Map;

/**
 * Encapsulates the data necessary to create a project. Used in conjunction with the project specification and project
 * creator to ultimately create a project.
 *
 * @author Trevor Pering
 */
public class ProjectCreationContext implements ProjectContext {

  /**
   * Map of the template data for this project creation.
   */
  private final Map<String, Object> templateData = Maps.newTreeMap();

  /**
   * Description of this specification.
   */
  private final String description;

  /**
   * Logger to use for this context.
   */
  private final Log log;

  /**
   * The project itself.
   */
  private Project project;

  /**
   * The project itself.
   */
  private GroupProjectTemplateSpecification groupProjectTemplateSpecification;

  /**
   * The template for the project.
   */
  private File specificationBase;

  /**
   * Base directory where output files should be rooted.
   */
  private File baseDirectory;

  /**
   * The workbench task context.
   */
  private WorkbenchTaskContext workbenchTaskContext;

  /**
   * Create a project creation specification with the given description.
   *
   * @param description
   *          context description
   * @param log
   *          logger to use for this context
   */
  public ProjectCreationContext(String description, Log log) {
    this.description = description;
    this.log = log;
  }

  /**
   * Set the project.
   *
   * @param project
   *          project to set
   */
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * @return get the group project template specification
   */
  public GroupProjectTemplateSpecification getGroupProjectTemplateSpecification() {
    return groupProjectTemplateSpecification;
  }

  /**
   * Set the group project template specification.
   *
   * @param groupProjectTemplateSpecification
   *          thing to set
   */
  public void setGroupProjectTemplateSpecification(
      GroupProjectTemplateSpecification groupProjectTemplateSpecification) {
    this.groupProjectTemplateSpecification = groupProjectTemplateSpecification;
  }

  @Override
  public <T extends Project> T getProject() {
    return (T) project;
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
   * Get the workbench task context.
   *
   * @return the context
   */
  @Override
  public WorkbenchTaskContext getWorkbenchTaskContext() {
    return workbenchTaskContext;
  }

  /**
   * Set the workbench used for creating projects.
   *
   * @param workbenchTaskContext
   *          the workbench task context
   */
  public void setWorkbenchTaskContext(WorkbenchTaskContext workbenchTaskContext) {
    this.workbenchTaskContext = workbenchTaskContext;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ProjectType> T getProjectType() {
    return (T) getWorkbenchTaskContext().getWorkbench().getProjectTypeRegistry().getProjectType(project);
  }

  @Override
  public File getProjectTargetFile(File rootDirectory, String target) {
    throw new UnsupportedOperationException("getProjectTarget not supported for creation context");
  }

  @Override
  public FileCollector getResourceFileCollector() {
    throw new UnsupportedOperationException("getResourceSourceMap not supported for creation context");
  }

  /**
   * @return description of this specification
   */
  public String getDescription() {
    return description;
  }

  @Override
  public Log getLog() {
    return log;
  }
}
