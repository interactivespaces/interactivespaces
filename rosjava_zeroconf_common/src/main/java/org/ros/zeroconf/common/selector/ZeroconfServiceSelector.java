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

import org.ros.zeroconf.common.ZeroconfServiceInfo;

import java.util.concurrent.TimeUnit;

/**
 * Holds a collection of {@link ZeroconfServiceInfo} objects and picks one for
 * use.
 *
 * @author Keith M. Hughes
 */
public interface ZeroconfServiceSelector<T extends ZeroconfServiceInfo> {

  /**
   * Add a new service to the collection.
   *
   * @param serviceInfo
   *          the new service record to add
   */
  void addService(T serviceInfo);

  /**
   * Remove a service from the collection.
   *
   * <p>
   * Does nothing if the service wasn't there.
   *
   * @param serviceInfo
   *          the service record to remove
   */
  void removeService(T serviceInfo);

  /**
   * Select a service from the collection.
   *
   * <p>
   * Lower valued priorities are chosen over those with higher priorities.
   *
   * @return A service, if there is one, or {@code null} if none.
   */
  T selectService();

  /**
   * Get a service.
   *
   * <p>
   * This call will wait until a service is available and will block
   * indefinitely.
   *
   * @return
   */
  T getService();

  /**
   * Get a service.
   *
   * <p>
   * This call will wait until a service is available for the amount of time
   * requested.
   *
   * @param timeout
   *          the time to wait
   * @param unit
   *          the units for the wait time
   * @return
   */
  T getService(long timeout, TimeUnit unit);

  /**
   * Are there services available?
   *
   * @return {@code true} if services are available.
   */
  boolean areServicesAvailable();
}
