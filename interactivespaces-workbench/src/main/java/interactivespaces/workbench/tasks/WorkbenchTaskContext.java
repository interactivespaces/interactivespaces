/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.workbench.tasks;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.resource.NamedVersionedResourceCollection;
import interactivespaces.resource.NamedVersionedResourceWithData;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.system.core.configuration.CoreConfiguration;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.util.graph.DependencyResolver;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.NativeApplicationRunnerCollection;
import interactivespaces.workbench.InteractiveSpacesContainer;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.ProjectManager;
import interactivespaces.workbench.project.ProjectTaskContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
 * The main context for a workbench task run.
 *
 * @author Keith M. Hughes
 */
public class WorkbenchTaskContext {

  /**
   * The base folder for extras in the workbench.
   */
  public static final String EXTRAS_BASE_FOLDER = "extras";

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
   * Configuration property giving the project path for the workbench.
   */
  public static final String CONFIGURATION_INTERACTIVESPACES_WORKBENCH_PROJECT_PATH =
      "interactivespaces.workbench.project.path";

  /**
   * Configuration property giving the location of the controller the workbench is using.
   */
  public static final String CONFIGURATION_CONTROLLER_BASEDIR = "interactivespaces.controller.basedir";

  /**
   * Configuration property giving the location of the master the workbench is using.
   */
  public static final String CONFIGURATION_MASTER_BASEDIR = "interactivespaces.master.basedir";

