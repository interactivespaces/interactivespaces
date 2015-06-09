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

package interactivespaces.workbench.project.group;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.creator.ProjectCreationContext;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public class GroupProjectTemplate extends BaseProjectTemplate {

  @Override
  protected void onTemplateSetup(ProjectCreationContext context) {
    context.addTemplateDataEntry("group", context.getGroupProjectTemplateSpecification());
  }

  @Override
  public void onTemplateWrite(ProjectCreationContext context) {
    GroupProjectTemplateSpecification groupProjectTemplateSpecification =
        context.getGroupProjectTemplateSpecification();
    int projectIndex = 0;
    try {
      for (Project project : groupProjectTemplateSpecification.getProjectList()) {
        projectIndex++;
        context.getWorkbenchTaskContext().getWorkbench()
            .getProjectCreator().create(makeCreationSpecification(project, context));
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Error while creating projectGroup, project #%d/%d", projectIndex,
          groupProjectTemplateSpecification.getProjectList().size()), e);
    }
  }

  /**
   * Make a creation specification for the given project in the group.
   *
   * @param project
   *          individual project to make a specification for
   * @param groupCreationSpec
   *          containing creation spec for the overall group
   *
   * @return project creation specification
   */
  private ProjectCreationContext makeCreationSpecification(
      Project project, ProjectCreationContext groupCreationSpec) {
    ProjectCreationContext creationSpecification =
        new ProjectCreationContext("sub-project", groupCreationSpec.getLog());
    creationSpecification.setProject(project);
    creationSpecification.setWorkbenchTaskContext(groupCreationSpec.getWorkbenchTaskContext());
    creationSpecification.setBaseDirectory(groupCreationSpec.getBaseDirectory());
    creationSpecification.setSpecificationBase(groupCreationSpec.getSpecificationBase());
    creationSpecification.addTemplateDataEntry("baseDirectory", groupCreationSpec.getBaseDirectory());
    creationSpecification.addTemplateDataEntry("group", groupCreationSpec.getGroupProjectTemplateSpecification());
    creationSpecification.addTemplateDataEntry("creationSpec", groupCreationSpec.getTemplateData());
    return creationSpecification;
  }
}

