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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.configuration.LiveActivityConfigurationManager;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.controller.logging.ActivityLogFactory;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.io.FileSupport;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.ros.concurrent.DefaultScheduledExecutorService;

import java.io.File;
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
  private ActivityInstallationManager activityInstallationManager;
  private LocalSpaceControllerRepository controllerRepository;
  private ActiveControllerActivityFactory activeControllerActivityFactory;
  private NativeActivityRunnerFactory nativeAppRunnerFactory;
  private LiveActivityConfigurationManager configurationManager;
  private ActivityStorageManager activityStorageManager;
  private ActivityLogFactory activityLogFactory;
  private SpaceControllerCommunicator controllerCommunicator;
  private InteractiveSpacesSystemControl spaceSystemControl;
  private SpaceControllerInfoPersister controllerInfoPersister;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private ServiceRegistry serviceRegistry;
  private ControllerDataBundleManager dataBundleManager;
  private InteractiveSpacesFilesystem systemFilesystem;

  private Configuration systemConfiguration;

  private FileSupport fileSupport;

  private Log log;

  @Before
  public void setup() {
    log = mock(Log.class);

    executorService = new DefaultScheduledExecutorService();

    activityInstallationManager = mock(ActivityInstallationManager.class);
    controllerRepository = mock(LocalSpaceControllerRepository.class);
    activeControllerActivityFactory = mock(ActiveControllerActivityFactory.class);
    nativeAppRunnerFactory = mock(NativeActivityRunnerFactory.class);
    configurationManager = mock(LiveActivityConfigurationManager.class);
    activityStorageManager = mock(ActivityStorageManager.class);
    activityLogFactory = mock(ActivityLogFactory.class);
    controllerCommunicator = mock(SpaceControllerCommunicator.class);
    spaceSystemControl = mock(InteractiveSpacesSystemControl.class);
    controllerInfoPersister = mock(SpaceControllerInfoPersister.class);
    systemConfiguration = SimpleConfiguration.newConfiguration();
    dataBundleManager = mock(SpaceControllerDataBundleManager.class);

    systemFilesystem = mock(InteractiveSpacesFilesystem.class);

    timeProvider = mock(TimeProvider.class);

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
    systemConfiguration.setValue(
        InteractiveSpacesEnvironment.CONFIGURATION_CONTAINER_FILE_CONTROLLABLE, "false");

    controller =
        new StandardSpaceController(activityInstallationManager, controllerRepository,
            activeControllerActivityFactory, nativeAppRunnerFactory, configurationManager,
            activityStorageManager, activityLogFactory, controllerCommunicator,
            controllerInfoPersister, spaceSystemControl, spaceEnvironment, dataBundleManager);

    fileSupport = mock(FileSupport.class);
    controller.setFileSupport(fileSupport);

    controller.startup();
  }

  @After
  public void cleanup() {
    controller.shutdown();
    executorService.shutdown();
  }

  /**
   * A live activity has an active instance in the controller and it is running.
   * Startup should generate an RUNNING event.
   */
  @Test
  public void testStartupActivityAlreadyRunning() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        controller.startupActivity(activityUuid);
      }

    }, ActivityState.RUNNING, ActivityState.RUNNING);
  }

  /**
   * A live activity has an active instance in the controller and it is active.
   * Startup should generate an ACTIVE event.
   */
  @Test
  public void testStartupActivityAlreadyActive() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        controller.startupActivity(activityUuid);
      }

    }, ActivityState.ACTIVE, ActivityState.ACTIVE);
  }

  /**
   * A live activity has an active instance in the controller and it not
   * running. Shutdown should generate a READY event.
   */
  @Test
  public void testShutdownActivityNotRunning() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        controller.shutdownActivity(activityUuid);
      }

    }, ActivityState.READY, ActivityState.READY);
  }

  /**
   * A live activity has an active instance in the controller and it is active.
   * Activate should generate an ACTIVE event.
   */
  @Test
  public void testActivateActivityAlreadyActive() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        controller.activateActivity(activityUuid);
      }

    }, ActivityState.ACTIVE, ActivityState.ACTIVE);
  }

  private void tryActivityStateAction(ActivityAction action, ActivityState startState,
      ActivityState finishState) throws Exception {
    String activityUuid = "foop";
    InternalActivityFilesystem activityFilesystem = mock(InternalActivityFilesystem.class);
    when(activityStorageManager.getActivityFilesystem(activityUuid)).thenReturn(activityFilesystem);
    LiveActivityConfiguration configuration = mock(LiveActivityConfiguration.class);
    when(configurationManager.getConfiguration(activityFilesystem)).thenReturn(configuration);

    SimpleInstalledLiveActivity liveActivity = new SimpleInstalledLiveActivity();
    when(controllerRepository.getInstalledLiveActivityByUuid(activityUuid))
    .thenReturn(liveActivity);

    ActiveControllerActivity expectedActive = mock(ActiveControllerActivity.class);
    when(
        activeControllerActivityFactory.newActiveActivity(liveActivity, activityFilesystem,
            configuration, controller)).thenReturn(expectedActive);
    when(expectedActive.getCachedActivityStatus()).thenReturn(new ActivityStatus(startState, null));
    when(expectedActive.getActivityStatus()).thenReturn(new ActivityStatus(startState, null));
    when(expectedActive.getActivityState()).thenReturn(startState);
    when(expectedActive.getUuid()).thenReturn(activityUuid);
    ActiveControllerActivity active = controller.getActiveActivityByUuid(activityUuid, true);
    assertEquals(expectedActive, active);

    action.doit(activityUuid);
    Thread.sleep(1000);

    ArgumentCaptor<ActivityStatus> activityStatus = ArgumentCaptor.forClass(ActivityStatus.class);

    verify(controllerCommunicator, times(1)).publishActivityStatus(eq(activityUuid),
        activityStatus.capture());
    assertEquals(finishState, activityStatus.getValue().getState());
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

    controller.cleanActivityTmpData(uuid);

    verify(activityStorageManager, times(1)).cleanTmpActivityDataDirectory(uuid);
  }

  /**
   * Test trying to clean a live activity's temp dir when it is active.
   */
  @Test
  public void testCleanLiveActivityTempDataDirWhenRunning() {
    String uuid = "foo";

    InstalledLiveActivity installedActivity = mock(InstalledLiveActivity.class);
    when(installedActivity.getUuid()).thenReturn(uuid);
    ActiveControllerActivity active =
        new ActiveControllerActivity(installedActivity, null, null, null, controller);
    controller.addActiveActivity(uuid, active);
    active.setCachedActivityStatus(new ActivityStatus(ActivityState.RUNNING, ""));

    controller.cleanActivityTmpData(uuid);

    verify(activityStorageManager, times(0)).cleanTmpActivityDataDirectory(uuid);
  }

  /**
   * Test cleaning a live activity's permanent dir.
   */
  @Test
  public void testCleanLiveActivityPermanentDataDir() {
    String uuid = "foo";

    controller.cleanActivityPermanentData(uuid);

    verify(activityStorageManager, times(1)).cleanPermanentActivityDataDirectory(uuid);
  }

  /**
   * Test trying to clean a live activity's permanent dir when it is active.
   */
  @Test
  public void testCleanLiveActivityPermanentDataDirWhenRunning() {
    String uuid = "foo";

    InstalledLiveActivity installedActivity = mock(InstalledLiveActivity.class);
    when(installedActivity.getUuid()).thenReturn(uuid);
    ActiveControllerActivity active =
        new ActiveControllerActivity(installedActivity, null, null, null, controller);
    controller.addActiveActivity(uuid, active);
    active.setCachedActivityStatus(new ActivityStatus(ActivityState.RUNNING, ""));

    controller.cleanActivityPermanentData(uuid);

    verify(activityStorageManager, times(0)).cleanPermanentActivityDataDirectory(uuid);
  }

  /**
   * An action for an activity to do in the middle of a test sequence.
   *
   * @author Keith M. Hughes
   */
  public interface ActivityAction {

    /**
     * The operation to do for a given UUID
     *
     * @param uuid
     *          UUID of the activity
     */
    void doit(String uuid);
  }
}
