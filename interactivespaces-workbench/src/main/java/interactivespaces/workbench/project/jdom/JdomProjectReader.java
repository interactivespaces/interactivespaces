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

package interactivespaces.workbench.project.jdom;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.ProjectDeployment;
import interactivespaces.workbench.project.ProjectReader;
import interactivespaces.workbench.project.activity.ActivityProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectAssemblyConstituent;
import interactivespaces.workbench.project.constituent.ProjectBundleConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;
import interactivespaces.workbench.project.library.LibraryProjectConstituent;

import com.google.common.collect.Maps;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A {@link interactivespaces.workbench.project.ProjectReader} based on JDOM.
 *
 * @author Keith M. Hughes
 */
public class JdomProjectReader extends JdomReader implements ProjectReader {

  /**
   * The default version range for projects which do not specify a version
   * range.
   */
  public static final VersionRange INTERACTIVESPACES_VERSION_RANGE_DEFAULT = new VersionRange(new Version(1, 0, 0),
      new Version(2, 0, 0), false);

  /**
   * Element name for a JDOM project specification.
   */
  public static final String PROJECT_ELEMENT_NAME_PROJECT = "project";

  /**
   * Element name for a group of projects.
   */
  public static final String GROUP_ELEMENT_NAME = "projects";

  /**
   * Add all the base constituent types to the static map.
   */
  {
    addConstituentType(new ProjectResourceConstituent.ProjectResourceBuilderFactory());
    addConstituentType(new ProjectResourceConstituent.ProjectSourceBuilderFactory());
    addConstituentType(new ProjectAssemblyConstituent.ProjectAssemblyConstituentFactory());
    addConstituentType(new ProjectBundleConstituent.ProjectBundleConstituentFactory());
    addConstituentType(new ActivityProjectConstituent.ActivityProjectBuilderFactory());
    addConstituentType(new LibraryProjectConstituent.LibraryProjectBuilderFactory());
  }

  /**
   * The name of the project.
   */
  public static final String PROJECT_ELEMENT_NAME_NAME = "name";

  /**
   * The description of the project.
   */
  public static final String PROJECT_ELEMENT_NAME_DESCRIPTION = "description";

  /**
   * The identifying name of the project.
   */
  public static final String PROJECT_ELEMENT_NAME_IDENTIFYING_NAME = "identifyingName";

  /**
   * The project attribute for the project type.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_TYPE = "type";

  /**
   * The project attribute for the project builder.
   */
  public static final String PROJECT_ATTRIBUTE_NAME_PROJECT_BUILDER = "builder";

  /**
   * The project attribute for the version of Interactive Spaces which is
   * required.
   */
  public static final String PROJECT_ATTRIBUTE_NAME_PROJECT_INTERACTIVE_SPACES_VERSION = "interactiveSpacesVersion";

  /**
   * The version of the project.
   */
  public static final String PROJECT_ELEMENT_NAME_VERSION = "version";

  /**
   * The base directory of the project.
   */
  public static final String PROJECT_ELEMENT_NAME_BASE_DIRECTORY = "baseDirectory";

  /**
   * Project definition file element name for resources.
   */
  private static final String PROJECT_ELEMENT_RESOURCES_NAME = "resources";

  /**
   * Project definition file element name for sources.
   */
  private static final String PROJECT_ELEMENT_NAME_SOURCES = "sources";

  /**
   * Project definition file element name for metadata.
   */
  private static final String PROJECT_ELEMENT_NAME_METADATA = "metadata";

  /**
   * Project definition file element name for metadata.
   */
  private static final String PROJECT_ELEMENT_NAME_METADATA_ITEM = "item";

