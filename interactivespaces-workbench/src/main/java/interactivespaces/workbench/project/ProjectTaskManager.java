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

import interactivespaces.workbench.tasks.WorkbenchTaskContext;

/**
 * A manager for creating project tasks.
 *
 * @author Keith M. Hughes
 */
public interface ProjectTaskManager {

  /**
   * Create a new project task context.
   *
   * @param project
   *          the project
   * @param workbenchTaskContext
   *          the workbench task context
   *
   * @return the new project task context
   */
  ProjectTaskContext newProjectTaskContext(Project project, WorkbenchTaskContext workbenchTaskContext);

  /**
   * Add any pre-tasks to the context.
   *
   * @param project
   *          the project
   * @param projectTaskContext
   *          the context for project tasks
   * @param workbenchTaskContext
   *          the context for workbench tasks
   */
  void addPreTasks(Project project, ProjectTaskContext projectTaskContext, WorkbenchTaskContext workbenchTaskContext);

  /**
   * Create a new clean task.
   *
   * @param project
   *          the project
   * @param projectTaskContext
   *          the context for project tasks
   * @param workbenchTaskContext
   *          the context for workbench tasks
   */
  void addCleanTasks(Project project, ProjectTaskContext projectTaskContext, WorkbenchTaskContext workbenchTaskContext);

  /**
   * Create a new build task.
   *
   * @param project
   *          the project
   * @param projectTaskContext
   *          the context for project tasks
   * @param workbenchTaskContext
   *          the task context
   */
  void addBuildTasks(Project project, ProjectTaskContext projectTaskContext, WorkbenchTaskContext workbenchTaskContext);

  /**
   * Create a new deployment task.
   *
   * @param deploymentType
   *          the type of deployment
   * @param project
   *          the project
   * @param projectTaskContext
   *          the context for project tasks
   * @param workbenchTaskContext
   *          the task context for the workbench
   */
  void addDeploymentTasks(String deploymentType, Project project, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext);

  /**
   * Create a new docs task.
   *
   * @param project
   *          the project
   * @param projectTaskContext
   *          the context for project tasks
   * @param workbenchTaskContext
   *          the task context
   */
  void addDocsTasks(Project project, ProjectTaskContext projectTaskContext, WorkbenchTaskContext workbenchTaskContext);

  /**
   * Add tasks for creating IDE projects.
   *
   * @param project
   *          the project
   * @param ide
   *          which IDE to create project files for
   * @param projectTaskContext
   *          context for project tasks
   * @param workbenchTaskContext
   *          context for workbench tasks
   */
  void addIdeProjectTasks(Project project, String ide, ProjectTaskContext projectTaskContext,
      WorkbenchTaskContext workbenchTaskContext);
}
