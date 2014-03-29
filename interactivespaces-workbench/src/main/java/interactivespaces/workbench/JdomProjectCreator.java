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

package interactivespaces.workbench;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.JdomProjectReader;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.group.JdomProjectGroupReader;
import interactivespaces.workbench.project.group.GroupProject;
import org.jdom.Element;

import java.io.File;
import java.util.List;

/**
 * Create projects given input specifications in XML.
 *
 * @author Trevor Pering
 */
public class JdomProjectCreator {

  /**
   * Containing workbench.
   */
  private final InteractiveSpacesWorkbench interactiveSpacesWorkbench;

  /**
   * XML reader used for parsing input specification.
   */
  private final JdomReader jdomReader;

  /**
   * Create a new instance.
   *
   * @param interactiveSpacesWorkbench
   *          containing workbench instance
   */
  public JdomProjectCreator(InteractiveSpacesWorkbench interactiveSpacesWorkbench) {
    this.interactiveSpacesWorkbench = interactiveSpacesWorkbench;
    jdomReader = new JdomReader(interactiveSpacesWorkbench);
  }

  /**
   * Create projects from a specification, could be a project or a group of projects.
   *
   * @param commands
   *          specific creation commands
   */
  void createProjectsFromSpecification(List<String> commands, File specFile, File baseDirectory) {
    try {
      Element rootElement = jdomReader.getRootElement(specFile);
      String type = rootElement.getName();
      if (JdomProjectGroupReader.PROJECT_GROUP_ELEMENT_NAME.equals(type)) {
        createProjectGroupFromElement(rootElement, specFile, baseDirectory);
      } else if (JdomProjectReader.ELEMENT_NAME.equals(type)) {
        createProjectFromElement(rootElement, baseDirectory);
      } else {
        throw new SimpleInteractiveSpacesException("Unknown root element type " + type);
      }
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
   * @param specFile
   *          specification file
   * @param baseDirectory
   */
  private void createProjectGroupFromElement(Element rootElement, File specFile, File baseDirectory) {

    GroupProject groupProject =
        interactiveSpacesWorkbench.getProjectTypeRegistry().newProject(GroupProject.PROJECT_TYPE_NAME);
    groupProject.setSpecificationSource(specFile);
    groupProject.setBaseDirectory(baseDirectory);

    JdomProjectGroupReader projectGroupReader = new JdomProjectGroupReader(interactiveSpacesWorkbench);
    projectGroupReader.processSpecification(groupProject, rootElement);

    ProjectCreationSpecification spec = new ProjectCreationSpecification();
    spec.setProject(groupProject);

    interactiveSpacesWorkbench.getProjectCreator().create(spec);
  }

  /**
   * Create an output project from a project specification element.
   *
   * @param rootElement
   *          input root element
   * @param baseDirectory
   */
  private void createProjectFromElement(Element rootElement, File baseDirectory) {
    JdomProjectReader projectReader = new JdomProjectReader(interactiveSpacesWorkbench);

    Project project = projectReader.makeProjectFromElement(rootElement);
    project.setType(ActivityProject.PROJECT_TYPE_NAME);
    project.setBaseDirectory(baseDirectory);

    ProjectCreationSpecification spec = new ProjectCreationSpecification();
    spec.setProject(project);

    interactiveSpacesWorkbench.getProjectCreator().create(spec);
  }
}
