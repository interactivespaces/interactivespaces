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

package interactivespaces.activity.deployment.ros;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.deployment.LiveActivityDeploymentRequest;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse.ActivityDeployStatus;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse.ContainerResourceDeploymentCommitStatus;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentItem;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse.QueryResponseStatus;
import interactivespaces.resource.ResourceDependency;
import interactivespaces.resource.ResourceDependencyReference;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.system.resources.ContainerResourceLocation;

import com.google.common.collect.Lists;

import interactivespaces_msgs.ContainerResourceCommitRequestMessage;
import interactivespaces_msgs.ContainerResourceCommitResponseMessage;
import interactivespaces_msgs.ContainerResourceQueryItem;
import interactivespaces_msgs.ContainerResourceQueryRequestMessage;
import interactivespaces_msgs.ContainerResourceQueryResponseMessage;
import interactivespaces_msgs.LiveActivityDeployRequest;
import interactivespaces_msgs.LiveActivityDeployStatus;
import interactivespaces_msgs.LocatableResourceDescription;
import org.ros.message.MessageFactory;

import java.util.List;

/**
 * A translator from internal messages to and from ROS messages.
 *
 * @author Keith M. Hughes
 */
public class RosDeploymentMessageTranslator {

  /**
   * The value that the detail should have in the ROS message for container resource deployment commit responses if
   * there is no detail.
   */
  public static final String CONTAINER_RESOURCE_DEPLOYMENT_COMMIT_RESPONSE_DETAIL_NONE = "";

  /**
   * The value that the detail should have in the ROS message for container live activity deployment responses if there
   * is no detail.
   */
  public static final String CONTAINER_LIVE_ACTIVITY_DEPLOYMENT_RESPONSE_DETAIL_NONE = "";

  /**
   * Serialize an activity deployment request into a ROS message.
   *
   * @param request
   *          the deployment request
   * @param rosRequest
   *          the ROS message
   */
  public static void serializeActivityDeploymentRequest(LiveActivityDeploymentRequest request,
      LiveActivityDeployRequest rosRequest) {
    rosRequest.setTransactionId(request.getTransactionId());
    rosRequest.setUuid(request.getUuid());
    rosRequest.setIdentifyingName(request.getIdentifyingName());
    rosRequest.setVersion(request.getVersion().toString());
    rosRequest.setActivitySourceUri(request.getActivitySourceUri());
  }

  /**
   * Deserialize an activity deployment request from a ROS message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the deserialized message
   */
  public static LiveActivityDeploymentRequest
      deserializeActivityDeploymentRequest(LiveActivityDeployRequest rosMessage) {
    return new LiveActivityDeploymentRequest(rosMessage.getTransactionId(), rosMessage.getUuid(),
        rosMessage.getIdentifyingName(), Version.parseVersion(rosMessage.getVersion()),
        rosMessage.getActivitySourceUri());
  }

  /**
   * Serialize the activity deployment status into the corresponding ROS message.
   *
   * @param deployStatus
   *          the deployment status
   * @param rosMessage
   *          the ROS message
   */
  public static void serializeDeploymentStatus(LiveActivityDeploymentResponse deployStatus,
      LiveActivityDeployStatus rosMessage) {
    rosMessage.setTransactionId(deployStatus.getTransactionId());
    rosMessage.setUuid(deployStatus.getUuid());
    rosMessage.setTimeDeployed(deployStatus.getTimeDeployed());

    String detail = deployStatus.getStatusDetail();
    if (detail == null) {
      detail = CONTAINER_LIVE_ACTIVITY_DEPLOYMENT_RESPONSE_DETAIL_NONE;
    }
    rosMessage.setStatusDetail(detail);

    switch (deployStatus.getStatus()) {
      case STATUS_SUCCESS:
        rosMessage.setStatus(LiveActivityDeployStatus.STATUS_SUCCESS);
        break;

      case STATUS_FAILURE_COPY:
        rosMessage.setStatus(LiveActivityDeployStatus.STATUS_FAILURE_COPY);
        break;

      case STATUS_FAILURE_UNPACK:
        rosMessage.setStatus(LiveActivityDeployStatus.STATUS_FAILURE_UNPACK);
        break;
      default:
        throw new SimpleInteractiveSpacesException(String.format("Unhandled status %s for status %s", deployStatus
            .getStatus().getClass().getName()));
    }
  }