  /**
   * A file filter for detecting directories.
   */
  private static final FileFilter DIRECTORY_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };

  /**
   * The workbench for the context.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * The workbench configuration to be used by this task.
   */
  private final Configuration workbenchConfig;

  /**
   * The list of tasks.
   */
  private final List<DependencyWorkbenchTask> tasks = Lists.newArrayList();

  /**
   * The collection of projects scanned from the project path.
   */
  private final NamedVersionedResourceCollection<NamedVersionedResourceWithData<Project>> projectsPath;

  /**
   * {@code true} if the project path has been scanned.
   */
  private boolean projectPathScanned = false;

  /**
   * File support for file operations.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Some task had an error during the task processing process.
   */
  private boolean errors = false;

  /**
   * The native application runners collection for this context.
   */
  private NativeApplicationRunnerCollection nativeApplicationRunners;

  /**
   * Map of project task contextx for the current run.
   */
  private Map<Project, ProjectTaskContext> projectTaskContexts = Maps.newHashMap();

  /**
   * Construct a new context.
   *
   * @param workbench
   *          the workbench context
   * @param workbenchConfig
   *          the workbench configuration to be used for this context
   */
  public WorkbenchTaskContext(InteractiveSpacesWorkbench workbench, Configuration workbenchConfig) {
    this.workbench = workbench;
    this.workbenchConfig = workbenchConfig;

    projectsPath = NamedVersionedResourceCollection.newNamedVersionedResourceCollection();
  }

  /**
   * Get the workbench for the context.
   *
   * @return the workbench
   */
  public InteractiveSpacesWorkbench getWorkbench() {
    return workbench;
  }

  /**
   * An error has happened, handle it properly.
   *
   * @param message
   *          message for the error
   * @param e
   *          any exception that may have happened, can be {@code null}
   */
  public void handleError(String message, Throwable e) {
    errors = true;

    logException(e);

    throw new InteractiveSpacesWorkbenchTaskInterruptionException();
  }

  /**
   * Log an exception.
   *
   * @param e
   *          the exception to log
   */
  private void logException(Throwable e) {
    if (e instanceof SimpleInteractiveSpacesException) {
      workbench.getLog().error(((SimpleInteractiveSpacesException) e).getCompoundMessage());
    } else {
      workbench.getLog().error("Error executing workbench commands", e);
    }
  }

  /**
   * An error has happened, handle it properly.
   *
   * @param message
   *          message for the error
   */
  public void handleError(String message) {
    handleError(message, null);
  }

  /**
   * Add a new collection of tasks to the context.
   *
   * @param tasks
   *          the tasks to add
   *
   * @return this task context
   */
  public WorkbenchTaskContext addTasks(DependencyWorkbenchTask... tasks) {
    if (tasks != null) {
      Collections.addAll(this.tasks, tasks);
    }

    return this;
  }

  /**
   * Do all tasks in the task list.
   */
  public void doTasks() {
    try {
      prepareForTaskPerformance();

      for (DependencyWorkbenchTask task : getTasksInDependencyOrder()) {

        performTask(task);

        if (errors) {
          break;
        }
      }
    } finally {
      endTaskPerformance();
    }
  }

  /**
   * Prepare for performing all tasks.
   */
  private void prepareForTaskPerformance() {
    nativeApplicationRunners =
        new NativeApplicationRunnerCollection(workbench.getSpaceEnvironment(), workbench.getLog());
    nativeApplicationRunners.startup();
  }

  /**
   * End performing all tasks.
   */
  private void endTaskPerformance() {
    nativeApplicationRunners.shutdown();
    nativeApplicationRunners = null;
  }

  /**
   * Perform a task, including any before and after tasks.
   *
   * <p>
   * Performance is interrupted if any tasks fail.
   *
   * @param task
   *          the task to perform
   */
  private void performTask(WorkbenchTask task) {
    performTaskList(task.getBeforeTasks());

    if (errors) {
      return;
    }

    performMainTask(task);

    if (errors) {
      return;
    }

    performTaskList(task.getAfterTasks());
  }

  /**
   * Perform all tasks in the list in proper order. Return immediately if any errors.
   *
   * @param tasks
   *          the tasks to perform
   */
  private void performTaskList(List<WorkbenchTask> tasks) {
    for (WorkbenchTask task : tasks) {
      performTask(task);

      if (errors) {
        break;
      }
    }
  }

  /**
   * Perform an individual task.
   *
   * @param task
   *          the task to perform
   */
  private void performMainTask(WorkbenchTask task) {
    try {
      task.perform(this);
    } catch (Throwable e) {
      // last change catching of any exceptions not explicitly covered.
      if (!(e instanceof InteractiveSpacesWorkbenchTaskInterruptionException)) {
        errors = true;
        logException(e);
      }
    }
  }

  /**
   * Order the tasks in dependency order.
   *
   * @return the tasks in dependency order
   */
  private List<DependencyWorkbenchTask> getTasksInDependencyOrder() {
    DependencyResolver<DependencyWorkbenchTask, DependencyWorkbenchTask> resolver =
        new DependencyResolver<DependencyWorkbenchTask, DependencyWorkbenchTask>();

    for (DependencyWorkbenchTask task : tasks) {
      resolver.addNode(task, task);
      resolver.addNodeDependencies(task, task.getTaskDependencies());
    }

    resolver.resolve();
    List<DependencyWorkbenchTask> orderedTasks = resolver.getOrdering();
    return orderedTasks;
  }

  /**
   * Has the project tasks had an error?
   *
   * @return {@code true} if errors
   */
  public boolean hasErrors() {
    return errors;
  }

  /**
   * Get all files in the workbench bootstrap folders, both system and user.
   *
   * @return all files in bootstrap folder.
   */
  public List<File> getAllWorkbenchBootstrapFiles() {
    List<File> files = Lists.newArrayList();

    addJarFiles(workbench.getWorkbenchFileSystem().getSystemBootstrapDirectory(), files);
    File userBootstrap = workbench.getWorkbenchFileSystem().getUserBootstrapDirectory();
    if (userBootstrap.exists() && userBootstrap.isDirectory()) {
      addJarFiles(userBootstrap, files);
    }

    return files;
  }

  /**
   * Get a list of all files on the controller's system bootstrap classpath.
   *
   * @return all files on the classpath
   */
  public List<File> getControllerSystemBootstrapClasspath() {
    List<File> classpath = Lists.newArrayList();

    addJarFiles(fileSupport.newFile(getControllerDirectory(), ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP),
        classpath);

    File controllerDirectory = getControllerDirectory();
    File javaSystemDirectory =
        fileSupport.newFile(controllerDirectory,
            InteractiveSpacesContainer.INTERACTIVESPACES_CONTAINER_FOLDER_LIB_SYSTEM_JAVA);
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
  public void addJarFiles(File directory, List<File> fileList) {
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
    File homeDir =
        fileSupport.newFile(workbenchConfig.getPropertyString(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_HOME));
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
        new File(new File(workbench.getWorkbenchFileSystem().getInstallDirectory(), EXTRAS_BASE_FOLDER), extraComponent)
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
   * Scan the project path.
   */
  public void scanProjectPath() {
    if (!projectPathScanned) {
      ProjectManager projectManager = workbench.getProjectManager();
      List<String> projectPaths =
          workbenchConfig.getPropertyStringList(CONFIGURATION_INTERACTIVESPACES_WORKBENCH_PROJECT_PATH,
              File.pathSeparator);
      if (projectPaths != null) {
        for (String projectPath : projectPaths) {
          File projectPathBaseDir = fileSupport.newFile(projectPath);
          processProjectPathDirectory(projectPathBaseDir, projectManager);
        }
      }

      projectPathScanned = true;
    }
  }

  /**
   * Process a directory as part of the project path.
   *
   * @param projectPathBaseDir
   *          the base directory to be scanned
   * @param projectManager
   *          the project manager
   */
  private void processProjectPathDirectory(File projectPathBaseDir, ProjectManager projectManager) {
    if (projectManager.isProjectFolder(projectPathBaseDir)) {
      processProjectPathProjectDirectory(projectPathBaseDir, projectManager);
    } else {
      processProjectPathSubDirectories(projectPathBaseDir, projectManager);
    }
  }

  /**
   * Process a project directory from the project path.
   *
   * @param projectDir
   *          the project directory
   * @param projectManager
   *          the project manager
   */
  private void processProjectPathProjectDirectory(File projectDir, ProjectManager projectManager) {
    Project project = projectManager.readProject(projectDir, workbench.getLog());
    addProjectToProjectPath(project);
  }

  /**
   * Add a project to the project path.
   *
   * @param project
   *          the project to add to the project path
   */
  public void addProjectToProjectPath(Project project) {
    String identifyingName = project.getIdentifyingName();
    Version version = project.getVersion();
    projectsPath.addResource(identifyingName, version, new NamedVersionedResourceWithData<Project>(identifyingName,
        version, project));
  }

  /**
   * Recursively process a directory for any project directories contained within.
   *
   * @param projectPathBaseDir
   *          the base directory to be scanned for subdirectories
   * @param projectManager
   *          the project manager to use
   */
  private void processProjectPathSubDirectories(File projectPathBaseDir, ProjectManager projectManager) {
    File[] subdirectories = projectPathBaseDir.listFiles(DIRECTORY_FILE_FILTER);
    if (subdirectories != null) {
      for (File subdirectory : subdirectories) {
        processProjectPathDirectory(subdirectory, projectManager);
      }
    }
  }

  /**
   * Get a project dependency from the project path.
   *
   * @param projectDependency
   *          the project dependency
   *
   * @return the project, or {@code null} if no projects satisfies the request
   */
  public Project getDynamicProjectFromProjectPath(ProjectDependency projectDependency) {
    return getDynamicProjectFromProjectPath(projectDependency.getIdentifyingName(), projectDependency.getVersion());
  }

  /**
   * Get a project from the project path.
   *
   * @param identifyingName
   *          identifying name of the project
   * @param versionRange
   *          version range of the project
   *
   * @return the project, or {@code null} if no projects satisfies the request
   */
  public Project getDynamicProjectFromProjectPath(String identifyingName, VersionRange versionRange) {
    // TODO(keith): Consider moving all project repository scanning functionality into its own object that can be shared
    // between tasks.
    scanProjectPath();

    NamedVersionedResourceWithData<Project> project = projectsPath.getResource(identifyingName, versionRange);
    if (project != null) {
      return project.getData();
    } else {
      return null;
    }
  }

  /**
   * Add in a new project task context to the context.
   *
   * @param projectTaskContext
   *          the new project task context
   */
  public void addProjectTaskContext(ProjectTaskContext projectTaskContext) {
    projectTaskContexts.put(projectTaskContext.getProject(), projectTaskContext);
  }

  /**
   * Get project task context for the given project.
   *
   * @param project
   *          the given project
   *
   * @return the context for the given project, or {@code null} if none
   */
  public ProjectTaskContext getProjectTaskContext(Project project) {
    return projectTaskContexts.get(project);
  }

  /**
   * Get the native application runners for the context.
   *
   * @return the native application runners
   */
  public NativeApplicationRunnerCollection getNativeApplicationRunners() {
    return nativeApplicationRunners;
  }

  /**
   * Get the context log.
   *
   * @return the context log
   */
  public Log getLog() {
    return getWorkbench().getLog();
  }

  /**
   * An interruption of workbench tasks.
   *
   * @author Keith M. Hughes
   */
  public static class InteractiveSpacesWorkbenchTaskInterruptionException extends InteractiveSpacesException {

    /**
     * Construct a new exception.
     */
    public InteractiveSpacesWorkbenchTaskInterruptionException() {
      super("Task execution interruption");
    }
  }
}
