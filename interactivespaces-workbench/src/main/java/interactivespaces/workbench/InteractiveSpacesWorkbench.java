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
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.system.BasicInteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDeployment;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.ActivityProjectManager;
import interactivespaces.workbench.project.activity.BasicActivityProjectManager;
import interactivespaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import interactivespaces.workbench.project.activity.ide.EclipseIdeProjectCreator;
import interactivespaces.workbench.project.activity.ide.EclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.activity.ide.NonJavaEclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.activity.packager.ActivityProjectPackager;
import interactivespaces.workbench.project.activity.packager.ActivityProjectPackagerImpl;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.activity.type.ProjectTypeRegistry;
import interactivespaces.workbench.project.activity.type.SimpleProjectTypeRegistry;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.builder.ProjectBuilder;
import interactivespaces.workbench.project.creator.ProjectCreationContext;
import interactivespaces.workbench.project.creator.ProjectCreator;
import interactivespaces.workbench.project.creator.ProjectCreatorImpl;
import interactivespaces.workbench.project.group.GroupProjectTemplateSpecification;
import interactivespaces.workbench.project.java.BndOsgiContainerBundleCreator;
import interactivespaces.workbench.project.java.ContainerBundleCreator;
import interactivespaces.workbench.project.java.ExternalJavadocGenerator;
import interactivespaces.workbench.project.java.JavadocGenerator;
import interactivespaces.workbench.project.jdom.JdomProjectGroupTemplateSpecificationReader;
import interactivespaces.workbench.ui.UserInterfaceFactory;
import interactivespaces.workbench.ui.editor.swing.PlainSwingUserInterfaceFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import org.apache.commons.logging.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
   * The base folder for extras.
   */
  public static final String EXTRAS_BASE_FOLDER = "extras";

  /**
   * Configuration property defining the project home directory.
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
   * Configuration property giving the location of the controller the workbench is using.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_HOME = "interactivespaces.home";

  /**
   * Configuration property giving the location of the controller the workbench is using.
   */
  public static final String CONFIGURATION_CONTROLLER_BASEDIR = "interactivespaces.controller.basedir";

  /**
   * Configuration property giving the location of the master the workbench is using.
   */
  public static final String CONFIGURATION_MASTER_BASEDIR = "interactivespaces.master.basedir";

  /**
   * Configuration for the workbench.
   */
  private final Configuration workbenchConfig;

  /**
   * The activity project manager for file operations.
   */
  private final ActivityProjectManager projectManager = new BasicActivityProjectManager(this);

  /**
   * The creator for new projects.
   */
  private final ProjectCreator projectCreator;

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
   * The base classloader to use for things like unit testing.
   */
  private final ClassLoader baseClassLoader;

  /**
   * Logger for the workbench.
   */
  private final Log log;

  /**
   * Construct a workbench.
   *
   * @param workbenchProperties
   *          the map of configuration properties for the workbench
   * @param baseClassLoader
   *          the base classloader for the workbench
   * @param log
   *          the logger to use
   */
  public InteractiveSpacesWorkbench(Map<String, String> workbenchProperties, ClassLoader baseClassLoader, Log log) {
    this.baseClassLoader = baseClassLoader;
    this.log = log;

    workbenchConfig = SimpleConfiguration.newConfiguration();

    workbenchConfig.setValues(workbenchProperties);
    workbenchConfig.setValue(CONFIGURATION_PROPERTY_WORKBENCH_HOME, workbenchFileSystem.getInstallDirectory()
        .getAbsolutePath());

    this.templater = new FreemarkerTemplater();
    templater.startup();

    projectTypeRegistry = new SimpleProjectTypeRegistry();
    projectCreator = new ProjectCreatorImpl(this, templater);
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
      handleError("Error while creating project", e);
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
          File deploymentLocation = new File(workbenchConfig.evaluate(deployment.getLocation()));
          System.out.format("Deploying to %s\n", deploymentLocation.getAbsolutePath());
          copyBuildArtifacts(project, deploymentLocation);
        }
      }

      return true;
    } catch (Exception e) {
      handleError("Error while creating project", e);

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
   * @param args
   *          the args for the OSGi bundle
   */
  public void createOsgi(List<String> args) {
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

        outputFile = fileSupport.newFile(args.get(i));
      } else if (COMMAND_LINE_FLAG_HEADERS.equals(arg)) {
        if (++i >= args.size()) {
          throw new SimpleInteractiveSpacesException("Missing headers value from osgi command");
        }

        headersFile = fileSupport.newFile(args.get(i));
      } else if (COMMAND_LINE_FLAG_SOURCEDIR.equals(arg)) {
        if (++i >= args.size()) {
          throw new SimpleInteractiveSpacesException("Missing source dir value from osgi command");
        }

        addJarFiles(fileSupport.newFile(args.get(i)), sources);
      } else {
        sources.add(fileSupport.newFile(arg));
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
      handleError("Error while creating project", e);
    }
  }

  /**
   * Get a list of all files on the controller's system bootstrap classpath.
   *
   * @return all files on the classpath
   */
  public List<File> getControllerSystemBootstrapClasspath() {
    List<File> classpath = Lists.newArrayList();

    addJarFiles(fileSupport.newFile(getControllerDirectory(), ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP), classpath);

    File controllerDirectory = getControllerDirectory();
    File javaSystemDirectory =
        fileSupport.newFile(controllerDirectory, InteractiveSpacesContainer.INTERACTIVESPACES_CONTAINER_FOLDER_LIB_SYSTEM_JAVA);
    if (!javaSystemDirectory.isDirectory()) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Controller directory %s configured by %s does not appear to be valid.", controllerDirectory,
          CONFIGURATION_CONTROLLER_BASEDIR));
    }

    // TODO(keith): Get these in a file somewhere.
    classpath.add(new File(javaSystemDirectory, "com.springsource.org.apache.commons.logging-1.1.1.jar"));
    classpath.add(new File(javaSystemDirectory, "org.apache.felix.framework-4.2.1.jar"));

    addControllerExtensionsClasspath(classpath);

    return classpath;
  }

  /**
   * Add all JAR files from the given directory to the list of files.
   *
   * @param directory
   *          the directory to get the jar files from
   * @param fileList
   *          the list to add the files to
   */
  private void addJarFiles(File directory, List<File> fileList) {
    File[] files = directory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(FILENAME_JAR_EXTENSION);
      }
    });
    if (files != null) {
      Collections.addAll(fileList, files);
    }
  }

  /**
   * Get the controller directory which is supporting this workbench.
   *
   * @return the controller directory
   */
  public File getControllerDirectory() {
    String controllerPath = workbenchConfig.getPropertyString(CONFIGURATION_CONTROLLER_BASEDIR);
    File controllerDirectory = new File(controllerPath);
    if (controllerDirectory.isAbsolute()) {
      return controllerDirectory;
    }
    File homeDir = fileSupport.newFile(workbenchConfig.getPropertyString(CONFIGURATION_INTERACTIVESPACES_HOME));
    return fileSupport.newFile(homeDir, controllerPath);
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param files
   *          the list of files to add to.
   */
  private void addControllerExtensionsClasspath(List<File> files) {
    File[] extensionFiles =
        fileSupport.newFile(
            fileSupport.newFile(getControllerDirectory(), ContainerFilesystemLayout.FOLDER_DEFAULT_CONFIG),
            ContainerFilesystemLayout.FOLDER_CONFIG_ENVIRONMENT).listFiles(new FilenameFilter() {
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
   * @param classpath
   *          the list of files to add to
   * @param extraComponent
   *          the extra component to add
   */
  public void addExtrasControllerExtensionsClasspath(List<File> classpath, String extraComponent) {
    File[] extraComponentFiles =
        new File(new File(workbenchFileSystem.getInstallDirectory(), EXTRAS_BASE_FOLDER), extraComponent)
            .listFiles(new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                return name.endsWith(FILENAME_JAR_EXTENSION);
              }
            });

    if (extraComponentFiles != null) {
      Collections.addAll(classpath, extraComponentFiles);
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
            String classpathAddition = line.substring(EXTENSION_FILE_PATH_KEYWORD_LENGTH).trim();

            // Want to be able to have files relative to the controller
            File classpathFile = fileSupport.newFile(classpathAddition);
            if (!classpathFile.isAbsolute()) {
              classpathFile = fileSupport.newFile(controllerBaseDir, classpathAddition);
            }
            files.add(classpathFile);
          }
        }
      }
    } catch (Exception e) {
      handleError("Error while creating project", e);
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
    String command = removeArgument(commands, "command");

    if (COMMAND_CREATE.equals(command)) {
      createProject(commands);
    } else if (COMMAND_OSGI.equals(command)) {
      createOsgi(commands);
    } else {
      File baseDir = new File(command);
      if (baseDir.isDirectory()) {
        if (projectManager.isProjectFolder(baseDir)) {
          if (!doCommandsOnProject(baseDir, commands)) {
            throw new SimpleInteractiveSpacesException("Errors encountered processing project");
          }
        } else if (commands.isEmpty()) {
          log.warn(String.format("No commands to execute on the non-project directory %s", baseDir.getPath()));
        } else {
          String commandModifier = removeArgument(commands, "command modifier");
          if (COMMAND_RECURSIVE.equals(commandModifier)) {
            if (!doCommandsOnTree(baseDir, commands)) {
              throw new SimpleInteractiveSpacesException("Previous errors encountered");
            }
          } else {
            throw new SimpleInteractiveSpacesException(String.format(
                "Cannot run command %s on non-project directory %s", commandModifier, baseDir.getPath()));
          }
        }
      } else {
        throw new SimpleInteractiveSpacesException(String.format("%s is not a directory", baseDir.getAbsolutePath()));
      }
    }

    if (!commands.isEmpty()) {
      throw new SimpleInteractiveSpacesException("Extra command line arguments: " + commands);
    }
  }

  /**
   * Process a project create command.
   *
   * @param commands
   *          command input to inform project creation
   */
  private void createProject(List<String> commands) {
    getLog().info("Creating project from specification...");
    File specFile =
        new File(determineTemplateSpec(removeArgument(commands, "specification project type"),
            removeArgument(commands, "specification project kind")));
    File baseDirectory = new File(removeArgument(commands, "base output directory"));

    JdomProjectGroupTemplateSpecificationReader projectReader = new JdomProjectGroupTemplateSpecificationReader(this);
    GroupProjectTemplateSpecification project = projectReader.readProjectGroupTemplateSpecification(specFile);

    ProjectCreationContext creationSpecification = new ProjectCreationContext(specFile.getAbsolutePath());
    creationSpecification.setGroupProjectTemplateSpecification(project);
    creationSpecification.setWorkbench(this);
    creationSpecification.setSpecificationBase(specFile.getParentFile());
    creationSpecification.setBaseDirectory(baseDirectory);

    projectCreator.create(creationSpecification);
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
    boolean success = true;

    if (projectManager.isProjectFolder(baseDir)) {
      try {
        if (!doCommandsOnProject(baseDir, Lists.newArrayList(commands))) {
          success = false;
        }
      } catch (Exception e) {
        getLog().error("Error encountered performing commands on project", e);
        success = false;
      }
    } else {
      File[] files = baseDir.listFiles(filter);
      if (files != null) {
        for (File possible : files) {
          success &= doCommandsOnTree(possible, commands, filter);
        }
      }
    }
    return success;
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
  private boolean doCommandsOnProject(Project project, List<String> commands) {
    if (commands.isEmpty()) {
      commands.add(COMMAND_BUILD);
    }

    boolean success = true;
    while (!commands.isEmpty() && success) {
      String command = removeArgument(commands, "workbench command");

      if (COMMAND_BUILD.equals(command)) {
        log.info(String.format("Building project %s", project.getBaseDirectory().getAbsolutePath()));
        success = buildProject(project);
      } else if (COMMAND_CLEAN.equals(command)) {
        log.info(String.format("Cleaning project %s", project.getBaseDirectory().getAbsolutePath()));
        success = cleanActivityProject(project);
      } else if (COMMAND_DOCS.equals(command)) {
        log.info(String.format("Building Docs for project %s", project.getBaseDirectory().getAbsolutePath()));
        success = generateDocs(project);
      } else if (COMMAND_IDE.equals(command)) {
        log.info(String.format("Building project IDE project %s", project.getBaseDirectory().getAbsolutePath()));
        success = generateIdeActivityProject(project, removeArgument(commands, "ide type"));
      } else if (COMMAND_DEPLOY.equals(command)) {
        log.info(String.format("Deploying project %s", project.getBaseDirectory().getAbsolutePath()));
        success = deployProject(project, removeArgument(commands, "deployment type"));
      }
    }

    return success;
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
  public ProjectCreator getProjectCreator() {
    return projectCreator;
  }

  /**
   * @return the workbenchProperties
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
   * Get all files in the workbench bootstrap folders, both system and user.
   *
   * @return all files in bootstrap folder.
   */
  public List<File> getAllWorkbenchBootstrapFiles() {
    List<File> files = Lists.newArrayList();

    addJarFiles(workbenchFileSystem.getSystemBootstrapDirectory(), files);
    File userBootstrap = workbenchFileSystem.getUserBootstrapDirectory();
    if (userBootstrap.exists() && userBootstrap.isDirectory()) {
      addJarFiles(userBootstrap, files);
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
  public void handleError(String message, Exception e) {
    throw new SimpleInteractiveSpacesException(message, e);
  }

  /**
   * Get the project type registry for creating new projects.
   *
   * @return project type registry
   */
  public ProjectTypeRegistry getProjectTypeRegistry() {
    return projectTypeRegistry;
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
}
