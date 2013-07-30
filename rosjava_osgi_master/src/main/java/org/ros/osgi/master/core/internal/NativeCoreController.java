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

import org.ros.osgi.master.core.CoreController;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * A {@link CoreController} which uses a native ROS Master, e.g. the C++ one.
 *
 * @author Keith M. Hughes
 */
public class NativeCoreController extends BaseCoreController {

  /**
   * Name of the binary for roscore.
   */
  private static final String ROSCORE_BINARY = "roscore";

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
      env.put("ROS_MASTER_URI", masterUri.toString());

      setProperRosHost(env);

      String[] envp = createEnvp(env);

      System.out.format("Starting up master %s\n", builder.toString());

      masterProcess = Runtime.getRuntime().exec(builder.toString(), envp);
    } catch (Exception e) {
      // TODO(keith): Better exceptions
      rosEnvironment.getLog().error("Could not start up master", e);

      throw new RuntimeException(e);
    }
  }

  /**
   * @param env
   * @return
   */
  protected String[] createEnvp(Map<String, String> env) {
    String[] envp = new String[env.size()];
    int i = 0;
    for (Entry<String, String> entry : env.entrySet()) {
      envp[i++] = String.format("%s=%s", entry.getKey(), entry.getValue());
    }
    return envp;
  }

  /**
   * @param env
   */
  protected void setProperRosHost(Map<String, String> env) {
    String host = rosEnvironment.getHost();

    // A really dumb regex for an IPv4 address.
    if (Pattern.matches("\\d+\\.\\d+\\.\\d+\\.\\d+", host)) {
      env.put("ROS_IP", host);
    } else {
      env.put("ROS_HOST", host);
    }
  }

  @Override
  public void shutdown() {
    System.out.println("Shutting down master");
    masterProcess.destroy();

    masterProcess = null;
  }
}
