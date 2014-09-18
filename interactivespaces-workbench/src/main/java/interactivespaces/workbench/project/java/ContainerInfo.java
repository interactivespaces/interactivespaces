/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.workbench.project.java;

import interactivespaces.resource.VersionRange;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Information needed for the container for a Java project.
 *
 * @author Keith M. Hughes
 */
public class ContainerInfo {

  /**
   * Classname of the bundle activator, can be {@code null}.
   */
  private String activatorClassname;

  /**
   * The private packages for the library.
   */
  private List<String> privatePackages = Lists.newArrayList();

  /**
   * The import packages for the library.
   */
  private List<ImportPackage> importPackages = Lists.newArrayList();

  /**
   * Get the classname of the bundle activator.
   *
   * @return the classname of the bundle activator, can be {@code null}.
   */
  public String getActivatorClassname() {
    return activatorClassname;
  }

  /**
   * Set the classname of the bundle activator.
   *
   * @param activatorClassname
   *          the classname of the bundle activator, can be {@code null}.
   */
  public void setActivatorClassname(String activatorClassname) {
    this.activatorClassname = activatorClassname;
  }

  /**
   * Get the list of private packages.
   *
   * <p>
   * This is the actual list, not a copy. Feel free to modify as needed.
   *
   * @return the list of private packages
   */
  public List<String> getPrivatePackages() {
    return privatePackages;
  }

  /**
   * Get the list of import packages.
   *
   * <p>
   * This is the actual list, not a copy. Feel free to modify as needed.
   *
   * @return the list of import packages
   */
  public List<ImportPackage> getImportPackages() {
    return importPackages;
  }

  /**
   * Representation of a package to be imported.
   *
   * @author Keith M. Hughes
   */
  public static class ImportPackage {

    /**
     * The name of the package.
     */
    private final String packageName;

    /**
     * {@code true} if the package is require.
     */
    private final boolean required;

    /**
     * The version range for the import, can be {@code null}.
     */
    private final VersionRange versionRange;

    /**
     * Construct a new import package.
     *
     * @param packageName
     *          the name of the import package
     * @param required
     *          {@code true} if the package is require
     * @param versionRange
     *          the version range for the import, can be {@code null}
     */
    public ImportPackage(String packageName, boolean required, VersionRange versionRange) {
      this.packageName = packageName;
      this.required = required;
      this.versionRange = versionRange;
    }

    /**
     * Get the OSGi header for the import.
     *
     * @return the OSGi header for the import
     */
    public String getOsgiHeader() {
      StringBuilder header = new StringBuilder(packageName);

      if (!required) {
        header.append(";resolution:=\"optional\"");
      }

      if (versionRange != null) {
        header.append(";version=\"").append(versionRange.toString()).append("\"");
      }

      return header.toString();
    }
  }
}
