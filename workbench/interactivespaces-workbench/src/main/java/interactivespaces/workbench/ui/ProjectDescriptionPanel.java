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

import interactivespaces.domain.support.IdentifyingNameValidator;
import interactivespaces.domain.support.DomainValidationResult;
import interactivespaces.domain.support.VersionValidator;
import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.resource.Version;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.ui.validation.ValidationMessageDisplay;
import interactivespaces.workbench.ui.validation.ValidationMessageType;
import interactivespaces.workbench.ui.validation.ValidationResult;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A panel to allow editing of project descriptions.
 *
 * @author Keith M. Hughes
 */
public class ProjectDescriptionPanel extends JPanel {

  /**
   * Number of rows in a description box.
   */
  public static final int DESCRIPTION_BOX_ROWS = 4;

  /**
   * Input control for the name of the project.
   */
  private final JTextField projectNameInput;

  /**
   * Input control for the description of the project.
   */
  private final JTextArea projectDescriptionInput;

  /**
   * Input control for the identifying name of the project.
   */
  private final JTextField projectIdentifyingNameInput;

  /**
   * Input control for the version of the project.
   */
  private final JTextField projectVersionInput;

  /**
   * The activity description being editted or created.
   */
  private Project project;

  /**
   * The display for validation messages.
   */
  private ValidationMessageDisplay validationMessageDisplay;

  /**
   * The validator for identifying names.
   */
  private final IdentifyingNameValidator identifyingNameValidator;

  /**
   * The validator for version numbers.
   */
  private final VersionValidator versionValidator;

  /**
   * Construct a description panel.
   *
   * @param project
   *          the project to back the panel
   */
  public ProjectDescriptionPanel(Project project) {
    setLayout(new GridBagLayout());

    KeyListener verifyingKeyListener = new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        checkValidation();
      }
    };

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);

    // Components that are too short or narrow for their space
    // Should be pinned to the northwest (upper left) corner
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JLabel("Name:"), gbc);
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    projectNameInput = new JTextField();
    projectNameInput.addKeyListener(verifyingKeyListener);
    add(projectNameInput, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JLabel("Description:"), gbc);
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = 1;
    projectDescriptionInput = new JTextArea();
    projectDescriptionInput.setRows(DESCRIPTION_BOX_ROWS);
    JScrollPane scrollPane = new JScrollPane(projectDescriptionInput);

    add(scrollPane, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridheight = 1;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JLabel("Identifying name:"), gbc);
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    projectIdentifyingNameInput = new JTextField();
    projectIdentifyingNameInput.addKeyListener(verifyingKeyListener);
    add(projectIdentifyingNameInput, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JLabel("Version:"), gbc);
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    projectVersionInput = new JTextField();
    projectVersionInput.addKeyListener(verifyingKeyListener);
    add(projectVersionInput, gbc);

    identifyingNameValidator = new IdentifyingNameValidator();
    versionValidator = new VersionValidator();
  }

  /**
   * Set the description to back the panel.
   *
   * @param project
   *          the project to use
   */
  public void setProject(Project project) {
    this.project = project;

    String name = project.getName();
    if (name != null) {
      projectNameInput.setText(name);
    }

    String description = project.getDescription();
    if (description != null) {
      projectDescriptionInput.setText(description);
    }

    String identifyingName = project.getIdentifyingName();
    if (identifyingName != null) {
      projectIdentifyingNameInput.setText(identifyingName);
    }

    Version version = project.getVersion();
    if (version != null) {
      projectVersionInput.setText(version.toString());
    }
  }

  /**
   * Synchronize the activity description to what is in the edit boxes.
   */
  private void syncProjectDescription() {
    project.setName(projectNameInput.getText());
    project.setDescription(projectDescriptionInput.getText());
    project.setIdentifyingName(projectIdentifyingNameInput.getText());
    project.setVersion(Version.parseVersion(projectVersionInput.getText()));
  }

  /**
   * Get the activity description being used.
   *
   * @return the activity description
   */
  public Project getProjectDescription() {
    if (project == null) {
      project = new ActivityProject();
    }

    syncProjectDescription();

    return project;
  }

  /**
   * Check the validation of this panel.
   *
   * @return the result of the validation check
   */
  public ValidationResult checkValidation() {
    String projectName = projectNameInput.getText().trim();
    int sizeName = projectName.length();
    if (sizeName == 0) {
      validationMessageDisplay.showValidationMessage(ValidationMessageType.ERROR, "A project name is required.");
      return ValidationResult.ERRORS;
    }

    String identifyingName = projectIdentifyingNameInput.getText().trim();
    DomainValidationResult validationResult = identifyingNameValidator.validate(identifyingName);
    if (validationResult.getResultType() == DomainValidationResultType.ERRORS) {
      validationMessageDisplay.showValidationMessage(ValidationMessageType.ERROR, validationResult.getDescription());

      return ValidationResult.ERRORS;
    }

    String version = projectVersionInput.getText().trim();
    validationResult = versionValidator.validate(version);
    if (validationResult.getResultType() == DomainValidationResultType.ERRORS) {
      validationMessageDisplay.showValidationMessage(ValidationMessageType.ERROR, validationResult.getDescription());

      return ValidationResult.ERRORS;
    }

    validationMessageDisplay.clearValidationMessage();

    return ValidationResult.OK;
  }

  /**
   * @param validationMessageDisplay
   *          the validationMessageDisplay to set
   */
  public void setValidationMessageDisplay(ValidationMessageDisplay validationMessageDisplay) {
    this.validationMessageDisplay = validationMessageDisplay;
  }
}
