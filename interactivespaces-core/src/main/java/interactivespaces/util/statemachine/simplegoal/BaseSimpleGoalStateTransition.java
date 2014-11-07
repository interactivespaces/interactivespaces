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
 * Base implementation of a simple state transition.
 *
 * @param <S>
 *          the state object
 * @param <C>
 *          the control object
 *
 * @author Keith M. Hughes
 */
public abstract class BaseSimpleGoalStateTransition<S, C> implements SimpleGoalStateTransition<S, C> {

  /**
   * Text description of the transition.
   */
  private String description;

  /**
   * Construct a new base transition.
   *
   * @param description
   *          the description of the transition
   */
  public BaseSimpleGoalStateTransition(String description) {
    this.description = description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public TransitionResult attemptTransition(S currentState, C control) {
    TransitionResult result = canTransition(currentState);
    if (result.equals(TransitionResult.OK)) {
      onTransition(currentState, control);
    }

    return result;
  }
}
