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

package interactivespaces.workbench.activity.project;

import java.io.File;

/**
 * A manager for handling projects.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityProjectManager {

	/**
	 * Is the folder an activity project folder?
	 * 
	 * @param baseDir
	 *            the potential base project folder
	 * 
	 * @return {@code true} if a valid project folder
	 */
	boolean isActivityProjectFolder(File baseDir);

	/**
	 * Load a new project.
	 * 
	 * @param baseProjectDir
	 */
	ActivityProject readActivityProject(File baseProjectDir);

	/**
	 * Get a source for the activity configuration.
	 * 
	 * @param project
	 * @return
	 */
	Source getActivityConfSource(ActivityProject project);

	/**
	 * Save a source file.
	 * 
	 * @param source
	 *            the source file to save
	 */
	void saveSource(Source source);
}
