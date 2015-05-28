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
import interactivespaces.master.api.master.internal.StandardMasterApiSpaceControllerManager;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Do some tests of the {@link StandardMasterApiSpaceControllerManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiControllerManagerTest {

  private StandardMasterApiSpaceControllerManager manager;
  private SpaceControllerRepository controllerRepository;
  private ActivityRepository activityRepository;
  private ActiveSpaceControllerManager activeSpaceControllerManager;
  private InteractiveSpacesEnvironment spaceEnvironment;
  private Log log;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    log = Mockito.mock(Log.class);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    controllerRepository = Mockito.mock(SpaceControllerRepository.class);
    activityRepository = Mockito.mock(ActivityRepository.class);
    activeSpaceControllerManager = Mockito.mock(ActiveSpaceControllerManager.class);

    manager = new StandardMasterApiSpaceControllerManager();

    manager.setSpaceControllerRepository(controllerRepository);
    manager.setActivityRepository(activityRepository);
    manager.setActiveSpaceControllerManager(activeSpaceControllerManager);
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

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanLiveActivityTempData(controller);
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

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanLiveActivityPermanentData(
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

    manager.cleanSpaceControllerTempData(id);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerTempData(controller);
  }

  /**
   * Test cleaning all the temp data for a controller.
   */
  @Test
  public void testControllerTempCleanAll() {
    String id = "1234";
    SpaceController controller = Mockito.mock(SpaceController.class);
    Mockito.when(controllerRepository.getSpaceControllerById(id)).thenReturn(controller);

    manager.cleanSpaceControllerActivitiesTempData(id);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1))
        .cleanSpaceControllerActivitiesTempData(controller);
  }

  /**
   * Test cleaning the temp data for a controller.
   */
  @Test
  public void testControllerPermanentClean() {
    String id = "1234";
    SpaceController controller = Mockito.mock(SpaceController.class);
    Mockito.when(controllerRepository.getSpaceControllerById(id)).thenReturn(controller);

    manager.cleanSpaceControllerPermanentData(id);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerPermanentData(
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

    manager.cleanSpaceControllerActivitiesPermanentData(id);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerActivitiesPermanentData(
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

    manager.cleanSpaceControllerTempDataAllSpaceControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerTempData(controller);
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

    manager.cleanSpaceControllerActivitiesTempDataAllSpaceControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerActivitiesTempData(
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

    manager.cleanSpaceControllerPermanentDataAllSpaceControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerPermanentData(
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

    manager.cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers();

    for (SpaceController controller : controllers) {
      Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).cleanSpaceControllerActivitiesPermanentData(
          controller);
    }
  }
}
