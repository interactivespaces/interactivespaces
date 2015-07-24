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

package org.ros.osgi.master.core.internal;

import org.ros.osgi.master.core.RosMasterController;
import org.ros.osgi.master.core.RosMasterControllerFactory;

/**
 * A standard factory for creating ROS master Controller instances.
 *
 * @author Keith M. Hughes
 */
public class StandardRosMasterControllerFactory implements RosMasterControllerFactory {

  @Override
  public RosMasterController newInternalController() {
    return new RosJavaRosMasterController();
  }

  @Override
  public RosMasterController newExternalController() {
    return new NativeRosMasterController();
  }
}
