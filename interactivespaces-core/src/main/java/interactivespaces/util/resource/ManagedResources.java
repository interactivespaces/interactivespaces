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

import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * A collection of {@link ManagedResource} instances.
 * 
 * <p>
 * The collection will start up and shut down the resources when it is started
 * up and shut down. Do not worry about these lifecycle events.
 * 
 * <p>
 * This class is NOT thread-safe.
 * 
 * @author Keith M. Hughes
 */
public class ManagedResources {

	/**
	 * The managed resources.
	 */
	private List<ManagedResource> resources = Lists.newArrayList();
	
	/**
	 * Logger for the managed resources.
	 */
	private Log log;

	public ManagedResources(Log log) {
		this.log = log;
	}

	/**
	 * Add a new resource to the collection.
	 * 
	 * @param resource
	 *            the resource to add
	 */
	public void addResource(ManagedResource resource) {
		resources.add(resource);
	}

	/**
	 * Clear all resources from the collection.
	 * 
	 * <p>
	 * The collection is cleared. No lifecycle methods are called on the
	 * resources.
	 */
	public void clear() {
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
	 * Do not call {@link #shutdownResources(Log)} if an exception is thrown out
	 * of this method.
	 */
	public void startupResources() {
		List<ManagedResource> startedResources = Lists.newArrayList();

		for (ManagedResource resource : resources) {
			try {
				resource.startup();

				startedResources.add(resource);
			} catch (Exception e) {
				shutdownResources(startedResources);

				throw new InteractiveSpacesException(
						"Could not start up all managed resources", e);
			}
		}
	}

	/**
	 * Shut down all resources.
	 * 
	 * <p>
	 * This will make a best attempt. A shutdown will be attempted on all
	 * resources, even if some throw an exception.
	 */
	public void shutdownResources() {
		shutdownResources(resources);
	}

	/**
	 * Shut down the specified resoures.
	 * 
	 * @param resources
	 *            some resources to shut down
	 * @param log
	 *            the log for exceptions and general reporting.
	 */
	private void shutdownResources(List<ManagedResource> resources) {
		for (ManagedResource resource : resources) {
			try {
				resource.shutdown();
			} catch (Exception e) {
				log.error("Could not shut down resoutrce", e);
			}
		}
	}

}
