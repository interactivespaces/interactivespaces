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

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;


/**
 * A base class for workbench tasks that provides some support.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseWorkbenchTask implements WorkbenchTask {

  /**
   * Tasks to be done before this task executes.
   */
  private List<WorkbenchTask> beforeTasks = Lists.newArrayList();

  /**
   * Tasks to be done after this task executes.
   */
  private List<WorkbenchTask> afterTasks = Lists.newArrayList();

  @Override
  public void addWorkbenchTaskBefore(WorkbenchTask... tasks) {
    if (tasks != null) {
      Collections.addAll(beforeTasks, tasks);
    }
  }

  @Override
  public List<WorkbenchTask> getBeforeTasks() {
    return beforeTasks;
  }

  @Override
  public void addWorkbenchTaskAfter(WorkbenchTask... tasks) {
    if (tasks != null) {
      Collections.addAll(afterTasks, tasks);
    }
  }

  @Override
  public List<WorkbenchTask> getAfterTasks() {
    return afterTasks;
  }
}
