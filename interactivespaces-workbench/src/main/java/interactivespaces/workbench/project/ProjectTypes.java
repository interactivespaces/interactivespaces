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

package interactivespaces.workbench.project;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.confederate.OverviewProject;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.assembly.AssemblyProject;
import interactivespaces.workbench.project.library.LibraryProject;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A registry of project types.
 *
 * @author Keith M. Hughes
 */
public final class ProjectTypes {

  /**
   * The map of project types.
   */
  private static final Map<String, Class<? extends Project>> PROJECT_TYPES;

  static {
    PROJECT_TYPES = Maps.newHashMap();
    PROJECT_TYPES.put(ActivityProject.PROJECT_TYPE_NAME, ActivityProject.class);
    PROJECT_TYPES.put(LibraryProject.PROJECT_TYPE_NAME, LibraryProject.class);
    PROJECT_TYPES.put(AssemblyProject.PROJECT_TYPE_NAME, AssemblyProject.class);
    PROJECT_TYPES.put(OverviewProject.PROJECT_TYPE_NAME, OverviewProject.class);
  }

  private ProjectTypes() {
  }

  /**
   * Get an instance of a project associated with a given type name.
   *
   * @param typeName
   *          the type name
   * @param <T>
   *          project type for the project, e.g. LibraryProject, ActivityProject
   *
   * @return a newly constructed project of the appropriate type.
   *
   * @throws InteractiveSpacesException
   *           either couldn't create the requested class or unknown project
   *           type
   */
  public static <T extends Project> T newProject(String typeName) throws InteractiveSpacesException {
    Class<? extends Project> clazz = PROJECT_TYPES.get(typeName);
    if (clazz != null) {
      try {
        @SuppressWarnings("unchecked")
        T project = (T) clazz.newInstance();
        project.setType(typeName);

        return project;
      } catch (Exception e) {
        throw new SimpleInteractiveSpacesException(String.format("Could not create project class with type name %s",
            typeName), e);
      }
    } else {
      throw new SimpleInteractiveSpacesException(String.format("Unknown project type name %s", typeName));
    }
  }
}
