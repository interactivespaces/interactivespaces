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

package interactivespaces.workbench.project;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.resource.Version;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.jdom.JdomProjectReader;
import interactivespaces.workbench.project.source.SimpleSource;
import interactivespaces.workbench.project.source.Source;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileInputStream;

/**
 * A basic {@link ProjectManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardProjectManager implements ProjectManager {

  /**
   * Name of a project file.
   */
  public static final String FILE_NAME_PROJECT = "project.xml";

  /**
   * Name of an activity file.
   */
  public static final String FILE_NAME_ACTIVITY = "activity.xml";

  /**
   * The workbench being used.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * File support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct the manager.
   *
   * @param workbench
   *          the workbench to use
   */
  public StandardProjectManager(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  @Override
  public boolean isProjectFolder(File baseDir) {
    File projectFile = getProjectFile(baseDir);
    if (projectFile.exists()) {
      return true;
    }
    return getActivityProjectFile(baseDir).exists();
  }

  @Override
  public Project readProject(File baseProjectDir, Log log) {
    File projectFile = getProjectFile(baseProjectDir);
    if (projectFile.exists()) {
      return readProjectFile(projectFile, log);
    }

    File activityFile = getActivityProjectFile(baseProjectDir);
    if (activityFile.exists()) {
      return convertActivity(activityFile);
    }

    throw new SimpleInteractiveSpacesException(String.format(
        "The folder %s does not contain any legal Interactive Spaces project files", baseProjectDir.getAbsolutePath()));
  }

  /**
   * Read a project file.
   *
   * @param projectFile
   *          the project file
   * @param log
   *          logger for reading the file
   *
   * @return the project
   */
  public Project readProjectFile(File projectFile, Log log) {
    JdomProjectReader jdomProjectReader = new JdomProjectReader(workbench);
    Project project = jdomProjectReader.readProject(projectFile);
    postProcessProject(project);

    return project;
  }

  /**
   * Do any post processing on the project.
   *
   * @param project
   *          the project to be processed
   */
  private void postProcessProject(Project project) {
    project.getConfiguration().setParent(workbench.getWorkbenchConfig());
  }

  /**
   * Get an activity file and convert it to a project.
   *
   * @param activityFile
   *          the activity file
   *
   * @return the project
   */
  private Project convertActivity(File activityFile) {
    ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
    FileInputStream activityDescriptionStream = null;
    try {
      activityDescriptionStream = new FileInputStream(activityFile);
      ActivityDescription activity = reader.readDescription(activityDescriptionStream);

      Project project = new ActivityProject();
      project.setBaseDirectory(activityFile.getParentFile());
      project.setName(activity.getName());
      project.setDescription(activity.getDescription());
      project.setBuilderType(activity.getBuilderType());
      project.setIdentifyingName(activity.getIdentifyingName());
      project.setVersion(Version.parseVersion(activity.getVersion()));
      project.setType(ActivityProject.PROJECT_TYPE_NAME);

      postProcessProject(project);

      return project;
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Cannot read activity description file %s",
          activityFile.getAbsolutePath()), e);
    }
  }

  @Override
  public Source getProjectXmlSource(Project project) {
    Source source = new SimpleSource();
    File sourceFile = getProjectFile(project.getBaseDirectory());
    source.setPath(sourceFile.getAbsolutePath());
    source.setProject(project);
    source.setContent(fileSupport.readFile(sourceFile));

    return source;
  }

  @Override
  public void saveSource(Source source) {
    fileSupport.writeFile(new File(source.getPath()), source.getContent());
  }

  /**
   * Get the project file.
   *
   * @param baseDir
   *          the project base directory
   *
   * @return the file for the project file
   */
  private File getProjectFile(File baseDir) {
    return fileSupport.newFile(baseDir, FILE_NAME_PROJECT);
  }

  /**
   * Get the activity description file from the base project folder.
   *
   * @param baseDir
   *          the base project folder
   *
   * @return the file for the project file
   */
  private File getActivityProjectFile(File baseDir) {
    return new File(baseDir, FILE_NAME_ACTIVITY);
  }
}
