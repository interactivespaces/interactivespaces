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

import interactivespaces.workbench.activity.project.creator.ActivityProjectTemplate;
import interactivespaces.workbench.ui.wizard.SingleComponentWizard;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * A wizard for activity template choice.
 *
 * @author Keith M. Hughes
 */
public class ActivityProjectTemplateChooserWizard extends SingleComponentWizard {

	/**
	 * Combo box for picking the project template.
	 */
	private JComboBox comboBox;
	
	/**
	 * The templates being shown.
	 */
	private List<ActivityProjectTemplate> templates;

	public ActivityProjectTemplateChooserWizard(List<ActivityProjectTemplate> templates) {
		this.templates = templates;
		comboBox = new JComboBox(templates.toArray());
	}

	@Override
	public JComponent getCurrentJComponent() {
		return comboBox;
	}
	
	/**
	 * Get the selected project template.
	 * 
	 * @return the selected project template.
	 */
	public ActivityProjectTemplate getSelectedTemplate() {
		return templates.get(comboBox.getSelectedIndex());
	}
}
