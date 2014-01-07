/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.util.resource;

import interactivespaces.InteractiveSpacesException;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * A collection of {@link ManagedResource} instances.
 *
 * <p>
 * The collection will start up and shut down the resources when it is started
 * up and shut down. Do not worry about these lifecycle events.
 *
 * @author Keith M. Hughes
 */
public class ManagedResources {

  /**
   * The managed resources.
   */
  private final List<ManagedResource> resources = Lists.newArrayList();

  /**
   * Logger for the managed resources.
   */
  private final Log log;

  /**
   * {@code true} if the collection has been officially started.
   */
  private boolean started;

  /**
   * Construct a new managed resource collection.
   *
   * @param log
   *          the log for the collection
   */
  public ManagedResources(Log log) {
    this.log = log;
  }

  /**
   * Add a new resource to the collection.
   *
   * @param resource
   *          the resource to add
   */
  public synchronized void addResource(ManagedResource resource) {
    if (started) {
      try {
        // Will only add if starts up properly
        resource.startup();
      } catch (Exception e) {
        throw new InteractiveSpacesException("Could not start up managed resource", e);
      }
    }

    resources.add(resource);
  }

  /**
   * Clear all resources from the collection.
   *
   * <p>
   * The collection is cleared. No lifecycle methods are called on the
   * resources.
   */
  public synchronized void clear() {
    resources.clear();
  }

  /**
   * Attempt to startup all resources in the manager.
   *
   * <p>
   * If all resources don't start up, all resources that were started will be
   * shut down.
   *
   * <p>
   * Do not call {@link #shutdownResources()} or
   * {@link #shutdownResourcesAndClear()} if an exception is thrown out of this
   * method.
   */
  public synchronized void startupResources() {
    List<ManagedResource> startedResources = Lists.newArrayList();

    for (ManagedResource resource : resources) {
      try {
        resource.startup();

        startedResources.add(resource);
      } catch (Exception e) {
        shutdownResources(startedResources);

        throw new InteractiveSpacesException("Could not start up all managed resources", e);
      }
    }

    started = true;
  }

  /**
   * Shut down all resources.
   *
   * <p>
   * This will make a best attempt. A shutdown will be attempted on all
   * resources, even if some throw an exception.
   */
  public synchronized void shutdownResources() {
    shutdownResources(resources);
  }

  /**
   * Shut down all resources and clear from the collection.
   *
   * <p>
   * This will make a best attempt. A shutdown will be attempted on all
   * resources, even if some throw an exception.
   */
  public synchronized void shutdownResourcesAndClear() {
    shutdownResources();
    clear();
  }

  /**
   * Shut down the specified resources.
   *
   * @param resources
   *          some resources to shut down
   */
  private void shutdownResources(List<ManagedResource> resources) {
    for (ManagedResource resource : resources) {
      try {
        resource.shutdown();
      } catch (Exception e) {
        log.error("Could not shut down resource", e);
      }
    }
  }
}
