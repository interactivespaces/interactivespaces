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

package org.ros.zeroconf.node.internal;

import org.ros.master.uri.MasterUriProvider;
import org.ros.master.uri.SwitchableMasterUriProvider;
import org.ros.master.uri.SwitchableMasterUriProvider.MasterUriProviderSwitcher;
import org.ros.osgi.common.RosEnvironment;
import org.ros.zeroconf.common.RosZeroconf;
import org.ros.zeroconf.common.RosZeroconfListener;
import org.ros.zeroconf.common.ZeroconfRosMasterInfo;
import org.ros.zeroconf.common.selector.LightweightZeroconfServiceSelector;
import org.ros.zeroconf.common.selector.ZeroconfServiceSelector;
import org.ros.zeroconf.node.NodeZeroconf;

/**
 * An implementation of Zeroconf for ROS nodes.
 *
 * @author Keith M. Hughes
 */
public class NodeZeroconfImpl implements NodeZeroconf, RosZeroconfListener {

  /**
   * The ROS environment this is running under.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The ROS zeroconf provider.
   */
  private RosZeroconf rosZeroconf;

  /**
   * The Master URI provider for the system, if any.
   */
  private SwitchableMasterUriProvider masterUriProvider;

  /**
   * The provider that was replaced when the node zeroconf had enough
   */
  private MasterUriProvider replacedProvider;

  /**
   * The node zerconf is started.
   */
  private boolean started;

  /**
   * The master selector.
   */
  private ZeroconfServiceSelector<ZeroconfRosMasterInfo> masterSelector =
      new LightweightZeroconfServiceSelector<ZeroconfRosMasterInfo>();

  @Override
  public synchronized void startup() {
    started = true;
    startUsingProvider();
  }

  @Override
  public synchronized void shutdown() {
    stopUsingProvider();
    if (rosZeroconf != null) {
      rosZeroconf.removeListener(this);
    }

    started = false;
  }

  /**
   * Set the Ros Environment the server should run in.
   *
   * @param rosEnvironment
   */
  public synchronized void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
    startUsingProvider();
  }

  /**
   * Remove the ROS Environment that was being used.
   *
   * @param rosEnvironment
   */
  public synchronized void unsetRosEnvironment(RosEnvironment rosEnvironment) {
    stopUsingProvider();
    this.rosEnvironment = null;
  }

  /**
   * Set the Ros Zeroconf the server should run with.
   *
   * @param rosZeroconf
   */
  public synchronized void setRosZeroconf(RosZeroconf rosZeroconf) {
    this.rosZeroconf = rosZeroconf;
    rosZeroconf.addListener(this);

    // Get all known masters and store.
    for (ZeroconfRosMasterInfo masterInfo : rosZeroconf.getKnownMasters()) {
      onNewRosMaster(masterInfo);
    }

    startUsingProvider();
  }

  /**
   * Remove the RosZeroconf that was being used.
   *
   * @param rosZeroconf
   */
  public synchronized void unsetRosZeroconf(RosZeroconf rosZeroconf) {
    rosZeroconf.removeListener(this);
    this.rosZeroconf = null;

    stopUsingProvider();
  }

  @Override
  public void onNewRosMaster(ZeroconfRosMasterInfo masterInfo) {
    if (rosEnvironment.getNetworkType().equals(masterInfo.getType())) {
      rosEnvironment.getLog().info(String.format("Adding ROS master %s", masterInfo));
      masterSelector.addService(masterInfo);
    }
  }

  @Override
  public void onRemoveRosMaster(ZeroconfRosMasterInfo masterInfo) {
    if (rosEnvironment.getNetworkType().equals(masterInfo.getType())) {
      rosEnvironment.getLog().info(String.format("Removing ROS master %s", masterInfo));
      masterSelector.removeService(masterInfo);
    }
  }

  /**
   * Set the master URI provider to use.
   *
   * @param masterUriProvider
   */
  public synchronized void setMasterUriProvider(SwitchableMasterUriProvider masterUriProvider) {
    this.masterUriProvider = masterUriProvider;
    startUsingProvider();
  }

  /**
   * Remove the master URI provider that was being used.
   *
   * @param masterUriProvider
   *          the provider that was being used
   */
  public synchronized void unsetMasterUriProvider(SwitchableMasterUriProvider masterUriProvider) {
    stopUsingProvider();
    this.masterUriProvider = null;
  }

  /**
   * Start using the zeroconf provider if it is appropriate.
   */
  private void startUsingProvider() {
    // Only switch if we have everything except a replaced provider
    if (started && masterUriProvider != null && rosEnvironment != null && rosZeroconf != null
        && replacedProvider == null) {
      masterUriProvider.switchProvider(new MasterUriProviderSwitcher() {

        @Override
        public MasterUriProvider switchProvider(MasterUriProvider previousProvider) {
          replacedProvider = previousProvider;

          return new ZeroconfMasterUriProvider(masterSelector);
        }
      });
    }
  }

  /**
   * Stop using the zeroconf provider if it is appropriate.
   */
  private void stopUsingProvider() {
    // Only switch if we have everything except a replaced provider
    if (replacedProvider != null) {
      masterUriProvider.switchProvider(new MasterUriProviderSwitcher() {

        @Override
        public MasterUriProvider switchProvider(MasterUriProvider previousProvider) {
          MasterUriProvider toUse = replacedProvider;
          replacedProvider = null;

          return toUse;
        }
      });
    }
  }
}
