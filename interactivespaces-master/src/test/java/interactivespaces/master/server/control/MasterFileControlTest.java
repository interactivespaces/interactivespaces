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

import interactivespaces.master.api.MasterApiAutomationManager;
import interactivespaces.master.api.MasterApiSpaceControllerManager;
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

  private MasterApiSpaceControllerManager masterApiControllerManager;

  private MasterApiAutomationManager masterApiAutomationManager;

  private MasterFileControl fileControl;

  private Log log;

  @Before
  public void setup() {
    log = mock(Log.class);
    spaceEnvironment = mock(InteractiveSpacesEnvironment.class);
    when(spaceEnvironment.getLog()).thenReturn(log);

    spaceSystemControl = mock(InteractiveSpacesSystemControl.class);
    masterApiControllerManager = mock(MasterApiSpaceControllerManager.class);
    masterApiAutomationManager = mock(MasterApiAutomationManager.class);

    fileControl = new MasterFileControl();
    fileControl.setSpaceEnvironment(spaceEnvironment);
    fileControl.setSpaceSystemControl(spaceSystemControl);
    fileControl.setMasterApiSpaceControllerManager(masterApiControllerManager);
    fileControl.setMasterApiAutomationManager(masterApiAutomationManager);
  }

  /**
   * Make sure shutdown is called on control if a shutdown command is received.
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
    fileControl.handleCommand(MasterFileControl.COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL);

    verify(masterApiControllerManager, times(1)).shutdownAllSpaceControllers();
  }

  /**
   * Make sure shutdown all activities on all controllers is called on if a
   * shutdown all activities on all controllers command is received.
   */
  @Test
  public void testShutdownAllActivitiesAllControllersCall() {
    fileControl.handleCommand(MasterFileControl.COMMAND_SPACE_CONTROLLERS_SHUTDOWN_ALL_ACTIVITIES);

    verify(masterApiControllerManager, times(1)).shutdownAllActivitiesAllSpaceControllers();
  }

  /**
   * Test starting up a live activity group.
   */
  @Test
  public void testStartupLiveActivityCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_STARTUP + id);

    verify(masterApiControllerManager, times(1)).startupLiveActivityGroup(id);
  }

  /**
   * Test shutting down a live activity group.
   */
  @Test
  public void testShutdownLiveActivityCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_SHUTDOWN + id);

    verify(masterApiControllerManager, times(1)).shutdownLiveActivityGroup(id);
  }

  /**
   * Test activating a live activity group.
   */
  @Test
  public void testActivateLiveActivityCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_ACTIVATE + id);

    verify(masterApiControllerManager, times(1)).activateLiveActivityGroup(id);
  }

  /**
   * Test deactivating a live activity group.
   */
  @Test
  public void testDeactivateLiveActivityCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_LIVE_ACTIVITY_GROUP_DEACTIVATE + id);

    verify(masterApiControllerManager, times(1)).deactivateLiveActivityGroup(id);
  }

  /**
   * Test starting up a space.
   */
  @Test
  public void testStartupSpaceCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_SPACE_STARTUP + id);

    verify(masterApiControllerManager, times(1)).startupSpace(id);
  }

  /**
   * Test shut down a space.
   */
  @Test
  public void testShutdownSpaceCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_SPACE_SHUTDOWN + id);

    verify(masterApiControllerManager, times(1)).shutdownSpace(id);
  }

  /**
   * Test activating a space.
   */
  @Test
  public void testActivateSpaceCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_SPACE_ACTIVATE + id);

    verify(masterApiControllerManager, times(1)).activateSpace(id);
  }

  /**
   * Test deactivating a space.
   */
  @Test
  public void testDectivateSpaceCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_SPACE_DEACTIVATE + id);

    verify(masterApiControllerManager, times(1)).deactivateSpace(id);
  }

  /**
   * Test running a script.
   */
  @Test
  public void testRunScriptCall() {
    String id = "123454321";
    fileControl.handleCommand(MasterFileControl.COMMAND_PREFIX_SCRIPT_RUN + id);

    verify(masterApiAutomationManager, times(1)).runScript(id);
  }
}
