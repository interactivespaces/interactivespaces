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

import static org.junit.Assert.assertEquals;

import interactivespaces.activity.ActivityStateTransitioner.SequenceTransitionResult;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Tests for {@link ActivityStateTransitioner} behavior.
 *
 * @author Keith M. Hughes
 */
public class ActivityStateTransitionerTest {

  private ActivityControl activity;

  private ActivityStateTransitioner transitioner;

  private Log log;

  @Before
  public void setup() {
    activity = Mockito.mock(ActivityControl.class);
    log = Mockito.mock(Log.class);
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  public void testSingleTransitionSuccess() {
    transitioner =
        new ActivityStateTransitioner(activity,
            ActivityStateTransitioner.transitions(ActivityStateTransition.STARTUP), log);
    SequenceTransitionResult result = transitioner.transition(ActivityState.READY);

    Mockito.verify(activity).startup();

    assertEquals(SequenceTransitionResult.DONE, result);
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  public void testSingleTransitionFailure() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).startup();

    transitioner =
        new ActivityStateTransitioner(activity,
            ActivityStateTransitioner.transitions(ActivityStateTransition.STARTUP), log);
    SequenceTransitionResult result = transitioner.transition(ActivityState.READY);

    Mockito.verify(activity).startup();

    assertEquals(SequenceTransitionResult.ERROR, result);
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  public void testMultipleTransitionSuccess() {
    InOrder activityInOrder = Mockito.inOrder(activity);

    transitioner =
        new ActivityStateTransitioner(activity, ActivityStateTransitioner.transitions(
            ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE), log);

    SequenceTransitionResult result1 = transitioner.transition(ActivityState.READY);
    assertEquals(SequenceTransitionResult.WORKING, result1);

    SequenceTransitionResult result2 = transitioner.transition(ActivityState.RUNNING);
    assertEquals(SequenceTransitionResult.DONE, result2);

    activityInOrder.verify(activity).startup();
    activityInOrder.verify(activity).activate();
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  public void testMultipleTransitionError() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).startup();

    transitioner =
        new ActivityStateTransitioner(activity, ActivityStateTransitioner.transitions(
            ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE), log);

    SequenceTransitionResult result1 = transitioner.transition(ActivityState.READY);
    assertEquals(SequenceTransitionResult.ERROR, result1);

    SequenceTransitionResult result2 = transitioner.transition(ActivityState.RUNNING);
    assertEquals(SequenceTransitionResult.CANT, result2);

    Mockito.verify(activity).startup();
    Mockito.verify(activity, Mockito.never()).activate();
  }
}
