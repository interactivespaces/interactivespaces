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

package interactivespaces.master.server.ui.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory.MultipleConnectionWebSocketHandler;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.services.ExtensionManager;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.MasterWebsocketManager;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.master.server.ui.UiSpaceManager;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A basic {@link MasterWebsocketManager} implementation.
 * 
 * <p>
 * At the moment this only sends activity and controller events to anyone
 * listening.
 * 
 * @author Keith M. Hughes
 */
public class BasicMasterWebsocketManager implements MasterWebsocketManager,
		MultipleConnectionWebSocketHandler, RemoteSpaceControllerClientListener {

	private static final String WEBSOCKET_STATUS_PARAMETER_NAME_STATUS_TIME = "statusTime";

	private static final String WEBSOCKET_STATUS_PARAMETER_NAME_STATUS = "status";

	private static final String WEBSOCKET_STATUS_PARAMETER_NAME_ID = "id";

	private static final String WEBSOCKET_STATUS_PARAMETER_NAME_UUID = "uuid";

	private static final String WEBSOCKET_STATUS_PARAMETER_VALUE_TYPE_LIVE_ACTIVITY = "liveactivity";

	private static final String WEBSOCKET_STATUS_PARAMETER_NAME_TYPE = "type";

	private static final String WEBSOCKET_COMMAND_EXTENSION_PREFIX = "/extension/";

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
	 * The UI manager for activities.
	 */
	private UiActivityManager uiActivityManager;

	/**
	 * The UI manager for spaces.
	 */
	private UiSpaceManager uiSpaceManager;

	/**
	 * The UI manager for controllers.
	 */
	private UiControllerManager uiControllerManager;

	@Override
	public void startup() {
		int port = spaceEnvironment
				.getSystemConfiguration()
				.getPropertyInteger(
						MasterWebsocketManager.CONFIGURATION_MASTER_WEBSOCKET_PORT,
						MasterWebsocketManager.CONFIGURATION_MASTER_WEBSOCKET_PORT_DEFAULT);

		webServer = new NettyWebServer("master", port,
				spaceEnvironment.getExecutorService(),
				spaceEnvironment.getLog());

		webSocketFactory = new MultipleConnectionWebServerWebSocketHandlerFactory(
				this, spaceEnvironment.getLog());

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
	public void onLiveActivityInstall(String uuid, boolean success) {
		// Don't care
	}

	@Override
	public void onLiveActivityDelete(String uuid, boolean success) {
		// Don't care
	}

	@Override
	public void onLiveActivityStateChange(String uuid, ActivityState state) {
		LiveActivity liveActivity = activityRepository
				.getLiveActivityByUuid(uuid);
		if (liveActivity != null) {
			Map<String, Object> data = Maps.newHashMap();

			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_TYPE,
					WEBSOCKET_STATUS_PARAMETER_VALUE_TYPE_LIVE_ACTIVITY);
			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_UUID, uuid);
			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_ID, liveActivity.getId());
			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_STATUS,
					state.getDescription());

			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_STATUS_TIME, new Date(
					spaceEnvironment.getTimeProvider().getCurrentTime()));

			webSocketFactory.sendJson(data);
		} else {
			spaceEnvironment
					.getLog()
					.warn(String
							.format("Recived status update in web socket master client for unknown live activity UUID %s",
									uuid));
		}
	}

	@Override
	public void onSpaceControllerConnectAttempted(String uuid) {
		// For now don't care.
	}

	@Override
	public void onSpaceControllerDisconnectAttempted(String uuid) {
		// For now don't care.
	}

	@Override
	public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
		// For now don't care.
	}

	@Override
	public void onSpaceControllerStatusChange(String uuid,
			SpaceControllerState status) {
		// For now don't care.
	}

	@Override
	public void handleNewWebSocketConnection(String connectionId) {
		spaceEnvironment.getLog().info(
				String.format("New web socket connection %s", connectionId));
	}

	@Override
	public void handleWebSocketClose(String connectionId) {
		spaceEnvironment.getLog().info(
				String.format("Closed web socket connection %s", connectionId));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleWebSocketReceive(String connectionId, Object data) {
		spaceEnvironment
				.getLog()
				.info(String
						.format("Data from web socket connection %s. Fool thinks we're listening",
								connectionId));

		Map<String, Object> callerArgs = (Map<String, Object>) data;
		Map<String, Object> calleeArgs = (Map<String, Object>) callerArgs
				.get("args");

		String command = (String) callerArgs.get("command");
		if (command.startsWith(WEBSOCKET_COMMAND_EXTENSION_PREFIX)) {
			String extensionName = command
					.substring(WEBSOCKET_COMMAND_EXTENSION_PREFIX.length());
			Map<String, Object> result = extensionManager.evaluateApiExtension(
					extensionName, calleeArgs);
			result.put("command", command);

			webSocketFactory.sendJson(connectionId, result);
		} else if (COMMAND_LIVE_ACTIVITY_VIEW.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityView(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_DEPLOY.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityDeploy(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_CONFIGURE.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityConfigure(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityGetConfiguration(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivitySetConfiguration(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_METADATA_SET.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivitySetMetadata(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_STARTUP.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityStartup(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_ACTIVATE.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityActivate(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_DEACTIVATE.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityDeactivate(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_SHUTDOWN.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityShutdown(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_STATUS.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityStatus(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_DELETE_LOCAL.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityDelete(calleeArgs));
		} else if (COMMAND_LIVE_ACTIVITY_DELETE_REMOTE.equals(command)) {
			webSocketFactory.sendJson(connectionId,
					liveActivityRemoteDelete(calleeArgs));
		} else {
			// For now nothing else.
		}
	}

	/**
	 * Get all data on a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityView(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		LiveActivity liveactivity = activityRepository.getLiveActivityById(id);
		if (liveactivity != null) {
			Map<String, Object> data = Maps.newHashMap();

			uiActivityManager.getLiveActivityViewJsonData(liveactivity, data);

			return JsonSupport.getSuccessJsonResponse(data);
		} else {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Deploy a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityDeploy(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.deployLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Configure a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityConfigure(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.configureLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Get the configuration for a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityGetConfiguration(
			Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			return JsonSupport.getSuccessJsonResponse(uiActivityManager
					.getLiveActivityConfiguration(id));
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Set the configuration for a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivitySetConfiguration(
			Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		Map<String, String> config = getRequiredMapArg(args, "config");
		try {

			uiActivityManager.configureLiveActivity(id,
					(Map<String, String>) config);

			return JsonSupport.getSimpleSuccessJsonResponse();
			// return JsonSupport
			// .getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Set the metadata for a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivitySetMetadata(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		Map<String, Object> metadata = getRequiredMapArg(args, "metadata");

		return uiActivityManager.updateLiveActivityMetadata(id, metadata);
		// return JsonSupport
		// .getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
	}

	/**
	 * Startup a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityStartup(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.startupLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Activate a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityActivate(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.activateLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Deactivate a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityDeactivate(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.deactivateLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Shutdown a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityShutdown(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.shutdownLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Request the status of a live activity.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityStatus(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		LiveActivity activity = activityRepository.getLiveActivityById(id);
		if (activity != null) {
			// Get an update from the controller
			uiControllerManager.statusLiveActivity(id);

			Map<String, Object> statusData = Maps.newHashMap();

			uiActivityManager.getLiveActivityStatusJsonData(activity,
					statusData);

			return JsonSupport.getSuccessJsonResponse(statusData);
		} else {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Delete a live activity from the master model.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityDelete(Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiActivityManager.deleteLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Dlete a live activity on its space controller.
	 * 
	 * @param args
	 *            args from the websocket call
	 * 
	 * @return call response for the request
	 * 
	 * @throws InteractiveSpacesException
	 *             if no ID, ID is not a live activity, or some other issue
	 */
	private Map<String, Object> liveActivityRemoteDelete(
			Map<String, Object> args) {
		String id = getRequiredStringArg(args, "id");
		try {
			uiControllerManager.deleteLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Get a JSON error response for no such live activity.
	 * 
	 * @return the JSON result
	 */
	private Map<String, Object> getNoSuchLiveActivityResult() {
		return JsonSupport
				.getFailureJsonResponse(UiActivityManager.MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN);
	}

	/**
	 * Get a required string argument from the args map.
	 * 
	 * @param args
	 *            the args map
	 * @param argName
	 *            the argument
	 * 
	 * @return the value of the arg
	 * 
	 * @throws InteractiveSpacesException
	 *             if there is no value for the requested arg
	 */
	private String getRequiredStringArg(Map<String, Object> args, String argName) {
		String value = (String) args.get(argName);
		if (value != null) {
			return value;
		} else {
			throw new InteractiveSpacesException("Unknown argument " + argName);
		}
	}

	/**
	 * Get a required string argument from the args map.
	 * 
	 * @param args
	 *            the args map
	 * @param argName
	 *            the argument
	 * 
	 * @return the value of the arg
	 * 
	 * @throws InteractiveSpacesException
	 *             if there is no value for the requested arg
	 */
	private <K, V> Map<K, V> getRequiredMapArg(Map<String, Object> args,
			String argName) {
		Object value = args.get(argName);
		if (value != null) {
			if (Map.class.isAssignableFrom(value.getClass())) {
				@SuppressWarnings("unchecked")
				Map<K, V> value2 = (Map<K, V>) value;
				return value2;
			} else {
				throw new InteractiveSpacesException("Argument not map "
						+ argName);
			}
		} else {
			throw new InteractiveSpacesException("Unknown argument " + argName);
		}
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * @param extensionManager
	 *            the extensionManager to set
	 */
	public void setExtensionManager(ExtensionManager extensionManager) {
		this.extensionManager = extensionManager;
	}

	/**
	 * @param remoteControllerClient
	 *            the remoteControllerClient to set
	 */
	public void setRemoteControllerClient(
			RemoteControllerClient remoteControllerClient) {
		this.remoteControllerClient = remoteControllerClient;
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param uiActivityManager
	 *            the uiActivityManager to set
	 */
	public void setUiActivityManager(UiActivityManager uiActivityManager) {
		this.uiActivityManager = uiActivityManager;
	}

	/**
	 * @param uiSpaceManager
	 *            the uiSpaceManager to set
	 */
	public void setUiSpaceManager(UiSpaceManager uiSpaceManager) {
		this.uiSpaceManager = uiSpaceManager;
	}

	/**
	 * @param uiControllerManager
	 *            the uiControllerManager to set
	 */
	public void setUiControllerManager(UiControllerManager uiControllerManager) {
		this.uiControllerManager = uiControllerManager;
	}
}
