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

package interactivespaces.workbench.devinfrastructure;

import interactivespaces.InteractiveSpacesException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.ros.log.RosLogFactory;
import org.ros.osgi.common.SimpleRosEnvironment;
import org.ros.osgi.master.core.internal.RosJavaRosMasterController;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Keith M. Hughes
 */
public class StandaloneDevelopmentInfrastructure implements DevelopmentInfrastructure {

  /**
   * The ROS Master URI.
   */
  private static final String ROS_MASTER_URI = "http://localhost:11311";

  /**
   * The number of threads in the pool for the scheduled executor.
   */
  private static final int NUMBER_THREADS_IN_POOL = 100;

  /**
   * The logger for the infrastructure.
   */
  private Log log;

  /**
   * The executor service to use.
   */
  private ScheduledExecutorService executorService;

  /**
   * The ROS environment.
   */
  private SimpleRosEnvironment rosEnvironment;

  /**
   * The controller for the ROS Master.
   */
  private RosJavaRosMasterController rosMasterController;

  @Override
  public void startup() {
    log = new Jdk14Logger("InteractiveSpaces-Development-Infrastructure");
    try {
      executorService = Executors.newScheduledThreadPool(NUMBER_THREADS_IN_POOL);

      initializeRosEnvironment();
      initializeRosMasterController();

    } catch (Throwable e) {
      throw InteractiveSpacesException.newFormattedException(e,
          "Could not start up the Interactive Spaces Development Infrastructure");
    }
  }

  @Override
  public void shutdown() {
    rosMasterController.shutdown();
    rosEnvironment.shutdown();
    executorService.shutdown();
  }

  /**
   * Initialize the ROS environment.
   *
   * @throws Exception
   *           something bad happened
   */
  private void initializeRosEnvironment() throws Exception {
    rosEnvironment = new SimpleRosEnvironment();
    rosEnvironment.setExecutorService(executorService);
    rosEnvironment.setMaster(true);

    // TODO(keith): Decide if this should come from a config.
    rosEnvironment.setMasterUri(new URI(ROS_MASTER_URI));
    rosEnvironment.setLog(log);
  }

  /**
   * Initialize the ROS Master.
   */
  private void initializeRosMasterController() {
    RosLogFactory.setLog(log);

    rosMasterController = new RosJavaRosMasterController();
    rosMasterController.setRosEnvironment(rosEnvironment);
    rosMasterController.startup();
  }
}
