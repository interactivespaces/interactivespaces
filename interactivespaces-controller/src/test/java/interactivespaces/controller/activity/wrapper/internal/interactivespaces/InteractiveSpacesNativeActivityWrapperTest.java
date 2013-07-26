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

package interactivespaces.controller.activity.wrapper.internal.interactivespaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.domain.pojo.SimpleInstalledLiveActivity;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link InteractiveSpacesNativeActivityWrapper}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesNativeActivityWrapperTest {

  private InstalledLiveActivity liveActivity;
  private ActivityFilesystem activityFilesystem;
  private Configuration configuration;
  private LiveActivityBundleLoader bundleLoader;

  private InteractiveSpacesNativeActivityWrapper wrapper;

  private File activityInstallFolder;

  @Before
  public void setup() {
    liveActivity = new SimpleInstalledLiveActivity();

    activityFilesystem = mock(ActivityFilesystem.class);

    activityInstallFolder = new File("activityinstall");
    when(activityFilesystem.getInstallDirectory()).thenReturn(activityInstallFolder);

    configuration = mock(Configuration.class);
    bundleLoader = mock(LiveActivityBundleLoader.class);

    wrapper =
        new InteractiveSpacesNativeActivityWrapper(liveActivity, activityFilesystem, configuration,
            bundleLoader);
  }

  /**
   * Test if a single new activity works.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSingleNew() throws Exception {
    String executable1 = "foo.jar";
    File executableFile1 = new File(activityInstallFolder, executable1);

    String bundleName = "foop";
    String bundleVersion = "1.0.0";

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);

    String className = "Activity1";

    when(
        configuration
            .getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE))
        .thenReturn(executable1);
    when(
        configuration
            .getRequiredPropertyString(InteractiveSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className);

    Class expectedActivityClass = Activity1.class;
    when(bundleLoader.getBundleClass(executableFile1, bundleName, bundleVersion, className))
        .thenReturn(expectedActivityClass);

    Activity activity = wrapper.newInstance();

    assertEquals(expectedActivityClass, activity.getClass());

    verify(bundleLoader, times(1)).getBundleClass(executableFile1, bundleName, bundleVersion,
        className);
  }

  /**
   * Test if two new activities does the call twice.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testTwoNew() throws Exception {
    String executable1 = "foo.jar";
    File executableFile1 = new File(activityInstallFolder, executable1);

    String bundleName = "foop";
    String bundleVersion = "1.0.0";

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);

    String className = "Activity1";

    when(
        configuration
            .getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE))
        .thenReturn(executable1);
    when(
        configuration
            .getRequiredPropertyString(InteractiveSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className);

    Class expectedActivityClass = Activity1.class;
    when(bundleLoader.getBundleClass(executableFile1, bundleName, bundleVersion, className))
        .thenReturn(expectedActivityClass);

    Activity activity1 = wrapper.newInstance();
    Activity activity2 = wrapper.newInstance();

    assertEquals(expectedActivityClass, activity1.getClass());
    assertEquals(expectedActivityClass, activity2.getClass());
    assertNotSame(activity1, activity2);

    verify(bundleLoader, times(2)).getBundleClass(executableFile1, bundleName, bundleVersion,
        className);
  }

  /**
   * Test if the executable changes between calls.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCofigChangeExecutable() throws Exception {
    String executable1 = "foo.jar";
    File executableFile1 = new File(activityInstallFolder, executable1);

    String executable2 = "bar.jar";
    File executableFile2 = new File(activityInstallFolder, executable2);

    String bundleName = "foop";
    String bundleVersion = "1.0.0";

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);

    String className = "Activity1";

    when(
        configuration
            .getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE))
        .thenReturn(executable1).thenReturn(executable2);
    when(
        configuration
            .getRequiredPropertyString(InteractiveSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className);

    Class expectedActivityClass1 = Activity1.class;
    Class expectedActivityClass2 = Activity2.class;
    when(bundleLoader.getBundleClass(executableFile1, bundleName, bundleVersion, className))
        .thenReturn(expectedActivityClass1);
    when(bundleLoader.getBundleClass(executableFile2, bundleName, bundleVersion, className))
        .thenReturn(expectedActivityClass2);

    Activity activity1 = wrapper.newInstance();
    Activity activity2 = wrapper.newInstance();

    assertEquals(expectedActivityClass1, activity1.getClass());
    assertEquals(expectedActivityClass2, activity2.getClass());
    assertNotSame(activity1, activity2);

    verify(bundleLoader, times(1)).getBundleClass(executableFile1, bundleName, bundleVersion,
        className);
    verify(bundleLoader, times(1)).getBundleClass(executableFile2, bundleName, bundleVersion,
        className);
  }

  /**
   * Test if the classname changes between calls.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCofigChangeClassName() throws Exception {
    String executable1 = "foo.jar";
    File executableFile1 = new File(activityInstallFolder, executable1);

    String bundleName = "foop";
    String bundleVersion = "1.0.0";

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);

    String className1 = "Activity1";
    String className2 = "Activity2";

    when(
        configuration
            .getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE))
        .thenReturn(executable1);
    when(
        configuration
            .getRequiredPropertyString(InteractiveSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className1).thenReturn(className2);

    Class expectedActivityClass1 = Activity1.class;
    Class expectedActivityClass2 = Activity2.class;
    when(bundleLoader.getBundleClass(executableFile1, bundleName, bundleVersion, className1))
        .thenReturn(expectedActivityClass1);
    when(bundleLoader.getBundleClass(executableFile1, bundleName, bundleVersion, className2))
        .thenReturn(expectedActivityClass2);

    Activity activity1 = wrapper.newInstance();
    Activity activity2 = wrapper.newInstance();

    assertEquals(expectedActivityClass1, activity1.getClass());
    assertEquals(expectedActivityClass2, activity2.getClass());
    assertNotSame(activity1, activity2);

    verify(bundleLoader, times(1)).getBundleClass(executableFile1, bundleName, bundleVersion,
        className1);
    verify(bundleLoader, times(1)).getBundleClass(executableFile1, bundleName, bundleVersion,
        className2);
  }

  /**
   * Test if the the bundle name and version changes between calls.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCofigChangeBundleNameVersion() throws Exception {
    String executable1 = "foo.jar";
    File executableFile1 = new File(activityInstallFolder, executable1);

    String bundleName1 = "foop";
    String bundleVersion1 = "1.0.0";
    String bundleName2 = "doop";
    String bundleVersion2 = "2.0.0";

    String className = "Activity1";

    when(
        configuration
            .getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE))
        .thenReturn(executable1);
    when(
        configuration
            .getRequiredPropertyString(InteractiveSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className);

    Class expectedActivityClass1 = Activity1.class;
    Class expectedActivityClass2 = Activity2.class;
    when(bundleLoader.getBundleClass(executableFile1, bundleName1, bundleVersion1, className))
        .thenReturn(expectedActivityClass1);
    when(bundleLoader.getBundleClass(executableFile1, bundleName2, bundleVersion2, className))
        .thenReturn(expectedActivityClass2);

    liveActivity.setIdentifyingName(bundleName1);
    liveActivity.setVersion(bundleVersion1);
    Activity activity1 = wrapper.newInstance();

    liveActivity.setIdentifyingName(bundleName2);
    liveActivity.setVersion(bundleVersion2);
    Activity activity2 = wrapper.newInstance();

    assertEquals(expectedActivityClass1, activity1.getClass());
    assertEquals(expectedActivityClass2, activity2.getClass());
    assertNotSame(activity1, activity2);

    verify(bundleLoader, times(1)).getBundleClass(executableFile1, bundleName1, bundleVersion1,
        className);
    verify(bundleLoader, times(1)).getBundleClass(executableFile1, bundleName2, bundleVersion2,
        className);
  }

  public static class Activity1 extends BaseActivity {
  }

  public static class Activity2 extends BaseActivity {
  }
}
