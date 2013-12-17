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

import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ContainerResourceDeploymentManager;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.internal.RemoteControllerClientListenerHelper;
import interactivespaces.resource.NamedVersionedResource;
import interactivespaces.resource.ResourceDependencyReference;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.resource.repository.ResourceRepositoryServer;
import interactivespaces.resource.repository.ResourceRepositoryStorageManager;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.uuid.JavaUuidGenerator;
import interactivespaces.util.uuid.UuidGenerator;

import com.google.common.collect.Maps;

import interactivespaces_msgs.LiveActivityDeleteRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A basic remote activity installer.
 *
 * <p>
 * This manager handles the master end of installing activities to a remote
 * endpoint.
 *
 * @author Keith M. Hughes
 */
public class BasicRemoteActivityDeploymentManager implements RemoteActivityDeploymentManager {

  /**
   * The client for making calls to a remote space controller.
   */
  private RemoteControllerClient remoteControllerClient;

  /**
   * Server for activity repository.
   */
  private ResourceRepositoryServer repositoryServer;

  /**
   * The manager for deploying container resources.
   */
  private ContainerResourceDeploymentManager containerResourceDeploymentManager;

  /**
   * Generator for transaction IDs.
   */
  private final UuidGenerator transactionIdGenerator = new JavaUuidGenerator();

  /**
   * Mapping of transaction IDs to the deployment requests.
   */
  private final Map<String, MasterActivityDeploymentRequest> deploymentRequests = Maps.newConcurrentMap();

  /**
   * The listeners for remote events.
   */
  private RemoteControllerClientListenerHelper remoteControllerClientListeners;

  /**
   * {@code true} if should send dependencies every time.
   */
  private final boolean alwaysSendDependencies = true;

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public void deployLiveActivity(ActiveLiveActivity activeLiveActivity) {

    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    Activity activity = liveActivity.getActivity();

    String transactionId = transactionIdGenerator.newUuid();

    MasterActivityDeploymentRequest request =
        new MasterActivityDeploymentRequest(activeLiveActivity, transactionId, repositoryServer.getResourceUri(
            ResourceRepositoryStorageManager.RESOURCE_CATEGORY_ACTIVITY, activity.getIdentifyingName(),
            Version.parseVersion(activity.getVersion())), spaceEnvironment.getTimeProvider().getCurrentTime());

    List<? extends ActivityDependency> dependencies = activity.getDependencies();
    if (dependencies != null && !dependencies.isEmpty()) {
      ContainerResourceDeploymentQueryRequest query = new ContainerResourceDeploymentQueryRequest(transactionId);
      for (ActivityDependency dependency : dependencies) {
        query
            .addQuery(new ResourceDependencyReference(dependency.getName(), new VersionRange(Version
                .parseVersion(dependency.getMinimumVersion()), Version.parseVersion(dependency.getMaximumVersion()),
                false)));
      }
      request.setResourceDeploymentQuery(query);
    }

    beginDeployment(request);
  }

  @Override
  public void handleLiveDeployResult(LiveActivityDeploymentResponse response) {
    MasterActivityDeploymentRequest request = deploymentRequests.get(response.getTransactionId());

    if (request != null) {
      spaceEnvironment.getLog().info(
          String.format("Got activity deployment status with transaction ID %s", response.getTransactionId()));

      if (request.getStatus() == MasterActivityDeploymentRequestStatus.DEPLOYING_ACTIVITY) {
        finalizeActivityDeployment(request, response);
      } else {
        spaceEnvironment.getLog().info(
            String.format("The activity deployment status with transaction ID %s was in inconsistent state %s",
                response.getTransactionId(), response.getStatus()));
      }
    } else {
      spaceEnvironment.getLog().info(
          String.format("Got activity deployment status with unknown transaction ID %s", response.getTransactionId()));
    }
  }

  /**
   * Finalize the activity deployment.
   *
   * @param request
   *          the deployment request
   * @param response
   *          the response to send
   */
  private void finalizeActivityDeployment(MasterActivityDeploymentRequest request,
      LiveActivityDeploymentResponse response) {
    updateDeploymentStatus(request, MasterActivityDeploymentRequestStatus.DEPLOYMENT_COMPLETE);

    deploymentRequests.remove(response.getTransactionId());

    remoteControllerClientListeners.signalActivityDeployStatus(response.getUuid(), response);
  }

  /**
   * Update the deployment status.
   *
   * @param request
   *          the request being updated
   * @param status
   *          the new status
   */
  private void updateDeploymentStatus(MasterActivityDeploymentRequest request,
      MasterActivityDeploymentRequestStatus status) {
    request.updateStatus(status, spaceEnvironment.getTimeProvider().getCurrentTime());
  }

