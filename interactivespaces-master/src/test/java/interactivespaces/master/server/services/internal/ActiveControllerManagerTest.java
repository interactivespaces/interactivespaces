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

package interactivespaces.master.server.services.internal;

import static org.junit.Assert.assertEquals;
import interactivespaces.activity.ActivityState;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActivityDeploymentManager;
import interactivespaces.master.server.services.SpaceControllerListener;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Tests for the {@link BasicActiveControllerManager}.
 * 
 * @author Keith M. Hughes
 */
public class ActiveControllerManagerTest extends BaseSpaceTest {
	private BasicActiveControllerManager activeControllerManager;

	private RemoteControllerClient remoteControllerClient;

	private RemoteControllerClientListenerHelper remoteControllerListenerHelper = new RemoteControllerClientListenerHelper();

	private SpaceControllerListener controllerListener;

	private ActivityDeploymentManager activityDeploymentManager;

	private Log log;

	private InteractiveSpacesEnvironment spaceEnvironment;

	@Before
	public void setup() {
		baseSetup();

		spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
		Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(
				timeProvider);

		log = Mockito.mock(Log.class);

		Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

		activityDeploymentManager = Mockito
				.mock(ActivityDeploymentManager.class);

		activeControllerManager = new BasicActiveControllerManager();

		activeControllerManager
				.setActivityDeploymentManager(activityDeploymentManager);

		remoteControllerClient = Mockito.mock(RemoteControllerClient.class);
		activeControllerManager
				.setRemoteControllerClient(remoteControllerClient);
		activeControllerManager.setSpaceEnvironment(spaceEnvironment);

		controllerListener = Mockito.mock(SpaceControllerListener.class);
		activeControllerManager.addControllerListener(controllerListener);

		remoteControllerListenerHelper = new RemoteControllerClientListenerHelper();
		remoteControllerListenerHelper.addListener(activeControllerManager);
	}

	/**
	 * Test a live activity going to the successful deploy states.
	 */
	@Test
	public void testRemoteDeployStateSuccess() {
		LiveActivity liveActivity = liveActivity(0);
		ActiveLiveActivity activeLiveActivity = activeLiveActivity(liveActivity);

		ActivityState oldState = ActivityState.DEPLOY_ATTEMPT;
		activeLiveActivity.setRuntimeState(oldState);
		remoteControllerListenerHelper.signalActivityStateChange(
				liveActivity.getUuid(), ActivityState.READY);
		assertEquals(ActivityState.READY, activeLiveActivity.getRuntimeState());
		Mockito.verify(controllerListener, Mockito.times(1))
				.onLiveActivityStateChange(liveActivity.getUuid(), oldState,
						ActivityState.READY);
	}

	/**
	 * Test a live activity going to the failure deploy states.
	 */
	@Test
	public void testRemoteDeployStateFailure() {
		LiveActivity liveActivity = liveActivity(0);
		ActiveLiveActivity activeLiveActivity = activeLiveActivity(liveActivity);

		ActivityState startState = ActivityState.DEPLOY_ATTEMPT;
		ActivityState finalState = ActivityState.DEPLOY_FAILURE;
		activeLiveActivity.setRuntimeState(startState);
		remoteControllerListenerHelper.signalActivityStateChange(
				liveActivity.getUuid(), finalState);
		assertEquals(finalState, activeLiveActivity.getRuntimeState());
		Mockito.verify(controllerListener, Mockito.times(1))
				.onLiveActivityStateChange(liveActivity.getUuid(), startState,
						finalState);
	}

