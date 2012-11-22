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

package interactivespaces.controller.activity.wrapper.internal.script.osgi;

import interactivespaces.controller.activity.wrapper.internal.script.ScriptActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.service.script.ScriptService;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * An OSGi activator for the script activity wrapper.
 * 
 * @author Keith M. Hughes
 */
public class OsgiScriptActivityWrapperActivator implements BundleActivator {

	/**
	 * OSGi service tracker for the interactive spaces environment.
	 */
	private MyServiceTracker<ScriptService> scriptServiceTracker;
	private MyServiceTracker<ActiveControllerActivityFactory> activeControllerActivityFactoryTracker;

	private BundleContext bundleContext;

	/**
	 * Object to give lock for putting this bundle's services together.
	 */
	private Object serviceLock = new Object();

	private ActiveControllerActivityFactory activeControllerActivityFactory;
	private ScriptActivityWrapperFactory scriptActivityWrapperFactory;

	@Override
	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;

		scriptServiceTracker = newMyServiceTracker(context,
				ScriptService.class.getName());

		activeControllerActivityFactoryTracker = newMyServiceTracker(context,
				ActiveControllerActivityFactory.class.getName());

		scriptServiceTracker.open();
		activeControllerActivityFactoryTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		scriptServiceTracker.close();
		activeControllerActivityFactoryTracker.close();

		if (activeControllerActivityFactory != null) {
			activeControllerActivityFactory.unregisterActivityWrapperFactory(scriptActivityWrapperFactory);
			activeControllerActivityFactory = null;
		}

	}

	private void gotAnotherReference() {
		synchronized (serviceLock) {
			ScriptService scriptService = scriptServiceTracker.getMyService();
			activeControllerActivityFactory = activeControllerActivityFactoryTracker
					.getMyService();

			if (scriptService != null
					&& activeControllerActivityFactory != null) {
				scriptActivityWrapperFactory = new ScriptActivityWrapperFactory(
						scriptService);
				activeControllerActivityFactory.registerActivityWrapperFactory(scriptActivityWrapperFactory);
			}
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
