/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.master.communication.internal;

import interactivespaces.master.communication.MasterCommunicationManager;
import interactivespaces.master.server.remote.RemoteMasterServerMessages;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * The standard manager for master communications.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterCommunicationManager implements MasterCommunicationManager {

  /**
   * The name given to the master communication server.
   */
  private static final String MASTER_COMMUNICATION_SERVER_NAME = "master";

  /**
   * The web server hosting the web socket connection.
   */
  private WebServer webServer;

  /**
   * The space environment.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public void startup() {
    int port =
        spaceEnvironment.getSystemConfiguration().getPropertyInteger(
            RemoteMasterServerMessages.CONFIGURATION_MASTER_COMMUNICATION_PORT,
            RemoteMasterServerMessages.CONFIGURATION_MASTER_COMMUNICATION_PORT_DEFAULT);

    webServer = new NettyWebServer(spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());
    webServer.setServerName(MASTER_COMMUNICATION_SERVER_NAME);
    webServer.setPort(port);

    webServer.startup();
  }

  @Override
  public void shutdown() {
    if (webServer != null) {
      webServer.shutdown();
      webServer = null;
    }
  }

  @Override
  public WebServer getWebServer() {
    return webServer;
  }

  /**
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
