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

package interactivespaces.controller.client.node;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStateTransition;
import interactivespaces.activity.ActivityStateTransition.TransitionResult;
import interactivespaces.activity.ActivityStateTransitioner;
import interactivespaces.activity.ActivityStateTransitionerCollection;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.component.CoreExistingActivityComponentFactory;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.ActivityConfigurationManager;
import interactivespaces.controller.activity.configuration.SimpleActivityConfiguration;
import interactivespaces.controller.activity.installation.ActivityInstallationListener;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.client.node.ros.SpaceControllerActivityWatcher;
import interactivespaces.controller.client.node.ros.SpaceControllerActivityWatcherListener;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.logging.ActivityLogFactory;
import interactivespaces.controller.logging.AlertStatusManager;
import interactivespaces.controller.logging.SimpleAlertStatusManager;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.uuid.JavaUuidGenerator;
import interactivespaces.util.uuid.UuidGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

/**
 * A base implementation of {@link SpaceController} which gives basic
 * implementation. Does not supply communication for the remote master.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseSpaceController implements SpaceController,
		SpaceControllerActivityWatcherListener {

	/**
	 * The default number of milliseconds the activity activityWatcher thread
	 * delays between scans.
	 */
	private static final int WATCHER_DELAY_DEFAULT = 1000;

	/**
	 * The default number of milliseconds the heartbeat thread delays between
	 * beats.
	 */
	public static final int HEARTBEAT_DELAY_DEFAULT = 10000;

	/**
	 * A live activity status for installed live activities that currently
	 * aren't running.
	 */
	private static final ActivityStatus LIVE_ACTIVITY_READY_STATUS = new ActivityStatus(
			ActivityState.READY, null);

	/**
	 * The heartbeatLoop for this controller.
	 */
	private ControllerHeartbeat heartbeat;

	/**
	 * Control for the heartbeat.
	 */
	private ScheduledFuture<?> heartbeatControl;

	/**
	 * Number of milliseconds the heartbeatLoop waits before each beat.
	 */
	private long heartbeatDelay = HEARTBEAT_DELAY_DEFAULT;

	/**
	 * All activities in this controller, indexed by UUID.
	 */
	protected Map<String, ActiveControllerActivity> activities = Maps
			.newHashMap();

	/**
	 * Watches activities for this controller.
	 */
	private SpaceControllerActivityWatcher activityWatcher;

	/**
	 * Control for the activity watcher.
	 */
	private ScheduledFuture<?> activityWatcherControl;

	/**
	 * Number of milliseconds the activityWatcher waits before scanning for
	 * activity state.
	 */
	protected long activityWatcherDelay = WATCHER_DELAY_DEFAULT;

	/**
	 * For important alerts worthy of paging, etc.
	 */
	protected AlertStatusManager alertStatusManager;

	/**
	 * Receives activities deployed to the controller.
	 */
	protected ActivityInstallationManager activityInstallationManager;

	/**
	 * A loader for container activities.
	 */
	protected ActiveControllerActivityFactory activeControllerActivityFactory;

	/**
	 * Local repository of controller information.
	 */
	protected LocalSpaceControllerRepository controllerRepository;

	/**
	 * A factory for native app runners.
	 */
	protected NativeActivityRunnerFactory nativeActivityRunnerFactory;

	/**
	 * The configuration manager for activities.
	 */
	protected ActivityConfigurationManager configurationManager;

	/**
	 * The component factory to be used by this controller.
	 */
	protected ActivityComponentFactory activityComponentFactory;

	/**
	 * Log factory for activities.
	 */
	private ActivityLogFactory activityLogFactory;

	/**
	 * The Interactive Spaces environment being run under.
	 */
	protected InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * The Interactive Spaces system controller.
	 */
	protected InteractiveSpacesSystemControl spaceSystemControl;

	/**
	 * Information about the controller
	 */
	private SimpleSpaceController controllerInfo = new SimpleSpaceController();

	/**
	 * The storage manager for activities.
	 */
	private ActivityStorageManager activityStorageManager;

	/**
	 * All activity state transitioners.
	 */
	private ActivityStateTransitionerCollection activityStateTransitioners;

	/**
	 * A listener for installation events.
	 */
	private ActivityInstallationListener activityInstallationListener = new ActivityInstallationListener() {
		@Override
		public void onActivityInstall(String uuid) {
			handleActivityInstall(uuid);
		}

		@Override
		public void onActivityRemove(String uuid) {
			handleActivityRemove(uuid);
		}
	};

	/**
	 * A listener for activity events.
	 */
	private ActivityListener activityListener = new ActivityListener() {
		@Override
		public void onActivityStatusChange(Activity activity,
				ActivityStatus oldStatus, ActivityStatus newStatus) {
			handleActivityListenerOnActivityStatusChange(activity, oldStatus,
					newStatus);
		}
	};

	/**
	 * The sequential event queue to be used for controller events.
	 */
	private SequentialEventQueue eventQueue;

	/**
	 * {@code true} if the controller was started up.
	 */
	private volatile boolean startedUp = false;

	@Override
	public void startup() {
		spaceEnvironment.getLog().info("Controller starting up");

		obtainControllerInfo();
		confirmUuid();

		activityComponentFactory = new CoreExistingActivityComponentFactory();
		activityStateTransitioners = new ActivityStateTransitionerCollection();

		// TODO(keith): Set this container-wide.
		eventQueue = new SequentialEventQueue(getSpaceEnvironment(),
				getSpaceEnvironment().getLog());
		eventQueue.startup();

		// TODO(keith): Set this container-wide.
		alertStatusManager = new SimpleAlertStatusManager();

		heartbeat = newControllerHeartbeat();
		getSpaceEnvironment().getExecutorService().scheduleAtFixedRate(
				new Runnable() {
					@Override
					public void run() {
						heartbeat.heartbeat();
					}
				}, heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);

		activityWatcher = new SpaceControllerActivityWatcher(spaceEnvironment);
		activityWatcher.addListener(this);
		activityWatcherControl = getSpaceEnvironment().getExecutorService()
				.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						activityWatcher.scan();
					}
				}, activityWatcherDelay, activityWatcherDelay,
						TimeUnit.MILLISECONDS);

		activityInstallationManager
				.addActivityInstallationListener(activityInstallationListener);

		onStartup();

		startupAutostartActivities();

		notifyRemoteMasterServerAboutStartup(controllerInfo);

		startedUp = true;

		// Make sure we shutdown all activities properly.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
	}

	/**
	 * Got a status change on an activity from the activity.
	 * 
	 * @param activity
	 *            the activity whose status changed
	 * @param oldStatus
	 *            the old status
	 * @param newStatus
	 *            the new status
	 */
	private void handleActivityListenerOnActivityStatusChange(
			final Activity activity, ActivityStatus oldStatus,
			final ActivityStatus newStatus) {
		publishActivityStatus(activity.getUuid(), newStatus);

		// TODO(keith): Android hates garbage collection. This may need an
		// object pool.
		eventQueue.addEvent(new Runnable() {
			@Override
			public void run() {
				activityStateTransitioners.transition(activity.getUuid(),
						newStatus.getState());
			}
		});
	}

	/**
	 * Make sure the controller had a UUID. If not, generate one.
	 */
	private void confirmUuid() {
		String uuid = controllerInfo.getUuid();
		if (uuid == null || uuid.trim().isEmpty()) {
			UuidGenerator uuidGenerator = new JavaUuidGenerator();
			uuid = uuidGenerator.newUuid();
			controllerInfo.setUuid(uuid);

			spaceEnvironment.getLog().warn(
					String.format(
							"No controller UUID found, generated UUID is %s",
							uuid));

			persistControllerInfo();
		}
	}

	/**
	 * Save the controller information in the configurations.
	 */
	private void persistControllerInfo() {
		Properties props = new Properties();
		props.put(CONFIGURATION_CONTROLLER_UUID, controllerInfo.getUuid());
		props.put(CONFIGURATION_CONTROLLER_NAME, controllerInfo.getName());
		props.put(CONFIGURATION_CONTROLLER_DESCRIPTION,
				controllerInfo.getDescription());

		File controllerInfoFile = new File(spaceEnvironment.getFilesystem()
				.getInstallDirectory(),
				"config/interactivespaces/controllerinfo.conf");
		FileWriter writer = null;
		try {
			writer = new FileWriter(controllerInfoFile);
			props.store(writer, "Autogenerated UUID");

			writer.flush();
			spaceEnvironment.getLog().info(
					String.format("Persisted new controller information to %s",
							controllerInfoFile.getAbsolutePath()));
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					String.format("Error while persisting %s",
							controllerInfoFile.getAbsolutePath()), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// Don't care
				}
			}
		}
	}

	/**
	 * Get controller information from the configs.
	 */
	protected void obtainControllerInfo() {
		Configuration systemConfiguration = spaceEnvironment
				.getSystemConfiguration();

		controllerInfo.setUuid(systemConfiguration
				.getPropertyString(CONFIGURATION_CONTROLLER_UUID));
		controllerInfo.setName(systemConfiguration.getPropertyString(
				CONFIGURATION_CONTROLLER_NAME, ""));
		controllerInfo.setDescription(systemConfiguration.getPropertyString(
				CONFIGURATION_CONTROLLER_DESCRIPTION, ""));
		controllerInfo
				.setHostId(systemConfiguration
						.getRequiredPropertyString(InteractiveSpacesEnvironment.CONFIGURATION_HOSTID));
	}

	/**
	 * Notify the master that the controller has started.
	 */
	protected abstract void notifyRemoteMasterServerAboutStartup(
			SimpleSpaceController controllerInfo);

	/**
	 * The controller is starting up.
	 * 
	 * <p>
	 * This is a chance for subclasses to do any additional startup
	 */
	protected void onStartup() {
		// Default is nothing to do.
	}

	@Override
	public void shutdown() {
		if (startedUp) {
			try {
				spaceEnvironment.getLog().info("Controller shutting down");

				activityStateTransitioners.clear();

				shutdownAllActivities();

				heartbeatControl.cancel(true);
				heartbeatControl = null;

				activityWatcherControl.cancel(true);
				activityWatcherControl = null;
				activityWatcher = null;

				eventQueue.shutdown();
				eventQueue = null;

				onShutdown();
			} finally {
				startedUp = false;
			}
		}
	}

	/**
	 * controller is shutting down.
	 * 
	 * <p>
	 * This is a chance for subclasses to do their specific shutdown.
	 */
	protected void onShutdown() {
		// Default is nothing to do.
	}

	@Override
	public ActivityComponentFactory getActivityComponentFactory() {
		return activityComponentFactory;
	}

	/**
	 * Prepare an instance to run.
	 * 
	 * @param activity
	 *            information about the activity whose instance is to be
	 *            initialized (think of as the class description.
	 * @param activityFilesystem
	 *            the filesystem for the activity instance
	 * @param instance
	 *            the instance of the activity being started up
	 * @param configuration
	 *            the configuration for the instance
	 * @param executionContext
	 *            the context for executing the activity in
	 */
	public void initializeInstance(InstalledLiveActivity activity,
			ActivityFilesystem activityFilesystem, Activity instance,
			Configuration configuration,
			ActivityExecutionContext executionContext) {
		String uuid = activity.getUuid();
		instance.setController(this);
		instance.setUuid(uuid);

		instance.setConfiguration(configuration);
		instance.setActivityFilesystem(activityFilesystem);
		instance.setSpaceEnvironment(spaceEnvironment);
		instance.setLog(activityLogFactory.createLogger(activity, configuration
				.getPropertyString(Activity.CONFIGURATION_PROPERTY_LOG_LEVEL,
						InteractiveSpacesEnvironment.LOG_LEVEL_ERROR)));
		instance.setExecutionContext(executionContext);
		instance.addActivityListener(getActivityListener());

		initializeConfiguration(configuration, activityFilesystem);

		specificInstanceInitialization(instance);
	}

	/**
	 * Startup the activities that need to start up when the controller starts.
	 */
	private void startupAutostartActivities() {
		for (InstalledLiveActivity activity : controllerRepository
				.getAllInstalledLiveActivities()) {
			switch (activity.getControllerStartupType()) {
			case STARTUP:
				startupActivity(activity.getUuid());
				break;
			case ACTIVATE:
				activateActivity(activity.getUuid());
				break;
			}
		}
	}

	/**
	 * Initialize the configuration with any special values needed for running.
	 * 
	 * @param configuration
	 *            the configuration to be modified
	 * @param activityFilesystem
	 *            the activities file system
	 */
	private void initializeConfiguration(Configuration configuration,
			ActivityFilesystem activityFilesystem) {
		configuration.setValue("activity.installdir", activityFilesystem
				.getInstallDirectory().getAbsolutePath());
		configuration.setValue("activity.logdir", activityFilesystem
				.getLogDirectory().getAbsolutePath());
		configuration.setValue("activity.datadir", activityFilesystem
				.getPermanentDataDirectory().getAbsolutePath());
		configuration.setValue("activity.tmpdir", activityFilesystem
				.getTempDataDirectory().getAbsolutePath());
		InteractiveSpacesFilesystem filesystem = spaceEnvironment
				.getFilesystem();
		configuration.setValue("system.datadir", filesystem.getDataDirectory()
				.getAbsolutePath());
		configuration.setValue("system.tmpdir", filesystem.getTempDirectory()
				.getAbsolutePath());
	}

	/**
	 * Perform any additional instance initialization needed.
	 * 
	 * @param instance
	 *            The activity instance to initialize
	 */
	public void specificInstanceInitialization(Activity instance) {
		// Default is nothing
	}

	@Override
	public InteractiveSpacesEnvironment getSpaceEnvironment() {
		return spaceEnvironment;
	}

	@Override
	public void startupAllActivities() {
		for (ActiveControllerActivity app : getAllActiveActivities()) {
			attemptActivityStartup(app);
		}
	}

	@Override
	public void shutdownAllActivities() {
		spaceEnvironment.getLog().info("Shutting down all activities");

		for (ActiveControllerActivity app : getAllActiveActivities()) {
			attemptActivityShutdown(app);
		}
	}

	@Override
	public void startupActivity(String uuid) {
		spaceEnvironment.getLog().info(
				String.format("Starting up activity %s", uuid));

		ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
		if (activity != null) {
			if (!activity.getCachedActivityStatus().getState().isRunning()) {
				activityWatcher.watchActivity(activity);
				attemptActivityStartup(activity);
			}
		} else {
			spaceEnvironment.getLog().warn(
					String.format("Activity %s does not exist on controller",
							uuid));
		}
	}

	@Override
	public void shutdownActivity(String uuid) {
		spaceEnvironment.getLog().info(
				String.format("Shutting down activity %s", uuid));

		ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
		if (activity != null) {
			attemptActivityShutdown(activity);
		} else {
			InstalledLiveActivity ia = controllerRepository
					.getInstalledLiveActivityByUuid(uuid);
			if (ia != null) {
				publishActivityStatus(uuid, LIVE_ACTIVITY_READY_STATUS);
			} else {
				spaceEnvironment.getLog().warn(
						String.format(
								"Activity %s does not exist on controller",
								uuid));
			}
		}
	}

	@Override
	public void statusActivity(String uuid) {
		spaceEnvironment.getLog().info(
				String.format("Getting status of activity %s", uuid));

		ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
		if (activity != null) {
			ActivityStatus activityStatus = activity.getActivityStatus();
			spaceEnvironment.getLog().info(
					String.format("Reporting activity status %s for %s", uuid,
							activityStatus));
			publishActivityStatus(activity.getUuid(), activityStatus);
		} else {
			InstalledLiveActivity liveActivity = controllerRepository
					.getInstalledLiveActivityByUuid(uuid);
			if (liveActivity != null) {
				spaceEnvironment.getLog().info(
						String.format("Reporting activity status %s for %s",
								uuid, LIVE_ACTIVITY_READY_STATUS));
				publishActivityStatus(uuid, LIVE_ACTIVITY_READY_STATUS);
			} else {
				spaceEnvironment.getLog().warn(
						String.format(
								"Activity %s does not exist on controller",
								uuid));
			}
		}
	}

	@Override
	public void activateActivity(String uuid) {
		spaceEnvironment.getLog().info(
				String.format("Activating activity %s", uuid));

		// Can create since can immediately request activate
		ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
		if (activity != null) {
			attemptActivityActivate(activity);
		} else {
			spaceEnvironment.getLog().warn(
					String.format("Activity %s does not exist on controller",
							uuid));
		}
	}

	@Override
	public void deactivateActivity(String uuid) {
		spaceEnvironment.getLog().info(
				String.format("Deactivating activity %s", uuid));

		ActiveControllerActivity activity = getActiveActivityByUuid(uuid, false);
		if (activity != null) {
			attemptActivityDeactivate(activity);
		} else {
			spaceEnvironment.getLog().warn(
					String.format("Activity %s does not exist on controller",
							uuid));
		}
	}

	/**
	 * Configure the activity.
	 * 
	 * @param uuid
	 *            uuid of the activity
	 * @param configuration
	 *            the configuration request
	 */
	protected void configureActivity(String uuid,
			Map<String, Object> configuration) {
		spaceEnvironment.getLog().info(
				String.format("Configuring activity %s", uuid));

		ActiveControllerActivity activity = getActiveActivityByUuid(uuid, true);
		if (activity != null) {
			activity.updateConfiguration(configuration);
		} else {
			spaceEnvironment.getLog().warn(
					String.format("Activity %s does not exist on controller",
							uuid));
		}
	}

	@Override
	public NativeActivityRunnerFactory getNativeActivityRunnerFactory() {
		return nativeActivityRunnerFactory;
	}

	/**
	 * Get an activity by UUID.
	 * 
	 * @param uuid
	 *            The UUID of the activity.
	 * @param create
	 *            True if should create the activity entry from the controller
	 *            repository if none found, false otherwise.
	 * 
	 * @return The activity with the given UUID. null if no such activity.
	 */
	protected ActiveControllerActivity getActiveActivityByUuid(String uuid,
			boolean create) {
		ActiveControllerActivity activity = null;
		synchronized (activities) {
			activity = activities.get(uuid);
			if (activity == null && create) {
				activity = createActivityFromRepository(uuid);

				if (activity != null) {
					activities.put(uuid, activity);
				}
			}
		}

		if (activity == null)
			spaceEnvironment.getLog().warn(
					String.format(
							"Could not find active live activity with uuid %s",
							uuid));

		return activity;
	}

	/**
	 * Create an activity from the repository.
	 * 
	 * @param uuid
	 *            UUID of the activity to create
	 * 
	 * @return The active app with a runner.
	 */
	protected ActiveControllerActivity createActivityFromRepository(String uuid) {
		InstalledLiveActivity liveActivity = controllerRepository
				.getInstalledLiveActivityByUuid(uuid);
		if (liveActivity != null) {
			ActivityFilesystem activityFilesystem = activityStorageManager
					.getActivityFilesystem(uuid);

			SimpleActivityConfiguration activityConfiguration = configurationManager
					.getConfiguration(activityFilesystem);
			activityConfiguration.load();

			ActiveControllerActivity activity = activeControllerActivityFactory
					.createActiveActivity(liveActivity, activityFilesystem,
							activityConfiguration, this);

			return activity;
		} else {
			return null;
		}
	}

	/**
	 * Start up an activity.
	 * 
	 * @param activity
	 *            The activity to start up.
	 */
	private void attemptActivityStartup(ActiveControllerActivity activity) {
		spaceEnvironment.getLog().info(
				String.format("Attempting startup of activity %s",
						activity.getUuid()));

		try {
			switch (activity.getActivityState()) {
			case STARTUP_FAILURE:
			case SHUTDOWN_FAILURE:
			case CRASHED:
				// If crashed, try a shutdown first.
				activity.shutdown();

			case READY:
				activity.startup();

				break;

			default:
				reportIllegalActivityStateTransition(activity,
						ActivityStateTransition.STARTUP);
			}
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					String.format("Unable to start activity %s",
							activity.getUuid()), e);
		}
	}

	/**
	 * Attempt to shut an activity down.
	 * 
	 * @param activity
	 *            the activity to shutdown
	 */
	private void attemptActivityShutdown(ActiveControllerActivity activity) {
		ActivityStateTransition transition = ActivityStateTransition.SHUTDOWN;
		if (transition.attemptTransition(activity.getActivityState(), activity)
				.equals(TransitionResult.ILLEGAL)) {
			reportIllegalActivityStateTransition(activity, transition);
		}
	}

	/**
	 * Attempt to activate an activity.
	 * 
	 * @param activity
	 *            The app to activate.
	 */
	private void attemptActivityActivate(ActiveControllerActivity activity) {
		switch (activity.getActivityState()) {
		case RUNNING:
		case ACTIVATE_FAILURE:
			activity.activate();

			break;

		case ACTIVE:
			// Nothing to do.
			break;

		case READY:
			setupActiveTarget(activity);
			break;

		default:
			reportIllegalActivityStateTransition(activity,
					ActivityStateTransition.ACTIVATE);
		}
	}

	/**
	 * Need to set up a target of going to active after a startup.
	 * 
	 * <p>
	 * This will start moving towards the goal.
	 * 
	 * @param activity
	 *            the activity to go to startup
	 */
	private void setupActiveTarget(final ActiveControllerActivity activity) {
		final String uuid = activity.getUuid();
		activityStateTransitioners.addTransitioner(
				uuid,
				new ActivityStateTransitioner(activity,
						ActivityStateTransitioner.transitions(
								ActivityStateTransition.STARTUP,
								ActivityStateTransition.ACTIVATE),
						spaceEnvironment.getLog()));

		// TODO(keith): Android hates garbage collection. This may need an
		// object pool.
		eventQueue.addEvent(new Runnable() {
			@Override
			public void run() {
				activityStateTransitioners.transition(uuid,
						activity.getActivityState());
			}
		});
	}

	/**
	 * Attempt to deactivate an activity.
	 * 
	 * @param activity
	 *            The app to deactivate.
	 */
	private void attemptActivityDeactivate(ActiveControllerActivity activity) {
		switch (activity.getActivityState()) {
		case ACTIVE:
		case DEACTIVATE_FAILURE:
			activity.deactivate();
			break;

		default:
			reportIllegalActivityStateTransition(activity,
					ActivityStateTransition.DEACTIVATE);
		}
	}

	/**
	 * Attempted an activity transition and it couldn't take place.
	 * 
	 * @param activity
	 *            the activity that was being transitioned
	 * @param attemptedChange
	 *            where the activity was going
	 */
	private void reportIllegalActivityStateTransition(
			ActiveControllerActivity activity,
			ActivityStateTransition attemptedChange) {
		spaceEnvironment.getLog().error(
				String.format("Tried to %s activity %s, was in state %s\n",
						attemptedChange, activity.getUuid(), activity
								.getActivityStatus().toString()));
	}

	/**
	 * Get a list of all activities running in the controller.
	 * 
	 * <p>
	 * Returned in no particular order. A new collection is made each time.
	 * 
	 * @return All activities running in the controller.
	 */
	public Collection<ActiveControllerActivity> getAllActiveActivities() {
		// TODO(keith): Think about how this should be in the controller.
		synchronized (activities) {
			return new ArrayList<ActiveControllerActivity>(activities.values());
		}
	}

	@Override
	public void onActivityError(ActiveControllerActivity activity,
			ActivityStatus oldStatus, ActivityStatus newStatus) {
		publishActivityStatus(activity.getUuid(), newStatus);

		// TODO(keith): need more nuance here.
		if (oldStatus.getState() == ActivityState.READY
				&& newStatus.getState() == ActivityState.STARTUP_FAILURE) {
			handleActivityCantStart(activity);
		}
	}

	@Override
	public void onActivityStatusChange(ActiveControllerActivity activity,
			ActivityStatus oldStatus, ActivityStatus newStatus) {
		publishActivityStatus(activity.getUuid(), newStatus);
	}

	/**
	 * Publish the state of an activity on the activity status topic.
	 * 
	 * @param uuid
	 *            the UUID of the live activity
	 * @param newStatus
	 *            the status to be sent
	 */
	protected abstract void publishActivityStatus(String uuid,
			ActivityStatus newStatus);

	/**
	 * An activity was unable to start up.
	 * 
	 * @param activity
	 *            the problematic activity
	 */
	private void handleActivityCantStart(ActiveControllerActivity activity) {
		Activity instance = activity.getInstance();
		alertStatusManager.announceStatus(activity);

		// Need better policy, for now, just clean up app and we will let the
		// controller
		// handle it.
		instance.handleStartupFailure();

		alertStatusManager.announceStatus(activity);
	}

	@Override
	public SimpleSpaceController getControllerInfo() {
		return controllerInfo;
	}

	/**
	 * Create a {@link ControllerHeartbeat} appropriate for the controller.
	 * 
	 * @return
	 */
	protected abstract ControllerHeartbeat newControllerHeartbeat();

	/**
	 * The activity installer is signaling an install.
	 * 
	 * @param uuid
	 *            UUID of the installed activity.
	 */
	private void handleActivityInstall(String uuid) {
		// Nothing to do right now
	}

	/**
	 * The activity installer is signaling a removal.
	 * 
	 * @param uuid
	 *            UUID of the installed activity.
	 */
	private void handleActivityRemove(String uuid) {
		spaceEnvironment.getLog().info(
				String.format("Removed activity %s", uuid));
	}

	/**
	 * Get the space controller's activity listener.
	 * 
	 * <p>
	 * This is used for event transmission to the master, among other things.
	 * 
	 * @return the activity listener
	 */
	public ActivityListener getActivityListener() {
		return activityListener;
	}

	/**
	 * Set the activity deployer the controller should use.
	 * 
	 * @param activityInstallationManager
	 *            the activityInstallationManager to set
	 */
	public void setActivityInstallationManager(
			ActivityInstallationManager activityInstallationManager) {
		this.activityInstallationManager = activityInstallationManager;
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			LocalSpaceControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}

	/**
	 * @param activeControllerActivityFactory
	 *            the activeControllerActivityFactory to set
	 */
	public void setActiveControllerActivityFactory(
			ActiveControllerActivityFactory activeControllerActivityFactory) {
		this.activeControllerActivityFactory = activeControllerActivityFactory;
	}

	/**
	 * @param nativeActivityRunnerFactory
	 *            the nativeActivityRunnerFactory to set
	 */
	public void setNativeActivityRunnerFactory(
			NativeActivityRunnerFactory nativeAppRunnerFactory) {
		this.nativeActivityRunnerFactory = nativeAppRunnerFactory;
	}

	/**
	 * @param configurationManager
	 *            the configurationManager to set
	 */
	public void setConfigurationManager(
			ActivityConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	/**
	 * @param activityStorageManager
	 *            the activityStorageManager to set
	 */
	public void setActivityStorageManager(
			ActivityStorageManager activityStorageManager) {
		this.activityStorageManager = activityStorageManager;
	}

	/**
	 * @param activityLogFactory
	 *            the activityLogFactory to set
	 */
	public void setActivityLogFactory(ActivityLogFactory activityLogFactory) {
		this.activityLogFactory = activityLogFactory;
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
	 * @param spaceSystemControl
	 *            the spaceSystemControl to set
	 */
	public void setSpaceSystemControl(
			InteractiveSpacesSystemControl spaceSystemControl) {
		this.spaceSystemControl = spaceSystemControl;
	}

	/**
	 * Give heartbeats from controller.
	 * 
	 * @author Keith M. Hughes
	 */
	public interface ControllerHeartbeat {

		/**
		 * Send the heartbeat.
		 */
		void heartbeat();
	}
}
