/*
 * Copyright (C) 2015 Google Inc.
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
 * A factory for creating ROS Master controller instances.
 *
 * <p>
 * An internal controller runs inside the Java process. An external controller does not.
 *
 * @author Keith M. Hughes
 */
public interface RosMasterControllerFactory {

  /**
   * Create a new internal controller.
   *
   * @return a new internal controller
   */
  RosMasterController newInternalController();

  /**
   * Create a new external controller.
   *
   * @return a new external controller
   */
  RosMasterController newExternalController();

}
