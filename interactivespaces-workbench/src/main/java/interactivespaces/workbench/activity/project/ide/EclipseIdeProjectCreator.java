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

package interactivespaces.workbench.activity.project.ide;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * Create an eclipse project.
 * 
 * @author Keith M. Hughes
 */
public class EclipseIdeProjectCreator {

	private static final JarFileFiler JAR_FILE_FILTER = new JarFileFiler();

	private static final String ECLIPSE_BUILDER_NON_JAVA = "org.eclipse.wst.common.project.facet.core.builder";
	private static final String ECLIPSE_NATURE_NON_JAVA = "org.eclipse.wst.common.project.facet.core.nature";

	private static final String ECLIPSE_BUILDER_JAVA = "org.eclipse.jdt.core.javabuilder";
	private static final String ECLIPSE_NATURE_JAVA = "org.eclipse.jdt.core.javanature";

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
	 *            project creating the IDE version for
	 * @param workbench
	 *            workbench being run under
	 */
	public void createProject(ActivityProject project,
			InteractiveSpacesWorkbench workbench) {
		try {
			// Create the freemarkerContext hash
			Map<String, Object> freemarkerContext = new HashMap<String, Object>();
			freemarkerContext.put("project", project);
			freemarkerContext.put("libs", getProjectLibs(workbench));

			addLanguageSpecificData(project, freemarkerContext);

			writeProjectFile(project, freemarkerContext);

			if ("java".equals(project.getActivity().getBuilderType())) {
				writeClasspathFile(project, freemarkerContext);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Add further context needed for the project.
	 * 
	 * @param project
	 * 
	 * @param freemarkerContext
	 */
	private void addLanguageSpecificData(ActivityProject project,
			Map<String, Object> freemarkerContext) {
		if ("java".equals(project.getActivity().getBuilderType())) {
			freemarkerContext.put("natures",
					Lists.newArrayList(ECLIPSE_NATURE_JAVA));
			freemarkerContext.put("builder", ECLIPSE_BUILDER_JAVA);
		} else {
			freemarkerContext.put("natures",
					Lists.newArrayList(ECLIPSE_NATURE_NON_JAVA));
			freemarkerContext.put("builder", ECLIPSE_BUILDER_NON_JAVA);
		}
	}

	/**
	 * Get file paths to all libraries needed for the project.
	 * 
	 * @param workbench
	 *            workbench being run under
	 * 
	 * @return full qualified path names for all libs
	 */
	private List<String> getProjectLibs(InteractiveSpacesWorkbench workbench) {
		List<String> libs = Lists.newArrayList();

		for (File lib : workbench.getControllerBootstrapDir().listFiles(
				JAR_FILE_FILTER)) {
			libs.add(lib.getAbsolutePath());
		}

		return libs;
	}

	/**
	 * Write the project file.
	 * 
	 * @param freemarkerConfig
	 * @param freemarkerContext
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void writeProjectFile(ActivityProject project,
			Map<String, Object> freemarkerContext) throws IOException,
			TemplateException {
		templater.writeTemplate(freemarkerContext,
				new File(project.getBaseDirectory(), ".project"),
				"ide/eclipse/project.ftl");
	}

	/**
	 * Write the classpath file.
	 * 
	 * @param freemarkerConfig
	 * @param freemarkerContext
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void writeClasspathFile(ActivityProject project,
			Map<String, Object> freemarkerContext) throws IOException,
			TemplateException {
		templater.writeTemplate(freemarkerContext,
				new File(project.getBaseDirectory(), ".classpath"),
				"ide/eclipse/java-classpath.ftl");
	}

	/**
	 * Write a a template out.
	 * 
	 * @param config
	 *            the freemarker config
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
	private static class JarFileFiler implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".jar");
		}

	}
}
