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

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory;
import interactivespaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory.MultipleConnectionWebSocketHandler;
import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ExtensionManager;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.ui.MasterWebsocketManager;
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

	private static final String WEBSOCKET_COMMAND_EXTENSION_PREFIX = "extension-";

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

		webSocketFactory = new MultipleConnectionWebServerWebSocketHandlerFactory(this,
				spaceEnvironment.getLog());

		webServer.setWebSocketHandlerFactory("", webSocketFactory);

		webServer.start();

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
	public void onLiveActivityStateChange(String uuid, ActivityState state) {
		LiveActivity liveActivity = activityRepository
				.getLiveActivityByUuid(uuid);
		if (liveActivity != null) {
			Map<String, Object> data = Maps.newHashMap();

			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_TYPE, WEBSOCKET_STATUS_PARAMETER_VALUE_TYPE_LIVE_ACTIVITY);
			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_UUID, uuid);
			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_ID, liveActivity.getId());
			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_STATUS, state.getDescription());

			data.put(WEBSOCKET_STATUS_PARAMETER_NAME_STATUS_TIME, new Date(spaceEnvironment.getTimeProvider()
					.getCurrentTime()));

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
		} else {
			// For now nothing else.
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
}
