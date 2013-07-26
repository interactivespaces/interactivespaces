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

package interactivespaces.master.server.ui;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;

import java.util.List;

/**
 * Manager for the ui control of controllers.
 *
 * @author Keith M. Hughes
 */
public interface UiControllerManager {

  /**
   * Message key for non-existent controllers.
   */
  public static final String MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN =
      "space.domain.controller.unknown";

  /**
   * Delete a controller from the controller repository.
   *
   * @param id
   *          ID of the controller.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deleteController(String id);

  /**
   * Shut down a set of controllers controller.
   *
   * @param ids
   *          IDs of the controllers
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager shutdownControllers(List<String> ids);

  /**
   * Connect to all controllers in the repository.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager connectToAllControllers();

  /**
   * Disconnect to all controllers in the repository.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager disconnectFromAllControllers();

  /**
   * Shut down all controllers in the repository.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager shutdownAllControllers();

  /**
   * Get the status from all controllers in the repository that are not marked
   * unknown. This means no one has tried connecting.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager statusFromAllControllers();

  /**
   * Get the status from all controllers in the repository, whether or not they
   * are connected.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager forceStatusFromAllControllers();

  /**
   * Connect to all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param ids
   *          IDs of all controllers.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager connectToControllers(List<String> ids);

  /**
   * Disconnect from all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param ids
   *          IDs of all controllers.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager disconnectFromControllers(List<String> ids);

  /**
   * Get a status from all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param ids
   *          IDs of all controllers.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager statusControllers(List<String> ids);

  /**
   * Shut down all activities on the specified controller.
   *
   * @param id
   *          ID of the controller.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager shutdownAllActivities(String id);

  /**
   * Shut down all activities on all controllers.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager shutdownAllActivitiesAllControllers();

  /**
   * Deploy all activities which are on the specified controller.
   *
   * @param id
   *          ID of the controller
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deployAllControllerActivityInstances(String id);

  /**
   * Deploy all out of date live activities which are based on the specified
   * activity.
   *
   * @param id
   *          ID of the activity
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deployAllActivityInstances(String id);

  /**
   * Deploy the specified live activity to its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deployLiveActivity(String id);

  /**
   * Delete the specified live activity from its controller.
   *
   * @param id
   *          ID of the live activity
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deleteLiveActivity(String id);

  /**
   * Configure a live activity on its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager configureLiveActivity(String id);

  /**
   * Start a live activity on its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager startupLiveActivity(String id);

  /**
   * Activate a live activity on its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager activateLiveActivity(String id);

  /**
   * Deactivate a live activity on its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deactivateLiveActivity(String id);

  /**
   * Shut a live activity down on its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager shutdownLiveActivity(String id);

  /**
   * Status of a live activity on its controller.
   *
   * @param id
   *          ID of the live activity.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager statusLiveActivity(String id);

  /**
   * Deploy the specified activity group.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deployLiveActivityGroup(String id);

  /**
   * Configure the specified activity group.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager configureLiveActivityGroup(String id);

  /**
   * Start an activity group on its controller.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager startupLiveActivityGroup(String id);

  /**
   * Activate an activity group on its controller.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager activateLiveActivityGroup(String id);

  /**
   * Deactivate an activity group on its controller.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager deactivateLiveActivityGroup(String id);

  /**
   * Shut an activity group
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager shutdownLiveActivityGroup(String id);

  /**
   * Force all live activities in a live activity group to shutdown
   *
   * @param id
   *          ID of the activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager forceShutdownLiveActivitiesLiveActivityGroup(String id);

  /**
   * Status of all live activities in a live activity group.
   *
   * @param id
   *          ID of the live activity group.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager statusLiveActivityGroup(String id);

  /**
   * Status of all live activities in all live activity groups in a space and
   * its subspaces.
   *
   * @param id
   *          ID of the space.
   *
   * @return the controller manager which performed the operation.
   */
  UiControllerManager liveActivityStatusSpace(String id);

  /**
   * Get a list of all live activities and, if any, the associated active
   * counterpart.
   *
   * <p>
   * The latter won't be there if the live activity isn't associated with a
   * controller.
   *
   * @return
   */
  List<UiLiveActivity> getAllUiLiveActivities();

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
   * @return
   */
  List<UiLiveActivity> getAllUiLiveActivitiesByController(SpaceController controller);

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
   * @return
   */
  UiLiveActivity getUiLiveActivity(String id);

  /**
   * Get the ui live activity component for all given live activities.
   *
   * @param activities
   *          the activities
   *
   * @return
   */
  List<UiLiveActivity> getUiLiveActivities(List<LiveActivity> activities);

}
