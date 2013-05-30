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
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.ProjectCreationSpecification;
import interactivespaces.workbench.ui.wizard.Wizard;
import interactivespaces.workbench.ui.wizard.WizardCollection;
import interactivespaces.workbench.ui.wizard.component.ChooseDirectoryWizard;

import java.io.File;

/**
 * A {@link Wizard} for creating a new project.
 * 
 * @author Keith M. Hughes
 */
public class NewProjectWizard extends WizardCollection {

	/**
	 * The directory choosing wizard
	 */
	private ChooseDirectoryWizard chooseDirectoryWizard;

	/**
	 * The activity description wizard
	 */
	private ActivityDescriptionWizard activityDescriptionWizard;

	/**
	 * Wizard for choosing the project template.
	 */
	private ActivityProjectTemplateChooserWizard activityProjectTemplateChooserWizard;

	/**
	 * The workbench UI.
	 */
	private WorkbenchUi workbenchUi;

	/**
	 * The workbench.
	 */
	private InteractiveSpacesWorkbench workbench;

	public NewProjectWizard(WorkbenchUi workbenchUi,
			InteractiveSpacesWorkbench workbench) {
		this.workbenchUi = workbenchUi;
		this.workbench = workbench;

		chooseDirectoryWizard = new ChooseDirectoryWizard();
		activityDescriptionWizard = new ActivityDescriptionWizard();
		activityProjectTemplateChooserWizard = new ActivityProjectTemplateChooserWizard(
				workbench.getActivityProjectCreator()
						.getActivityProjectTemplates());

		addWizards(chooseDirectoryWizard, activityDescriptionWizard,
				activityProjectTemplateChooserWizard);
	}

	@Override
	public void completeWizard() {
		super.completeWizard();

		ProjectCreationSpecification spec = new ProjectCreationSpecification();

		Project project = activityDescriptionWizard.getProject();

		// Folder will be by identifying name in the folder selected in the
		// directory choose dialog
		String identifyingName = project.getIdentifyingName();
		project.setBaseDirectory(new File(chooseDirectoryWizard
				.getSelectedDirectory(), identifyingName));

		spec.setProject(project);

		spec.setTemplate(activityProjectTemplateChooserWizard
				.getSelectedTemplate());
		spec.setLanguage("java");

		workbench.getActivityProjectCreator().createProject(spec);

		workbenchUi.setCurrentProject(project);
	}

}