	/**
	 * Test an app going through the start states.
	 */
	// @Test
	// public void testRemoteStartupStates() {
	// LiveActivity app = app(0);
	// ActiveLiveActivity activeLiveActivity = activeControllerManager
	// .getActiveActivity(app);
	//
	// activeLiveActivity.setState(ActivityState.STARTUP_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityStart(app.getUuid(),
	// RemoteActivityStartStatus.SUCCESS);
	// assertEquals(ActivityState.RUNNING, activeLiveActivity.getState());
	// Mockito.verify(controllerListener, Mockito.times(1)).onActivityStartup(
	// app);
	//
	// activeLiveActivity.setState(ActivityState.STARTUP_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityStart(app.getUuid(),
	// RemoteActivityStartStatus.FAILURE);
	// assertEquals(ActivityState.STARTUP_FAILURE,
	// activeLiveActivity.getState());
	// }
	//
	// /**
	// * Test an app going through the activate states.
	// */
	// @Test
	// public void testRemoteActivateStates() {
	// LiveActivity app = app(0);
	// ActiveLiveActivity activeLiveActivity = activeControllerManager
	// .getActiveActivity(app);
	//
	// activeLiveActivity.setState(ActivityState.ACTIVATE_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityActivate(app.getUuid(),
	// RemoteActivityActivateStatus.SUCCESS);
	// assertEquals(ActivityState.ACTIVE, activeLiveActivity.getState());
	// Mockito.verify(controllerListener, Mockito.times(1))
	// .onActivityActivate(app);
	//
	// activeLiveActivity.setState(ActivityState.ACTIVATE_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityActivate(app.getUuid(),
	// RemoteActivityActivateStatus.FAILURE);
	// assertEquals(ActivityState.ACTIVATE_FAILURE,
	// activeLiveActivity.getState());
	// }
	//
	// /**
	// * Test an app going through the deactivate states.
	// */
	// @Test
	// public void testRemoteDeactivateStates() {
	// LiveActivity app = app(0);
	// ActiveLiveActivity activeLiveActivity = activeControllerManager
	// .getActiveActivity(app);
	//
	// activeLiveActivity.setState(ActivityState.DEACTIVATE_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityDeactivate(app.getUuid(),
	// RemoteActivityDeactivateStatus.SUCCESS);
	// assertEquals(ActivityState.RUNNING, activeLiveActivity.getState());
	// Mockito.verify(controllerListener, Mockito.times(1))
	// .onActivityDeactivate(app);
	//
	// activeLiveActivity.setState(ActivityState.ACTIVATE_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityDeactivate(app.getUuid(),
	// RemoteActivityDeactivateStatus.FAILURE);
	// assertEquals(ActivityState.DEACTIVATE_FAILURE,
	// activeLiveActivity.getState());
	// }
	//
	// /**
	// * Test an app going through the shutdown states.
	// */
	// @Test
	// public void testRemoteShutdownStates() {
	// LiveActivity app = app(0);
	// ActiveLiveActivity activeLiveActivity = activeControllerManager
	// .getActiveActivity(app);
	//
	// activeLiveActivity.setState(ActivityState.SHUTDOWN_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityShutdown(app.getUuid(),
	// RemoteActivityShutdownStatus.SUCCESS);
	// assertEquals(ActivityState.READY, activeLiveActivity.getState());
	// Mockito.verify(controllerListener, Mockito.times(1))
	// .onActivityShutdown(app);
	//
	// activeLiveActivity.setState(ActivityState.SHUTDOWN_ATTEMPT);
	// remoteControllerListenerHelper.sendOnActivityShutdown(app.getUuid(),
	// RemoteActivityShutdownStatus.FAILURE);
	// assertEquals(ActivityState.SHUTDOWN_FAILURE,
	// activeLiveActivity.getState());
	// }

	/**
	 * Make sure an activity deploys.
	 */
	@Test
	public void testActivityDeployment() {
		LiveActivity app = liveActivity(10);

		activeControllerManager.deployLiveActivity(app);

		ActiveLiveActivity activeLiveActivity = activeControllerManager
				.getActiveLiveActivity(app);
		assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
				ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(app);
	}

