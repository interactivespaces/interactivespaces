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

	UNKNOWN("space.activity.state.unknown", false, false), 
	DOESNT_EXIST("space.activity.state.nonexistent", false, false), 
	DEPLOY_ATTEMPT("space.activity.state.deployment.attempt", false, false), 
	DEPLOY_FAILURE("space.activity.state.deployment.failure", false, true),
	READY("space.activity.state.ready", false, false),
	STARTUP_ATTEMPT("space.activity.state.start.attempt", false, false), 
	STARTUP_FAILURE("space.activity.state.start.failure", false, true),
	RUNNING("space.activity.state.running", true, false), 
	ACTIVATE_ATTEMPT("space.activity.state.activate.attempt", true, false),
	ACTIVATE_FAILURE("space.activity.state.activate.failure", true, true), 
	ACTIVE("space.activity.state.active", true, false), 
	DEACTIVATE_ATTEMPT("space.activity.state.deactivate.attempt", true, false),
	DEACTIVATE_FAILURE("space.activity.state.deactivate.failure", true, true),
	SHUTDOWN_ATTEMPT("space.activity.state.shutdown.attempt", false, false), 
	SHUTDOWN_FAILURE("space.activity.state.shutdown.failure", false, true), 
	CRASHED("space.activity.state.crashed", false, true);

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

	ActivityState(String description, boolean running, boolean error) {
		this.description = description;
		this.running = running;
		this.error = error;
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
}
