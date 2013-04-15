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

package interactivespaces.controller.client.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.ActivityConfigurationManager;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.logging.ActivityLogFactory;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.concurrent.DefaultScheduledExecutorService;

/**
 * Unit tests for the {@link StandardSpaceController}.
 * 
 * @author Keith M. Hughes
 */
public class StandardSpaceControllerTest {
	private StandardSpaceController controller;

	private ScheduledExecutorService executorService;

	private ActivityInstallationManager activityInstallationManager;
	private LocalSpaceControllerRepository controllerRepository;
	private ActiveControllerActivityFactory activeControllerActivityFactory;
	private NativeActivityRunnerFactory nativeAppRunnerFactory;
	private ActivityConfigurationManager configurationManager;
	private ActivityStorageManager activityStorageManager;
	private ActivityLogFactory activityLogFactory;
	private SpaceControllerCommunicator controllerCommunicator;
	private InteractiveSpacesSystemControl spaceSystemControl;
	private InteractiveSpacesEnvironment spaceEnvironment;

	private Configuration systemConfiguration;

	private Log log;

	@Before
	public void setup() {
		log = mock(Log.class);

		//executorService = new DefaultScheduledExecutorService();

		activityInstallationManager = mock(ActivityInstallationManager.class);
		controllerRepository = mock(LocalSpaceControllerRepository.class);
		activeControllerActivityFactory = mock(ActiveControllerActivityFactory.class);
		nativeAppRunnerFactory = mock(NativeActivityRunnerFactory.class);
		configurationManager = mock(ActivityConfigurationManager.class);
		activityStorageManager = mock(ActivityStorageManager.class);
		activityLogFactory = mock(ActivityLogFactory.class);
		controllerCommunicator = mock(SpaceControllerCommunicator.class);
		spaceSystemControl = mock(InteractiveSpacesSystemControl.class);

		systemConfiguration = SimpleConfiguration.newConfiguration();

		spaceEnvironment = mock(InteractiveSpacesEnvironment.class);
		when(spaceEnvironment.getLog()).thenReturn(log);
		when(spaceEnvironment.getSystemConfiguration()).thenReturn(
				systemConfiguration);
		when(spaceEnvironment.getExecutorService()).thenReturn(
				executorService);
		
		systemConfiguration.setValue(
				SpaceController.CONFIGURATION_CONTROLLER_UUID, "abc123");
		systemConfiguration
				.setValue(SpaceController.CONFIGURATION_CONTROLLER_NAME,
						"testcontroller");
		systemConfiguration.setValue(
				SpaceController.CONFIGURATION_CONTROLLER_DESCRIPTION, "yipee");
		systemConfiguration.setValue(
				InteractiveSpacesEnvironment.CONFIGURATION_HOSTID, "gloop");

		controller = new StandardSpaceController(activityInstallationManager,
				controllerRepository, activeControllerActivityFactory,
				nativeAppRunnerFactory, configurationManager,
				activityStorageManager, activityLogFactory,
				controllerCommunicator, spaceSystemControl, spaceEnvironment);

		//controller.startup();
	}

	@After
	public void cleanup() {
//		controller.shutdown();
//		executorService.shutdown();
	}

	/**
	 * A live activity has an active instance in the controller and it not
	 * running. Shutdown should generate a shutdown event.
	 */
	@Test
	public void testShutdownActivityNotRunning() {
		// controller.shutdownActivity("foop");
	}
}
