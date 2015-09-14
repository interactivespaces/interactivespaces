/*
 * Copyright (C) 2015 Google Inc.
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

import java.util.Set;

/**
 * A workbench task that has dependencies.
 *
 * @author Keith M. Hughes
 */
public interface DependencyWorkbenchTask extends WorkbenchTask {

  /**
   * Get the type of the workbench task.
   *
   * @return the type of the workbench task
   */
  String getName();

  /**
   * Add a dependency to the tasks.
   *
   * @param dependency
   *          the task that must be completed before this task
   */
  void addTaskDependency(DependencyWorkbenchTask dependency);

  /**
   * Get the dependencies for the task.
   *
   * @return the dependencies for the task
   */
  Set<DependencyWorkbenchTask> getTaskDependencies();

  /**
   * Apply task modifiers to this task.
   *
   * @param modifiers
   *        the modifiers to apply
   */
  void applyTaskModifiers(WorkbenchTaskModifiers modifiers);
}
