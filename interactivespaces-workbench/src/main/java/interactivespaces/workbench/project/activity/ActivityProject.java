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

import interactivespaces.domain.basic.pojo.SimpleConfigurationParameter;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectConfigurationProperty;

import com.google.common.collect.Lists;
import interactivespaces.workbench.project.ProjectTemplate;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.java.JavaProjectType;

import java.io.File;
import java.util.List;

/**
 * A project for an activity.
 *
 * @author Keith M. Hughes
 */
public class ActivityProject extends Project {

  /**
   * Name of the project type.
   */
  public static final String PROJECT_TYPE_NAME = "activity";

  /**
   * Folder where activities are stored.
   */
  public static final String SRC_MAIN_RESOURCES_ACTIVITY = ProjectType.SOURCE_MAIN_RESOURCES + "/activity";

  /**
   * Activity XML file name.
   */
  public static final String FILENAME_ACTIVITY_XML = "activity.xml";

  /**
   * Activity Conf file name.
   */
  public static final String FILENAME_ACTIVITY_CONF = "activity.conf";

  /**
   * Activity resource map file name.
   */
  public static final String FILENAME_RESOURCE_MAP = "resource.map";

  /**
   * The type of the activity, e.g. interactivespaces_native.
   */
  private String activityType;

  /**
   * The runtime name of the activity.
   */
  private String activityRuntimeName;

  /**
   * The name of the executable file.
   */
  private String activityExecutable;

  /**
   * Class of the activity.
   */
  private String activityClass;

  /**
   * Extra configuration parameters for the activity.
   */
  private final List<SimpleConfigurationParameter> extraConfigurationParameters = Lists.newArrayList();

  /**
   * Configuration properies for the activity.
   */
  private final List<ProjectConfigurationProperty> configurationProperties = Lists.newArrayList();

  /**
   * Get the type of the activity.
   *
   * @return type of the activity
   */
  public String getActivityType() {
    return activityType;
  }

  /**
   * Set the type of the activity.
   *
   * @param activityType
   *          the type of the activity
   */
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  /**
   * Get the name of the activity runtime.
   *
   * @return the name of the activity runtime
   */
  public String getActivityRuntimeName() {
    return activityRuntimeName;
  }

  /**
   * Set the name of the activity runtime.
   *
   * @param activityRuntimeName
   *          the name of the activity runtime
   */
  public void setActivityRuntimeName(String activityRuntimeName) {
    this.activityRuntimeName = activityRuntimeName;
  }

  /**
   * Get the executable for the activity.
   *
   * @return the executable for the activity
   */
  public String getActivityExecutable() {
    return activityExecutable;
  }

  /**
   * Set the executable for the activity.
   *
   * @param activityExecutable
   *          the executable for the activity
   */
  public void setActivityExecutable(String activityExecutable) {
    this.activityExecutable = activityExecutable;
  }

  /**
   * Get the class of the activity.
   *
   * @return the class of the activity
   */
  public String getActivityClass() {
    return activityClass;
  }

  /**
   * Set the Class of the activity.
   *
   * @param activityClass
   *          the class of the activity
   */
  public void setActivityClass(String activityClass) {
    this.activityClass = activityClass;
  }

  /**
   * Get the location of the config file.
   *
   * @return the location of the config file
   */
  public File getActivityConfigFile() {
    return new File(getActivityResourceDirectory(), FILENAME_ACTIVITY_CONF);
  }

  /**
   * Get the location of the source resource directory.
   *
   * @return the location of the source resource directory
   */
  public File getActivityResourceDirectory() {
    return new File(getBaseDirectory(), SRC_MAIN_RESOURCES_ACTIVITY);
  }

  /**
   * Get the location of the description file.
   *
   * @return the location of the description file
   */
  public File getActivityDescriptionFile() {
    return new File(getBaseDirectory(), FILENAME_ACTIVITY_XML);
  }

  /**
   * Get the location of the source directory for activities.
   *
   * @return the location of the source directory for activities
   */
  public File getActivitySourceDirectory() {
    return new File(getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
  }

  /**
   * Get all extra configurations for the activity.
   *
   * @return all extra configurations for the activity
   */
  public List<SimpleConfigurationParameter> getExtraConfigurationParameters() {
    return extraConfigurationParameters;
  }

  /**
   * Add an extra configuration parameter to the spec.
   *
   * @param parameter
   *          the new configuration parameter to add
   */
  public void addExtraConfigurationParameter(SimpleConfigurationParameter parameter) {
    extraConfigurationParameters.add(parameter);
  }

  /**
   * Add an extra configuration parameter to the spec.
   *
   * @param name
   *          the name of the parameter to add
   * @param value
   *          the value of the parameter to add
   */
  public void addExtraConfigurationParameter(String name, String value) {
    SimpleConfigurationParameter parameter = new SimpleConfigurationParameter();
    parameter.setName(name);
    parameter.setValue(value);

    extraConfigurationParameters.add(parameter);
  }

  /**
   * Get the configuration properties for the activity.
   *
   * @return the configuration properties for the activity
   */
  public List<ProjectConfigurationProperty> getConfigurationProperties() {
    return configurationProperties;
  }

  /**
   * Set the configuration properties for the activity.
   *
   * @param addConfigurationProperties
   *          the configuration properties for the activity
   */
  public void addConfigurationProperties(List<ProjectConfigurationProperty> addConfigurationProperties) {
    configurationProperties.addAll(addConfigurationProperties);
  }

  /**
   * Add a configuration properties to the activity.
   *
   * @param configurationProperty
   *          the configuration property for the activity
   */
  public void addConfigurationProperty(ProjectConfigurationProperty configurationProperty) {
    configurationProperties.add(configurationProperty);
  }
}
