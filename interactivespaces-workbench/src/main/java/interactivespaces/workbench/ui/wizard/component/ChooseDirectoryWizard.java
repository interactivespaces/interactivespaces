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

package interactivespaces.workbench.ui.wizard.component;

import interactivespaces.workbench.ui.wizard.SingleComponentWizard;
import interactivespaces.workbench.ui.wizard.WizardComponent;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * A {@link WizardComponent} for choosing a directory.
 *
 * @author Keith M. Hughes
 */
public class ChooseDirectoryWizard extends SingleComponentWizard {

  /**
   * The directory chooser for this panel.
   */
  private JFileChooser chooser;

  public ChooseDirectoryWizard() {
    chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    chooser.setControlButtonsAreShown(false);
  }

  @Override
  public JComponent getCurrentJComponent() {
    return chooser;
  }

  /**
   * Get the directory selected by the wizard.
   *
   * @return the directory selected
   */
  public File getSelectedDirectory() {
    File dir = chooser.getSelectedFile();
    if (dir == null) {
      dir = chooser.getCurrentDirectory();
    }

    return dir;
  }

  @Override
  public void completeWizard() {
    System.out.format("Got %s\n", getSelectedDirectory());
  }
}
