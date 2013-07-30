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

package interactivespaces.workbench.project.activity;

import interactivespaces.workbench.project.Project;

import java.io.File;

/**
 * A project for an activity.
 *
 * @author Keith M. Hughes
 */
public class ActivityProject {

  /**
   * Folder where activities are stored.
   */
  public static final String SRC_MAIN_RESOURCES_ACTIVITY = "src/main/resources/activity";

  /**
   * Activity XML file name.
   */
  public static final String FILENAME_ACTIVITY_XML = "activity.xml";

  /**
   * Activity Conf file name.
   */
  public static final String FILENAME_ACTIVITY_CONF = "activity.conf";

  /**
   * The project description for the activity.
   */
  private Project project;

  /**
   * Type of the activity.
   */
  private String activityType;

  /**
   * The runtime name of the activity.
   */
  private String activityRuntimeName;

  public ActivityProject(Project project) {
    this.project = project;
  }

  /**
   * @return the project
   */
  public Project getProject() {
    return project;
  }

  /**
   * Get the location of the config file.
   *
   * @return the location of the config file
   */
  public File getActivityConfigFile() {
    return new File(getActivityResourceDirectory(), "activity.conf");
  }

  /**
   * Get the location of the source resource directory.
   *
   * @return the location of the source resource directory
   */
  public File getActivityResourceDirectory() {
    return new File(project.getBaseDirectory(), SRC_MAIN_RESOURCES_ACTIVITY);
  }

  /**
   * Get the location of the description file.
   *
   * @return the location of the description file
   */
  public File getActivityDescriptionFile() {
    return new File(project.getBaseDirectory(), "activity.xml");
  }

  /**
   * Get the location of the source directory for activities.
   *
   * @return the location of the source directory for activities
   */
  public File getActivitySourceFolder() {
    return new File(project.getBaseDirectory(), SRC_MAIN_RESOURCES_ACTIVITY);
  }

  /**
   * @return the activityType
   */
  public String getActivityType() {
    return activityType;
  }

  /**
   * @param activityType
   *          the activityType to set
   */
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  /**
   * @return the activityRuntimeName
   */
  public String getActivityRuntimeName() {
    return activityRuntimeName;
  }

  /**
   * @param activityRuntimeName
   *          the activityRuntimeName to set
   */
  public void setActivityRuntimeName(String activityRuntimeName) {
    this.activityRuntimeName = activityRuntimeName;
  }
}
