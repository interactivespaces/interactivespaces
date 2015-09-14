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

package org.ros.osgi.master.core;

import org.ros.osgi.common.RosEnvironment;

/**
 * A controller for the ROS Master.
 *
 * @author Keith M. Hughes
 */
public interface RosMasterController {

  /**
   * Start the ROS Master up.
   */
  void startup();

  /**
   * Shut the ROS Master down.
   */
  void shutdown();

  /**
   * Add a listener to the controller.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(RosMasterControllerListener listener);

  /**
   * Remove a listener from the controller.
   *
   * <p>
   * Nothing will happen if the listener wasn't added before.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(RosMasterControllerListener listener);

  /**
   * Set the ROS environment to be used.
   *
   * @param rosEnvironment
   *          the ROS environment to be used
   */
  void setRosEnvironment(RosEnvironment rosEnvironment);

  /**
   * Get the ROS environment being used.
   *
   * @return the ROS environment
   */
  RosEnvironment getRosEnvironment();
}
