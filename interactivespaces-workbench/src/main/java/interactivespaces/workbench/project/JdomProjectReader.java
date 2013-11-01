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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.constituent.ProjectAssemblyConstituent;
import interactivespaces.workbench.project.constituent.ProjectBundleConstituent;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A {@link ProjectReader} based on JDOM.
 *
 * @author Keith M. Hughes
 */
public class JdomProjectReader implements ProjectReader {

  /**
   * Map of resource types to resource builders.
   */
  private static final Map<String, ProjectConstituent.Builder> PROJECT_CONSTITUENT_BUILDER_MAP =
      ImmutableMap.of(
          ProjectResourceConstituent.PROJECT_TYPE, new ProjectResourceConstituent.Builder(),
          ProjectResourceConstituent.PROJECT_TYPE_ALTERNATE, new ProjectResourceConstituent.Builder(),
          ProjectAssemblyConstituent.PROJECT_TYPE, new ProjectAssemblyConstituent.Builder(),
          ProjectBundleConstituent.PROJECT_TYPE, new ProjectBundleConstituent.Builder()
      );

  /**
   * Project definition file element name for resources.
   */
  private static final String PROJECT_RESOURCES_ELEMENT_NAME = "resources";

  /**
   * Project definition file element name for sources.
   */
  private static final String PROJECT_SOURCES_ELEMENT_NAME = "sources";

  @Override
  public Project readDescription(File projectFile) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(projectFile);
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(inputStream);

      Element rootElement = doc.getRootElement();

      Project project = new Project();
      project.setType(getProjectType(rootElement));
      project.setBaseDirectory(projectFile.getParentFile());

      List<String> errors = Lists.newArrayList();

      getProjectAttributes(project, rootElement, errors);
      getMainData(project, rootElement, errors);
      getMetadata(project, rootElement, errors);
      getDependencies(project, rootElement, errors);
      project.addResources(getConstituents(
          rootElement.getChild(PROJECT_RESOURCES_ELEMENT_NAME), errors));
      project.addSources(getConstituents(
          rootElement.getChild(PROJECT_SOURCES_ELEMENT_NAME), errors));
      getDeployments(project, rootElement, errors);

      if (!errors.isEmpty()) {
        StringBuilder errorBuilder = new StringBuilder();
        errorBuilder.append("Errors found during build:\n");
        for (String error : errors) {
          errorBuilder.append(error).append("\n");
        }
        throw new SimpleInteractiveSpacesException(errorBuilder.toString());
      }