  /**
   * Project definition file element name for metadata.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_METADATA_ITEM_NAME = "name";

  /**
   * Project definition file element name for dependencies.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPENDENCIES = "dependencies";

  /**
   * Project definition file element name for a dependency item.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPENDENCY_ITEM = "dependency";

  /**
   * Project definition file attribute name for the name of a dependency.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_NAME = "name";

  /**
   * Project definition file attribute name for the minimum version of a
   * dependency.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MINIMUM = "minimumVersion";

  /**
   * Project definition file attribute name for the maximum version of a
   * dependency.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MAXIMUM = "maximumVersion";

  /**
   * Project definition file attribute name for whether a dependency is
   * required.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_REQUIRED = "required";

  /**
   * Project definition file element name for deployments.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPLOYMENTS = "deployments";

  /**
   * Project definition file element name for a deployment item.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPLOYMENT_ITEM = "deployment";

  /**
   * Project definition file attribute name for the type of a deployment.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_TYPE = "type";

  /**
   * Project definition file attribute name for the method of a deployment.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_METHOD = "method";

  /**
   * Project definition file attribute name for the location for a deployment.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_LOCATION = "location";

  /**
   * Project definition file element name for configurations.
   */
  private static final String PROJECT_ELEMENT_NAME_CONFIGURATION = "configuration";

  /**
   * Project definition file attribute name for the name of a configuration
   * item.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_CONFIGURATION_ITEM_NAME = "name";

  /**
   * Construct a project reader.
   *
   * @param workbench
   *          containing workbench instance
   */
  public JdomProjectReader(InteractiveSpacesWorkbench workbench) {
    super(workbench);
  }

  @Override
  public Project readProject(File projectFile) {
    Element rootElement = getRootElement(projectFile);

    Project project = makeProjectFromElement(rootElement);
    project.setBaseDirectory(projectFile.getParentFile());
    return project;
  }

  /**
   * Process an element and return a new project.
   *
   * @param projectElement
   *          element to process
   *
   * @return project representing the element
   */
  Project makeProjectFromElement(Element projectElement) {
    Namespace projectNamespace = projectElement.getNamespace();

    if (!PROJECT_ELEMENT_NAME_PROJECT.equals(projectElement.getName())) {
      throw new SimpleInteractiveSpacesException("Invalid project root element name " + projectElement.getName());
    }

    String projectType = getProjectType(projectElement);
    Project project = getWorkbench().getProjectTypeRegistry().newProject(projectType);

    processPrototypeChain(project, projectNamespace, projectElement);
    configureProjectFromElement(project, projectNamespace, projectElement);

    if (project.getInteractiveSpacesVersionRange() == null) {
      getLog().warn(
          "Did not specify a range of needed Interactive Spaces versions. Setting default to "
              + INTERACTIVESPACES_VERSION_RANGE_DEFAULT);
      project.setInteractiveSpacesVersionRange(INTERACTIVESPACES_VERSION_RANGE_DEFAULT);
    }

    if (failure) {
      throw new SimpleInteractiveSpacesException("Project specification had errors");
    }

    return project;
  }

  /**
   * Process the prototype chain for the given element.
   *
   * @param project
   *          project where the results go
   * @param projectNamespace
   *          XML namespace for project elements
   * @param projectElement
   *          prototype chain root to follow
   */
  private void processPrototypeChain(Project project, Namespace projectNamespace, Element projectElement) {
    if (getJdomPrototypeProcessor() != null) {
      List<Element> prototypeChain = getJdomPrototypeProcessor().getPrototypeChain(projectElement);
      for (Element prototype : prototypeChain) {
        configureProjectFromElement(project, projectNamespace, prototype);
      }

      // Remove the not-useful prototype's name, since it would incorrectly be
      // naming this element.
      project.getAttributes().remove(JdomPrototypeProcessor.PROTOTYPE_NAME_ATTRIBUTE);
    }
  }

