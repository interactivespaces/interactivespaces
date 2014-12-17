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

package interactivespaces.controller.runtime;

import interactivespaces.util.data.resource.LocatableResource;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A dependency for the controller container.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerContainerDependency {

  /**
   * Name of the dependency.
   */
  private String name;

  /**
   * The minimum version of the dependency required.
   */
  private String minimumVersion;

  /**
   * The maximum version of the dependency required.
   */
  private String maximumVersion;

  /**
   * Is the dependency required or not?
   */
  private boolean required;

  /**
   * The resources unknown which can supply this dependency.
   */
  private List<LocatableResource> resources = Lists.newArrayList();

  /**
   * Construct the dependency.
   *
   * @param name
   *          the name of the dependency
   * @param minimumVersion
   *          the minimum version of the dependency
   * @param maximumVersion
   *          the maximum version of the dependency
   * @param required
   *          {@code true} if the depdency is required
   */
  public SpaceControllerContainerDependency(String name, String minimumVersion, String maximumVersion, boolean required) {
    this.name = name;
    this.minimumVersion = minimumVersion;
    this.maximumVersion = maximumVersion;
    this.required = required;
  }

  /**
   * Get the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the minimum version.
   *
   * @return the minimumVersion
   */
  public String getMinimumVersion() {
    return minimumVersion;
  }

  /**
   * Get the maximum version.
   *
   * @return the maximumVersion
   */
  public String getMaximumVersion() {
    return maximumVersion;
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
   * Get the resources.
   *
   * @return the resources
   */
  public List<LocatableResource> getResources() {
    return resources;
  }

  /**
   * Add a new resource to the dependency.
   *
   * @param resource
   *          the resource to add
   */
  public void addResource(LocatableResource resource) {
    resources.add(resource);
  }
}
