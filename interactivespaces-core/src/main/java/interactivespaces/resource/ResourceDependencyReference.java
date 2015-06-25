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

package interactivespaces.resource;

/**
 * A reference to a resource dependency.
 *
 * @author Keith M. Hughes
 */
public class ResourceDependencyReference implements ResourceDependency {

  /**
   * Name of the resource.
   */
  private final String name;

  /**
   * Version range for the dependency.
   */
  private final VersionRange versionRange;

  /**
   * Construct a resource dependency reference.
   *
   * @param name
   *          name of the resource
   * @param versionRange
   *          range of versions of the resource required
   */
  public ResourceDependencyReference(String name, VersionRange versionRange) {
    this.name = name;
    this.versionRange = versionRange;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public VersionRange getVersionRange() {
    return versionRange;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + versionRange.hashCode();
     return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    ResourceDependencyReference other = (ResourceDependencyReference) obj;
    if (!name.equals(other.name)) {
      return false;
    }

    return versionRange.equals(other.versionRange);
  }
}
