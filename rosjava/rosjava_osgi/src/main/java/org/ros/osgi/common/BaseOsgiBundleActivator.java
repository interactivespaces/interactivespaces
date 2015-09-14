/*
 * Copyright (C) 2014 Google Inc.
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

package org.ros.osgi.common;

import com.google.common.collect.Lists;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A base implementation of an OSGi bundle activator for ROS to use.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseOsgiBundleActivator implements BundleActivator {

  /**
   * All OSGi service registrations from this bundle.
   */
  private final List<ServiceRegistration<?>> osgiServiceRegistrations = Lists.newArrayList();

  /**
   * OSGi bundle context for this bundle.
   */
  private BundleContext bundleContext;

  /**
   * All service trackers we have.
   */
  private final Map<String, MyServiceTracker<?>> serviceTrackers = new HashMap<String, MyServiceTracker<?>>();

  /**
   * Object to give lock for putting this bundle's services together.
   */
  private final Object serviceLock = new Object();

  @Override
  public void start(BundleContext context) throws Exception {
    this.bundleContext = context;

    // Get the registrations from the subclass.
    onStart();

    // Open all the trackers.
    for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
      tracker.open();
    }
  }

  @Override
  public void stop(BundleContext context) throws Exception {

    onStop();

    unregisterOsgiServices();

    // Close all the trackers.
    for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
      tracker.close();
    }
    serviceTrackers.clear();
  }

  /**
   * The bundle is starting. Add any requests for services.
   */
  protected void onStart() {
    // Default is to do nothing.
  }

  /**
   * Bundle is shutting down. Do any extra cleanup.
   */
  protected void onStop() {
    // Default is do nothing.
  }

  /**
   * Register an OSGi service.
   *
   * @param name
   *          name for the OSGi service
   * @param service
   *          the OSGi service
   */
  protected void registerOsgiService(String name, Object service) {
    osgiServiceRegistrations.add(bundleContext.registerService(name, service, null));
  }

  /**
   * Got another reference from a dependency.
   */
  protected void gotAnotherReference() {
    synchronized (serviceLock) {
      // If missing any of our needed services, punt.
      for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
        if (tracker.getMyService() == null) {
          return;
        }
      }

      allRequiredServicesAvailable();
    }
  }

  /**
   * All required services are available.
   */
  protected abstract void allRequiredServicesAvailable();

  /**
   * Unregister all OSGi-registered services.
   */
  private void unregisterOsgiServices() {
    for (ServiceRegistration<?> service : osgiServiceRegistrations) {
      service.unregister();
    }
    osgiServiceRegistrations.clear();
  }

  /**
   * Create a new service tracker.
   *
   * @param serviceName
   *          name of the service class
   * @param <T>
   *          class being tracked by the service tracker
   *
   * @return the service tracker
   */
  protected <T> MyServiceTracker<T> newMyServiceTracker(String serviceName) {
    MyServiceTracker<T> tracker = new MyServiceTracker<T>(bundleContext, serviceName);

    serviceTrackers.put(serviceName, tracker);

    return tracker;
  }

  /**
   * Get the bundle context for the bundle.
   *
   * @return the bundle context
   */
  public BundleContext getBundleContext() {
    return bundleContext;
  }

  /**
   * An OSGi service tracking class.
   *
   * @param <T>
   *          the class of the service being tracked
   *
   * @author Keith M. Hughes
   */
  public final class MyServiceTracker<T> extends ServiceTracker {

    /**
     * The reference for the service object being waited for.
     */
    private final AtomicReference<T> serviceReference = new AtomicReference<T>();

    /**
     * Construct a service tracker.
     *
     * @param context
     *          bundle context the tracker is running under
     * @param serviceName
     *          the name of the service
     */
    public MyServiceTracker(BundleContext context, String serviceName) {
      super(context, serviceName, null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
      @SuppressWarnings("unchecked")
      T service = (T) super.addingService(reference);

      if (serviceReference.compareAndSet(null, service)) {
        gotAnotherReference();
      }

      return service;
    }

    /**
     * Get the service needed.
     *
     * @return the service, or {@code null} if it hasn't been obtained yet.
     */
    public T getMyService() {
      return serviceReference.get();
    }
  }
}
