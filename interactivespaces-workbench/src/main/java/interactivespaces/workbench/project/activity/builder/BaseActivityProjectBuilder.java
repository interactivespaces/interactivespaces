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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.builder.BaseProjectBuilder;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;

/**
 * A base activity project builder which takes care of the portions of the build
 * needed by all types of activity projects.
 *
 * @author Keith M. Hughes
 */
public class BaseActivityProjectBuilder extends BaseProjectBuilder<ActivityProject> {

  /**
   * Path for the location of the activity.xml template.
   */
  public static final String ACTIVITY_XML_TEMPLATE_PATHNAME = "activity/activity.xml.ftl";

  /**
   * Path for the location of the activity.conf template.
   */
  public static final String ACTIVITY_CONF_TEMPLATE_PATHNAME = "activity/activity.conf.ftl";

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public boolean build(ActivityProject project, ProjectBuildContext context) {
    File stagingDirectory = new File(context.getBuildDirectory(), BUILD_STAGING_DIRECTORY);
    fileSupport.directoryExists(stagingDirectory);

    if (onBuild(project, context, stagingDirectory)) {
      copyActivityResources(project, stagingDirectory, context);
      handleActivityXml(project, stagingDirectory, context);
      handleActivityConf(project, stagingDirectory, context);
      processResources(project, stagingDirectory, context);
      writeResourceMap(project, stagingDirectory, context);

      processExtraConstituents(project, stagingDirectory, context);

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
   * @param context
   *          context for the build
   *
   * @throws InteractiveSpacesException
   *           if the activity resource directory does not exist
   */
  private void copyActivityResources(ActivityProject project, File stagingDirectory, ProjectBuildContext context)
      throws InteractiveSpacesException {
    File activityDirectory = new File(project.getBaseDirectory(), ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
    if (activityDirectory.exists()) {
      fileSupport.copyDirectory(activityDirectory, stagingDirectory, true, context.getResourceSourceMap());
    }
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
  private void handleActivityXml(ActivityProject project, File stagingDirectory, ProjectBuildContext context) {
    File activityXmlDest = new File(stagingDirectory, ActivityProject.FILENAME_ACTIVITY_XML);

    File activityXmlSrc = new File(project.getBaseDirectory(), ActivityProject.FILENAME_ACTIVITY_XML);
    if (activityXmlSrc.exists()) {
      fileSupport.copyFile(activityXmlSrc, activityXmlDest);
    } else {
      Map<String, Object> templateData = Maps.newHashMap();
      templateData.put("project", project);

      context.getWorkbench().getTemplater()
          .writeTemplate(templateData, activityXmlDest, ACTIVITY_XML_TEMPLATE_PATHNAME);
    }
  }

  /**
   * Handle the activity.conf generation, if needed.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          the staging directory
   * @param context
   *          the build context
   */
  private void handleActivityConf(ActivityProject project, File stagingDirectory, ProjectBuildContext context) {
    // If the activity conf exists, do not create another.
    File activityConfFile = project.getActivityConfigFile();
    if (activityConfFile.exists()) {
      context.getWorkbench().getLog().info("The project already has an activity.conf, so not generating a new one");
      return;
    }

    File generatedActivityConfFile = new File(stagingDirectory, ActivityProject.FILENAME_ACTIVITY_CONF);
    Map<String, Object> templateData = Maps.newHashMap();
    templateData.put("project", project);

    context
        .getWorkbench()
        .getTemplater()
        .writeTemplate(templateData, generatedActivityConfFile,
            BaseActivityProjectBuilder.ACTIVITY_CONF_TEMPLATE_PATHNAME);
  }

}
