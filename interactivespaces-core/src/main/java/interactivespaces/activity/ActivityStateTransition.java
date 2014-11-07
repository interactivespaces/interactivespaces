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

import interactivespaces.util.statemachine.simplegoal.BaseSimpleGoalStateTransition;

/**
 * A transition from one activity state to another.
 *
 * @author Keith M. Hughes
 */
public abstract class ActivityStateTransition extends BaseSimpleGoalStateTransition<ActivityState, ActivityControl> {

  /**
   * Transition for starting up an activity.
   */
  public static final ActivityStateTransition STARTUP = new StartupActivityStateTransition(
      "space.activity.state.transition.startup");

  /**
   * Transition for activating an activity.
   */
  public static final ActivityStateTransition ACTIVATE = new ActivateActivityStateTransition(
      "space.activity.state.transition.activate");

  /**
   * Transition for deactivating an activity.
   */
  public static final ActivityStateTransition DEACTIVATE = new DeactivateActivityStateTransition(
      "space.activity.state.transition.deactivate");

  /**
   * Transition for shutting down an activity.
   */
  public static final ActivityStateTransition SHUTDOWN = new ShutdownActivityStateTransition(
      "space.activity.state.transition.shutdown");

  /**
   * Construct a new activity state transition.
   *
   * @param description
   *          the description of the transition
   */
  ActivityStateTransition(String description) {
    super(description);
  }

  @Override
  public String toString() {
    return "ActivityStateTransition [description=" + getDescription() + "]";
  }

  /**
   * Transition for starting up an activity.
   */
  private static final class StartupActivityStateTransition extends ActivityStateTransition {

    /**
     * Construct a startup transition.
     *
     * @param description
     *          the description of the transition
     */
    private StartupActivityStateTransition(String description) {
      super(description);
    }

    @Override
    public TransitionResult canTransition(ActivityState currentState) {
      if (currentState.isRunning()) {
        return TransitionResult.NOOP;
      } else {
        switch (currentState) {
          case STARTUP_FAILURE:
          case SHUTDOWN_FAILURE:
          case CRASHED:
          case READY:
            return TransitionResult.OK;

          default:
            return TransitionResult.ILLEGAL;
        }
      }
    }

    @Override
    public void onTransition(ActivityState currentState, ActivityControl activity) {
      // Make one final attempt to shut it down.
      if (currentState == ActivityState.SHUTDOWN_FAILURE) {
        activity.shutdown();
      }

      activity.startup();
    }
  }

  /**
   * Transition for activating an activity.
   */
  private static final class ActivateActivityStateTransition extends ActivityStateTransition {

    /**
     * Construct an activate transition.
     *
     * @param description
     *          the description of the transition
     */
    private ActivateActivityStateTransition(String description) {
      super(description);
    }

    @Override
    public TransitionResult canTransition(ActivityState state) {
      if (state == ActivityState.RUNNING || state == ActivityState.ACTIVATE_FAILURE) {
        return TransitionResult.OK;
      } else if (state == ActivityState.ACTIVE) {
        return TransitionResult.NOOP;
      } else {
        return TransitionResult.ILLEGAL;
      }
    }

    @Override
    public void onTransition(ActivityState currentState, ActivityControl activity) {
      activity.activate();
    }
  }

  /**
   * Transition for deactivating an activity.
   */
  private static final class DeactivateActivityStateTransition extends ActivityStateTransition {

    /**
     * Construct a deactivate transition.
     *
     * @param description
     *          the description of the transition
     */
    private DeactivateActivityStateTransition(String description) {
      super(description);
    }

    @Override
    public TransitionResult canTransition(ActivityState state) {
      if (state.equals(ActivityState.ACTIVE) || state.equals(ActivityState.ACTIVATE_FAILURE)
          || state.equals(ActivityState.DEACTIVATE_FAILURE)) {
        return TransitionResult.OK;
      } else if (state.equals(ActivityState.RUNNING)) {
        return TransitionResult.NOOP;
      } else {
        return TransitionResult.ILLEGAL;
      }
    }

    @Override
    public void onTransition(ActivityState currentState, ActivityControl activity) {
      activity.deactivate();
    }
  }

  /**
   * Transition for shutting down an activity.
   */
  private static final class ShutdownActivityStateTransition extends ActivityStateTransition {

    /**
     * Construct a shutdown transition.
     *
     * @param description
     *          the description of the transition
     */
    private ShutdownActivityStateTransition(String description) {
      super(description);
    }

    @Override
    public TransitionResult canTransition(ActivityState state) {
      // Pretty much can always shut down
      switch (state) {
        case READY:
          return TransitionResult.NOOP;
        case DEPLOY_ATTEMPT:
        case DEPLOY_FAILURE:
        case DOESNT_EXIST:
        case UNKNOWN:
          return TransitionResult.ILLEGAL;
        default:
          return TransitionResult.OK;
      }
    }

    @Override
    public void onTransition(ActivityState currentState, ActivityControl activity) {
      activity.shutdown();
    }
  }
}
