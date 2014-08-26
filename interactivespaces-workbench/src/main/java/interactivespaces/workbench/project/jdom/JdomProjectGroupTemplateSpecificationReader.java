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

package interactivespaces.workbench.project.jdom;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.resource.Version;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectGroupTemplateSpecificationReader;
import interactivespaces.workbench.project.creator.TemplateAssignConstituent;
import interactivespaces.workbench.project.creator.TemplateFileConstituent;
import interactivespaces.workbench.project.group.GroupProjectTemplateSpecification;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.util.List;

/**
 * Reader for a XML-based project group using jdom.
 *
 * @author Trevor Pering
 */
public class JdomProjectGroupTemplateSpecificationReader extends JdomReader implements
    ProjectGroupTemplateSpecificationReader {

  /**
   * Element name for a project group.
   */
  public static final String PROJECT_GROUP_TEMPLATE_SPECIFICATION_ELEMENT_NAME = "projectGroupTemplateSpecification";

  /**
   * Element name for a project group.
   */
  public static final String PROJECT_GROUP_ELEMENT_NAME = "projectGroup";

  /**
   * Add all the base constituent types to the static map.
   */
  {
    addConstituentType(new TemplateFileConstituent.TemplateFileConstituentFactory());
    addConstituentType(new TemplateAssignConstituent.TemplateAssignConstituentFactory());
  }

  /**
   * Construct a project reader.
   *
   * @param workbench
   *          the workbench to use
   */
  public JdomProjectGroupTemplateSpecificationReader(InteractiveSpacesWorkbench workbench) {
    super(workbench);
    setJdomPrototypeProcessor(new JdomPrototypeProcessor());
  }

  @Override
  public GroupProjectTemplateSpecification readProjectGroupTemplateSpecification(File specFile) {
    try {
      Element rootElement = getRootElement(specFile);
      String type = rootElement.getName();
      GroupProjectTemplateSpecification specification;
      if (PROJECT_GROUP_TEMPLATE_SPECIFICATION_ELEMENT_NAME.equals(type)) {
        specification = makeFromElement(rootElement);
      } else if (PROJECT_GROUP_ELEMENT_NAME.equals(type)) {
        specification = makeFromElement(rootElement);
      } else {
        throw new SimpleInteractiveSpacesException("Unknown root element type " + type);
      }
      specification.setSpecificationSource(specFile);
      return specification;
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While processing specification file " + specFile.getAbsolutePath(), e);
    }
  }

  /**
   * Create a project group from a given element.
   *
   * @param rootElement
   *          input element
   *
   * @return new specification
   */
  private GroupProjectTemplateSpecification makeFromElement(Element rootElement) {
    Namespace namespace = rootElement.getNamespace();

    GroupProjectTemplateSpecification groupProjectTemplateSpecification = new GroupProjectTemplateSpecification();

    List<Element> children = getChildren(rootElement);
    for (Element child : children) {
      addElementToSpec(groupProjectTemplateSpecification, namespace, child);
    }

    return groupProjectTemplateSpecification;
  }

  /**
   * Add the given element to the spec.
   *
   * @param spec
   *          specification to configure
   * @param namespace
   *          XML namespace for elements
   * @param child
   *          child element to add
   */
  private void addElementToSpec(GroupProjectTemplateSpecification spec, Namespace namespace, Element child) {
    String name = child.getName();

    try {
      if (JdomProjectReader.GROUP_ELEMENT_NAME.equals(name)) {
        addProjects(spec, child);
      } else if (JdomPrototypeProcessor.GROUP_ELEMENT_NAME.equals(name)) {
        addPrototypes(spec, child);
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_NAME.equals(name)) {
        spec.setName(child.getTextTrim());
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_DESCRIPTION.equals(name)) {
        spec.setDescription(child.getTextTrim());
      } else if (JdomReader.PROJECT_ELEMENT_NAME_TEMPLATES.equals(name)) {
        // This is really a prototype chain for the entire group project, but we need to control when it is processed,
        // so instead snarf the attribute from the templates element when it is processed.
        processPrototypeChain(spec, namespace, child);
        spec.addExtraConstituents(getContainerConstituents(namespace, child, null));
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
   * Add all prototypes to the specification.
   *
   * @param spec
   *          specification to configure
   * @param group
   *          prototype group to add
   */
  private void addPrototypes(GroupProjectTemplateSpecification spec, Element group) {
    List<Element> children = getChildren(group);
    for (Element entry : children) {
      getJdomPrototypeProcessor().addPrototypeElement(entry);
    }
  }

  /**
   * Process the prototype chain for the given element.
   *
   * @param spec
   *          specification to configure
   * @param namespace
   *          XML namespace for elements
   * @param projectElement
   *          element to populate from
   */
  private void
      processPrototypeChain(GroupProjectTemplateSpecification spec, Namespace namespace, Element projectElement) {
    if (getJdomPrototypeProcessor() != null) {
      List<Element> prototypeChain = getJdomPrototypeProcessor().getPrototypeChain(projectElement);
      for (Element prototype : prototypeChain) {
        configureProjectGroupFromElement(spec, namespace, prototype);
      }
      // Remove the not-useful prototype's name, since it would incorrectly be naming this element.
      spec.getAttributes().remove(JdomPrototypeProcessor.PROTOTYPE_NAME_ATTRIBUTE);
    }
  }

  /**
   * Configure a project given an element.
   *
   * @param spec
   *          specification to configure
   * @param namespace
   *          XML namespace for elements
   * @param projectElement
   *          project element to read from
   */
  private void configureProjectGroupFromElement(GroupProjectTemplateSpecification spec, Namespace namespace,
      Element projectElement) {
    getSpecificationAttributes(spec, projectElement);
    getMainData(spec, namespace, projectElement);

    spec.addExtraConstituents(getContainerConstituents(namespace,
        projectElement.getChild(JdomReader.PROJECT_ELEMENT_NAME_TEMPLATES), null));
  }

  /**
   * Get any attributes attached to the project.
   *
   * @param spec
   *          the specification whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getSpecificationAttributes(GroupProjectTemplateSpecification spec, Element rootElement) {
    @SuppressWarnings("unchecked")
    List<Attribute> attributes = rootElement.getAttributes();
    for (Attribute attribute : attributes) {
      spec.addAttribute(attribute.getName(), attribute.getValue());
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
  private void addProjects(GroupProjectTemplateSpecification spec, Element group) {
    List<Element> children = getChildren(group);
    for (Element entry : children) {
      JdomProjectReader projectReader = new JdomProjectReader(getWorkbench());
      projectReader.addConstituentType(new TemplateFileConstituent.TemplateFileConstituentFactory());
      projectReader.addConstituentType(new TemplateAssignConstituent.TemplateAssignConstituentFactory());
      projectReader.setJdomPrototypeProcessor(getJdomPrototypeProcessor());
      Project project = projectReader.makeProjectFromElement(entry);
      project.setSpecificationSource(spec.getSpecificationSource());
      project.setBaseDirectory(new File("."));
      spec.addProject(project);
    }
  }

  /**
   * Get the main data from the document.
   *
   * @param spec
   *          the specification whose data is being read
   * @param namespace
   *          XML namespace for elements
   * @param rootElement
   *          root element to read from
   */
  private void getMainData(GroupProjectTemplateSpecification spec, Namespace namespace, Element rootElement) {
    spec.setName(getChildTextTrimmed(rootElement, namespace, JdomProjectReader.PROJECT_ELEMENT_NAME_NAME,
        spec.getName()));
    spec.setDescription(getChildTextTrimmed(rootElement, namespace, JdomProjectReader.PROJECT_ELEMENT_NAME_DESCRIPTION,
        spec.getDescription()));

    String version = getChildTextTrimmed(rootElement, namespace, JdomProjectReader.PROJECT_ELEMENT_NAME_VERSION);
    if (version != null) {
      spec.setVersion(Version.parseVersion(version));
    }
  }
}
