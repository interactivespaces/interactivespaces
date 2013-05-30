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

package interactivespaces.workbench.project.activity.ide;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ActivityProject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Create an eclipse project.
 * 
 * @author Keith M. Hughes
 */
public class EclipseIdeProjectCreator {

	private static final String TEMPLATE_FILEPATH_ECLIPSE_PROJECT = "ide/eclipse/project.ftl";

	private static final String FILENAME_PROJECT_FILE = ".project";

	private static final JarFileFilter JAR_FILE_FILTER = new JarFileFilter();

	/**
	 * The Templater.
	 */
	private FreemarkerTemplater templater;

	public EclipseIdeProjectCreator(FreemarkerTemplater templater) {
		this.templater = templater;
	}

	/**
	 * Create the IDE project.
	 * 
	 * @param project
	 *            project creating the IDE version for param spec the
	 *            specification giving details about the IDE build
	 * @param workbench
	 *            workbench being run under
	 */
	public void createProject(Project project,
			EclipseIdeProjectCreatorSpecification spec,
			InteractiveSpacesWorkbench workbench) {
		try {
			// Create the freemarkerContext hash
			Map<String, Object> freemarkerContext = new HashMap<String, Object>();
			freemarkerContext.put("project", project);

			spec.addSpecificationData(project, freemarkerContext);

			writeProjectFile(project, freemarkerContext);

			spec.writeAdditionalFiles(project, freemarkerContext, templater,
					workbench);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Write the project file.
	 * 
	 * @param freemarkerConfig
	 * @param freemarkerContext
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void writeProjectFile(Project project,
			Map<String, Object> freemarkerContext) throws IOException,
			TemplateException {
		templater.writeTemplate(freemarkerContext,
				new File(project.getBaseDirectory(), FILENAME_PROJECT_FILE),
				TEMPLATE_FILEPATH_ECLIPSE_PROJECT);
	}

	/**
	 * Write a a template out.
	 * 
	 * @param config
	 *            the freemarker template configuration
	 * @param freemarkerContext
	 *            the data for the templates
	 * @param template
	 *            location of the template
	 * @param outputFile
	 *            the output file
	 * 
	 * @throws IOException
	 * @throws TemplateException
	 */
	protected void writeTemplate(Configuration config,
			Map<String, Object> freemarkerContext, String template,
			File outputFile) throws IOException, TemplateException {
		Template temp = config.getTemplate(template);

		Writer out = new FileWriter(outputFile);
		temp.process(freemarkerContext, out);
		out.flush();
	}

	/**
	 * File filter that only takes JAR files.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class JarFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".jar");
		}

	}
}
