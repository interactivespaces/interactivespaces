/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.workbench.project.activity.type;

import com.google.common.collect.Lists;

import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.type.android.AndroidActivityProjectType;
import interactivespaces.workbench.project.activity.type.java.JavaActivityProjectType;
import interactivespaces.workbench.project.assembly.AssemblyProjectType;
import interactivespaces.workbench.project.library.LibraryProjectType;

import java.util.List;

/**
 * A simple implemention of a {@link ProjectTypeRegistry}.
 *
 * @author Keith M. Hughes
 */
public class SimpleProjectTypeRegistery implements ProjectTypeRegistry {

  /**
   * The mapping of names to project types.
   */
  private List<ProjectType> types = Lists.newArrayList();

  /**
   * Create a basic registery for project types.
   */
  public SimpleProjectTypeRegistery() {
    registerProjectType(new JavaActivityProjectType());
    registerProjectType(new AndroidActivityProjectType());
    registerProjectType(new LibraryProjectType());
    registerProjectType(new AssemblyProjectType());
  }

  @Override
  public ProjectType getProjectType(Project project) {
    for (ProjectType type : types) {
      if (type.isProperType(project)) {
        return type;
      }
    }

    return null;
  }

  @Override
  public void registerProjectType(ProjectType type) {
    types.add(type);
  }
}
