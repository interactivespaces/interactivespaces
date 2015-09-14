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

import interactivespaces.liveactivity.runtime.LiveActivityStorageManager;
import interactivespaces.liveactivity.runtime.SimpleActivityInstallationManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationListener;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager.RemoveActivityResult;
import interactivespaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link SimpleActivityInstallationManager}.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivityInstallationManagerTest {

  private LocalLiveActivityRepository repository;

  private InteractiveSpacesEnvironment spaceEnvironment;

  private LiveActivityStorageManager activityStorageManager;

  private SimpleActivityInstallationManager installationManager;

  private ActivityInstallationListener listener;

  @Before
  public void setup() {
    repository = Mockito.mock(LocalLiveActivityRepository.class);
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
    activityStorageManager = Mockito.mock(LiveActivityStorageManager.class);
    listener = Mockito.mock(ActivityInstallationListener.class);

    installationManager =
        new SimpleActivityInstallationManager(repository, activityStorageManager, spaceEnvironment);

    installationManager.addActivityInstallationListener(listener);
  }

  /**
   * Test a deletion for an activity which exists.
   */
  @Test
  public void testDeleteExist() {
    String uuid = "foo";

    InstalledLiveActivity activity = Mockito.mock(InstalledLiveActivity.class);

    Mockito.when(repository.getInstalledLiveActivityByUuid(uuid)).thenReturn(activity);

    RemoveActivityResult result = installationManager.removeActivity(uuid);

    RemoveActivityResult expectedResult = RemoveActivityResult.SUCCESS;
    assertEquals(expectedResult, result);

    Mockito.verify(repository, Mockito.times(1)).getInstalledLiveActivityByUuid(uuid);
    Mockito.verify(repository, Mockito.times(1)).deleteInstalledLiveActivity(activity);
    Mockito.verify(activityStorageManager, Mockito.times(1)).removeActivityLocation(uuid);
    Mockito.verify(listener, Mockito.times(1)).onActivityRemove(uuid, expectedResult);
  }

  /**
   * Test a deletion for an activity which does not exist.
   */
  @Test
  public void testDeleteNotExist() {
    String uuid = "foo";

    Mockito.when(repository.getInstalledLiveActivityByUuid(uuid)).thenReturn(null);

    RemoveActivityResult result = installationManager.removeActivity(uuid);

    RemoveActivityResult expectedResult = RemoveActivityResult.DOESNT_EXIST;
    assertEquals(expectedResult, result);

    Mockito.verify(repository, Mockito.times(1)).getInstalledLiveActivityByUuid(uuid);
    Mockito.verify(listener, Mockito.times(1)).onActivityRemove(uuid, expectedResult);
  }
}
