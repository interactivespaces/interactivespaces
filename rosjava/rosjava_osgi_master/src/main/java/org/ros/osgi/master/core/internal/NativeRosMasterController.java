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

import org.ros.osgi.master.core.RosMasterController;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * A {@link RosMasterController} which uses a native ROS Master.
 *
 * @author Keith M. Hughes
 */
public class NativeRosMasterController extends BaseRosMasterController {

  /**
   * Name of the binary for roscore.
   */
  private static final String ROSCORE_BINARY = "roscore";

  /**
   * The environment variable for the ROS Master URI.
   */
  private static final String ENVIRONMENT_VARIABLE_ROS_MASTER_URI = "ROS_MASTER_URI";

  /**
   * The environment variable if the ROS Master URI is defined with an IP address and not a DNS host name.
   */
  private static final String ENVIRONMENT_VARIABLE_ROS_IP = "ROS_IP";

  /**
   * The environment variable if the ROS Master URI is defined with an DNS host name and not an IP address.
   */
  private static final String ENVIRONMENT_VARIABLE_ROS_HOST = "ROS_HOST";

  /**
   * A simple regex for recognizing an IPv4 IP address.
   */
  private static final String IP_ADDRESS_REGEX = "\\d+\\.\\d+\\.\\d+\\.\\d+";

  /**
   * Process running the master.
   */
  private Process masterProcess;

  @Override
  public void startup() {
    // TODO(keith): If want to keep this, pick up from ROS environment
    // through a getProperty() interface that
    // can hide where config came from.
    String rosCorePath = null; // context.getBundleContext().getProperty("org.ros.core.path");
    try {
      URI masterUri = rosEnvironment.getMasterUri();

      StringBuilder builder = new StringBuilder();
      builder.append(rosCorePath).append(File.separatorChar).append(ROSCORE_BINARY).append(" -p ")
          .append(masterUri.getPort());

      // Need to make sure have all ROS environment variables. make sure
      // proper
      // master URI and host set.
      //
      // Might as well get everything else while at it.
      Map<String, String> env = new HashMap<String, String>(System.getenv());
      env.put(ENVIRONMENT_VARIABLE_ROS_MASTER_URI, masterUri.toString());

      setProperRosHost(env);

      String[] envp = createEnvp(env);

      rosEnvironment.getLog().info(String.format("Starting up ROS Master %s", builder.toString()));

      masterProcess = Runtime.getRuntime().exec(builder.toString(), envp);

      signalRosMasterStartup();
    } catch (Exception e) {
      // TODO(keith): Better exceptions
      rosEnvironment.getLog().error("Could not start up master", e);

      throw new RuntimeException(e);
    }
  }

  @Override
  public void shutdown() {
    rosEnvironment.getLog().info("Shutting down ROS Master");
    masterProcess.destroy();

    masterProcess = null;

    signalRosMasterShutdown();
  }

  /**
   * Create the environment variables fo the ROS Master run.
   *
   * @param processEnvironmentVariables
   *          the map of process environment variables
   *
   * @return an array of strings giving the environment variables
   */
  protected String[] createEnvp(Map<String, String> processEnvironmentVariables) {
    String[] envp = new String[processEnvironmentVariables.size()];

    int i = 0;
    for (Entry<String, String> entry : processEnvironmentVariables.entrySet()) {
      envp[i++] = String.format("%s=%s", entry.getKey(), entry.getValue());
    }

    return envp;
  }

  /**
   * Get the environment variables for the ROS host.
   *
   * @param processEnvironmentVariables
   *          the environment to place the values in
   */
  protected void setProperRosHost(Map<String, String> processEnvironmentVariables) {
    String host = rosEnvironment.getHost();

    // A really dumb regex for an IPv4 address.
    if (Pattern.matches(IP_ADDRESS_REGEX, host)) {
      processEnvironmentVariables.put(ENVIRONMENT_VARIABLE_ROS_IP, host);
    } else {
      processEnvironmentVariables.put(ENVIRONMENT_VARIABLE_ROS_HOST, host);
    }
  }
}
