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

package interactivespaces.master.server.services;

import interactivespaces.resource.NamedVersionedResource;
import interactivespaces.resource.ResourceDependency;
import interactivespaces.util.resource.ManagedResource;

import java.util.Set;

/**
 * A manager for obtaining container reosurces.
 *
 * @author Keith M. Hughes
 */
public interface ContainerResourceDeploymentManager extends ManagedResource {

  /**
   * Take a collection of dependency requirements and calculate the set of
   * resources which best meets the requirements.
   *
   * @param dependencies
   *          the dependencies
   *
   * @return the items which meet the requirements
   */
  Set<NamedVersionedResource> satisfyDependencies(Set<ResourceDependency> dependencies);

  /**
   * Commit a set of resources to a space controller.
   *
   * @param controller
   *          the controller to commit to
   * @param resources
   *          the resources to commit
   */
  void commitResources(ActiveSpaceController controller, Set<NamedVersionedResource> resources);

  /**
   * Commit a set of resources to a space controller for an ongoing transaction.
   *
   * @param transactionId
   *          the transaction ID to use for the committing
   * @param controller
   *          the controller to commit to
   * @param resources
   *          the resources to commit
   */
  void commitResources(String transactionId, ActiveSpaceController controller, Set<NamedVersionedResource> resources);
}
