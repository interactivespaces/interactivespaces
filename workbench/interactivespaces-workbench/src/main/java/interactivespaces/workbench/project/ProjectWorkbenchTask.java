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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.tasks.BaseDependencyWorkbenchTask;
import interactivespaces.workbench.tasks.WorkbenchTaskContext;

/**
 * A workbench task for project tasks.
 *
 * @author Keith M. Hughes
 */
public abstract class ProjectWorkbenchTask extends BaseDependencyWorkbenchTask {

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
   * @param name
   *          the name of the task
   * @param project
   *          the project the task is being performed on
   * @param projectTaskContext
   *          context for the project task
   */
  public ProjectWorkbenchTask(String name, Project project, ProjectTaskContext projectTaskContext) {
    super(name);

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

  @Override
  public void perform(WorkbenchTaskContext workbenchTaskContext) throws InteractiveSpacesException {
    projectTaskContext.setCurrentTaskName(getName());

    onPerform();

    projectTaskContext.setCurrentTaskName(null);
  }

  /**
   * Perform the task.
   *
   * @throws InteractiveSpacesException
   *           something bad happened
   */
  protected abstract void onPerform() throws InteractiveSpacesException;
}
