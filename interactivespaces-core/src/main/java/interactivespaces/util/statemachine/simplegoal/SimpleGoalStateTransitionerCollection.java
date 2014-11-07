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

import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner.SimpleGoalStateTransitionResult;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A collection of {@link SimpleGoalStateTransitioner} instances.
 *
 * <p>
 * This class will remove completed transitioners when they reach the
 * {@link SimpleGoalStateTransitioner.SimpleGoalStateTransitionResult.DONE} or
 * {@link SimpleGoalStateTransitioner.SimpleGoalStateTransitionResult.ERROR} states.
 *
 * @param <S>
 *          the type of the state object
 * @param <C>
 *          the type of the control object
 *
 * @author Keith M. Hughes
 */
public class SimpleGoalStateTransitionerCollection<S, C> {

  /**
   * a map of activity controllers keyed by the key for the transitioner.
   */
  private Map<String, SimpleGoalStateTransitioner<S, C>> transitioners = Maps.newHashMap();

  /**
   * Add a new transitioner to the collection.
   *
   * @param transitionerKey
   *          key of the transitioner
   * @param transitioner
   *          the transitioner
   */
  public synchronized void addTransitioner(String transitionerKey, SimpleGoalStateTransitioner<S, C> transitioner) {
    transitioners.put(transitionerKey, transitioner);
  }

  /**
   * Transition a state.
   *
   * <p>
   * Does nothing if there is no transitioner with the given key.
   *
   * @param transitionerKey
   *          key of the state being transitioned
   * @param newState
   *          the new state for the activity
   */
  public synchronized void transition(String transitionerKey, S newState) {
    SimpleGoalStateTransitioner<S, C> transitioner = transitioners.get(transitionerKey);
    if (transitioner != null) {
      SimpleGoalStateTransitionResult result = transitioner.transition(newState);
      if (result.equals(SimpleGoalStateTransitionResult.DONE) || result.equals(SimpleGoalStateTransitionResult.ERROR)) {
        transitioners.remove(transitionerKey);
      }
    }
  }

  /**
   * Does the collection contain a transitioner for the given key?
   *
   * @param transitionerKey
   *          key of the state being transitioned
   *
   * @return {@code true} if the collection contains a transitioner with the key
   */
  public synchronized boolean containsTransitioner(String transitionerKey) {
    return transitioners.containsKey(transitionerKey);
  }

  /**
   * Clear the collection out.
   */
  public synchronized void clear() {
    transitioners.clear();
  }
}
