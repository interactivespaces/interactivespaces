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
import interactivespaces.controller.client.master.RemoteActivityDeployer;
import interactivespaces.controller.client.master.RemoteActivityDeployerListener;
import interactivespaces.controller.client.master.RemoteActivityInstallStatus;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.RemoteControllerClient;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.ros.message.interactivespaces_msgs.LiveActivityDeployRequest;

/**
 * A ROS-based Interactive Spaces activity installer.
 * 
 * @author Keith M. Hughes
 */
public class RosRemoteActivityDeployer implements RemoteActivityDeployer {
	
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
	private List<RemoteActivityDeployerListener> listeners = new CopyOnWriteArrayList<RemoteActivityDeployerListener>();

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
		LiveActivityDeployRequest request = new LiveActivityDeployRequest();
		request.uuid = activity.getUuid();
		request.identifying_name = activity.getActivity()
				.getIdentifyingName();
		request.version = activity.getActivity().getVersion();
		request.activity_source_uri = repositoryServer
				.getActivityUri(activity.getActivity());

		remoteControllerClient.deployActivity(activity, request);
	}

	@Override
	public void addListener(RemoteActivityDeployerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(RemoteActivityDeployerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Run the deployment goal.
	 * 
	 * @param liveActivity
	 *            the live activity being deployed
	 * @param request
	 *            the deployment request
	 */
	private void runInstallGoal(LiveActivity liveActivity,
			LiveActivityDeployRequest request) {
//		final NodeConfiguration configuration = rosEnvironment
//				.getPublicNodeConfiguration();
//		RosSpaceControllerSupport.setNamespaceForActivityDeployment(
//				configuration, "/" + remoteNodeName);
//		String deployerNodeName = RosSpaceControllerConstants.ACTIVITY_DEPLOYMENT_DEPLOYER_NODE_NAME
//				+ rosEnvironment.getNodeName();
//		configuration.setNodeName(deployerNodeName);
//
//		// The node name will include the node name of this server.
//		final ActivityInstallSimpleActionClient sac = deploymentActionSpec
//				.buildSimpleActionClient(RosSpaceControllerConstants.ACTIVITY_DEPLOYMENT_CLIENT_NODE_NAME
//						+ "/" + deployerNodeName);
//
//		sac.addClientPubSub(rosEnvironment.newNode(configuration));
//
//		try {
//			// wait for the action server to start
//			sac.waitForServer(); // will wait for infinite time
//
//			final CountDownLatch done = new CountDownLatch(1);
//			sac.sendGoal(
//					goal,
//					new SimpleActionClientCallbacks<ActivityInstallFeedback, ActivityInstallResult>() {
//						@Override
//						public void activeCallback() {
//							log.debug("Starting deployment");
//						}
//
//						@Override
//						public void feedbackCallback(
//								ActivityInstallFeedback feedback) {
//							if (log.isDebugEnabled()) {
//								String statusMessage = "unknown";
//								switch (feedback.status) {
//								case ActivityInstallFeedback.STATUS_STARTED_COPY:
//									statusMessage = "Started copy";
//									break;
//
//								case ActivityInstallFeedback.STATUS_COMPLETED_COPY:
//									statusMessage = "Completed copy";
//									break;
//
//								case ActivityInstallFeedback.STATUS_COMPLETED_UNPACK:
//									statusMessage = "Completed unpacking";
//									break;
//
//								}
//
//								log.debug(String
//										.format("URI %s installing as uuid %s, status %s\n",
//												feedback.activity_source_uri,
//												feedback.uuid, statusMessage));
//							}
//						}
//
//						@Override
//						public void doneCallback(SimpleClientGoalState state,
//								ActivityInstallResult result) {
//							RemoteActivityInstallStatus status = RemoteActivityInstallStatus.FAIL;
//							if (ActivityInstallResult.STATUS_SUCCESS == result.status) {
//								status = RemoteActivityInstallStatus.SUCCESS;
//
//							}
//
//							signalListenersOnInstall(result.uuid, status);
//
//							if (log.isInfoEnabled()) {
//								String statusMessage = "unknown";
//								switch (result.status) {
//								case ActivityInstallResult.STATUS_FAILURE_COPY:
//									statusMessage = "Copy failed";
//									break;
//
//								case ActivityInstallResult.STATUS_FAILURE_UNPACK:
//									statusMessage = "Unpack failed";
//									break;
//
//								case ActivityInstallResult.STATUS_SUCCESS:
//									statusMessage = "Success";
//									break;
//
//								}
//
//								log.info(String
//										.format("Activity with uuid %s deployed with status %s",
//												result.uuid, statusMessage));
//							}
//
//							done.countDown();
//						}
//					});
//
//			done.await();
//		} catch (Exception e) {
//			log.error("Could not deploy activity", e);
//		} finally {
//			sac.shutdown();
//		}
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
		for (RemoteActivityDeployerListener listener : listeners) {
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
	 * @param remoteControllerClient the remoteControllerClient to set
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
