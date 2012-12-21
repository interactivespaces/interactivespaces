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

package interactivespaces.service;

import java.util.Map;

/**
 * A registry of services which can be used.
 * 
 * @author Keith M. Hughes
 */
public interface ServiceRegistry {

	/**
	 * Register a service with the registry.
	 * 
	 * @param name
	 *            the name of the service
	 * @param service
	 *            the service instance
	 */
	void registerService(String name, Service service);

	/**
	 * Register a service with the registry.
	 * 
	 * @param name
	 *            the name of the service
	 * @param service
	 *            the service instance
	 * @param metadata
	 *            any metadata for the service (can be {@code null})
	 */
	void registerService(String name, Service service,
			Map<String, Object> metadata);

	/**
	 * Unregister a service with the registry.
	 * 
	 * <p>
	 * Does nothing if the service wasn't registered.
	 * 
	 * @param name
	 *            the name of the service
	 * @param service
	 *            the service to unregister
	 */
	void unregisterService(String name, Service service);

	/**
	 * Get a given service from the registry.
	 * 
	 * @param name
	 *            the name of the desired service
	 * 
	 * @return the requested service, or {@code null} if there is no such
	 *         service registered
	 */
	<T extends Service> T getService(String name);
}
