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
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.ActivityProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectAssemblyConstituent;
import interactivespaces.workbench.project.constituent.ProjectBundleConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;
import interactivespaces.workbench.project.creator.TemplateAssignConstituent;
import interactivespaces.workbench.project.creator.TemplateFileConstituent;
import interactivespaces.workbench.project.group.GroupProject;
import org.jdom.Attribute;
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
  public JdomProjectGroupReader(InteractiveSpacesWorkbench workbench) {
    super(workbench);
    setJdomPrototypeProcessor(new JdomPrototypeProcessor());
  }

  //@Override
  public GroupProject readProject(File specFile) {
    try {
      Element rootElement = getRootElement(specFile);
      String type = rootElement.getName();
      GroupProject project;
      if (PROJECT_GROUP_TEMPLATE_SPECIFICATION_ELEMENT_NAME.equals(type)) {
        project =  makeGroupProjectFromElement(rootElement);
      } else if (PROJECT_GROUP_ELEMENT_NAME.equals(type)) {
        project =  makeGroupProjectFromElement(rootElement);
      } else {
        throw new SimpleInteractiveSpacesException("Unknown root element type " + type);
      }
      project.setSpecificationSource(specFile);
      return project;
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          "While processing specification file " + specFile.getAbsolutePath(), e);
    }
  }

  /**
   * Create a project group from a given element.
   *
   * @param rootElement
   *          input element
   *
   * @return new project
   */
  private GroupProject makeGroupProjectFromElement(Element rootElement) {
    GroupProject groupProject = new GroupProject();
    //groupProject.setType(GroupProject.PROJECT_TYPE_NAME);

    List<Element> children = getChildren(rootElement);
    for (Element child : children) {
      addElementToSpec(groupProject, child);
    }

    return groupProject;
  }

  /**
   * Create an output project from a project specification element.
   *
   * @param rootElement
   *          input root element
   *
   * @return new project
   */
  private Project makeSimpleProjectFromElement(Element rootElement) {
    JdomProjectReader projectReader = new JdomProjectReader(getWorkbench());

    Project project = projectReader.makeProjectFromElement(rootElement);
    project.setType(ActivityProject.PROJECT_TYPE_NAME);

    return project;
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
      } else if (JdomPrototypeProcessor.GROUP_ELEMENT_NAME.equals(name)) {
        addPrototypes(spec, child);
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_NAME.equals(name)) {
        spec.setName(child.getTextTrim());
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_DESCRIPTION.equals(name)) {
        spec.setDescription(child.getTextTrim());
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME_TEMPLATES.equals(name)) {
        // This is really a prototype chain for the entire group project, but we need to control when it is
        // processed, so instead snarf the attribute from the templates element when it is processed.
        processPrototypeChain(spec, child);
        spec.addExtraConstituents(getContainerConstituents(child, null));
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
      getJdomPrototypeProcessor().addPrototypeElement(entry);
    }
  }

  /**
   * Process the prototype chain for the given element.
   *
   * @param project
   *          project where the results go
   * @param projectElement
   *          element to populate from
   */
  private void processPrototypeChain(GroupProject project, Element projectElement) {
    if (getJdomPrototypeProcessor() != null) {
      List<Element> prototypeChain = getJdomPrototypeProcessor().getPrototypeChain(projectElement);
      for (Element prototype : prototypeChain) {
        configureProjectGroupFromElement(project, prototype);
      }
      // Remove the not-useful prototype's name, since it would incorrectly be naming this element.
      project.getAttributes().remove(JdomPrototypeProcessor.PROTOTYPE_NAME_ATTRIBUTE);
    }
  }

  /**
   * Configure a project given an element.
   *
   * @param project
   *          project to configure
   * @param projectElement
   */
  private void configureProjectGroupFromElement(GroupProject project, Element projectElement) {
    getProjectAttributes(project, projectElement);
    getMainData(project, projectElement);

    project.addExtraConstituents(getContainerConstituents(
        projectElement.getChild(JdomProjectReader.PROJECT_ELEMENT_NAME_TEMPLATES), null));
  }

  /**
   * Get any attributes attached to the project.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getProjectAttributes(GroupProject project, Element rootElement) {
    @SuppressWarnings("unchecked")
    List<Attribute> attributes = rootElement.getAttributes();
    for (Attribute attribute : attributes) {
      project.addAttribute(attribute.getName(), attribute.getValue());
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
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   */
  private void getMainData(GroupProject project, Element rootElement) {
    project.setName(
        getChildTextTrimmed(rootElement, JdomProjectReader.PROJECT_ELEMENT_NAME_NAME, project.getName()));
    project.setDescription(
        getChildTextTrimmed(rootElement, JdomProjectReader.PROJECT_ELEMENT_NAME_DESCRIPTION, project.getDescription()));

    String version = getChildTextTrimmed(rootElement, JdomProjectReader.PROJECT_ELEMENT_NAME_VERSION);
    if (version != null) {
      project.setVersion(Version.parseVersion(version));
    }

  }
}
