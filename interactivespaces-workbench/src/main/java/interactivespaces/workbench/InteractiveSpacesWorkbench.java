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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.system.BasicInteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectManager;
import interactivespaces.workbench.project.ProjectTaskContext;
import interactivespaces.workbench.project.ProjectTaskManager;
import interactivespaces.workbench.project.StandardProjectManager;
import interactivespaces.workbench.project.StandardProjectTaskManager;
import interactivespaces.workbench.project.activity.type.ProjectTypeRegistry;
import interactivespaces.workbench.project.activity.type.StandardProjectTypeRegistry;
import interactivespaces.workbench.project.creator.ProjectCreationContext;
import interactivespaces.workbench.project.creator.ProjectCreator;
import interactivespaces.workbench.project.creator.ProjectCreatorImpl;
import interactivespaces.workbench.project.group.GroupProjectTemplateSpecification;
import interactivespaces.workbench.project.java.BndOsgiContainerBundleCreator;
import interactivespaces.workbench.project.java.ContainerBundleCreator;
import interactivespaces.workbench.project.jdom.JdomProjectGroupTemplateSpecificationReader;
import interactivespaces.workbench.tasks.WorkbenchTaskContext;
import interactivespaces.workbench.ui.UserInterfaceFactory;
import interactivespaces.workbench.ui.editor.swing.PlainSwingUserInterfaceFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * A workbench for working with Interactive Spaces Activity development.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbench {

  /**
   * Command line flag for specifying a source directory.
   */
  public static final String COMMAND_LINE_FLAG_SOURCEDIR = "--sourcedir";

  /**
   * Command line flag for specifying a headers file.
   */
  public static final String COMMAND_LINE_FLAG_HEADERS = "--headers";

  /**
   * Command line flag for specifying an output file.
   */
  public static final String COMMAND_LINE_FLAG_OUTPUT = "--output";

  /**
   * The directory from where the workbench was invoked.
   */
  public static final String CONFIGURATION_PROPERTY_INTERACTIVESPACES_RUNDIR = "interactivespaces.rundir";

  /**
   * Configuration property defining the workbench home directory.
   */
  public static final String CONFIGURATION_PROPERTY_WORKBENCH_HOME = "workbench.home";

  /**
   * Command to recursively walk over a set of directories looking for the IS project folders.
   */
  public static final String COMMAND_RECURSIVE = "walk";

  /**
   * Command to create an OSGi bundle from an existing jar.
   */
  public static final String COMMAND_OSGI = "osgi";

  /**
   * Command to create a new project.
   */
  public static final String COMMAND_CREATE = "create";

  /**
   * Command to create a new project directly from a specification file.
   */
  public static final String COMMAND_CREATE_SPEC = "spec";

  /**
   * Command to deploy a project.
   */
  public static final String COMMAND_DEPLOY = "deploy";

  /**
   * Command to create an IDE project for an IS project.
   */
  public static final String COMMAND_IDE = "ide";

  /**
   * Command to create documentation from a project.
   */
  public static final String COMMAND_DOCS = "docs";

  /**
   * Command to clean a project.
   */
  public static final String COMMAND_CLEAN = "clean";

  /**
   * command to build a project.
   */
  public static final String COMMAND_BUILD = "build";

  /**
   * Configuration for the workbench.
   */
  private final Configuration workbenchConfig;

  /**
   * The activity project manager for file operations.
   */
  private final ProjectManager projectManager = new StandardProjectManager(this);

  /**
   * The creator for new projects.
   */
  private final ProjectCreator projectCreator;

  /**
   * The registry of project types.
   */
  private final ProjectTypeRegistry projectTypeRegistry;

  /**
   * The templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * File system for the workbench.
   */
  private final InteractiveSpacesFilesystem workbenchFileSystem = new BasicInteractiveSpacesFilesystem(new File(".")
      .getAbsoluteFile().getParentFile());

  /**
   * The user interface factory to be used by the workbench.
   */
  private final UserInterfaceFactory userInterfaceFactory = new PlainSwingUserInterfaceFactory();

  /**
   * File support for file operations.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * The base classloader to use for things like unit testing.
   */
  private final ClassLoader baseClassLoader;

  /**
   * Logger for the workbench.
   */
  private final Log log;

  /**
   * Manager for creating project tasks.
   */
  private ProjectTaskManager projectTaskManager;

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The directory that the workbench was initially run in.
   */
  private File runDirectory;

  /**
   * Construct a workbench.
   *
   * @param spaceEnvironment
   *          the space environment for the workbench
   * @param baseClassLoader
   *          the base classloader for the workbench
   */
  public InteractiveSpacesWorkbench(InteractiveSpacesEnvironment spaceEnvironment, ClassLoader baseClassLoader) {
    this.spaceEnvironment = spaceEnvironment;
    this.baseClassLoader = baseClassLoader;
    this.log = spaceEnvironment.getLog();

    workbenchConfig = spaceEnvironment.getSystemConfiguration();
    workbenchConfig.setValue(CONFIGURATION_PROPERTY_WORKBENCH_HOME, workbenchFileSystem.getInstallDirectory()
        .getAbsolutePath());

    String runDirPath = workbenchConfig.getPropertyString(CONFIGURATION_PROPERTY_INTERACTIVESPACES_RUNDIR);
    if (runDirPath != null) {
      runDirectory = new File(runDirPath);
    } else {
      // Go to whatever Java thinks our current directory is.
      runDirectory = new File(".").getAbsoluteFile();
    }

    this.templater = new FreemarkerTemplater(this);
    templater.startup();

    projectTypeRegistry = new StandardProjectTypeRegistry();
    projectCreator = new ProjectCreatorImpl(this, templater);
    projectTaskManager = new StandardProjectTaskManager(projectTypeRegistry, templater);
  }

  /**
   * Create an OSGi bundle from an existing JAR.
   *
   * @param args
   *          the args for the OSGi bundle
   */
  public void createOsgi(List<String> args) {
    WorkbenchTaskContext workbenchTaskContext = newWorkbenchTaskContext();

    if (args.isEmpty()) {
      throw new SimpleInteractiveSpacesException("No args given for OSGI command");
    }

    List<File> sources = Lists.newArrayList();
    File outputFile = null;
    File headersFile = null;
    for (int i = 0; i < args.size(); i++) {
      String arg = args.get(i);
      if (COMMAND_LINE_FLAG_OUTPUT.equals(arg)) {
        if (++i >= args.size()) {
          throw new SimpleInteractiveSpacesException("Missing output value from osgi command");
        }

        outputFile = fileSupport.resolveFile(runDirectory, args.get(i));
      } else if (COMMAND_LINE_FLAG_HEADERS.equals(arg)) {
        if (++i >= args.size()) {
          throw new SimpleInteractiveSpacesException("Missing headers value from osgi command");
        }

        headersFile = fileSupport.resolveFile(runDirectory, args.get(i));
      } else if (COMMAND_LINE_FLAG_SOURCEDIR.equals(arg)) {
        if (++i >= args.size()) {
          throw new SimpleInteractiveSpacesException("Missing source dir value from osgi command");
        }

        workbenchTaskContext.addJarFiles(fileSupport.resolveFile(runDirectory, args.get(i)), sources);
      } else {
        sources.add(fileSupport.resolveFile(runDirectory, arg));
      }
    }

    if (sources.isEmpty()) {
      throw new SimpleInteractiveSpacesException("No input jar files for the osgi command");
    }

    args.clear();

    log.info(String.format("Making %s into an OSGi bundle\n", sources));

    ContainerBundleCreator osgiBundleCreator =
        new BndOsgiContainerBundleCreator(workbenchFileSystem.getTempDirectory(), log);
    try {
      osgiBundleCreator.createBundle(sources, outputFile, headersFile, null);
    } catch (Exception e) {
      handleError("Error while creating OSGi bundle", e);
    }
  }

  /**
   * Perform a series of commands.
   *
   * @param commands
   *          the commands to run
   *
   * @return {@code true} if all commands ran successfully
   */
  public boolean doCommands(List<String> commands) {
    logWorkbenchCommand(commands);

    try {
      String command = removeArgument(commands, "command");

      if (COMMAND_CREATE.equals(command)) {
        createProject(commands);
      } else if (COMMAND_OSGI.equals(command)) {
        createOsgi(commands);
      } else {
        File baseDir = fileSupport.resolveFile(runDirectory, command);
        if (baseDir.isDirectory()) {
          if (projectManager.isProjectFolder(baseDir)) {
            return doCommandsOnProject(baseDir, commands);
          } else if (commands.isEmpty()) {
            log.warn(String.format("No commands to execute on the non-project directory %s", baseDir.getPath()));
          } else {
            String commandModifier = removeArgument(commands, "command modifier");
            if (COMMAND_RECURSIVE.equals(commandModifier)) {
              if (!doCommandsOnTree(baseDir, commands)) {
                log.error("Errors while processing project tree " + baseDir.getAbsolutePath());
                return false;
              }
            } else {
              log.error(String.format("Cannot run command %s on non-project directory %s", commandModifier,
                  baseDir.getPath()));
              return false;
            }
          }
        } else {
          log.error(String.format("%s is not a directory", baseDir.getAbsolutePath()));

          return false;
        }
      }

      if (!commands.isEmpty()) {
        log.warn("Extra command line arguments: " + commands);
      }

      return true;
    } catch (Throwable e) {
      if (e instanceof SimpleInteractiveSpacesException) {
        log.error(((SimpleInteractiveSpacesException) e).getCompoundMessage());
      } else {
        log.error("Error executing workbench commands", e);
      }

      return false;
    }
  }

  /**
   * Log all information about the workbench invocation.
   *
   * @param commands
   *          the command line for the workbench
   */
  private void logWorkbenchCommand(List<String> commands) {
    getLog().info(
        String.format("Using workbench %s",
            getSpaceEnvironment().getSystemConfiguration().getPropertyString(CONFIGURATION_PROPERTY_WORKBENCH_HOME)));
    getLog().info(String.format("Workbench commands: %s", Joiner.on(" ").join(commands)));
  }

  /**
   * Process a project create command.
   *
   * @param commands
   *          command input to inform project creation
   */
  private void createProject(List<String> commands) {
    getLog().info("Creating project from specification...");
    String specFilePath =
        determineTemplateSpec(removeArgument(commands, "specification project type"),
            removeArgument(commands, "specification project kind"));
    File specFile = fileSupport.resolveFile(runDirectory, specFilePath);
    String baseDirectoryPath = removeArgument(commands, "base output directory");
    File baseDirectory = fileSupport.resolveFile(runDirectory, baseDirectoryPath);

    JdomProjectGroupTemplateSpecificationReader projectReader = new JdomProjectGroupTemplateSpecificationReader(this);
    GroupProjectTemplateSpecification project = projectReader.readProjectGroupTemplateSpecification(specFile);

    ProjectCreationContext creationSpecification = new ProjectCreationContext(specFile.getAbsolutePath(), getLog());
    creationSpecification.setGroupProjectTemplateSpecification(project);
    creationSpecification.setWorkbenchTaskContext(newWorkbenchTaskContext());
    creationSpecification.setSpecificationBase(specFile.getParentFile());
    creationSpecification.setBaseDirectory(baseDirectory);

    try {
      projectCreator.create(creationSpecification);
      getLog().info("Successfully created project in " + baseDirectoryPath);
    } catch (Exception e) {
      handleError("Error while creating project " + creationSpecification.getDescription(), e);
    }
  }

  /**
   * Determine a template specification path from the input parameters.
   *
   * @param type
   *          project type (e.g., {@code activity} or {@code library})
   * @param kind
   *          project kind (e.g., {@code java} or {@code web})
   *
   * @return project specification path
   */
  private String determineTemplateSpec(String type, String kind) {
    if (COMMAND_CREATE_SPEC.equals(type)) {
      return kind;
    }
    String parameterName = String.format("interactivespaces.workbench.template.%s.%s", type, kind);
    String specPath = workbenchConfig.getPropertyString(parameterName);
    if (Strings.isNullOrEmpty(specPath)) {
      throw new SimpleInteractiveSpacesException("Missing spec path configuration " + parameterName);
    }
    return specPath;
  }

  /**
   * Do a series of workbench commands on a project directory.
   *
   * @param baseDir
   *          base directory of the project
   * @param commands
   *          the commands to be done
   *
   * @return {@code true} if there were no command errors
   */
  public boolean doCommandsOnProject(File baseDir, List<String> commands) {
    Project project = projectManager.readProject(baseDir, log);

    return doCommandsOnProject(project, commands);
  }

  /**
   * Walk over a set of folders looking for project files to build.
   *
   * @param baseDir
   *          base file to start looking for projects in
   * @param commands
   *          commands to run on all project files
   *
   * @return {@code true} if the tree was walked successfully
   */
  private boolean doCommandsOnTree(File baseDir, List<String> commands) {
    FileFilter filter = new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    };
    File[] files = baseDir.listFiles(filter);
    boolean success = true;
    if (files != null) {
      for (File possible : files) {
        success &= doCommandsOnTree(possible, commands, filter);
      }
    }

    // Walking the tree implicitly consumes all the commands, so clear them out
    // here.
    commands.clear();

    return success;
  }

  /**
   * Walk over a set of folders looking for project files to build.
   *
   * @param baseDir
   *          base folder which may be a project folder or may contain project folders
   * @param commands
   *          commands to run on all project files
   * @param filter
   *          file filter to identify which files in the tree to execute on
   *
   * @return {@code true} if the tree was walked successfully
   */
  private boolean doCommandsOnTree(File baseDir, List<String> commands, FileFilter filter) {
    if (projectManager.isProjectFolder(baseDir)) {
      try {
        return doCommandsOnProject(baseDir, Lists.newArrayList(commands));
      } catch (Throwable e) {
        getLog().error("Error encountered performing commands on project", e);
        return false;
      }
    } else {
      boolean success = true;

      File[] files = baseDir.listFiles(filter);
      if (files != null) {
        for (File possible : files) {
          success &= doCommandsOnTree(possible, commands, filter);
        }
      }
      return success;
    }
  }

  /**
   * Remove one argument from the list.
   *
   * @param commands
   *          list of input commands
   * @param description
   *          description to use on error
   *
   * @return command string removed from the list
   */
  private String removeArgument(List<String> commands, String description) {
    if (commands.isEmpty()) {
      throw new SimpleInteractiveSpacesException("Missing argument " + description);
    }
    return commands.remove(0);
  }

  /**
   * Perform a sequence of commands on a project.
   *
   * @param project
   *          the project being acted on
   * @param commands
   *          the commands to perform on the project
   *
   * @return {@code true} if there were no command errors
   */
  public boolean doCommandsOnProject(Project project, List<String> commands) {
    // Make build the default command on a project directory.
    if (commands.isEmpty()) {
      commands.add(COMMAND_BUILD);
    }

    WorkbenchTaskContext workbenchTaskContext = newWorkbenchTaskContext();
    ProjectTaskContext projectTaskContext = projectTaskManager.newProjectTaskContext(project, workbenchTaskContext);

    while (!commands.isEmpty()) {
      String command = removeArgument(commands, "workbench command");

      if (COMMAND_BUILD.equals(command)) {
        addProjectBuildTasks(project, projectTaskContext, workbenchTaskContext);
      } else if (COMMAND_CLEAN.equals(command)) {
        addProjectCleanTasks(project, projectTaskContext, workbenchTaskContext);
      } else if (COMMAND_DOCS.equals(command)) {
        addProjectDocsTasks(project, projectTaskContext, workbenchTaskContext);
      } else if (COMMAND_IDE.equals(command)) {
        addProjectIdeTasks(commands, project, projectTaskContext, workbenchTaskContext);
      } else if (COMMAND_DEPLOY.equals(command)) {
        addProjectDeployTasks(commands, project, projectTaskContext, workbenchTaskContext);
      }
    }

    workbenchTaskContext.doTasks();

    return !workbenchTaskContext.hasErrors();
  }

  /**
   * Add tasks for building a project.
   *
   * @param project
   *          the project to be built
   * @param projectTaskContext
   *          context for project tasks
   * @param workbenchTaskContext
   *          context for workbench tasks
   */
  public void addProjectBuildTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    projectTaskManager.addBuildTasks(project, projectTaskContext, workbenchTaskContext);
  }

  /**
   * Clean a project.
   *
   * @param project
   *          the project to be cleaned
   * @param projectTaskContext
   *          context for project tasks
   * @param workbenchTaskContext
   *          context for workbench tasks
   */
  public void addProjectCleanTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    projectTaskManager.addCleanTasks(project, projectTaskContext, workbenchTaskContext);
  }

  /**
   * Generate an IDE project for the project.
   *
   * @param commands
   *          a partially consumed list of commands
   * @param project
   *          the activity project to generate the IDE project for
   * @param projectTaskContext
   *          context for project tasks
   * @param workbenchTaskContext
   *          context for workbench tasks
   */
  public void addProjectIdeTasks(List<String> commands, Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    String ide = removeArgument(commands, "ide type");
    projectTaskManager.addIdeProjectTasks(project, ide, projectTaskContext, workbenchTaskContext);
  }

  /**
   * Deploy a project.
   *
   * @param commands
   *          a partially consumed list of commands
   * @param project
   *          the activity project to generate the IDE project for
   * @param projectTaskContext
   *          context for project tasks
   * @param workbenchTaskContext
   *          context for workbench tasks
   */
  public void addProjectDeployTasks(List<String> commands, Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    String deploymentType = removeArgument(commands, "deployment type");
    projectTaskManager.addDeploymentTasks(deploymentType, project, projectTaskContext, workbenchTaskContext);
  }

  /**
   * Generate the docs for a project.
   *
   * @param project
   *          the project
   * @param projectTaskContext
   *          context for project tasks
   * @param workbenchTaskContext
   *          context for workbench tasks
   */
  public void addProjectDocsTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    projectTaskManager.addDocsTasks(project, projectTaskContext, workbenchTaskContext);
  }

  /**
   * Create a new context for workbench tasks.
   *
   * @return a context for workbench tasks
   */
  public WorkbenchTaskContext newWorkbenchTaskContext() {
    return new WorkbenchTaskContext(this, workbenchConfig);
  }

  /**
   * Get the workbench file system.
   *
   * @return the workbench file system
   */
  public InteractiveSpacesFilesystem getWorkbenchFileSystem() {
    return workbenchFileSystem;
  }

  /**
   * @return the projectManager
   */
  public ProjectManager getProjectManager() {
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
  public ProjectCreator getProjectCreator() {
    return projectCreator;
  }

  /**
   * Get the workbench configuration.
   *
   * @return the workbench configuration
   */
  public Configuration getWorkbenchConfig() {
    return workbenchConfig;
  }

  /**
   * @return the templater
   */
  public FreemarkerTemplater getTemplater() {
    return templater;
  }

  /**
   * Get the project type registry.
   *
   * @return the project type registry
   */
  public ProjectTypeRegistry getProjectTypeRegistry() {
    return projectTypeRegistry;
  }

  /**
   * Log an error.
   *
   * @param message
   *          any message for the error
   * @param e
   *          the exception
   */
  public void handleError(String message, Throwable e) {
    throw new SimpleInteractiveSpacesException(message, e);
  }

  /**
   * Get the run directory for the workbench.
   *
   * @return the run directory
   */
  public File getRunDirectory() {
    return runDirectory;
  }

  /**
   * Get the base classloader for the system.
   *
   * @return the base classloader for the system
   */
  public ClassLoader getBaseClassLoader() {
    return baseClassLoader;
  }

  /**
   * Get the workbench logger.
   *
   * @return the workbench logger
   */
  public Log getLog() {
    return log;
  }

  /**
   * Get the space environment for the workbench.
   *
   * @return the space environment
   */
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }
}
