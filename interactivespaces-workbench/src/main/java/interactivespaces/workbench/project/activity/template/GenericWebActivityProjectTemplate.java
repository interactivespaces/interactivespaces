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
import interactivespaces.activity.component.web.WebBrowserActivityComponent;
import interactivespaces.activity.component.web.WebServerActivityComponent;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import java.util.List;
import java.util.Map;

/**
 * A project creator for java projects.
 *
 * @author Keith M. Hughes
 */
public class GenericWebActivityProjectTemplate extends BaseActivityProjectTemplate {

  private static final String TEMPLATE_BASE = "activity/generic/web/simple/";

  {
    addFileTemplate(TEMPLATE_BASE + "SimpleWebActivity.java.ftl",
        "${project.getActivitySourceDirectory()}/${javaPackagePath}/${activityClassName}.java");
    addFileTemplate(TEMPLATE_BASE + "index.html.ftl",
        "${project.getActivityResourceDirectory()}/${webAppBasePath}/index.html");
    addFileTemplate(TEMPLATE_BASE + "SimpleWebActivity.js.ftl",
        "${project.getActivityResourceDirectory()}/${webAppBasePath}/js/${webAppFileBase}.js");
    addFileTemplate(TEMPLATE_BASE + "SimpleWebActivity.css.ftl",
        "${project.getActivityResourceDirectory()}/${webAppBasePath}/css/${webAppFileBase}.css");
  }

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
   * Construct a template.
   */
  public GenericWebActivityProjectTemplate() {
    super("Generic Simple Web Project", "web");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    String classBase = activityProject.getActivityRuntimeName();
    String webActivityNativeHostClass = classBase.substring(0, 1).toUpperCase() + classBase.substring(1);
    fullTemplateData.put("activityClassName", webActivityNativeHostClass);
    fullTemplateData.put("javaPackagePath", activityProject.getIdentifyingName().replace('.', '/'));
    fullTemplateData.put("webAppFileBase", activityProject.getIdentifyingName().replace('.', '-'));
    fullTemplateData.put("webAppBasePath", WEB_APP_BASE_WEB_PATH);

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
}
