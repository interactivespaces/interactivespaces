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

import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.internal.InteractiveSpacesXBeeCommunicationEndpointService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The Bundle Activator for the core InteractiveSpaces services.
 *
 * @author Keith M. Hughes
 */
public class ServicesCoreOsgiBundleActivator implements BundleActivator {

	/**
	 * OSGi service tracker for the interactive spaces environment.
	 */
	private MyServiceTracker<InteractiveSpacesEnvironment> interactiveSpacesEnvironmentTracker;

	/**
	 * The mail receiver service created by this bundle.
	 */
	private InteractiveSpacesXBeeCommunicationEndpointService xbeeCommEndpointService;

	/**
	 * OSGi bundle context for this bundle.
	 */
	private BundleContext bundleContext;

	/**
	 * Object to give lock for putting this bundle's services together.
	 */
	private Object serviceLock = new Object();

	@Override
	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;

		interactiveSpacesEnvironmentTracker = newMyServiceTracker(context,
				InteractiveSpacesEnvironment.class.getName());
		interactiveSpacesEnvironmentTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		xbeeCommEndpointService.shutdown();

		interactiveSpacesEnvironmentTracker
				.getMyService()
				.getServiceRegistry()
				.unregisterService(XBeeCommunicationEndpointService.SERVICE_NAME,
						xbeeCommEndpointService);

		xbeeCommEndpointService = null;

		interactiveSpacesEnvironmentTracker.close();
		interactiveSpacesEnvironmentTracker = null;
	}

	/**
	 * Another service reference has come in. Handle.
	 */
	private void gotAnotherReference() {
		synchronized (serviceLock) {
			xbeeCommEndpointService = new InteractiveSpacesXBeeCommunicationEndpointService();

			interactiveSpacesEnvironmentTracker
					.getMyService()
					.getServiceRegistry()
					.registerService(XBeeCommunicationEndpointService.SERVICE_NAME,
							xbeeCommEndpointService);

			xbeeCommEndpointService.startup();
		}
	}

	/**
	 * Create a new service tracker.
	 *
	 * @param context
	 *            the bundle context
	 * @param serviceName
	 *            name of the service class
	 *
	 * @return the service tracker
	 */
	<T> MyServiceTracker<T> newMyServiceTracker(BundleContext context,
			String serviceName) {
		return new MyServiceTracker<T>(context, serviceName);
	}

	private final class MyServiceTracker<T> extends ServiceTracker {
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
