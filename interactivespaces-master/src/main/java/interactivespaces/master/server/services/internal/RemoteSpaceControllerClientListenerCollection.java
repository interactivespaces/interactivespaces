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

package interactivespaces.master.server.services.internal;

import interactivespaces.activity.ActivityState;
import interactivespaces.control.message.activity.LiveActivityDeleteResponse;
import interactivespaces.control.message.activity.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * A collection of {@link RemoteSpaceControllerClientListener} instances.
 *
 * <p>
 * There will be one per remote controller client.
 *
 * @author Keith M. Hughes
 */
public class RemoteSpaceControllerClientListenerCollection {

  /**
   * Listeners registered with helper.
   */
  private final List<RemoteSpaceControllerClientListener> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * Logger for this helper.
   */
  private final Log log;

  /**
   * Construct a helper.
   *
   * @param log
   *          the logger to use
   */
  public RemoteSpaceControllerClientListenerCollection(Log log) {
    this.log = log;
  }

  /**
   * Add in a new event listener.
   *
   * @param listener
   *          the new listener
   */
  public void addListener(RemoteSpaceControllerClientListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove an event listener.
   *
   * <p>
   * Does nothing if the listener wasn't registered.
   *
   * @param listener
   *          the listener to remove
   */
  public void removeListener(RemoteSpaceControllerClientListener listener) {
    listeners.remove(listener);
  }

  /**
   * Signal a space controller connecting.
   *
   * @param controller
   *          the controller being disconnected from
   */
  public void signalSpaceControllerConnectAttempt(ActiveSpaceController controller) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectAttempted(controller);
      } catch (Throwable e) {
        log.error(String.format("Error handling space controller connect event for %s", controller.getDisplayName()),
            e);
      }
    }
  }

  /**
   * Signal a space controller disconnecting.
   *
   * @param controller
   *          the controller being disconnected from
   */
  public void signalSpaceControllerDisconnectAttempt(ActiveSpaceController controller) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerDisconnectAttempted(controller);
      } catch (Throwable e) {
        log.error(
            String.format("Error handling space controller disconnect event for %s", controller.getDisplayName()), e);
      }
    }
  }

  /**
   * Signal a space controller heartbeat.
   *
   * @param uuid
   *          uuid of the controller
   * @param timestamp
   *          timestamp of the heartbeat
   */
  public void signalSpaceControllerHeartbeat(String uuid, long timestamp) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerHeartbeat(uuid, timestamp);
      } catch (Throwable e) {
        log.error(String.format("Error handling space controller heartbeat event for UUID %s and timestamp %d", uuid,
            timestamp), e);
      }
    }
  }

  /**
   * Signal that the controller status has been updated.
   *
   * @param uuid
   *          the UUID of the space controller
   * @param state
   *          the new state
   */
  public void signalSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerStatusChange(uuid, state);
      } catch (Throwable e) {
        log.error(
            String.format("Error handling space controller status change event for UUID %s and state %s", uuid, state),
            e);
      }
    }
  }

  /**
   * Signal that the space controller is shutting down.
   *
   * @param uuid
   *          the UUID of the space controller
   */
  public void signalSpaceControllerShutdown(String uuid) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerShutdown(uuid);
      } catch (Throwable e) {
        log.error(
            String.format("Error handling space controller shutdown event for UUID %s", uuid), e);
      }
    }
  }

  /**
   * Send the on deployment message to all listeners.
   *
   * @param uuid
   *          UUID of the activity
   * @param result
   *          the result of the deployment
   */
  public void signalActivityDeployStatus(String uuid, LiveActivityDeploymentResponse result) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityDeployment(uuid, result);
      } catch (Throwable e) {
        log.error(String.format("Error handling space controller deployment status event for UUID %s and result %s",
            uuid, result), e);
      }
    }
  }

  /**
   * Send the on deletion message to all listeners.
   *
   * @param uuid
   *          UUID of the activity.
   * @param result
   *          result of the deletion
   */
  public void signalActivityDelete(String uuid, LiveActivityDeleteResponse result) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityDelete(uuid, result);
      } catch (Throwable e) {
        log.error(String.format("Error handling live activity delete event for UUID %s and result %s", uuid, result),
            e);
      }
    }
  }

  /**
   * Send the activity state change message to all listeners.
   *
   * @param uuid
   *          UUID of the activity
   * @param newRuntimeState
   *          runtime status of the remote activity
   * @param newRuntimeStateDetail
   *          detail about the new runtime state, can be {@code null}
   */
  public void signalActivityStateChange(String uuid, ActivityState newRuntimeState, String newRuntimeStateDetail) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityRuntimeStateChange(uuid, newRuntimeState, newRuntimeStateDetail);
      } catch (Throwable e) {
        log.error(String.format(
            "Error handling live activity state change event for UUID %s and new runtime state %s", uuid,
            newRuntimeState), e);
      }
    }
  }

  /**
   * Send the data bundle state change message to all listeners.
   *
   * @param uuid
   *          UUID of the activity
   * @param status
   *          data bundle status
   */
  public void signalDataBundleState(String uuid, DataBundleState status) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onDataBundleStateChange(uuid, status);
      } catch (Throwable e) {
        log.error(
            String.format("Error handling live activity data bundle event for UUID %s and status %s", uuid, status), e);
      }
    }
  }

  /**
   * Clear all listeners from the helper.
   */
  public void clear() {
    listeners.clear();
  }
}
