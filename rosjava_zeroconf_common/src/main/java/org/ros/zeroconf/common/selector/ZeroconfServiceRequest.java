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

package org.ros.zeroconf.common.selector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ros.zeroconf.common.ZeroconfServiceInfo;

/**
 * A request for a Zeroconf service.
 * 
 * <p>
 * This is used for those who want to block while waiting.
 * 
 * @author Keith M. Hughes
 */
public class ZeroconfServiceRequest<T extends ZeroconfServiceInfo> {

	/**
	 * The latch used to wait.
	 */
	private CountDownLatch latch = new CountDownLatch(1);

	/**
	 * The service to be returned.
	 */
	private volatile T service;

	/**
	 * Get a service.
	 * 
	 * <p>
	 * This call will block indefinitely.
	 * 
	 * @return
	 */
	public T getService() throws InterruptedException {
		latch.await();

		return service;
	}

	/**
	 * Get a service.
	 * 
	 * @param timeout
	 *            the time to wait
	 * @param unit
	 *            the units for the wait time
	 * 
	 * @return the service requested, or {@code} null if none were available
	 *         before timeout
	 */
	public T getService(long timeout, TimeUnit unit)
			throws InterruptedException {
		latch.await(timeout, unit);

		// Don't care about the race of suddenly finding ourselves with a
		// service.
		
		return service;
	}

	/**
	 * Set the service for the request.
	 * 
	 * @param service
	 */
	public void setService(T service) {
		this.service = service;

		latch.countDown();
	}
}
