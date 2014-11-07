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

import interactivespaces.util.statemachine.simplegoal.SimpleGoalStateTransition.TransitionResult;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.Queue;

/**
 * Run an activity through a set of state transitions.
 *
 * <p>
 * This class is not threadsafe!
 *
 * @param <S>
 *          type for the state
 * @param <C>
 *          type for the state control
 *
 * @author Keith M. Hughes
 */
public class SimpleGoalStateTransitioner<S, C> {

  /**
   * Control.
   */
  private C control;

  /**
   * The transitions which need to take place.
   */
  private Queue<SimpleGoalStateTransition<S, C>> transitions = Lists.newLinkedList();

  /**
   * Whether or not there was an error.
   */
  private boolean errored;

  /**
   * Log for errors.
   */
  private Log log;

  /**
   * Construct a new transitioner.
   *
   * @param control
   *          the control for the item being transitioned
   * @param log
   *          the logger to use
   */
  public SimpleGoalStateTransitioner(C control, Log log) {
    this.control = control;
    this.log = log;
  }

  /**
   * Add new transitions to the transitioner.
   *
   * @param newTransitions
   *          the new transitions to add
   *
   * @return the transitioner
   */
  public SimpleGoalStateTransitioner<S, C> addTransitions(SimpleGoalStateTransition<S, C>... newTransitions) {
    if (newTransitions != null) {
      for (SimpleGoalStateTransition<S, C> transition : newTransitions) {
        transitions.add(transition);
      }
    }

    return this;
  }

  /**
   * Transition to a new state.
   *
   * @param currentState
   *          the state that was transitioned to
   *
   * @return the result of the transition
   */
  public SimpleGoalStateTransitionResult transition(S currentState) {
    if (errored) {
      return SimpleGoalStateTransitionResult.CANT;
    }

    SimpleGoalStateTransition<S, C> nextTransition = transitions.peek();
    if (nextTransition == null) {
      return SimpleGoalStateTransitionResult.DONE;
    }

    TransitionResult canTransition = nextTransition.canTransition(currentState);
    if (canTransition == TransitionResult.WAIT) {
      // Don't consume the transition yet. WAIT means we are on our way.
      return SimpleGoalStateTransitionResult.WORKING;
    } else if (canTransition == TransitionResult.OK) {
      transitions.poll();
      try {
        nextTransition.onTransition(currentState, control);
      } catch (Throwable e) {
        errored = true;

        log.error("Error during goal transition", e);

        return SimpleGoalStateTransitionResult.ERROR;
      }
    } else {
      log.warn("Goal state machine cannot transition");
      return SimpleGoalStateTransitionResult.CANT;
    }

    if (transitions.isEmpty()) {
      return SimpleGoalStateTransitionResult.DONE;
    } else {
      return SimpleGoalStateTransitionResult.WORKING;
    }
  }

  /**
   * Result of a transition attempt.
   *
   * @author Keith M. Hughes
   */
  public enum SimpleGoalStateTransitionResult {

    /**
     * Still working on the transitions.
     */
    WORKING,

    /**
     * The transitions are done.
     */
    DONE,

    /**
     * Can't transition. may be because it errored before, or the transition is illegal.
     */
    CANT,

    /**
     * An error occurred during transition.
     */
    ERROR
  }
}
