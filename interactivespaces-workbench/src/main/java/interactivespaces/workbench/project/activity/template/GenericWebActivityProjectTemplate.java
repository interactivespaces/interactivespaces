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

import interactivespaces.activity.component.web.WebBrowserActivityComponent;
import interactivespaces.activity.component.web.WebServerActivityComponent;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import java.io.File;
import java.util.Map;

/**
 * A project creator for web projects.
 *
 * @author Trevor Pering
 */
public class GenericWebActivityProjectTemplate extends BaseNativeActivityProjectTemplate {

  /**
   * Template input base for all web templates.
   */
  private static final String TEMPLATE_BASE = "activity/generic/web/simple/";

  /**
   * Add the set of file templates for this project.
   */
  {
    addFileTemplate("${activityJavaDir}/${activityClassName}.java", TEMPLATE_BASE + "SimpleWebActivity.java.ftl");
    addFileTemplate("${webAppBaseDir}/index.html", TEMPLATE_BASE + "index.html.ftl");
    addFileTemplate("${webAppBaseDir}/js/${webAppFileBase}.js", TEMPLATE_BASE + "SimpleWebActivity.js.ftl");
    addFileTemplate("${webAppBaseDir}/css/${webAppFileBase}.css", TEMPLATE_BASE + "SimpleWebActivity.css.ftl");
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

    activityProject.setActivityClass(activityProject.getIdentifyingName() + "." + webActivityNativeHostClass);
    super.onTemplateSetup(spec, activityProject, fullTemplateData);

    fullTemplateData.put("webAppBaseDir",
        fullTemplateData.get("activityResourceDir") + File.separator + WEB_APP_BASE_WEB_PATH);
    fullTemplateData.put("webAppFileBase", activityProject.getIdentifyingName().replace('.', '-'));

    activityProject.addConfigurationProperty(new ProjectConfigurationProperty(
        WebServerActivityComponent.CONFIGURATION_WEBAPP_CONTENT_LOCATION, WEB_APP_BASE_WEB_PATH, false, null));
    activityProject.addConfigurationProperty(new ProjectConfigurationProperty(
        WebServerActivityComponent.CONFIGURATION_WEBAPP_WEB_SERVER_PORT, WEB_APP_DEFAULT_PORT, false, null));
    activityProject.addConfigurationProperty(new ProjectConfigurationProperty(
        WebBrowserActivityComponent.CONFIGURATION_INITIAL_PAGE, WEB_APP_INITIAL_PAGE, false, null));
  }
}
