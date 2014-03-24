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

import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.ui.validation.ValidationMessageDisplay;
import interactivespaces.workbench.ui.validation.ValidationResult;
import interactivespaces.workbench.ui.wizard.SingleComponentWizard;
import interactivespaces.workbench.ui.wizard.Wizard;

import javax.swing.JComponent;

/**
 * A {@link Wizard} for getting an Project Description.
 *
 * @author Keith M. Hughes
 */
public class ProjectDescriptionWizard extends SingleComponentWizard {

  /**
   * The project description panel to use.
   */
  private final ProjectDescriptionPanel panel;

  /**
   * Construct a new wizard.
   *
   * @param project
   *          the project being created
   */
  public ProjectDescriptionWizard(Project project) {
    panel = new ProjectDescriptionPanel(project);
  }

  @Override
  public JComponent getCurrentJComponent() {
    return panel;
  }

  /**
   * Get the project from the wizard.
   *
   * @return the project
   */
  public Project getProject() {
    return panel.getProjectDescription();
  }

  @Override
  public ValidationResult validateCurrentWizard(boolean finalCheck) {
    return panel.checkValidation();
  }

  @Override
  public void setValidationMessageDisplay(ValidationMessageDisplay validationMessageDisplay) {
    super.setValidationMessageDisplay(validationMessageDisplay);
    panel.setValidationMessageDisplay(validationMessageDisplay);
  }
}
