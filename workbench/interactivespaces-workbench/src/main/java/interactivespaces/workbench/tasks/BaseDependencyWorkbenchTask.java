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

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * A base implementation for dependency based tasks.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseDependencyWorkbenchTask extends BaseWorkbenchTask implements DependencyWorkbenchTask {

  /**
   * The name of the task.
   */
  private String name;

  /**
   * The task dependencies for this task.
   */
  private Set<DependencyWorkbenchTask> taskDependencies = Sets.newHashSet();

  /**
   * Construct the task.
   *
   * @param name
   *          the name
   */
  public BaseDependencyWorkbenchTask(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void addTaskDependency(DependencyWorkbenchTask dependency) {
    taskDependencies.add(dependency);
  }

  @Override
  public Set<DependencyWorkbenchTask> getTaskDependencies() {
    return taskDependencies;
  }

  @Override
  public void applyTaskModifiers(WorkbenchTaskModifiers modifiers) {
    for (WorkbenchTaskFactory taskFactory : modifiers.getBeforeTasks()) {
      addWorkbenchTaskBefore(taskFactory.newTask());
    }

    for (WorkbenchTaskFactory taskFactory : modifiers.getAfterTasks()) {
      addWorkbenchTaskAfter(taskFactory.newTask());
    }
  }
}
