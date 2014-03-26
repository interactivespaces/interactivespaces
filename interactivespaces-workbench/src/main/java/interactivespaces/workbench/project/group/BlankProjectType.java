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

package interactivespaces.workbench.project.group;

import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectTemplate;
import interactivespaces.workbench.project.activity.ide.EclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.activity.ide.NonJavaEclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.assembly.AssemblyProjectBuilder;
import interactivespaces.workbench.project.builder.ProjectBuilder;

/**
 * A project type for assembly projects.
 *
 * @author Trevor Pering
 */
public class BlankProjectType implements ProjectType {

  @Override
  public String getProjectTypeName() {
    return BlankProject.PROJECT_TYPE_NAME;
  }

  @Override
  public boolean isProperType(Project project) {
    return BlankProject.PROJECT_TYPE_NAME.equals(project.getType());
  }

  @Override
  public ProjectBuilder newBuilder() {
    return new AssemblyProjectBuilder();
  }

  @Override
  public ProjectTemplate newProjectTemplate() {
    return new BaseProjectTemplate();
  }

  @Override
  public EclipseIdeProjectCreatorSpecification getEclipseIdeProjectCreatorSpecification() {
    return new NonJavaEclipseIdeProjectCreatorSpecification();
  }
}
