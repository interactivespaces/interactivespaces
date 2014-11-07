/*
 * Copyright (C) 2014 Google Inc.
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

/**
 * A simple state machine transition.
 *
 * @param <S>
 *          the type of the state
 * @param <C>
 *          the type of the control
 *
 * @author Keith M. Hughes
 */
public interface SimpleGoalStateTransition<S, C> {

  /**
   * Get the description of the state.
   *
   * @return the description
   */
  String getDescription();

  /**
   * Can a transition happen from the given state?
   *
   * @param currentState
   *          the current state
   *
   * @return the result of the transition
   */
  TransitionResult canTransition(S currentState);

  /**
   * Perform the operation of the transition.
   *
   * @param currentState
   *          the current state
   * @param control
   *          the controller for the items being transitioned
   */
  void onTransition(S currentState, C control);

  /**
   * Attempt to do the transition. If it is legal, it will be done.
   *
   * @param currentState
   *          current state
   * @param control
   *          the controller for the items being transitioned
   *
   * @return what the transition result was when it was checked
   */
  TransitionResult attemptTransition(S currentState, C control);

  /**
   * Results from a transition.
   *
   * @author Keith M. Hughes
   */
  public enum TransitionResult {

    /**
     * The transition is OK to happen.
     */
    OK,

    /**
     * Wait to do the transition.
     */
    WAIT,

    /**
     * The current transition is illegal from the current state.
     */
    ILLEGAL,

    /**
     * This transition is a no-op.
     */
    NOOP
  }
}
