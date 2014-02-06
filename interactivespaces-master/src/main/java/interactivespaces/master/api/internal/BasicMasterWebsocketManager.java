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

package interactivespaces.master.api.internal;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory.MultipleConnectionWebSocketHandler;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiControllerManager;
import interactivespaces.master.api.MasterApiSpaceManager;
import interactivespaces.master.api.MasterWebsocketManager;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ExtensionManager;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.internal.DataBundleState;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

/**
 * A basic {@link MasterWebsocketManager} implementation.
 *
 * <p>
 * At the moment this only sends activity and controller events to anyone
 * listening.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterWebsocketManager extends BaseMasterApiManager implements MasterWebsocketManager,
    MultipleConnectionWebSocketHandler, RemoteSpaceControllerClientListener {

  /**
   * Status parameter name for the status time in the master websocket
   * connection.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_NAME_STATUS_TIME = "statusTime";

  /**
   * Status parameter name for the status in the master websocket connection.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_NAME_STATUS = "status";

  /**
   * Status parameter name for the detailed status in the master websocket
   * connection.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_NAME_DETAIL = "statusDetail";

  /**
   * Status parameter name for the ID of the entity in the master websocket
   * connection.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_NAME_ID = "id";

  /**
   * Status parameter name for the UUID of the entity in the master websocket
   * connection.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_NAME_UUID = "uuid";

  /**
   * Status parameter name for the type of the entity in the master websocket
   * connection.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_NAME_TYPE = "type";

  /**
   * Status parameter value if the entity type is a live activity.
   */
  public static final String WEBSOCKET_STATUS_PARAMETER_VALUE_TYPE_LIVE_ACTIVITY = "liveactivity";

  /**
   * The prefix for using an extension.
   */
  public static final String WEBSOCKET_COMMAND_EXTENSION_PREFIX = "/extension/";

  /**
   * The space environment.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The web server hosting the web socket connection.
   */
  private WebServer webServer;

  /**
   * Web socket handler for the connection to the browser.
   */
  private MultipleConnectionWebServerWebSocketHandlerFactory webSocketFactory;

  /**
   * Client for communication with a remote controller.
   */
  private RemoteControllerClient remoteControllerClient;

  /**
   * The manager for extensions.
   */
  private ExtensionManager extensionManager;

  /**
   * Repository for all activity entities..
   */
  private ActivityRepository activityRepository;

  /**
   * The Master API manager for activities.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * The Master API manager for spaces.
   */
  private MasterApiSpaceManager masterApiSpaceManager;

  /**
   * The Master API manager for controllers.
   */
  private MasterApiControllerManager masterApiControllerManager;

  @Override
  public void startup() {
    int port =
        spaceEnvironment.getSystemConfiguration().getPropertyInteger(
            MasterWebsocketManager.CONFIGURATION_MASTER_WEBSOCKET_PORT,
            MasterWebsocketManager.CONFIGURATION_MASTER_WEBSOCKET_PORT_DEFAULT);

    webServer = new NettyWebServer("master", port, spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());

    webSocketFactory = new MultipleConnectionWebServerWebSocketHandlerFactory(this, spaceEnvironment.getLog());

    webServer.setWebSocketHandlerFactory("", webSocketFactory);

    webServer.startup();

    remoteControllerClient.addRemoteSpaceControllerClientListener(this);
  }

  @Override
  public void shutdown() {
    remoteControllerClient.removeRemoteSpaceControllerClientListener(this);

    if (webServer != null) {
      webServer.shutdown();
      webServer = null;
    }
  }

  @Override
  public void onLiveActivityDeployment(String uuid, LiveActivityDeploymentResponse result) {
    // Don't care
  }

  @Override
  public void onLiveActivityDelete(String uuid, LiveActivityDeleteResult result) {
    // Don't care
  }

  @Override
  public void onLiveActivityStateChange(String uuid, ActivityState state, String detail) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      Map<String, Object> data = Maps.newHashMap();

      data.put(WEBSOCKET_STATUS_PARAMETER_NAME_TYPE, WEBSOCKET_STATUS_PARAMETER_VALUE_TYPE_LIVE_ACTIVITY);
      data.put(WEBSOCKET_STATUS_PARAMETER_NAME_UUID, uuid);
      data.put(WEBSOCKET_STATUS_PARAMETER_NAME_ID, liveActivity.getId());
      data.put(WEBSOCKET_STATUS_PARAMETER_NAME_STATUS, state.getDescription());
      data.put(WEBSOCKET_STATUS_PARAMETER_NAME_DETAIL, detail);

      data.put(WEBSOCKET_STATUS_PARAMETER_NAME_STATUS_TIME, new Date(spaceEnvironment.getTimeProvider()
          .getCurrentTime()));

      webSocketFactory.sendJson(data);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Recived status update in web socket master client for unknown live activity UUID %s", uuid));
    }
  }

  @Override
  public void onDataBundleStateChange(String uuid, DataBundleState state) {
    // For now don't care.
  }

  @Override
  public void onSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    // For now don't care.
  }

  @Override
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    // For now don't care.
  }

  @Override
  public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
    // For now don't care.
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState status) {
    // For now don't care.
  }

  @Override
  public void handleNewWebSocketConnection(String connectionId) {
    spaceEnvironment.getLog().info(String.format("New web socket connection %s", connectionId));
  }

  @Override
  public void handleWebSocketClose(String connectionId) {
    spaceEnvironment.getLog().info(String.format("Closed web socket connection %s", connectionId));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handleWebSocketReceive(String connectionId, Object data) {
    spaceEnvironment.getLog().info(
        String.format("Data from web socket connection %s. Fool thinks we're listening", connectionId));

    Map<String, Object> callerArgs = (Map<String, Object>) data;
    Map<String, Object> calleeArgs = (Map<String, Object>) callerArgs.get("args");

    String command = (String) callerArgs.get("command");
    if (command.startsWith(WEBSOCKET_COMMAND_EXTENSION_PREFIX)) {
      String extensionName = command.substring(WEBSOCKET_COMMAND_EXTENSION_PREFIX.length());
      Map<String, Object> result = extensionManager.evaluateApiExtension(extensionName, calleeArgs);
      result.put("command", command);

      webSocketFactory.sendJson(connectionId, result);
    } else if (COMMAND_LIVE_ACTIVITY_VIEW.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityView(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_DEPLOY.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityDeploy(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_CONFIGURE.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityConfigure(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityGetConfiguration(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivitySetConfiguration(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_METADATA_SET.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivitySetMetadata(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_STARTUP.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityStartup(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_ACTIVATE.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityActivate(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_DEACTIVATE.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityDeactivate(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_SHUTDOWN.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityShutdown(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_STATUS.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityStatus(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_DELETE_LOCAL.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityDelete(calleeArgs));
    } else if (COMMAND_LIVE_ACTIVITY_DELETE_REMOTE.equals(command)) {
      webSocketFactory.sendJson(connectionId, liveActivityRemoteDelete(calleeArgs));
    } else {
      // For now nothing else.
    }
  }

  /**
   * Get all data on a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityView(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiActivityManager.getLiveActivityView(id);
  }

  /**
   * Deploy a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityDeploy(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.deployLiveActivity(id);
  }

  /**
   * Configure a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityConfigure(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.configureLiveActivity(id);
  }

  /**
   * Get the configuration for a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityGetConfiguration(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiActivityManager.getLiveActivityConfiguration(id);
  }

  /**
   * Set the configuration for a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivitySetConfiguration(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    Map<String, String> config = getRequiredMapArg(args, "config");
    return masterApiActivityManager.configureLiveActivity(id, config);
  }

  /**
   * Set the metadata for a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivitySetMetadata(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    Map<String, Object> metadata = getRequiredMapArg(args, "metadata");

    return masterApiActivityManager.updateLiveActivityMetadata(id, metadata);
  }

  /**
   * Startup a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityStartup(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.startupLiveActivity(id);
  }

  /**
   * Activate a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityActivate(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.activateLiveActivity(id);
  }

  /**
   * Deactivate a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityDeactivate(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.deactivateLiveActivity(id);
  }

  /**
   * Shutdown a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityShutdown(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.shutdownLiveActivity(id);
  }

  /**
   * Request the status of a live activity.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityStatus(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.statusLiveActivity(id);
  }

  /**
   * Delete a live activity from the master model.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityDelete(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiActivityManager.deleteLiveActivity(id);
  }

  /**
   * Dlete a live activity on its space controller.
   *
   * @param args
   *          args from the websocket call
   *
   * @return call response for the request
   */
  private Map<String, Object> liveActivityRemoteDelete(Map<String, Object> args) {
    String id = getRequiredStringArg(args, "id");
    return masterApiControllerManager.deleteLiveActivity(id);
  }

  /**
   * Get a required string argument from the args map.
   *
   * @param args
   *          the args map
   * @param argName
   *          the argument
   *
   * @return the value of the arg
   *
   * @throws SimpleInteractiveSpacesException
   *           if there is no value for the requested arg
   */
  private String getRequiredStringArg(Map<String, Object> args, String argName) throws SimpleInteractiveSpacesException {
    String value = (String) args.get(argName);
    if (value != null) {
      return value;
    } else {
      throw new SimpleInteractiveSpacesException("Unknown argument " + argName);
    }
  }

  /**
   * Get a required string argument from the args map.
   *
   * @param args
   *          the args map
   * @param argName
   *          the argument
   * @param <K>
   *          type for keys in the map
   * @param <V>
   *          type for values in the map
   *
   * @return the value of the arg
   *
   * @throws SimpleInteractiveSpacesException
   *           if there is no value for the requested arg
   */
  private <K, V> Map<K, V> getRequiredMapArg(Map<String, Object> args, String argName)
      throws SimpleInteractiveSpacesException {
    Object value = args.get(argName);
    if (value != null) {
      if (Map.class.isAssignableFrom(value.getClass())) {
        @SuppressWarnings("unchecked")
        Map<K, V> value2 = (Map<K, V>) value;
        return value2;
      } else {
        throw new SimpleInteractiveSpacesException("Argument not map " + argName);
      }
    } else {
      throw new SimpleInteractiveSpacesException("Unknown argument " + argName);
    }
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * @param extensionManager
   *          the extensionManager to set
   */
  public void setExtensionManager(ExtensionManager extensionManager) {
    this.extensionManager = extensionManager;
  }

  /**
   * @param remoteControllerClient
   *          the remoteControllerClient to set
   */
  public void setRemoteControllerClient(RemoteControllerClient remoteControllerClient) {
    this.remoteControllerClient = remoteControllerClient;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param masterApiActivityManager
   *          the uiActivityManager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * @param masterApiSpaceManager
   *          the uiSpaceManager to set
   */
  public void setMasterApiSpaceManager(MasterApiSpaceManager masterApiSpaceManager) {
    this.masterApiSpaceManager = masterApiSpaceManager;
  }

  /**
   * @param masterApiControllerManager
   *          the uiControllerManager to set
   */
  public void setMasterApiControllerManager(MasterApiControllerManager masterApiControllerManager) {
    this.masterApiControllerManager = masterApiControllerManager;
  }
}
