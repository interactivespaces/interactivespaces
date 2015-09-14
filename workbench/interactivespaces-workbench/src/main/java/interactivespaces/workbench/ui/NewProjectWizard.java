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

package interactivespaces.workbench.ui;

import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.creator.ProjectCreationContext;
import interactivespaces.workbench.ui.wizard.WizardCollection;
import interactivespaces.workbench.ui.wizard.component.ChooseDirectoryWizard;
import interactivespaces.workbench.ui.wizard.component.ChooseFileWizard;

import java.io.File;

/**
 * A {@link Wizard} for creating a new project.
 *
 * @author Keith M. Hughes
 */
public class NewProjectWizard extends WizardCollection {

  /**
   * The directory choosing wizard.
   */
  private final ChooseDirectoryWizard chooseDirectoryWizard;

  /**
   * The activity description wizard.
   */
  private final ProjectDescriptionWizard activityDescriptionWizard;

  /**
   * Wizard for choosing the project template.
   */
  private final ChooseFileWizard activityProjectTemplateChooserWizard;

  /**
   * The workbench UI.
   */
  private final WorkbenchUi workbenchUi;

  /**
   * The workbench.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Construct a new project wizard.
   *
   * @param workbenchUi
   *        the workbench UI component
   * @param workbench
   *        the workbench
   */
  public NewProjectWizard(WorkbenchUi workbenchUi, InteractiveSpacesWorkbench workbench) {
    this.workbenchUi = workbenchUi;
    this.workbench = workbench;

    chooseDirectoryWizard = new ChooseDirectoryWizard();
    activityProjectTemplateChooserWizard  = new ChooseFileWizard();
    activityDescriptionWizard = new ProjectDescriptionWizard(null);

    addWizards(chooseDirectoryWizard, activityProjectTemplateChooserWizard, activityDescriptionWizard);
  }

  @Override
  public void completeWizard() {
    super.completeWizard();

    Project project = activityDescriptionWizard.getProject();

    ProjectCreationContext context = new ProjectCreationContext("from wizard", workbench.getLog());
    context.setProject(project);
    // TODO(keith): Fix this.
    //context.setWorkbench(workbench);

    // Folder will be by identifying name in the folder selected in the directory choose dialog
    String identifyingName = project.getIdentifyingName();
    project.setBaseDirectory(new File(chooseDirectoryWizard.getSelectedDirectory(), identifyingName));

    context.setSpecificationBase(activityProjectTemplateChooserWizard.getSelectedFile().getParentFile());

    workbench.getProjectCreator().create(context);

    workbenchUi.setCurrentProject(project);
  }

}