	/**
	 * Make sure an activity starts up.
	 */
	@Test
	public void testActivityStartup() {
		LiveActivity app = liveActivity(10);

		activeControllerManager.startupLiveActivity(app);

		ActiveLiveActivity activeLiveActivity = activeControllerManager
				.getActiveLiveActivity(app);
		assertActiveActivityState(activeLiveActivity, true, 0, false, 0,
				ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(app);

	}

	/**
	 * Make sure activity shuts down.
	 */
	@Test
	public void testActivityShutdown() {
		LiveActivity app = liveActivity(10);

		activeControllerManager.startupLiveActivity(app);
		activeControllerManager.shutdownLiveActivity(app);

		ActiveLiveActivity activeLiveActivity = activeControllerManager
				.getActiveLiveActivity(app);
		assertActiveActivityState(activeLiveActivity, false, 0, false, 0,
				ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(app);
	}

	/**
	 * Make sure all activity activates.
	 */
	@Test
	public void testActivityActivate() {
		LiveActivity app = liveActivity(10);

		activeControllerManager.startupLiveActivity(app);
		activeControllerManager.activateLiveActivity(app);

		ActiveLiveActivity activeLiveActivity = activeControllerManager
				.getActiveLiveActivity(app);
		assertActiveActivityState(activeLiveActivity, true, 0, true, 0,
				ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(app);
	}

	/**
	 * Make sure an activity deactivates.
	 */
	@Test
	public void testActivityDeactivate() {
		LiveActivity app = liveActivity(10);

		activeControllerManager.startupLiveActivity(app);
		activeControllerManager.activateLiveActivity(app);
		activeControllerManager.deactivateLiveActivity(app);

		ActiveLiveActivity activeLiveActivity = activeControllerManager
				.getActiveLiveActivity(app);
		assertActiveActivityState(activeLiveActivity, true, 0, false, 0,
				ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(app);
	}

	/**
	 * Make sure all activities in a group deploy.
	 */
	@Test
	public void testActivityGroupDeployment() {
		List<LiveActivity> liveActivities = liveActivities(10, 11);
		List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities);

		activeControllerManager.deployLiveActivityGroup(group1);

		assertActiveActivityState(activeActivities.get(0), false, 0, false, 0,
				ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
		assertActiveActivityState(activeActivities.get(1), false, 0, false, 0,
				ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(activeActivities.get(0).getLiveActivity());
		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(activeActivities.get(1).getLiveActivity());
	}

	/**
	 * Make sure all activities of the activity group start up.
	 */
	@Test
	public void testActivityGroupStartup() {
		List<LiveActivity> liveActivities = liveActivities(10, 11);
		List<ActiveLiveActivity> activeActivities = activeLiveActivities(liveActivities);

		LiveActivityGroup group1 = liveActivityGroup(liveActivities);

		activeControllerManager.startupLiveActivityGroup(group1);

		assertActiveActivityState(activeActivities.get(0), false, 1, false, 0,
				ActivityState.STARTUP_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeActivities.get(1), false, 1, false, 0,
				ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeActivities.get(0).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeActivities.get(1).getLiveActivity());
	}

	/**
	 * Make sure all activities of the activity group shutdown.
	 */
	@Test
	public void testActivityGroupShutdown() {
		List<LiveActivity> liveActivities = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.shutdownLiveActivityGroup(group1);

		assertActiveActivityState(activeLiveActivities.get(0), false, 0, false,
				0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities.get(1), false, 0, false,
				0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(activeLiveActivities.get(0).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(activeLiveActivities.get(1).getLiveActivity());
	}

	/**
	 * Make sure all activities of the activity group activate.
	 */
	@Test
	public void testActivityGroupActivate() {
		List<LiveActivity> liveActivities = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.activateLiveActivityGroup(group1);

		assertActiveActivityState(activeLiveActivities.get(0), false, 1, false,
				1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities.get(1), false, 1, false,
				1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(activeLiveActivities.get(0).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(activeLiveActivities.get(1).getLiveActivity());
	}

	/**
	 * Make sure all activities in the activity group deactivate.
	 */
	@Test
	public void testActivityGroupDeactivate() {
		List<LiveActivity> liveActivities = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities = activeLiveActivities(liveActivities);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.activateLiveActivityGroup(group1);
		activeControllerManager.deactivateLiveActivityGroup(group1);

		assertActiveActivityState(activeLiveActivities.get(0), false, 1, false,
				0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities.get(1), false, 1, false,
				0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(
						activeLiveActivities.get(0).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(
						activeLiveActivities.get(1).getLiveActivity());
	}

	/**
	 * Make sure all activities in each group deploy, even if duplicated.
	 */
	@Test
	public void testMultipleActivityGroupAllDeploymentSeparateCalls() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.deployLiveActivityGroup(group1);
		activeControllerManager.deployLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(
						activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two subtrees. Make sure deployed twice since two
		// calls.
		Mockito.verify(activityDeploymentManager, Mockito.times(2))
				.deployLiveActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Make sure all activities in each group deploy only once when call with
	 * deploy set.
	 */
	@Test
	public void testMultipleActivityGroupAllDeploymentOverlaping() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		Set<ActiveLiveActivity> deployedActivities = Sets.newHashSet();
		activeControllerManager.deployActiveLiveActivityGroupChecked(
				activeControllerManager.getActiveLiveActivityGroup(group1),
				deployedActivities);
		activeControllerManager.deployActiveLiveActivityGroupChecked(
				activeControllerManager.getActiveLiveActivityGroup(group2),
				deployedActivities);

		assertEquals(3, deployedActivities.size());

		assertActiveActivityState(activeLiveActivities1.get(0), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.DEPLOY_ATTEMPT);

		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(
						activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two subtrees. Make sure deployed once since tracking.
		Mockito.verify(activityDeploymentManager, Mockito.times(1))
				.deployLiveActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Make sure all elements of the multiple activity groups start up.
	 */
	@Test
	public void testMultipleActivityGroupAllStartup() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 2,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 2,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two subtrees. Make sure only started once.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Start up activities in two activity groups. One activity will be in both.
	 * Force it to shutdown.
	 */
	@Test
	public void testMultipleActivityGroupAllStartupForceShutdown() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		activeControllerManager.shutdownLiveActivity(liveActivity(10));

		assertActiveActivityState(activeLiveActivities1.get(0), false, 0,
				false, 0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 0,
				false, 0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities2.get(0).getLiveActivity());

		// This one should be shutdown
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Make sure all elements of the multiple activity groups shutdown.
	 */
	@Test
	public void testMultipleActivityGroupAllShutdown() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		activeControllerManager.shutdownLiveActivityGroup(group1);
		activeControllerManager.shutdownLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 0,
				false, 0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 0,
				false, 0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 0,
				false, 0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 0,
				false, 0, ActivityState.SHUTDOWN_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(
						activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two subtrees. Make sure only stopped once.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.shutdownActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Multiple activity groups. One activity is in both. Activate one of the
	 * groups containing the activity and make sure that unshared activities are
	 * activated, but the shared activity is left alone.
	 */
	@Test
	public void testMultipleActivityGroupPartialStartup() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 0,
				false, 0, ActivityState.UNKNOWN, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.never())
				.startupActivity(activeLiveActivities1.get(1).getLiveActivity());

		// This one was in only the started up group.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two groups. Make sure was started up.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.startupActivity(activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Make sure all activities of the multiple activity groups activate.
	 */
	@Test
	public void testMultipleActivityGroupAllActivate() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		activeControllerManager.activateLiveActivityGroup(group1);
		activeControllerManager.activateLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 2,
				false, 2, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 1,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 2,
				false, 2, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(
						activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two subtrees. Make sure only started once.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Multiple activity groups are started up. One activity is in both.
	 * Activate one of the groups containing the activity and make sure that
	 * unshared activities are activated, but the shared activity is left alone.
	 */
	@Test
	public void testMultipleActivityGroupPartialActivate() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		activeControllerManager.activateLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 2,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 1,
				false, 0, ActivityState.STARTUP_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 2,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.never())
				.activateActivity(
						activeLiveActivities1.get(1).getLiveActivity());

		// This one was in only the activated group.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two groups. Make sure was activated.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.activateActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Make sure all activities in the multiple activity groups deactivate.
	 */
	@Test
	public void testMultipleActivityGroupAllDeactivate() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		activeControllerManager.activateLiveActivityGroup(group1);
		activeControllerManager.activateLiveActivityGroup(group2);

		activeControllerManager.deactivateLiveActivityGroup(group1);
		activeControllerManager.deactivateLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 2,
				false, 0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 1,
				false, 0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 2,
				false, 0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(
						activeLiveActivities1.get(1).getLiveActivity());
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two subtrees. Make sure only stopped once.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Multiple activity groups are deactivated. One activity is in both. Stop
	 * one of the groups containing the activity and make sure that unshared
	 * activities are deactivated, but the shared activity is left alone.
	 */
	@Test
	public void testMultipleActivityGroupPartialDeactivate() {
		List<LiveActivity> liveActivities1 = liveActivities(10, 11);
		List<ActiveLiveActivity> activeLiveActivities1 = activeLiveActivities(liveActivities1);
		LiveActivityGroup group1 = liveActivityGroup(liveActivities1);

		List<LiveActivity> liveActivities2 = liveActivities(12, 10);
		List<ActiveLiveActivity> activeLiveActivities2 = activeLiveActivities(liveActivities2);
		LiveActivityGroup group2 = liveActivityGroup(liveActivities2);

		activeControllerManager.startupLiveActivityGroup(group1);
		activeControllerManager.startupLiveActivityGroup(group2);

		activeControllerManager.activateLiveActivityGroup(group1);
		activeControllerManager.activateLiveActivityGroup(group2);

		activeControllerManager.deactivateLiveActivityGroup(group2);

		assertActiveActivityState(activeLiveActivities1.get(0), false, 2,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities1.get(1), false, 1,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		assertActiveActivityState(activeLiveActivities2.get(0), false, 1,
				false, 0, ActivityState.DEACTIVATE_ATTEMPT, ActivityState.READY);
		assertActiveActivityState(activeLiveActivities2.get(1), false, 2,
				false, 1, ActivityState.ACTIVATE_ATTEMPT, ActivityState.READY);

		Mockito.verify(remoteControllerClient, Mockito.never())
				.deactivateActivity(
						activeLiveActivities1.get(1).getLiveActivity());

		// This one was in only the deactivated group.
		Mockito.verify(remoteControllerClient, Mockito.times(1))
				.deactivateActivity(
						activeLiveActivities2.get(0).getLiveActivity());

		// This one was in two groups. Make sure not stopped.
		Mockito.verify(remoteControllerClient, Mockito.never())
				.deactivateActivity(
						activeLiveActivities1.get(0).getLiveActivity());
	}

	/**
	 * Get all active live activities.
	 * 
	 * @param liveActivities
	 *            the live activities
	 * 
	 * @return the active live activities
	 */
	public ActiveLiveActivity activeLiveActivity(LiveActivity liveActivity) {
		return activeControllerManager.getActiveLiveActivity(liveActivity);
	}

	/**
	 * Get all active live activities.
	 * 
	 * @param liveActivities
	 *            the live activities
	 * 
	 * @return the active live activities
	 */
	public List<ActiveLiveActivity> activeLiveActivities(
			LiveActivity... liveActivities) {
		List<ActiveLiveActivity> activeActivities = Lists.newArrayList();

		for (LiveActivity liveActivity : liveActivities) {
			activeControllerManager.getActiveLiveActivity(liveActivity);
		}

		return activeActivities;
	}

	/**
	 * Get all active live activities.
	 * 
	 * @param liveActivities
	 *            the live activities
	 * 
	 * @return the active live activities
	 */
	public List<ActiveLiveActivity> activeLiveActivities(
			List<LiveActivity> liveActivities) {
		List<ActiveLiveActivity> activeActivities = Lists.newArrayList();

		for (LiveActivity liveActivity : liveActivities) {
			activeActivities.add(activeControllerManager
					.getActiveLiveActivity(liveActivity));
		}

		return activeActivities;
	}
}
