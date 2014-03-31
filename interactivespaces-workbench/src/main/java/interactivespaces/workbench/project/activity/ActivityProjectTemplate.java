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

package interactivespaces.workbench.project.activity;

import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.creator.ProjectCreationContext;
import interactivespaces.workbench.project.java.JavaProjectType;

import java.io.File;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public class ActivityProjectTemplate extends BaseProjectTemplate {

  @Override
  protected void onTemplateSetup(ProjectCreationContext context) {
    context.addTemplateDataEntry("activity", context.getProject());
  }

  /**
   * Get the activity source directory.
   *
   * @param context
   *          specification for the build
   *
   * @return the source directory for activity sources
   */
  public File getActivitySourceDirectory(ProjectCreationContext context) {
    return new File(context.getProject().getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
  }

  /**
   * Get the activity resource directory.
   *
   * @param context
   *          specification for the build
   *
   * @return the resource directory for activity components
   */
  public File getActivityResourceDirectory(ProjectCreationContext context) {
    return new File(context.getProject().getBaseDirectory(), ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
  }
}
