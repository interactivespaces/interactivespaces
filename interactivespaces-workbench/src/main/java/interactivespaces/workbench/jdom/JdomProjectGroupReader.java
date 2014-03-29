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

package interactivespaces.workbench.jdom;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.resource.Version;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.group.GroupProject;
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
   * @param groupProject
   *          target for processed input
   * @param rootElement
   *          input element
   */
  void processSpecification(GroupProject groupProject, Element rootElement) {
    if (!PROJECT_GROUP_ELEMENT_NAME.equals(rootElement.getName())) {
      throw new SimpleInteractiveSpacesException("Illegal root element name " + rootElement.getName());
    }

    List<Element> children = getChildren(rootElement);
    for (Element child : children) {
      addElementToSpec(groupProject, child);
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
  private void addElementToSpec(GroupProject spec, Element child) {
    String name = child.getName();

    try {
      if (JdomProjectReader.GROUP_ELEMENT_NAME.equals(name)) {
        addProjects(spec, child);
      } else if (JdomPrototypeManager.GROUP_ELEMENT_NAME.equals(name)) {
        addPrototypes(spec, child);
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_NAME.equals(name)) {
        spec.setName(child.getTextTrim());
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_DESCRIPTION.equals(name)) {
        spec.setDescription(child.getTextTrim());
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_VERSION.equals(name)) {
        spec.setVersion(Version.parseVersion(child.getTextTrim()));
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
  private void addPrototypes(GroupProject spec, Element group) {
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
  private void addProjects(GroupProject spec, Element group) {
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

}
