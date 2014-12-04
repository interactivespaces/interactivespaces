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

import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;

/**
 * A dependency for the project.
 *
 * @author Keith M. Hughes
 */
public class ProjectDependency {

  /**
   * The identifying name of the dependency.
   */
  private String identifyingName;

  /**
   * The minimum version necessary for the activity.
   */
  private Version minimumVersion;

  /**
   * The maximum version necessary for the activity.
   */
  private Version maximumVersion;

  /**
   * Is the dependency required?
   *
   * <p>
   * {@code true} if the dependency is required
   */
  private boolean required;

  /**
   * Get the identifying name of the dependency.
   *
   * @return the identifying name of the dependency
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Set the identifying name of the dependency.
   *
   * @param identifyingName
   *          the identifying name of the dependency
   */
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  /**
   * Get the minimum version necessary for the activity.
   *
   * @return the minimum version
   */
  public Version getMinimumVersion() {
    return minimumVersion;
  }

  /**
   * Set the minimum version necessary for the activity.
   *
   * @param minimumVersion
   *          the minimum version
   */
  public void setMinimumVersion(Version minimumVersion) {
    this.minimumVersion = minimumVersion;
  }

  /**
   * Get the maximum version necessary for the activity.
   *
   * @return the maximum version
   */
  public Version getMaximumVersion() {
    return maximumVersion;
  }

  /**
   * Set the maximum version necessary for the activity.
   *
   * @param maximumVersion
   *          the maximum version
   */
  public void setMaximumVersion(Version maximumVersion) {
    this.maximumVersion = maximumVersion;

  }

  /**
   * Get the version range for this dependency.
   *
   * @return the version range for this dependency
   */
  public VersionRange getVersionRange() {
    // TODO(keith): Get rid of setting min and max when there is time to test
    // everything. Also removing the OSGi method below can then go away.
    return new VersionRange(minimumVersion, maximumVersion, false);
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

  /**
   * Get the OSGi formatted version string for this dependency.
   *
   * @return the OSGI version string
   */
  public String getOsgiVersionString() {
    StringBuilder builder = new StringBuilder();
    if (maximumVersion != null) {
      builder.append("[").append(minimumVersion).append(',').append(maximumVersion);
      if (minimumVersion.equals(maximumVersion)) {
        // If same want exact range
        builder.append(']');
      } else {
        // Want exclusive range
        builder.append(')');
      }
    } else {
      builder.append(minimumVersion);
    }

    return builder.toString();
  }

  @Override
  public String toString() {
    return "ProjectDependency [identifyingName=" + identifyingName + ", minimumVersion=" + minimumVersion
        + ", maximumVersion=" + maximumVersion + ", required=" + required + "]";
  }
}
