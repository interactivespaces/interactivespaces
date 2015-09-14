/*
 * Copyright (C) 2014 Google Inc.
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

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.SettableTimeProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests for the {@link StandardLiveActivityRunner}.
 *
 * <p>
 * It is only necessary to check failure of startup here as operations other than the actual activity itself can fail,
 * such as the loading of the activity.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRunnerTest {
  private StandardLiveActivityRunner runner;
  private InstalledLiveActivity installedActivity;
  private ActivityWrapper activityWrapper;
  private InternalLiveActivityFilesystem activityFilesystem;
  private LiveActivityConfiguration configuration;
  private LiveActivityRunnerListener liveActivityRunnerListener;
  private LiveActivityRuntime liveActivityRuntime;
  private Activity instance;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private SettableTimeProvider timeProvider;

  @Before
  public void setup() {
    instance = Mockito.mock(Activity.class);
    installedActivity = Mockito.mock(InstalledLiveActivity.class);
    activityWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(activityWrapper.newInstance()).thenReturn(instance);
    activityFilesystem = Mockito.mock(InternalLiveActivityFilesystem.class);
    configuration = Mockito.mock(LiveActivityConfiguration.class);
    liveActivityRunnerListener = Mockito.mock(LiveActivityRunnerListener.class);
    liveActivityRuntime = Mockito.mock(LiveActivityRuntime.class);

    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
    timeProvider = new SettableTimeProvider();

    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    Mockito.when(liveActivityRuntime.getSpaceEnvironment()).thenReturn(spaceEnvironment);

    runner =
        new StandardLiveActivityRunner(installedActivity, activityWrapper, activityFilesystem, configuration,
            liveActivityRunnerListener, liveActivityRuntime);
  }

  /**
   * Test a successful startup.
   */
  @Test
  public void testStartupSuccess() {
    runner.startup();

    Mockito.verify(instance).startup();
  }

  /**
   * Test an unsuccessful startup.
   */
  @Test
  public void testStartupFail() {
    Mockito.when(activityWrapper.newInstance()).thenThrow(new RuntimeException());
    runner.startup();

    Mockito.verify(instance, Mockito.times(0)).startup();
    Mockito.verify(liveActivityRunnerListener).onNoInstanceActivityStatusEvent(runner);
  }

  /**
   * Test a successful shutdown.
   */
  @Test
  public void testShutdownSuccess() {
    runner.setInstance(instance);
    runner.shutdown();

    Mockito.verify(instance).shutdown();
    Mockito.verify(instance).checkActivityState();
    Mockito.verify(instance).getActivityStatus();
    Mockito.verify(activityWrapper).done();
  }

  /**
   * Test a failed shutdown.
   */
  @Test
  public void testShutdownFail() {
    Throwable exception = new RuntimeException();
    Mockito.doThrow(exception).when(instance).shutdown();

    runner.setInstance(instance);
    runner.shutdown();

    Mockito.verify(instance, Mockito.times(0)).checkActivityState();
    Mockito.verify(instance, Mockito.times(0)).getActivityStatus();
    Mockito.verify(activityWrapper).done();

    ArgumentCaptor<ActivityStatus> activityStatus = ArgumentCaptor.forClass(ActivityStatus.class);
    Mockito.verify(instance).setActivityStatus(activityStatus.capture());

    Assert.assertEquals(exception, activityStatus.getValue().getException());
    Assert.assertEquals(ActivityState.SHUTDOWN_FAILURE, activityStatus.getValue().getState());
  }

  /**
   * Test a successful activate.
   */
  @Test
  public void testActivateSuccess() {
    runner.setInstance(instance);
    runner.activate();

    Mockito.verify(instance).activate();
  }

  /**
   * Test a failed activate.
   */
  @Test
  public void testActivateFail() {
    Throwable exception = new RuntimeException();
    Mockito.doThrow(exception).when(instance).activate();

    runner.setInstance(instance);
    runner.activate();

    ArgumentCaptor<ActivityStatus> activityStatus = ArgumentCaptor.forClass(ActivityStatus.class);
    Mockito.verify(instance).setActivityStatus(activityStatus.capture());

    Assert.assertEquals(exception, activityStatus.getValue().getException());
    Assert.assertEquals(ActivityState.ACTIVATE_FAILURE, activityStatus.getValue().getState());
  }

  /**
   * Test a successful deactivate.
   */
  @Test
  public void testDectivateSuccess() {
    runner.setInstance(instance);
    runner.deactivate();

    Mockito.verify(instance).deactivate();
  }

  /**
   * Test a failed deactivate.
   */
  @Test
  public void testDeactivateFail() {
    Throwable exception = new RuntimeException();
    Mockito.doThrow(exception).when(instance).deactivate();

    runner.setInstance(instance);
    runner.deactivate();

    ArgumentCaptor<ActivityStatus> activityStatus = ArgumentCaptor.forClass(ActivityStatus.class);
    Mockito.verify(instance).setActivityStatus(activityStatus.capture());

    Assert.assertEquals(exception, activityStatus.getValue().getException());
    Assert.assertEquals(ActivityState.DEACTIVATE_FAILURE, activityStatus.getValue().getState());
  }

  /**
   * Test sampling the runner.
   */
  @Test
  public void testSampling() {
    ActivityState testState = ActivityState.ACTIVE;
    String testDescription = "goober";
    ActivityStatus testStatus = new ActivityStatus(testState, testDescription);
    Mockito.when(instance.getActivityStatus()).thenReturn(testStatus);
    runner.setInstance(instance);
    runner.sampleActivityStatus();

    Mockito.verify(instance).checkActivityState();
    Assert.assertEquals(testStatus, runner.getCachedActivityStatus());
  }
}
