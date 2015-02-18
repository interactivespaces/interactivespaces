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
package interactivespaces.workbench.project.tasks;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.process.NativeApplicationDescription;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.ProjectTaskContext;
import interactivespaces.workbench.project.StandardProjectTaskNames;
import interactivespaces.workbench.project.constituent.BaseProjectConstituent;
import interactivespaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.tasks.NativeWorkbenchTaskFactory;
import interactivespaces.workbench.tasks.WorkbenchTaskContext;
import interactivespaces.workbench.tasks.WorkbenchTaskFactory;
import interactivespaces.workbench.tasks.WorkbenchTaskModifiers;

import com.google.common.collect.Maps;

import org.jdom.Element;
import org.jdom.Namespace;

import java.util.List;
import java.util.Map;

/**
 * A project constituent for handling the tasks section of a project file.
 *
 * @author Keith M. Hughes
 */
public class TasksProjectConstituent extends BaseProjectConstituent {

  /**
   * Element type for the resource.
   */
  public static final String TYPE_NAME = "tasks";

  /**
   * Element name for tasks.
   */
  public static final String ELEMENT_NAME_TASKS = "tasks";

  /**
   * Element name for an individual task.
   */
  public static final String ELEMENT_NAME_TASK = "task";

  /**
   * Element name for a task name.
   */
  public static final String ELEMENT_NAME_TASK_NAME = "name";

  /**
   * Element name for a task position.
   */
  public static final String ELEMENT_NAME_TASK_POSITION = "position";

  /**
   * The element attribute value for the task position being after the current task.
   */
  public static final String ELEMENT_ATTRIBUTE_VALUE_TASK_POSITION_AFTER = "after";

  /**
   * The element attribute value for the task position being before the current task.
   */
  public static final String ELEMENT_ATTRIBUTE_VALUE_TASK_POSITION_BEFORE = "before";

  /**
   * Element name for a task type.
   */
  public static final String ELEMENT_NAME_TASK_TYPE = "type";

  /**
   * The task type for a native task.
   */
  public static final String TASK_TYPE_NATIVE = "native";

  /**
   * The name of the executable path element for a native task type.
   */
  public static final String TASK_TYPE_NATIVE_EXECUTABLE_PATH = "executable-path";

  /**
   * The name of the argument element for a native task type.
   */
  public static final String TASK_TYPE_NATIVE_ARG = "arg";

  /**
   * The project task entries indexed by task name.
   */
  private Map<String, WorkbenchTaskModifiers> projectTaskModifiers = Maps.newTreeMap();

  @Override
  public void processConstituent(Project project, ProjectContext context) {
    if (!(context instanceof ProjectTaskContext)) {
      SimpleInteractiveSpacesException.throwFormattedException("Projects using context type %s cannot have tasks",
          context.getClass().getName());
      return;
    }

    ProjectTaskContext taskContext = (ProjectTaskContext) context;

    if (!StandardProjectTaskNames.TASK_NAME_PRE.equals(taskContext.getCurrentTaskName())) {
      return;
    }

    taskContext.modifyProjectTasks(projectTaskModifiers.values());
  }

  /**
   * Add a task factory to a name.
   *
   * @param name
   *          the name
   * @param position
   *          the position of the task
   * @param factory
   *          the factory
   */
  public void addTaskFactory(String name, String position, WorkbenchTaskFactory factory) {
    WorkbenchTaskModifiers taskEntry = projectTaskModifiers.get(name);
    if (taskEntry == null) {
      taskEntry = new WorkbenchTaskModifiers(name);
      projectTaskModifiers.put(name, taskEntry);
    }

    if (ELEMENT_ATTRIBUTE_VALUE_TASK_POSITION_BEFORE.equals(position)) {
      taskEntry.addBeforeTask(factory);
    } else if (ELEMENT_ATTRIBUTE_VALUE_TASK_POSITION_AFTER.equals(position)) {
      taskEntry.addAfterTask(factory);
    }
  }

  /**
   * Factory for building the constituent builder.
   *
   * @author Keith M. Hughes
   */
  public static class TasksProjectConstituentBuilderFactory implements ProjectConstituentBuilderFactory {

    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectTasksBuilder newBuilder() {
      return new ProjectTasksBuilder();
    }
  }

  /**
   * Builder class for creating new tasks instances.
   */
  private static class ProjectTasksBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent
        buildConstituentFromElement(Namespace namespace, Element resourceElement, Project project) {
      TasksProjectConstituent resource = new TasksProjectConstituent();

      @SuppressWarnings("unchecked")
      List<Element> taskElements = resourceElement.getChildren(ELEMENT_NAME_TASK, namespace);
      for (Element taskElement : taskElements) {
        if (hasErrors()) {
          break;
        }

        String name = taskElement.getAttributeValue(ELEMENT_NAME_TASK_NAME, namespace);
        String position = taskElement.getAttributeValue(ELEMENT_NAME_TASK_POSITION, namespace);
        String type = taskElement.getAttributeValue(ELEMENT_NAME_TASK_TYPE, namespace);

        // TODO(keith): add in a factory for these readers once

        if (TASK_TYPE_NATIVE.equals(type)) {
          Element nativeElement = taskElement.getChild(TASK_TYPE_NATIVE, namespace);

          NativeApplicationDescription nativeDescription = new NativeApplicationDescription();

          String executablePath = nativeElement.getChildTextTrim(TASK_TYPE_NATIVE_EXECUTABLE_PATH, namespace);
          if (executablePath != null && !executablePath.isEmpty()) {
            nativeDescription.setExecutablePath(executablePath);
          } else {
            addError("There is no executable-path element");
          }

          @SuppressWarnings("unchecked")
          List<Element> argumentElements = nativeElement.getChildren(TASK_TYPE_NATIVE_ARG, namespace);
          for (Element argumentElement : argumentElements) {
            String argument = argumentElement.getTextTrim();
            if (argument != null && !argument.isEmpty()) {
              nativeDescription.addArguments(argument);
            }
          }

          resource.addTaskFactory(name, position, new ProjectNativeWorkbenchTaskFactory(project, nativeDescription));
        } else {
          addError(String.format("Unknown task type %s", type));
        }
      }

      if (hasErrors()) {
        return null;
      } else {
        return resource;
      }
    }
  }

  /**
   * A factory for native tasks that uses the project configuration.
   *
   * @author Keith M. Hughes
   */
  public static class ProjectNativeWorkbenchTaskFactory extends NativeWorkbenchTaskFactory {

    /**
     * The project this factory is part of.
     */
    private Project project;

    /**
     * Construct a new factory.
     *
     * @param project
     *          the project
     * @param nativeDescription
     *          the description of the native application
     */
    public ProjectNativeWorkbenchTaskFactory(Project project, NativeApplicationDescription nativeDescription) {
      super(nativeDescription);

      this.project = project;
    }

    @Override
    protected Configuration getConfiguration(WorkbenchTaskContext workbenchTaskContext) {
      return project.getConfiguration();
    }
  }
}
