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

import interactivespaces.InteractiveSpacesException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A {@link ProjectReader} based on JDOM.
 *
 * @author Keith M. Hughes
 */
public class JdomProjectReader implements ProjectReader {

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

      getMainData(project, rootElement, errors);
      getMetadata(project, rootElement, errors);
      getDependencies(project, rootElement, errors);
      getResources(project, rootElement, errors);
      getDeployments(project, rootElement, errors);

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
    return rootElement.getAttributeValue("type");
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
    String name = rootElement.getChildText("name");
    project.setName(new String(name.trim()));

    String description = rootElement.getChildText("description");
    project.setDescription(new String(description.trim()));

    String identifyingName = rootElement.getChildText("identifyingName");
    project.setIdentifyingName(new String(identifyingName.trim()));

    String version = rootElement.getChildText("version");
    project.setVersion(new String(version.trim()));

    String builder = rootElement.getAttributeValue("builder");
    project.setBuilderType(builder);
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
        String name = itemElement.getAttributeValue("name");
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
    String name = dependencyElement.getAttributeValue("name");
    if (name == null) {
      errors.add("Dependency has no name");
    }

    String minimumVersion = dependencyElement.getAttributeValue("minimumVersion");
    String maximumVersion = dependencyElement.getAttributeValue("maximumVersion");

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

    String requiredString = dependencyElement.getAttributeValue("required", "true");

    ProjectDependency dependency = new ProjectDependency();

    dependency.setName(name);
    dependency.setMinimumVersion(minimumVersion);
    dependency.setMaximumVersion(maximumVersion);
    dependency.setRequired("true".equals(requiredString));

    return dependency;
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
  private void getResources(Project project, Element rootElement, List<String> errors) {
    Element resourcesElement = rootElement.getChild("resources");

    if (resourcesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> resourceElements = resourcesElement.getChildren("resource");

      for (Element resourceElement : resourceElements) {
        ProjectResource resource = getResource(resourceElement, errors);
        if (resource != null) {
          project.addResource(resource);
        }
      }
    }
  }

  /**
   * Get an project dependency from the dependency element.
   *
   * @param resourceElement
   *          the element containing the data
   * @param errors
   *          any errors found in the metadata
   *
   * @return the dependency found in the element
   */
  private ProjectResource getResource(Element resourceElement, List<String> errors) {
    boolean addedErrors = false;

    String sourceDir = resourceElement.getAttributeValue("sourceDirectory");
    String sourceFile = resourceElement.getAttributeValue("sourceFile");
    String destDir = resourceElement.getAttributeValue("destinationDirectory");
    String destFile = resourceElement.getAttributeValue("destinationFile");

    if (destFile == null && destDir == null) {
      destDir = ".";
    }

    if (sourceFile == null && sourceDir == null) {
      addedErrors = true;
      errors.add("Resource has no source");
    }
    if (sourceDir != null) {
      if (sourceFile != null) {
        addedErrors = true;
        errors.add("Resource has both a source file and directory");
      }
      if (destFile != null) {
        addedErrors = true;
        errors.add("Resource has a source directory and a destination file");
      }
    }
    // TODO(keith): Enumerate all possible errors

    if (addedErrors) {
      return null;
    } else {

      ProjectResource resource = new ProjectResource();

      resource.setDestinationDirectory(destDir);
      resource.setSourceDirectory(sourceDir);
      resource.setDestinationFile(destFile);
      resource.setSourceFile(sourceFile);

      return resource;
    }
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
    String type = deploymentElement.getAttributeValue("type");
    String method = deploymentElement.getAttributeValue("method");
    String location = deploymentElement.getAttributeValue("location");

    // TODO(keith): Enumerate all possible errors

    return new ProjectDeployment(type, method, location);
  }
}
