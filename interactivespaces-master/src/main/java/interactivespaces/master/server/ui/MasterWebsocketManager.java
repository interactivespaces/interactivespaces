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

package interactivespaces.master.server.ui;

/**
 * A websocket connection to the master.
 *
 * @author Keith M. Hughes
 */
public interface MasterWebsocketManager {

  /**
   * Web socket command for getting view data for a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_VIEW = "/liveactivity/view";

  /**
   * Web socket command for deploying a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_DEPLOY = "/liveactivity/deploy";

  /**
   * Web socket command for configuring a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_CONFIGURE = "/liveactivity/configure";

  /**
   * Web socket command for getting configuration data for a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET =
      "/liveactivity/configuration/get";

  /**
   * Web socket command for setting configuration data for a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET =
      "/liveactivity/configuration/set";

  /**
   * Web socket command for setting metadata for a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_METADATA_SET = "/liveactivity/metadata/set";

  /**
   * Web socket command for starting up a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_STARTUP = "/liveactivity/startup";

  /**
   * Web socket command for activating a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_ACTIVATE = "/liveactivity/activate";

  /**
   * Web socket command for deactivating a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_DEACTIVATE = "/liveactivity/deactivate";

  /**
   * Web socket command for shutting down a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_SHUTDOWN = "/liveactivity/shutdown";

  /**
   * Web socket command for getting the status for a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_STATUS = "/liveactivity/status";

  /**
   * Web socket command for deleting the master's entry for a live activity.
   */
  public static final String COMMAND_LIVE_ACTIVITY_DELETE_LOCAL = "/liveactivity/delete/local";

  /**
   * Web socket command for deleting a live activity from its controller.
   */
  public static final String COMMAND_LIVE_ACTIVITY_DELETE_REMOTE = "/liveactivity/delete/remote";

  /**
   * Configuration property for the websocket port for the master.
   */
  public static final String CONFIGURATION_MASTER_WEBSOCKET_PORT =
      "interactivespaces.master.api.websocket.port";

  /**
   * Default value for configuration property for the websocket port for the
   * master.
   */
  public static final int CONFIGURATION_MASTER_WEBSOCKET_PORT_DEFAULT = 8090;

  /**
   * Start up the manager.
   */
  void startup();

  /**
   * Shut down the manager.
   */
  void shutdown();
}
