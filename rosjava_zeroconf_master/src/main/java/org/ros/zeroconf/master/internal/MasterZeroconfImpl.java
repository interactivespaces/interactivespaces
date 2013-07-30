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

package org.ros.zeroconf.master.internal;

import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.CoreController;
import org.ros.osgi.master.core.CoreControllerListener;
import org.ros.zeroconf.common.RosZeroconf;
import org.ros.zeroconf.common.ZeroconfRosMasterInfo;
import org.ros.zeroconf.master.MasterZeroconf;

import java.net.URI;

/**
 * An implementation of the Zeroconf master.
 *
 * @author Keith M. Hughes
 */
public class MasterZeroconfImpl implements MasterZeroconf, CoreControllerListener {

  /**
   * The ROS environment this is running under.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The ROS core controller.
   */
  private CoreController coreController;

  /**
   * The ROS zeroconf provider.
   */
  private RosZeroconf rosZeroconf;

  /**
   * Information about the master.
   */
  private ZeroconfRosMasterInfo masterInfo;

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
    if (coreController != null) {
      coreController.removeListener(this);
    }
    unregisterMaster();
  }

  /**
   * Set the Ros Environment the server should run in.
   *
   * @param rosEnvironment
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * Remove the ROS Environment that was being used.
   *
   * @param rosEnvironment
   */
  public void unsetRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = null;
  }

  /**
   * Set the Core Controller the server should run in.
   *
   * @param coreController
   */
  public void setCoreController(CoreController coreController) {
    this.coreController = coreController;

    // Assuming that addListener will call the onCoreStartup()
    coreController.addListener(this);
  }

  /**
   * Remove the Core Controller that was being used.
   *
   * @param coreController
   */
  public void unsetCoreController(CoreController coreController) {
    this.coreController.removeListener(this);
    this.coreController = null;
    unregisterMaster();
  }

  /**
   * Set the Ros Zeroconf the server should run with.
   *
   * @param rosZeroconf
   */
  public void setRosZeroconf(RosZeroconf rosZeroconf) {
    this.rosZeroconf = rosZeroconf;
    registerMaster();
  }

  /**
   * Remove the RosZeroconf that was being used.
   *
   * @param rosZeroconf
   */
  public void unsetRosZeroconf(RosZeroconf rosZeroconf) {
    unregisterMaster();
    this.rosZeroconf = null;
  }

  @Override
  public void onCoreStartup() {
    unregisterMaster();

    String nodeName = rosEnvironment.getNodeName();
    if (nodeName.startsWith("/")) {
      nodeName = nodeName.substring(1);
    }

    URI rosMasterUri = rosEnvironment.getMasterUri();

    // First 1 at end is the priority of the mater and the second 1 is the
    // weight of the server.
    // TODO(keith): Make this settable.
    masterInfo =
        new ZeroconfRosMasterInfo(nodeName, rosEnvironment.getNetworkType(), "http",
            rosMasterUri.getHost(), rosMasterUri.getPort(), 1, 1);
    registerMaster();
  }

  @Override
  public void onCoreShutdown() {
    unregisterMaster();
  }

  /**
   * Register the master information object, if any.
   */
  private void registerMaster() {
    if (masterInfo != null && rosZeroconf != null) {
      rosZeroconf.registerMaster(masterInfo);
    }
  }

  /**
   * Unregister the master information object, if any.
   */
  private void unregisterMaster() {
    if (masterInfo != null && rosZeroconf != null) {
      rosZeroconf.unregisterMaster(masterInfo);
    }
  }
}
