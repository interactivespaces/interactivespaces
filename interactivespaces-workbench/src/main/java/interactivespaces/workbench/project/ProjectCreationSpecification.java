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
 */
public class ProjectCreationSpecification {

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

  public File getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public Map<String, Object> getTemplateData() {
    return templateData;
  }

  public void addTemplateData(Map<String, Object> addData) {
    templateData.putAll(addData);
  }

  public void addTemplateDataEntry(String key, Object value) {
    templateData.put(key, value);
  }

}
