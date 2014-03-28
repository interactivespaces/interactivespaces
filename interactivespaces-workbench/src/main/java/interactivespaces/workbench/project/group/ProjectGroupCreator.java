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

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreator;
import interactivespaces.workbench.project.ProjectCreatorImpl;

/**
 * A project group creator implementation.
 *
 * @author Trevor Pering
 */
public class ProjectGroupCreator {

  /**
   * Templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * The workbench used by the creator.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * The project creator used for projects in the group.
   */
  private final ProjectCreator projectCreator;

  /**
   * Create a basic instance.
   *
   * @param workbench
   *          containing workbench
   *
   */
  public ProjectGroupCreator(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
    templater = new FreemarkerTemplater();
    templater.setEvaluationPasses(2);
    templater.initialize();
    projectCreator = new ProjectCreatorImpl(workbench, templater);
  }

  /**
   * Create a new group of projects.
   *
   * @param spec
   *          spec for which to create
   */
  public void create(ProjectGroup spec) {
    int projectIndex = 0;
    try {
      for (Project project : spec.getProjectList()) {
        projectIndex++;
        projectCreator.instantiate(makeCreationSpecification(spec, project));
      }
    } catch (Exception e) {
      workbench.handleError(String.format(
          "Error while creating projectGroup, project #%d/%d", projectIndex, spec.getProjectList().size()), e);
    }
  }

  /**
   * Make a creation specification for the given project in the group.
   *
   * @param projectGroup
   *          specification group
   * @param project
   *          individual project
   *
   * @return project creation specification
   */
  private ProjectCreationSpecification makeCreationSpecification(ProjectGroup projectGroup, Project project) {
    ProjectCreationSpecification creationSpecification = new ProjectCreationSpecification();
    creationSpecification.setProject(project);
    creationSpecification.setBaseDirectory(projectGroup.getBaseDirectory());
    creationSpecification.setSpecificationBase(projectGroup.getSpecificationSource().getParentFile());
    creationSpecification.addTemplateDataEntry("baseDirectory", projectGroup.getBaseDirectory());
    return creationSpecification;
  }

}
