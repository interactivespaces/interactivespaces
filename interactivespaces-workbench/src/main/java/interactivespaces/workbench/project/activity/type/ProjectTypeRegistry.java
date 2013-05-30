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

package interactivespaces.workbench.project.activity.type;

import interactivespaces.workbench.project.Project;

/**
 * A registry of {link ProjectType} instances.
 * 
 * @author Keith M. Hughes
 */
public interface ProjectTypeRegistry {

	/**
	 * Get an project type.
	 * 
	 * @param project
	 *            the project we want the type for
	 * 
	 * @return the project type, or {@code null} if none for the given name
	 */
	ProjectType getProjectType(Project project);

	/**
	 * Register a project type.
	 * 
	 * <p>
	 * If there is already a project type of the given name, the new one will
	 * replace the old one.
	 * 
	 * @param type
	 *            the project type to register
	 */
	void registerProjectType(ProjectType type);
}
