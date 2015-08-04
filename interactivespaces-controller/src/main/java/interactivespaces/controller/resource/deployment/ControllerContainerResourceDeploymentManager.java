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

package interactivespaces.controller.resource.deployment;

import interactivespaces.InteractiveSpacesExceptionUtils;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse.ContainerResourceDeploymentCommitStatus;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentItem;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse.QueryResponseStatus;
import interactivespaces.resource.ResourceDependency;
import interactivespaces.resource.io.HttpCopierResourceSource;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.resources.ContainerResource;
import interactivespaces.system.resources.ContainerResourceCollection;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.system.resources.ContainerResourceType;
import interactivespaces.util.web.HttpClientHttpContentCopier;
import interactivespaces.util.web.HttpContentCopier;

import java.util.List;

/**
 * The controller manager for resource deployment.
 *
 * @author Keith M. Hughes
 */
public class ControllerContainerResourceDeploymentManager implements ContainerResourceDeploymentManager {

  /**
   * The container's resource manager.
   */
  private final ContainerResourceManager containerResourceManager;

  /**
   * The content copier.
   */
  private HttpContentCopier contentCopier;

  /**
   * The space environment.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Construct a deployment manager.
   *
   * @param containerResourceManager
   *          the resource manager for the container
   * @param spaceEnvironment
   *          the space environment
   */
  public ControllerContainerResourceDeploymentManager(ContainerResourceManager containerResourceManager,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.containerResourceManager = containerResourceManager;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    contentCopier = new HttpClientHttpContentCopier();
    contentCopier.startup();
  }

  @Override
  public void shutdown() {
    if (contentCopier != null) {
      contentCopier.shutdown();
      contentCopier = null;
    }
  }

  @Override
  public ContainerResourceDeploymentQueryResponse queryResources(
      ContainerResourceDeploymentQueryRequest deploymentQuery) {
    ContainerResourceCollection currentResources = containerResourceManager.getResources();
    if (currentlySatisfiesQuery(deploymentQuery, currentResources)) {
      return new ContainerResourceDeploymentQueryResponse(deploymentQuery.getTransactionId(),
          QueryResponseStatus.SPECIFIC_QUERY_SATISFIED);
    } else {
      ContainerResourceDeploymentQueryResponse response =
          new ContainerResourceDeploymentQueryResponse(deploymentQuery.getTransactionId(),
              QueryResponseStatus.SPECIFIC_QUERY_NOT_SATISFIED);

      List<ContainerResource> allContainerResources = currentResources.getAllResources();
      // TODO(keith): place all resources into the response for dependency
      // calculations on the master.

      return response;
    }
  }

  /**
   * Query the container to see if a series of deployment requests are already satisfied.
   *
   * @param deploymentQuery
   *          the query
   * @param currentResources
   *          the current resources in the container
   *
   * @return {@code true} if already satisfies the query
   */
  private boolean currentlySatisfiesQuery(ContainerResourceDeploymentQueryRequest deploymentQuery,
      ContainerResourceCollection currentResources) {
    for (ResourceDependency query : deploymentQuery.getQueries()) {
      ContainerResource resource = currentResources.getResource(query.getName(), query.getVersionRange());
      if (resource == null) {
        return false;
      }
    }

    return true;
  }

  @Override
  public ContainerResourceDeploymentCommitResponse commitResources(ContainerResourceDeploymentCommitRequest request) {
    boolean success = true;
    String detail = null;
    for (ContainerResourceDeploymentItem item : request.getItems()) {
      try {
        containerResourceManager.addResource(item.asContainerResource(ContainerResourceType.LIBRARY),
            new HttpCopierResourceSource(item.getResourceSourceUri(), contentCopier));
      } catch (Throwable e) {
        success = false;
        detail = InteractiveSpacesExceptionUtils.getExceptionDetail(e);
        spaceEnvironment.getLog().error(
            String.format("Could not install deployment resource %s\n%s", item.getResourceSourceUri(), detail));

        break;
      }
    }

    return new ContainerResourceDeploymentCommitResponse(request.getTransactionId(),
        success ? ContainerResourceDeploymentCommitStatus.SUCCESS : ContainerResourceDeploymentCommitStatus.FAILURE,
        detail);
  }
}
