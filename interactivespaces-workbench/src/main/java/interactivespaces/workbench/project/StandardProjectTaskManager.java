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

package interactivespaces.workbench.project;

import interactivespaces.configuration.Configuration;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import interactivespaces.workbench.project.activity.packager.ActivityProjectPackager;
import interactivespaces.workbench.project.activity.packager.StandardActivityProjectPackager;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.activity.type.ProjectTypeRegistry;
import interactivespaces.workbench.project.builder.ProjectBuilder;
import interactivespaces.workbench.project.ide.EclipseIdeProjectCreator;
import interactivespaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.ide.NonJavaEclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.java.ExternalJavadocGenerator;
import interactivespaces.workbench.project.java.JavadocGenerator;
import interactivespaces.workbench.tasks.WorkbenchTaskContext;

import com.google.common.collect.Maps;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;

/**
 * Standard implementation of the project tasks manager.
 *
 * @author Keith M. Hughes
 */
public class StandardProjectTaskManager implements ProjectTaskManager {

  /**
   * The registry of project types.
   */
  private final ProjectTypeRegistry projectTypeRegistry;

  /**
   * A packager for activities.
   */
  private final ActivityProjectPackager activityProjectPackager;

  /**
   * The IDE project creator.
   */
  private final EclipseIdeProjectCreator ideProjectCreator;

  /**
   * The templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new manager.
   *
   * @param projectTypeRegistry
   *          the project type registry to use
   * @param templater
   *          the templater to use
   */
  public StandardProjectTaskManager(ProjectTypeRegistry projectTypeRegistry, FreemarkerTemplater templater) {
    this.projectTypeRegistry = projectTypeRegistry;
    this.templater = templater;

    activityProjectPackager = new StandardActivityProjectPackager();
    ideProjectCreator = new EclipseIdeProjectCreator(templater);
  }

  @Override
  public ProjectTaskContext newProjectTaskContext(Project project, WorkbenchTaskContext workbenchTaskContext) {
    ProjectType type = projectTypeRegistry.getProjectType(project);

    ProjectTaskContext projectTaskContext = new ProjectTaskContext(type, project, workbenchTaskContext);

    // Always add a pre-task
    addPreTasks(project, projectTaskContext, workbenchTaskContext);

    workbenchTaskContext.addProjectTaskContext(projectTaskContext);

    return projectTaskContext;
  }

