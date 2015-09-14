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

package interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.resource.Version;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.File;

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
        new InteractiveSpacesNativeActivityWrapper(liveActivity, activityFilesystem, configuration, bundleLoader);
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
    Version bundleVersion = new Version(1, 0, 0);

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);

    String className = "Activity1";

    when(configuration.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE)).thenReturn(
        executable1);
    when(
        configuration
            .getRequiredPropertyString(InteractiveSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className);

    Class expectedActivityClass = Activity1.class;
    Bundle activityBundle = mock(Bundle.class);
    when(bundleLoader.loadLiveActivityBundle(liveActivity, executableFile1)).thenReturn(activityBundle);
    when(activityBundle.loadClass(className)).thenReturn(expectedActivityClass);

    Activity activity = wrapper.newInstance();

    assertEquals(expectedActivityClass, activity.getClass());

    wrapper.done();

    verify(bundleLoader).dismissLiveActivityBundle(liveActivity);
  }

  public static class Activity1 extends BaseActivity {
  }
}
