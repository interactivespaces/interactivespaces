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

import java.util.Set;

/**
 * A task in the workbench.
 *
 * @author Keith M. Hughes
 */
public interface WorkbenchTask {

  /**
   * Add a dependency to the tasks.
   *
   * @param dependency
   *          the task that must be completed before this task
   */
  void addTaskDependency(WorkbenchTask dependency);

  /**
   * Get the dependencies for the task.
   *
   * @return the dependencies for the task
   */
  Set<WorkbenchTask> getTaskDependencies();

  /**
   * Perform the task.
   *
   * <p>
   * If the task completes, it was successful. If it throws an exception, something bad happened.
   *
   * @param workbenchTaskContext
   *          the context for workbench tasks
   *
   * @throws InteractiveSpacesException
   *           the task had an issue
   */
  void perform(WorkbenchTaskContext workbenchTaskContext) throws InteractiveSpacesException;
}
