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

package interactivespaces.workbench.project.activity;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.JdomProjectReader;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectReader;

import java.io.File;
import java.io.FileInputStream;

/**
 * A basic {@link ActivityProjectManager}.
 * 
 * @author Keith M. Hughes
 */
public class BasicActivityProjectManager implements ActivityProjectManager {

	@Override
	public boolean isProjectFolder(File baseDir) {
		File projectFile = new File(baseDir, "project.xml");
		if (projectFile.exists()) {
			return true;
		}
		return new File(baseDir, "activity.xml").exists();
	}

	@Override
	public Project readProject(File baseProjectDir) {
		File projectFile = new File(baseProjectDir, "project.xml");
		if (projectFile.exists()) {
			return readProjectFile(projectFile);
		}

		File activityFile = new File(baseProjectDir, "activity.xml");
		if (activityFile.exists()) {
			return convertActivity(activityFile);
		}

		throw new InteractiveSpacesException(
				String.format(
						"The folder %s does not contain any legal Interactive Spaces project files",
						baseProjectDir.getAbsolutePath()));
	}

	/**
	 * Read a project file.
	 * 
	 * @param projectFile
	 *            the project file
	 * 
	 * @return the project
	 */
	public Project readProjectFile(File projectFile) {
		ProjectReader reader = new JdomProjectReader();

		try {
			return reader.readDescription(projectFile);
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Cannot read project file %s",
					projectFile.getAbsolutePath()), e);
		}
	}

	/**
	 * Get an activity file and convert it to a project.
	 * 
	 * @param activityFile
	 *            the activity file
	 * 
	 * @return the project
	 */
	private Project convertActivity(File activityFile) {
		ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
		FileInputStream activityDescriptionStream = null;
		try {
			activityDescriptionStream = new FileInputStream(activityFile);
			ActivityDescription activity = reader
					.readDescription(activityDescriptionStream);

			Project project = new Project();
			project.setBaseDirectory(activityFile.getParentFile());
			project.setName(activity.getName());
			project.setDescription(activity.getDescription());
			project.setBuilderType(activity.getBuilderType());
			project.setIdentifyingName(activity.getIdentifyingName());
			project.setVersion(activity.getVersion());
			project.setType("activity");

			return project;
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Cannot read activity description file %s",
					activityFile.getAbsolutePath()), e);
		}
	}

	@Override
	public Source getActivityConfSource(Project project) {
		// TODO(keith): This sucks!!!
		ActivityProject aproject = new ActivityProject(project);
		Source source = new SimpleSource();
		File sourceFile =aproject.getActivityConfigFile();
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
	 *            the base project folder
	 * 
	 * @return the file for the project file
	 */
	private File getActivityProjectFile(File baseDir) {
		return new File(baseDir, "activity.xml");
	}
}