  /**
   * Configure a project given an element.
   *
   * @param project
   *          project to configure
   * @param projectNamespace
   *          XML namespace for the project element
   * @param projectElement
   *          input element
   */
  private void configureProjectFromElement(Project project, Namespace projectNamespace, Element projectElement) {
    getProjectAttributes(project, projectElement);
    getMainData(project, projectNamespace, projectElement);
    getMetadata(project, projectNamespace, projectElement);
    getDependencies(project, projectNamespace, projectElement);
    getConfiguration(project, projectNamespace, projectElement);

    project.addResources(getContainerConstituents(projectNamespace,
        projectElement.getChild(PROJECT_ELEMENT_RESOURCES_NAME, projectNamespace), project));
    project.addSources(getContainerConstituents(projectNamespace,
        projectElement.getChild(PROJECT_ELEMENT_NAME_SOURCES, projectNamespace), project));

    project.addExtraConstituents(getContainerConstituents(projectNamespace,
        projectElement.getChild(PROJECT_ELEMENT_NAME_TEMPLATES, projectNamespace), project));

    project.addExtraConstituents(getIndividualConstituent(projectNamespace,
        projectElement.getChild(ActivityProjectConstituent.ACTIVITY_ELEMENT, projectNamespace), project));
    project.addExtraConstituents(getIndividualConstituent(projectNamespace,
        projectElement.getChild(LibraryProjectConstituent.LIBRARY_ELEMENT, projectNamespace), project));

    getDeployments(project, projectNamespace, projectElement);
  }

