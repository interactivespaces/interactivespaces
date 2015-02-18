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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Information about a project task and how it will be modified at runtime by other tasks.
 *
 * @author Keith M. Hughes
 */
public class WorkbenchTaskModifiers {

  /**
   * The name for the tasks to get these modifiers.
   */
  private String name;

  /**
   * The task factories for before tasks.
   */
  private List<WorkbenchTaskFactory> beforeTasks = Lists.newArrayList();

  /**
   * The task factories for after tasks.
   */
  private List<WorkbenchTaskFactory> afterTasks = Lists.newArrayList();

  /**
   * Construct a new item.
   *
   * @param name
   *          the name for the item
   */
  public WorkbenchTaskModifiers(String name) {
    this.name = name;
  }

  /**
   * Get the task name.
   *
   * @return the task name
   */
  public String getTaskName() {
    return name;
  }

  /**
   * Get all before tasks.
   *
   * @return the before tasks
   */
  public List<WorkbenchTaskFactory> getBeforeTasks() {
    return beforeTasks;
  }

  /**
   * Add a before task.
   *
   * @param factory
   *          the task factory to add
   */
  public void addBeforeTask(WorkbenchTaskFactory factory) {
    beforeTasks.add(factory);
  }

  /**
   * Get all after tasks.
   *
   * @return the after tasks
   */
  public List<WorkbenchTaskFactory> getAfterTasks() {
    return afterTasks;
  }

  /**
   * Add an after task.
   *
   * @param factory
   *          the task factory to add
   */
  public void addAfterTask(WorkbenchTaskFactory factory) {
    afterTasks.add(factory);
  }

  @Override
  public String toString() {
    return "ProjectTaskEntry [name=" + name + ", beforeTasks=" + beforeTasks + ", afterTasks=" + afterTasks + "]";
  }
}
