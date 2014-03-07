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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.ProjectTemplate;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.type.android.GenericAndroidActivityProjectTemplate;
import interactivespaces.workbench.project.activity.type.java.GenericJavaActivityProjectTemplate;

import java.io.File;
import java.util.Map;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivityProjectTemplate extends BaseProjectTemplate {

  /**
   * The activity project type.
   */
  public static final String PROJECT_TYPE = "activity";

  /**
   * Construct the template.
   *
   * @param displayName
   *          display name for the template
   */
  public BaseActivityProjectTemplate(String displayName) {
    super(displayName);

    addSourceDirectory(ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
  }

  /**
   * Get a generic project template by language.
   *
   * @param language
   *          the language
   *
   * @return the generic template for that language
   */
  static BaseActivityProjectTemplate getActivityProjectTemplateByLanguage(String language) {
    if ("java".equals(language)) {
      return new GenericJavaActivityProjectTemplate();
    } else if ("python".equals(language)) {
      return new GenericPythonActivityProjectTemplate();
    } else if ("javascript".equals(language)) {
      return new GenericJavascriptActivityProjectTemplate();
    } else if ("android".equals(language)) {
      return new GenericAndroidActivityProjectTemplate();
    } else {
      throw new InteractiveSpacesException(String.format("Unknown language %s", language));
    }
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, Map<String, Object> fullTemplateData) {
    ActivityProject activityProject = spec.getProject();
    fullTemplateData.put("activity", activityProject);

    // Create an activity name from the identifying name
    String identifyingName = spec.getProject().getIdentifyingName();
    String[] parts = identifyingName.split("\\.");
    StringBuilder activityRuntimeName = new StringBuilder(parts[0]);

    for (int i = 1; i < parts.length; ++i) {
      String part = parts[i];
      activityRuntimeName.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
    }

    activityProject.setActivityRuntimeName(activityRuntimeName.toString());

    onTemplateSetup(spec, activityProject, fullTemplateData);
  }

  /**
   * Template is being set up.
   *
   * @param spec
   *          spec for the project
   * @param activityProject
   *          the activity part of the project
   * @param fullTemplateData
   *          the template data to be handed to this template
   */
  public abstract void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData);

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
