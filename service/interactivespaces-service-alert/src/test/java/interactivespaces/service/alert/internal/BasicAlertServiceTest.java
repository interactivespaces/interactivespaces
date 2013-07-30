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

package interactivespaces.service.alert.internal;

import static org.mockito.Mockito.when;

import interactivespaces.service.alert.AlertNotifier;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.SettableTimeProvider;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link BasicAlertService}
 *
 * @author Keith M. Hughes
 */
public class BasicAlertServiceTest {

  private BasicAlertService alertService;

  private InteractiveSpacesEnvironment spaceEnvironment;

  private SettableTimeProvider timeProvider;

  private AlertNotifier notifier1;

  private AlertNotifier notifier2;

  private Log log;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
    timeProvider = new SettableTimeProvider();
    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    log = Mockito.mock(Log.class);
    when(spaceEnvironment.getLog()).thenReturn(log);

    notifier1 = Mockito.mock(AlertNotifier.class);
    notifier2 = Mockito.mock(AlertNotifier.class);

    alertService = new BasicAlertService();
    alertService.setSpaceEnvironment(spaceEnvironment);
    alertService.registerAlertNotifier(notifier1);
    alertService.registerAlertNotifier(notifier2);
  }

  /**
   * Want to see both notifiers work.
   */
  @Test
  public void testFullNotify() {
    String alertType = "phideaux";
    String id = "foobar";
    String message = "it burns... it burns...";

    alertService.raiseAlert(alertType, id, message);

    Mockito.verify(notifier1, Mockito.times(1)).notify(Mockito.eq(alertType), Mockito.eq(id),
        Mockito.eq(message));
    Mockito.verify(notifier2, Mockito.times(1)).notify(Mockito.eq(alertType), Mockito.eq(id),
        Mockito.eq(message));

    Mockito.verify(log, Mockito.never()).error(Mockito.anyString(), Mockito.any(Throwable.class));
  }

  /**
   * One notifier will crap out. Both should at least try to run.
   */
  @Test
  public void testFirstNotifyFails() {
    String alertType = "phideaux";
    String id = "foobar";
    String message = "it burns... it burns...";

    RuntimeException e = new RuntimeException();
    Mockito.doThrow(e).when(notifier1).notify(alertType, id, message);

    alertService.raiseAlert(alertType, id, message);

    Mockito.verify(notifier1, Mockito.times(1)).notify(Mockito.eq(alertType), Mockito.eq(id),
        Mockito.eq(message));
    Mockito.verify(notifier2, Mockito.times(1)).notify(Mockito.eq(alertType), Mockito.eq(id),
        Mockito.eq(message));

    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));
  }
}