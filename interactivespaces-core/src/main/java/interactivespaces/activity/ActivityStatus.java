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
 * Status of an Interactive Spaces activity.
 *
 * @author Keith M. Hughes
 */
public class ActivityStatus {
	
	/**
	 * Status of the activity.
	 */
	private final ActivityState state;
	
	/**
	 * Description of the state.
	 */
	private final String description;
	
	/**
	 * Exception, if any.
	 * 
	 * <p>
	 * Can be {@code null}.
	 */
	private final Throwable exception;

	public ActivityStatus(ActivityState state, String description) {
		this(state, description, null);
	}

	public ActivityStatus(ActivityState state,
			String description, Throwable exception) {
		this.state = state;
		this.description = description;
		this.exception = exception;
	}

	/**
	 * @return the state
	 */
	public ActivityState getState() {
		return state;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return exception;
	}

	@Override
	public String toString() {
		return "ActivityState [state=" + state + ", description="
				+ description + ", exception=" + exception + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + state.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityStatus other = (ActivityStatus) obj;
		if (state != other.state)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;

		return true;
	}
}
