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

package interactivespaces.system.bootstrap.osgi;

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.FileSystemConfigurationStorageManager;
import interactivespaces.configuration.SystemConfigurationStorageManager;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.evaluation.SimpleExpressionEvaluatorFactory;
import interactivespaces.system.BasicInteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.system.core.configuration.ConfigurationProvider;
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.system.internal.osgi.OsgiInteractiveSpacesSystemControl;
import interactivespaces.system.internal.osgi.RosOsgiInteractiveSpacesEnvironment;
import interactivespaces.time.LocalTimeProvider;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ros.concurrent.DefaultScheduledExecutorService;
import org.ros.master.uri.MasterUriProvider;
import org.ros.master.uri.StaticMasterUriProvider;
import org.ros.master.uri.SwitchableMasterUriProvider;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.common.SimpleRosEnvironment;

/**
 * Activate general services needed by a Spaces container.
 * 
 * @author Keith M. Hughes
 */
public class GeneralInteractiveSpacesSupportActivator implements
		BundleActivator {

	/**
	 * Property containing the Interactive Spaces root dir. This will be an
	 * absolute path.
	 */
	public static final String PROPERTY_INTERACTIVESPACES_BASE_INSTALL_DIR = "interactivespaces.rootdir";

	/**
	 * Threadpool for everyone to use.
	 */
	private ScheduledExecutorService executorService;

	/**
	 * Interactive Spaces environment for the container.
	 */
	private RosOsgiInteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * The Interactive Spaces-wide file system.
	 */
	private BasicInteractiveSpacesFilesystem filesystem;

	/**
	 * ROS environment for the container.
	 */
	private SimpleRosEnvironment rosEnvironment;

	/**
	 * The storage manager for system configurations.
	 */
	private SystemConfigurationStorageManager systemConfigurationStorageManager;

	/**
	 * Factory for expression evaluators.
	 */
	private SimpleExpressionEvaluatorFactory expressionEvaluatorFactory;

	/**
	 * The system control for Interactive Spaces.
	 */
	private OsgiInteractiveSpacesSystemControl systemControl;

	/**
	 * The ROS master URI provider in use.
	 */
	private SwitchableMasterUriProvider masterUriProvider;

	/**
	 * The platform logging provider.
	 */
	private LoggingProvider loggingProvider;

	/**
	 * The platform configuration provider.
	 */
	private ConfigurationProvider configurationProvider;

	/**
	 * Start up the activator.
	 */
	public void start(BundleContext context) throws Exception {
		String baseInstallDirProperty = context
				.getProperty(PROPERTY_INTERACTIVESPACES_BASE_INSTALL_DIR);
		File baseInstallDir = new File(baseInstallDirProperty);

		try {
			getCoreServices(context);

			setupSpaceEnvironment(context, baseInstallDir);

			registerServices(context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	}

	/**
	 * Register all services which need to be made available to others.
	 * 
	 * @param context
	 */
	private void registerServices(BundleContext context) {
		context.registerService(ExpressionEvaluatorFactory.class.getName(),
				expressionEvaluatorFactory, null);
		context.registerService(
				SystemConfigurationStorageManager.class.getName(),
				systemConfigurationStorageManager, null);
		context.registerService(InteractiveSpacesEnvironment.class.getName(),
				spaceEnvironment, null);
		context.registerService(RosEnvironment.class.getName(), rosEnvironment,
				null);
		context.registerService(InteractiveSpacesSystemControl.class.getName(),
				systemControl, null);
		context.registerService(SwitchableMasterUriProvider.class.getName(),
				masterUriProvider, null);
	}

	/**
	 * Set up the {@link InteractiveSpacesEnvironment} everyone should use.
	 * 
	 * @param context
	 *            the OSGi bundle context
	 * @param containerProperties
	 *            properties for the base container
	 * @param baseInstallDir
	 *            the base directory where Interactive Spaces is installed
	 */
	private void setupSpaceEnvironment(BundleContext context,
			File baseInstallDir) {
		Map<String, String> containerProperties = configurationProvider
				.getInitialConfiguration();

		systemControl = new OsgiInteractiveSpacesSystemControl(context);

		executorService = new DefaultScheduledExecutorService();

		filesystem = new BasicInteractiveSpacesFilesystem(baseInstallDir);
		filesystem.startup();

		spaceEnvironment = new RosOsgiInteractiveSpacesEnvironment();
		spaceEnvironment.setExecutorService(executorService);
		spaceEnvironment.setLoggingProvider(loggingProvider);
		spaceEnvironment.setFilesystem(filesystem);
		spaceEnvironment.setNetworkType(containerProperties
				.get(InteractiveSpacesEnvironment.CONFIGURATION_NETWORK_TYPE));

		setupSystemConfiguration(context, containerProperties);

		spaceEnvironment.setTimeProvider(new LocalTimeProvider());

		setupRosEnvironment(context, containerProperties,
				loggingProvider.getLog());

		spaceEnvironment.setValue("environment.ros", rosEnvironment);
	}

	/**
	 * Set up the full ROS environment.
	 * 
	 * @param context
	 * @param containerProperties
	 * @param log
	 */
	private void setupRosEnvironment(BundleContext context,
			Map<String, String> containerProperties, Log log) {
		rosEnvironment = new SimpleRosEnvironment();
		rosEnvironment.setExecutorService(executorService);
		rosEnvironment.setLog(spaceEnvironment.getLog());
		rosEnvironment
				.setMaster(InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_TYPE_MASTER.equals(containerProperties
						.get(InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_TYPE)));
		rosEnvironment.setNetworkType(containerProperties
				.get(InteractiveSpacesEnvironment.CONFIGURATION_NETWORK_TYPE));

		for (Entry<String, String> entry : containerProperties.entrySet()) {
			rosEnvironment.setProperty(entry.getKey(), entry.getValue());
		}

		configureRosFromInteractiveSpaces(containerProperties);

		// Want to start Interactive Spaces with no master URI unless there was
		// one in the config properties.
		rosEnvironment.setMasterUri(null);
		rosEnvironment.startup();

		MasterUriProvider baseProvider = null;
		URI masterUri = rosEnvironment.getMasterUri();
		if (masterUri != null) {
			log.info(String.format("Have initial ROS master URI %s", masterUri));
			baseProvider = new StaticMasterUriProvider(masterUri);
		}

		masterUriProvider = new SwitchableMasterUriProvider(baseProvider);
		rosEnvironment.setMasterUriProvider(masterUriProvider);
	}

	/**
	 * Configure the ROS environment from the interactive spaces properties.
	 * 
	 * @param containerProperties
	 *            the properties from the container configuration
	 */
	private void configureRosFromInteractiveSpaces(
			Map<String, String> containerProperties) {
		rosEnvironment
				.setProperty(
						RosEnvironment.PROPERTY_ROS_NODE_NAME,
						"/"
								+ containerProperties
										.get(InteractiveSpacesEnvironment.CONFIGURATION_HOSTID));
		rosEnvironment.setProperty(RosEnvironment.PROPERTY_ROS_NETWORK_TYPE,
				spaceEnvironment.getNetworkType());
		rosEnvironment
				.setProperty(
						RosEnvironment.CONFIGURATION_ROS_CONTAINER_TYPE,
						spaceEnvironment
								.getSystemConfiguration()
								.getRequiredPropertyString(
										InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_TYPE));
		rosEnvironment
				.setProperty(
						RosEnvironment.PROPERTY_ROS_HOST,
						spaceEnvironment
								.getSystemConfiguration()
								.getRequiredPropertyString(
										InteractiveSpacesEnvironment.CONFIGURATION_HOSTNAME));
	}

	/**
	 * Set up the system configuration.
	 * 
	 * @param context
	 */
	private void setupSystemConfiguration(BundleContext context,
			Map<String, String> containerProperties) {
		expressionEvaluatorFactory = new SimpleExpressionEvaluatorFactory();

		FileSystemConfigurationStorageManager fileSystemConfigurationStorageManager = new FileSystemConfigurationStorageManager();
		fileSystemConfigurationStorageManager.setLog(spaceEnvironment.getLog());
		fileSystemConfigurationStorageManager
				.setExpressionEvaluatorFactory(expressionEvaluatorFactory);
		fileSystemConfigurationStorageManager
				.setInteractiveSpacesFilesystem(filesystem);

		systemConfigurationStorageManager = fileSystemConfigurationStorageManager;
		systemConfigurationStorageManager.startup();

		Configuration systemConfiguration = systemConfigurationStorageManager
				.getSystemConfiguration();

		for (Entry<String, String> entry : containerProperties.entrySet()) {
			systemConfiguration.setValue(entry.getKey(), entry.getValue());
		}

		spaceEnvironment.setSystemConfiguration(systemConfiguration);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		systemConfigurationStorageManager.shutdown();
		systemConfigurationStorageManager = null;

		rosEnvironment.shutdown();
		rosEnvironment = null;
	}
}
