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

package interactivespaces.workbench.activity.project.builder;

import interactivespaces.workbench.activity.project.ActivityProject;
import interactivespaces.workbench.activity.project.ActivityProjectBuildContext;

/**
 * A builder for activities.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityBuilder {

	/**
	 * Build the given project.
	 * 
	 * @param project
	 *            the project to build
	 * @param context
	 *            the context to use when building the activity
	 */
	void build(ActivityProject project, ActivityProjectBuildContext context);
}
