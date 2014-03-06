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

package interactivespaces.workbench.project;

import interactivespaces.domain.basic.pojo.SimpleConfigurationParameter;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A specification for creating a project.
 *
 * @author Keith M. Hughes
 */
public class ProjectCreationSpecification {

  /**
   * The language for the project.
   */
  private String language;

  /**
   * The template for the project.
   */
  private ProjectTemplate template;

  /**
   * The project itself.
   */
  private Project project;

  /**
   * Extra configuration parameters for the activity.
   */
  private final List<SimpleConfigurationParameter> extraConfigurationParameters = Lists.newArrayList();

  /**
   * A list of all source directories needed.
   */
  private final List<String> sourceDirectories = Lists.newArrayList();

  /**
   * Get the programming language for the project.
   *
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set the programming language for the project.
   *
   * @param language
   *          the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Get the project template.
   *
   * @return the template
   */
  public ProjectTemplate getTemplate() {
    return template;
  }

  /**
   * Set the project template.
   *
   * @param template
   *          the template to set
   */
  public void setTemplate(ProjectTemplate template) {
    this.template = template;
  }

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
   * Get all source directories needed by the project.
   *
   * @return the source directories
   */
  public List<String> getSourceDirectories() {
    return sourceDirectories;
  }

  /**
   * Add a new source directory to the spec.
   *
   * @param sourceDirectory
   *          the new source directory
   */
  public void addSourceDirectory(String sourceDirectory) {
    sourceDirectories.add(sourceDirectory);
  }

  /**
   * Get all extra configurations for the activity.
   *
   * @return all extra configurations for the activity
   */
  public List<SimpleConfigurationParameter> getExtraConfigurationParameters() {
    return extraConfigurationParameters;
  }

  /**
   * Add an extra configuration parameter to the spec.
   *
   * @param parameter
   *          the new configuration parameter to add
   */
  public void addExtraConfigurationParameter(SimpleConfigurationParameter parameter) {
    extraConfigurationParameters.add(parameter);
  }

  /**
   * Add an extra configuration parameter to the spec.
   *
   * @param name
   *          the name of the parameter to add
   * @param value
   *          the value of the parameter to add
   */
  public void addExtraConfigurationParameter(String name, String value) {
    extraConfigurationParameters.add(new SimpleConfigurationParameter(name, value));
  }
}
