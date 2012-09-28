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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Wizard for creating a new Interactive Spaces project.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesNewProjectDialog extends JDialog {

	/**
	 * Regex definition for project names.
	 */
	private static final String PROJECT_NAME_REGEX = "[a-zA-Z_]{1}[a-zA-Z0-9_]*";

	/**
	 * Regex pattern for project names.
	 */
	private static final Pattern PROJECT_NAME_PATTERN;

	static {
		PROJECT_NAME_PATTERN = Pattern.compile(PROJECT_NAME_REGEX);
	}

	/**
	 * Input control for the name of the project.
	 */
	private JTextField projectNameInput;

	/**
	 * Input control for the description of the project.
	 */
	private JTextArea projectDescriptionInput;

	/**
	 * Input control for the identifying name of the project.
	 */
	private JTextField projectIdentifyingNameInput;

	/**
	 * Input control for the version of the project.
	 */
	private JTextField projectVersionInput;

	public InteractiveSpacesNewProjectDialog(Frame parent) {
		//super("New Project", parent, null);
	}

	//@Override
	protected void initializeWizard() {
	}

	//@Override
	public void getWizardPanel(JPanel mainPanel, GridBagConstraints gbc) {
		setSize(500, 400);

		KeyListener verifyingKeyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				//checkWizard(false);
			}
		};

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(new JLabel("Name:"), gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		projectNameInput = new JTextField();
		projectNameInput.addKeyListener(verifyingKeyListener);
		mainPanel.add(projectNameInput, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(new JLabel("Description:"), gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 1;
		projectDescriptionInput = new JTextArea();
		projectDescriptionInput.setRows(4);
		JScrollPane scrollPane = new JScrollPane(projectDescriptionInput);
		
		mainPanel.add(scrollPane, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(new JLabel("Identifying name:"), gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		projectIdentifyingNameInput = new JTextField();
		projectIdentifyingNameInput.addKeyListener(verifyingKeyListener);
		mainPanel.add(projectIdentifyingNameInput, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(new JLabel("Version:"), gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		projectVersionInput = new JTextField();
		projectVersionInput.addKeyListener(verifyingKeyListener);
		mainPanel.add(projectVersionInput, gbc);
	}
//
//	@Override
//	protected JComponent getMainPanel() {
//		
//		JFileChooser chooser = new JFileChooser();
//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
//		chooser.setControlButtonsAreShown(false);
//		return chooser;
//	}

//	public ValidationResult checkWizard(boolean finalCheck) {
//		String projectName = projectNameInput.getText().trim();
//		int sizeName = projectName.length();
//		if (sizeName == 0) {
//			writeMessage(MessageType.ERROR, "A project name is required.");
//			return ValidationResult.ERRORS;
//		}
//
//		if (!PROJECT_NAME_PATTERN.matcher(projectName).matches()) {
//			writeMessage(MessageType.ERROR,
//					"Illegal characters in the project name.");
//			return ValidationResult.ERRORS;
//		}
//
//		clearMessage();
//		return ValidationResult.OK;
//	}

	protected void completeWizard() {
	}
}
