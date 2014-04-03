/*
 * Copyright (C) 2014 Google Inc.
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

import interactivespaces.interaction.model.entity.SimpleTrackedEntity;
import interactivespaces.interaction.model.entity.TrackedEntity;
import interactivespaces.interaction.model.entity.TrackedEntityListener;
import interactivespaces.service.image.depth.UserTrackerDepthCameraEndpoint;
import interactivespaces.service.image.depth.internal.openni2.libraries.NiTE2Library;
import interactivespaces.service.image.depth.internal.openni2.libraries.NiTE2Library.NiteStatus;
import interactivespaces.service.image.depth.internal.openni2.libraries.NiTE2Library.NiteUserTrackerHandle;
import interactivespaces.service.image.depth.internal.openni2.libraries.NitePoint3f;
import interactivespaces.service.image.depth.internal.openni2.libraries.NiteUserData;
import interactivespaces.service.image.depth.internal.openni2.libraries.NiteUserTrackerFrame;
import interactivespaces.service.image.depth.internal.openni2.libraries.OpenNI2Library;
import interactivespaces.service.image.depth.internal.openni2.libraries.OpenNI2Library.OniDeviceHandle;
import interactivespaces.service.image.depth.internal.openni2.libraries.OpenNI2Library.OniStatus;
import interactivespaces.util.geometry.Vector3;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A depth camera user tracking endpoint using OpenNI2 and NiTE2.
 *
 * @author Keith M. Hughes
 */
public class Openni2UserTrackerDepthCameraEndpoint implements UserTrackerDepthCameraEndpoint {

  /**
   * The default value for the amount of time between camera frame updates. In
   * milliseconds.
   */
  public static final int READER_LOOP_RATE_DEFAULT = 100;

  /**
   * The ID for the camera.
   */
  private final String cameraId;

  /**
   * Executor service for the read loop.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The tracked entity listeners.
   */
  private final List<TrackedEntityListener<Vector3>> trackedEntityListeners = Lists.newCopyOnWriteArrayList();

  /**
   * Logger for this endpoint.
   */
  private final Log log;

  /**
   * The depth camera being used for this endpoint.
   */
  private Pointer<OniDeviceHandle> camera;

  /**
   * The NiTE2 user tracker which interprets the point cloud and gets the
   * people.
   */
  private Pointer<NiteUserTrackerHandle> userTracker;

  /**
   * The loop for reading the user tracker frames.
   */
  private ScheduledFuture<?> frameReaderLoop;

  /**
   * Pointers to the tracker frame being returned from the NiTE user tracker.
   */
  private Pointer<Pointer<NiteUserTrackerFrame>> pointerTrackerFrame;

  /**
   * How often the reader loop will update. In milliseconds.
   */
  private int readerLoopRate = READER_LOOP_RATE_DEFAULT;

  /**
   * Is the endpoint sampling the camera?
   */
  private final AtomicBoolean sampling = new AtomicBoolean(false);

