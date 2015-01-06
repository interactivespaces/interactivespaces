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

import interactivespaces.workbench.tasks.BaseWorkbenchTask;

/**
 * A workbench task for project tasks.
 *
 * @author Keith M. Hughes
 */
public abstract class ProjectWorkbenchTask extends BaseWorkbenchTask {

  /**
   * The project the task is being performed on.
   */
  private Project project;

  /**
   * The project task context.
   */
  private ProjectTaskContext projectTaskContext;

  /**
   * Construct a new task.
   *
   * @param project
   *          the project the task is being performed on
   * @param projectTaskContext
   *        context for the project task
   */
  public ProjectWorkbenchTask(Project project, ProjectTaskContext projectTaskContext) {
    this.project = project;
    this.projectTaskContext = projectTaskContext;
  }

  /**
   * Get the project the task is being performed on.
   *
   * @return the project
   */
  public Project getProject() {
    return project;
  }

  /**
   * Get the project task context.
   *
   * @return the project task context
   */
  public ProjectTaskContext getProjectTaskContext() {
    return projectTaskContext;
  }
}
