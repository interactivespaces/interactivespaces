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

package interactivespaces.resource.repository.internal;

import interactivespaces.resource.repository.ResourceRepositoryServer;
import interactivespaces.resource.repository.ResourceRepositoryStorageManager;
import interactivespaces.service.web.HttpResponseCode;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.Files;

import java.io.IOException;
import java.io.InputStream;

/**
 * An Interactive Spaces resource repository server using HTTP.
 *
 * @author Keith M. Hughes
 */
public class HttpResourceRepositoryServer implements ResourceRepositoryServer {

  /**
   * Default port for the HTTP server which serves activities during deployment.
   */
  public static final String CONFIGURATION_PROPERTY_ACTIVITY_RESPOSITORY_SERVER_PORT =
      "interactivespaces.repository.activities.server.port";

  /**
   * Default port for the HTTP server which serves activities during deployment.
   */
  public static final int ACTIVITY_RESPOSITORY_SERVER_PORT_DEFAULT = 10000;

  /**
   * The internal name given to the web server being used for the activity
   * repository.
   */
  private static final String ACTIVITY_REPOSITORY_SERVER_NAME =
      "interactivespaces_activity_repository";

  /**
   * Webserver for the activity repository.
   */
  private WebServer repositoryServer;

  /**
   * Port the repository server listens on.
   */
  private int repositoryPort;

  /**
   * Base URL of the repository.
   */
  private String repositoryBaseUrl;

  /**
   * Path prefix for the repository URL.
   */
  private String repositoryUrlPathPrefix = "interactivespaces/resource/artifact";

  /**
   * The Interactive Spaces environment.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Storage manager for the activity repository.
   */
  private ResourceRepositoryStorageManager repositoryStorageManager;

  @Override
  public void startup() {
    repositoryPort =
        spaceEnvironment.getSystemConfiguration().getPropertyInteger(
            CONFIGURATION_PROPERTY_ACTIVITY_RESPOSITORY_SERVER_PORT,
            ACTIVITY_RESPOSITORY_SERVER_PORT_DEFAULT);
    repositoryServer =
        new NettyWebServer(ACTIVITY_REPOSITORY_SERVER_NAME, repositoryPort,
            spaceEnvironment.getExecutorService(), spaceEnvironment.getExecutorService(),
            spaceEnvironment.getLog());

    String webappPath = "/" + repositoryUrlPathPrefix;

    repositoryServer.addDynamicContentHandler(webappPath, true, new HttpDynamicRequestHandler() {
      @Override
      public void handle(HttpRequest request, HttpResponse response) {
        handleResourceRequest(request, response);
      }
    });

    repositoryServer.startup();

    repositoryBaseUrl =
        "http://"
            + spaceEnvironment.getSystemConfiguration().getRequiredPropertyString(
                InteractiveSpacesEnvironment.CONFIGURATION_HOSTNAME) + ":"
            + repositoryServer.getPort() + webappPath;

    spaceEnvironment.getLog().info(
        String.format("HTTP Resource Repository started with base URL %s", repositoryBaseUrl));
  }

  @Override
  public void shutdown() {
    repositoryServer.shutdown();
  }

  @Override
  public String getResourceUri(String category, String name, String version) {
    // TODO(keith): Get this from something fancier which we can store resources
    // in, get their meta-data, etc.
    return repositoryBaseUrl + "/" + category + "/" + name + "/" + version;
  }

  /**
   * A request has come in for a resource.
   *
   * @param request
   *          the http request
   * @param response
   *          the response
   */
  private void handleResourceRequest(HttpRequest request, HttpResponse response) {
    spaceEnvironment.getLog().info(
        String.format("Got resource repository request %s", request.getUri()));

    String[] pathComponents = request.getUri().getPath().split("\\/");
    String category = pathComponents[pathComponents.length - 3];
    String name = pathComponents[pathComponents.length - 2];
    String version = pathComponents[pathComponents.length - 1];

    spaceEnvironment.getLog().info(
        String.format("Got resource repository request for resource %s:%s of category %s", name,
            version, category));

    InputStream resourceStream =
        repositoryStorageManager.getResourceStream(category, name, version);
    if (resourceStream != null) {
      response.setResponseCode(HttpResponseCode.OK);
      try {
        Files.copyInputStream(resourceStream, response.getOutputStream());
      } catch (IOException e) {
        spaceEnvironment.getLog().error(
            String.format("Error while writing resource %s:%s of category %s", name, version,
                category));
      }
    } else {
      spaceEnvironment.getLog().warn(
          String.format("No such resource %s:%s of category %s", name, version, category));
      response.setResponseCode(HttpResponseCode.NOT_FOUND);
    }
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * @param repositoryStorageManager
   *          the repositoryStorageManager to set
   */
  public void
      setRepositoryStorageManager(ResourceRepositoryStorageManager repositoryStorageManager) {
    this.repositoryStorageManager = repositoryStorageManager;
  }
}
