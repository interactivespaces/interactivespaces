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

import interactivespaces.container.control.message.activity.LiveActivityDeleteRequest;
import interactivespaces.container.control.message.activity.LiveActivityDeploymentRequest;
import interactivespaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import interactivespaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.master.server.services.internal.RemoteSpaceControllerClientListenerCollection;
import interactivespaces.util.resource.ManagedResource;

/**
 * A client for speaking to a remote controller.
 *
 * @author Keith M. Hughes
 */
public interface RemoteSpaceControllerClient extends ManagedResource {

/**
   * Connect to the given controller.
   *
   * <p>
   * Connecting to a controller is necessary to receive status messages
   * from it.
   *
   * <p>
   * All operations which affect a controller, such as
   * {@link #shutdownSpacecontrollerAllActivities(ActiveSpaceController) and
   * {@link #activateLiveActivity(ActiveLiveActivity) all will autoconnect to a
   * controller if necessary.
   *
   * <p>
   * Warning: In the current version, the first autoconnect attempt will have to
   * happen at least twice with a suitable delay due to connect times. This needs
   * to change.
   *
   * @param controller
   *        the controller to connect to
   */
  void connectToSpaceController(ActiveSpaceController controller);

  /**
   * Disconnect from the given controller.
   *
   * @param controller
   *          the controller to disconnect from
   */
  void disconnectFromSpaceController(ActiveSpaceController controller);

  /**
   * Request the shutdown of a controller.
   *
   * @param controller
   *          the controller to shut down
   */
  void requestSpaceControllerShutdown(ActiveSpaceController controller);

  /**
   * Request the status of a controller.
   *
   * @param controller
   *          the controller to get the status from
   */
  void requestSpaceControllerStatus(ActiveSpaceController controller);

  /**
   * Request the shut down of all activities running on a controller.
   *
   * @param controller
   *          the controller containing the activities to stop running
   */
  void shutdownSpacecontrollerAllActivities(ActiveSpaceController controller);

  /**
   * Configure the space controller.
   *
   * @param controller
   *          controller to configure
   */
  void configureSpaceController(ActiveSpaceController controller);

  /**
   * Clean the temp data folder for the controller.
   *
   * @param controller
   *          controller to clean
   */
  void cleanSpaceControllerTempData(ActiveSpaceController controller);

  /**
   * Clean the permanent data folder for the controller.
   *
   * @param controller
   *          controller to clean
   */
  void cleanSpaceControllerPermanentData(ActiveSpaceController controller);

  /**
   * Clean the temp data folder for all live activities on the controller.
   *
   * @param controller
   *          controller to clean
   */
  void cleanSpaceControllerActivitiesTempData(ActiveSpaceController controller);

  /**
   * Clean the permanent data folder for all live activities on the controller.
   *
   * @param controller
   *          controller to clean
   */
  void cleanSpaceControllerActivitiesPermanentData(ActiveSpaceController controller);

  /**
   * Capture the data bundle for the given controller.
   *
   * @param controller
   *          controller for which to capture the data bundle
   */
  void captureSpaceControllerDataBundle(ActiveSpaceController controller);

  /**
   * Restore the data bundle for the given controller.
   *
   * @param controller
   *          controller for which to capture the data bundle
   */
  void restoreSpaceControllerDataBundle(ActiveSpaceController controller);

  /**
   * Deploy an activity to its controller.
   *
   * @param liveActivity
   *          the live activity being deployed
   * @param request
   *          the deployment request
   */
  void deployLiveActivity(ActiveLiveActivity liveActivity, LiveActivityDeploymentRequest request);

  /**
   * Query a controller about a resource deployment.
   *
   * @param controller
   *          the controller to be queried
   * @param query
   *          the query
   */
  void querySpaceControllerResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentQueryRequest query);

  /**
   * Commit a resource deployment to a controller.
   *
   * @param controller
   *          the controller to be queried
   * @param request
   *          the commit request
   */
  void commitSpaceControllerResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentCommitRequest request);

  /**
   * Delete a live activity from its controller.
   *
   * @param liveActivity
   *          the live activity being deleted
   * @param request
   *          the deletion request
   */
  void deleteLiveActivity(ActiveLiveActivity liveActivity, LiveActivityDeleteRequest request);

  /**
   * Fully configure an activity on its controller.
   *
   * @param activity
   *          the activity to configure
   */
  void fullConfigureLiveActivity(ActiveLiveActivity activity);

  /**
   * Start an activity on its controller.
   *
   * @param activity
   *          the activity to start
   */
  void startupLiveActivity(ActiveLiveActivity activity);

  /**
   * Activate an activity on its controller.
   *
   * @param activity
   *          the activity to activate
   */
  void activateLiveActivity(ActiveLiveActivity activity);

  /**
   * Deactivate an activity on its controller.
   *
   * @param activity
   *          the activity to deactivate
   */
  void deactivateLiveActivity(ActiveLiveActivity activity);

  /**
   * Shut an activity down on its controller.
   *
   * @param activity
   *          the activity to shut down
   */
  void shutdownLiveActivity(ActiveLiveActivity activity);

  /**
   * Get the status of an activity on its controller.
   *
   * @param activity
   *          the activity to get the status from
   */
  void statusLiveActivity(ActiveLiveActivity activity);

  /**
   * Clean the permanent data for an activity on its controller.
   *
   * @param activity
   *          the activity to clean
   */
  void cleanLiveActivityPermanentData(ActiveLiveActivity activity);

  /**
   * Clean the temp data for an activity on its controller.
   *
   * @param activity
   *          the activity to clean
   */
  void cleanLiveActivityTempData(ActiveLiveActivity activity);

  /**
   * Add in a new event listener for events from the client.
   *
   * @param listener
   *          the new listener
   */
  void addRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener);

  /**
   * Remove an event listener for events from the client.
   *
   * <p>
   * Does nothing if the listener wasn't registered.
   *
   * @param listener
   *          the listener to remove
   */
  void removeRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener);

  /**
   * Get all the status listeners for controllers.
   *
   * @return the helper which handles the listeners
   */
  RemoteSpaceControllerClientListenerCollection getRemoteControllerClientListeners();

  /**
   * Register a remote activity deployment manager with the remote controller.
   *
   * @param remoteActivityDeploymentManager
   *          the deployment manager
   *
   * @return the helper for the manager to use
   */
  RemoteSpaceControllerClientListenerCollection registerRemoteActivityDeploymentManager(
      RemoteActivityDeploymentManager remoteActivityDeploymentManager);
}
