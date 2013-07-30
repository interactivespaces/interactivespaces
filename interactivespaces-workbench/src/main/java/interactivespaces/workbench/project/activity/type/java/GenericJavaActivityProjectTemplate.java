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

package interactivespaces.workbench.project.activity.type.java;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.creator.BaseActivityProjectTemplate;

import java.io.File;
import java.util.Map;

/**
 * A project creator for java projects.
 *
 * @author Keith M. Hughes
 */
public class GenericJavaActivityProjectTemplate extends BaseActivityProjectTemplate {

  public GenericJavaActivityProjectTemplate() {
    super("Generic Simple Java Project");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {
    Project project = spec.getProject();
    project.setBuilderType("java");

    activityProject.setActivityType("interactivespaces_native");

    spec.setExecutable(project.getIdentifyingName() + "-" + project.getVersion() + ".jar");
    spec.addExtraConfigurationParameter("space.activity.log.level",
        InteractiveSpacesEnvironment.LOG_LEVEL_INFO);
    spec.addExtraConfigurationParameter("space.activity.java.class", project.getIdentifyingName()
        + ".SimpleJavaActivity");
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    Project project = spec.getProject();

    // TODO(keith): Fix this when start supporting Windoze
    String pathname = project.getIdentifyingName().replace('.', '/');
    File sourceDirectory = new File(project.getBaseDirectory(), "src/main/java/" + pathname);
    makeDirectory(sourceDirectory);

    templater.writeTemplate(fullTemplateData, new File(sourceDirectory, "SimpleJavaActivity.java"),
        "activity/generic/java/simple/SimpleJavaActivity.java.ftl");
  }
}
