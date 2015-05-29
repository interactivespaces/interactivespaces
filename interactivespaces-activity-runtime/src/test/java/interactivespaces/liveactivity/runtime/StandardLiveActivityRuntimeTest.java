/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.alert.AlertStatusManager;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfigurationManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.liveactivity.runtime.logging.LiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import interactivespaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.concurrency.ImmediateRunSequentialEventQueue;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests for the {@link StandardLiveActivityRuntime}.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRuntimeTest {
  private StandardLiveActivityRuntime liveActivityRuntime;

  private TimeProvider timeProvider;
  private ActivityInstallationManager liveActivityInstallationManager;
  private LocalLiveActivityRepository liveActivityRepository;
  private LiveActivityRunnerFactory liveActivityRunnerFactory;
  private NativeActivityRunnerFactory nativeAppRunnerFactory;
  private LiveActivityConfigurationManager liveActivityConfigurationManager;
  private LiveActivityStorageManager liveActivityStorageManager;
  private LiveActivityLogFactory liveActivityLogFactory;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private ServiceRegistry serviceRegistry;
  private AlertStatusManager alertStatusManager;
  private LiveActivityRunnerSampler liveActivityRunnerSampler;
  private LiveActivityStatusPublisher liveActivityStatusPublisher;
  private LiveActivityRuntimeComponentFactory liveActivityRuntimeComponentFactory;
  private RemoteLiveActivityRuntimeMonitorService runtimeDebugService;

  private Log log;

  @Before
  public void setup() {
    log = mock(Log.class);

    liveActivityInstallationManager = mock(ActivityInstallationManager.class);
    liveActivityRepository = mock(LocalLiveActivityRepository.class);
    liveActivityRunnerFactory = mock(LiveActivityRunnerFactory.class);
    nativeAppRunnerFactory = mock(NativeActivityRunnerFactory.class);
    liveActivityConfigurationManager = mock(LiveActivityConfigurationManager.class);
    liveActivityStorageManager = mock(LiveActivityStorageManager.class);
    liveActivityLogFactory = mock(LiveActivityLogFactory.class);
    alertStatusManager = mock(AlertStatusManager.class);
    liveActivityRunnerSampler = mock(LiveActivityRunnerSampler.class);
    liveActivityStatusPublisher = mock(LiveActivityStatusPublisher.class);
    runtimeDebugService = mock(RemoteLiveActivityRuntimeMonitorService.class);

    liveActivityRuntimeComponentFactory = mock(LiveActivityRuntimeComponentFactory.class);
    when(liveActivityRuntimeComponentFactory.newLiveActivityRunnerFactory()).thenReturn(liveActivityRunnerFactory);

    timeProvider = mock(TimeProvider.class);

    spaceEnvironment = mock(InteractiveSpacesEnvironment.class);
    when(spaceEnvironment.getLog()).thenReturn(log);
    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    serviceRegistry = mock(ServiceRegistry.class);
    when(spaceEnvironment.getServiceRegistry()).thenReturn(serviceRegistry);

    liveActivityRuntime =
        new StandardLiveActivityRuntime(liveActivityRuntimeComponentFactory, liveActivityRepository,
            liveActivityInstallationManager, liveActivityLogFactory, liveActivityConfigurationManager,
            liveActivityStorageManager, alertStatusManager, new ImmediateRunSequentialEventQueue(),
            runtimeDebugService, spaceEnvironment);

    liveActivityRuntime.setAlertStatusManager(alertStatusManager);
    liveActivityRuntime.setLiveActivityRunnerSampler(liveActivityRunnerSampler);
    liveActivityRuntime.setLiveActivityStatusPublisher(liveActivityStatusPublisher);

    liveActivityRuntime.startup();
  }

  @After
  public void cleanup() {
    liveActivityRuntime.shutdown();
  }

  @Test
  public void testStartup() {
    Mockito.verify(liveActivityRunnerSampler).startup();
  }

  @Test
  public void testShutdown() {
    liveActivityRuntime.shutdown();

    Mockito.verify(liveActivityRunnerSampler).shutdown();
  }

  /**
   * A live activity has an active instance in the controller and it is running. Startup should generate an RUNNING
   * event.
   */
  @Test
  public void testStartupActivityAlreadyRunning() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        liveActivityRuntime.startupLiveActivity(activityUuid);
      }

    }, ActivityState.RUNNING, ActivityState.RUNNING);
  }

  /**
   * A live activity has an active instance in the controller and it is active. Startup should generate an ACTIVE event.
   */
  @Test
  public void testStartupActivityAlreadyActive() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        liveActivityRuntime.startupLiveActivity(activityUuid);
      }

    }, ActivityState.ACTIVE, ActivityState.ACTIVE);
  }

  /**
   * A live activity has an active instance in the controller and it not running. Shutdown should generate a READY
   * event.
   */
  @Test
  public void testShutdownActivityNotRunning() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        liveActivityRuntime.shutdownLiveActivity(activityUuid);
      }

    }, ActivityState.READY, ActivityState.READY);
  }

  /**
   * A live activity has an active instance in the controller and it is active. Activate should generate an ACTIVE
   * event.
   */
  @Test
  public void testActivateActivityAlreadyActive() throws Exception {
    tryActivityStateAction(new ActivityAction() {
      @Override
      public void doit(String activityUuid) {
        liveActivityRuntime.activateLiveActivity(activityUuid);
      }

    }, ActivityState.ACTIVE, ActivityState.ACTIVE);
  }

  /**
   * Try doing an state action.
   *
   * @param action
   *          the action
   * @param startState
   *          the start state
   * @param finishState
   *          the expected finish state
   *
   * @throws Exception
   *           any exception thrown by the action
   */
  private void tryActivityStateAction(ActivityAction action, ActivityState startState, ActivityState finishState)
      throws Exception {
    String activityUuid = "foop";
    SimpleInstalledLiveActivity liveActivity = new SimpleInstalledLiveActivity();
    when(liveActivityRepository.getInstalledLiveActivityByUuid(activityUuid)).thenReturn(liveActivity);

    InternalLiveActivityFilesystem activityFilesystem = mock(InternalLiveActivityFilesystem.class);
    when(liveActivityStorageManager.getActivityFilesystem(activityUuid)).thenReturn(activityFilesystem);
    LiveActivityConfiguration configuration = mock(LiveActivityConfiguration.class);

    when(liveActivityConfigurationManager.newLiveActivityConfiguration(liveActivity, activityFilesystem)).thenReturn(
        configuration);

    BasicLiveActivityRunner expectedActive = mock(BasicLiveActivityRunner.class);
    when(
        liveActivityRunnerFactory.newLiveActivityRunner(liveActivity, activityFilesystem, configuration,
            liveActivityRuntime, liveActivityRuntime)).thenReturn(expectedActive);
    when(expectedActive.getCachedActivityStatus()).thenReturn(new ActivityStatus(startState, null));
    when(expectedActive.sampleActivityStatus()).thenReturn(new ActivityStatus(startState, null));
    // when(expectedActive.getActivityState()).thenReturn(startState);
    when(expectedActive.getUuid()).thenReturn(activityUuid);
    LiveActivityRunner active = liveActivityRuntime.getLiveActivityRunnerByUuid(activityUuid, true);
    assertEquals(expectedActive, active);

    action.doit(activityUuid);
    Thread.sleep(1000);

    ArgumentCaptor<ActivityStatus> activityStatus = ArgumentCaptor.forClass(ActivityStatus.class);

    verify(liveActivityStatusPublisher, times(1)).publishActivityStatus(eq(activityUuid), activityStatus.capture());
    assertEquals(finishState, activityStatus.getValue().getState());
  }

  /**
   * Test cleaning a live activity's temp dir.
   */
  @Test
  public void testCleanLiveActivityTempDataDir() {
    String uuid = "foo";

    InstalledLiveActivity installedActivity = mock(InstalledLiveActivity.class);
    when(installedActivity.getUuid()).thenReturn(uuid);
    BasicLiveActivityRunner active =
        new BasicLiveActivityRunner(installedActivity, null, null, null, liveActivityRuntime, liveActivityRuntime);
    liveActivityRuntime.addLiveActivityRunner(uuid, active);
    active.setCachedActivityStatus(new ActivityStatus(ActivityState.READY, ""));

    liveActivityRuntime.cleanLiveActivityTmpData(uuid);

    verify(liveActivityStorageManager, times(1)).cleanTmpActivityDataDirectory(uuid);
  }

  /**
   * Test trying to clean a live activity's temp dir when it is active.
   */
  @Test
  public void testCleanLiveActivityTempDataDirWhenRunning() {
    String uuid = "foo";

    InstalledLiveActivity installedActivity = mock(InstalledLiveActivity.class);
    when(installedActivity.getUuid()).thenReturn(uuid);
    BasicLiveActivityRunner active =
        new BasicLiveActivityRunner(installedActivity, null, null, null, liveActivityRuntime, liveActivityRuntime);
    liveActivityRuntime.addLiveActivityRunner(uuid, active);
    active.setCachedActivityStatus(new ActivityStatus(ActivityState.RUNNING, ""));

    liveActivityRuntime.cleanLiveActivityTmpData(uuid);

    verify(liveActivityStorageManager, times(0)).cleanTmpActivityDataDirectory(uuid);
  }

  /**
   * Test cleaning a live activity's permanent dir.
   */
  @Test
  public void testCleanLiveActivityPermanentDataDir() {
    String uuid = "foo";

    InstalledLiveActivity installedActivity = mock(InstalledLiveActivity.class);
    when(installedActivity.getUuid()).thenReturn(uuid);
    BasicLiveActivityRunner active =
        new BasicLiveActivityRunner(installedActivity, null, null, null, liveActivityRuntime, liveActivityRuntime);
    liveActivityRuntime.addLiveActivityRunner(uuid, active);
    active.setCachedActivityStatus(new ActivityStatus(ActivityState.READY, ""));

    liveActivityRuntime.cleanLiveActivityPermanentData(uuid);

    verify(liveActivityStorageManager, times(1)).cleanPermanentActivityDataDirectory(uuid);
  }

  /**
   * Test trying to clean a live activity's permanent dir when it is active.
   */
  @Test
  public void testCleanLiveActivityPermanentDataDirWhenRunning() {
    String uuid = "foo";

    InstalledLiveActivity installedActivity = mock(InstalledLiveActivity.class);
    when(installedActivity.getUuid()).thenReturn(uuid);
    BasicLiveActivityRunner active =
        new BasicLiveActivityRunner(installedActivity, null, null, null, liveActivityRuntime, liveActivityRuntime);
    liveActivityRuntime.addLiveActivityRunner(uuid, active);
    active.setCachedActivityStatus(new ActivityStatus(ActivityState.RUNNING, ""));

    liveActivityRuntime.cleanLiveActivityPermanentData(uuid);

    verify(liveActivityStorageManager, times(0)).cleanPermanentActivityDataDirectory(uuid);
  }

  /**
   * Test handling a live activity notification issue.
   */
  @Test
  public void testHandlingLiveActivityRunnerNotification() {
    ActivityStatus status = new ActivityStatus(ActivityState.STARTUP_FAILURE, null);

    LiveActivityRunner runner = mock(LiveActivityRunner.class);
    when(runner.getCachedActivityStatus()).thenReturn(status);

    liveActivityRuntime.onNoInstanceActivityStatusEvent(runner);

    verify(alertStatusManager).announceLiveActivityStatus(runner);
  }

  /**
   * Test an activity successfully starting up through the activity event listening channels.
   */
  @Test
  public void testActivityStartupSuccess() {
    Activity activity = mock(Activity.class);
    String uuid = "foo";
    when(activity.getUuid()).thenReturn(uuid);

    ActivityStatus oldStatus = new ActivityStatus(ActivityState.READY, null);
    ActivityStatus newStatus = new ActivityStatus(ActivityState.RUNNING, null);

    LiveActivityRunner runner = mock(LiveActivityRunner.class);
    liveActivityRuntime.addLiveActivityRunner(uuid, runner);
    when(runner.sampleActivityStatus()).thenReturn(newStatus);

    liveActivityRuntime.getActivityListener().onActivityStatusChange(activity, oldStatus, newStatus);

    verify(liveActivityRunnerSampler).startSamplingRunner(runner);
    verify(liveActivityStatusPublisher).publishActivityStatus(uuid, newStatus);
    verify(alertStatusManager, times(0)).announceLiveActivityStatus(runner);
  }

  /**
   * Test an activity failing to start up through the activity event listening channels.
   */
  @Test
  public void testActivityStartupFailure() {
    Activity activity = mock(Activity.class);
    String uuid = "foo";
    when(activity.getUuid()).thenReturn(uuid);

    ActivityStatus oldStatus = new ActivityStatus(ActivityState.READY, null);
    ActivityStatus newStatus = new ActivityStatus(ActivityState.STARTUP_FAILURE, null);

    LiveActivityRunner runner = mock(LiveActivityRunner.class);
    liveActivityRuntime.addLiveActivityRunner(uuid, runner);
    when(runner.sampleActivityStatus()).thenReturn(newStatus);

    liveActivityRuntime.getActivityListener().onActivityStatusChange(activity, oldStatus, newStatus);

    verify(liveActivityRunnerSampler, times(0)).startSamplingRunner(runner);
    verify(liveActivityStatusPublisher).publishActivityStatus(uuid, newStatus);
    verify(alertStatusManager).announceLiveActivityStatus(runner);
  }

  /**
   * Test an activity successfully activating through the activity event listening channels.
   */
  @Test
  public void testActivityActivateSuccess() {
    Activity activity = mock(Activity.class);
    String uuid = "foo";
    when(activity.getUuid()).thenReturn(uuid);

    ActivityStatus oldStatus = new ActivityStatus(ActivityState.RUNNING, null);
    ActivityStatus newStatus = new ActivityStatus(ActivityState.ACTIVE, null);

    LiveActivityRunner runner = mock(LiveActivityRunner.class);
    liveActivityRuntime.addLiveActivityRunner(uuid, runner);
    when(runner.sampleActivityStatus()).thenReturn(newStatus);

    liveActivityRuntime.getActivityListener().onActivityStatusChange(activity, oldStatus, newStatus);

    verify(liveActivityRunnerSampler, times(0)).startSamplingRunner(runner);
    verify(liveActivityStatusPublisher).publishActivityStatus(uuid, newStatus);
    verify(alertStatusManager, times(0)).announceLiveActivityStatus(runner);
  }

  /**
   * Test an activity failing to activate through the activity event listening channels.
   */
  @Test
  public void testActivityActivateFailure() {
    Activity activity = mock(Activity.class);
    String uuid = "foo";
    when(activity.getUuid()).thenReturn(uuid);

    ActivityStatus oldStatus = new ActivityStatus(ActivityState.RUNNING, null);
    ActivityStatus newStatus = new ActivityStatus(ActivityState.ACTIVATE_FAILURE, null);

    LiveActivityRunner runner = mock(LiveActivityRunner.class);
    liveActivityRuntime.addLiveActivityRunner(uuid, runner);
    when(runner.sampleActivityStatus()).thenReturn(newStatus);

    liveActivityRuntime.getActivityListener().onActivityStatusChange(activity, oldStatus, newStatus);

    verify(liveActivityRunnerSampler, times(0)).startSamplingRunner(runner);
    verify(liveActivityStatusPublisher).publishActivityStatus(uuid, newStatus);
    verify(alertStatusManager).announceLiveActivityStatus(runner);
  }

  /**
   * An action for an activity to do in the middle of a test sequence.
   *
   * @author Keith M. Hughes
   */
  public interface ActivityAction {

    /**
     * The operation to do for a given UUID.
     *
     * @param uuid
     *          UUID of the activity
     */
    void doit(String uuid);
  }
}
