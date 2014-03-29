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

import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;

/**
 * Encapsulates the data necessary to create a project. Used in conjunction with the project specification and
 * project creator to ultimately create a project.
 *
 * @author Trevor Pering
 */
public class ProjectCreationSpecification {

  /**
   * Map of the template data for this project creation.
   */
  private final Map<String, Object> templateData = Maps.newTreeMap();

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
   * Get the project description associated with the spec.
   *
   * @param <T>
   *          the project type
   *
   * @return the project
   */
  @SuppressWarnings("unchecked")
  public <T extends Project> T getProject() {
    return (T) project;
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

}
