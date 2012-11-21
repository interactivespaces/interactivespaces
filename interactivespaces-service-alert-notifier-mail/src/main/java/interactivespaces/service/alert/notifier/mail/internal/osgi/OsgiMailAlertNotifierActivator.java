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

package interactivespaces.service.alert.notifier.mail.internal.osgi;

import interactivespaces.service.alert.AlertService;
import interactivespaces.service.alert.notifier.mail.internal.BasicMailAlertNotifier;
import interactivespaces.service.mail.sender.MailSenderService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.ros.osgi.common.RosEnvironment;

/**
 * An OSGi activator for a mail alert notifier.
 * 
 * @author Keith M. Hughes
 */
public class OsgiMailAlertNotifierActivator implements BundleActivator {

	/**
	 * OSGi service tracker for the interactive spaces environment.
	 */
	private MyServiceTracker<InteractiveSpacesEnvironment> interactiveSpacesEnvironmentTracker;
	private MyServiceTracker<MailSenderService> interactiveSpacesSystemControlTracker;
	private MyServiceTracker<AlertService> rosEnvironmentTracker;

	private BundleContext bundleContext;
	private BasicMailAlertNotifier mailAlertNotifier;

	/**
	 * Object to give lock for putting this bundle's services together.
	 */
	private Object serviceLock = new Object();

	@Override
	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;

		interactiveSpacesEnvironmentTracker = newMyServiceTracker(context,
				InteractiveSpacesEnvironment.class.getName());

		interactiveSpacesSystemControlTracker = newMyServiceTracker(context,
				InteractiveSpacesSystemControl.class.getName());

		rosEnvironmentTracker = newMyServiceTracker(context,
				RosEnvironment.class.getName());

		interactiveSpacesEnvironmentTracker.open();
		rosEnvironmentTracker.open();
		interactiveSpacesSystemControlTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		interactiveSpacesEnvironmentTracker.close();
		interactiveSpacesSystemControlTracker.close();
		rosEnvironmentTracker.close();

		if (mailAlertNotifier != null) {
			mailAlertNotifier.shutdown();
			mailAlertNotifier = null;
		}
	}

	private void gotAnotherReference() {
		synchronized (serviceLock) {
			InteractiveSpacesEnvironment spaceEnvironment = interactiveSpacesEnvironmentTracker
					.getMyService();
			MailSenderService mailSenderService = interactiveSpacesSystemControlTracker
					.getMyService();
			AlertService alertService = rosEnvironmentTracker.getMyService();

			if (spaceEnvironment != null && mailSenderService != null
					&& alertService != null && rosEnvironmentTracker != null) {
				mailAlertNotifier = new BasicMailAlertNotifier(alertService,
						mailSenderService, spaceEnvironment);
				mailAlertNotifier.startup();
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
