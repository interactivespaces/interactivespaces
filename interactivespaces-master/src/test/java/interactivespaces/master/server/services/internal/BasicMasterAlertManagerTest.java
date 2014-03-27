/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.master.server.services.internal;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.server.services.MasterAlertManager;
import interactivespaces.service.alert.AlertService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.SettableTimeProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Unit tests for {@link BasicMasterAlertManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterAlertManagerTest {

  private BasicMasterAlertManager alertManager;

  private InteractiveSpacesEnvironment spaceEnvironment;

  private SettableTimeProvider timeProvider;

  private AlertService alertService;

  private SpaceControllerRepository controllerRepository;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
    timeProvider = new SettableTimeProvider();
    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    controllerRepository = Mockito.mock(SpaceControllerRepository.class);

    alertService = Mockito.mock(AlertService.class);
    alertManager = new BasicMasterAlertManager();
    alertManager.setSpaceEnvironment(spaceEnvironment);
    alertManager.setAlertService(alertService);
    alertManager.setSpaceControllerRepository(controllerRepository);
  }

  /**
   * Don't trigger after two scans.
   */
  @Test
  public void testAlertManagerScanNoTrigger() {
    String uuid = "foo";

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    int initialTimestamp = 1000;
    timeProvider.setCurrentTime(initialTimestamp);
    alertManager.getSpaceControllerListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    alertManager.getSpaceControllerListener().onSpaceControllerHeartbeat(uuid,
        initialTimestamp + alertManager.getSpaceControllerHeartbeatTime() - 1);
    timeProvider.setCurrentTime(initialTimestamp + alertManager.getSpaceControllerHeartbeatTime());
    alertManager.scan();

    Mockito.verify(alertService, Mockito.never()).raiseAlert(Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString());
  }

  /**
   * Trigger after two scans.
   */
  @Test
  public void testAlertManagerScanTrigger() {
    String uuid = "this.is.my.uuid";
    int initialTimestamp = 1000;

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("hostess");
    controller.setName("NumeroUno");
    Mockito.when(controllerRepository.getSpaceControllerByUuid(uuid)).thenReturn(controller);

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    timeProvider.setCurrentTime(initialTimestamp);
    alertManager.getSpaceControllerListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    timeProvider.setCurrentTime(initialTimestamp + alertManager.getSpaceControllerHeartbeatTime()
        + 1);
    alertManager.scan();

    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    Mockito.verify(alertService, Mockito.times(1)).raiseAlert(
        Mockito.eq(MasterAlertManager.ALERT_TYPE_CONTROLLER_TIMEOUT), Mockito.eq(uuid),
        message.capture());

    String m = message.getValue();
    assertTrue(m.contains(uuid));
    assertTrue(m.contains(controller.getId()));
    assertTrue(m.contains(controller.getHostId()));
    assertTrue(m.contains(controller.getName()));
  }

  /**
   * Don't trigger after two scans because of disconnect.
   */
  @Test
  public void testAlertManagerScanNoTriggerFromDisconnect() {
    String uuid = "this.is.my.uuid";
    int initialTimestamp = 1000;
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    timeProvider.setCurrentTime(initialTimestamp);
    alertManager.getSpaceControllerListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    alertManager.getSpaceControllerListener().onSpaceControllerDisconnectAttempted(active);
    timeProvider.setCurrentTime(initialTimestamp + alertManager.getSpaceControllerHeartbeatTime()
        + 1);
    alertManager.scan();

    Mockito.verify(alertService, Mockito.never()).raiseAlert(
        Mockito.eq(MasterAlertManager.ALERT_TYPE_CONTROLLER_TIMEOUT), Mockito.eq(uuid),
        Mockito.anyString());
  }
}
