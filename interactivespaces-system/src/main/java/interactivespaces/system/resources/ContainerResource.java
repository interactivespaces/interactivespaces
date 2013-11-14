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

package interactivespaces.system.resources;

import interactivespaces.resource.NamedVersionedResource;
import interactivespaces.resource.Version;

/**
 * A resource in a container, e.g. a bundle.
 *
 * @author Keith M. Hughes
 */
public class ContainerResource extends NamedVersionedResource {

  /**
   * Location of the resource in a container.
   */
  private final ContainerResourceLocation location;

  /**
   * Construct a new container resource.
   *
   * @param name
   *          name of the resource
   * @param version
   *          version of the resource
   * @param location
   *          location of the resource
   */
  public ContainerResource(String name, Version version, ContainerResourceLocation location) {
    super(name, version);

    this.location = location;
  }

  /**
   * Get the location of the container resource.
   *
   * @return the location of the container resource
   */
  public ContainerResourceLocation getLocation() {
    return location;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + location.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ContainerResource other = (ContainerResource) obj;
    if (location != other.location) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ContainerResource [location=" + location + ", getName()=" + getName() + ", getVersion()=" + getVersion()
        + "]";
  }
}
