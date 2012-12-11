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

package interactivespaces.workbench;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityIdentifyingNameValidator;
import interactivespaces.domain.support.ActivityVersionValidator;
import interactivespaces.domain.support.DomainValidationResult;
import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.domain.support.Validator;
import interactivespaces.workbench.activity.project.ActivityProject;
import interactivespaces.workbench.activity.project.ActivityProjectBuildContext;
import interactivespaces.workbench.activity.project.ActivityProjectCreationSpecification;
import interactivespaces.workbench.activity.project.ActivityProjectManager;
import interactivespaces.workbench.activity.project.BasicActivityProjectManager;
import interactivespaces.workbench.activity.project.builder.ActivityBuilder;
import interactivespaces.workbench.activity.project.creator.ActivityProjectCreator;
import interactivespaces.workbench.activity.project.creator.ActivityProjectCreatorImpl;
import interactivespaces.workbench.activity.project.ide.EclipseIdeProjectCreator;
import interactivespaces.workbench.activity.project.ide.EclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.activity.project.ide.NonJavaEclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.activity.project.packager.ActivityProjectPackager;
import interactivespaces.workbench.activity.project.packager.ActivityProjectPackagerImpl;
import interactivespaces.workbench.activity.project.type.ActivityProjectType;
import interactivespaces.workbench.activity.project.type.ActivityProjectTypeRegistry;
import interactivespaces.workbench.activity.project.type.SimpleActivityProjectTypeRegistery;
import interactivespaces.workbench.ui.UserInterfaceFactory;
import interactivespaces.workbench.ui.editor.swing.PlainSwingUserInterfaceFactory;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

