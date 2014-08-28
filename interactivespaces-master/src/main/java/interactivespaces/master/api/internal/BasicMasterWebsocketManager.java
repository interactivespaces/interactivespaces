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
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiAutomationManager;
import interactivespaces.master.api.MasterApiMessage;
import interactivespaces.master.api.MasterApiSpaceControllerManager;
import interactivespaces.master.api.MasterWebsocketManager;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ExtensionManager;
import interactivespaces.master.server.services.RemoteSpaceControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.internal.DataBundleState;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;
import interactivespaces.service.web.server.BasicMultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.service.web.server.MultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.service.web.server.MultipleConnectionWebSocketHandler;
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
  private RemoteSpaceControllerClient remoteSpaceControllerClient;

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
   * The Master API manager for controllers.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * The Master API manager for automation.
   */
  private MasterApiAutomationManager masterApiAutomationManager;

  /**
   * A mapping of command name to the handler for that command.
   */
  private final Map<String, MasterApiWebSocketCommandHandler> commandHandlers = Maps.newHashMap();

  /**
   * Construct a new manager.
   */
  public BasicMasterWebsocketManager() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiActivityManager.getLiveActivityView(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deployLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.configureLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiActivityManager.getLiveActivityConfiguration(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        Map<String, String> config = getRequiredMapArg(commandArgs, "config");
        return masterApiActivityManager.configureLiveActivity(id, config);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        Map<String, Object> metadata = getRequiredMapArg(commandArgs, "metadata");

        return masterApiActivityManager.updateMetadataLiveActivity(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.startupLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.activateLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deactivateLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.shutdownLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.statusLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiActivityManager.deleteLiveActivity(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_REMOTE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deleteLiveActivity(id);
      }
    });

    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiActivityManager.getLiveActivityGroupView(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deployLiveActivityGroup(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.configureLiveActivityGroup(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        Map<String, Object> metadata = getRequiredMapArg(commandArgs, "metadata");

        return masterApiActivityManager.updateMetadataLiveActivityGroup(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.startupLiveActivityGroup(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.activateLiveActivityGroup(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deactivateLiveActivityGroup(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.shutdownLiveActivityGroup(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.statusLiveActivityGroup(id);
      }
    });

    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiActivityManager.getSpaceView(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deploySpace(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.configureSpace(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessage.MASTER_API_COMMAND_SPACE_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        Map<String, Object> metadata = getRequiredMapArg(commandArgs, "metadata");

        return masterApiActivityManager.updateMetadataSpace(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.startupSpace(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.activateSpace(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.deactivateSpace(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.shutdownSpace(id);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_SPACE_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiSpaceControllerManager.statusSpace(id);
      }
    });

    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessage.MASTER_API_COMMAND_NAMEDSCRIPT_RUN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getRequiredStringArg(commandArgs, MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID);
        return masterApiAutomationManager.runScript(id);
      }
    });
  }

  @Override
  public void startup() {
    int port =
        spaceEnvironment.getSystemConfiguration().getPropertyInteger(
            MasterWebsocketManager.CONFIGURATION_MASTER_WEBSOCKET_PORT,
            MasterWebsocketManager.CONFIGURATION_MASTER_WEBSOCKET_PORT_DEFAULT);

    webServer = new NettyWebServer(spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());
    webServer.setServerName("master");
    webServer.setPort(port);

    webSocketFactory = new BasicMultipleConnectionWebServerWebSocketHandlerFactory(this, spaceEnvironment.getLog());

    webServer.setWebSocketHandlerFactory("", webSocketFactory);

    webServer.startup();

    remoteSpaceControllerClient.addRemoteSpaceControllerClientListener(this);
  }

  @Override
  public void shutdown() {
    remoteSpaceControllerClient.removeRemoteSpaceControllerClientListener(this);

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
  public void onLiveActivityRuntimeStateChange(String uuid, ActivityState runtimeState, String detail) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      Map<String, Object> data = Maps.newHashMap();

      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_STATUS_TYPE,
          MasterApiMessage.MASTER_API_PARAMETER_VALUE_TYPE_STATUS_LIVE_ACTIVITY);
      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_UUID, uuid);
      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE, runtimeState.name());
      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE_DESCRIPTION,
          runtimeState.getDescription());
      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_STATUS_DETAIL, detail);

      data.put(MasterApiMessage.MASTER_API_PARAMETER_NAME_STATUS_TIME, new Date(spaceEnvironment.getTimeProvider()
          .getCurrentTime()));

      Map<String, Object> message = Maps.newHashMap();
      message.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_TYPE,
          MasterApiMessage.MASTER_API_MESSAGE_TYPE_STATUS_UPDATE);
      message.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA, data);

      webSocketFactory.sendJson(message);
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
    Map<String, Object> message = (Map<String, Object>) data;

    String command = (String) message.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_TYPE);
    Map<String, Object> commandArgs =
        (Map<String, Object>) message.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);

    String requestId = (String) message.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_REQUEST_ID);

    try {
      if (command.startsWith(MasterApiMessage.MASTER_API_COMMAND_EXTENSION_PREFIX)) {
        String extensionName = command.substring(MasterApiMessage.MASTER_API_COMMAND_EXTENSION_PREFIX.length());
        Map<String, Object> responseMessage = extensionManager.evaluateApiExtension(extensionName, commandArgs);
        responseMessage.put("command", command);
        responseMessage.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_TYPE,
            MasterApiMessage.MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE);
        potentiallyAddRequestId(responseMessage, requestId);

        webSocketFactory.sendJson(connectionId, responseMessage);
      } else {
        MasterApiWebSocketCommandHandler handler = commandHandlers.get(command);
        if (handler != null) {
          Map<String, Object> responseMessage = handler.execute(commandArgs);
          responseMessage.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_TYPE,
              MasterApiMessage.MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE);
          potentiallyAddRequestId(responseMessage, requestId);
          webSocketFactory.sendJson(connectionId, responseMessage);
        } else {
          spaceEnvironment.getLog().error(
              String.format("Master API websocket connection got unknown command %s", command));
        }
      }
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Error while performing Master API websocket command %s", command),
          e);
    }
  }

  /**
   * Add the request ID into the message if there is one.
   *
   * @param message
   *          the message
   * @param requestId
   *          the request ID
   */
  private void potentiallyAddRequestId(Map<String, Object> message, String requestId) {
    if (requestId != null) {
      message.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_REQUEST_ID, requestId);
    }
  }

  /**
   * Register a command handler with the manager.
   *
   * @param handler
   *          the command handler
   */
  private void registerMasterApiHandler(MasterApiWebSocketCommandHandler handler) {
    commandHandlers.put(handler.getCommandName(), handler);
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
  public void setRemoteSpaceControllerClient(RemoteSpaceControllerClient remoteControllerClient) {
    this.remoteSpaceControllerClient = remoteControllerClient;
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
   *          activity manager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * @param masterApiControllerManager
   *          the controller manager to set
   */
  public void setMasterApiSpaceControllerManager(MasterApiSpaceControllerManager masterApiControllerManager) {
    this.masterApiSpaceControllerManager = masterApiControllerManager;
  }

  /**
   * @param masterApiAutomationManager
   *          the automation manager to set
   */
  public void setMasterApiAutomationManager(MasterApiAutomationManager masterApiAutomationManager) {
    this.masterApiAutomationManager = masterApiAutomationManager;
  }

  /**
   * Command handler for a web socket command.
   *
   * @author Keith M. Hughes
   */
  public abstract static class MasterApiWebSocketCommandHandler {

    /**
     * The name of the command.
     */
    private final String commandName;

    /**
     * Create a command handler.
     *
     * @param commandName
     *          the command name
     */
    public MasterApiWebSocketCommandHandler(String commandName) {
      this.commandName = commandName;
    }

    /**
     * Get the name of the command.
     *
     * @return the name of the command
     */
    public String getCommandName() {
      return commandName;
    }

    /**
     * Execute the command.
     *
     * @param commandArgs
     *          the arguments for the command
     *
     * @return the result of the command
     */
    public abstract Map<String, Object> execute(Map<String, Object> commandArgs);

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
    protected String getRequiredStringArg(Map<String, Object> args, String argName)
        throws SimpleInteractiveSpacesException {
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
    protected <K, V> Map<K, V> getRequiredMapArg(Map<String, Object> args, String argName)
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
  }
}
