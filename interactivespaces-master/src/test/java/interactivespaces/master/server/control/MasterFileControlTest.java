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

package interactivespaces.master.server.control;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.master.server.ui.UiSpaceManager;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link MasterFileControl} class.
 * 
 * @author Keith M. Hughes
 */
public class MasterFileControlTest {

	private InteractiveSpacesEnvironment spaceEnvironment;

	private InteractiveSpacesSystemControl spaceSystemControl;

	private UiControllerManager uiControllerManager;

	private UiSpaceManager uiSpaceManager;

	private MasterFileControl fileControl;

	private Log log;

	@Before
	public void setup() {
		log = mock(Log.class);
		spaceEnvironment = mock(InteractiveSpacesEnvironment.class);
		when(spaceEnvironment.getLog()).thenReturn(log);

		spaceSystemControl = mock(InteractiveSpacesSystemControl.class);
		uiControllerManager = mock(UiControllerManager.class);
		uiSpaceManager = mock(UiSpaceManager.class);

		fileControl = new MasterFileControl();
		fileControl.setSpaceEnvironment(spaceEnvironment);
		fileControl.setSpaceSystemControl(spaceSystemControl);
		fileControl.setUiControllerManager(uiControllerManager);
		fileControl.setUiSpaceManager(uiSpaceManager);
	}

	/**
	 * Make sure shutdown is called on control if a shutdown command is
	 * received.
	 */
	@Test
	public void testShutdownCall() {
		fileControl.handleCommand(MasterFileControl.COMMAND_SHUTDOWN);

		verify(spaceSystemControl, times(1)).shutdown();
	}

	/**
	 * Make sure shutdown all controllers is called on if a shutdown all
	 * controllers command is received.
	 */
	@Test
	public void testShutdownAllControllersCall() {
		fileControl
				.handleCommand(MasterFileControl.COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL);

		verify(uiControllerManager, times(1)).shutdownAllControllers();
	}

	/**
	 * Make sure shutdown all activities on all controllers is called on if a
	 * shutdown all activities on all controllers command is received.
	 */
	@Test
	public void testShutdownAllActivitiesAllControllersCall() {
		fileControl
				.handleCommand(MasterFileControl.COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL_ACTIVITIES);

		verify(uiControllerManager, times(1))
				.shutdownAllActivitiesAllControllers();
	}

	/**
	 * Test starting up a live activity group.
	 */
	@Test
	public void testStartupLiveActivityCall() {
		String id = "123454321";
		fileControl
				.handleCommand(MasterFileControl.COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_STARTUP
						+ id);

		verify(uiControllerManager, times(1)).startupLiveActivityGroup(id);
	}

	/**
	 * Test activating a live activity group.
	 */
	@Test
	public void testActivateLiveActivityCall() {
		String id = "123454321";
		fileControl
				.handleCommand(MasterFileControl.COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_ACTIVATE
						+ id);

		verify(uiControllerManager, times(1)).activateLiveActivityGroup(id);
	}

	/**
	 * Test starting up a space.
	 */
	@Test
	public void testStartupSpaceCall() {
		String id = "123454321";
		fileControl
				.handleCommand(MasterFileControl.COMMAND_PREFIX_SPACE_STARTUP
						+ id);

		verify(uiSpaceManager, times(1)).startupSpace(id);
	}

	/**
	 * Test activating a space.
	 */
	@Test
	public void testActivateSpaceCall() {
		String id = "123454321";
		fileControl
				.handleCommand(MasterFileControl.COMMAND_PREFIX_SPACE_ACTIVATE
						+ id);

		verify(uiSpaceManager, times(1)).activateSpace(id);
	}
}
