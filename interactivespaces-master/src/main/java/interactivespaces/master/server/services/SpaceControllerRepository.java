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

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.expression.FilterExpression;

import java.util.List;

/**
 * A repository which stores the domain model for space controllers.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerRepository {

  /**
   * Create a new controller. It will not be saved in the repository.
   *
   * <p>
   * The controller will be assigned a UUID.
   *
   * @return the new controller instance
   */
  SpaceController newSpaceController();

  /**
   * Create a new controller from a template. It will not be saved in the
   * repository.
   *
   * <p>
   * The controller will be assigned a UUID.
   *
   * @param template
   *          the template controller whose values will be copied in
   *
   * @return the new controller instance
   */
  SpaceController newSpaceController(SpaceController template);

  /**
   * Create a new controller from a template object with a specified UUID. It
   * will not be saved in the repository.
   *
   * @param uuid
   *          the UUID to give to the controller
   * @param template
   *          the template controller whose values will be copied in
   *
   * @return the new controller instance
   */
  SpaceController newSpaceController(String uuid, SpaceController template);

  /**
   * Get the number of space controllers in the repository.
   *
   * @return the number of space controllers in the repository
   */
  long getNumberSpaceControllers();

  /**
   * Get all controllers in the repository.
   *
   * @return all controllers in the repository
   */
  List<SpaceController> getAllSpaceControllers();

  /**
   * Get all controllers in the repository matching a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return all controllers in the repository matching the given filter
   */
  List<SpaceController> getSpaceControllers(FilterExpression filter);

  /**
   * Get a controller by its ID.
   *
   * @param id
   *          the id of the desired controller
   *
   * @return the controller with the given id or {@code null} if no such
   *         controller
   */
  SpaceController getSpaceControllerById(String id);

  /**
   * Get a controller by its UUID.
   *
   * @param uuid
   *          The UUID of the desired controller.
   *
   * @return the controller with the given UUID or {@code null} if no such
   *         controller
   */
  SpaceController getSpaceControllerByUuid(String uuid);

  /**
   * Save a controller in the repository.
   *
   * <p>
   * Is used both to save a new controller into the repository for the first
   * time or to update edits to the controller.
   *
   * @param controller
   *          the controller to save
   *
   * @return the persisted controller, use this one going forward
   */
  SpaceController saveSpaceController(SpaceController controller);

  /**
   * Delete a controller in the repository.
   *
   * @param controller
   *          the controller to delete
   */
  void deleteSpaceController(SpaceController controller);
}
