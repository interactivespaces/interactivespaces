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

package interactivespaces.workbench.project.activity.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.ProjectTemplate;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.java.JavaProjectType;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivityProjectTemplate extends BaseProjectTemplate {

  /**
   * Static map of all template types, by language.
   */
  private static final Map<String, BaseActivityProjectTemplate> PROJECT_TEMPLATES = Maps.newHashMap();

  /**
   * Project templates stored as an unmodifiable list.
   */
  private static final List<ProjectTemplate> PROJECT_TEMPLATE_LIST;

  /**
   * Initializer block for all activity project templates.
   */
  static {
    addProjectTemplate(new BaseNativeActivityProjectTemplate());
    addProjectTemplate(new GenericJavaActivityProjectTemplate());
    addProjectTemplate(new GenericJavascriptActivityProjectTemplate());
    addProjectTemplate(new GenericWebActivityProjectTemplate());
    addProjectTemplate(new GenericPythonActivityProjectTemplate());
    addProjectTemplate(new GenericAndroidActivityProjectTemplate());

    List<ProjectTemplate> intermediateList = Lists.newArrayList();
    intermediateList.addAll(PROJECT_TEMPLATES.values());
    PROJECT_TEMPLATE_LIST = Collections.unmodifiableList(intermediateList);
  }

  /**
   * Get a generic project template by language.
   *
   * @param language
   *          the language
   *
   * @return the generic template for that language
   */
  public static BaseActivityProjectTemplate getActivityProjectTemplateByLanguage(String language) {
    if (!PROJECT_TEMPLATES.containsKey(language)) {
      throw new InteractiveSpacesException(String.format("Unknown language %s", language));
    }
    return PROJECT_TEMPLATES.get(language);
  }

  /**
   * @return unmodifiable list of all the supported project templates
   */
  public static List<ProjectTemplate> getProjectTemplates() {
    return PROJECT_TEMPLATE_LIST;
  }

  /**
   * Language supported by this activity project template.
   */
  private final String language;

  /**
   * Construct the template.
   *
   * @param displayName
   *          display name for the template
   * @param language
   *          language for this activity template
   */
  public BaseActivityProjectTemplate(String displayName, String language) {
    super(displayName);

    this.language = language;

    addSourceDirectory(ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
  }

  /**
   * Add a project template to the static map, indexed by language.
   *
   * @param projectTemplate
   *          template to add
   */
  private static void addProjectTemplate(BaseActivityProjectTemplate projectTemplate) {
    PROJECT_TEMPLATES.put(projectTemplate.getLanguage(), projectTemplate);
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    ActivityProject activityProject = spec.getProject();
    fullTemplateData.put("activity", activityProject);

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

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec,
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

  /**
   * @return language supported by this project template
   */
  public String getLanguage() {
    return language;
  }
}
