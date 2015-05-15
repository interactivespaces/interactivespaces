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

package interactivespaces.master.api.master.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.api.master.MasterApiActivityManager;
import interactivespaces.master.api.master.MasterApiAutomationManager;
import interactivespaces.master.api.master.MasterApiSpaceControllerManager;
import interactivespaces.master.api.master.MasterWebsocketManager;
import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

/**
 * A basic {@link MasterWebsocketManager} implementation.
 *
 * <p>
 * At the moment this only sends activity and controller events to anyone listening.
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
    registerActivityHandlers();
    registerLiveActivityHandlers();
    registerLiveActivityGroupHandlers();
    registerSpaceHandlers();
    registerSpaceControllerHandlers();
    registerMiscHandlers();
  }

  /**
   * Register all handlers for Live Activity commands.
   */
  private void registerActivityHandlers() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getActivitiesByFilter(getFilter(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getActivityView(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployAllLiveActivityInstances(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata =
            getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiActivityManager.updateActivityMetadata(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.deleteActivity(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for Live Activity commands.
   */
  private void registerLiveActivityHandlers() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivitiesByFilter(getFilter(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivityView(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.configureLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivityConfiguration(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, String> config = getConfiguration(commandArgs);
        return masterApiActivityManager.configureLiveActivity(id, config);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata =
            getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiActivityManager.updateLiveActivityMetadata(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.startupLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.activateLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deactivateLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.deleteLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_REMOTE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deleteLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanLiveActivityPermanentData(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanLiveActivityTempData(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for Live Activity Group commands.
   */
  private void registerLiveActivityGroupHandlers() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivityGroupsByFilter(getFilter(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivityGroupView(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.configureLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata =
            getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiActivityManager.updateLiveActivityGroupMetadata(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.startupLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.activateLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deactivateLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN_FORCE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.forceShutdownLiveActivitiesLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.deleteLiveActivityGroup(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for Space commands.
   */
  private void registerSpaceHandlers() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getSpacesByFilter(getFilter(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getSpaceView(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deploySpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.configureSpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata =
            getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiActivityManager.updateSpaceMetadata(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.startupSpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.activateSpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deactivateSpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownSpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusSpace(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.deleteSpace(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for Space Controller commands.
   */
  private void registerSpaceControllerHandlers() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.getSpaceControllersByFilter(getFilter(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.getSpaceControllerView(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURATION_GET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.getSpaceControllerConfiguration(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURATION_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, String> config = getConfiguration(commandArgs);
        return masterApiSpaceControllerManager.setSpaceControllerConfiguration(id, config);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.configureSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata =
            getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiSpaceControllerManager.updateSpaceControllerMetadata(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONNECT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.connectToSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DISCONNECT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.disconnectFromSpaceControllers(Lists
            .newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONNECT_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.connectToAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DISCONNECT_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.disconnectFromAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_STATUS_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusFromAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployAllActivityInstancesSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DEPLOY_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployAllActivityInstancesAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerPermanentData(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerTempData(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_PERMANENT_CLEAN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerPermanentDataAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_TEMPORARY_CLEAN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerTempDataAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesPermanentData(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesTempData(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesTempDataAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_SHUTDOWN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownAllActivities(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_SHUTDOWN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownAllActivitiesAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_CAPTURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.captureDataSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_RESTORE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.restoreDataSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_DATA_CAPTURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.captureDataAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_DATA_RESTORE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.restoreDataAllSpaceControllers();
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deleteSpaceController(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for misc commands.
   */
  private void registerMiscHandlers() {
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiAutomationManager.getNamedScriptsByFilter(getFilter(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_VIEW) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiAutomationManager.getNamedScriptView(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_RUN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiAutomationManager.runNamedScript(getEntityId(commandArgs));
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata =
            getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiAutomationManager.updateNamedScriptMetadata(id, metadata);
      }
    });
    registerMasterApiHandler(new MasterApiWebSocketCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_DELETE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiAutomationManager.deleteNamedScript(getEntityId(commandArgs));
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

      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_TYPE,
          MasterApiMessages.MASTER_API_PARAMETER_VALUE_TYPE_STATUS_LIVE_ACTIVITY);
      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_UUID, uuid);
      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE, runtimeState.name());
      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE_DESCRIPTION,
          runtimeState.getDescription());
      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_DETAIL, detail);

      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_TIME, new Date(spaceEnvironment.getTimeProvider()
          .getCurrentTime()));

      Map<String, Object> message = Maps.newHashMap();
      message.put(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_TYPE,
          MasterApiMessages.MASTER_API_MESSAGE_TYPE_STATUS_UPDATE);
      message.put(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA, data);

      webSocketFactory.sendJson(message);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Received status update in web socket master client for unknown live activity UUID %s", uuid));
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

    String command = (String) message.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_TYPE);
    Map<String, Object> commandArgs =
        (Map<String, Object>) message.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA);

    String requestId = (String) message.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_REQUEST_ID);

    try {
      if (command.startsWith(MasterApiMessages.MASTER_API_COMMAND_EXTENSION_PREFIX)) {
        String extensionName = command.substring(MasterApiMessages.MASTER_API_COMMAND_EXTENSION_PREFIX.length());
        Map<String, Object> responseMessage = extensionManager.evaluateApiExtension(extensionName, commandArgs);
        responseMessage.put("command", command);
        responseMessage.put(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_TYPE,
            MasterApiMessages.MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE);
        potentiallyAddRequestId(responseMessage, requestId);

        webSocketFactory.sendJson(connectionId, responseMessage);
      } else {
        executeWithCommandHandler(connectionId, command, commandArgs, requestId);
      }
    } catch (Throwable e) {
      spaceEnvironment.getLog().error(
          String.format("Error while performing Master API websocket command %s", command), e);

      Map<String, Object> responseMessage = MasterApiMessageSupport.getFailureResponse(e.getMessage());
      potentiallyAddRequestId(responseMessage, requestId);

      try {
        webSocketFactory.sendJson(connectionId, responseMessage);
      } catch (Throwable e1) {
        spaceEnvironment.getLog().error(
            String.format("Error while responding to failure of Master API websocket command %s", command), e1);
      }
    }
  }

  /**
   * Execute the command with a command handler.
   *
   * @param connectionId
   *          the connection IS to the remote web socket client
   * @param command
   *          the command to be executed
   * @param commandArgs
   *          the arguments for the command, can be {@code null}
   * @param requestId
   *          the request ID for the command, can be {@code null}
   */
  @VisibleForTesting
      void
      executeWithCommandHandler(String connectionId, String command, Map<String, Object> commandArgs, String requestId) {
    MasterApiWebSocketCommandHandler handler = commandHandlers.get(command);
    if (handler != null) {
      Map<String, Object> responseMessage = handler.execute(commandArgs);
      responseMessage.put(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_TYPE,
          MasterApiMessages.MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE);
      potentiallyAddRequestId(responseMessage, requestId);
      webSocketFactory.sendJson(connectionId, responseMessage);
    } else {
      spaceEnvironment.getLog()
          .error(String.format("Master API websocket connection got unknown command %s", command));
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
      message.put(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_REQUEST_ID, requestId);
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
  public abstract class MasterApiWebSocketCommandHandler {

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

    /**
     * Get the filter parameter from the command arguments.
     *
     * @param commandArgs
     *          the command arguments
     *
     * @return the filter, or {code null} if none
     */
    protected String getFilter(Map<String, Object> commandArgs) {
      return (commandArgs != null) ? (String) commandArgs.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_FILTER)
          : null;
    }

    /**
     * Get the entity ID from the command arguments.
     *
     * @param commandArgs
     *          the command arguments
     *
     * @return the entity ID
     *
     * @throws InteractiveSpacesException
     *           the entity ID was not in the command arguments
     */
    protected String getEntityId(Map<String, Object> commandArgs) throws InteractiveSpacesException {
      return getRequiredStringArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID);
    }

    /**
     * Get the configuration map from the command arguments.
     *
     * @param commandArgs
     *          the command arguments
     *
     * @return the configuration map
     *
     * @throws InteractiveSpacesException
     *           the configuration map was not in the command arguments
     */
    protected Map<String, String> getConfiguration(Map<String, Object> commandArgs) throws InteractiveSpacesException {
      return getRequiredMapArg(commandArgs, MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_CONFIG);
    }
  }
}
