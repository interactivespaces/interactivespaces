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

package interactivespaces.container.resource.deployment;

import interactivespaces.resource.Version;
import interactivespaces.system.resources.ContainerResource;
import interactivespaces.system.resources.ContainerResourceLocation;

/**
 * An item to be part of a resource deployment.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceDeploymentItem extends ContainerResource {

  /**
   * URI for locating the resource.
   */
  private final String resourceSourceUri;

  /**
   * Construct a new resource deployment item.
   *
   * @param name
   *          name of the resource
   * @param version
   *          version of the resource
   * @param location
   *          location of the resource
   * @param resourceSourceUri
   *          URI for getting the resource
   */
  public ContainerResourceDeploymentItem(String name, Version version, ContainerResourceLocation location,
      String resourceSourceUri) {
    super(name, version, location);

    this.resourceSourceUri = resourceSourceUri;
  }

  /**
   * Get the URI for obtaining the resource.
   *
   * @return the URI for obtaining the resource
   */
  public String getResourceSourceUri() {
    return resourceSourceUri;
  }
}
