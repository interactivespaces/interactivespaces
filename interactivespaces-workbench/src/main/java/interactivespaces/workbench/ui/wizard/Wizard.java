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

package interactivespaces.workbench.ui.wizard;

import interactivespaces.workbench.ui.validation.ValidationMessageDisplay;
import interactivespaces.workbench.ui.validation.ValidationResult;

import javax.swing.JComponent;

/**
 * A wizard for UI events.
 * 
 * @author Keith M. Hughes
 */
public interface Wizard {

	/**
	 * Does the manager currently have a next panel?
	 * 
	 * @return {@code true} if there is another panel to move to
	 */
	boolean hasNext();

	/**
	 * Move to the next panel.
	 * 
	 * <p>
	 * Does nothing if there is no next panel.
	 */
	void moveNext();

	/**
	 * Does the manager currently have a previous panel?
	 * 
	 * @return {@code true} if there is a previous panel to move to
	 */
	boolean hasPrevious();

	/**
	 * Move to the previous panel.
	 * 
	 * <p>
	 * Does nothing if there is no previous panel.
	 */
	void movePrevious();

	/**
	 * Get the current panel for the wizard.
	 * 
	 * @return
	 */
	JComponent getCurrentJComponent();

	/**
	 * Check the current wizard to see if it is validating.
	 * 
	 * @param finalCheck
	 *            is this the final check?
	 * 
	 * @return the {@link ValidationResult}
	 */
	ValidationResult validateCurrentWizard(boolean finalCheck);

	/**
	 * Initialize the wizard.
	 */
	void initializeWizard();

	/**
	 * Complete the wizard.
	 */
	void completeWizard();

	/**
	 * Set the validation message display to be used.
	 * 
	 * @param validationMessageDisplay
	 *            the display to use
	 */
	void setValidationMessageDisplay(
			ValidationMessageDisplay validationMessageDisplay);
}
