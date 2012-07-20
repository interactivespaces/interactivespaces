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

package interactivespaces.master.server.services;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;

import java.util.List;

/**
 * A manager for controllers running on nodes on the network.
 * 
 * <p>
 * This manager should not be usually used directly, but should instead be used
 * through a {@link ActiveSpaceManager}.
 * 
 * @author Keith M. Hughes
 */
public interface ActiveControllerManager {

	/**
	 * Connect to the specified controller.
	 * 
	 * <p>
	 * Most functions here will automatically connect to a controller. This is
	 * useful if status updates are needed before doing anything else with the
	 * controller.
	 * 
	 * @param controller
	 */
	void connectController(SpaceController controller);

	/**
	 * Disconnect from the specified controller.
	 * 
	 * <p>
	 * Most functions here will automatically connect to a controller. This is
	 * useful if status updates are needed before doing anything else with the
	 * controller.
	 * 
	 * @param controller
	 */
	void disconnectController(SpaceController controller);

	/**
	 * Restart a controller node.
	 * 
	 * <p>
	 * The controller node will be brought up in a clean state.
	 * 
	 * @param controller
	 *            The node to restart.
	 */
	void restartController(SpaceController controller);

	/**
	 * Shutdown a controller node.
	 * 
	 * @param controller
	 *            The node to shutdown.
	 */
	void shutdownController(SpaceController controller);

	/**
	 * Request a status from a controller node.
	 * 
	 * @param controller
	 *            The node.
	 */
	void statusController(SpaceController controller);

	/**
	 * Shutdown all activities on a controller node.
	 * 
	 * @param controller
	 *            The node to shutdown.
	 */
	void shutdownAllActivities(SpaceController controller);

	/**
	 * Deploy an activity on a controller.
	 * 
	 * @param activity
	 *            The activity to deploy.
	 */
	void deployLiveActivity(LiveActivity activity);

	/**
	 * Configure an activity on a controller.
	 * 
	 * @param activity
	 *            The activity to configure.
	 */
	void configureLiveActivity(LiveActivity activity);

	/**
	 * Start an activity on a controller.
	 * 
	 * @param activity
	 *            The activity to start.
	 */
	void startupLiveActivity(LiveActivity activity);

	/**
	 * Activate an activity on a controller.
	 * 
	 * @param activity
	 *            The activity to activate.
	 */
	void activateLiveActivity(LiveActivity activity);

	/**
	 * Deactivate an activity on a controller.
	 * 
	 * @param activity
	 *            The activity to deactivate.
	 */
	void deactivateLiveActivity(LiveActivity activity);

	/**
	 * Shut down an activity on a controller.
	 * 
	 * <p>
	 * The activity will be shut down even if it is running in several activity
	 * groups.
	 * 
	 * @param activity
	 *            The activity to shut down.
	 */
	void shutdownLiveActivity(LiveActivity activity);

	/**
	 * Status of an activity on its controller.
	 * 
	 * 
	 * @param activity
	 *            The activity.
	 */
	void statusLiveActivity(LiveActivity activity);

	/**
	 * Deploy an activity group on a controller.
	 * 
	 * @param activityGroup
	 *            The activity group to deploy.
	 */
	void deployLiveActivityGroup(LiveActivityGroup activityGroup);

	/**
	 * Configure an activity group on a controller.
	 * 
	 * @param activityGroup
	 *            The activity group to deploy.
	 */
	void configureLiveActivityGroup(LiveActivityGroup activityGroup);

	/**
	 * Start an activity group on a controller.
	 * 
	 * @param activityGroup
	 *            The activity group to start.
	 */
	void startupLiveActivityGroup(LiveActivityGroup activityGroup);

	/**
	 * Activate an activity group on a controller.
	 * 
	 * @param activityGroup
	 *            The activity group to activate.
	 */
	void activateLiveActivityGroup(LiveActivityGroup activityGroup);

	/**
	 * Deactivate an activity group on a controller.
	 * 
	 * @param activityGroup
	 *            The activity group to deactivate.
	 */
	void deactivateLiveActivityGroup(LiveActivityGroup activityGroup);

	/**
	 * Shut down an activity group on a controller.
	 * 
	 * <p>
	 * The individual activities in the group will only be actually shut down if
	 * they have been shut down in all activity groups which started the
	 * activity up.
	 * 
	 * @param activityGroup
	 *            The activity group to shut down.
	 */
	void shutdownLiveActivityGroup(LiveActivityGroup activityGroup);

	/**
	 * Get the active activity associated with a given activity.
	 * 
	 * @param activity
	 *            The activity.
	 * 
	 * @return The active activity for the activity.
	 */
	ActiveLiveActivity getActiveLiveActivity(LiveActivity activity);

	/**
	 * Get the active activities associated with the given activities.
	 * 
	 * @param activities
	 *            the activities
	 * 
	 * @return the active activities for the activities
	 */
	List<ActiveLiveActivity> getActiveLiveActivities(
			List<LiveActivity> activities);

	/**
	 * Get the active controller associated with a given controller.
	 * 
	 * @param controller
	 *            The controller.
	 * 
	 * @return The active controller for the controller.
	 */
	ActiveSpaceController getActiveSpaceController(SpaceController controller);

	/**
	 * Get the active controllers associated with the given controllers.
	 * 
	 * @param controllers
	 *            the controllers
	 * 
	 * @return the active controllers for the controllers
	 */
	List<ActiveSpaceController> getActiveSpaceControllers(
			List<SpaceController> controller);

	/**
	 * Add in a new controller listener.
	 * 
	 * @param listener
	 *            the new listener
	 */
	void addControllerListener(SpaceControllerListener listener);

	/**
	 * Remove a controller listener.
	 * 
	 * <p>
	 * Nothing will happen if the listener was not in.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	void removeControllerListener(SpaceControllerListener listener);
}
