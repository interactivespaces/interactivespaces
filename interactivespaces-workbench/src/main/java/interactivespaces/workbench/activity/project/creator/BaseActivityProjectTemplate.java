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

package interactivespaces.workbench.activity.project.creator;

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProjectCreationSpecification;

import java.util.Map;

/**
 * A base implementation of a project template.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivityProjectTemplate implements ActivityProjectTemplate {

	/**
	 * The display name for the template.
	 */
	private String displayName;

	public BaseActivityProjectTemplate(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
