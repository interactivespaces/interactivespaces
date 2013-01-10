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

package interactivespaces.master.server.services.internal;

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivityGroup;

import java.util.Set;

/**
 * An internal active controller manager which exposes more methods.
 * 
 * @author Keith M. Hughes
 */
public interface InternalActiveControllerManager extends
		ActiveControllerManager {

	/**
	 * Get the active activity group for a given activity group.
	 * 
	 * @param activityGroup
	 *            the activity group
	 * 
	 * @return the active activity group
	 */
	ActiveLiveActivityGroup getActiveLiveActivityGroup(
			LiveActivityGroup activityGroup);

	/**
	 * Deploy a active activity.
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to deploy
	 */
	void deployActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Delete a active activity.
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to delete
	 */
	void deleteActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Configure a active activity.
	 * 
	 * <p>
	 * A full configuration packet will be sent
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to configure
	 */
	void configureActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Start a active activity.
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to start up
	 */
	void startupActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Activate a active activity.
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to activate
	 */
	void activateActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Deactivate a active activity.
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to deactivate
	 */
	void deactivateActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Shut down a active activity.
	 * 
	 * @param activeLiveActivity
	 *            the active live activity to shut down
	 */
	void shutdownActiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Status of a active live activity.
	 * 
	 * @param tiveLiveActivity
	 *            the active live activity
	 */
	void statusActiveLiveActivity(ActiveLiveActivity activeLiveActivity);

	/**
	 * Deploy a active activity group on a controller.
	 * 
	 * @param activeLiveActivityGroup
	 *            The active activity group to deploy.
	 */
	void deployActiveLiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup);

	/**
	 * Deploy a active activity group on a controller.
	 * 
	 * <p>
	 * Live activities in the set will not be deployed. Also, any Live
	 * Activities deployed will be added to the set.
	 * 
	 * @param activeLiveActivityGroup
	 *            the active activity group to deploy
	 * @param deployedLiveActivities
	 *            the live activities that have been deployed so far (can be
	 *            {@code null})
	 */
	void deployActiveLiveActivityGroupChecked(
			ActiveLiveActivityGroup activeLiveActivityGroup,
			Set<ActiveLiveActivity> deployedLiveActivities);

	/**
	 * Configure a active activity group on a controller.
	 * 
	 * @param activeLiveActivityGroup
	 *            The active activity group to configure.
	 */
	void configureActiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup);

	/**
	 * Deploy a active activity group on a controller.
	 * 
	 * <p>
	 * Live activities in the set will not be configured. Also, any Live
	 * Activities deployed will be added to the set.
	 * 
	 * @param activeLiveActivityGroup
	 *            the active activity group to configure
	 * @param configuredLiveActivities
	 *            the live activities that have been configured so far (can be
	 *            {@code null})
	 */
	void configureActiveLiveActivityGroupChecked(
			ActiveLiveActivityGroup activeLiveActivityGroup,
			Set<ActiveLiveActivity> configuredLiveActivities);

	/**
	 * Start a active activity group on a controller.
	 * 
	 * @param activeLiveActivityGroup
	 *            The active activity group to start.
	 */
	void startupActiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup);

	/**
	 * Activate a active activity group on a controller.
	 * 
	 * @param activeLiveActivityGroup
	 *            The active activity group to activate.
	 */
	void activateActiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup);

	/**
	 * Deactivate a active activity group on a controller.
	 * 
	 * @param activeLiveActivityGroup
	 *            The active activity group to deactivate.
	 */
	void deactivateActiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup);

	/**
	 * Shut down an active activity group on a controller.
	 * 
	 * @param activeLiveActivityGroup
	 *            The active activity group to shut down.
	 */
	void shutdownActiveActivityGroup(ActiveLiveActivityGroup activeLiveActivityGroup);
}