  /**
   * Serialize the activity deployment status into the corresponding ROS message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the deployment response
   */
  public static LiveActivityDeploymentResponse deserializeDeploymentStatus(LiveActivityDeployStatus rosMessage) {
    ActivityDeployStatus status;
    switch (rosMessage.getStatus()) {
      case LiveActivityDeployStatus.STATUS_SUCCESS:
        status = ActivityDeployStatus.STATUS_SUCCESS;
        break;

      case LiveActivityDeployStatus.STATUS_FAILURE_COPY:
        status = ActivityDeployStatus.STATUS_FAILURE_COPY;
        break;

      case LiveActivityDeployStatus.STATUS_FAILURE_UNPACK:
        status = ActivityDeployStatus.STATUS_FAILURE_UNPACK;
        break;
      default:
        throw new SimpleInteractiveSpacesException(String.format("Unknown activity deployment status code %d",
            rosMessage.getStatus()));
    }

    String detail = rosMessage.getStatusDetail();
    if (CONTAINER_LIVE_ACTIVITY_DEPLOYMENT_RESPONSE_DETAIL_NONE.equals(detail)) {
      detail = null;
    }

    return new LiveActivityDeploymentResponse(rosMessage.getTransactionId(), rosMessage.getUuid(), status, detail,
        rosMessage.getTimeDeployed());
  }

  /**
   * Serialize a resource deployment query.
   *
   * @param query
   *          the query
   * @param rosMessage
   *          the ROS message
   * @param messageFactory
   *          the ROS message factory to use for building components
   */
  public static void serializeResourceDeploymentQuery(ContainerResourceDeploymentQueryRequest query,
      ContainerResourceQueryRequestMessage rosMessage, MessageFactory messageFactory) {
    rosMessage.setType(ContainerResourceQueryRequestMessage.TYPE_SPECIFIC_QUERY);
    rosMessage.setTransactionId(query.getTransactionId());

    List<ContainerResourceQueryItem> rosItems = Lists.newArrayList();
    for (ResourceDependency dependency : query.getQueries()) {
      ContainerResourceQueryItem rosItem = messageFactory.newFromType(ContainerResourceQueryItem._TYPE);
      rosItem.setName(dependency.getName());
      rosItem.setVersionRange(dependency.getVersionRange().toString());

      rosItems.add(rosItem);
    }
    rosMessage.setItems(rosItems);
  }

  /**
   * Deserialize a resource deployment query from its ROS message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the query
   */
  public static ContainerResourceDeploymentQueryRequest deserializeContainerResourceDeploymentQuery(
      ContainerResourceQueryRequestMessage rosMessage) {
    ContainerResourceDeploymentQueryRequest query =
        new ContainerResourceDeploymentQueryRequest(rosMessage.getTransactionId());
    for (ContainerResourceQueryItem rosItem : rosMessage.getItems()) {
      query.addQuery(new ResourceDependencyReference(rosItem.getName(), VersionRange.parseVersionRange(rosItem
          .getVersionRange())));
    }
    return query;
  }

  /**
   * Serialize a resource deployment query response message.
   *
   * @param response
   *          the deployment response
   * @param rosMessage
   *          the ROS message
   */
  public static void serializeResourceDeploymentQueryResponse(ContainerResourceDeploymentQueryResponse response,
      ContainerResourceQueryResponseMessage rosMessage) {
    rosMessage.setTransactionId(response.getTransactionId());

    int status;
    switch (response.getStatus()) {
      case SPECIFIC_QUERY_SATISFIED:
        status = ContainerResourceQueryResponseMessage.STATUS_SPECIFIC_QUERY_SATISFIED;
        break;
      case SPECIFIC_QUERY_NOT_SATISFIED:
        status = ContainerResourceQueryResponseMessage.STATUS_SPECIFIC_QUERY_NOT_SATISFIED;
        break;
      case GENERAL_QUERY_RESPONSE:
        status = ContainerResourceQueryResponseMessage.STATUS_GENERAL_QUERY_RESPONSE;
        break;
      default:
        throw new SimpleInteractiveSpacesException(String.format("Unhandled status %s for %s", response.getStatus(),
            response.getStatus().getClass().getName()));
    }

    rosMessage.setStatus(status);
  }

  /**
   * Deserialize a resource deployment response message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the response
   */
  public static ContainerResourceDeploymentQueryResponse deserializeResourceDeploymentQueryResponse(
      ContainerResourceQueryResponseMessage rosMessage) {
    QueryResponseStatus status;
    switch (rosMessage.getStatus()) {
      case ContainerResourceQueryResponseMessage.STATUS_SPECIFIC_QUERY_SATISFIED:
        status = QueryResponseStatus.SPECIFIC_QUERY_SATISFIED;
        break;
      case ContainerResourceQueryResponseMessage.STATUS_SPECIFIC_QUERY_NOT_SATISFIED:
        status = QueryResponseStatus.SPECIFIC_QUERY_NOT_SATISFIED;
        break;
      case ContainerResourceQueryResponseMessage.STATUS_GENERAL_QUERY_RESPONSE:
        status = QueryResponseStatus.GENERAL_QUERY_RESPONSE;
        break;
      default:
        throw new SimpleInteractiveSpacesException(String.format("Unknown status code %d for %s",
            rosMessage.getStatus(), rosMessage.getClass().getName()));
    }

    ContainerResourceDeploymentQueryResponse response =
        new ContainerResourceDeploymentQueryResponse(rosMessage.getTransactionId(), status);

    return response;
  }