/**
 * A workbench for working with Interactive Spaces Activity development.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbench {

	private static final String FILENAME_JAR_EXTENSION = ".jar";

	/**
	 * The file extension used for files which give container extensions.
	 */
	public static final String EXTENSION_FILE_EXTENSION = ".ext";

	/**
	 * The keyword header for a package line on an extensions file.
	 */
	public static final String EXTENSION_FILE_PATH_KEYWORD = "path:";

	/**
	 * The length of the keyword header for a package line on an extensions
	 * file.
	 */
	public static final int EXTENSION_FILE_PATH_KEYWORD_LENGTH = EXTENSION_FILE_PATH_KEYWORD
			.length();

	/**
	 * Configuration property giving the location of the controller the
	 * workbench is using.
	 */
	public static final String CONFIGURATION_CONTROLLER_BASEDIR = "interactivespaces.controller.basedir";

	/**
	 * Configuration property giving the location of the master the workbench is
	 * using.
	 */
	public static final String CONFIGURATION_MASTER_BASEDIR = "interactivespaces.master.basedir";

	/**
	 * Properties for the workbench.
	 */
	private Properties workbenchProperties;

	/**
	 * The activity project manager for file operations.
	 */
	private ActivityProjectManager activityProjectManager = new BasicActivityProjectManager();

	/**
	 * The creator for new projects.
	 */
	private ActivityProjectCreator activityProjectCreator;

	/**
	 * A packager for activities.
	 */
	private ActivityProjectPackager activityProjectPackager;

	/**
	 * The registry of activity project types.
	 */
	private ActivityProjectTypeRegistry activityProjectTypeRegistry;

	/**
	 * The IDE project creator.
	 */
	private EclipseIdeProjectCreator ideProjectCreator;

	/**
	 * The templater to use.
	 */
	private FreemarkerTemplater templater;

	/**
	 * Base directory the workbench is installed in.
	 */
	private File workbenchBaseDir = new File(".").getAbsoluteFile();

	/**
	 * The user interface factory to be used by the workbench.
	 */
	private UserInterfaceFactory userInterfaceFactory = new PlainSwingUserInterfaceFactory();

	public InteractiveSpacesWorkbench(Properties workbenchProperties) {
		this.workbenchProperties = workbenchProperties;

		this.templater = new FreemarkerTemplater();
		templater.startup();

		activityProjectTypeRegistry = new SimpleActivityProjectTypeRegistery();
		activityProjectCreator = new ActivityProjectCreatorImpl(this, templater);
		activityProjectPackager = new ActivityProjectPackagerImpl();
		ideProjectCreator = new EclipseIdeProjectCreator(templater);
	}

	/**
	 * Build a project.
	 * 
	 * @param project
	 *            the project to be built
	 */
	public void buildActivityProject(ActivityProject project) {
		ActivityProjectBuildContext context = new ActivityProjectBuildContext(
				project, this);

		// If no type, there is nothing special to do for building.
		ActivityProjectType type = getActivityProjectType(project);
		if (type != null) {
			ActivityBuilder builder = type.newBuilder();
			builder.build(project, context);
		}

		activityProjectPackager.packageActivityProject(project, context);
	}

	/**
	 * Get the project type for the project.
	 * 
	 * @param project
	 *            the activity project
	 * 
	 * @return the type of the project, if set, or {@code null} if none
	 *         specified
	 * 
	 * @throws InteractiveSpacesException
	 *             if no unknown type
	 */
	private ActivityProjectType getActivityProjectType(ActivityProject project) {
		String name = project.getActivityDescription().getBuilderType();
		if (name != null) {
			ActivityProjectType type = activityProjectTypeRegistry
					.getActivityProjectType(name);
			if (type != null) {
				return type;
			} else {
				throw new InteractiveSpacesException(String.format(
						"No builder found for type %s", name));
			}
		} else {
			return null;
		}
	}

	/**
	 * Generate an IDE project for the project.
	 * 
	 * @param project
	 *            the activity project to generate the IDE project for
	 * @param ide
	 *            the name of the IDE to generate the project for
	 */
	public void generateIdeActivityProject(ActivityProject project, String ide) {
		EclipseIdeProjectCreatorSpecification spec;
		ActivityProjectType type = getActivityProjectType(project);
		if (type != null) {
			spec = type.getEclipseIdeProjectCreatorSpecification();
		} else {
			spec = new NonJavaEclipseIdeProjectCreatorSpecification();
		}

		ideProjectCreator.createProject(project, spec, this);
	}

	/**
	 * Get a list of all files on the controller's classpath.
	 * 
	 * @return
	 */
	public List<File> getControllerClasspath() {
		List<File> classpath = Lists.newArrayList();

		File[] files = getControllerBootstrapDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(FILENAME_JAR_EXTENSION);
			}
		});
		if (files != null) {
			for (File file : files) {
				classpath.add(file);
			}
		}

		File javaSystemDirectory = new File(new File(
				workbenchProperties
						.getProperty(CONFIGURATION_CONTROLLER_BASEDIR)),
				"lib/system/java");
		classpath.add(new File(javaSystemDirectory,
				"com.springsource.org.apache.commons.logging-1.1.1.jar"));

		addControllerExtensionsClasspath(classpath);
		addAlternateControllerExtensionsClasspath(classpath);

		return classpath;
	}

	/**
	 * Get the bootstrap directory for the controller.
	 * 
	 * @return the bootstrap directory for the controller
	 */
	private File getControllerBootstrapDir() {
		return new File(new File(
				workbenchProperties
						.getProperty(CONFIGURATION_CONTROLLER_BASEDIR)),
				"bootstrap");
	}

	/**
	 * Add all extension classpath entries that the controller specifies.
	 * 
	 * @param files
	 *            the list of files to add to.
	 */
	private void addControllerExtensionsClasspath(List<File> files) {
		File[] extensionFiles = new File(new File(
				workbenchProperties
						.getProperty(CONFIGURATION_CONTROLLER_BASEDIR)),
				"lib/system/java").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(EXTENSION_FILE_EXTENSION);
			}
		});
		if (extensionFiles == null)
			return;

		for (File extensionFile : extensionFiles) {
			processExtensionFile(files, extensionFile);
		}

	}

	/**
	 * Add all extension classpath entries that the controller specifies.
	 * 
	 * @param files
	 *            the list of files to add to.
	 */
	private void addAlternateControllerExtensionsClasspath(List<File> files) {
		File[] alternateFiles = new File(workbenchBaseDir, "alternate")
				.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(FILENAME_JAR_EXTENSION);
					}
				});
		if (alternateFiles == null)
			return;

		for (File alternateFile : alternateFiles) {
			files.add(alternateFile);
		}

	}

	/**
	 * process an extension file.
	 * 
	 * @param files
	 *            the collection of jars described in the extension files
	 * 
	 * @param extensionFile
	 *            the extension file to process
	 */
	private void processExtensionFile(List<File> files, File extensionFile) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(extensionFile));

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					int pos = line.indexOf(EXTENSION_FILE_PATH_KEYWORD);
					if (pos == 0
							&& line.length() > EXTENSION_FILE_PATH_KEYWORD_LENGTH) {
						files.add(new File(line
								.substring(EXTENSION_FILE_PATH_KEYWORD_LENGTH)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// Don't care.
				}
			}
		}
	}

	/**
	 * Perform a series of commands.
	 * 
	 * @param commands
	 */
	public void doCommands(List<String> commands) {
		String command = commands.get(0);
		commands.remove(0);

		if ("create".equals(command)) {
			System.out.println("Creating project");
			createProject(commands);
		} else {
			ActivityProject project = activityProjectManager
					.readActivityProject(new File(command));
			doCommandsOnProject(project, commands);
		}
	}

	/**
	 * Create a project.
	 * 
	 * @param commands
	 *            the commands to execute
	 */
	private void createProject(List<String> commands) {
		ActivityProjectCreationSpecification spec = new ActivityProjectCreationSpecification();

		ActivityDescription activity = getActivityFromConsole();

		ActivityProject project = new ActivityProject(activity);
		project.setBaseDirectory(new File(activity.getIdentifyingName()));

		spec.setProject(project);

		String command = commands.remove(0);
		if ("language".equals(command)) {
			spec.setLanguage(commands.remove(0));
		} else if ("template".equals(command)) {
			String source = commands.remove(0);
			if ("example".equals(source)) {
				System.out.println("Not implemented yet");
				return;
			} else if ("site".equals(source)) {
				System.out.println("Not implemented yet");
				return;
			}
		}

		activityProjectCreator.createProject(spec);
	}

	/**
	 * Get the activity data from the console.
	 * 
	 * @return the activity data input from the console
	 */
	private ActivityDescription getActivityFromConsole() {
		Console console = System.console();

		if (console != null) {
			String identifyingName = getValue("Identifying name",
					new ActivityIdentifyingNameValidator(), console);
			String version = getValue("Version",
					new ActivityVersionValidator(), console);
			String name = console.readLine("Name: ");
			String description = console.readLine("Description: ");

			ActivityDescription activity = new ActivityDescription();
			activity.setIdentifyingName(identifyingName);
			activity.setVersion(version);
			activity.setName(name);
			activity.setDescription(description);

			return activity;
		} else {
			throw new InteractiveSpacesException("Could not allocate console");
		}
	}

	/**
	 * Get a value from the user.
	 * 
	 * @param prompt
	 *            the prompt for the user
	 * @param validator
	 *            the validator for the value
	 * @param console
	 *            the console for IO
	 * 
	 * @return a valid value for the prquestion
	 */
	private String getValue(String prompt, Validator validator, Console console) {
		String fullPrompt = prompt + ": ";

		String value = console.readLine(fullPrompt);
		DomainValidationResult result = validator.validate(value);
		while (result.getResultType().equals(DomainValidationResultType.ERRORS)) {
			console.printf(result.getDescription());
			value = console.readLine(fullPrompt);
		}

		return value;
	}

	/**
	 * Perform a sequence of commands on a project.
	 * 
	 * @param project
	 *            the project being acted on
	 * @param commands
	 *            the commands to perform on the project
	 */
	private void doCommandsOnProject(ActivityProject project,
			List<String> commands) {
		if (commands.isEmpty()) {
			commands.add("build");
		}

		while (!commands.isEmpty()) {
			String command = commands.remove(0);

			if ("build".equals(command)) {
				System.out.println("Building project");
				buildActivityProject(project);
			} else if ("ide".equals(command)) {
				System.out.println("Building project IDE project");
				generateIdeActivityProject(project, commands.remove(0));
			}
		}
	}

	/**
	 * @return the activityProjectManager
	 */
	public ActivityProjectManager getActivityProjectManager() {
		return activityProjectManager;
	}

	/**
	 * @return the userInterfaceFactory
	 */
	public UserInterfaceFactory getUserInterfaceFactory() {
		return userInterfaceFactory;
	}

	/**
	 * @return the activityProjectCreator
	 */
	public ActivityProjectCreator getActivityProjectCreator() {
		return activityProjectCreator;
	}

	/**
	 * @return the workbenchProperties
	 */
	public Properties getWorkbenchProperties() {
		return workbenchProperties;
	}
}
