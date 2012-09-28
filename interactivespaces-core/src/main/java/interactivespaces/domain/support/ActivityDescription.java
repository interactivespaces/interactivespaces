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

package interactivespaces.domain.support;

import interactivespaces.domain.basic.pojo.SimpleActivity;

/**
 * A description of an activity
 *
 * @author Keith M. Hughes
 */
public class ActivityDescription extends SimpleActivity {
	
	/**
	 * The activity's runtime name.
	 */
	private String activityRuntimeName;

	/**
	 * The type of builder for the project.
	 */
	private String builderType;
	
	/**
	 * The runtime for the activity.
	 */
	private LiveActivityRuntime runtime;

	/**
	 * Get the activity topic name for the project.
	 * 
	 * @return the activity topic name
	 */
	public String getActivityRuntimeName() {
		return activityRuntimeName;
	}

	/**
	 * Set the activity runtime name for the project.
	 * 
	 * @param activityRuntimeName 
	 * 			the activity runtime name
	 */
	public void setActivityRuntimeName(String activityRuntimeName) {
		this.activityRuntimeName = activityRuntimeName;
	}

	/**
	 * Get the type of builder to be used for the activity.
	 * 
	 * @return the type of builder
	 */
	public String getBuilderType() {
		return builderType;
	}

	/**
	 * Set the type of builder to be used for the project.
	 * 
	 * @param builderType
	 *            the type of builder
	 */
	public void setBuilderType(String builderType) {
		this.builderType = builderType;
	}

	/**
	 * @return the runtime
	 */
	public LiveActivityRuntime getRuntime() {
		return runtime;
	}

	/**
	 * @param runtime the runtime to set
	 */
	public void setRuntime(LiveActivityRuntime runtime) {
		this.runtime = runtime;
	}
}