  /**
   * Construct a new endpoint.
   *
   * @param cameraId
   *          ID of the camera to use
   * @param executorService
   *          the executor service to use
   * @param log
   *          logger for the endpoint
   */
  public Openni2UserTrackerDepthCameraEndpoint(String cameraId, ScheduledExecutorService executorService, Log log) {
    this.cameraId = cameraId;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void setReaderLoopRate(int readerLoopRate) {
    this.readerLoopRate = readerLoopRate;
  }

  @Override
  public synchronized void startup() {
    camera = Pointer.allocate(OniDeviceHandle.class);
    IntValuedEnum<OniStatus> cameraStatus = OpenNI2Library.oniDeviceOpen(Pointer.pointerToCString(cameraId), camera);
    if (cameraStatus != OniStatus.ONI_STATUS_OK) {
      OpenNi2Support.throwExtendedOpenNIError("Could not allocate OpenNI camera", cameraStatus);
    }

    // TODO(keith): Get this attaching the requested camera, for now gets any
    // camera available
    userTracker = Pointer.allocate(NiteUserTrackerHandle.class);
    IntValuedEnum<NiteStatus> niteStatus = NiTE2Library.niteInitializeUserTracker(userTracker);
    // IntValuedEnum<NiteStatus> niteStatus =
    // NiTE2Library.niteInitializeUserTrackerByDevice(camera, userTracker);
    if (niteStatus != NiteStatus.NITE_STATUS_OK) {
      OpenNi2Support.throwExtendedNiteError("Could not allocate NiTE user tracker", niteStatus);
    }

    pointerTrackerFrame = Pointer.allocatePointer(NiteUserTrackerFrame.class);

    frameReaderLoop = executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        processUserTrackFrame();
      }
    }, 0, readerLoopRate, TimeUnit.MILLISECONDS);

    sampling.set(true);
  }

  @Override
  public synchronized void shutdown() {
    sampling.set(false);

    if (frameReaderLoop != null) {
      frameReaderLoop.cancel(true);
    }

    IntValuedEnum<NiteStatus> niteStatus = NiTE2Library.niteShutdownUserTracker(userTracker.get());
    if (niteStatus != NiteStatus.NITE_STATUS_OK) {
      log.error(OpenNi2Support.getFullNiteMessage("Could not shut down NiTE user tracker", niteStatus));
    }

    IntValuedEnum<OniStatus> openniStatus = OpenNI2Library.oniDeviceClose(camera.get());
    if (openniStatus != OniStatus.ONI_STATUS_OK) {
      log.error(OpenNi2Support.getFullOpenNIMessage("Could not shut down camera, status was %s", openniStatus));
    }

    // TODO(keith): release all allocated memory, make sure no matter what no
    // memory leaks
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
   * A new NiTE User Track frame is available. Process it.
   */
  private void processUserTrackFrame() {
    List<TrackedEntity<Vector3>> entities = Lists.newArrayList();

    synchronized (this) {
      if (!sampling.get()) {
        return;
      }

      IntValuedEnum<NiteStatus> niteStatus =
          NiTE2Library.niteReadUserTrackerFrame(userTracker.get(), pointerTrackerFrame);

      if (niteStatus != NiteStatus.NITE_STATUS_OK) {
        log.error(OpenNi2Support.getFullNiteMessage("Error while reading a user tracking frame", niteStatus));
        return;
      }

      try {
        NiteUserTrackerFrame frame = pointerTrackerFrame.get().get();

        int numUsers = frame.userCount();
        Pointer<NiteUserData> users = frame.pUser();

        for (int user = 0; user < numUsers; user++) {
          NiteUserData userData = users.get(user);

          NitePoint3f com = userData.centerOfMass();

          int state = userData.state();

          SimpleTrackedEntity<Vector3> entity =
              new SimpleTrackedEntity<Vector3>(Integer.toString(userData.id()), new Vector3(com.x(), com.y(), com.z()),
                  (state & NiTE2Library.NiteUserState.NITE_USER_STATE_NEW.value) != 0,
                  (state & NiTE2Library.NiteUserState.NITE_USER_STATE_VISIBLE.value) != 0,
                  (state & NiTE2Library.NiteUserState.NITE_USER_STATE_LOST.value) != 0);
          entities.add(entity);
        }
      } catch (Exception e) {
        log.error("Error during depth camera scan", e);

        return;
      } finally {
        niteStatus = NiTE2Library.niteUserTrackerFrameRelease(userTracker.get(), pointerTrackerFrame.get());
      }
    }

    sendTrackedEntityUpdate(entities);
  }

  /**
   * Send out an update on tracked entities to all registered listeners.
   *
   * @param entities
   *          the entities to send
   */
  private void sendTrackedEntityUpdate(List<TrackedEntity<Vector3>> entities) {
    for (TrackedEntityListener<Vector3> listener : trackedEntityListeners) {
      try {
        listener.onTrackedEntityUpdate(entities);
      } catch (Exception e) {
        log.error("Error while processing a depth camera tracked entity", e);
      }
    }
  }
}
