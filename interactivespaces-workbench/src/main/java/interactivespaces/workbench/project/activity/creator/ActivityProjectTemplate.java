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

package interactivespaces.workbench.project.activity.creator;

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.CreationSpecification;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.java.JavaProjectType;

import java.io.File;
import java.util.Map;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public class ActivityProjectTemplate extends BaseProjectTemplate {

  /**
   * Construct the template.
   *
   *
   */
  public ActivityProjectTemplate() {
    super("Activity Project Template");

    addSourceDirectory(ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
  }

  @Override
  protected void onTemplateSetup(CreationSpecification spec, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    Project project = ((ProjectCreationSpecification) spec).getProject();
    fullTemplateData.put("activity", project);
    super.onTemplateSetup(spec, templater, fullTemplateData);
  }

  @Override
  public void writeSpecificTemplates(CreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    // Do nothing by default -- only use common templates.
  }

  /**
   * Get the activity source directory.
   *
   * @param spec
   *          specification for the build
   *
   * @return the source directory for activity sources
   */
  public File getActivitySourceDirectory(ProjectCreationSpecification spec) {
    return new File(spec.getProject().getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
  }

  /**
   * Get the activity resource directory.
   *
   * @param spec
   *          specification for the build
   *
   * @return the resource directory for activity components
   */
  public File getActivityResourceDirectory(ProjectCreationSpecification spec) {
    return new File(spec.getProject().getBaseDirectory(), ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
  }
}
