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

package interactivespaces.activity.impl;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.SupportedActivity;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.ActivityComponentCollection;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.util.concurrency.CommandCollection;
import interactivespaces.util.resource.ManagedResource;
import interactivespaces.util.resource.ManagedResourceCollection;

import java.util.Map;

/**
 * Support for building an Interactive Spaces activity.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseActivity extends ActivitySupport implements
		SupportedActivity {

	/**
	 * All components in the activity.
	 */
	private ActivityComponentCollection components = new ActivityComponentCollection();;

	/**
	 * Context all activity components will run under.
	 */
	private ActivityComponentContext componentContext;

	/**
	 * The collection of managed resources for the activity.
	 */
	private ManagedResourceCollection managedResources = new ManagedResourceCollection();

	/**
	 * The commands which are being managed.
	 */
	private CommandCollection managedCommands;

	/**
	 * Get one of the components for the activity.
	 * 
	 * @param componentName
	 *            the name of the component
	 * @return the component with the given name, or {@code null} if none
	 */
	public <T extends ActivityComponent> T getComponent(String componentName) {
		return components.getActivityComponent(componentName);
	}

	/**
	 * Add a new managed resource to the activity.
	 * 
	 * @param resource
	 *            the resource to add
	 */
	public void addManagedResource(ManagedResource resource) {
		managedResources.addResource(resource);
	}

	@Override
	public void updateConfiguration(Map<String, Object> update) {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivityConfigurationUpdate(update);
		} catch (Exception e) {
			logException("Failure when calling onActivityConfigurationUpdate",
					e);
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	@Override
	public void startup() {
		long beginStartTime = getSpaceEnvironment().getTimeProvider()
				.getCurrentTime();

		setActivityStatus(ActivityState.STARTUP_ATTEMPT, null);

		componentContext = new ActivityComponentContext(this, components,
				getController().getActivityComponentFactory());

		managedCommands = new CommandCollection(getSpaceEnvironment()
				.getExecutorService());

		boolean running = false;
		componentContext.lockRunningSet();
		try {
			commonActivitySetup();

			callOnActivitySetup();

			managedResources.startupResources(getLog());

			components
					.configureComponents(getConfiguration(), componentContext);
			components.startupComponents();

			callOnActivityStartup();

			setActivityStatus(ActivityState.RUNNING, null);
			running = true;

			if (getLog().isInfoEnabled()) {
				getLog().info(
						String.format(
								"Live activity %s running in %d milliseconds",
								getUuid(),
								(getSpaceEnvironment().getTimeProvider()
										.getCurrentTime() - beginStartTime)));
			}
		} catch (Exception e) {

			logException("Could not start activity up", e);

			setActivityStatus(ActivityState.STARTUP_FAILURE, null, e);
		} finally {
			componentContext.unlockRunningSet(running);
		}
	}

	/**
	 * Get the collection of managed commands.
	 * 
	 * @return the managed commands (will be {@code null} if the activity has
	 *         not been started, though will be available for any startup callbacks.
	 */
	public CommandCollection getManagedCommands() {
		return managedCommands;
	}

	/**
	 * Properly call {@link #onActivityStartup()}.
	 */
	private final void callOnActivitySetup() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivitySetup();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Properly call {@link #onActivityStartup()}.
	 */
	private final void callOnActivityStartup() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivityStartup();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	@Override
	public void handleStartupFailure() {
		try {
			callOnActivityStartupFailure();
		} finally {
			shutdown();
		}
	}

	/**
	 * Properly call {@link #onActivityStartupFailure}.
	 */
	private void callOnActivityStartupFailure() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivityStartupFailure();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Any activity specific handling of startup failures.
	 */
	public void onActivityStartupFailure() {
		// Default is to do nothing.
	}

	@Override
	public void activate() {
		try {
			callOnActivityActivate();

			setActivityStatus(ActivityState.ACTIVE, null);
		} catch (Exception e) {
			logException("Cannot activate activity", e);

			setActivityStatus(ActivityState.ACTIVATE_FAILURE, null);
		}
	}

	/**
	 * Properly call {@link #onActivityActivate()}.
	 */
	private void callOnActivityActivate() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();
		try {
			onActivityActivate();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Activate the activity.
	 */
	public void onActivityActivate() {
		// Default is to do nothing.
	}

	@Override
	public boolean isActivated() {
		return ActivityState.ACTIVE.equals(getActivityStatus().getState());
	}

	@Override
	public void deactivate() {
		try {
			callOnActivityDeactivate();

			setActivityStatus(ActivityState.RUNNING, null);
		} catch (Exception e) {
			logException("Cannot deactivate activity", e);

			setActivityStatus(ActivityState.DEACTIVATE_FAILURE, null);
		}
	}

	/**
	 * Properly call {@link #onActivityDeactivate()}.
	 */
	private void callOnActivityDeactivate() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();
		try {
			onActivityDeactivate();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Deactivate the activity.
	 */
	public void onActivityDeactivate() {
		// Default is to do nothing.
	}

	@Override
	public void shutdown() {
		componentContext.clearRunning();

		if (managedCommands != null) {
			managedCommands.shutdownAll();
			managedCommands = null;
		}

		boolean cleanShutdown = true;
		try {
			callOnActivityShutdown();
		} catch (Exception e) {
			logException("Error while calling onActivityShutdown", e);
			cleanShutdown = false;
		}

		try {
			callOnActivityCleanup();
		} catch (Exception e) {
			logException("Error while calling onActivityCleanup", e);
			cleanShutdown = false;
		}
		try {
			commonActivityCleanup();
		} catch (Exception e) {
			logException("Error while cleaning up common activity", e);
			cleanShutdown = false;
		}

		try {
			if (!components.shutdownAndClear()) {
				cleanShutdown = false;
			}
		} catch (Exception e) {
			logException("Error while shutting down activity components", e);
			cleanShutdown = false;
		}

		managedResources.shutdownResources(getLog());
		managedResources.clear();

		if (cleanShutdown) {
			setActivityStatus(ActivityState.READY, "Post clean shutdown");
		} else {
			setActivityStatus(ActivityState.SHUTDOWN_FAILURE,
					"Failures during shutdown");
		}
	}

	/**
	 * Properly call {@link #onActivityShutdown()}.
	 */
	private void callOnActivityShutdown() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivityShutdown();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Properly call {@link #onActivityCleanup()}.
	 */
	private void callOnActivityCleanup() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivityCleanup();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	@Override
	public void checkActivityState() {
		// If in startup, don't do a scan
		ActivityState state = getActivityStatus().getState();
		if (state == ActivityState.STARTUP_ATTEMPT || !state.isRunning()) {
			return;
		}

		boolean areAllComponentsRunning = components.areAllComponentsRunning();
		boolean callOnCheckActivityState = callOnCheckActivityState();
		if (!areAllComponentsRunning || !callOnCheckActivityState) {
			setActivityStatus(ActivityState.CRASHED,
					"Activity no longer running");
			getLog().error(
					String.format(
							"Activity marked as CRASHED, components stat %s, onCheckActivityState() %s",
							areAllComponentsRunning, callOnCheckActivityState));

			try {
				callOnActivityFailure();
			} catch (Exception e) {
				logException("Error while calling onActivityFailure", e);
			}
			try {
				callOnActivityCleanup();
			} catch (Exception e) {
				logException("Error while calling onActivityCleanup", e);
			}
			try {
				commonActivityCleanup();
			} catch (Exception e) {
				logException("Error while cleaning up activity", e);
			}
		}
	}

	/**
	 * Properly call {@link #onActivityShutdown()}.
	 */
	private void callOnActivityFailure() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			onActivityFailure();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Call {@link #onCheckActivityState()} properly
	 * 
	 * @return
	 */
	private boolean callOnCheckActivityState() {
		ActivityMethodInvocation invocation = getExecutionContext()
				.enterMethod();

		try {
			return onCheckActivityState();
		} finally {
			getExecutionContext().exitMethod(invocation);
		}
	}

	/**
	 * Setup any needed activity components and other startup.
	 * 
	 * <p>
	 * This method is not normally used by activity developers, they should
	 * install components in {@link #addActivityComponent(ActivityComponent)}.
	 * This allows a support base class to add in things unknown to the casual
	 * user.
	 */
	public void commonActivitySetup() {
		// Default is do nothing.
	}

	/**
	 * Cleanup any activity in support implementations.
	 * 
	 * <p>
	 * This method is not normally used by activity developers, they should
	 * clean up their activity in {@link #onActivityCleanup()}. This allows a
	 * support base class to add in things unknown to the casual user.
	 */
	public void commonActivityCleanup() {
		// Default is do nothing.
	}

	@Override
	public void onActivityConfigurationUpdate(Map<String, Object> update) {
		// Default is nothing on startup.
	}

	@Override
	public void onActivitySetup() {
		// Default is nothing on startup.
	}

	@Override
	public void onActivityStartup() {
		// Default is nothing on startup.
	}

	@Override
	public void onActivityShutdown() {
		// Default is nothing on on shutdown.
	}

	@Override
	public <T extends ActivityComponent> T addActivityComponent(T component) {
		components.addComponent(component);

		return component;
	}

	@Override
	public <T extends ActivityComponent> T addActivityComponent(
			String componentType) {
		ActivityComponentFactory componentFactory = componentContext
				.getComponentFactory();

		ActivityComponent component = componentFactory
				.newComponent(componentType);
		components.addComponent(component);

		@SuppressWarnings("unchecked")
		T c = (T) component;
		return c;
	}

	@Override
	public void addActivityComponents(String... componentTypes) {
		ActivityComponentFactory componentFactory = componentContext
				.getComponentFactory();

		for (String componentType : componentTypes) {
			components.addComponent(componentFactory
					.newComponent(componentType));
		}
	}

	@Override
	public boolean onCheckActivityState() {
		// Default is all is OK.
		return true;
	}

	@Override
	public void onActivityFailure() {
		// Default is to do nothing.
	}

	@Override
	public void onActivityCleanup() {
		// Default is to do nothing.
	}

	/**
	 * Get the component context.
	 * 
	 * @return the component context
	 */
	public ActivityComponentContext getActivityComponentContext() {
		return componentContext;
	}
}