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

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A project creator for java projects.
 *
 * @author Keith M. Hughes
 */
public class GenericJavascriptActivityProjectTemplate extends BaseActivityProjectTemplate {
  /**
   * The filename for the activity executable name.
   */
  public static final String ACTIVITY_EXECUTABLE_FILENAME = "SimpleJavascriptActivity.js";

  /**
   * Pathname to the template.
   */
  public static final String TEMPLATE_PATHNAME = "activity/generic/javascript/simple/" + ACTIVITY_EXECUTABLE_FILENAME
      + ".ftl";

  /**
   * Construct a template.
   */
  public GenericJavascriptActivityProjectTemplate() {
    super("Generic Simple Javascript Project", "javascript");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    activityProject.setActivityType("script");

    activityProject.setActivityExecutable(ACTIVITY_EXECUTABLE_FILENAME);

    List<ProjectConfigurationProperty> configurationProperties = Lists.newArrayList();
    configurationProperties.add(new ProjectConfigurationProperty("space.activity.log.level", null, false,
        InteractiveSpacesEnvironment.LOG_LEVEL_INFO));

    activityProject.setConfigurationProperties(configurationProperties);
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
    templater.writeTemplate(fullTemplateData,
        new File(getActivityResourceDirectory(spec), ACTIVITY_EXECUTABLE_FILENAME), TEMPLATE_PATHNAME);
  }
}
