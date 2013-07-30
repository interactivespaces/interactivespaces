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

import com.google.common.collect.Lists;

import interactivespaces.workbench.ui.validation.ValidationMessageDisplay;
import interactivespaces.workbench.ui.validation.ValidationResult;

import java.util.List;

import javax.swing.JComponent;

/**
 * A collection of {@link Wizard} elements which work as a unit.
 *
 * <p>
 * The wizards will work themselves in the order added.
 *
 * <p>
 * This class assumes at least 1 wizard added to the collection
 *
 * @author Keith M. Hughes
 */
public class WizardCollection implements Wizard {

  /**
   * The collection of wizards.
   */
  private List<Wizard> wizards = Lists.newArrayList();

  /**
   * The current wizard.
   */
  private int currentWizard = 1;

  /**
   * Add wizards to the collection.
   *
   * @param wizards
   *          the wizards to add
   */
  public void addWizards(Wizard... wizards) {
    for (Wizard wizard : wizards) {
      this.wizards.add(wizard);
    }
  }

  @Override
  public boolean hasNext() {
    // Assume collection has at least 1 member

    Wizard current = getCurrentWizard();
    if (current.hasNext()) {
      return true;
    }

    return currentWizard < wizards.size();
  }

  @Override
  public void moveNext() {
    Wizard current = getCurrentWizard();
    if (current.hasNext()) {
      current.moveNext();
    } else {
      currentWizard++;
    }
  }

  @Override
  public boolean hasPrevious() {
    Wizard current = getCurrentWizard();
    if (current.hasPrevious()) {
      return true;
    }

    return currentWizard > 1;
  }

  @Override
  public void movePrevious() {
    Wizard current = getCurrentWizard();
    if (current.hasPrevious()) {
      current.movePrevious();
    }

    currentWizard--;
  }

  @Override
  public JComponent getCurrentJComponent() {
    return getCurrentWizard().getCurrentJComponent();
  }

  @Override
  public ValidationResult validateCurrentWizard(boolean finalCheck) {
    return getCurrentWizard().validateCurrentWizard(finalCheck);
  }

  @Override
  public void initializeWizard() {
    // Walk the wizards in the order added.
    for (Wizard wizard : wizards) {
      wizard.initializeWizard();
    }
  }

  @Override
  public void completeWizard() {
    // Walk the wizards in the order added.
    for (Wizard wizard : wizards) {
      wizard.completeWizard();
    }
  }

  @Override
  public void setValidationMessageDisplay(ValidationMessageDisplay validationMessageDisplay) {
    for (Wizard wizard : wizards) {
      wizard.setValidationMessageDisplay(validationMessageDisplay);
    }
  }

  /**
   * Get the current wizard.
   *
   * @return the current wizard
   */
  private Wizard getCurrentWizard() {
    return wizards.get(currentWizard - 1);
  }
}
