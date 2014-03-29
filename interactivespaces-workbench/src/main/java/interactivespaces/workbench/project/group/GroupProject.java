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

package interactivespaces.workbench.project.group;

import com.google.common.collect.Lists;
import interactivespaces.workbench.project.Project;

import java.io.File;
import java.util.List;

/**
 * A group of projects.
 *
 * @author Trevor Pering
 */
public class GroupProject extends Project {

  /**
   * Name of the project type.
   */
  public static final String PROJECT_TYPE_NAME = "group";

  /**
   * List of projects in this group.
   */
  private final List<Project> projectList = Lists.newArrayList();

  /**
   * Get the project list.
   *
   * @return list of projects in this group
   */
  public List<Project> getProjectList() {
    return projectList;
  }

  /**
   * Add a project to the group.
   *
   * @param project
   *          project to add
   */
  public void addProject(Project project) {
    projectList.add(project);
  }

}
