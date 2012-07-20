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

package interactivespaces.controller.activity.configuration;

import interactivespaces.activity.ActivityFilesystem;

/**
 * Manage configurations for Interactive Spaces activities.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityConfigurationManager {

	/**
	 * Get a configuration.
	 * 
	 * @param activityFilesystem
	 *            filesystem for the activity
	 * @return the installation specific configuration
	 */
	SimpleActivityConfiguration getConfiguration(
			ActivityFilesystem activityFilesystem);
}
