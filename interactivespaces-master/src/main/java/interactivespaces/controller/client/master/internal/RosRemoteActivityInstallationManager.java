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

package interactivespaces.controller.client.master.internal;

import interactivespaces.controller.client.master.RemoteActivityInstallationManager;
import interactivespaces.controller.client.master.RemoteActivityInstallationManagerListener;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.resource.repository.ResourceRepositoryServer;

import interactivespaces_msgs.InteractiveSpacesContainerResource;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeployRequest;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A ROS-based Interactive Spaces activity installer.
 *
 * @author Keith M. Hughes
 */
public class RosRemoteActivityInstallationManager implements RemoteActivityInstallationManager {

  /**
   * The client for making calls to a remote space controller.
   */
  private RemoteControllerClient remoteControllerClient;

  /**
   * Server for activity repository.
   */
  private ResourceRepositoryServer repositoryServer;

  /**
   * All listeners for installer events.
   */
  private List<RemoteActivityInstallationManagerListener> listeners =
      new CopyOnWriteArrayList<RemoteActivityInstallationManagerListener>();

  /**
   * Logger for this installer.
   */
  private Log log;

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public void deployActivity(ActiveLiveActivity activeLiveActivity) {
    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    Activity activity = liveActivity.getActivity();

    LiveActivityDeployRequest request = remoteControllerClient.newLiveActivityDeployRequest();
    request.setUuid(liveActivity.getUuid());
    request.setIdentifyingName(activity.getIdentifyingName());
    request.setVersion(activity.getVersion());
    request.setActivitySourceUri(repositoryServer.getResourceUri(activity.getIdentifyingName(),
        activity.getVersion()));

    List<? extends ActivityDependency> dependencies = activity.getDependencies();
    if (dependencies != null) {
      for (ActivityDependency dependency : dependencies) {
        InteractiveSpacesContainerResource resource =
            remoteControllerClient.newInteractiveSpacesContainerResource();
        resource.setName(dependency.getName());
        resource.setMinimumVersion(dependency.getMinimumVersion());
        resource.setMaximumVersion(dependency.getMaximumVersion());
        resource.setRequired(dependency.isRequired());
        resource.setLocation(InteractiveSpacesContainerResource.LOCATION_BOOTSTRAP);
      }
    }

    remoteControllerClient.deployActivity(activeLiveActivity, request);
  }

  @Override
  public void deleteActivity(ActiveLiveActivity activeLiveActivity) {
    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    LiveActivityDeleteRequest request = remoteControllerClient.newLiveActivityDeleteRequest();
    request.setUuid(liveActivity.getUuid());
    request.setIdentifyingName(liveActivity.getActivity().getIdentifyingName());
    request.setVersion(liveActivity.getActivity().getVersion());
    request.setForce(1);

    remoteControllerClient.deleteActivity(activeLiveActivity, request);
  }

  @Override
  public void addListener(RemoteActivityInstallationManagerListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(RemoteActivityInstallationManagerListener listener) {
    listeners.remove(listener);
  }

  /**
   * @param repositoryServer
   *          The repository server to use. Can be null.
   */
  public void setRepositoryServer(ResourceRepositoryServer repositoryServer) {
    this.repositoryServer = repositoryServer;
  }

  /**
   * @param remoteControllerClient
   *          the remoteControllerClient to set
   */
  public void setRemoteControllerClient(RemoteControllerClient remoteControllerClient) {
    this.remoteControllerClient = remoteControllerClient;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
