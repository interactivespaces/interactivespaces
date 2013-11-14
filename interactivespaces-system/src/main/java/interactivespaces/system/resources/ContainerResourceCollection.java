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

import interactivespaces.resource.NamedVersionedResourceCollection;

import java.util.Set;

/**
 * A collection of container resources.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceCollection {

  /**
   * A collection of named versioned resources.
   */
  private final NamedVersionedResourceCollection<ContainerResource> resources = NamedVersionedResourceCollection
      .newNamedVersionedResourceCollection();

  /**
   * Construct a resource collection.
   *
   * @param resources
   *          the resources to be in the collection
   */
  public ContainerResourceCollection(Set<ContainerResource> resources) {
    for (ContainerResource resource : resources) {
      this.resources.addResource(resource.getName(), resource.getVersion(), resource);
    }
  }
}
