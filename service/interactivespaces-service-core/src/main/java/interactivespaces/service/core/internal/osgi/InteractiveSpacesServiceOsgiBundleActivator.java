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

package interactivespaces.service.core.internal.osgi;

import interactivespaces.service.core.internal.osgi.ServicesCoreOsgiBundleActivator.MyServiceTracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleActivator;

/**
 * A base class for creating OSGi BundleActivator subclasses for Interactive
 * Spaces services.
 *
 * @author Keith M. Hughes
 */
public abstract class InteractiveSpacesServiceOsgiBundleActivator implements BundleActivator {

  /**
   * Create a new service tracker.
   *
   * @param context
   *          the bundle context
   * @param serviceName
   *          name of the service class
   *
   * @return the service tracker
   */
  protected <T> MyServiceTracker<T> newMyServiceTracker(BundleContext context, String serviceName) {
    return new MyServiceTracker<T>(context, serviceName);
  }

  public final class MyServiceTracker<T> extends ServiceTracker {
    private AtomicReference<T> serviceReference = new AtomicReference<T>();

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

    public T getMyService() {
      return serviceReference.get();
    }
  }
}
