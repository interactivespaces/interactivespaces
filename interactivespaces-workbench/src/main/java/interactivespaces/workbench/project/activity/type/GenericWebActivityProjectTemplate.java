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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A project creator for java projects.
 *
 * @author Keith M. Hughes
 */
public class GenericWebActivityProjectTemplate extends BaseActivityProjectTemplate {

  public static final Map<String, String> TEMPLATE_MAP = ImmutableMap.of(
      "activity/generic/web/simple/index.html.ftl", "webapp/index.html",
      "activity/generic/web/simple/SimpleWebActivity.js.ftl", "webapp/js/%s.js",
      "activity/generic/web/simple/SimpleWebActivity.css.ftl", "webapp/css/%s.css"
  );

  /**
   * Construct a template.
   */
  public GenericWebActivityProjectTemplate() {
    super("Generic Simple Web Project", "web");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    activityProject.setBuilderType("java");

    activityProject.setActivityType("interactivespaces_native");
    activityProject.setActivityClass(activityProject.getIdentifyingName() + ".WebAppClassname");

    List<ProjectConfigurationProperty> configurationProperties = Lists.newArrayList();
    configurationProperties.add(new ProjectConfigurationProperty("space.activity.log.level", null, false,
        InteractiveSpacesEnvironment.LOG_LEVEL_INFO));

    activityProject.setConfigurationProperties(configurationProperties);
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
    String baseFileName = "foo-bar";
    for (Map.Entry<String, String> template : TEMPLATE_MAP.entrySet()) {
      String outName = String.format(template.getValue(), baseFileName);
      templater.writeTemplate(fullTemplateData,
          new File(getActivityResourceDirectory(spec), outName), template.getKey());
    }
  }
}
