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

package interactivespaces.workbench.project.jdom;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.constituent.ProjectConstituent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import org.apache.commons.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Base-class for facilitating reading XML documents using jdom.
 *
 * @author Trevor pering
 */
public class JdomReader {

  /**
   * Project definition file element name for templates.
   */
  public static final String PROJECT_ELEMENT_NAME_TEMPLATES = "templateComponents";

  /**
   * Map of resource types to resource builders.
   */
  private final Map<String, ProjectConstituent.ProjectConstituentFactory> projectConstituentFactoryMap = Maps
      .newHashMap();

  /**
   * {@code true} if read was successful.
   */
  protected boolean failure;

  /**
   * Interactive spaces workbench used by this reader.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Prototype manager to use when reading/constructing projects, may be
   * {@code null} if none available.
   */
  private JdomPrototypeProcessor jdomPrototypeProcessor;

  /**
   * Create a new jdom reader.
   *
   * @param workbench
   *          containing workbench instance
   */
  public JdomReader(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  /**
   * Add a constituent type to the factory.
   *
   * @param constituentFactory
   *          factory to add
   */
  protected void addConstituentType(ProjectConstituent.ProjectConstituentFactory constituentFactory) {
    projectConstituentFactoryMap.put(constituentFactory.getName(), constituentFactory);
  }

  /**
   * Get the root element for a given input file.
   *
   * @param inputFile
   *          input project file
   *
   * @return top-level element
   */
  Element getRootElement(File inputFile) {
    Document doc;
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(inputFile);
      SAXBuilder builder = new SAXBuilder();
      doc = builder.build(inputStream);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Exception while processing %s", inputFile.getAbsolutePath()),
          e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
    return doc.getRootElement();
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param namespace
   *          namespace for the key element
   * @param key
   *          variable key
   *
   * @return trimmed element text
   *
   * @throws InteractiveSpacesException
   *           if the child element is not provided
   */
  protected String getChildTextTrimmed(Element element, Namespace namespace, String key)
      throws InteractiveSpacesException {
    try {
      return element.getChildTextTrim(key, namespace);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Looking for text of child: " + key, e);
    }
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param namespace
   *          namespace for the key element
   * @param key
   *          variable key
   * @param fallback
   *          fallback when not defined
   *
   * @return trimmed element text
   */
  protected String getChildTextTrimmed(Element element, Namespace namespace, String key, String fallback) {
    String value = getChildTextTrimmed(element, namespace, key);
    return (value == null || (!value.isEmpty() && fallback != null)) ? fallback : value;
  }

  /**
   * Return a required element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value or {@code null} if none found
   */
  protected String getRequiredAttributeValue(Element element, String key) {
    String value = getAttributeValue(element, key);
    if (value == null) {
      getLog().error("Missing required attribute " + key);
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
  protected String getAttributeValue(Element element, String key, String fallback) {
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
  protected String getAttributeValue(Element element, String key) {
    return getAttributeValue(element, key, null);
  }

  /**
   * Get the children of the given element.
   *
   * @param element
   *          element to return the children of
   *
   * @return element children
   */
  @SuppressWarnings("unchecked")
  protected List<Element> getChildren(Element element) {
    return element.getChildren();
  }

  /**
   * @return workbench used by this reader
   */
  public InteractiveSpacesWorkbench getWorkbench() {
    return workbench;
  }

  /**
   * An error has occurred.
   *
   * @param error
   *          text of the error message
   */
  protected void addError(String error) {
    getLog().error(error);
    failure = true;
  }

  /**
   * Logger to use while reading.
   *
   * @return logger
   */
  public Log getLog() {
    return workbench.getLog();
  }

  /**
   * @return the prototype processor
   */
  public JdomPrototypeProcessor getJdomPrototypeProcessor() {
    return jdomPrototypeProcessor;
  }

  /**
   * Set the prototype processor.
   *
   * @param jdomPrototypeProcessor
   *          prototype processor
   */
  public void setJdomPrototypeProcessor(JdomPrototypeProcessor jdomPrototypeProcessor) {
    this.jdomPrototypeProcessor = jdomPrototypeProcessor;
  }

  /**
   * @return constituent factory map
   */
  public Map<String, ProjectConstituent.ProjectConstituentFactory> getProjectConstituentFactoryMap() {
    return projectConstituentFactoryMap;
  }

  /**
   * Add the constituents from a container in the document the document.
   *
   * @param namespace
   *          XML namespace for elements
   * @param containerElement
   *          root element of the XML DOM containing the project data
   * @param project
   *          the project being read
   *
   * @return the constituents for the project
   */
  protected List<ProjectConstituent> getContainerConstituents(Namespace namespace, Element containerElement,
      Project project) {
    if (containerElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();
    List<Element> childElements = getChildren(containerElement);

    for (Element childElement : childElements) {
      getConstituent(namespace, childElement, project, constituents);
    }

    return constituents;
  }

  /**
   * Add the constituents from a container in the document the document.
   *
   * @param namespace
   *          XML namespace for elements
   * @param constituentElement
   *          XML element containing the constituent data
   * @param project
   *          the project being read
   *
   * @return the constituents for the element
   */
  protected List<ProjectConstituent> getIndividualConstituent(Namespace namespace, Element constituentElement,
      Project project) {
    if (constituentElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();

    getConstituent(namespace, constituentElement, project, constituents);

    return constituents;
  }

  /**
   * Get the constituent from the element which describes it..
   *
   * @param namespace
   *          XML namespace for elements
   * @param constituentElement
   *          the element containing the constituent
   * @param project
   *          the project being built
   * @param constituents
   *          the list of constituents currently being extracted
   */
  private void getConstituent(Namespace namespace, Element constituentElement, Project project,
      List<ProjectConstituent> constituents) {
    String type = constituentElement.getName();
    ProjectConstituent.ProjectConstituentFactory factory = getProjectConstituentFactoryMap().get(type);
    if (factory == null) {
      addError(String.format("Unknown resource type '%s'", type));
    } else {
      ProjectConstituent.ProjectConstituentBuilder projectConstituentBuilder = factory.newBuilder();
      projectConstituentBuilder.setLog(getLog());
      ProjectConstituent constituent =
          projectConstituentBuilder.buildConstituentFromElement(namespace, constituentElement, project);
      if (constituent != null) {
        constituents.add(constituent);
      }
    }
  }
}
