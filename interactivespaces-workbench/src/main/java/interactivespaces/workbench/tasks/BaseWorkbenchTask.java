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

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * A base class for workbench tasks that provides some support.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseWorkbenchTask implements WorkbenchTask {

  /**
   * The task dependencies for this task.
   */
  private Set<WorkbenchTask> taskDependencies = Sets.newHashSet();

  @Override
  public void addTaskDependency(WorkbenchTask dependency) {
    taskDependencies.add(dependency);
  }

  @Override
  public Set<WorkbenchTask> getTaskDependencies() {
    return taskDependencies;
  }
}
