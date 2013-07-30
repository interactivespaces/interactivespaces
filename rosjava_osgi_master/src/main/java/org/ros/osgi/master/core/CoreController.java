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

/**
 * A controller for the ROS Master (Core)
 *
 * @author Keith M. Hughes
 */
public interface CoreController {

  /**
   * Start the Ros master up.
   */
  void startup();

  /**
   * Shut the master down.
   */
  void shutdown();

  /**
   * Add a controller listener to the controller.
   *
   * @param listener
   */
  void addListener(CoreControllerListener listener);

  /**
   * Remove a controller listener from the controller.
   *
   * <p>
   * Nothing will happen if the listener wasn't added before.
   *
   * @param listener
   */
  void removeListener(CoreControllerListener listener);
}
