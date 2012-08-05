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

/**
 * A transition from one activity state to another.
 * 
 * @author Keith M. Hughes
 */
public abstract class ActivityStateTransition {

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
	 * Text description of the transition.
	 */
	private String description;

	ActivityStateTransition(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Can a transition happen from the given state?
	 * 
	 * @param state
	 *            the current state
	 * 
	 * @return the result of the transition
	 */
	public abstract TransitionResult canTransition(ActivityState state);

	/**
	 * Perform the proper transition.
	 * 
	 * @param activity
	 *            the activity to transition
	 */
	public abstract void transition(ActivityControl activity);

	/**
	 * Attempt to do the transition. If it is legal, it will be done.
	 * 
	 * @param state
	 *            current state of the activity
	 * @param activity
	 *            control for the activity
	 * 
	 * @return what the transition result was when it was checked
	 */
	public TransitionResult attemptTransition(ActivityState state,
			ActivityControl activity) {
		TransitionResult result = canTransition(state);
		if (result.equals(TransitionResult.OK)) {
			transition(activity);
		}

		return result;
	}

	/**
	 * Transition for starting up an activity.
	 */
	private static class StartupActivityStateTransition extends
			ActivityStateTransition {

		StartupActivityStateTransition(String description) {
			super(description);
		}

		@Override
		public TransitionResult canTransition(ActivityState currentState) {
			if (currentState.isRunning()) {
				return TransitionResult.NOOP;
			} else if (currentState.equals(ActivityState.READY)) {
				return TransitionResult.OK;
			} else {
				return TransitionResult.ILLEGAL;
			}
		}

		@Override
		public void transition(ActivityControl activity) {
			activity.startup();
		}
	}

	/**
	 * Transition for activating an activity.
	 */
	private static class ActivateActivityStateTransition extends
			ActivityStateTransition {

		ActivateActivityStateTransition(String description) {
			super(description);
		}

		@Override
		public TransitionResult canTransition(ActivityState state) {
			if (state.equals(ActivityState.RUNNING)
					|| state.equals(ActivityState.ACTIVATE_FAILURE)) {
				return TransitionResult.OK;
			} else if (state.equals(ActivityState.ACTIVE)) {
				return TransitionResult.NOOP;
			} else {
				return TransitionResult.ILLEGAL;
			}
		}

		@Override
		public void transition(ActivityControl activity) {
			activity.activate();
		}
	}

	/**
	 * Transition for deactivating an activity.
	 */
	private static class DeactivateActivityStateTransition extends
			ActivityStateTransition {

		DeactivateActivityStateTransition(String description) {
			super(description);
		}

		@Override
		public TransitionResult canTransition(ActivityState state) {
			if (state.equals(ActivityState.ACTIVE)
					|| state.equals(ActivityState.ACTIVATE_FAILURE)
					|| state.equals(ActivityState.DEACTIVATE_FAILURE)) {
				return TransitionResult.OK;
			} else if (state.equals(ActivityState.RUNNING)) {
				return TransitionResult.NOOP;
			} else {
				return TransitionResult.ILLEGAL;
			}
		}

		@Override
		public void transition(ActivityControl activity) {
			activity.deactivate();
		}
	}

	/**
	 * Transition for shutting down an activity.
	 */
	private static class ShutdownActivityStateTransition extends
			ActivityStateTransition {

		ShutdownActivityStateTransition(String description) {
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
		public void transition(ActivityControl activity) {
			activity.shutdown();
		}
	}

	public enum TransitionResult {
		OK, WAIT, ILLEGAL, NOOP
	}
}
