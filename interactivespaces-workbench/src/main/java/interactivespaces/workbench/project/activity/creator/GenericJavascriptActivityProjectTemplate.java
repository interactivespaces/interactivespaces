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

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.ProjectCreationSpecification;

import java.io.File;
import java.util.Map;

/**
 * A project creator for java projects.
 *
 * @author Keith M. Hughes
 */
public class GenericJavascriptActivityProjectTemplate extends BaseActivityProjectTemplate {

  public GenericJavascriptActivityProjectTemplate() {
    super("Generic Simple Javascript Project");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    activityProject.setActivityType("script");

    spec.setExecutable("SimpleJavascriptActivity.js");
    spec.addExtraConfigurationParameter("space.activity.log.level",
        InteractiveSpacesEnvironment.LOG_LEVEL_INFO);
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    templater.writeTemplate(fullTemplateData, new File(getActivityResourceDirectory(spec),
        "SimpleJavascriptActivity.js"),
        "activity/generic/javascript/simple/SimpleJavascriptActivity.js.ftl");
  }
}
