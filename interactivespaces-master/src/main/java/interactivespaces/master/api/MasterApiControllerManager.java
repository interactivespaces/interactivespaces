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

package interactivespaces.master.api;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;

import java.util.List;
import java.util.Map;

/**
 * Manager for the UI control of controllers.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiControllerManager {

  /**
   * Message key for non-existent controllers.
   */
  String MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN = "space.domain.controller.unknown";

  /**
   * Get the view of all space controllers.
   *
   * @return the Master API view of all space controllers
   */
  Map<String, Object> getSpaceControllerAllView();

  /**
   * Get the view of a space controller.
   *
   * @param id
   *          ID of the space controller
   *
   * @return the Master API view of the space controller
   */
  Map<String, Object> getSpaceControllerView(String id);

  /**
   * Delete a controller from the controller repository.
   *
   * @param id
   *          ID of the controller.
   *
   * @return the Master API response
   */
  Map<String, Object> deleteController(String id);

  /**
   * Shut down a set of controllers controller.
   *
   * @param ids
   *          IDs of the controllers
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownControllers(List<String> ids);

  /**
   * Connect to all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> connectToAllControllers();

  /**
   * Disconnect to all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> disconnectFromAllControllers();

  /**
   * Shut down all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownAllControllers();

  /**
   * Get the status from all controllers in the repository that are not marked
   * unknown. This means no one has tried connecting.
   *
   * @return the Master API response
   */
  Map<String, Object> statusFromAllControllers();

  /**
   * Get the status from all controllers in the repository, whether or not they
   * are connected.
   *
   * @return the Master API response
   */
  Map<String, Object> forceStatusFromAllControllers();

  /**
   * Connect to all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param ids
   *          IDs of all controllers
   *
   * @return the Master API response
   */
  Map<String, Object> connectToControllers(List<String> ids);

  /**
   * Disconnect from all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param ids
   *          IDs of all controllers
   *
   * @return the Master API response
   */
  Map<String, Object> disconnectFromControllers(List<String> ids);

  /**
   * Get a status from all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param ids
   *          IDs of all controllers
   *
   * @return the Master API response
   */
  Map<String, Object> statusControllers(List<String> ids);

  /**
   * Clean the temp data folder for the specified controller.
   *
   * @param id
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerTempData(String id);

  /**
   * Clean the temp data folder for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerTempDataAllControllers();

  /**
   * Clean the permanent data folder for the controller.
   *
   * @param id
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerPermanentData(String id);

  /**
   * Clean the permanent data folder for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerPermanentDataAllControllers();

  /**
   * Clean the temp data folder for all live activities on a controller.
   *
   * @param id
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerActivitiesTempData(String id);

  /**
   * Clean the temp data folder for all live activities on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerActivitiesTempDataAllControllers();

  /**
   * Clean the permanent data folder for all live activities on a controller.
   *
   * @param id
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerActivitiesPermanentData(String id);

  /**
   * Clean the permanent data folder all live activities on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanControllerActivitiesPermanentDataAllControllers();

  /**
   * Capture the data bundle for the given controller ID.
   *
   * @param id
   *          Id of the target controller
   *
   * @return the Master API response
   */
  Map<String, Object> captureControllerDataBundle(String id);

  /**
   * Restore the data bundle for the given controller ID.
   *
   * @param id
   *          Id of the target controller
   *
   * @return the Master API response
   */
  Map<String, Object> restoreControllerDataBundle(String id);

  /**
   * Capture the data bundle for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> captureDataAllControllers();

  /**
   * Restore the data bundles for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> restoreDataAllControllers();

  /**
   * Shut down all activities on the specified controller.
   *
   * @param id
   *          ID of the controller
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownAllActivities(String id);

  /**
   * Shut down all activities on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownAllActivitiesAllControllers();

  /**
   * Deploy all activities which are on the specified controller.
   *
   * @param id
   *          ID of the controller
   *
   * @return the Master API response
   */
  Map<String, Object> deployAllControllerActivityInstances(String id);

  /**
   * Deploy all out of date live activities which are based on the specified
   * activity.
   *
   * @param id
   *          ID of the activity
   *
   * @return the Master API response
   */
  Map<String, Object> deployAllActivityInstances(String id);

  /**
   * Deploy the specified live activity to its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> deployLiveActivity(String id);

  /**
   * Delete the specified live activity from its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> deleteLiveActivity(String id);

  /**
   * Configure a live activity on its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> configureLiveActivity(String id);

  /**
   * Start a live activity on its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> startupLiveActivity(String id);

  /**
   * Activate a live activity on its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> activateLiveActivity(String id);

  /**
   * Deactivate a live activity on its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> deactivateLiveActivity(String id);

  /**
   * Shut a live activity down on its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownLiveActivity(String id);

  /**
   * Status of a live activity on its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> statusLiveActivity(String id);

  /**
   * Clean the permanent data folder for the live activity.
   *
   * @param id
   *          ID of the live activity to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanLiveActivityPermanentData(String id);

  /**
   * Clean the temp data folder for the live activity.
   *
   * @param id
   *          ID of the live activity to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanLiveActivityTempData(String id);

  /**
   * Deploy the specified activity group.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> deployLiveActivityGroup(String id);

  /**
   * Configure the specified activity group.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> configureLiveActivityGroup(String id);

  /**
   * Start an activity group on its controller.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> startupLiveActivityGroup(String id);

  /**
   * Activate an activity group on its controller.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> activateLiveActivityGroup(String id);

  /**
   * Deactivate an activity group on its controller.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> deactivateLiveActivityGroup(String id);

  /**
   * Shut an activity group.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response.
   */
  Map<String, Object> shutdownLiveActivityGroup(String id);

  /**
   * Force all live activities in a live activity group to shutdown.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> forceShutdownLiveActivitiesLiveActivityGroup(String id);

  /**
   * Status of all live activities in a live activity group.
   *
   * @param id
   *          ID of the live activity group
   *
   * @return the Master API response
   */
  Map<String, Object> statusLiveActivityGroup(String id);

  /**
   * Status of all live activities in all live activity groups in a space and
   * its subspaces.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> liveActivityStatusSpace(String id);

  /**
   * Get a list of all live activities and, if any, the associated active
   * counterpart.
   *
   * <p>
   * The latter won't be there if the live activity isn't associated with a
   * controller.
   *
   * @return all UI live activities
   */
  List<MasterApiLiveActivity> getAllUiLiveActivities();

  /**
   * Get a list of all live activities and, if any, the associated active
   * counterpart, which are on the specified controller.
   *
   * <p>
   * The latter won't be there if the live activity isn't associated with a
   * controller.
   *
   * @param controller
   *          the controller which contains the activities
   *
   * @return all UI live activities for the controller
   */
  List<MasterApiLiveActivity> getAllUiLiveActivitiesByController(SpaceController controller);

  /**
   * Get a live activity and, if any, the associated active counterpart.
   *
   * <p>
   * The latter won't be there if the live activity isn't associated with a
   * controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the UI live activity
   */
  MasterApiLiveActivity getUiLiveActivity(String id);

  /**
   * Get the UI live activity component for all given live activities.
   *
   * @param activities
   *          the activities
   *
   * @return list of the UI live activities
   */
  List<MasterApiLiveActivity> getUiLiveActivities(List<LiveActivity> activities);
}
