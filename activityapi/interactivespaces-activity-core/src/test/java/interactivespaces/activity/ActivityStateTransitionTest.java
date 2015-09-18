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

package interactivespaces.activity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * tests for {@link ActivityStateTransition}.
 *
 * @author Keith M. Hughes
 */
public class ActivityStateTransitionTest {
  private ActivityControl activity;

  @Before
  public void setup() {
    activity = Mockito.mock(ActivityControl.class);
  }

  /**
   * Make sure we get a startup.
   */
  @Test
  public void testStartup() {
    ActivityStateTransition.STARTUP.onTransition(ActivityState.READY, activity);

    Mockito.verify(activity).startup();
  }

  /**
   * Make sure we get a startup with shutdown if from a shutdown fail.
   */
  @Test
  public void testStartupFromFailedShutdown() {
    InOrder activityInOrder = Mockito.inOrder(activity);

    ActivityStateTransition.STARTUP.onTransition(ActivityState.SHUTDOWN_FAILURE, activity);

    activityInOrder.verify(activity).shutdown();
    activityInOrder.verify(activity).startup();
  }

  /**
   * Make sure we get a shutdown.
   */
  @Test
  public void testShutdown() {
    ActivityStateTransition.SHUTDOWN.onTransition(ActivityState.RUNNING, activity);

    Mockito.verify(activity).shutdown();
  }

  /**
   * Make sure we get a startup.
   */
  @Test
  public void testActivate() {
    ActivityStateTransition.ACTIVATE.onTransition(ActivityState.RUNNING, activity);

    Mockito.verify(activity).activate();
  }

  /**
   * Make sure we get a deactivate.
   */
  @Test
  public void testDeactivate() {
    ActivityStateTransition.DEACTIVATE.onTransition(ActivityState.ACTIVE, activity);

    Mockito.verify(activity).deactivate();
  }
}
