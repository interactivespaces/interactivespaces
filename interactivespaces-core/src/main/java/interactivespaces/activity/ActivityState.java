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
 * State of an activity.
 * 
 * @author Keith M. Hughes
 */
public enum ActivityState {

	UNKNOWN("space.activity.state.unknown", false, false, true), 
	DOESNT_EXIST("space.activity.state.nonexistent", false, false, true), 
	DEPLOY_ATTEMPT("space.activity.state.deployment.attempt", false, false, false), 
	DEPLOY_FAILURE("space.activity.state.deployment.failure", false, true, true), 
	READY("space.activity.state.ready", false, false, true), 
	STARTUP_ATTEMPT("space.activity.state.start.attempt", false, false, false), 
	STARTUP_FAILURE("space.activity.state.start.failure", false, true, true), 
	RUNNING("space.activity.state.running", true, false, true), 
	ACTIVATE_ATTEMPT("space.activity.state.activate.attempt", true, false, false), 
	ACTIVATE_FAILURE("space.activity.state.activate.failure", true, true, true), 
	ACTIVE("space.activity.state.active", true, false, true), 
	DEACTIVATE_ATTEMPT("space.activity.state.deactivate.attempt", true, false, false),
	DEACTIVATE_FAILURE("space.activity.state.deactivate.failure", true, true, true), 
	SHUTDOWN_ATTEMPT("space.activity.state.shutdown.attempt", false, false, false), 
	SHUTDOWN_FAILURE("space.activity.state.shutdown.failure", false, true, true), 
	CRASHED("space.activity.state.crashed", false, true, true);

	/**
	 * Text description of the state.
	 */
	private String description;

	/**
	 * Is this a running state?
	 */
	private boolean running;

	/**
	 * Is this an error state?
	 */
	private boolean error;

	/**
	 * Is this a state that can be transitioned from (as opposed to merely
	 * notification)?
	 */
	private boolean transitional;

	ActivityState(String description, boolean running, boolean error,
			boolean transitional) {
		this.description = description;
		this.running = running;
		this.error = error;
		this.transitional = transitional;
	}

	/**
	 * Get the description key for the state.
	 * 
	 * @return a non-human consumable key which can be used for i18n.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Should the activity be considered running?
	 * 
	 * @return {@code true} if the activity is considered to be running.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Should the activity be considered in error?
	 * 
	 * @return {@code true} if the activity is considered to be in error.
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Is the activity's state transitional (as opposed to notificational)?
	 * 
	 * @return {@code true} if the activity's state is considered to be
	 *         transitional.
	 */
	public boolean isTransitional() {
		return transitional;
	}
}
