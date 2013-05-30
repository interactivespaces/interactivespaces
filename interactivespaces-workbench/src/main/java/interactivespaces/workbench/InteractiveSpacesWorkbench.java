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
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.domain.support.ActivityIdentifyingNameValidator;
import interactivespaces.domain.support.ActivityVersionValidator;
import interactivespaces.domain.support.DomainValidationResult;
import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.domain.support.Validator;
import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ProjectBuildContext;
import interactivespaces.workbench.project.activity.ActivityProjectManager;
import interactivespaces.workbench.project.activity.BasicActivityProjectManager;
import interactivespaces.workbench.project.activity.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import interactivespaces.workbench.project.activity.builder.ProjectBuilder;
import interactivespaces.workbench.project.activity.creator.ProjectCreator;
import interactivespaces.workbench.project.activity.creator.ProjectCreatorImpl;
import interactivespaces.workbench.project.activity.ide.EclipseIdeProjectCreator;
import interactivespaces.workbench.project.activity.ide.EclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.activity.ide.NonJavaEclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.activity.packager.ActivityProjectPackager;
import interactivespaces.workbench.project.activity.packager.ActivityProjectPackagerImpl;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.activity.type.ProjectTypeRegistry;
import interactivespaces.workbench.project.activity.type.SimpleProjectTypeRegistery;
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
import java.util.Map;

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
	private Map<String, String> workbenchConfig;

	/**
	 * Configuration for the workbench.
	 */
	private SimpleConfiguration workbenchSimpleConfig;

	/**
	 * The activity project manager for file operations.
	 */
	private ActivityProjectManager projectManager = new BasicActivityProjectManager();

	/**
	 * The creator for new projects.
	 */
	private ProjectCreator activityProjectCreator;

	/**
	 * A packager for activities.
	 */
	private ActivityProjectPackager activityProjectPackager;

	/**
	 * The registry of activity project types.
	 */
	private ProjectTypeRegistry projectTypeRegistry;

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

	public InteractiveSpacesWorkbench(Map<String, String> workbenchConfig) {
		this.workbenchConfig = workbenchConfig;
		workbenchSimpleConfig = SimpleConfiguration.newConfiguration();

		for (Map.Entry<String, String> entry : workbenchConfig.entrySet()) {
			workbenchSimpleConfig.setValue(entry.getKey(), entry.getValue());
		}

		this.templater = new FreemarkerTemplater();
		templater.startup();

		projectTypeRegistry = new SimpleProjectTypeRegistery();
		activityProjectCreator = new ProjectCreatorImpl(this, templater);
		activityProjectPackager = new ActivityProjectPackagerImpl();
		ideProjectCreator = new EclipseIdeProjectCreator(templater);
	}

	/**
	 * Build a project.
	 * 
	 * @param project
	 *            the project to be built
	 */
	public void buildProject(Project project) {
		ProjectBuildContext context = new ProjectBuildContext(project, this);

		// If no type, there is nothing special to do for building.
		ProjectType type = projectTypeRegistry.getProjectType(project);
		ProjectBuilder builder = null;
		if (type != null) {
			builder = type.newBuilder();
		} else {
			builder = new BaseActivityProjectBuilder();
		}

		builder.build(project, context);

		if ("activity".equals(project.getType())) {
			activityProjectPackager.packageActivityProject(project, context);
		}
	}

	/**
	 * Clean a project.
	 * 
	 * @param project
	 *            the project to be built
	 */
	public void cleanActivityProject(Project project) {
		ProjectBuildContext context = new ProjectBuildContext(project, this);

		File buildDirectory = context.getBuildDirectory();

		if (buildDirectory.exists()) {
			Files.deleteDirectoryContents(buildDirectory);
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
	public void generateIdeActivityProject(Project project, String ide) {
		EclipseIdeProjectCreatorSpecification spec;
		ProjectType type = projectTypeRegistry.getProjectType(project);
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
				workbenchConfig.get(CONFIGURATION_CONTROLLER_BASEDIR)),
				"lib/system/java");
		classpath.add(new File(javaSystemDirectory,
				"com.springsource.org.apache.commons.logging-1.1.1.jar"));

		addControllerExtensionsClasspath(classpath);

		return classpath;
	}

	/**
	 * Get the bootstrap directory for the controller.
	 * 
	 * @return the bootstrap directory for the controller
	 */
	private File getControllerBootstrapDir() {
		return new File(new File(
				workbenchConfig.get(CONFIGURATION_CONTROLLER_BASEDIR)),
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
				workbenchConfig.get(CONFIGURATION_CONTROLLER_BASEDIR)),
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
	 * @param alternate
	 *            the alternate files to add
	 */
	public void addAlternateControllerExtensionsClasspath(List<File> files,
			String alternate) {
		File[] alternateFiles = new File(
				new File(workbenchBaseDir, "alternate"), alternate)
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
		String command = commands.remove(0);

		if ("create".equals(command)) {
			System.out.println("Creating project");
			createProject(commands);
		} else {
			File baseDir = new File(command);
			if (projectManager.isProjectFolder(baseDir)) {
				doCommandsOnProject(baseDir, commands);
			} else {
				if (!commands.isEmpty() && "walk".equals(commands.get(0))) {
					commands.remove(0);

					doCommandsOnTree(baseDir, commands);
				} else {
					System.out.format("%s is not a project directory\n",
							baseDir.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Do a series of workbench commands on a project directory
	 * 
	 * @param baseDir
	 *            base directory of the project
	 * @param commands
	 *            the commands to be done
	 */
	public void doCommandsOnProject(File baseDir, List<String> commands) {
		Project project = projectManager.readProject(baseDir);
		doCommandsOnProject(project, commands);
	}

	/**
	 * Walk over a set of folders looking for project files to build
	 * 
	 * @param baseDir
	 *            base file to start looking for projects in
	 * 
	 * @param commands
	 *            commands to run on all project files
	 */
	private void doCommandsOnTree(File baseDir, List<String> commands) {
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		File[] files = baseDir.listFiles(filter);
		if (files != null) {
			for (File possible : files) {
				doCommandsOnTree(possible, commands, filter);
			}
		}
	}

	/**
	 * Walk over a set of folders looking for project files to build
	 * 
	 * @param baseDir
	 *            base folder which may be a project folder or may contain
	 *            project folders
	 * @param commands
	 *            commands to run on all project files
	 */
	private void doCommandsOnTree(File baseDir, List<String> commands,
			FileFilter filter) {
		if (projectManager.isProjectFolder(baseDir)) {
			doCommandsOnProject(baseDir, Lists.newArrayList(commands));
		} else {
			File[] files = baseDir.listFiles(filter);
			if (files != null) {
				for (File possible : files) {
					doCommandsOnTree(possible, commands, filter);
				}
			}
		}
	}

	/**
	 * Create a project.
	 * 
	 * @param commands
	 *            the commands to execute
	 */
	private void createProject(List<String> commands) {
		ProjectCreationSpecification spec = new ProjectCreationSpecification();

		Project project = getProjectFromConsole();
		project.setBaseDirectory(new File(project.getIdentifyingName()));

		spec.setProject(project);

		String projectType = "activity";

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
		} else if ("type".equals(command)) {
			projectType = commands.remove(0);
		}

		project.setType(projectType);

		activityProjectCreator.createProject(spec);
	}

	/**
	 * Get the activity data from the console.
	 * 
	 * @return the activity data input from the console
	 */
	private Project getProjectFromConsole() {
		Console console = System.console();

		if (console != null) {
			String identifyingName = getValue("Identifying name",
					new ActivityIdentifyingNameValidator(), console);
			String version = getValue("Version",
					new ActivityVersionValidator(), console);
			String name = console.readLine("Name: ");
			String description = console.readLine("Description: ");

			Project project = new Project();
			project.setIdentifyingName(identifyingName);
			project.setVersion(version);
			project.setName(name);
			project.setDescription(description);

			return project;
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
	private void doCommandsOnProject(Project project, List<String> commands) {
		if (commands.isEmpty()) {
			commands.add("build");
		}

		while (!commands.isEmpty()) {
			String command = commands.remove(0);

			if ("build".equals(command)) {
				System.out.format("Building project %s\n", project
						.getBaseDirectory().getAbsolutePath());
				buildProject(project);
			} else if ("clean".equals(command)) {
				System.out.format("Cleaning project %s\n", project
						.getBaseDirectory().getAbsolutePath());
				cleanActivityProject(project);
			} else if ("ide".equals(command)) {
				System.out.format("Building project IDE project %s\n", project
						.getBaseDirectory().getAbsolutePath());
				generateIdeActivityProject(project, commands.remove(0));
			}
		}
	}

	/**
	 * @return the projectManager
	 */
	public ActivityProjectManager getProjectManager() {
		return projectManager;
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
	public ProjectCreator getActivityProjectCreator() {
		return activityProjectCreator;
	}

	/**
	 * @return the workbenchProperties
	 */
	public SimpleConfiguration getWorkbenchConfig() {
		return workbenchSimpleConfig;
	}

	/**
	 * @return the templater
	 */
	public FreemarkerTemplater getTemplater() {
		return templater;
	}
}
