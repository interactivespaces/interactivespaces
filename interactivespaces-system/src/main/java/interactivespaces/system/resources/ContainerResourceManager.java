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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.resource.io.ResourceSource;

import org.osgi.framework.Bundle;

import java.io.File;

/**
 * A manager for controlling container resources.
 *
 * @author Keith M. Hughes
 */
public interface ContainerResourceManager {

  /**
   * Get all current resources in the container.
   *
   * <p>
   * Because of the dynamic nature of an Interactive Spaces container, do not keep copies of this object around because
   * the resources available can change over time.
   *
   * @return all resources
   */
  ContainerResourceCollection getResources();

  /**
   * Add a resource to the container.
   *
   * @param resource
   *          the full information about the new resource
   * @param source
   *          the source of the resource
   */
  void addResource(ContainerResource resource, ResourceSource source);

  /**
   * Load a bundle into the container and start it.
   *
   * @param bundleFile
   *          the bundle file
   * @param type
   *          the type for the bundle
   *
   * @return the bundle
   *
   * @throws InteractiveSpacesException
   *        the bundle didn't exist or couldn't start
   */
  Bundle loadAndStartBundle(File bundleFile, ContainerResourceType type) throws InteractiveSpacesException;

  /**
   * Uninstall a bundle.
   *
   * @param bundle
   *        the bundle must be one that was installed
   */
  void uninstallBundle(Bundle bundle);
}
