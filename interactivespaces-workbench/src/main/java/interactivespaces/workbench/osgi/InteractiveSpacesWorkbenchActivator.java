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

package interactivespaces.workbench.osgi;

import interactivespaces.system.core.configuration.ConfigurationProvider;
import interactivespaces.system.core.container.ContainerCustomizerProvider;
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.ui.WorkbenchUi;

import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 * OSGi activator for the Interactive Spaces Workbench.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbenchActivator implements BundleActivator {

	/**
	 * The workbench UI, if any.
	 */
	private WorkbenchUi ui;

	/**
	 * The context for the workbench's OSGi bundle.
	 */
	private BundleContext bundleContext;

	/**
	 * The platform logging provider.
	 */
	private LoggingProvider loggingProvider;

	/**
	 * The platform configuration provider.
	 */
	private ConfigurationProvider configurationProvider;

	/**
	 * The platform container customizer provider.
	 * 
	 * <p>
	 * Can be {@code null} if none is provided.
	 */
	private ContainerCustomizerProvider containerCustomizerProvider;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;

		getCoreServices(bundleContext);

		try {
			run();
		} catch (Exception e) {
			loggingProvider.getLog().error("Could not run workbench", e);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		// Nothing right now.
	}

	/**
	 * Get all core services needed.
	 * 
	 * <p>
	 * These services should have been provided by the OSGi container bootstrap
	 * and so will be immediately available. They will never go away since they
	 * are only destroyed when bundle 0 goes, which means the entire container
	 * is being shut down.
	 */
	private void getCoreServices(BundleContext context) throws Exception {
		ServiceReference loggingProviderServiceReference = context
				.getServiceReference(LoggingProvider.class.getName());
		loggingProvider = (LoggingProvider) context
				.getService(loggingProviderServiceReference);

		ServiceReference configurationProviderServiceReference = context
				.getServiceReference(ConfigurationProvider.class.getName());
		configurationProvider = (ConfigurationProvider) context
				.getService(configurationProviderServiceReference);

		ServiceReference containerCustomizerProviderServiceReference = context
				.getServiceReference(ContainerCustomizerProvider.class
						.getName());
		containerCustomizerProvider = (ContainerCustomizerProvider) context
				.getService(containerCustomizerProviderServiceReference);
	}

	/**
	 * Start the workbench running.
	 */
	public void run() {
		InteractiveSpacesWorkbench workbench = new InteractiveSpacesWorkbench(
				configurationProvider.getInitialConfiguration());

		List<String> commandLineArguments = containerCustomizerProvider.getCommandLineArguments();
		if (!commandLineArguments.isEmpty()) {
			try {
				workbench.doCommands(commandLineArguments);
			} finally {
				try {
					bundleContext.getBundle(0).stop();
				} catch (BundleException e) {
					loggingProvider.getLog().error("Error stopping container", e);
				}
			}
		} else {
			ui = new WorkbenchUi(workbench);
		}
	}
}
