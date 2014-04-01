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
import com.google.common.collect.Maps;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.jdom.JdomProjectReader;
import org.jdom.Element;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A group of projects.
 *
 * @author Trevor Pering
 */
public class GroupProject {

  /**
   * Name of the project type.
   */
  public static final String PROJECT_TYPE_NAME = "group";

  /**
   * The descriptive name of the project.
   */
  private String name;

  /**
   * The description of the project.
   */
  private String description;

  /**
   * Version of the project.
   */
  private Version version;
  /**
   * The source specification directory for the project.
   */
  private File specificationSource;

  /**
   * An extra set of project constituents. Some constituents are common to all
   * projects, these will be specific to an actual project type and will be
   * processed by the specific project type builder as that builder needs.
   */
  private final List<ProjectConstituent> extraConstituents = Lists.newArrayList();

  /**
   * Attributes for this project.
   */
  private final Map<String, String> attributes = Maps.newHashMap();

  /**
   * List of projects in this group.
   */
  private final List<Project> projectList = Lists.newArrayList();

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public File getSpecificationSource() {
    return specificationSource;
  }

  public void setSpecificationSource(File specificationSource) {
    this.specificationSource = specificationSource;
  }
  /**
   * Add an extra constituent to the project.
   *
   * @param constituent
   *          the constituent to add
   */
  public void addExtraConstituent(ProjectConstituent constituent) {
    extraConstituents.add(constituent);
  }

  /**
   * Add extra constituents to the project.
   *
   * @param constituents
   *          the constituents to add, can be {@code null}
   */
  public void addExtraConstituents(List<ProjectConstituent> constituents) {
    if (constituents != null) {
      extraConstituents.addAll(constituents);
    }
  }

  /**
   * Get the list of extra constituents.
   *
   * @return the list of extra constituents
   */
  public List<ProjectConstituent> getExtraConstituents() {
    return extraConstituents;
  }

  /**
   * Add an attribute to this project.
   *
   * @param key
   *          attribute name
   * @param value
   *          attribute value
   */
  public void addAttribute(String key, String value) {
    attributes.put(key, value);
  }

  /**
   * Get an attribute from this project.
   *
   * @param key
   *          attribute key
   *
   * @return attribute value or {@code null} if no value
   */
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * Get the attributes for this project.
   *
   * @return attribute map
   */
  public Map<String, String> getAttributes() {
    return attributes;
  }

}
