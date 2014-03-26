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

package interactivespaces.workbench.confederate;

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreator;
import interactivespaces.workbench.project.ProjectCreatorImpl;

/**
 * A {@link interactivespaces.workbench.project.ProjectCreator} implementation.
 *
 * @author Keith M. Hughes
 */
public class ConfederacyCreator {

  /**
   * Templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * The workbench used by the creator.
   */
  private final InteractiveSpacesWorkbench workbench;

  private final ProjectCreator projectCreator;

  /**
   * Create a basic instance.
   *
   * @param workbench
   *          containing workbench
   *
   */
  public ConfederacyCreator(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
    templater = new FreemarkerTemplater();
    templater.startup();
    templater.setEvaluationPasses(2);
    projectCreator = new ProjectCreatorImpl(workbench, templater);
  }

  public void create(Confederacy spec) {
    try {
      for (Project project : spec.getProjectList()) {
        projectCreator.instantiate(makeCreationSpecification(spec, project));
      }
    } catch (Exception e) {
      workbench.handleError("Error while creating confederacy", e);
    }
  }

  private ProjectCreationSpecification makeCreationSpecification(Confederacy confederacy, Project project) {
    ProjectCreationSpecification creationSpecification = new ProjectCreationSpecification();
    creationSpecification.setProject(project);
    creationSpecification.setBaseDirectory(confederacy.getBaseDirectory());
    creationSpecification.setSpecificationBase(confederacy.getSpecificationSource().getParentFile());
    creationSpecification.addTemplateDataEntry("baseDirectory", confederacy.getBaseDirectory());
    return creationSpecification;
  }

}
