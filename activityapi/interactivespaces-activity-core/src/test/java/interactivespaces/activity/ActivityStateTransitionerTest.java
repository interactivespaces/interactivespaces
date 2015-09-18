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

import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner;
import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner.SimpleGoalStateTransitionResult;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Tests for {@link SimpleGoalStateTransitioner} behavior.
 *
 * @author Keith M. Hughes
 */
public class ActivityStateTransitionerTest {

  private ActivityControl activity;

  private SimpleGoalStateTransitioner<ActivityState, ActivityControl> transitioner;

  private Log log;

  @Before
  public void setup() {
    activity = Mockito.mock(ActivityControl.class);
    log = Mockito.mock(Log.class);
  }

  /**
   * A single step transition which succeeds
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSingleTransitionSuccess() {
    transitioner =
        new SimpleGoalStateTransitioner<ActivityState, ActivityControl>(activity, log)
            .addTransitions(ActivityStateTransition.STARTUP);

    SimpleGoalStateTransitionResult result = transitioner.transition(ActivityState.READY);

    Mockito.verify(activity).startup();

    assertEquals(SimpleGoalStateTransitionResult.DONE, result);
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testSingleTransitionFailure() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).startup();

    transitioner =
        new SimpleGoalStateTransitioner<ActivityState, ActivityControl>(activity, log)
            .addTransitions(ActivityStateTransition.STARTUP);
    SimpleGoalStateTransitionResult result = transitioner.transition(ActivityState.READY);

    Mockito.verify(activity).startup();

    assertEquals(SimpleGoalStateTransitionResult.ERROR, result);
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testMultipleTransitionSuccess() {
    InOrder activityInOrder = Mockito.inOrder(activity);

    transitioner =
        new SimpleGoalStateTransitioner<ActivityState, ActivityControl>(activity, log).addTransitions(
            ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE);

    SimpleGoalStateTransitionResult result1 = transitioner.transition(ActivityState.READY);
    assertEquals(SimpleGoalStateTransitionResult.WORKING, result1);

    SimpleGoalStateTransitionResult result2 = transitioner.transition(ActivityState.RUNNING);
    assertEquals(SimpleGoalStateTransitionResult.DONE, result2);

    activityInOrder.verify(activity).startup();
    activityInOrder.verify(activity).activate();
  }

  /**
   * A single step transition which succeeds
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testMultipleTransitionError() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).startup();

    transitioner =
        new SimpleGoalStateTransitioner<ActivityState, ActivityControl>(activity, log).addTransitions(
            ActivityStateTransition.STARTUP, ActivityStateTransition.ACTIVATE);

    SimpleGoalStateTransitionResult result1 = transitioner.transition(ActivityState.READY);
    assertEquals(SimpleGoalStateTransitionResult.ERROR, result1);

    SimpleGoalStateTransitionResult result2 = transitioner.transition(ActivityState.RUNNING);
    assertEquals(SimpleGoalStateTransitionResult.CANT, result2);

    Mockito.verify(activity).startup();
    Mockito.verify(activity, Mockito.never()).activate();
  }
}
