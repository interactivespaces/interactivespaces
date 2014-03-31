/*
 * Copyright (C) 2013 Google Inc.
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * An Interactive Spaces project.
 *
 * @author Keith M. Hughes
 */
public abstract class Project {

  /**
   * The base directory for the project.
   */
  private File baseDirectory;

  /**
   * The source specification directory for the project.
   */
  private File specificationSource;

  /**
   * The type of builder for the project.
   */
  private String builderType;

  /**
   * The type of the project.
   */
  private String type;

  /**
   * The identifying name of the project.
   */
  private String identifyingName;

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
   * The version range for interactive spaces for the project.
   */
  private VersionRange interactiveSpacesVersion;

  /**
   * The dependencies the project has.
   */
  private final List<ProjectDependency> dependencies = Lists.newArrayList();

  /**
   * The resources the project requires. A null value indicates that no
   * resources have been specified for the project (which is slightly different
   * than saying an empty set of resources have been specified, which would be
   * indicated by the empty list).
   */
  private final List<ProjectConstituent> resources = Lists.newArrayList();

  /**
   * The sources the project requires. A null value indicates that no sources
   * have been specified for the project (which is slightly different than
   * saying an empty set of sources have been specified, which would be
   * indicated by the empty list).
   */
  private final List<ProjectConstituent> sources = Lists.newArrayList();

  /**
   * An extra set of project constituents. Some constituents are common to all
   * projects, these will be specific to an actual project type and will be
   * processed by the specific project type builder as that builder needs.
   */
  private final List<ProjectConstituent> extraConstituents = Lists.newArrayList();

  /**
   * The deployments the project requires.
   */
  private final List<ProjectDeployment> deployments = Lists.newArrayList();

  /**
   * The meta data for this project.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  /**
   * Attributes for this project.
   */
  private final Map<String, String> attributes = Maps.newHashMap();

  /**
   * The configuration for this project.
   */
  private final Configuration configuration = SimpleConfiguration.newConfiguration();

  /**
   * Get the type of the project.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Set the type of the project.
   *
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Set the base directory for the project.
   *
   * @param baseDirectory
   *          the base directory for the project
   */
  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * Get the base directory of the project.
   *
   * @return the base directory
   */
  public File getBaseDirectory() {
    return baseDirectory;
  }

  /**
   * Get the specification source for this project.
   *
   * @return specification source
   */
  public File getSpecificationSource() {
    return specificationSource;
  }

  /**
   * Set the specification source for this project.
   *
   * @param specificationSource
   *          specification source
   */
  public void setSpecificationSource(File specificationSource) {
    this.specificationSource = specificationSource;
  }

  /**
   * Get the type of builder for the project.
   *
   * @return the builder type
   */
  public String getBuilderType() {
    return builderType;
  }

  /**
   * Set the builder type of the project.
   *
   * @param builderType
   *          the builder type to set
   */
  public void setBuilderType(String builderType) {
    this.builderType = builderType;
  }

  /**
   * Get the identifying name for the project.
   *
   * @return the identifying name
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Set the identifying name for the project.
   *
   * @param identifyingName
   *          the identifying name
   */
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  /**
   * Get the descriptive name for the project.
   *
   * @return the descriptive name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the descriptive name for the project.
   *
   * @param name
   *          the descriptive name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the description of the project.
   *
   * @return the description. Can be {@code null}.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of the project.
   *
   * @param description
   *          the description. Can be {@code null}.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the version for the project.
   *
   * @return The version
   */
  public Version getVersion() {
    return version;
  }

  /**
   * Set the version for the project.
   *
   * @param version
   *          the version
   */
  public void setVersion(Version version) {
    this.version = version;
  }

  /**
   * Get the range of Interactive Spaces versions that this project requires.
   *
   * @return the range of Interactive Spaces versions that this project requires
   */
  public VersionRange getInteractiveSpacesVersionRange() {
    return interactiveSpacesVersion;
  }

  /**
   * Set the range of Interactive Spaces versions that this project requires.
   *
   * @param interactiveSpacesVersionRange
   *          the range of Interactive Spaces versions that this project
   *          requires
   */
  public void setInteractiveSpacesVersionRange(VersionRange interactiveSpacesVersionRange) {
    this.interactiveSpacesVersion = interactiveSpacesVersionRange;
  }

  /**
   * Add a dependency to the project.
   *
   * @param dependency
   *          the dependency to add
   */
  public void addDependency(ProjectDependency dependency) {
    dependencies.add(dependency);
  }

  /**
   * Get a list of all dependencies the project has.
   *
   * @return a copy of the dependencies list
   */
  public List<ProjectDependency> getDependencies() {
    return Lists.newArrayList(dependencies);
  }

  /**
   * Add a deployment to the project.
   *
   * @param deployment
   *          the deployment to add
   */
  public void addDeployment(ProjectDeployment deployment) {
    deployments.add(deployment);
  }

  /**
   * Get a list of all deployments the project has.
   *
   * @return a copy of the deployments list
   */
  public List<ProjectDeployment> getDeployments() {
    return Lists.newArrayList(deployments);
  }

  /**
   * Add resources to the project.
   *
   * @param addResources
   *          the resources to add
   */
  public void addResources(List<ProjectConstituent> addResources) {
    if (addResources != null) {
      resources.addAll(addResources);
    }
  }

  /**
   * Get a list of all resources the project has.
   *
   * @return a new copy of the resources list
   */
  public List<ProjectConstituent> getResources() {
    return Lists.newArrayList(resources);
  }

  /**
   * Add sources to the project.
   *
   * @param addSources
   *          the sources to add
   */
  public void addSources(List<ProjectConstituent> addSources) {
    if (addSources != null) {
      sources.addAll(addSources);
    }
  }

  /**
   * Get a list of all sources the project has.
   *
   * @return a new copy of the sources list
   */
  public List<ProjectConstituent> getSources() {
    return Lists.newArrayList(sources);
  }

  /**
   * Add a project resource to this project.
   *
   * @param resource
   *          the project resource
   */
  public void addResource(ProjectResourceConstituent resource) {
    resources.add(resource);
  }

  /**
   * Set the metadata for the project.
   *
   * <p>
   * This removes the old metadata completely.
   *
   * @param metadata
   *          the metadata for the project (can be {@link null}
   */
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  /**
   * Get the metadata for the project.
   *
   * @return the project's meta data
   */
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  /**
   * Get the configuration for the project.
   *
   * @return the project's configurations
   */
  public Configuration getConfiguration() {
    return configuration;
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
}
