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
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.domain.support.ActivityIdentifyingNameValidator;
import interactivespaces.domain.support.DomainValidationResult;
import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.domain.support.Validator;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionValidator;
import interactivespaces.system.BasicInteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.ProjectDeployment;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.ActivityProjectManager;
import interactivespaces.workbench.project.activity.BasicActivityProjectManager;
import interactivespaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
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
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.builder.ProjectBuilder;
import interactivespaces.workbench.project.java.BndOsgiBundleCreator;
import interactivespaces.workbench.project.java.ExternalJavadocGenerator;
import interactivespaces.workbench.project.java.JavadocGenerator;
import interactivespaces.workbench.project.java.OsgiBundleCreator;
import interactivespaces.workbench.ui.UserInterfaceFactory;
import interactivespaces.workbench.ui.editor.swing.PlainSwingUserInterfaceFactory;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import org.apache.commons.logging.Log;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

/**
 * A workbench for working with Interactive Spaces Activity development.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbench {

  /**
   * Configuration property defining the project home directory.
   */
  public static final String CONFIGURATION_PROPERTY_WORKBENCH_HOME = "workbench.home";

  /**
   * Command to recursively walk over a set of directories looking for the IS
   * project folders.
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
   * File extension for a Java jar file.
   */
  public static final String FILENAME_JAR_EXTENSION = ".jar";

  /**
   * The file extension used for files which give container extensions.
   */
  public static final String EXTENSION_FILE_EXTENSION = ".ext";

  /**
   * The keyword header for a package line on an extensions file.
   */
  public static final String EXTENSION_FILE_PATH_KEYWORD = "path:";

  /**
   * The length of the keyword header for a package line on an extensions file.
   */
  public static final int EXTENSION_FILE_PATH_KEYWORD_LENGTH = EXTENSION_FILE_PATH_KEYWORD.length();

  /**
   * Configuration property giving the location of the controller the workbench
   * is using.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_HOME = "interactivespaces.home";

  /**
   * Configuration property giving the location of the controller the workbench
   * is using.
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
  private final Map<String, String> workbenchConfig;

  /**
   * Configuration for the workbench.
   */
  private final SimpleConfiguration workbenchSimpleConfig;

  /**
   * The activity project manager for file operations.
   */
  private final ActivityProjectManager projectManager = new BasicActivityProjectManager(this);

  /**
   * The creator for new projects.
   */
  private final ProjectCreator activityProjectCreator;

  /**
   * A packager for activities.
   */
  private final ActivityProjectPackager activityProjectPackager;

  /**
   * The registry of activity project types.
   */
  private final ProjectTypeRegistry projectTypeRegistry;

  /**
   * The IDE project creator.
   */
  private final EclipseIdeProjectCreator ideProjectCreator;

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
   * Logger for the workbench.
   */
  private final Log log;

  /**
   * Construct a workbench.
   *
   * @param workbenchConfig
   *          the configuration for the workbench
   * @param log
   *          the logger to use
   */
  public InteractiveSpacesWorkbench(Map<String, String> workbenchConfig, Log log) {
    this.workbenchConfig = workbenchConfig;
    this.log = log;

    workbenchSimpleConfig = SimpleConfiguration.newConfiguration();

    workbenchSimpleConfig.setValues(workbenchConfig);
    workbenchSimpleConfig.setValue(CONFIGURATION_PROPERTY_WORKBENCH_HOME, workbenchFileSystem.getInstallDirectory()
        .getAbsolutePath());

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
   *          the project to be built
   *
   * @return {@code true} if properly built project
   */
  public boolean buildProject(Project project) {
    // If no type, there is nothing special to do for building.
    ProjectType type = projectTypeRegistry.getProjectType(project);
    ProjectBuilder builder = null;
    if (type != null) {
      builder = type.newBuilder();
    } else {
      builder = new BaseActivityProjectBuilder();
    }

    ProjectBuildContext context = new ProjectBuildContext(type, project, this);

    try {
      if (builder.build(project, context)) {
        if (ActivityProject.PROJECT_TYPE_NAME.equals(project.getType())) {
          activityProjectPackager.packageActivityProject(project, context);
        }
        return true;
      }
    } catch (Exception e) {
      logError("Error while creating project", e);
    }

    return false;
  }

  /**
   * Clean a project.
   *
   * @param project
   *          the project to be built
   *
   * @return {@code true} if the project was cleaned properly
   */
  public boolean cleanActivityProject(Project project) {
    ProjectBuildContext context = new ProjectBuildContext(null, project, this);

    File buildDirectory = context.getBuildDirectory();

    if (buildDirectory.exists()) {
      fileSupport.deleteDirectoryContents(buildDirectory);
    }

    return true;
  }

  /**
   * Generate an IDE project for the project.
   *
   * @param project
   *          the activity project to generate the IDE project for
   * @param ide
   *          the name of the IDE to generate the project for
   *
   *
   * @return {@code true} if successful
   */
  public boolean generateIdeActivityProject(Project project, String ide) {
    EclipseIdeProjectCreatorSpecification spec;
    ProjectType type = projectTypeRegistry.getProjectType(project);
    if (type != null) {
      spec = type.getEclipseIdeProjectCreatorSpecification();
    } else {
      spec = new NonJavaEclipseIdeProjectCreatorSpecification();
    }

    ProjectBuildContext context = new ProjectBuildContext(type, project, this);
    return ideProjectCreator.createProject(project, context, spec);
  }

  /**
   * Deploy a project.
   *
   * @param project
   *          the activity project to generate the IDE project for
   * @param type
   *          the name of the IDE to generate the project for
   *
   * @return {@code true} if successful
   */
  public boolean deployProject(Project project, String type) {
    // TODO(keith): write a class for this
    try {
      for (ProjectDeployment deployment : project.getDeployments()) {
        if (type.equals(deployment.getType())) {
          File deploymentLocation = new File(workbenchSimpleConfig.evaluate(deployment.getLocation()));
          System.out.format("Deploying to %s\n", deploymentLocation.getAbsolutePath());
          copyBuildArtifacts(project, deploymentLocation);
        }
      }

      return true;
    } catch (Exception e) {
      logError("Error while creating project", e);

      return false;
    }
  }

  /**
   * Copy the necessary build artifacts for the proejct.
   *
   * @param project
   *          project sourcing the build artifacts
   * @param destination
   *          destination directory for the artifacts
   */
  private void copyBuildArtifacts(Project project, File destination) {
    File[] artifacts = new File(project.getBaseDirectory(), COMMAND_BUILD).listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isFile();
      }
    });
    if (artifacts != null) {
      for (File artifact : artifacts) {
        fileSupport.copyFile(artifact, new File(destination, artifact.getName()));
      }
    }
  }

  /**
   * Create an OSGi bundle from an existing JAR.
   *
   * @param file
   *          the jar file
   */
  public void createOsgi(String file) {
    System.out.format("Making %s into an OSGi bundle\n", file);

    OsgiBundleCreator osgiBundleCreator = new BndOsgiBundleCreator();
    try {
      osgiBundleCreator.createBundle(new File(file), null, null);
    } catch (Exception e) {
      logError("Error while creating project", e);
    }
  }

  /**
   * Get a list of all files on the controller's system bootstrap classpath.
   *
   * @return all files on the classpath
   */
  public List<File> getControllerSystemBootstrapClasspath() {
    List<File> classpath = Lists.newArrayList();

    addClasspathFiles(new File(getControllerDirectory(), ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP), classpath);

    File controllerDirectory = getControllerDirectory();
    File javaSystemDirectory =
        new File(controllerDirectory, InteractiveSpacesContainer.INTERACTIVESPACES_CONTAINER_FOLDER_LIB_SYSTEM_JAVA);
    if (!javaSystemDirectory.isDirectory()) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Controller directory %s configured by %s does not appear to be valid.", controllerDirectory,
          CONFIGURATION_CONTROLLER_BASEDIR));
    }
    classpath.add(new File(javaSystemDirectory, "com.springsource.org.apache.commons.logging-1.1.1.jar"));

    addControllerExtensionsClasspath(classpath);

    return classpath;
  }

  /**
   * Add all JAR files from the given directory to the classdpath.
   *
   * @param directory
   *          the directory to get the classpath files from
   * @param classpath
   *          the classpath to add the files to
   */
  private void addClasspathFiles(File directory, List<File> classpath) {
    File[] files = directory.listFiles(new FileFilter() {
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
  }

  /**
   * Get the controller directory which is supporting this workbench.
   *
   * @return the controller directory
   */
  public File getControllerDirectory() {
    String controllerPath = workbenchConfig.get(CONFIGURATION_CONTROLLER_BASEDIR);
    File controllerDirectory = new File(controllerPath);
    if (controllerDirectory.isAbsolute()) {
      return controllerDirectory;
    }
    File homeDir = new File(workbenchConfig.get(CONFIGURATION_INTERACTIVESPACES_HOME));
    return new File(homeDir, controllerPath);
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param files
   *          the list of files to add to.
   */
  private void addControllerExtensionsClasspath(List<File> files) {
    File[] extensionFiles =
        new File(getControllerDirectory(),
            InteractiveSpacesContainer.INTERACTIVESPACES_CONTAINER_FOLDER_LIB_SYSTEM_JAVA)
            .listFiles(new FilenameFilter() {

              @Override
              public boolean accept(File dir, String name) {
                return name.endsWith(EXTENSION_FILE_EXTENSION);
              }
            });

    if (extensionFiles != null) {
      for (File extensionFile : extensionFiles) {
        processExtensionFile(files, extensionFile, getControllerDirectory());
      }
    }
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param files
   *          the list of files to add to.
   * @param alternate
   *          the alternate files to add
   */
  public void addAlternateControllerExtensionsClasspath(List<File> files, String alternate) {
    File[] alternateFiles =
        new File(new File(workbenchFileSystem.getInstallDirectory(), "alternate"), alternate)
            .listFiles(new FilenameFilter() {

              @Override
              public boolean accept(File dir, String name) {
                return name.endsWith(FILENAME_JAR_EXTENSION);
              }
            });

    if (alternateFiles != null) {
      for (File alternateFile : alternateFiles) {
        files.add(alternateFile);
      }
    }
  }

  /**
   * process an extension file.
   *
   * @param files
   *          the collection of jars described in the extension files
   * @param extensionFile
   *          the extension file to process
   * @param controllerBaseDir
   *          base directory of the controller
   */
  private void processExtensionFile(List<File> files, File extensionFile, File controllerBaseDir) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(extensionFile));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          int pos = line.indexOf(EXTENSION_FILE_PATH_KEYWORD);
          if (pos == 0 && line.length() > EXTENSION_FILE_PATH_KEYWORD_LENGTH) {
            String classpathAddition = line.substring(EXTENSION_FILE_PATH_KEYWORD_LENGTH);

            // Want to be able to have files relative to the controller
            File classpathFile = new File(classpathAddition);
            if (!classpathFile.isAbsolute()) {
              classpathFile = new File(controllerBaseDir, classpathAddition);
            }
            files.add(classpathFile);
          }
        }
      }
    } catch (Exception e) {
      logError("Error while creating project", e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }

  /**
   * Perform a series of commands.
   *
   * @param commands
   *          the commands to run
   */
  public void doCommands(List<String> commands) {
    String command = commands.remove(0);

    if (COMMAND_CREATE.equals(command)) {
      System.out.println("Creating project");
      createProject(commands);
    } else if (COMMAND_OSGI.equals(command)) {
      createOsgi(commands.remove(0));
    } else {
      File baseDir = new File(command);
      if (projectManager.isProjectFolder(baseDir)) {
        doCommandsOnProject(baseDir, commands);
      } else {
        if (!commands.isEmpty() && COMMAND_RECURSIVE.equals(commands.get(0))) {
          commands.remove(0);

          doCommandsOnTree(baseDir, commands);
        } else {
          throw new SimpleInteractiveSpacesException(String.format("%s is not a project directory",
              baseDir.getAbsolutePath()));
        }
      }
    }
  }

  /**
   * Do a series of workbench commands on a project directory.
   *
   * @param baseDir
   *          base directory of the project
   * @param commands
   *          the commands to be done
   */
  public void doCommandsOnProject(File baseDir, List<String> commands) {
    Project project = projectManager.readProject(baseDir, log);
    doCommandsOnProject(project, commands);
  }

  /**
   * Walk over a set of folders looking for project files to build.
   *
   * @param baseDir
   *          base file to start looking for projects in
   *
   * @param commands
   *          commands to run on all project files
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
   * Walk over a set of folders looking for project files to build.
   *
   * @param baseDir
   *          base folder which may be a project folder or may contain project
   *          folders
   * @param commands
   *          commands to run on all project files
   * @param filter
   *          file filter to identify which files in the tree to execute on
   */
  private void doCommandsOnTree(File baseDir, List<String> commands, FileFilter filter) {
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
   *          the commands to execute
   */
  private void createProject(List<String> commands) {
    ProjectCreationSpecification spec = new ProjectCreationSpecification();

    ActivityProject project = new ActivityProject();
    project.setType(ActivityProject.PROJECT_TYPE_NAME);
    populateProjectFromConsole(project);
    project.setBaseDirectory(new File(project.getIdentifyingName()));

    spec.setProject(project);

    String command = commands.remove(0);
    if ("language".equals(command)) {
      spec.setLanguage(commands.remove(0));
    } else if ("template".equals(command)) {
      String source = commands.remove(0);
      if ("example".equals(source)) {
        log.error("Not implemented yet");
        return;
      } else if ("site".equals(source)) {
        log.error("Not implemented yet");
        return;
      }
    }

    activityProjectCreator.createProject(spec);
  }

  /**
   * Populate the project data from the console.
   *
   * @param project
   *          where the project data should be stored
   */
  private void populateProjectFromConsole(Project project) {
    Console console = System.console();

    if (console != null) {
      String identifyingName = getValue("Identifying name", new ActivityIdentifyingNameValidator(), console);
      String version = getValue("Version", new VersionValidator(), console);
      String name = console.readLine("Name: ");
      String description = console.readLine("Description: ");

      project.setIdentifyingName(identifyingName);
      project.setVersion(Version.parseVersion(version));
      project.setName(name);
      project.setDescription(description);
    } else {
      throw new SimpleInteractiveSpacesException("Could not allocate console");
    }
  }

  /**
   * Get a value from the user.
   *
   * @param prompt
   *          the prompt for the user
   * @param validator
   *          the validator for the value
   * @param console
   *          the console for IO
   *
   * @return a valid value for the question
   */
  private String getValue(String prompt, Validator validator, Console console) {
    String fullPrompt = prompt + ": ";

    String value = console.readLine(fullPrompt);
    DomainValidationResult result = validator.validate(value);
    while (result.getResultType().equals(DomainValidationResultType.ERRORS)) {
      console.printf("%s\n", result.getDescription());
      value = console.readLine(fullPrompt);
      result = validator.validate(value);
    }

    return value;
  }

  /**
   * Perform a sequence of commands on a project.
   *
   * @param project
   *          the project being acted on
   * @param commands
   *          the commands to perform on the project
   */
  private void doCommandsOnProject(Project project, List<String> commands) {
    if (commands.isEmpty()) {
      commands.add(COMMAND_BUILD);
    }

    boolean noErrors = true;
    while (!commands.isEmpty() && noErrors) {
      String command = commands.remove(0);

      if (COMMAND_BUILD.equals(command)) {
        System.out.format("Building project %s\n", project.getBaseDirectory().getAbsolutePath());
        noErrors = buildProject(project);
      } else if (COMMAND_CLEAN.equals(command)) {
        System.out.format("Cleaning project %s\n", project.getBaseDirectory().getAbsolutePath());
        noErrors = cleanActivityProject(project);
      } else if (COMMAND_DOCS.equals(command)) {
        System.out.format("Building Docs for project %s\n", project.getBaseDirectory().getAbsolutePath());
        noErrors = generateDocs(project);
      } else if (COMMAND_IDE.equals(command)) {
        System.out.format("Building project IDE project %s\n", project.getBaseDirectory().getAbsolutePath());
        noErrors = generateIdeActivityProject(project, commands.remove(0));
      } else if (COMMAND_DEPLOY.equals(command)) {
        System.out.format("Deploying project %s\n", project.getBaseDirectory().getAbsolutePath());
        noErrors = deployProject(project, commands.remove(0));
      }
    }
  }

  /**
   * generate the docs for a project.
   *
   * @param project
   *          the project
   *
   * @return {@code true} if the build was successful
   */
  public boolean generateDocs(Project project) {
    // TODO(keith): Make work for other project types
    if ("library".equals(project.getType()) || "java".equals(project.getBuilderType())) {
      JavadocGenerator generator = new ExternalJavadocGenerator();
      ProjectBuildContext context = new ProjectBuildContext(null, project, this);

      generator.generate(context);

      return true;
    } else {
      System.err.format("Project located at %s is not a java project\n", project.getBaseDirectory().getAbsolutePath());

      return false;
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

  /**
   * Get all files in the workbench bootstrap folders, both system and user.
   *
   * @return all files in bootstrap folder.
   */
  public List<File> getAllWorkbenchBootstrapFiles() {
    List<File> files = Lists.newArrayList();

    addClasspathFiles(workbenchFileSystem.getSystemBootstrapDirectory(), files);
    File userBootstrap = workbenchFileSystem.getUserBootstrapDirectory();
    if (userBootstrap.exists() && userBootstrap.isDirectory()) {
      addClasspathFiles(userBootstrap, files);
    }

    return files;
  }

  /**
   * Log an error.
   *
   * @param message
   *          any message for the error
   * @param e
   *          the exception
   */
  public void logError(String message, Exception e) {
    System.err.println(message);

    if (e instanceof SimpleInteractiveSpacesException) {
      getLog().error(((SimpleInteractiveSpacesException) e).getCompoundMessage());
    } else {
      getLog().error(message, e);
    }
  }

  /**
   * Get the workbench logger.
   *
   * @return the workbench logger
   */
  public Log getLog() {
    return log;
  }
}
