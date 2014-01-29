/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.image.depth.internal.openni2;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.interaction.model.entity.SimpleTrackedEntity;
import interactivespaces.interaction.model.entity.TrackedEntity;
import interactivespaces.interaction.model.entity.TrackedEntityListener;
import interactivespaces.service.image.depth.DepthCameraEndpoint;
import interactivespaces.util.InteractiveSpacesUtilities;
import interactivespaces.util.geometry.Vector3;

import com.google.common.collect.Lists;

import com.primesense.nite.Point3D;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;
import org.apache.commons.logging.Log;
import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;

import java.util.List;

/**
 * A depth camera endpoint using OpenNI 2
 *
 * @author Keith M. Hughes
 */
public class Openni2DepthCameraEndpoint implements DepthCameraEndpoint {

  /**
   * The OpenNI/NiTE user tracker.
   */
  private UserTracker tracker;

  /**
   * The tracked entity listeners.
   */
  private final List<TrackedEntityListener<Vector3>> trackedEntityListeners = Lists.newCopyOnWriteArrayList();

  /**
   * Logger for this endpoint.
   */
  private final Log log;

  /**
   * Construct a new endpoint.
   *
   * @param log
   *          logger for the endpoint
   */
  public Openni2DepthCameraEndpoint(Log log) {
    this.log = log;
  }

  @Override
  public void startup() {
    List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
    if (devicesInfo.isEmpty()) {
      throw new SimpleInteractiveSpacesException("No supported depth camera devices connected");
    }

    // TODO(keith): support specifying the camera
    String uri = devicesInfo.get(0).getUri();
    log.info(uri);
    Device device = Device.open(uri);
    InteractiveSpacesUtilities.delay(1000);
    UserTracker.
    tracker = UserTracker.create(device);

//
//    tracker.addNewFrameListener(new NewFrameListener() {
//
//      @Override
//      public void onNewFrame(UserTracker tracker) {
//        processNewFrame(tracker);
//      }
//
//    });
  }

  @Override
  public void shutdown() {
    if (tracker != null) {
      tracker.destroy();
    }
  }

  @Override
  public void addTrackedEntityListener(TrackedEntityListener<Vector3> listener) {
    trackedEntityListeners.add(listener);
  }

  @Override
  public void removeTrackedEntityListener(TrackedEntityListener<Vector3> listener) {
    trackedEntityListeners.remove(listener);
  }

  /**
   * Process a new frame from a user tracker.
   *
   * @param tracker
   *          the tracker that received the frame
   */
  private void processNewFrame(UserTracker tracker) {
    UserTrackerFrameRef frame = tracker.readFrame();
    try {
      List<TrackedEntity<Vector3>> entities = Lists.newArrayList();
      for (UserData user : frame.getUsers()) {
        Point3D<Float> CoM = user.getCenterOfMass();

        SimpleTrackedEntity<Vector3> entity =
            new SimpleTrackedEntity<Vector3>(Integer.toString(user.getId()), new Vector3(CoM.getX(), CoM.getY(),
                CoM.getZ()), user.isNew(), user.isVisible(), user.isLost());
        entities.add(entity);
      }

      sendNewTrackedEntities(entities);
    } finally {
      frame.release();
    }
  }

  /**
   * Send out the new tracked entities event to all registered listeners.
   *
   * @param entities
   *          the entities to send
   */
  private void sendNewTrackedEntities(List<TrackedEntity<Vector3>> entities) {
    for (TrackedEntityListener<Vector3> listener : trackedEntityListeners) {
      try {
        listener.onNewTrackedEntities(entities);
      } catch (Exception e) {
        log.error("Error while processing a depth camera tracked entity", e);
      }
    }
  }
}
