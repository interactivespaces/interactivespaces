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

package interactivespaces.util.statemachine.simplegoal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner.SimpleGoalStateTransitionResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link SimpleGoalStateTransitionerCollection}.
 *
 * @author Keith M. Hughes
 */
public class GoalStateTransitionerCollectionTest {

  private Object testState;

  private SimpleGoalStateTransitionerCollection<Object, Object> collection;

  private SimpleGoalStateTransitioner<Object, Object> transitioner;

  @Before
  @SuppressWarnings("unchecked")
  public void setup() {
    collection = new SimpleGoalStateTransitionerCollection<Object, Object>();

    transitioner = Mockito.mock(SimpleGoalStateTransitioner.class);
  }

  /**
   * Make sure the collection contains transitioners properly.
   */
  @Test
  public void testContaining() {
    String transitionerKey = "foo";
    assertFalse(collection.containsTransitioner(transitionerKey));

    collection.addTransitioner(transitionerKey, transitioner);
    assertTrue(collection.containsTransitioner(transitionerKey));
  }

  /**
   * Make sure the collection leaves the transitioner if it is still working.
   */
  @Test
  public void testLeavesIfWorking() {
    String transitionerKey = "foo";
    collection.addTransitioner(transitionerKey, transitioner);
    assertTrue(collection.containsTransitioner(transitionerKey));

    Mockito.when(transitioner.transition(testState)).thenReturn(SimpleGoalStateTransitionResult.WORKING);

    collection.transition(transitionerKey, testState);
    assertTrue(collection.containsTransitioner(transitionerKey));
  }

  /**
   * Make sure the collection leaves the transitioner if it is done.
   */
  @Test
  public void testLeavesIfDone() {
    String transitionerKey = "foo";
    collection.addTransitioner(transitionerKey, transitioner);
    assertTrue(collection.containsTransitioner(transitionerKey));

    Mockito.when(transitioner.transition(testState)).thenReturn(SimpleGoalStateTransitionResult.DONE);

    collection.transition(transitionerKey, testState);
    assertFalse(collection.containsTransitioner(transitionerKey));
  }

  /**
   * Make sure the collection leaves the transitioner if it errors.
   */
  @Test
  public void testLeavesIfError() {
    String transitionerKey = "foo";
    collection.addTransitioner(transitionerKey, transitioner);
    assertTrue(collection.containsTransitioner(transitionerKey));

    Mockito.when(transitioner.transition(testState)).thenReturn(SimpleGoalStateTransitionResult.ERROR);

    collection.transition(transitionerKey, testState);
    assertFalse(collection.containsTransitioner(transitionerKey));
  }

  /**
   * Make sure OK if not there.
   */
  @Test
  public void testNotThere() {
    String transitionerKey = "not_there";

    collection.transition(transitionerKey, testState);
  }
}
