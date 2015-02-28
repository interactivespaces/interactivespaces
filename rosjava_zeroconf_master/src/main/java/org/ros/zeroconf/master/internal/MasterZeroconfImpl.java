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
import org.ros.osgi.master.core.RosMasterController;
import org.ros.osgi.master.core.RosMasterControllerListener;
import org.ros.zeroconf.common.RosZeroconf;
import org.ros.zeroconf.common.ZeroconfRosMasterInfo;
import org.ros.zeroconf.master.MasterZeroconf;

import java.net.URI;

/**
 * An implementation of the Zeroconf master.
 *
 * @author Keith M. Hughes
 */
public class MasterZeroconfImpl implements MasterZeroconf, RosMasterControllerListener {

  /**
   * A comma-separated list of the protocols supported by the ROS Master for the zeroconf system.
   */
  public static final String ZEROCONF_PROTOCOL_SUPPORTED = "http";

  /**
   * The ROS environment this is running under.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The ROS core controller.
   */
  private RosMasterController rosMasterController;

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
    // Nothing to do.
  }

  @Override
  public void shutdown() {
    if (rosMasterController != null) {
      rosMasterController.removeListener(this);
    }
    unregisterMaster();
  }

  /**
   * Set the ROS Environment the server should run in.
   *
   * @param rosEnvironment
   *          the ROS environment to use
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * Set the ROS Master Controller the server should run in.
   *
   * @param rosMasterController
   *          the ROS Master controller
   */
  public void setRosMasterController(RosMasterController rosMasterController) {
    this.rosMasterController = rosMasterController;

    // Assuming that addListener will call the onRosMasterStartup()
    rosMasterController.addListener(this);
  }

  /**
   * Remove the ROS Master Controller that was being used.
   *
   * @param rosMasterController
   *          the ROS Master controller
   */
  public void unsetRosMasterController(RosMasterController rosMasterController) {
    this.rosMasterController.removeListener(this);
    this.rosMasterController = null;
    unregisterMaster();
  }

  /**
   * Set the ROS Zeroconf the server should run with.
   *
   * @param rosZeroconf
   *          the zeroconf for the ROS Master
   */
  public void setRosZeroconf(RosZeroconf rosZeroconf) {
    this.rosZeroconf = rosZeroconf;
    registerMaster();
  }

  /**
   * Remove the RosZeroconf that was being used.
   *
   * @param rosZeroconf
   *          the zeroconf for the ROS Master
   */
  public void unsetRosZeroconf(RosZeroconf rosZeroconf) {
    unregisterMaster();
    this.rosZeroconf = null;
  }

  @Override
  public void onRosMasterStartup() {
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
        new ZeroconfRosMasterInfo(nodeName, rosEnvironment.getNetworkType(), ZEROCONF_PROTOCOL_SUPPORTED,
            rosMasterUri.getHost(), rosMasterUri.getPort(), 1, 1);
    registerMaster();
  }

  @Override
  public void onRosMasterShutdown() {
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
