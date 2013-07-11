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
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.File;

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
  private String repositoryUrlPathPrefix = "interactivespaces/repository/activity";

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

    repositoryServer.startup();

    String webappPath = "/" + repositoryUrlPathPrefix;
    repositoryBaseUrl =
        "http://"
            + spaceEnvironment.getSystemConfiguration().getRequiredPropertyString(
                InteractiveSpacesEnvironment.CONFIGURATION_HOSTNAME) + ":"
            + repositoryServer.getPort() + webappPath;

    File repositoryBaseFolder = new File(repositoryStorageManager.getRepositoryBaseLocation());
    repositoryServer.addStaticContentHandler(webappPath, repositoryBaseFolder);

    spaceEnvironment.getLog().info(
        String.format("HTTP Activity repository started with base URL %s", repositoryBaseUrl));
    spaceEnvironment.getLog().info(
        String.format("HTTP Activity repository serving from %s",
            repositoryBaseFolder.getAbsolutePath()));
  }

  @Override
  public void shutdown() {
    repositoryServer.shutdown();
  }

  @Override
  public String getResourceUri(String name, String version) {
    // TODO(keith): Get this from something fancier which we can store resources
    // in, get their meta-data, etc.
    return repositoryBaseUrl + "/"
        + repositoryStorageManager.getRepositoryResourceName(name, version);
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