  /**
   * Get the project type from the root element.
   *
   * @param rootElement
   *          the root element of the XML doc
   *
   * @return the project type
   */
  private String getProjectType(Element rootElement) {
    return getRequiredAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_TYPE);
  }

  /**
   * Get the main data from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param projectNamespace
   *          XML namespace for the project
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getMainData(Project project, Namespace projectNamespace, Element rootElement) {
    project.setName(getChildTextTrimmed(rootElement, projectNamespace, PROJECT_ELEMENT_NAME_NAME, project.getName()));
    project.setDescription(getChildTextTrimmed(rootElement, projectNamespace, PROJECT_ELEMENT_NAME_DESCRIPTION,
        project.getDescription()));
    project.setIdentifyingName(getChildTextTrimmed(rootElement, projectNamespace,
        PROJECT_ELEMENT_NAME_IDENTIFYING_NAME, project.getIdentifyingName()));

    String version = getChildTextTrimmed(rootElement, projectNamespace, PROJECT_ELEMENT_NAME_VERSION);
    if (version != null) {
      project.setVersion(Version.parseVersion(version));
    }

    String baseDirectory = getChildTextTrimmed(rootElement, projectNamespace, PROJECT_ELEMENT_NAME_BASE_DIRECTORY);
    if (baseDirectory != null) {
      project.setBaseDirectory(new File(baseDirectory));
    }

    project.setBuilderType(getAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_BUILDER,
        project.getBuilderType()));

    String interactiveSpacesVersionRangeAttribute =
        getAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_INTERACTIVE_SPACES_VERSION, null);

    if (interactiveSpacesVersionRangeAttribute != null) {
      VersionRange versionRange = VersionRange.parseVersionRange(interactiveSpacesVersionRangeAttribute);
      project.setInteractiveSpacesVersionRange(versionRange);
    }
  }

  /**
   * Get any attributes attached to the project.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getProjectAttributes(Project project, Element rootElement) {
    @SuppressWarnings("unchecked")
    List<Attribute> attributes = rootElement.getAttributes();
    for (Attribute attribute : attributes) {
      project.addAttribute(attribute.getName(), attribute.getValue());
    }
  }

  /**
   * Get the metadata from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param projectNamespace
   *          XML namespace for the project
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getMetadata(Project project, Namespace projectNamespace, Element rootElement) {
    Element metadataElement = rootElement.getChild(PROJECT_ELEMENT_NAME_METADATA, projectNamespace);

    if (metadataElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> itemElements = metadataElement.getChildren(PROJECT_ELEMENT_NAME_METADATA_ITEM, projectNamespace);

      Map<String, Object> metadata = Maps.newHashMap();
      for (Element itemElement : itemElements) {
        String name = getRequiredAttributeValue(itemElement, PROJECT_ATTRIBUTE_NAME_METADATA_ITEM_NAME);
        String value = itemElement.getTextNormalize();
        metadata.put(name, value);
      }

      project.setMetadata(metadata);
    }
  }

  /**
   * Get the configuration from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param projectNamespace
   *          XML namespace for the project
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getConfiguration(Project project, Namespace projectNamespace, Element rootElement) {
    Element configurationElement = rootElement.getChild(PROJECT_ELEMENT_NAME_CONFIGURATION, projectNamespace);

    if (configurationElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> propertyElements =
          configurationElement.getChildren(ActivityProjectConstituent.PROPERTY_ELEMENT_NAME, projectNamespace);

      Configuration configuration = project.getConfiguration();
      for (Element propertyElement : propertyElements) {
        String name = getRequiredAttributeValue(propertyElement, PROJECT_ATTRIBUTE_NAME_CONFIGURATION_ITEM_NAME);
        if (name == null) {
          continue;
        }
        String value = propertyElement.getTextNormalize();
        configuration.setValue(name, value);
      }
    }
  }

  /**
   * Get the resources from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param projectNamespace
   *          namespace for the project
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getDependencies(Project project, Namespace projectNamespace, Element rootElement) {
    Element dependenciesElement = rootElement.getChild(PROJECT_ELEMENT_NAME_DEPENDENCIES, projectNamespace);

    if (dependenciesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> dependencyElements =
          dependenciesElement.getChildren(PROJECT_ELEMENT_NAME_DEPENDENCY_ITEM, projectNamespace);

      for (Element dependencyElement : dependencyElements) {
        ProjectDependency dependency = getDependency(dependencyElement);
        project.addDependency(dependency);
      }
    }
  }

  /**
   * Get an project dependency from the dependency element.
   *
   * @param dependencyElement
   *          the element containing the data
   *
   * @return the dependency found in the element
   */
  private ProjectDependency getDependency(Element dependencyElement) {
    String name = getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_NAME);
    if (name == null) {
      addError("Dependency has no name");
    }

    String minimumVersion =
        getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MINIMUM);
    String maximumVersion =
        getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MAXIMUM);

    if (minimumVersion != null) {
      if (maximumVersion == null) {
        maximumVersion = minimumVersion;
      }
    } else if (maximumVersion != null) {
      // If here was no minimum version
      minimumVersion = maximumVersion;
    } else {
      addError("Dependency has no version constraints");
    }

    String requiredString =
        getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_REQUIRED, "true");

    ProjectDependency dependency = new ProjectDependency();

    dependency.setName(name);
    dependency.setMinimumVersion(Version.parseVersion(minimumVersion));
    dependency.setMaximumVersion(Version.parseVersion(maximumVersion));
    dependency.setRequired("true".equals(requiredString));

    return dependency;
  }

  /**
   * Get the deployments from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param projectNamespace
   *          XML namespace for the project
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getDeployments(Project project, Namespace projectNamespace, Element rootElement) {
    Element deploymentsElement = rootElement.getChild(PROJECT_ELEMENT_NAME_DEPLOYMENTS, projectNamespace);

    if (deploymentsElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> deploymentElements =
          deploymentsElement.getChildren(PROJECT_ELEMENT_NAME_DEPLOYMENT_ITEM, projectNamespace);

      for (Element deploymentElement : deploymentElements) {
        ProjectDeployment deployment = getDeployment(deploymentElement);
        if (deployment != null) {
          project.addDeployment(deployment);
        }
      }
    }
  }

  /**
   * Get an project deployment from the deployment element.
   *
   * @param deploymentElement
   *          the element containing the data
   *
   * @return the deployment found in the element
   */
  private ProjectDeployment getDeployment(Element deploymentElement) {
    String type = getRequiredAttributeValue(deploymentElement, PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_TYPE);
    String method = getAttributeValue(deploymentElement, PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_METHOD);
    String location = getAttributeValue(deploymentElement, PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_LOCATION);

    // TODO(keith): Enumerate all possible errors

    return new ProjectDeployment(type, method, location);
  }

}
