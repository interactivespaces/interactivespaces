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

import java.util.List;

/**
 * A task in the workbench.
 *
 * <p>
 * Before and after tasks are not part of dependencies, they are done in the order given.
 *
 * @author Keith M. Hughes
 */
public interface WorkbenchTask {


  /**
   * Add tasks to be executed exactly before the task starts.
   *
   * <p>
   * Tasks will be executed in the order added.
   *
   * @param tasks
   *        the tasks to add
   */
  void addWorkbenchTaskBefore(WorkbenchTask... tasks);

  /**
   * Get the tasks to be done before this task.
   *
   * @return the tasks to be done before this task
   */
  List<WorkbenchTask> getBeforeTasks();

  /**
   * Add tasks to be executed exactly after the task completes.
   *
   * <p>
   * Tasks will be executed in the order added.
   *
   * @param tasks
   *        the tasks to add
   */
  void addWorkbenchTaskAfter(WorkbenchTask... tasks);

  /**
   * Get the tasks to be done after this task.
   *
   * @return the tasks to be done after this task
   */
  List<WorkbenchTask> getAfterTasks();

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
