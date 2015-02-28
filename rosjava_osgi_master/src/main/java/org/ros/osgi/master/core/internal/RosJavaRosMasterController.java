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

package org.ros.osgi.master.core.internal;

import org.ros.RosCore;
import org.ros.node.NodeConfiguration;

import java.net.URI;

/**
 * Bring up a pure Java ROS Master.
 *
 * @author Keith M. Hughes
 */
public class RosJavaRosMasterController extends BaseRosMasterController {

  /**
   * The ROS Master.
   */
  private RosCore rosMaster;

  @Override
  public void startup() {
    rosEnvironment.getLog().info("ROS Master starting up");

    try {
      NodeConfiguration configuration = rosEnvironment.getPublicNodeConfiguration();
      URI masterUri = configuration.getMasterUri();
      rosMaster =
          RosCore.newPublic(masterUri.getHost(), masterUri.getPort(),
              rosEnvironment.getExecutorService());
      rosMaster.start();

      rosMaster.awaitStart();

      signalRosMasterStartup();

      started = true;

    } catch (InterruptedException e) {
      // TODO(keith): Decide what to do about exception.
      rosEnvironment.getLog().error("ROS Master startup interrupted", e);
    } catch (Exception e) {
      rosEnvironment.getLog().error("Could not start up ROS Master", e);
    }
  }

  @Override
  public void shutdown() {
    rosEnvironment.getLog().info("ROS Master shutting down");
    started = false;

    rosMaster.shutdown();
    rosMaster = null;

    signalRosMasterShutdown();
  }
}
