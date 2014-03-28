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

package interactivespaces.workbench.project;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.type.ProjectType;

/**
 * A {@link ProjectCreator} implementation.
 *
 * @author Keith M. Hughes
 */
public class ProjectCreatorImpl implements ProjectCreator {

  /**
   * Templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * The workbench used by the creator.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Create a basic instance.
   *
   * @param workbench
   *          containing workbench
   * @param templater
   *          templater to use
   */
  public ProjectCreatorImpl(InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater) {
    this.workbench = workbench;
    this.templater = templater;
  }

  @Override
  public void instantiate(ProjectCreationSpecification spec) {
    try {
      Project project = spec.getProject();
      ProjectType projectType = workbench.getProjectTypeRegistry().getProjectType(project);
      if (projectType == null) {
        throw new SimpleInteractiveSpacesException(String.format("Invalid type for project type/builder %s/%s",
            project.getType(), project.getBuilderType()));
      }
      ProjectTemplate projectTemplate = projectType.newProjectTemplate();
      projectTemplate.setTemplater(templater);
      projectTemplate.process(spec);

    } catch (Exception e) {
      workbench.handleError("Error while creating project", e);
    }
  }
}
