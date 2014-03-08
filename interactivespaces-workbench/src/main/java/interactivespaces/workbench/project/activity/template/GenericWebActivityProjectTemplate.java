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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import interactivespaces.activity.component.web.WebBrowserActivityComponent;
import interactivespaces.activity.component.web.WebServerActivityComponent;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
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

  /**
   * Map of file/template pairs to add to the template.
   */
  public static final Map<String, String> RESOURCE_TEMPLATE_MAP = ImmutableMap.of(
      "activity/generic/web/simple/index.html.ftl", "%s/index.html",
      "activity/generic/web/simple/SimpleWebActivity.js.ftl", "%s/js/%s.js",
      "activity/generic/web/simple/SimpleWebActivity.css.ftl", "%s/css/%s.css"
  );

  /**
   * Map representing  main source activity file(s).
   */
  public static final Map<String, String> SRC_TEMPLATE_MAP = ImmutableMap.of(
      "activity/generic/web/simple/SimpleWebActivity.java.ftl", "%s/%s.java)");

  /**
   * Subdirectory used to hold all web-app runtime files.
   */
  public static final String WEB_APP_BASE_WEB_PATH = "webapp";

  /**
   * Initial configured port of the web-app server.
   */
  public static final String WEB_APP_DEFAULT_PORT = "9090";

  /**
   * Initial page of web-app html.
   */
  public static final String WEB_APP_INITIAL_PAGE = "index.html";

  /**
   * The native host class for the web-app server.
   */
  private String webActivityNativeHostClass;

  /**
   * The web-app filename base used for naming web-app files.
   */
  private String webActivityFileNameBase;

  /**
   * Construct a template.
   */
  public GenericWebActivityProjectTemplate() {
    super("Generic Simple Web Project", "web");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    String classBase = activityProject.getActivityRuntimeName();
    webActivityNativeHostClass = classBase.substring(0, 1).toUpperCase() + classBase.substring(1);
    fullTemplateData.put("activityClassName", webActivityNativeHostClass);
    webActivityFileNameBase = activityProject.getIdentifyingName().replace('.', '-');
    fullTemplateData.put("webAppFileBase", webActivityFileNameBase);

    activityProject.setBuilderType("java");

    activityProject.setActivityType("interactivespaces_native");
    activityProject.setActivityClass(activityProject.getIdentifyingName() + "." + webActivityNativeHostClass);

    List<ProjectConfigurationProperty> configurationProperties = Lists.newArrayList();
    configurationProperties.add(new ProjectConfigurationProperty(
        "space.activity.log.level", null, false, InteractiveSpacesEnvironment.LOG_LEVEL_INFO));
    configurationProperties.add(new ProjectConfigurationProperty(
        WebServerActivityComponent.CONFIGURATION_WEBAPP_CONTENT_LOCATION, null, false, WEB_APP_BASE_WEB_PATH));
    configurationProperties.add(new ProjectConfigurationProperty(
        WebServerActivityComponent.CONFIGURATION_WEBAPP_WEB_SERVER_PORT, null, false, WEB_APP_DEFAULT_PORT));
    configurationProperties.add(new ProjectConfigurationProperty(
        WebBrowserActivityComponent.CONFIGURATION_INITIAL_PAGE, null, false, WEB_APP_INITIAL_PAGE));

    activityProject.setConfigurationProperties(configurationProperties);
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
    for (Map.Entry<String, String> template : RESOURCE_TEMPLATE_MAP.entrySet()) {
      String outName = String.format(template.getValue(), WEB_APP_BASE_WEB_PATH, webActivityFileNameBase);
      templater.writeTemplate(fullTemplateData,
          new File(getActivityResourceDirectory(spec), outName), template.getKey());
    }

    Project project = spec.getProject();
    String packagePath = project.getIdentifyingName().replace('.', '/');
    for (Map.Entry<String, String> template : SRC_TEMPLATE_MAP.entrySet()) {
      String outName = String.format(template.getValue(), packagePath, webActivityNativeHostClass);
      templater.writeTemplate(fullTemplateData,
          new File(getActivitySourceDirectory(spec), outName), template.getKey());
    }
  }
}