  @Override
  public void addPreTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    workbenchTaskContext.addTasks(new ProjectPreTask(project, projectTaskContext));
  }

  @Override
  public void addCleanTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    workbenchTaskContext.addTasks(new ProjectCleanTask(project, projectTaskContext));
  }

  @Override
  public void addBuildTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    // TODO(keith): Consider putting this map into the workbench task context with tasks having a classification, e.g.
    // build, clean, etc. If so, workbench task context objects should be used once or have a reset functionality.
    Map<Project, ProjectBuildTask> buildTasks = Maps.newHashMap();
    addProjectBuildTask(project, projectTaskContext, workbenchTaskContext, buildTasks);
  }

  /**
   * Add a build task for a given project to the growing list of tasks.
   *
   * <p>
   * All dependent tasks are added.
   *
   * @param project
   *          the project a build task is needed for
   * @param projectTaskContext
   *          the task context for the project to be built
   * @param workbenchTaskContext
   *          the workbench task context the build is to be done under
   * @param existingBuildTasks
   *          all project build tasks
   *
   * @return the new build task
   */
  private ProjectBuildTask addProjectBuildTask(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext, Map<Project, ProjectBuildTask> existingBuildTasks) {
    ProjectBuildTask newBuildTask = new ProjectBuildTask(project, projectTaskContext);
    projectTaskContext.addProjectTasks(newBuildTask);

    existingBuildTasks.put(project, newBuildTask);

    // TODO(keith): Should this go in the expanding build tasks? Probably if knew general task category.
    ProjectPackageTask newPackageTask = new ProjectPackageTask(project, projectTaskContext);
    newPackageTask.addTaskDependency(newBuildTask);
    workbenchTaskContext.addTasks(newPackageTask);

    for (ProjectDependency dependency : project.getDependencies()) {
      if (dependency.isDynamic()) {
        Project dependencyProject = workbenchTaskContext.getDynamicProjectFromProjectPath(dependency);
        if (dependencyProject != null) {
          ProjectBuildTask dependencyBuildTask = existingBuildTasks.get(dependencyProject);
          if (dependencyBuildTask == null) {
            // No build task currently for this dependency, so create it.
            dependencyBuildTask =
                addProjectBuildTask(dependencyProject, newProjectTaskContext(dependencyProject, workbenchTaskContext),
                    workbenchTaskContext, existingBuildTasks);

            // Set the provider for the dependency.
            dependency.setProvider(new DynamicProjectProjectDependencyProvider(dependencyBuildTask
                .getProjectTaskContext()));
          }

          // Add the dependency context from the build context so that the current task can get all artifacts from the
          // dependency.
          ProjectTaskContext dependencyProjectTaskContext = dependencyBuildTask.getProjectTaskContext();
          projectTaskContext.addDynamicProjectDependencyContext(dependencyProjectTaskContext)
              .addDynamicProjectDependencyContexts(dependencyProjectTaskContext.getDynamicProjectDependencyContexts());

          newBuildTask.addTaskDependency(dependencyBuildTask);
        }
      }
    }

    return newBuildTask;
  }

  @Override
  public void addDocsTasks(Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    workbenchTaskContext.addTasks(new ProjectDocTask(project, projectTaskContext));
  }

  @Override
  public void addDeploymentTasks(String deploymentType, Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    workbenchTaskContext.addTasks(new ProjectDeploymentTask(deploymentType, project, projectTaskContext));
  }

  @Override
  public void addIdeProjectTasks(Project project, String ide, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext) {
    workbenchTaskContext.addTasks(new ProjectIdeTask(ide, project, projectTaskContext));
  }

  /**
   * The pre task for tasks to be done to a chain of project commands.
   *
   * @author Keith M. Hughes
   */
  public class ProjectPreTask extends ProjectWorkbenchTask {

    /**
     * Construct a new task.
     *
     * @param project
     *          the project being cleaned
     * @param projectTaskContext
     *          context for the project tasks
     */
    public ProjectPreTask(Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_PRE, project, projectTaskContext);
    }

    @Override
    public void onPerform() {
      ProjectTaskContext projectTaskContext = getProjectTaskContext();

      projectTaskContext.processExtraConstituents();
    }
  }

  /**
   * The task for cleaning projects.
   *
   * @author Keith M. Hughes
   */
  public class ProjectCleanTask extends ProjectWorkbenchTask {

    /**
     * Construct a new task.
     *
     * @param project
     *          the project being cleaned
     * @param projectTaskContext
     *          context for the project tasks
     */
    public ProjectCleanTask(Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_CLEAN, project, projectTaskContext);
    }

    @Override
    public void onPerform() {
      ProjectTaskContext projectTaskContext = getProjectTaskContext();

      projectTaskContext.getLog().info(
          String.format("Cleaning project %s", getProject().getBaseDirectory().getAbsolutePath()));

      File buildDirectory = projectTaskContext.getBuildDirectory();

      if (buildDirectory.exists()) {
        fileSupport.deleteDirectoryContents(buildDirectory);
      }
    }
  }

  /**
   * The task for building projects.
   *
   * @author Keith M. Hughes
   */
  public class ProjectBuildTask extends ProjectWorkbenchTask {

    /**
     * Construct a new task.
     *
     * @param project
     *          the project being built
     * @param projectTaskContext
     *          context for project tasks
     */
    public ProjectBuildTask(Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_BUILD, project, projectTaskContext);
    }

    @Override
    public void onPerform() {
      ProjectTaskContext projectTaskContext = getProjectTaskContext();

      // If no type, there is nothing special to do for building.
      ProjectBuilder builder = null;
      ProjectType type = projectTaskContext.getProjectType();
      if (type != null) {
        builder = type.newBuilder();
      } else {
        builder = new BaseActivityProjectBuilder();
      }

      projectTaskContext.getLog().info(
          String.format("Building project %s", getProject().getBaseDirectory().getAbsolutePath()));
      projectTaskContext.getLog().info(
          String.format("Using Interactive Spaces Space Controller %s", projectTaskContext.getWorkbenchTaskContext()
              .getControllerDirectory().getAbsolutePath()));

      builder.build(getProject(), projectTaskContext);
    }
  }

  /**
   * The task for packaging projects.
   *
   * @author Keith M. Hughes
   */
  public class ProjectPackageTask extends ProjectWorkbenchTask {

    /**
     * Construct a new task.
     *
     * @param project
     *          the project being built
     * @param projectTaskContext
     *          context for project tasks
     */
    public ProjectPackageTask(Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_PACKAGE, project, projectTaskContext);
    }

    @Override
    public void onPerform() {

      try {
        getProjectTaskContext().getLog().info(
            String.format("Packaging project %s", getProject().getBaseDirectory().getAbsolutePath()));

        if (ActivityProject.PROJECT_TYPE_NAME.equals(getProject().getType())) {
          activityProjectPackager.packageActivityProject(getProject(), getProjectTaskContext());
        }
      } catch (Throwable e) {
        getProjectTaskContext().getWorkbenchTaskContext().handleError("Error while packaging project", e);
      }
    }
  }

  /**
   * The task for deploying projects.
   *
   * @author Keith M. Hughes
   */
  public class ProjectDeploymentTask extends ProjectWorkbenchTask {

    /**
     * The type of the deployment.
     */
    private String deploymentType;

    /**
     * Construct a new task.
     *
     * @param deploymentType
     *          the type of the deployment
     * @param project
     *          the project being built
     * @param projectTaskContext
     *          the context for project tasks
     */
    public ProjectDeploymentTask(String deploymentType, Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_DEPLOY, project, projectTaskContext);

      this.deploymentType = deploymentType;
    }

    @Override
    public void onPerform() {
      ProjectTaskContext projectTaskContext = getProjectTaskContext();

      try {
        Project project = getProject();
        projectTaskContext.getLog().info(
            String.format("Deploying project %s", project.getBaseDirectory().getAbsolutePath()));

        Configuration projectConfig = projectTaskContext.getProject().getConfiguration();
        for (ProjectDeployment deployment : project.getDeployments()) {
          if (deploymentType.equals(deployment.getType())) {
            File deploymentLocation = new File(projectConfig.evaluate(deployment.getLocation()));
            projectTaskContext.getLog().info(String.format("Deploying to %s\n", deploymentLocation.getAbsolutePath()));
            copyBuildArtifacts(deploymentLocation);
          }
        }
      } catch (Throwable e) {
        projectTaskContext.getWorkbenchTaskContext().handleError("Error while deploying project", e);
      }
    }

    /**
     * Copy the necessary build artifacts for the project.
     *
     * @param destination
     *          the destination directory for the deployment
     */
    private void copyBuildArtifacts(File destination) {
      File[] artifacts = getProjectTaskContext().getBuildDirectory().listFiles(new FileFilter() {
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
  }

  /**
   * The task for creating docs for projects.
   *
   * @author Keith M. Hughes
   */
  public class ProjectDocTask extends ProjectWorkbenchTask {

    /**
     * Construct a new task.
     *
     * @param project
     *          the project whose docs are being generated
     * @param projectTaskContext
     *          context for the project tasks
     */
    public ProjectDocTask(Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_DOC, project, projectTaskContext);
    }

    @Override
    public void onPerform() {
      ProjectTaskContext projectTaskContext = getProjectTaskContext();

      Project project = getProject();

      projectTaskContext.getLog().info(
          String.format("Building Docs for project %s", project.getBaseDirectory().getAbsolutePath()));

      // TODO(keith): Make work for other project types
      if ("library".equals(project.getType()) || "java".equals(project.getBuilderType())) {
        JavadocGenerator generator = new ExternalJavadocGenerator();

        generator.generate(getProjectTaskContext());
      } else {
        projectTaskContext.getLog().warn(
            String
                .format("Project located at %s is not a java project\n", project.getBaseDirectory().getAbsolutePath()));
      }
    }
  }

  /**
   * The task for creating IDe projects for projects.
   *
   * @author Keith M. Hughes
   */
  public class ProjectIdeTask extends ProjectWorkbenchTask {

    /**
     * The IDE to create projects for.
     */
    private String ide;

    /**
     * Construct a new task.
     *
     * @param ide
     *          the IDE to create the project for
     * @param project
     *          the project whose IDE files are being generated
     * @param projectTaskContext
     *          context for the project tasks
     */
    public ProjectIdeTask(String ide, Project project, ProjectTaskContext projectTaskContext) {
      super(StandardProjectTaskNames.TASK_NAME_IDE, project, projectTaskContext);

      this.ide = ide;
    }

    @Override
    public void onPerform() {
      ProjectTaskContext projectTaskContext = getProjectTaskContext();

      Project project = getProject();

      projectTaskContext.getLog().info(
          String.format("Building project IDE project %s", project.getBaseDirectory().getAbsolutePath()));

      EclipseIdeProjectCreatorSpecification spec;
      ProjectType type = getProjectTaskContext().getProjectType();
      if (type != null) {
        spec = type.getEclipseIdeProjectCreatorSpecification();
      } else {
        spec = new NonJavaEclipseIdeProjectCreatorSpecification();
      }

      ideProjectCreator.createProject(project, projectTaskContext, spec);
    }
  }
}
