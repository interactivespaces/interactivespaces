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

package interactivespaces.controller.internal.osgi;

import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.controller.activity.configuration.PropertyFileActivityConfigurationManager;
import interactivespaces.controller.client.node.SimpleActivityInstallationManager;
import interactivespaces.controller.client.node.SimpleActivityStorageManager;
import interactivespaces.controller.client.node.StandardSpaceController;
import interactivespaces.controller.client.node.internal.osgi.OsgiActiveControllerActivityFactory;
import interactivespaces.controller.client.node.ros.RosSpaceControllerActivityInstaller;
import interactivespaces.controller.client.node.ros.RosSpaceControllerCommunicator;
import interactivespaces.controller.logging.InteractiveSpacesEnvironmentActivityLogFactory;
import interactivespaces.controller.repository.internal.file.FileLocalSpaceControllerRepository;
import interactivespaces.controller.ui.internal.osgi.OsgiControllerShell;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.service.script.ScriptService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.ros.osgi.common.RosEnvironment;

/**
 * An OSGi activator for an Interactive Spaces controller
 * 
 * @author Keith M. Hughes
 */
public class OsgiControllerActivator implements BundleActivator {

	/**
	 * OSGi service tracker for the interactive spaces environment.
	 */
	private MyServiceTracker<InteractiveSpacesEnvironment> interactiveSpacesEnvironmentTracker;
	private MyServiceTracker<InteractiveSpacesSystemControl> interactiveSpacesSystemControlTracker;
	private MyServiceTracker<RosEnvironment> rosEnvironmentTracker;
	private MyServiceTracker<ExpressionEvaluatorFactory> expressionEvaluatorFactoryTracker;
	private MyServiceTracker<ScriptService> scriptServiceTracker;

	private BundleContext bundleContext;
	private SimpleActivityStorageManager activityStorageManager;
	private FileLocalSpaceControllerRepository controllerRepository;
	private SimpleActivityInstallationManager activityInstallationManager;
	private RosSpaceControllerActivityInstaller controllerActivityInstaller;
	private StandardSpaceController spaceController;
	private OsgiControllerShell controllerShell;

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

		expressionEvaluatorFactoryTracker = newMyServiceTracker(context,
				ExpressionEvaluatorFactory.class.getName());

		scriptServiceTracker = newMyServiceTracker(context,
				ScriptService.class.getName());

		scriptServiceTracker.open();
		expressionEvaluatorFactoryTracker.open();
		interactiveSpacesEnvironmentTracker.open();
		rosEnvironmentTracker.open();
		interactiveSpacesSystemControlTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		interactiveSpacesEnvironmentTracker.close();
		interactiveSpacesSystemControlTracker.close();
		rosEnvironmentTracker.close();
		expressionEvaluatorFactoryTracker.close();
		scriptServiceTracker.close();
		
		if (controllerShell != null) {
			controllerShell.deactivate();
			controllerShell = null;
		}
		
		if (spaceController != null) {
			spaceController.shutdown();
			spaceController = null;
		}
		
		if (activityInstallationManager != null) {
			activityInstallationManager.shutdown();
			activityInstallationManager = null;
		}
		
		if (controllerActivityInstaller != null) {
			controllerActivityInstaller.shutdown();
			controllerActivityInstaller = null;
		}
		
		if (activityStorageManager != null) {
			activityStorageManager.shutdown();
			activityStorageManager = null;
		}
		
		if (controllerRepository != null) {
			controllerRepository.shutdown();
			controllerRepository = null;
		}

	}

	private void gotAnotherReference() {
		synchronized (serviceLock) {
			InteractiveSpacesEnvironment spaceEnvironment = interactiveSpacesEnvironmentTracker
					.getMyService();
			InteractiveSpacesSystemControl spaceSystemControl = interactiveSpacesSystemControlTracker
					.getMyService();
			RosEnvironment rosEnvironment = rosEnvironmentTracker
					.getMyService();
			ExpressionEvaluatorFactory expressionEvaluatorFactory = expressionEvaluatorFactoryTracker
					.getMyService();
			ScriptService scriptService = scriptServiceTracker.getMyService();

			if (spaceEnvironment != null && spaceSystemControl != null
					&& rosEnvironment != null && rosEnvironmentTracker != null
					&& expressionEvaluatorFactory != null
					&& scriptService != null) {
				activityStorageManager = new SimpleActivityStorageManager(
						spaceEnvironment);
				activityStorageManager.startup();

				controllerRepository = new FileLocalSpaceControllerRepository(
						activityStorageManager, spaceEnvironment);
				controllerRepository.startup();

				PropertyFileActivityConfigurationManager activityConfigurationManager = new PropertyFileActivityConfigurationManager(
						expressionEvaluatorFactory, spaceEnvironment);

				activityInstallationManager = new SimpleActivityInstallationManager(
						controllerRepository, activityStorageManager,
						spaceEnvironment);
				activityInstallationManager.startup();

				OsgiActiveControllerActivityFactory controllerActivityFactory = new OsgiActiveControllerActivityFactory(
						bundleContext);
				controllerActivityFactory.setScriptService(scriptService);

				controllerActivityInstaller = new RosSpaceControllerActivityInstaller(
						activityInstallationManager, spaceEnvironment);
				controllerActivityInstaller.startup();

				SimpleNativeActivityRunnerFactory nativeActivityRunnerFactory = new SimpleNativeActivityRunnerFactory(
						spaceEnvironment);

				InteractiveSpacesEnvironmentActivityLogFactory activityLogFactory = new InteractiveSpacesEnvironmentActivityLogFactory(
						spaceEnvironment);

				RosSpaceControllerCommunicator spaceControllerCommunicator = new RosSpaceControllerCommunicator(
						controllerActivityInstaller, rosEnvironment,
						spaceEnvironment);

				spaceController = new StandardSpaceController(
						activityInstallationManager, controllerRepository,
						controllerActivityFactory, nativeActivityRunnerFactory,
						activityConfigurationManager, activityStorageManager,
						activityLogFactory, spaceControllerCommunicator,
						spaceSystemControl, spaceEnvironment);
				spaceController.startup();

				controllerShell = new OsgiControllerShell(
						spaceController, spaceSystemControl,
						controllerRepository, bundleContext);
				controllerShell.startup();
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
