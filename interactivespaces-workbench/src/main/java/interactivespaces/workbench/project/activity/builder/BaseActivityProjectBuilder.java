/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.workbench.project.activity.builder;

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.builder.BaseProjectBuilder;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import java.io.File;
import java.util.Map;

/**
 * A base activity project builder which takes care of the portions of the build
 * needed by all types of activity projects.
 *
 * @author Keith M. Hughes
 */
public class BaseActivityProjectBuilder extends BaseProjectBuilder {

  @Override
  public boolean build(Project project, ProjectBuildContext context) {
    File stagingDirectory =
        new File(context.getBuildDirectory(), BUILD_STAGING_DIRECTORY);
    Files.directoryExists(stagingDirectory);

    if (onBuild(project, context, stagingDirectory)) {

      copyActivityResources(project, stagingDirectory);
      copyActivityXml(project, stagingDirectory, context);
      processResources(project, stagingDirectory, context);

      return true;
    } else {
      return false;
    }
  }

  /**
   * Copy all activity resources for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   */
  private void copyActivityResources(Project project, File stagingDirectory)
      throws InteractiveSpacesException {
    File activityDirectory =
        new File(project.getBaseDirectory(), ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
    if (!activityDirectory.exists()) {
      throw new InteractiveSpacesException(String.format("Activity directory %s does not exist",
          activityDirectory.getAbsolutePath()));
    }
    Files.copyDirectory(activityDirectory, stagingDirectory, true);
  }

  /**
   * Get the activity xml for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  private void copyActivityXml(Project project, File stagingDirectory, ProjectBuildContext context) {
    File activityXmlDest = new File(stagingDirectory, ActivityProject.FILENAME_ACTIVITY_XML);

    File activityXmlSrc =
        new File(project.getBaseDirectory(), ActivityProject.FILENAME_ACTIVITY_XML);
    if (activityXmlSrc.exists()) {
      Files.copyFile(activityXmlSrc, activityXmlDest);
    } else {
      Map<String, Object> templateData = Maps.newHashMap();
      templateData.put("project", project);
      templateData.put("activity", new ActivityProject(project));

      context.getWorkbench().getTemplater()
          .writeTemplate(templateData, activityXmlDest, "activity/activity.xml.ftl");
    }
  }

}
