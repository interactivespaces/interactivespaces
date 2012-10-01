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
import interactivespaces.workbench.activity.project.builder.ActivityBuilderFactory;
import interactivespaces.workbench.activity.project.builder.SimpleActivityBuilderFactory;
import interactivespaces.workbench.activity.project.creator.ActivityProjectCreator;
import interactivespaces.workbench.activity.project.creator.ActivityProjectCreatorImpl;
import interactivespaces.workbench.activity.project.ide.EclipseIdeProjectCreator;
import interactivespaces.workbench.activity.project.packager.ActivityProjectPackager;
import interactivespaces.workbench.activity.project.packager.ActivityProjectPackagerImpl;
import interactivespaces.workbench.ui.UserInterfaceFactory;
import interactivespaces.workbench.ui.editor.swing.PlainSwingUserInterfaceFactory;

import java.io.Console;
import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * A workbench for working with Interactive Spaces Activity development.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbench {

	/**
	 * Configuration property giving the location of the controller the
	 * workbench is using.
	 */
	private static final String CONFIGURATION_CONTROLLER_BASEDIR = "interactivespaces.controller.basedir";

	/**
	 * Configuration property giving the location of the master the workbench is
	 * using.
	 */
	private static final String CONFIGURATION_MASTER_BASEDIR = "interactivespaces.master.basedir";

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
	 * The builder factory.
	 */
	private ActivityBuilderFactory activityBuilderFactory;
	
	/**
	 * The IDE project creator.
	 */
	private EclipseIdeProjectCreator ideProjectCreator;
	
	/**
	 * The templater to use.
	 */
	private FreemarkerTemplater templater;
	
	/**
	 * The user interface factory to be used by the workbench.
	 */
	private UserInterfaceFactory userInterfaceFactory = new PlainSwingUserInterfaceFactory();

	public InteractiveSpacesWorkbench(Properties workbenchProperties) {
		this.workbenchProperties = workbenchProperties;
		
		this.templater = new FreemarkerTemplater();
		templater.startup();

		activityProjectCreator = new ActivityProjectCreatorImpl(this, templater);
		activityProjectPackager = new ActivityProjectPackagerImpl();
		activityBuilderFactory = new SimpleActivityBuilderFactory();
		ideProjectCreator = new EclipseIdeProjectCreator(templater);
	}

	/**
	 * Build a project.
	 * 
	 * @param project
	 */
	public void buildActivityProject(ActivityProject project) {
		ActivityProjectBuildContext context = new ActivityProjectBuildContext(
				project, this);

		ActivityBuilder builder = activityBuilderFactory.newBuilder(project);
		builder.build(project, context);
		activityProjectPackager.packageActivityProject(project, context);
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
		ideProjectCreator.createProject(project, this);
	}

	/**
	 * Get the bootstrap directory for the controller.
	 * 
	 * @return the bootstrap directory for the controller
	 */
	public File getControllerBootstrapDir() {
		return new File(new File(
				workbenchProperties
						.getProperty(CONFIGURATION_CONTROLLER_BASEDIR)),
				"bootstrap");
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
			ActivityProject project = activityProjectManager.readActivityProject(new File(command));
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
}
