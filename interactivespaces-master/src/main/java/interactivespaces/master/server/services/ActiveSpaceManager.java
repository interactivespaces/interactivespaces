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

import interactivespaces.domain.space.Space;

/**
 * Manager for instantiating a space.
 *
 * @author Keith M. Hughes
 */
public interface ActiveSpaceManager {

  /**
   * Deploy all assets for the space.
   *
   * @param space
   *          the space
   */
  void deploySpace(Space space);

  /**
   * Configure all assets in the space
   *
   * @param space
   *          the space
   */
  void configureSpace(Space space);

  /**
   * Start the given space. This will start all required activities needed by
   * the space. Child spaces will be started first.
   *
   * @param space
   *          The space to start.
   */
  void startupSpace(Space space);

  /**
   * Shut a given space down. Child spaces will be shut down first.
   *
   * @param space
   */
  void shutdownSpace(Space space);

  /**
   * Activate a space. Child spaces will be activated first.
   *
   * @param space
   */
  void activateSpace(Space space);

  /**
   * Deactivate a space. Child spaces will be deactivated first.
   *
   * @param space
   */
  void deactivateSpace(Space space);
}
