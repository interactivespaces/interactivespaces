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

import com.google.common.collect.Maps;

import interactivespaces.activity.ActivityStateTransitioner.SequenceTransitionResult;

import java.util.Map;

/**
 * A collection of {@link ActivityStateTransitioner} instances.
 *
 * <p>
 * This class will remove completed transitioners when they reach the
 * {@link ActivityStateTransitioner.SequenceTransitionResult.DONE} or
 * {@link ActivityStateTransitioner.SequenceTransitionResult.ERROR} states.
 *
 * @author Keith M. Hughes
 */
public class ActivityStateTransitionerCollection {

  /**
   * a map of activity controllers keyed by the UUID for the activity.
   */
  private Map<String, ActivityStateTransitioner> transitioners = Maps.newHashMap();

  /**
   * Add a new transitioner to the collection.
   *
   * @param uuid
   *          UUID of the activity being transitioned
   * @param transitioner
   *          the transitioner
   */
  public synchronized void addTransitioner(String uuid, ActivityStateTransitioner transitioner) {
    transitioners.put(uuid, transitioner);
  }

  /**
   * Transition an activity.
   *
   * <p>
   * Does nothing if there is no activity with the given UUID
   *
   * @param uuid
   *          UUID of the activity being transitioned
   * @param newState
   *          the new state for the activity
   */
  public synchronized void transition(String uuid, ActivityState newState) {
    ActivityStateTransitioner transitioner = transitioners.get(uuid);
    if (transitioner != null) {
      SequenceTransitionResult result = transitioner.transition(newState);
      if (result.equals(SequenceTransitionResult.DONE)
          || result.equals(SequenceTransitionResult.ERROR)) {
        transitioners.remove(uuid);
      }
    }
  }

  /**
   * Does the collection contain a transitioner for the given UUID
   *
   * <p>
   * Does nothing if there is no activity with the given UUID
   *
   * @param uuid
   *          UUID of the activity being transitioned
   */
  public synchronized boolean containsTransitioner(String uuid) {
    return transitioners.containsKey(uuid);
  }

  /**
   * Clear the collection out.
   */
  public synchronized void clear() {
    transitioners.clear();
  }
}
