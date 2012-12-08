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
import interactivespaces.workbench.activity.project.type.android.GenericAndroidActivityProjectTemplate;
import interactivespaces.workbench.activity.project.type.java.GenericJavaActivityProjectTemplate;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * A {@link ActivityProjectCreator} implementation.
 * 
 * @author Keith M. Hughes
 */
public class ActivityProjectCreatorImpl implements ActivityProjectCreator {

	/**
	 * The list of activities to be handed to clients.
	 */
	private List<ActivityProjectTemplate> activityProjectTemplates;

	/**
	 * The list of activities to be used internally.
	 */
	private List<ActivityProjectTemplate> activityProjectTemplatesInternal;

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

		activityProjectTemplatesInternal = Lists.newArrayList();
		activityProjectTemplates = Collections
				.unmodifiableList(activityProjectTemplatesInternal);

		activityProjectTemplatesInternal
				.add(new GenericJavaActivityProjectTemplate());
		activityProjectTemplatesInternal
				.add(new GenericJavascriptActivityProjectTemplate());
		activityProjectTemplatesInternal
				.add(new GenericPythonActivityProjectTemplate());
		activityProjectTemplatesInternal
				.add(new GenericAndroidActivityProjectTemplate());
	}

	@Override
	public List<ActivityProjectTemplate> getActivityProjectTemplates() {
		return activityProjectTemplates;
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
		StringBuilder activityRuntimeName = new StringBuilder(parts[0]);

		for (int i = 1; i < parts.length; ++i) {
			String part = parts[i];
			activityRuntimeName.append(part.substring(0, 1).toUpperCase())
					.append(part.substring(1));
		}

		spec.getProject().getActivityDescription()
				.setActivityRuntimeName(activityRuntimeName.toString());
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
		ActivityProjectTemplate template = spec.getTemplate();
		if (template == null) {
			template = getActivityProjectTemplateByLanguage(spec.getLanguage());
		}
		
		writeProjectTemplate(template, spec, templateData);
	}

	/**
	 * Get a generic project template by language.
	 * 
	 * @param language
	 *            the language
	 * 
	 * @return the generic template for that language
	 */
	private ActivityProjectTemplate getActivityProjectTemplateByLanguage(
			String language) {
		if ("java".equals(language)) {
			return new GenericJavaActivityProjectTemplate();
		} else if ("python".equals(language)) {
			return new GenericPythonActivityProjectTemplate();
		} else if ("javascript".equals(language)) {
			return new GenericJavascriptActivityProjectTemplate();
		} else if ("android".equals(language)) {
			return new GenericAndroidActivityProjectTemplate();
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
			ActivityProjectTemplate sourceDescription,
			ActivityProjectCreationSpecification spec,
			Map<String, Object> templateData) {
		sourceDescription.process(spec, workbench, templater, templateData);
	}
}
