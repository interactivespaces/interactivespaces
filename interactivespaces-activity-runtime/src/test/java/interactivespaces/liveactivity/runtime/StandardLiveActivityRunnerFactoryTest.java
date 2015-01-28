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

package interactivespaces.liveactivity.runtime;

import interactivespaces.controller.SpaceController;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.Version;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link StandardLiveActivityRunnerFactory}.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRunnerFactoryTest {

  private StandardLiveActivityRunnerFactory runnerFactory;
  private LiveActivityRunnerListener runnerListener;
  private SpaceController controller;
  private LiveActivityRuntime liveActivityRuntime;
  private LiveActivityConfiguration configuration;
  private InternalLiveActivityFilesystem filesystem;
  private InstalledLiveActivity liveActivity;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private Log log;

  @Before
  public void setup() {
    log = Mockito.mock(Log.class);
    runnerFactory = new StandardLiveActivityRunnerFactory(log);
    runnerListener = Mockito.mock(LiveActivityRunnerListener.class);
    controller = Mockito.mock(SpaceController.class);
    liveActivityRuntime = Mockito.mock(LiveActivityRuntime.class);
    filesystem = Mockito.mock(InternalLiveActivityFilesystem.class);
    liveActivity = Mockito.mock(InstalledLiveActivity.class);
    configuration = Mockito.mock(LiveActivityConfiguration.class);
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);
    Mockito.when(controller.getSpaceEnvironment()).thenReturn(spaceEnvironment);
  }

  /**
   * Test a type with no version.
   */
  @Test
  public void registerUnversionedType() {

    ActivityWrapperFactory wrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(wrapperFactory.getVersion()).thenReturn(null);
    Mockito.when(wrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper wrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(wrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(wrapper);

    runnerFactory.registerActivityWrapperFactory(wrapperFactory);

    LiveActivityRunner aactivity =
        runnerFactory.newLiveActivityRunner("foo", liveActivity, filesystem, configuration, runnerListener,
            liveActivityRuntime);
    Assert.assertEquals(wrapper, aactivity.getActivityWrapper());
  }

  /**
   * Test a type with no version being trumped by a version.
   */
  @Test
  public void getHighestVersion() {

    ActivityWrapperFactory unversionedWrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(unversionedWrapperFactory.getVersion()).thenReturn(null);
    Mockito.when(unversionedWrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper unversionedWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        unversionedWrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(unversionedWrapper);

    runnerFactory.registerActivityWrapperFactory(unversionedWrapperFactory);

    ActivityWrapperFactory versionedWrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(versionedWrapperFactory.getVersion()).thenReturn(new Version(1, 2, 3));
    Mockito.when(versionedWrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper versionedWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        versionedWrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(versionedWrapper);

    runnerFactory.registerActivityWrapperFactory(versionedWrapperFactory);

    LiveActivityRunner aactivity =
        runnerFactory.newLiveActivityRunner("foo", liveActivity, filesystem, configuration, runnerListener,
            liveActivityRuntime);
    Assert.assertEquals(versionedWrapper, aactivity.getActivityWrapper());
  }

  /**
   * Test make sure we get the 0.0.0 version if we ask for it.
   */
  @Test
  public void getExactVersion() {

    ActivityWrapperFactory unversionedWrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(unversionedWrapperFactory.getVersion()).thenReturn(null);
    Mockito.when(unversionedWrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper unversionedWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        unversionedWrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(unversionedWrapper);

    runnerFactory.registerActivityWrapperFactory(unversionedWrapperFactory);

    ActivityWrapperFactory versionedWrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(versionedWrapperFactory.getVersion()).thenReturn(new Version(1, 2, 3));
    Mockito.when(versionedWrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper versionedWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        versionedWrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(versionedWrapper);

    runnerFactory.registerActivityWrapperFactory(versionedWrapperFactory);

    LiveActivityRunner aactivity =
        runnerFactory.newLiveActivityRunner("foo;0.0.0", liveActivity, filesystem, configuration, runnerListener,
            liveActivityRuntime);
    Assert.assertEquals(unversionedWrapper, aactivity.getActivityWrapper());
  }

  /**
   * Test make sure we get a version in a requested range if we ask for it.
   */
  @Test
  public void getRangeVersion() {

    ActivityWrapperFactory version1WrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(version1WrapperFactory.getVersion()).thenReturn(new Version(1, 2, 3));
    Mockito.when(version1WrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper version1Wrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        version1WrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(version1Wrapper);

    runnerFactory.registerActivityWrapperFactory(version1WrapperFactory);

    ActivityWrapperFactory version2WrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(version2WrapperFactory.getVersion()).thenReturn(new Version(2, 3, 4));
    Mockito.when(version2WrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper version2Wrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        version2WrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(version2Wrapper);

    runnerFactory.registerActivityWrapperFactory(version2WrapperFactory);

    LiveActivityRunner aactivity =
        runnerFactory.newLiveActivityRunner("foo;[1.1, 1.3)", liveActivity, filesystem, configuration, runnerListener,
            liveActivityRuntime);
    Assert.assertEquals(version1Wrapper, aactivity.getActivityWrapper());
  }

  /**
   * Ask for a version range too high, make sure we fail.
   */
  @Test
  public void askForVersionTooHigh() {

    ActivityWrapperFactory unversionedWrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(unversionedWrapperFactory.getVersion()).thenReturn(null);
    Mockito.when(unversionedWrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper unversionedWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        unversionedWrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(unversionedWrapper);

    runnerFactory.registerActivityWrapperFactory(unversionedWrapperFactory);

    ActivityWrapperFactory versionedWrapperFactory = Mockito.mock(ActivityWrapperFactory.class);
    Mockito.when(versionedWrapperFactory.getVersion()).thenReturn(new Version(1, 2, 3));
    Mockito.when(versionedWrapperFactory.getActivityType()).thenReturn("foo");

    ActivityWrapper versionedWrapper = Mockito.mock(ActivityWrapper.class);
    Mockito.when(
        versionedWrapperFactory.newActivityWrapper(liveActivity, filesystem, configuration, liveActivityRuntime))
        .thenReturn(versionedWrapper);

    runnerFactory.registerActivityWrapperFactory(versionedWrapperFactory);

    try {
      runnerFactory.newLiveActivityRunner("foo;[5, 6)", liveActivity, filesystem, configuration, runnerListener,
          liveActivityRuntime);

      Assert.fail();
    } catch (Exception e) {
      // what is wanted
    }
  }

  /**
   * Test trying a type that wasn't there.
   */
  @Test
  public void testUnknownType() {
    try {
      runnerFactory.newLiveActivityRunner("foo", liveActivity, filesystem, configuration, runnerListener,
          liveActivityRuntime);

      Assert.fail();
    } catch (Exception e) {
      // expected
    }
  }
}
