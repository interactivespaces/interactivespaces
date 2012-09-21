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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.util.io.Files;

import java.io.File;
import java.io.FileInputStream;

/**
 * A basic {@link ActivityProjectManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicActivityProjectManager implements ActivityProjectManager {
	
	@Override
	public boolean isActivityProjectFolder(File baseDir) {
		return getActivityProjectFile(baseDir).exists();
	}

	@Override
	public ActivityProject readActivityProject(File baseProjectDir) {
		ActivityDescriptionReader reader = new JdomActivityDescriptionReader();

		File descriptionFile = getActivityProjectFile(baseProjectDir);
		try {
			FileInputStream activityDescriptionStream = new FileInputStream(
					descriptionFile);
			ActivityDescription activity = reader
					.readDescription(activityDescriptionStream);

			ActivityProject project = new ActivityProject(activity);
			project.setBaseDirectory(baseProjectDir);
			return project;
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Cannot read activity description file %s",
					descriptionFile.getAbsolutePath()), e);
		}
	}

	@Override
	public Source getActivityConfSource(ActivityProject project) {
		
		Source source = new SimpleSource();
		File sourceFile = project.getActivityConfigFile();
		source.setPath(sourceFile.getAbsolutePath());
		source.setProject(project);
		source.setContent(Files.readFile(sourceFile));
		
		return source;
	}

	@Override
	public void saveSource(Source source) {
		Files.writeFile(new File(source.getPath()), source.getContent());
	}

	/**
	 * Get the activity description file from the base project folder.
	 * 
	 * @param baseDir
	 * 		the base project folder
	 * 
	 * @return the file for the project file
	 */
	private File getActivityProjectFile(File baseDir) {
		return new File(baseDir, "activity.xml");
	}
}
