/*
 * Copyright (C) 2014 Google Inc.
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

import org.ros.osgi.common.BaseOsgiBundleActivator;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.CoreController;

/**
 * Bundle activator for this bundle.
 *
 * @author Keith M. Hughes
 */
public class RosOsgiMasterBundleActivator extends BaseOsgiBundleActivator {

  /**
   * The service tracker for the ROS environment.
   */
  private MyServiceTracker<RosEnvironment> rosEnvironmentServiceTracker;

  /**
   * The ROS master.
   */
  private JavaCoreController rosMaster;

  @Override
  protected void onStart() {
    rosEnvironmentServiceTracker = newMyServiceTracker(RosEnvironment.class.getName());
  }

  @Override
  protected void onStop() {
    if (rosMaster != null) {
      rosMaster.shutdown();
      rosMaster = null;
    }
  }

  @Override
  protected void allRequiredServicesAvailable() {
    RosEnvironment rosEnvironment = rosEnvironmentServiceTracker.getMyService();

    rosMaster = new JavaCoreController();
    rosMaster.setRosEnvironment(rosEnvironment);

    rosMaster.startup();
    registerOsgiService(CoreController.class.getName(), rosMaster);
  }
}
