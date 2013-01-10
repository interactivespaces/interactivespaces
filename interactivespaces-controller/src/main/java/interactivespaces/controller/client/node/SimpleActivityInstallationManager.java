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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.installation.ActivityInstallationListener;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.domain.ActivityInstallationStatus;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.Files;
import interactivespaces.util.web.HttpClientHttpContentCopier;
import interactivespaces.util.web.HttpContentCopier;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link ActivityInstallationManager}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleActivityInstallationManager implements
		ActivityInstallationManager {

	/**
	 * Configuration property giving the location of the activity staging
	 * directory.
	 */
	public static final String CONTROLLER_APPLICATION_STAGING_DIRECTORY_PROPERTY = "interactivespaces.controller.activity.staging.directory";

	/**
	 * The default folder for staging activity installs.
	 */
	private static final String CONTROLLER_APPLICATIONS_STAGING_DEFAULT = "controller/activities/staging";

	/**
	 * Mapping from UUID to the temporary file for an install.
	 */
	private Map<String, File> uuidToTemporary = new HashMap<String, File>();

	/**
	 * Base directory where files will be staged as they are copied in.
	 */
	private File stagingBaseDirectory;

	/**
	 * Copies files from the remote location.
	 */
	private HttpContentCopier remoteCopier = new HttpClientHttpContentCopier();

	/**
	 * The Interactive Spaces environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Local repository of controller information.
	 */
	private LocalSpaceControllerRepository controllerRepository;

	/**
	 * The storage manager for activities.
	 */
	private ActivityStorageManager activityStorageManager;

	/**
	 * The listeners for this installer.
	 */
	private List<ActivityInstallationListener> listeners = new ArrayList<ActivityInstallationListener>();

	public SimpleActivityInstallationManager(
			LocalSpaceControllerRepository controllerRepository,
			ActivityStorageManager activityStorageManager,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.controllerRepository = controllerRepository;
		this.activityStorageManager = activityStorageManager;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public void startup() {
		remoteCopier.startup();

		Configuration systemConfiguration = spaceEnvironment
				.getSystemConfiguration();
		stagingBaseDirectory = new File(spaceEnvironment.getFilesystem()
				.getInstallDirectory(), systemConfiguration.getPropertyString(
				CONTROLLER_APPLICATION_STAGING_DIRECTORY_PROPERTY,
				CONTROLLER_APPLICATIONS_STAGING_DEFAULT));
	}

	@Override
	public void shutdown() {
		remoteCopier.shutdown();
	}

	@Override
	public void copyActivity(String uuid, String uri) {
		String fileName = uuid + ".zip";

		File stagedLocation = null;
		synchronized (uuidToTemporary) {
			stagedLocation = uuidToTemporary.get(uuid);
			if (stagedLocation != null)
				throw new InteractiveSpacesException(
						"UUID already being copied: " + uuid);

			stagedLocation = new File(stagingBaseDirectory, fileName);
			uuidToTemporary.put(uuid, stagedLocation);
		}

		remoteCopier.copy(uri, stagedLocation);
	}

	@Override
	public Date installActivity(String uuid, String activityIdentifyingName,
			String version) {
		File stagedLocation = null;
		synchronized (uuidToTemporary) {
			stagedLocation = uuidToTemporary.get(uuid);
			if (stagedLocation == null)
				throw new InteractiveSpacesException(
						"No staged file with given UUID: " + uuid);
		}

		ActivityFilesystem activityFilesystem = activityStorageManager
				.getActivityFilesystem(uuid);

		File installDirectory = activityFilesystem.getInstallDirectory();
		Files.deleteDirectoryContents(installDirectory);
		Files.unzip(stagedLocation, installDirectory);

		Date installedDate = persistInstallation(uuid, activityIdentifyingName,
				version, activityStorageManager.getBaseActivityLocation(uuid));

		spaceEnvironment.getLog().info(
				String.format("Activity %s version %s installed with uuid %s",
						activityIdentifyingName, version, uuid));

		notifyInstalledActivity(uuid);

		return installedDate;
	}

	/**
	 * Persist information about the installation.
	 * 
	 * @param uuid
	 *            UUID of the installed activity
	 * @param identifyingName
	 *            identifying name of the installed activity
	 * @param version
	 *            version of the installed activity
	 * @param baseInstallationLocation
	 *            the root folder of the installation
	 */
	private Date persistInstallation(String uuid, String identifyingName,
			String version, File baseInstallationLocation) {
		// Make sure the app is only stored once.
		InstalledLiveActivity activity = controllerRepository
				.getInstalledLiveActivityByUuid(uuid);
		if (activity == null) {
			activity = controllerRepository.newInstalledLiveActivity();
		}
		Date installedDate = new Date(spaceEnvironment.getTimeProvider()
				.getCurrentTime());

		activity.setUuid(uuid);
		activity.setIdentifyingName(identifyingName);
		activity.setVersion(version);
		activity.setBaseInstallationLocation(baseInstallationLocation
				.getAbsolutePath());
		activity.setLastDeployedDate(installedDate);
		activity.setInstallationStatus(ActivityInstallationStatus.OK);

		controllerRepository.saveInstalledLiveActivity(activity);

		return installedDate;
	}

	@Override
	public boolean removeActivity(String uuid) {
		// TODO(keith): Move this elsewhere
		InstalledLiveActivity activity = controllerRepository
				.getInstalledLiveActivityByUuid(uuid);
		if (activity != null) {
			controllerRepository.deleteInstalledLiveActivity(activity);

			activityStorageManager.removeActivityLocation(uuid);

			notifyRemovedActivity(uuid);

			return true;
		}

		return false;
	}

	@Override
	public void removePackedActivity(String uuid) {
		File stagedLocation = null;
		synchronized (uuidToTemporary) {
			stagedLocation = uuidToTemporary.remove(uuid);
		}

		if (stagedLocation != null) {
			if (!stagedLocation.delete()) {
				spaceEnvironment.getLog().warn(
						String.format(
								"Could not delete staged file %s for UUID %s",
								stagedLocation, uuid));
			}
		} else {
			spaceEnvironment.getLog().warn(
					String.format("No staged file with UUID %s", uuid));
		}
	}

	/**
	 * Notify everyone who needs to know that an activity has been installed.
	 * 
	 * @param uuid
	 *            UUID of the installed activity.
	 */
	private void notifyInstalledActivity(String uuid) {
		for (ActivityInstallationListener listener : getListeners()) {
			listener.onActivityInstall(uuid);
		}
	}

	/**
	 * Notify everyone who needs to know that an activity has been removed.
	 * 
	 * @param uuid
	 *            UUID of the installed activity.
	 */
	private void notifyRemovedActivity(String uuid) {
		for (ActivityInstallationListener listener : getListeners()) {
			listener.onActivityRemove(uuid);
		}
	}

	@Override
	public void addActivityInstallationListener(
			ActivityInstallationListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeActivityInstallationListener(
			ActivityInstallationListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Obtain a fresh copy of the listener list in a thread-safe manner.
	 * 
	 * @return
	 */
	private List<ActivityInstallationListener> getListeners() {
		synchronized (listeners) {
			return new ArrayList<ActivityInstallationListener>(listeners);
		}
	}
}