  @Override
  public boolean handleResourceDeploymentQueryResponse(ContainerResourceDeploymentQueryResponse response) {
    MasterActivityDeploymentRequest request = deploymentRequests.get(response.getTransactionId());

    if (request != null) {
      spaceEnvironment.getLog().info(
          String.format(
              "Got resource deployment query response as part of a live activity deployment with transaction ID %s",
              response.getTransactionId()));

      switch (response.getStatus()) {
        case SPECIFIC_QUERY_SATISFIED:
          deployActivity(request);

          break;
        case SPECIFIC_QUERY_NOT_SATISFIED:
          // TODO(keith): Now try and satisfy the dependencies and commit them.

          break;
        default:
          spaceEnvironment
              .getLog()
              .warn(
                  String
                      .format(
                          "Got resource deployment query response for live activity deployment with transaction ID %s has inconsistent status %s",
                          response.getTransactionId(), response.getStatus()));
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean handleResourceDeploymentCommitResponse(ContainerResourceDeploymentCommitResponse response) {
    MasterActivityDeploymentRequest request = deploymentRequests.get(response.getTransactionId());

    if (request != null) {
      spaceEnvironment.getLog().info(
          String.format(
              "Got resource deployment commit response as part of a live activity deployment with transaction ID %s",
              response.getTransactionId()));

      switch (response.getStatus()) {
        case SUCCESS:
          deployActivity(request);

          break;
        case FAILURE:
          finalizeActivityDeployment(request,
              new LiveActivityDeploymentResponse(request.getTransactionId(), request.getUuid(),
                  ActivityDeployStatus.STATUS_FAILURE_DEPENDENCIES_NOT_SATISFIED, spaceEnvironment.getTimeProvider()
                      .getCurrentTime()));

          break;
        default:
          spaceEnvironment
              .getLog()
              .warn(
                  String
                      .format(
                          "Got resource deployment commit response for live activity deployment with transaction ID %s has inconsistent status %s",
                          response.getTransactionId(), response.getStatus()));
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public void deleteLiveActivity(ActiveLiveActivity activeLiveActivity) {
    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    LiveActivityDeleteRequest request = remoteControllerClient.newLiveActivityDeleteRequest();
    request.setUuid(liveActivity.getUuid());
    request.setIdentifyingName(liveActivity.getActivity().getIdentifyingName());
    request.setVersion(liveActivity.getActivity().getVersion());
    request.setForce(1);

    remoteControllerClient.deleteActivity(activeLiveActivity, request);
  }

  /**
   * Begin the deployment of an activity.
   *
   * @param request
   *          the deployment request
   */
  private void beginDeployment(MasterActivityDeploymentRequest request) {
    deploymentRequests.put(request.getTransactionId(), request);

    ContainerResourceDeploymentQueryRequest query = request.getResourceDeploymentQuery();
    if (query != null) {
      if (shouldSendDependencies(request)) {
        Set<NamedVersionedResource> dependencyResults =
            containerResourceDeploymentManager.satisfyDependencies(query.getQueries());

        updateDeploymentStatus(request, MasterActivityDeploymentRequestStatus.SATISFYING_DEPENDENCIES);
        containerResourceDeploymentManager.commitResources(request.getTransactionId(), request.getLiveActivity()
            .getActiveController(), dependencyResults);
      } else {
        // Has dependencies so must query them.
        updateDeploymentStatus(request, MasterActivityDeploymentRequestStatus.QUERYING_DEPENDENCIES);
        remoteControllerClient.queryResourceDeployment(request.getLiveActivity().getActiveController(), query);
      }
    } else {
      // No dependencies so can start.
      deployActivity(request);
    }
  }

  /**
   * Should the dependencies be sent regardless?
   *
   * @param request
   *          the requested deployment
   *
   * @return {@code true} if the dependencies should be sent regardless
   */
  private boolean shouldSendDependencies(MasterActivityDeploymentRequest request) {
    // TODO(keith): Add something where we look for things like dev qualifiers.
    return alwaysSendDependencies;
  }

  /**
   * Do the actual activity deployment.
   *
   * @param request
   *          the deployment request
   */
  private void deployActivity(MasterActivityDeploymentRequest request) {
    updateDeploymentStatus(request, MasterActivityDeploymentRequestStatus.DEPLOYING_ACTIVITY);
    remoteControllerClient.deployActivity(request.getLiveActivity(), request);
  }

  /**
   * Set the repository server to use.
   *
   * @param repositoryServer
   *          the repository server to use, can be {@code null}
   */
  public void setRepositoryServer(ResourceRepositoryServer repositoryServer) {
    this.repositoryServer = repositoryServer;
  }

  /**
   * Set the remote controller client to use.
   *
   * @param remoteControllerClient
   *          the remote controller client to use
   */
  public void setRemoteControllerClient(RemoteControllerClient remoteControllerClient) {
    this.remoteControllerClient = remoteControllerClient;
    remoteControllerClientListeners = remoteControllerClient.registerRemoteActivityDeploymentManager(this);
  }

  /**
   * Set the container resource deployment manager to use.
   *
   * @param containerResourceDeploymentManager
   *          the container resource deployment manager to use
   */
  public void setContainerResourceDeploymentManager(
      ContainerResourceDeploymentManager containerResourceDeploymentManager) {
    this.containerResourceDeploymentManager = containerResourceDeploymentManager;
  }

  /**
   * Set the space environment to use.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
