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

package interactivespaces.workbench.project;

/**
 * A dependency for the project.
 *
 * @author Keith M. Hughes
 */
public class ProjectDependency {

  /**
   * The name of the dependency.
   */
  private String name;

  /**
   * The minimum version necessary for the activity.
   */
  private String minimumVersion;

  /**
   * The maximum version necessary for the activity.
   */
  private String maximumVersion;

  /**
   * Is the dependency required?
   *
   * <p>
   * {@code true} if the dependency is required
   */
  private boolean required;

  /**
   * Get the name of the dependency.
   *
   * @return The name of the dependency.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the dependency.
   *
   * @param name
   *          the name of the dependency
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the minimum version necessary for the activity.
   *
   * @return
   */
  public String getMinimumVersion() {
    return minimumVersion;
  }

  /**
   * Set the minimum version necessary for the activity.
   *
   * @param minimumVersion
   *          the minimum version
   */
  public void setMinimumVersion(String minimumVersion) {
    this.minimumVersion = minimumVersion;
  }

  /**
   * Get the maximum version necessary for the activity.
   *
   * @return
   */
  public String getMaximumVersion() {
    return maximumVersion;
  }

  /**
   * Set the maximum version necessary for the activity.
   *
   * @param maximumVersion
   *          the maximum version
   */
  public void setMaximumVersion(String maximumVersion) {
    this.maximumVersion = maximumVersion;

  }

  /**
   * Is the dependency required?
   *
   * @return {@code true} if the dependency is required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * Set if the dependency is required.
   *
   * @param required
   *          {@code true} if the dependency is required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }
}
