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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProjectCreationSpecification;

import java.io.File;
import java.util.Map;

/**
 * A project creator for java projects.
 * 
 * @author Keith M. Hughes
 */
public class JavaProjectSourceDescription implements ProjectSourceDescription {

	@Override
	public void process(ActivityProjectCreationSpecification spec,
			InteractiveSpacesWorkbench workbench,
			FreemarkerTemplater templater, Map<String, Object> templateData) {
		ActivityDescription activity = spec.getProject().getActivityDescription();
		activity.setBuilderType("java");
		spec.getProject().setActivityType("interactivespaces_native");

		spec.setExecutable(activity.getIdentifyingName() + "-"
				+ activity.getVersion() + ".jar");
		spec.addExtraConfigurationParameter("space.activity.log.level",
				InteractiveSpacesEnvironment.LOG_LEVEL_INFO);
		spec.addExtraConfigurationParameter("space.activity.java.class",
				activity.getIdentifyingName() + ".SimpleJavaActivity");

		File javaSourceDir = makeSourceDirs(spec);

		// TODO(keith): Fix this when start supporting Windoze
		String pathname = spec.getProject().getActivityDescription().getIdentifyingName()
				.replace('.', '/');
		File sourceDirectory = new File(javaSourceDir, pathname);
		if (!sourceDirectory.mkdirs()) {
			throw new InteractiveSpacesException(String.format(
					"Could not create java source directory %s",
					sourceDirectory.getAbsolutePath()));
		}

		templater.writeTemplate(templateData, new File(sourceDirectory,
				"SimpleJavaActivity.java"),
				"activity/generic/java/simple/SimpleJavaActivity.java.ftl");
	}

	/**
	 * Make the source directories necessary for the project.
	 * 
	 * @param spec
	 *            the project specification
	 * 
	 * @return the base Java source directory.
	 */
	private File makeSourceDirs(ActivityProjectCreationSpecification spec) {
		File javaSourceDir = new File(spec.getProject().getBaseDirectory(),
				"src/main/java");

		if (!javaSourceDir.mkdirs()) {
			throw new InteractiveSpacesException(String.format(
					"Could not create java source directory %s",
					javaSourceDir.getAbsolutePath()));
		}

		return javaSourceDir;
	}
}