      return project;
    } catch (Exception e) {
      throw new InteractiveSpacesException("Unable to read project description", e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // Don't care
        }
      }
    }
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
    return getRequiredAttributeValue(rootElement, "type");
  }

  /**
   * Get the main data from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   * @param errors
   *          any errors found in the metadata
   */
  private void getMainData(Project project, Element rootElement, List<String> errors) {
    String name = getChildTextTrimmed(rootElement, "name");
    project.setName(name);

    String description = getChildTextTrimmed(rootElement, "description");
    project.setDescription(description);

    String identifyingName = getChildTextTrimmed(rootElement, "identifyingName");
    project.setIdentifyingName(identifyingName);

    String version = getChildTextTrimmed(rootElement, "version");
    project.setVersion(version);

    String builder = getAttributeValue(rootElement, "builder", null);
    project.setBuilderType(builder);
  }

  /**
   * Get any attributes attached to the project.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   * @param errors
   *          any errors found in the metadata
   */
  private void getProjectAttributes(Project project, Element rootElement, List<String> errors) {
    @SuppressWarnings("unchecked")
    List<Attribute> attributes = rootElement.getAttributes();
    for (Attribute attribute : attributes) {
      project.addAttribute(attribute.getName(), attribute.getValue());
    }
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param key
   *          variable key
   *
   * @return trimmed element text
   *
   * @throws SimpleInteractiveSpacesException
   *           if the child element is not provided
   */
  private String getChildTextTrimmed(Element element, String key)
      throws SimpleInteractiveSpacesException {
    try {
      return element.getChildText(key).trim();
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Looking for text of child: " + key, e);
    }
  }

  /**
   * Return a required element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value
   *
   * @throws SimpleInteractiveSpacesException
   *           if the element attribute is not provided
   */
  private String getRequiredAttributeValue(Element element, String key)
      throws SimpleInteractiveSpacesException {
    String value = getAttributeValue(element, key);
    if (value == null) {
      throw new SimpleInteractiveSpacesException("Missing required attribute " + key);
    }
    return value;
  }

  /**
   * Return a given element attribute, using the default value if not found.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   * @param fallback
   *          default attribute value
   *
   * @return attribute value
   */
  private String getAttributeValue(Element element, String key, String fallback) {
    return element.getAttributeValue(key, fallback);
  }

  /**
   * Return a given element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value, or {@code null} if not found.
   */
  private String getAttributeValue(Element element, String key) {
    return getAttributeValue(element, key, null);
  }

  /**
   * Get the metadata from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   * @param errors
   *          any errors found in the metadata
   */
  private void getMetadata(Project project, Element rootElement, List<String> errors) {
    Element metadataElement = rootElement.getChild("metadata");

    if (metadataElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> itemElements = metadataElement.getChildren("item");

      Map<String, Object> metadata = Maps.newHashMap();
      for (Element itemElement : itemElements) {
        String name = getRequiredAttributeValue(itemElement, "name");
        String value = itemElement.getTextNormalize();
        metadata.put(name, value);
      }

      project.setMetadata(metadata);
    }
  }

  /**
   * Get the resources from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   * @param errors
   *          any errors found in the metadata
   */
  private void getDependencies(Project project, Element rootElement, List<String> errors) {
    Element dependenciesElement = rootElement.getChild("dependencies");

    if (dependenciesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> dependencyElements = dependenciesElement.getChildren("dependency");

      for (Element dependencyElement : dependencyElements) {
        ProjectDependency dependency = getDependency(dependencyElement, errors);
        project.addDependency(dependency);
      }
    }
  }

  /**
   * Get an project dependency from the dependency element.
   *
   * @param dependencyElement
   *          the element containing the data
   * @param errors
   *          any errors found in the metadata
   *
   * @return the dependency found in the element
   */
  private ProjectDependency getDependency(Element dependencyElement, List<String> errors) {
    String name = getAttributeValue(dependencyElement, "name");
    if (name == null) {
      errors.add("Dependency has no name");
    }

    String minimumVersion = getAttributeValue(dependencyElement, "minimumVersion");
    String maximumVersion = getAttributeValue(dependencyElement, "maximumVersion");

    if (minimumVersion != null) {
      if (maximumVersion == null) {
        maximumVersion = minimumVersion;
      }
    } else if (maximumVersion != null) {
      // If here was no minimum version
      minimumVersion = maximumVersion;
    } else {
      errors.add("Dependency has no version constraints");
    }

    String requiredString = getAttributeValue(dependencyElement, "required", "true");

    ProjectDependency dependency = new ProjectDependency();

    dependency.setName(name);
    dependency.setMinimumVersion(minimumVersion);
    dependency.setMaximumVersion(maximumVersion);
    dependency.setRequired("true".equals(requiredString));

    return dependency;
  }

  /**
   * Add the constituents from the document.
   *
   * @param containerElement
   *          root element of the XML DOM containing the project data
   * @param errors
   *          any errors found in the metadata
   */
  private List<ProjectConstituent> getConstituents(Element containerElement, List<String> errors) {
    if (containerElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();
    @SuppressWarnings("unchecked")
    List<Element> childElements = containerElement.getChildren();

    for (Element childElement : childElements) {
      String type = childElement.getName();
      ProjectConstituent.Builder builder = PROJECT_CONSTITUENT_BUILDER_MAP.get(type);
      if (builder == null) {
        errors.add(String.format("Unknown resource type '%s'", type));
      } else {
        ProjectConstituent constituent = builder.buildConstituentFromElement(childElement, errors);
        if (constituent != null) {
          constituents.add(constituent);
        }
      }
    }

    return constituents;
  }

  /**
   * Get the deployments from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   * @param errors
   *          any errors found in the metadata
   */
  private void getDeployments(Project project, Element rootElement, List<String> errors) {
    Element deploymentsElement = rootElement.getChild("deployments");

    if (deploymentsElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> deploymentElements = deploymentsElement.getChildren("deployment");

      for (Element deploymentElement : deploymentElements) {
        ProjectDeployment deployment = getDeployment(deploymentElement, errors);
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
   * @param errors
   *          any errors found in the metadata
   *
   * @return the deployment found in the element
   */
  private ProjectDeployment getDeployment(Element deploymentElement, List<String> errors) {
    String type = getRequiredAttributeValue(deploymentElement, "type");
    String method = getAttributeValue(deploymentElement, "method");
    String location = getAttributeValue(deploymentElement, "location");

    // TODO(keith): Enumerate all possible errors

    return new ProjectDeployment(type, method, location);
  }
}
