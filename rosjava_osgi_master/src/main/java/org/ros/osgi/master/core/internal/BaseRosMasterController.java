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

import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.RosMasterController;
import org.ros.osgi.master.core.RosMasterControllerListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Support class that gives some convenience methods for the {@link RosMasterController}.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseRosMasterController implements RosMasterController {

  /**
   * The ROS Environment this ROS Master is to run in.
   */
  protected RosEnvironment rosEnvironment;

  /**
   * All listeners for ROS Master events.
   */
  private List<RosMasterControllerListener> listeners = new CopyOnWriteArrayList<RosMasterControllerListener>();

  /**
   * {@code true} if the ROS Master is started.
   */
  protected boolean started;

  @Override
  public void addListener(RosMasterControllerListener listener) {
    // Will be missing the started message otherwise.
    if (started) {
      listener.onRosMasterStartup();
    }

    listeners.add(listener);
  }

  @Override
  public void removeListener(RosMasterControllerListener listener) {
    listeners.remove(listener);
  }

  /**
   * Tell all listeners the ROS Master has started.
   */
  protected void signalRosMasterStartup() {
    for (RosMasterControllerListener listener : listeners) {
      listener.onRosMasterStartup();
    }
  }

  /**
   * Tell all listeners the ROS Master has shut down.
   */
  protected void signalRosMasterShutdown() {
    for (RosMasterControllerListener listener : listeners) {
      listener.onRosMasterShutdown();
    }
  }

  @Override
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  @Override
  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }
}