  /**
   * Serialize a resource deployment commit.
   *
   * @param request
   *          the request
   * @param rosMessage
   *          the ROS message
   * @param messageFactory
   *          the ROS message factory to use for building components
   */
  public static void serializeResourceDeploymentCommit(ContainerResourceDeploymentCommitRequest request,
      ContainerResourceCommitRequestMessage rosMessage, MessageFactory messageFactory) {
    rosMessage.setTransactionId(request.getTransactionId());

    List<interactivespaces_msgs.ContainerResourceDeploymentItem> rosItems = Lists.newArrayList();
    for (ContainerResourceDeploymentItem item : request.getItems()) {
      interactivespaces_msgs.ContainerResourceDeploymentItem rosItem =
          messageFactory.newFromType(interactivespaces_msgs.ContainerResourceDeploymentItem._TYPE);
      LocatableResourceDescription rosResource = rosItem.getResource();
      rosResource.setName(item.getName());
      rosResource.setVersion(item.getVersion().toString());
      rosResource.setSignature(item.getSignature());
      rosResource.setLocationUri(item.getResourceSourceUri());

      // TODO(keith): Translate this properly from the request.
      rosItem.getLocation().setMainLocation(interactivespaces_msgs.ContainerResourceLocation.LOCATION_USER_BOOTSTRAP);

      rosItems.add(rosItem);
    }
    rosMessage.setItems(rosItems);
  }

  /**
   * Serialize a resource deployment commit.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the deserialized request
   */
  public static ContainerResourceDeploymentCommitRequest deserializeResourceDeploymentCommit(
      ContainerResourceCommitRequestMessage rosMessage) {
    ContainerResourceDeploymentCommitRequest request =
        new ContainerResourceDeploymentCommitRequest(rosMessage.getTransactionId());
    rosMessage.setTransactionId(request.getTransactionId());

    for (interactivespaces_msgs.ContainerResourceDeploymentItem rosItem : rosMessage.getItems()) {
      LocatableResourceDescription resource = rosItem.getResource();
      // TODO(keith): Translate resource locations from the query.
      request
          .addItem(new ContainerResourceDeploymentItem(resource.getName(),
              Version.parseVersion(resource.getVersion()), ContainerResourceLocation.USER_BOOTSTRAP, resource
                  .getSignature(), resource.getLocationUri()));
    }

    return request;
  }

  /**
   * Serialize a resource deployment query response message.
   *
   * @param response
   *          the deployment response
   * @param rosMessage
   *          the ROS message
   */
  public static void serializeResourceDeploymentCommitResponse(ContainerResourceDeploymentCommitResponse response,
      ContainerResourceCommitResponseMessage rosMessage) {
    rosMessage.setTransactionId(response.getTransactionId());

    int status;
    switch (response.getStatus()) {
      case SUCCESS:
        status = ContainerResourceCommitResponseMessage.STATUS_SUCCESS;
        break;
      case FAILURE:
        status = ContainerResourceCommitResponseMessage.STATUS_FAILURE;
        break;
      default:
        throw new SimpleInteractiveSpacesException(String.format("Unhandled status %s for %s", response.getStatus(),
            response.getStatus().getClass().getName()));
    }

    rosMessage.setStatus(status);

    String detail = response.getDetail();
    if (detail == null) {
      detail = CONTAINER_RESOURCE_DEPLOYMENT_COMMIT_RESPONSE_DETAIL_NONE;
    }

    rosMessage.setDetail(detail);
  }

  /**
   * Deserialize a resource deployment commit response message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the response
   */
  public static ContainerResourceDeploymentCommitResponse deserializeResourceDeploymentCommitResponse(
      ContainerResourceCommitResponseMessage rosMessage) {
    ContainerResourceDeploymentCommitStatus status;
    switch (rosMessage.getStatus()) {
      case ContainerResourceCommitResponseMessage.STATUS_SUCCESS:
        status = ContainerResourceDeploymentCommitStatus.SUCCESS;
        break;
      case ContainerResourceCommitResponseMessage.STATUS_FAILURE:
        status = ContainerResourceDeploymentCommitStatus.FAILURE;
        break;
      default:
        throw new SimpleInteractiveSpacesException(String.format("Unknown status code %d for %s",
            rosMessage.getStatus(), rosMessage.getClass().getName()));
    }

    String detail = rosMessage.getDetail();
    if (CONTAINER_RESOURCE_DEPLOYMENT_COMMIT_RESPONSE_DETAIL_NONE.equals(detail)) {
      detail = null;
    }

    ContainerResourceDeploymentCommitResponse response =
        new ContainerResourceDeploymentCommitResponse(rosMessage.getTransactionId(), status, detail);

    return response;
  }
}
