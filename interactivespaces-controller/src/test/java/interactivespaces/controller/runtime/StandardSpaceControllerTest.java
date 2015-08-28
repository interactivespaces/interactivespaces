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

package interactivespaces.controller.runtime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.container.control.message.activity.LiveActivityDeleteRequest;
import interactivespaces.container.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.runtime.configuration.SpaceControllerConfigurationManager;
import interactivespaces.liveactivity.runtime.LiveActivityRuntime;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.concurrency.ImmediateRunSequentialEventQueue;
import interactivespaces.util.io.FileSupport;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.concurrent.DefaultScheduledExecutorService;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Unit tests for the {@link StandardSpaceController}.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceControllerTest {
  private StandardSpaceController controller;

  private ScheduledExecutorService executorService;

  private TimeProvider timeProvider;
  private SpaceControllerCommunicator controllerCommunicator;
  private InteractiveSpacesSystemControl spaceSystemControl;
  private SpaceControllerInfoPersister controllerInfoPersister;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private ServiceRegistry serviceRegistry;
  private SpaceControllerDataBundleManager dataBundleManager;
  private InteractiveSpacesFilesystem systemFilesystem;
  private SpaceControllerConfigurationManager spaceControllerConfigurationManager;
  private LiveActivityRuntime liveActivityRuntime;
  private SpaceControllerActivityInstallationManager spaceControllerActivityInstallManager;

  private Configuration systemConfiguration;

  private FileSupport fileSupport;

  private Log log;

  @Before
  public void setup() {
    log = mock(Log.class);

    executorService = new DefaultScheduledExecutorService();

    controllerCommunicator = mock(SpaceControllerCommunicator.class);
    spaceSystemControl = mock(InteractiveSpacesSystemControl.class);
    controllerInfoPersister = mock(SpaceControllerInfoPersister.class);
    systemConfiguration = SimpleConfiguration.newConfiguration();
    dataBundleManager = mock(StandardSpaceControllerDataBundleManager.class);
    spaceControllerConfigurationManager = mock(SpaceControllerConfigurationManager.class);
    spaceControllerActivityInstallManager = mock(SpaceControllerActivityInstallationManager.class);

    systemFilesystem = mock(InteractiveSpacesFilesystem.class);

    timeProvider = mock(TimeProvider.class);

    liveActivityRuntime = mock(LiveActivityRuntime.class);

    spaceEnvironment = mock(InteractiveSpacesEnvironment.class);
    when(spaceEnvironment.getLog()).thenReturn(log);
    when(spaceEnvironment.getSystemConfiguration()).thenReturn(systemConfiguration);
    when(spaceEnvironment.getExecutorService()).thenReturn(executorService);
    when(spaceEnvironment.getFilesystem()).thenReturn(systemFilesystem);
    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    serviceRegistry = mock(ServiceRegistry.class);
    when(spaceEnvironment.getServiceRegistry()).thenReturn(serviceRegistry);

    systemConfiguration.setValue(SpaceController.CONFIGURATION_CONTROLLER_UUID, "abc123");
    systemConfiguration.setValue(SpaceController.CONFIGURATION_CONTROLLER_NAME, "testcontroller");
    systemConfiguration.setValue(SpaceController.CONFIGURATION_CONTROLLER_DESCRIPTION, "yipee");
    systemConfiguration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_HOSTID, "gloop");
    systemConfiguration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_FILE_CONTROLLABLE, "false");

    controller =
        new StandardSpaceController(spaceControllerActivityInstallManager, null, controllerCommunicator,
            controllerInfoPersister, spaceSystemControl, dataBundleManager, spaceControllerConfigurationManager,
            liveActivityRuntime, new ImmediateRunSequentialEventQueue(), spaceEnvironment);

    fileSupport = mock(FileSupport.class);
    controller.setFileSupport(fileSupport);

    controller.startup();
  }

  @After
  public void cleanup() {
    controller.shutdown();
    executorService.shutdown();
  }

  @Test
  public void testSetup() {
    verify(liveActivityRuntime).setLiveActivityStatusPublisher(controller);
  }

  /**
   * Test cleaning the permanent data directory.
   */
  @Test
  public void testCleanPermanentDataDir() {
    File systemDatadir = new File("permanent");

    when(systemFilesystem.getDataDirectory()).thenReturn(systemDatadir);

    controller.cleanControllerPermanentData();

    verify(fileSupport, times(1)).deleteDirectoryContents(systemDatadir);
  }

  /**
   * Test cleaning the permanent data directory.
   */
  @Test
  public void testCleanTempDataDir() {
    File systemDatadir = new File("temp");

    when(systemFilesystem.getTempDirectory()).thenReturn(systemDatadir);

    controller.cleanControllerTempData();

    verify(fileSupport, times(1)).deleteDirectoryContents(systemDatadir);
  }

  /**
   * Test cleaning a live activity's temp dir.
   */
  @Test
  public void testCleanLiveActivityTempDataDir() {
    String uuid = "foo";

    controller.cleanLiveActivityTmpData(uuid);

    verify(liveActivityRuntime, times(1)).cleanLiveActivityTmpData(uuid);
  }

  /**
   * Test cleaning a live activity's permanent dir.
   */
  @Test
  public void testCleanLiveActivityPermanentDataDir() {
    String uuid = "foo";

    controller.cleanLiveActivityPermanentData(uuid);

    verify(liveActivityRuntime, times(1)).cleanLiveActivityPermanentData(uuid);
  }

  /**
   * Test starting an activity.
   */
  @Test
  public void testLiveActivityStartup() {
    String uuid = "foo";

    controller.startupLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).startupLiveActivity(uuid);
  }

  /**
   * Test shutting down an activity.
   */
  @Test
  public void testLiveActivityShutdown() {
    String uuid = "foo";

    controller.shutdownLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).shutdownLiveActivity(uuid);
  }

  /**
   * Test activating an activity.
   */
  @Test
  public void testLiveActivityActivate() {
    String uuid = "foo";

    controller.activateLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).activateLiveActivity(uuid);
  }

  /**
   * Test deactivating an activity.
   */
  @Test
  public void testLiveActivityDeactivate() {
    String uuid = "foo";

    controller.deactivateLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).deactivateLiveActivity(uuid);
  }

  /**
   * Test getting the status of an activity.
   */
  @Test
  public void testLiveActivityStatus() {
    String uuid = "foo";

    controller.statusLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).statusLiveActivity(uuid);
  }

  /**
   * Test configuring an activity.
   */
  @Test
  public void testLiveActivityconfigure() {
    Map<String, String> configuration = Maps.newHashMap();
    configuration.put("a", "b");

    String uuid = "foo";

    controller.configureLiveActivity(uuid, configuration);

    verify(liveActivityRuntime, times(1)).configureLiveActivity(uuid, configuration);
  }

  /**
   * Test deleting a live activity when it is possible to delete.
   */
  @Test
  public void testLiveActivityDelete() {
    String uuid = "1.2.3.4";

    when(liveActivityRuntime.isLiveActivityRunning(uuid)).thenReturn(false);

    LiveActivityDeleteRequest request = new LiveActivityDeleteRequest(uuid, "foo", "1.2.3", false);

    controller.deleteLiveActivity(request);

    verify(spaceControllerActivityInstallManager).handleDeleteRequest(request);
  }

  /**
   * Test deleting a live activity when it is running.
   */
  @Test
  public void testLiveActivityDeleteWhenRunning() {
    String uuid = "1.2.3.4";

    when(liveActivityRuntime.isLiveActivityRunning(uuid)).thenReturn(true);

    LiveActivityDeleteRequest request = new LiveActivityDeleteRequest(uuid, "foo", "1.2.3", false);

    LiveActivityDeleteResponse response = controller.deleteLiveActivity(request);

    assertEquals(uuid, response.getUuid());
    assertEquals(LiveActivityDeleteResponse.LiveActivityDeleteStatus.FAILURE, response.getStatus());

    verify(spaceControllerActivityInstallManager, times(0)).handleDeleteRequest(request);
  }
}
