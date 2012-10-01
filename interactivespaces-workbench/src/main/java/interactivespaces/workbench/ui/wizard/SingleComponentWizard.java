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

/**
 * A {@link WizardComponent} which has only 1 internal component.
 *
 * @author Keith M. Hughes
 */
public abstract class SingleComponentWizard implements WizardComponent {
	private ValidationMessageDisplay validationMessageDisplay;
	
	@Override
	public boolean hasNext() {
		// Nowhere to move to
		return false;
	}

	@Override
	public void moveNext() {
		// Nowhere to move to
	}

	@Override
	public boolean hasPrevious() {
		// Nowhere to move to
		return false;
	}

	@Override
	public void movePrevious() {
		// Nowhere to move to
	}

	@Override
	public void initializeWizard() {
		// Default is nothing to initialize.
	}

	@Override
	public void completeWizard() {
		// Default is nothing to complete.
	}
	
	@Override
	public ValidationResult validateCurrentWizard(boolean finalCheck) {
		// Default is all is OK.
		return ValidationResult.OK;
	}

	@Override
	public void setValidationMessageDisplay(ValidationMessageDisplay validationMessageDisplay) {
		this.validationMessageDisplay = validationMessageDisplay;
	}

	/**
	 * @return the validationMessageDisplay
	 */
	public ValidationMessageDisplay getValidationMessageDisplay() {
		return validationMessageDisplay;
	}
}
