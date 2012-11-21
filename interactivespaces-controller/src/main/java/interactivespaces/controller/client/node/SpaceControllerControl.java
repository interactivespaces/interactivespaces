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

package interactivespaces.controller.client.node;

import java.util.List;
import java.util.Map;

import interactivespaces.controller.SpaceController;
import interactivespaces.controller.domain.InstalledLiveActivity;

/**
 * Control points for a {@link SpaceController}.
 * 
 * @author Keith M. Hughes
 * @since Aug 24, 2012
 */
public interface SpaceControllerControl extends SpaceController {

	/**
	 * Shut down the controller container.
	 */
	void shutdownControllerContainer();

	/**
	 * Configure the activity.
	 * 
	 * @param uuid
	 *            uuid of the activity
	 * @param configuration
	 *            the configuration request
	 */
	void configureActivity(String uuid, Map<String, Object> configuration);

	/**
	 * Get all live activities installed on this controller.
	 * 
	 * @returnAll locally installed activities.
	 */
	List<InstalledLiveActivity> getAllInstalledLiveActivities();

	/**
	 * Get an activity by UUID.
	 * 
	 * @param uuid
	 *            The UUID of the activity.
	 * 
	 * @return The activity with the given UUID. null if no such activity.
	 */
	ActiveControllerActivity getActiveActivityByUuid(String uuid);
}