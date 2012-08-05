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

import interactivespaces.activity.ActivityStateTransition.TransitionResult;

import java.util.Queue;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Run an activity through a set of state transitions.
 * 
 * <p>
 * This class is not threadsafe!
 * 
 * @author Keith M. Hughes
 */
public class ActivityStateTransitioner {

	/**
	 * Create the queue of transitions in a syntactically pleasing way.
	 * 
	 * @param transitions
	 *            the transitions to be queued
	 * 
	 * @return the queue of transitions
	 */
	public static Queue<ActivityStateTransition> transitions(
			ActivityStateTransition... transitions) {
		Queue<ActivityStateTransition> result = Lists.newLinkedList();

		if (transitions != null) {
			for (ActivityStateTransition transition : transitions) {
				result.add(transition);
			}
		}

		return result;
	}

	/**
	 * Control for the activity.
	 */
	private ActivityControl activity;

	/**
	 * The transitions which need to take place.
	 */
	private Queue<ActivityStateTransition> transitions;

	/**
	 * Whether or not there was an error.
	 */
	private boolean errored;

	/**
	 * Log for errors.
	 */
	private Log log;

	/**
	 * @param activity
	 *            the control for the activity being transitioned
	 * @param transitions
	 *            the transitions that should be handled
	 */
	public ActivityStateTransitioner(ActivityControl activity,
			Queue<ActivityStateTransition> transitions, Log log) {
		this.activity = activity;
		this.transitions = transitions;
		this.log = log;
	}

	/**
	 * Transition to a new state.
	 * 
	 * @param nextState
	 *            the state that was transitioned to
	 * 
	 * @return the result of the transition
	 */
	public SequenceTransitionResult transition(ActivityState nextState) {
		if (!nextState.isTransitional()) {
			return SequenceTransitionResult.WORKING;
		}

		if (errored) {
			return SequenceTransitionResult.CANT;
		}

		ActivityStateTransition nextTransition = transitions.peek();
		if (nextTransition == null) {
			return SequenceTransitionResult.DONE;
		}

		TransitionResult canTransition = nextTransition
				.canTransition(nextState);
		if (canTransition.equals(TransitionResult.WAIT)) {
			// Don't consume the transition yet. WAIT means we are on our way.
			return SequenceTransitionResult.WORKING;
		} else if (canTransition.equals(TransitionResult.OK)) {
			transitions.poll();
			try {
				nextTransition.transition(activity);
			} catch (Exception e) {
				errored = true;

				log.error("Error during activity transition", e);

				return SequenceTransitionResult.ERROR;
			}
		} else {
			log.warn("Activity cannot transition");
			return SequenceTransitionResult.CANT;
		}

		if (transitions.isEmpty()) {
			return SequenceTransitionResult.DONE;
		} else {
			return SequenceTransitionResult.WORKING;
		}
	}

	/**
	 * Result of a transition attempt.
	 * 
	 * @author Keith M. Hughes
	 */
	public enum SequenceTransitionResult {

		/**
		 * Still working on the transitions.
		 */
		WORKING,

		/**
		 * The transitions are done.
		 */
		DONE,

		/**
		 * Can't transition. may be because it errored before, or the transition
		 * is illegal.
		 */
		CANT,

		/**
		 * An error occured during transition.
		 */
		ERROR
	}
}
