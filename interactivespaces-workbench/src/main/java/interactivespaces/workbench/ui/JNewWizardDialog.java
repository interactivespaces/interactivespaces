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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A base class for creating wizard classes.
 * 
 * @author Keith M. Hughes
 * @since Aug 27, 2012
 */
public abstract class JNewWizardDialog extends JDialog implements ActionListener {
	public JNewWizardDialog(Frame parent) {
		super(parent, "New Project", true);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		finishButton = new JButton("Finish");
		finishButton.addActionListener(this);

		// Put the buttons in a flow panel so that they get centered and are
		// next to each other
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(cancelButton);
		buttonPanel.add(finishButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		messageIcon = new ImagePanel();
		messageLabel = new JLabel();
		infoPanel.add(messageIcon);
		infoPanel.add(messageLabel);
		getContentPane().add(infoPanel, BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(new GridBagLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		// Components that are too short or narrow for their space
		// Should be pinned to the northwest (upper left) corner
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		getWizardPanel(mainPanel, gbc);

		initializeWizard();
	}

	/**
	 * Initialize the wizard.
	 */
	protected void initializeWizard() {
		// Default is do nothing.
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Object command = ae.getSource();

		if (command.equals(finishButton)) {
			if (checkWizard(true) != ValidationResult.ERRORS) {
				completeWizard();

				setVisible(false);
				dispose();
			}
		} else if (command.equals(cancelButton)) {
			setVisible(false);
			dispose();
		}
	}

	protected String getDefaultMessage() {
		return "   ";
	}

	/**
	 * The result of a validation.
	 * 
	 * @author Keith M. Hughes
	 * @since Feb 8, 2011
	 */
	public enum ValidationResult {
		/**
		 * Everything validated properly.
		 */
		OK,

		/**
		 * There were warnings, but no errors.
		 */
		WARNINGS,

		/**
		 * There are errors.
		 */
		ERRORS
	}

	/**
	 * The types of messages that can be written.
	 * 
	 * @author Keith M. Hughes
	 * @since May 8, 2010
	 */
	public enum MessageType {
		INFO, WARNING, ERROR
	}

	/**
	 * Write out a message to the information window.
	 * 
	 * @param type
	 *            Type of the message.
	 * @param message
	 *            The message.
	 */
	public void writeMessage(MessageType type, String message) {
		hasMessage = true;
		switch (type) {
		case ERROR:
			messageIcon
					.setImage(ImagePanel.loadImage("images/WizardError.gif"));
			hasErrorMessage = true;
			break;
		case INFO:
			messageIcon.setImage(ImagePanel.loadImage("images/WizardInfo.gif"));
			hasErrorMessage = false;
			break;
		case WARNING:
			messageIcon.setImage(ImagePanel
					.loadImage("images/WizardWarning.gif"));
			hasErrorMessage = false;
			break;
		default:
			messageIcon.setImage(null);
		}

		messageLabel.setText(message);
	}

	/**
	 * Clear any message out.
	 */
	public void clearMessage() {
		messageIcon.setImage(null);
		messageLabel.setText("");

		hasMessage = false;
		hasErrorMessage = false;
	}

	/**
	 * Add the wizard specific components.
	 */
	protected void getWizardPanel(JPanel mainPanel, GridBagConstraints gbc) {
		// The default is to do nothing.
	}

	/**
	 * Check to see if the wizard is ready to complete.
	 * 
	 * <p>
	 * This method should pop any warning windows for fields not properly filled
	 * out.
	 * 
	 * @param finalCheck
	 *            TODO
	 * 
	 * @return The result of verifying the wizard.
	 */
	protected ValidationResult checkWizard(boolean finalCheck) {
		return ValidationResult.OK;
	}

	/**
	 * Complete the generation of the project.
	 */
	protected void completeWizard() {
		// Default is to do nothing
	}

	/**
	 * Where the message icon will go.
	 */
	private ImagePanel messageIcon;

	/**
	 * There is a message on display
	 */
	protected boolean hasMessage;

	/**
	 * There is an error.
	 */
	protected boolean hasErrorMessage;

	/**
	 * Where the messages will go.
	 */
	private JLabel messageLabel;

	private JButton cancelButton;

	private JButton finishButton;
}
