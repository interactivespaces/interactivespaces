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
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProject;
import interactivespaces.workbench.activity.project.ActivityProjectCreationSpecification;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link ActivityProjectCreator} implementation.
 * 
 * @author Keith M. Hughes
 */
public class ActivityProjectCreatorImpl implements ActivityProjectCreator {

	/**
	 * Templater to use
	 */
	private FreemarkerTemplater templater;

	/**
	 * The workbench used by the creator.
	 */
	private InteractiveSpacesWorkbench workbench;

	public ActivityProjectCreatorImpl(InteractiveSpacesWorkbench workbench,
			FreemarkerTemplater templater) {
		this.workbench = workbench;
		this.templater = templater;
	}

	@Override
	public void createProject(ActivityProjectCreationSpecification spec) {
		try {
			ActivityProject project = spec.getProject();
			createProjectStructure(project);

			addSpecDetails(spec);

			// Create the templateData hash
			Map<String, Object> templateData = new HashMap<String, Object>();
			templateData.put("spec", spec);
			templateData.put("project", project);

			writeProjectTemplate(spec, templateData);

			writeActivityXml(spec, templateData);
			writeActivityConf(spec, templateData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Add any additional details to the spec.
	 * 
	 * @param spec
	 *            the spec to add to
	 */
	private void addSpecDetails(ActivityProjectCreationSpecification spec) {
		// Create an activity name from the identifying name
		String identifyingName = spec.getProject().getActivityDescription()
				.getIdentifyingName();
		String[] parts = identifyingName.split("\\.");
		StringBuilder activityName = new StringBuilder(parts[0]);

		for (int i = 1; i < parts.length; ++i) {
			String part = parts[i];
			activityName.append(part.substring(0, 1).toUpperCase()).append(
					part.substring(1));
		}

		spec.getProject().getActivityDescription().setName(activityName.toString());
	}

	/**
	 * Create all common directory structures for the project.
	 * 
	 * @param project
	 *            the project being created.
	 */
	private void createProjectStructure(ActivityProject project) {
		makeDirectory(project.getBaseDirectory());
		makeDirectory(project.getActivityResourceDirectory());
	}

	/**
	 * Make a directory.
	 * 
	 * Throw an exception if can't.
	 * 
	 * @param baseDirectory
	 */
	protected void makeDirectory(File directory) {
		if (!directory.mkdirs()) {
			throw new InteractiveSpacesException(String.format(
					"Cannot create directory %s", directory.getAbsolutePath()));
		}
	}

	/**
	 * 
	 * @param spec
	 *            the build specification
	 * @param templateData
	 *            data for any templates
	 */
	private void writeActivityXml(ActivityProjectCreationSpecification spec,
			Map<String, Object> templateData) {
		templater.writeTemplate(templateData, spec.getProject()
				.getActivityDescriptionFile(), "activity.xml.ftl");
	}

	/**
	 * @param spec
	 *            the build specification
	 * @param templateData
	 *            data to go into the template
	 */
	private void writeActivityConf(ActivityProjectCreationSpecification spec,
			Map<String, Object> templateData) {
		templater.writeTemplate(templateData, spec.getProject()
				.getActivityConfigFile(), "activity.conf.ftl");
	}

	/**
	 * Write out the code template.
	 * 
	 * @param spec
	 *            the build specification
	 * @param templateData
	 *            data to go into the template
	 */
	private void writeProjectTemplate(
			ActivityProjectCreationSpecification spec,
			Map<String, Object> templateData) {
		String language = spec.getLanguage();
		if ("java".equals(language)) {
			writeProjectTemplate(new JavaProjectSourceDescription(), spec,
					templateData);
		} else if ("python".equals(language)) {
			writeProjectTemplate(new PythonProjectSourceDescription(), spec,
					templateData);
		} else if ("javascript".equals(language)) {
			writeProjectTemplate(new JavascriptProjectSourceDescription(), spec,
					templateData);
		} else {
			throw new InteractiveSpacesException(String.format(
					"Unknown language %s", language));
		}
	}

	/**
	 * Write out the code template.
	 * 
	 * @param spec
	 *            the build specification
	 * @param templateData
	 *            data to go into the template
	 */
	private void writeProjectTemplate(
			ProjectSourceDescription sourceDescription,
			ActivityProjectCreationSpecification spec,
			Map<String, Object> templateData) {
		sourceDescription.process(spec, workbench, templater, templateData);
	}
}
