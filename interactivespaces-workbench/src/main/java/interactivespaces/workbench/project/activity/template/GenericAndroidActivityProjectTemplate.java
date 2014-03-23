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

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import com.google.common.collect.Lists;
import interactivespaces.workbench.project.java.JavaProjectType;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A project creator for Android projects.
 *
 * @author Keith M. Hughes
 */
public class GenericAndroidActivityProjectTemplate extends BaseActivityProjectTemplate {

  /**
   * Java classname for the activity.
   */
  public static final String ACTIVITY_CLASSNAME = "SimpleAndroidActivity";

  /**
   * Filename for the activity.
   */
  public static final String ACTIVITY_FILENAME = ACTIVITY_CLASSNAME + ".java";

  /**
   * Pathname to the template.
   */
  private static final String TEMPLATE_PATHNAME = "activity/generic/android/simple/" + ACTIVITY_FILENAME + ".ftl";

  /**
   * Construct the template.
   */
  public GenericAndroidActivityProjectTemplate() {
    super("Generic Simple Android Project", "android");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    activityProject.setBuilderType("android");

    activityProject.setActivityType("interactivespaces_native");

    activityProject.setActivityExecutable(activityProject.getIdentifyingName() + "-" + activityProject.getVersion()
        + ".jar");
    activityProject.setActivityClass(activityProject.getIdentifyingName() + "." + ACTIVITY_CLASSNAME);

    List<ProjectConfigurationProperty> configurationProperties = Lists.newArrayList();
    configurationProperties.add(new ProjectConfigurationProperty("space.activity.log.level",
        InteractiveSpacesEnvironment.LOG_LEVEL_INFO, false, null
    ));

    activityProject.addConfigurationProperties(configurationProperties);
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
    Project project = spec.getProject();

    // TODO(keith): Fix this when start supporting Windoze
    String pathname = project.getIdentifyingName().replace('.', '/');
    File sourceDirectory = new File(project.getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA + pathname);
    makeDirectory(sourceDirectory);

    templater.writeTemplate(fullTemplateData, new File(sourceDirectory, ACTIVITY_FILENAME), TEMPLATE_PATHNAME);
  }
}
