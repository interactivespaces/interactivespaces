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
 * <p>
 * Dynamic dependencies have meaning only in the workbench.
 *
 * @author Keith M. Hughes
 */
public class ProjectDependency {

  /**
   * The identifying name of the dependency.
   */
  private String identifyingName;

  /**
   * The range of versions necessary for the activity.
   */
  private VersionRange versionRange;

  /**
   * Is the dependency required?
   *
   * <p>
   * {@code true} if the dependency is required
   */
  private boolean required;

  /**
   * {@code true} if the dependency is dynamic.
   */
  private boolean dynamic;

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
   * Set the version range for this dependency.
   *
   * @param versionRange
   *          the version range for this dependency
   */
  public void setVersionRange(VersionRange versionRange) {
    this.versionRange = versionRange;
  }

  /**
   * Get the version range for this dependency.
   *
   * @return the version range for this dependency
   */
  public VersionRange getVersion() {
    return versionRange;
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
   * Is the dependency dynamic?
   *
   * @return {@code true} if the dependency is dynamic
   */
  public boolean isDynamic() {
    return dynamic;
  }

  /**
   * Set if the dependency is dynamic.
   *
   * @param dynamic
   *          {@code true} if the dependency is dynamic
   */
  public void setDynamic(boolean dynamic) {
    this.dynamic = dynamic;
  }

  /**
   * Get the OSGi formatted version string for this dependency.
   *
   * @return the OSGI version string
   */
  public String getOsgiVersionString() {
    StringBuilder builder = new StringBuilder();
    Version minimumVersion = versionRange.getMinimum();
    Version maximumVersion = versionRange.getMaximum();
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
    return "ProjectDependency [identifyingName=" + identifyingName + ", versionRange=" + versionRange
        + ", required=" + required + ", dynamic=" + dynamic + "]";
  }
}
