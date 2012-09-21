/**
 * 
 */
package interactivespaces.workbench.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * 
 * @author Keith M. Hughes
 * @since Aug 27, 2012
 */
public class InteractiveSpacesNewProjectDialog extends JNewWizardDialog {
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

	public InteractiveSpacesNewProjectDialog(Frame parent) {
		super(parent);
	}

	@Override
	protected void initializeWizard() {
	}

	@Override
	public void getWizardPanel(JPanel mainPanel, GridBagConstraints gbc) {
		setSize(500, 200);

		KeyListener verifyingKeyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				checkWizard(false);
			}
		};

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("Project name:"), gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		projectNameInput = new JTextField();
		projectNameInput.addKeyListener(verifyingKeyListener);
		mainPanel.add(projectNameInput, gbc);
	}

	public ValidationResult checkWizard(boolean finalCheck) {
		String projectName = projectNameInput.getText().trim();
		int sizeName = projectName.length();
		if (sizeName == 0) {
			writeMessage(MessageType.ERROR, "A project name is required.");
			return ValidationResult.ERRORS;
		}

		if (!PROJECT_NAME_PATTERN.matcher(projectName).matches()) {
			writeMessage(MessageType.ERROR,
					"Illegal characters in the project name.");
			return ValidationResult.ERRORS;
		}

		clearMessage();
		return ValidationResult.OK;
	}

	@Override
	protected void completeWizard() {
	}

	/**
	 * Input control for the name of the project.
	 */
	private JTextField projectNameInput;
}
