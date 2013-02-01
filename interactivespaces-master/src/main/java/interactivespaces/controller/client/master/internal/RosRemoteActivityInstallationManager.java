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

package interactivespaces.controller.client.master.internal;

import interactivespaces.activity.repository.ActivityRepositoryServer;
import interactivespaces.controller.client.master.RemoteActivityInstallStatus;
import interactivespaces.controller.client.master.RemoteActivityInstallationManager;
import interactivespaces.controller.client.master.RemoteActivityInstallationManagerListener;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeployRequest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;

/**
 * A ROS-based Interactive Spaces activity installer.
 * 
 * @author Keith M. Hughes
 */
public class RosRemoteActivityInstallationManager implements
		RemoteActivityInstallationManager {

	/**
	 * The client for making calls to a remote space controller.
	 */
	private RemoteControllerClient remoteControllerClient;

	/**
	 * Server for activity repository.
	 */
	private ActivityRepositoryServer repositoryServer;

	/**
	 * All listeners for installer events.
	 */
	private List<RemoteActivityInstallationManagerListener> listeners = new CopyOnWriteArrayList<RemoteActivityInstallationManagerListener>();

	/**
	 * Logger for this installer.
	 */
	private Log log;

	@Override
	public void startup() {
	}

	@Override
	public void shutdown() {
		// Nothing to do
	}

	@Override
	public void deployActivity(LiveActivity activity) {
		LiveActivityDeployRequest request = remoteControllerClient
				.newLiveActivityDeployRequest();
		request.setUuid(activity.getUuid());
		request.setIdentifyingName(activity.getActivity().getIdentifyingName());
		request.setVersion(activity.getActivity().getVersion());
		request.setActivitySourceUri(repositoryServer.getActivityUri(activity
				.getActivity()));

		remoteControllerClient.deployActivity(activity, request);
	}

	@Override
	public void deleteActivity(LiveActivity activity) {
		LiveActivityDeleteRequest request = remoteControllerClient
				.newLiveActivityDeleteRequest();
		request.setUuid(activity.getUuid());
		request.setIdentifyingName(activity.getActivity().getIdentifyingName());
		request.setVersion(activity.getActivity().getVersion());
		request.setForce(1);

		remoteControllerClient.deleteActivity(activity, request);
	}

	@Override
	public void addListener(RemoteActivityInstallationManagerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(
			RemoteActivityInstallationManagerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Signal all listeners about the status of the install.
	 * 
	 * @param uuid
	 *            uuid of the activity which was installed
	 * @param status
	 *            status of the install
	 */
	private void signalListenersOnInstall(String uuid,
			RemoteActivityInstallStatus status) {
		for (RemoteActivityInstallationManagerListener listener : listeners) {
			listener.onRemoteActivityInstall(uuid, status);
		}
	}

	/**
	 * @param repositoryServer
	 *            The repository server to use. Can be null.
	 */
	public void setRepositoryServer(ActivityRepositoryServer repositoryServer) {
		this.repositoryServer = repositoryServer;
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
	 * @param log
	 *            the log to set
	 */
	public void setLog(Log log) {
		this.log = log;
	}
}
