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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.JdomReader;
import interactivespaces.workbench.project.JdomProjectReader;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.TemplateVar;
import org.jdom.Element;

import java.io.File;
import java.util.List;

/**
 * Reader for a XML-based project group using jdom.
 *
 * @author Trevor Pering
 */
public class JdomProjectGroupReader extends JdomReader {

  /**
   * Element name for a project group.
   */
  public static final String PROJECT_GROUP_ELEMENT_NAME = "projectGroup";

  /**
   * Prototype manager to use for this project group specification.
   */
  private final JdomPrototypeManager jdomPrototypeManager = new JdomPrototypeManager();

  /**
   * Construct a project reader.
   *
   * @param workbench
   *          the workbench to use
   */
  public JdomProjectGroupReader(InteractiveSpacesWorkbench workbench) {
    super(workbench);
  }

  /**
   * Process the given specification.
   *
   * @param projectGroup
   *          target for processed input
   * @param rootElement
   *          input element
   */
  public void processSpecification(ProjectGroup projectGroup, Element rootElement) {
    if (!PROJECT_GROUP_ELEMENT_NAME.equals(rootElement.getName())) {
      throw new SimpleInteractiveSpacesException("Illegal root element name " + rootElement.getName());
    }

    List<Element> children = getChildren(rootElement);
    for (Element child : children) {
      addElementToSpec(projectGroup, child);
    }
  }

  /**
   * Add the given element to the spec.
   *
   * @param spec
   *          spec to add to
   * @param child
   *          child element to add
   */
  private void addElementToSpec(ProjectGroup spec, Element child) {
    String name = child.getName();

    try {
      if (JdomProjectReader.GROUP_ELEMENT_NAME.equals(name)) {
        addProjects(spec, child);
      } else if (JdomPrototypeManager.GROUP_ELEMENT_NAME.equals(name)) {
        addPrototypes(spec, child);
      } else {
        throw new SimpleInteractiveSpacesException("Unrecognized element name: " + name);
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While processing projectGroup element: " + name, e);
    }
  }

  /**
   * Add all prototypes to the spec.
   *
   * @param spec
   *          spec to add
   * @param group
   *          prototype group to add
   */
  private void addPrototypes(ProjectGroup spec, Element group) {
    List<Element> children = getChildren(group);
    for (Element entry : children) {
      jdomPrototypeManager.addPrototypeElement(entry);
    }
  }

  /**
   * Add all projects to the spec.
   *
   * @param spec
   *          spec to add
   * @param group
   *          projects group to add
   */
  private void addProjects(ProjectGroup spec, Element group) {
    List<Element> children = getChildren(group);
    for (Element entry : children) {
      JdomProjectReader projectReader = new JdomProjectReader(getWorkbench());
      projectReader.setJdomPrototypeManager(jdomPrototypeManager);
      Project project = projectReader.makeProjectFromElement(entry);
      project.setSpecificationSource(spec.getSpecificationSource());
      project.setBaseDirectory(new File("."));
      spec.addProject(project);
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
  protected TemplateVar getTemplateVarFromElement(Element element) {
    String name = getRequiredAttributeValue(element, TemplateVar.NAME_ATTRIBUTE_NAME);
    String value = getRequiredAttributeValue(element, TemplateVar.VALUE_ATTRIBUTE_NAME);
    return new TemplateVar(name, value);
  }
}
