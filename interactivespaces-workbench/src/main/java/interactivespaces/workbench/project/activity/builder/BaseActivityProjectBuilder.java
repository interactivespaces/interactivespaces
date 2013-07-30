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
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectResource;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.ProjectBuildContext;

import java.io.File;
import java.util.Map;

/**
 * A base activity project builder which takes care of the portions of the build
 * needed by all types of activity projects
 *
 * @author Keith M. Hughes
 */
public class BaseActivityProjectBuilder implements ProjectBuilder {

  /**
   * Subdirectory of build folder which contains the staged activity
   */
  public static final String ACTIVITY_BUILD_DIRECTORY_STAGING = "staging";

  @Override
  public boolean build(Project project, ProjectBuildContext context) {
    try {
      File stagingDirectory =
          new File(context.getBuildDirectory(), ACTIVITY_BUILD_DIRECTORY_STAGING);
      makeDirectory(stagingDirectory);

      if (onBuild(project, context, stagingDirectory)) {

        copyActivityResources(project, stagingDirectory);
        copyActivityXml(project, stagingDirectory, context);
        copyResources(project, stagingDirectory, context);

        return true;
      } else {
        return false;
      }
    } catch (SimpleInteractiveSpacesException e) {
      System.out.format("Error while building project: %s\n", e.getMessage());

      return false;
    } catch (InteractiveSpacesException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

      return false;
    }
  }

  /**
   * Build has begun. Do any specific parts of the build.
   *
   * @param project
   *          the project
   * @param context
   *          the build context
   * @param stagingDirectory
   *          the staging directory where build artifacts go
   */
  public boolean onBuild(Project project, ProjectBuildContext context, File stagingDirectory) {
    // Default is nothing
    return true;
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

  /**
   * Copy the needed resources for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  private void copyResources(Project project, File stagingDirectory, ProjectBuildContext context) {
    for (ProjectResource resource : project.getResources()) {
      copyResource(resource, project, stagingDirectory, context);
    }
  }

  /**
   * Copy the needed resource for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
   *          context for the build
   */
  private void copyResource(ProjectResource resource, Project project, File stagingDirectory,
      ProjectBuildContext context) {
    if (resource.getDestinationDirectory() != null) {
      File destDir = new File(stagingDirectory, resource.getDestinationDirectory());
      makeDirectory(destDir);

      if (resource.getSourceDirectory() != null) {
        String evaluate =
            context.getWorkbench().getWorkbenchConfig().evaluate(resource.getSourceDirectory());
        System.out.println(evaluate);
        File srcDir = new File(evaluate);
        Files.copyDirectory(srcDir, destDir, true);
      } else {
        // There is a file to be copied.
        File srcFile =
            new File(context.getWorkbench().getWorkbenchConfig().evaluate(resource.getSourceFile()));
        Files.copyFile(srcFile, new File(destDir, srcFile.getName()));
      }
    } else {
      // Have a dest file
      // There is a file to be copied.
      File destFile =
          new File(context.getWorkbench().getWorkbenchConfig()
              .evaluate(resource.getDestinationFile()));
      File srcFile =
          new File(context.getWorkbench().getWorkbenchConfig().evaluate(resource.getSourceFile()));
      Files.copyFile(srcFile, destFile);
    }
  }

  /**
   * Make sure a required directory exists. If it doesn't, create it.
   *
   * @param directory
   *          the directory to create
   */
  public void makeDirectory(File directory) {
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new InteractiveSpacesException(String.format("Cannot create directory %s",
            directory.getAbsolutePath()));
      }
    }
  }

}
