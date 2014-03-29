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

package interactivespaces.workbench.jdom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.jdom.JdomReader;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.ProjectDeployment;
import interactivespaces.workbench.project.ProjectReader;
import interactivespaces.workbench.project.TemplateFile;
import interactivespaces.workbench.project.TemplateVar;
import interactivespaces.workbench.project.activity.ActivityProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectAssemblyConstituent;
import interactivespaces.workbench.project.constituent.ProjectBundleConstituent;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;
import interactivespaces.workbench.project.constituent.ProjectTemplateConstituent;
import interactivespaces.workbench.jdom.JdomPrototypeManager;
import org.jdom.Attribute;
import org.jdom.Element;

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
  public static final String ELEMENT_NAME = "project";

  /**
   * Element name for a group of projects.
   */
  public static final String GROUP_ELEMENT_NAME = "projects";

  /**
   * Map of resource types to resource builders.
   */
  private static final Map<String, ProjectConstituent.ProjectConstituentFactory> PROJECT_CONSTITUENT_FACTORY_MAP =
      Maps.newHashMap();

  /**
   * Add all the base constituent types to the static map.
   */
  static {
    addConstituentType(new ProjectResourceConstituent.ProjectResourceBuilderFactory());
    addConstituentType(new ProjectResourceConstituent.ProjectSourceBuilderFactory());
    addConstituentType(new ProjectAssemblyConstituent.ProjectAssemblyConstituentFactory());
    addConstituentType(new ProjectBundleConstituent.ProjectBundleConstituentFactory());
    addConstituentType(new ProjectTemplateConstituent.ProjectTemplateConstituentFactory());
    addConstituentType(new ActivityProjectConstituent.ActivityProjectBuilderFactory());
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
  private static final String PROJECT_ELEMENT_NAME_IDENTIFYING_NAME = "identifyingName";

  /**
   * The project attribute for the project type.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_TYPE = "type";

  /**
   * The project attribute for the project builder.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_BUILDER = "builder";

  /**
   * The project attribute for the version of Interactive Spaces which is
   * required.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_INTERACTIVE_SPACES_VERSION = "interactiveSpacesVersion";

  /**
   * The version of the project.
   */
  public static final String PROJECT_ELEMENT_NAME_VERSION = "version";

  /**
   * The base directory of the project.
   */
  private static final String PROJECT_ELEMENT_NAME_BASE_DIRECTORY = "baseDirectory";

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
   * Prototype manager to use when reading/constructing projects.
   */
  private JdomPrototypeManager jdomPrototypeManager;

  /**
   * Construct a project reader.
   *
   * @param workbench
   *          containing workbench instance
   */
  public JdomProjectReader(InteractiveSpacesWorkbench workbench) {
    super(workbench);
  }

  /**
   * Add a constituent type to the factory.
   *
   * @param constituentFactory
   *          factory to add
   */
  private static void addConstituentType(ProjectConstituent.ProjectConstituentFactory constituentFactory) {
    PROJECT_CONSTITUENT_FACTORY_MAP.put(constituentFactory.getName(), constituentFactory);
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
    if (!ELEMENT_NAME.equals(projectElement.getName())) {
      throw new SimpleInteractiveSpacesException("Invalid project root element name " + projectElement.getName());
    }

    String projectType = getProjectType(projectElement);
    Project project = getWorkbench().getProjectTypeRegistry().newProject(projectType);

    if (jdomPrototypeManager != null) {
      List<Element> prototypeChain = jdomPrototypeManager.getPrototypeChain(projectElement);
      for (Element prototype : prototypeChain) {
        configureProjectFromElement(project, prototype);
      }
    }
    configureProjectFromElement(project, projectElement);

    if (project.getInteractiveSpacesVersionRange() == null) {
      getLog().warn("Did not specify a range of needed Interactive Spaces versions. Setting default to "
          + INTERACTIVESPACES_VERSION_RANGE_DEFAULT);
      project.setInteractiveSpacesVersionRange(INTERACTIVESPACES_VERSION_RANGE_DEFAULT);
    }

    if (failure) {
      throw new SimpleInteractiveSpacesException("Project specification had errors");
    }

    return project;
  }

  /**
   * Configure a project given an element.
   *
   * @param project
   *          project to configure
   * @param projectElement
   *          input element
   */
  private void configureProjectFromElement(Project project, Element projectElement) {
    getProjectAttributes(project, projectElement);
    getMainData(project, projectElement);
    getMetadata(project, projectElement);
    getDependencies(project, projectElement);
    getConfiguration(project, projectElement);
    getTemplateVars(project, projectElement);
    project.addResources(getContainerConstituents(projectElement.getChild(PROJECT_ELEMENT_RESOURCES_NAME), project));
    project.addSources(getContainerConstituents(projectElement.getChild(PROJECT_ELEMENT_NAME_SOURCES), project));

    getContainerConstituents(projectElement.getChild(TemplateFile.GROUP_ELEMENT_NAME), project);

    project.addExtraConstituents(getIndividualConstituent(
        projectElement.getChild(ActivityProjectConstituent.ACTIVITY_ELEMENT), project));

    getDeployments(project, projectElement);
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
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getMainData(Project project, Element rootElement) {
    project.setName(
        getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_NAME, project.getName()));
    project.setDescription(
        getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_DESCRIPTION, project.getDescription()));
    project.setIdentifyingName(
        getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_IDENTIFYING_NAME, project.getIdentifyingName()));

    String version = getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_VERSION);
    if (version != null) {
      project.setVersion(Version.parseVersion(version));
    }

    String baseDirectory = getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_BASE_DIRECTORY);
    if (baseDirectory != null) {
      project.setBaseDirectory(new File(baseDirectory));
    }

    project.setBuilderType(
        getAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_BUILDER, project.getBuilderType()));

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
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getMetadata(Project project, Element rootElement) {
    Element metadataElement = rootElement.getChild(PROJECT_ELEMENT_NAME_METADATA);

    if (metadataElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> itemElements = metadataElement.getChildren(PROJECT_ELEMENT_NAME_METADATA_ITEM);

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
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getConfiguration(Project project, Element rootElement) {
    Element configurationElement = rootElement.getChild(PROJECT_ELEMENT_NAME_CONFIGURATION);

    if (configurationElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> propertyElements =
          configurationElement.getChildren(ActivityProjectConstituent.PROPERTY_ELEMENT_NAME);

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
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getDependencies(Project project, Element rootElement) {
    Element dependenciesElement = rootElement.getChild(PROJECT_ELEMENT_NAME_DEPENDENCIES);

    if (dependenciesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> dependencyElements = dependenciesElement.getChildren(PROJECT_ELEMENT_NAME_DEPENDENCY_ITEM);

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
   * Add the constituents from a container in the document the document.
   *
   * @param containerElement
   *          root element of the XML DOM containing the project data
   * @param project
   *          the project being read
   *
   * @return the constituents for the project
   */
  private List<ProjectConstituent> getContainerConstituents(Element containerElement, Project project) {
    if (containerElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();
    @SuppressWarnings("unchecked")
    List<Element> childElements = containerElement.getChildren();

    for (Element childElement : childElements) {
      getConstituent(childElement, project, constituents);
    }

    return constituents;
  }

  /**
   * Add the constituents from a container in the document the document.
   *
   * @param constituentElement
   *          XML element containing the constituent data
   * @param project
   *          the project being read
   *
   * @return the constituents for the element
   */
  private List<ProjectConstituent> getIndividualConstituent(Element constituentElement, Project project) {
    if (constituentElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();

    getConstituent(constituentElement, project, constituents);

    return constituents;
  }

  /**
   * Get the constituent from the element which describes it..
   *
   * @param constituentElement
   *          the element containing the constituent
   * @param project
   *          the project being built
   * @param constituents
   *          the list of constituents currently being extracted
   */
  private void getConstituent(Element constituentElement, Project project, List<ProjectConstituent> constituents) {
    String type = constituentElement.getName();
    ProjectConstituent.ProjectConstituentFactory factory = PROJECT_CONSTITUENT_FACTORY_MAP.get(type);
    if (factory == null) {
      addError(String.format("Unknown resource type '%s'", type));
    } else {
      ProjectConstituent constituent =
          factory.newBuilder(getLog()).buildConstituentFromElement(constituentElement, project);
      if (constituent != null) {
        constituents.add(constituent);
      }
    }
  }

  /**
   * Get the deployments from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getDeployments(Project project, Element rootElement) {
    Element deploymentsElement = rootElement.getChild(PROJECT_ELEMENT_NAME_DEPLOYMENTS);

    if (deploymentsElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> deploymentElements = deploymentsElement.getChildren(PROJECT_ELEMENT_NAME_DEPLOYMENT_ITEM);

      for (Element deploymentElement : deploymentElements) {
        ProjectDeployment deployment = getDeployment(deploymentElement);
        if (deployment != null) {
          project.addDeployment(deployment);
        }
      }
    }
  }

  /**
   * Get the template vars from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getTemplateVars(Project project, Element rootElement) {
    Element varsElement = rootElement.getChild(TemplateVar.GROUP_ELEMENT_NAME);

    if (varsElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> varElements = varsElement.getChildren(TemplateVar.ELEMENT_NAME);

      for (Element varElement : varElements) {
        project.addTemplateVar(getTemplateVarFromElement(varElement));
      }
    }
  }

  /**
   * Get a template variable from a given element.
   *
   * @param element
   *          element to parse
   *
   * @return template variable for the element
   */
  private TemplateVar getTemplateVarFromElement(Element element) {
    String name = getRequiredAttributeValue(element, TemplateVar.NAME_ATTRIBUTE_NAME);
    String value = getRequiredAttributeValue(element, TemplateVar.VALUE_ATTRIBUTE_NAME);
    return new TemplateVar(name, value);
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

  /**
   * Get the prototype manager for this reader.
   *
   * @return prototype manager
   */
  JdomPrototypeManager getJdomPrototypeManager() {
    return jdomPrototypeManager;
  }

  /**
   * Set the prototype manager used by this reader.
   *
   * @param jdomPrototypeManager
   *          prototype manager to use
   */
  void setJdomPrototypeManager(JdomPrototypeManager jdomPrototypeManager) {
    this.jdomPrototypeManager = jdomPrototypeManager;
  }

}
