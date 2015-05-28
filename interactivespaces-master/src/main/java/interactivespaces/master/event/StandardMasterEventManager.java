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

package interactivespaces.master.event;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;

import org.apache.commons.logging.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A helper for messages to {@link MasterEventListener} instances.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterEventManager implements MasterEventManager {

  /**
   * Listeners registered with helper.
   */
  private final List<MasterEventListener> listeners = new CopyOnWriteArrayList<MasterEventListener>();

  /**
   * The logger for this manager.
   */
  private Log log;

  @Override
  public void addListener(MasterEventListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(MasterEventListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void signalSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    for (MasterEventListener listener : listeners) {
      listener.onSpaceControllerConnectAttempted(controller);
    }
  }

  @Override
  public void signalSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    for (MasterEventListener listener : listeners) {
      listener.onSpaceControllerDisconnectAttempted(controller);
    }
  }

  @Override
  public void signalSpaceControllerHeartbeat(ActiveSpaceController controller, long timestamp) {
    for (MasterEventListener listener : listeners) {
      listener.onSpaceControllerHeartbeat(controller, timestamp);
    }
  }

  @Override
  public void signalSpaceControllerStatusChange(ActiveSpaceController controller, SpaceControllerState state) {
    for (MasterEventListener listener : listeners) {
      listener.onSpaceControllerStatusChange(controller, state);
    }
  }

  @Override
  public void signalLiveActivityDeploy(ActiveLiveActivity liveActivity, LiveActivityDeploymentResponse result,
      long timestamp) {
    for (MasterEventListener listener : listeners) {
      listener.onLiveActivityDeploy(liveActivity, result, timestamp);
    }
  }

  @Override
  public void signalLiveActivityDelete(ActiveLiveActivity liveActivity, LiveActivityDeleteResult result, long timestamp) {
    for (MasterEventListener listener : listeners) {
      listener.onLiveActivityDelete(liveActivity, result, timestamp);
    }
  }

  @Override
  public void signalLiveActivityStateChange(ActiveLiveActivity liveActivity, ActivityState oldState, ActivityState newState) {
    for (MasterEventListener listener : listeners) {
      listener.onLiveActivityStateChange(liveActivity, oldState, newState);
    }
  }

  @Override
  public void removeAllListeners() {
    listeners.clear();
  }

  /**
   * Set the logger for this manager.
   *
   * @param log
   *          the logger
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
