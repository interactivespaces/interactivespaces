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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import interactivespaces.activity.ActivityStateTransitioner.SequenceTransitionResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link ActivityStateTransitionerCollection}.
 *
 * @author Keith M. Hughes
 */
public class ActivityStateTransitionerCollectionTest {

  private ActivityStateTransitionerCollection collection;

  private ActivityStateTransitioner transitioner;

  @Before
  public void setup() {
    collection = new ActivityStateTransitionerCollection();

    transitioner = Mockito.mock(ActivityStateTransitioner.class);
  }

  /**
   * Make sure the collection contains transitioners properly.
   */
  @Test
  public void testContaining() {
    String uuid = "foo";
    assertFalse(collection.containsTransitioner(uuid));

    collection.addTransitioner(uuid, transitioner);
    assertTrue(collection.containsTransitioner(uuid));
  }

  /**
   * Make sure the collection leaves the transitioner if it is still working.
   */
  @Test
  public void testLeavesIfWorking() {
    String uuid = "foo";
    collection.addTransitioner(uuid, transitioner);
    assertTrue(collection.containsTransitioner(uuid));

    Mockito.when(transitioner.transition(ActivityState.READY)).thenReturn(
        SequenceTransitionResult.WORKING);

    collection.transition(uuid, ActivityState.READY);
    assertTrue(collection.containsTransitioner(uuid));
  }

  /**
   * Make sure the collection leaves the transitioner if it is done.
   */
  @Test
  public void testLeavesIfDone() {
    String uuid = "foo";
    collection.addTransitioner(uuid, transitioner);
    assertTrue(collection.containsTransitioner(uuid));

    Mockito.when(transitioner.transition(ActivityState.READY)).thenReturn(
        SequenceTransitionResult.DONE);

    collection.transition(uuid, ActivityState.READY);
    assertFalse(collection.containsTransitioner(uuid));
  }

  /**
   * Make sure the collection leaves the transitioner if it errors.
   */
  @Test
  public void testLeavesIfError() {
    String uuid = "foo";
    collection.addTransitioner(uuid, transitioner);
    assertTrue(collection.containsTransitioner(uuid));

    Mockito.when(transitioner.transition(ActivityState.READY)).thenReturn(
        SequenceTransitionResult.ERROR);

    collection.transition(uuid, ActivityState.READY);
    assertFalse(collection.containsTransitioner(uuid));
  }

  /**
   * Make sure OK if not there.
   */
  @Test
  public void testNotThere() {
    String uuid = "not_there";

    collection.transition(uuid, ActivityState.READY);
  }
}
