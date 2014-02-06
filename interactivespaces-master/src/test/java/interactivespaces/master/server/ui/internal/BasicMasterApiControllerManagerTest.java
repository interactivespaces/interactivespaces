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

package interactivespaces.master.server.ui.internal;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.api.internal.BasicMasterApiControllerManager;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Do some tests of the {@link BasicMasterApiControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiControllerManagerTest {

  private BasicMasterApiControllerManager manager;
  private ControllerRepository controllerRepository;
  private ActivityRepository activityRepository;
  private ActiveControllerManager activeControllerManager;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private Log log;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    log = Mockito.mock(Log.class);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    controllerRepository = Mockito.mock(ControllerRepository.class);
    activityRepository = Mockito.mock(ActivityRepository.class);
    activeControllerManager = Mockito.mock(ActiveControllerManager.class);

    manager = new BasicMasterApiControllerManager();

    manager.setControllerRepository(controllerRepository);
    manager.setActivityRepository(activityRepository);
    manager.setActiveControllerManager(activeControllerManager);
  }

  /**
   * Test cleaning the temp data for a live activity.
   */
  @Test
  public void testLiveActivityTempClean() {
    String id = "1234";
    LiveActivity controller = Mockito.mock(LiveActivity.class);
    Mockito.when(activityRepository.getLiveActivityById(id)).thenReturn(controller);

    manager.cleanLiveActivityTempData(id);

    Mockito.verify(activeControllerManager, Mockito.times(1)).cleanLiveActivityTempData(controller);
  }

  /**
   * Test cleaning the permanent data for a live activity.
   */
  @Test
  public void testLiveActivityPermanentClean() {
    String id = "1234";
    LiveActivity controller = Mockito.mock(LiveActivity.class);
    Mockito.when(activityRepository.getLiveActivityById(id)).thenReturn(controller);

    manager.cleanLiveActivityPermanentData(id);

    Mockito.verify(activeControllerManager, Mockito.times(1)).cleanLiveActivityPermanentData(
        controller);
  }

  /**
   * Test cleaning the temp data for a controller.
   */
  @Test
  public void testControllerTempClean() {
    String id = "1234";
    SpaceController controller = Mockito.mock(SpaceController.class);
    Mockito.when(controllerRepository.getSpaceControllerById(id)).thenReturn(controller);

    manager.cleanControllerTempData(id);

    Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerTempData(controller);
  }

  /**
   * Test cleaning all the temp data for a controller.
   */
  @Test
  public void testControllerTempCleanAll() {
    String id = "1234";
    SpaceController controller = Mockito.mock(SpaceController.class);
    Mockito.when(controllerRepository.getSpaceControllerById(id)).thenReturn(controller);

    manager.cleanControllerActivitiesTempData(id);

    Mockito.verify(activeControllerManager, Mockito.times(1))
        .cleanControllerActivitiesTempData(controller);
  }

  /**
   * Test cleaning the temp data for a controller.
   */
  @Test
  public void testControllerPermanentClean() {
    String id = "1234";
    SpaceController controller = Mockito.mock(SpaceController.class);
    Mockito.when(controllerRepository.getSpaceControllerById(id)).thenReturn(controller);

    manager.cleanControllerPermanentData(id);

    Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerPermanentData(
        controller);
  }

  /**
   * Test cleaning all the temp data for a controller.
   */
  @Test
  public void testControllerPermanentCleanAll() {
    String id = "1234";
    SpaceController controller = Mockito.mock(SpaceController.class);
    Mockito.when(controllerRepository.getSpaceControllerById(id)).thenReturn(controller);

    manager.cleanControllerActivitiesPermanentData(id);

    Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerActivitiesPermanentData(
        controller);
  }

  /**
   * Test cleaning the temp data for a controller.
   */
  @Test
  public void testAllControllerTempClean() {
    List<SpaceController> controllers =
        Lists
            .newArrayList(Mockito.mock(SpaceController.class), Mockito.mock(SpaceController.class));
    Mockito.when(controllerRepository.getAllSpaceControllers()).thenReturn(controllers);

    manager.cleanControllerTempDataAllControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerTempData(controller);
    }
  }

  /**
   * Test cleaning all the temp data for a controller.
   */
  @Test
  public void testAllControllerTempCleanAll() {
    List<SpaceController> controllers =
        Lists
            .newArrayList(Mockito.mock(SpaceController.class), Mockito.mock(SpaceController.class));
    Mockito.when(controllerRepository.getAllSpaceControllers()).thenReturn(controllers);

    manager.cleanControllerActivitiesTempDataAllControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerActivitiesTempData(
          controller);
    }
  }

  /**
   * Test cleaning the temp data for a controller.
   */
  @Test
  public void testAllControllerPermanentClean() {
    List<SpaceController> controllers =
        Lists
            .newArrayList(Mockito.mock(SpaceController.class), Mockito.mock(SpaceController.class));
    Mockito.when(controllerRepository.getAllSpaceControllers()).thenReturn(controllers);

    manager.cleanControllerPermanentDataAllControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerPermanentData(
          controller);
    }
  }

  /**
   * Test cleaning all the temp data for a controller.
   */
  @Test
  public void testAllControllerPermanentCleanAll() {
    List<SpaceController> controllers =
        Lists
            .newArrayList(Mockito.mock(SpaceController.class), Mockito.mock(SpaceController.class));
    Mockito.when(controllerRepository.getAllSpaceControllers()).thenReturn(controllers);

    manager.cleanControllerActivitiesPermanentDataAllControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeControllerManager, Mockito.times(1)).cleanControllerActivitiesPermanentData(
          controller);
    }
  }
}
