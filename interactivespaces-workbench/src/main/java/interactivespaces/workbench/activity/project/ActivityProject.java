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

package interactivespaces.workbench.activity.project;

import interactivespaces.domain.support.ActivityDescription;

import java.io.File;

/**
 * A project for an activity.
 *
 * @author Keith M. Hughes
 */
public class ActivityProject {
	
	/**
	 * The base directory for the file.
	 */
	private File baseDirectory;
	
	/**
	 * The information known about the activity.
	 */
	private ActivityDescription activityDescription;
	
	/**
	 * Type of the activity.
	 */
	private String activityType;

	public ActivityProject(ActivityDescription activityDescription) {
		this.activityDescription = activityDescription;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @return the baseDirectory
	 */
	public File getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * Get the description for the activity
	 * 
	 * @return the activity
	 */
	public ActivityDescription getActivityDescription() {
		return activityDescription;
	}
	
	/**
	 * Get the location of the config file.
	 * 
	 * @return the location of the config file
	 */
	public File getActivityConfigFile() {
		return new File(getActivityResourceDirectory(), "activity.conf");
	}
	
	/**
	 * Get the location of the source resource directory.
	 * 
	 * @return the location of the source resource directory
	 */
	public File getActivityResourceDirectory() {
		return new File(baseDirectory, "src/main/resources/activity");
	}
	
	/**
	 * Get the location of the description file.
	 * 
	 * @return the location of the description file
	 */
	public File getActivityDescriptionFile() {
		return new File(baseDirectory, "activity.xml");
	}
	
	/**
	 * Get the location of the source directory for activities.
	 * 
	 * @return the location of the source directory for activities
	 */
	public File getActivitySourceFolder() {
		return new File(baseDirectory, "src/main/resources/activity");
	}

	/**
	 * @return the activityType
	 */
	public String getActivityType() {
		return activityType;
	}

	/**
	 * @param activityType the activityType to set
	 */
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
}
