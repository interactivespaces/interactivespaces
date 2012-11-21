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

package interactivespaces.controller.repository.internal.file;

import interactivespaces.activity.ActivityControllerStartupType;
import interactivespaces.activity.ActivityState;
import interactivespaces.controller.client.node.ActivityStorageManager;
import interactivespaces.controller.domain.ActivityInstallationStatus;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.Files;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Controller repository using Db4o.
 * 
 * @author Keith M. Hughes
 */
public class FileLocalSpaceControllerRepository implements
		LocalSpaceControllerRepository {

	/**
	 * Name of the data file for the activity data.
	 */
	public static final String DATAFILE_NAME = "activity.data";

	/**
	 * The storage manager for activities.
	 */
	private ActivityStorageManager activityStorageManager;

	/**
	 * A map of all activities by their UUID.
	 */
	private Map<String, SimpleInstalledLiveActivity> activitiesByUuid = Maps
			.newHashMap();

	/**
	 * The space environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * The lock for protecting reads and writes.
	 */
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	public FileLocalSpaceControllerRepository(
			ActivityStorageManager activityStorageManager,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.activityStorageManager = activityStorageManager;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public void startup() {
		// Treat this as a write
		lock.writeLock().lock();
		try {
			for (String uuid : activityStorageManager
					.getAllInstalledActivityUuids()) {
				SimpleInstalledLiveActivity activity = readDataFile(uuid);
				if (activity != null) {
					activitiesByUuid.put(activity.getUuid(), activity);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void shutdown() {
		// Nothing to do
	}

	@Override
	public InstalledLiveActivity newInstalledLiveActivity() {
		return new SimpleInstalledLiveActivity();
	}

	@Override
	public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
		lock.readLock().lock();
		try {
			List<InstalledLiveActivity> result = Lists.newArrayList();
			for (SimpleInstalledLiveActivity activity : activitiesByUuid
					.values()) {
				result.add(new SimpleInstalledLiveActivity(activity));
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public InstalledLiveActivity getInstalledLiveActivityByUuid(String uuid) {
		lock.readLock().lock();
		try {
			SimpleInstalledLiveActivity activity = activitiesByUuid.get(uuid);
			if (activity != null) {
				return new SimpleInstalledLiveActivity(activity);
			} else {
				return null;
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public InstalledLiveActivity saveInstalledLiveActivity(
			InstalledLiveActivity activity) {
		lock.writeLock().lock();
		try {
			writeDataFile(activity);
			activitiesByUuid.put(activity.getUuid(),
					new SimpleInstalledLiveActivity(activity));

			return activity;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void deleteInstalledLiveActivity(InstalledLiveActivity activity) {
		lock.writeLock().lock();
		try {
			String uuid = activity.getUuid();
			SimpleInstalledLiveActivity sactivity = activitiesByUuid.get(uuid);
			if (sactivity != null) {
				activitiesByUuid.remove(uuid);
				getActivityDataFile(uuid).delete();
			} else {
				spaceEnvironment
						.getLog()
						.warn(String
								.format("Attempt to delete installed activity %s which is doesn't seem to exist",
										activitiesByUuid));
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Read the datafile for an activity.
	 * 
	 * @param uuid
	 *            UUID of the activity
	 * 
	 * @return the activity, or {@code null} if there was no data file
	 */
	private SimpleInstalledLiveActivity readDataFile(String uuid) {
		File datafile = getActivityDataFile(uuid);
		if (datafile.isFile()) {
			SimpleInstalledLiveActivity activity = new SimpleInstalledLiveActivity();

			String contents = Files.readFile(datafile);
			String[] lines = contents.split("\\n");

			activity.setUuid(uuid);
			activity.setBaseInstallationLocation(activityStorageManager
					.getBaseActivityLocation(uuid).getAbsolutePath());
			activity.setIdentifyingName(lines[0]);
			activity.setVersion(lines[1]);
			activity.setLastDeployedDate(new Date(Long.parseLong(lines[2])));
			activity.setLastActivityState(ActivityState.valueOf(lines[3]));
			activity.setInstallationStatus(ActivityInstallationStatus
					.valueOf(lines[4]));
			activity.setControllerStartupType(ActivityControllerStartupType
					.valueOf(lines[5]));

			return activity;
		} else {
			spaceEnvironment.getLog().warn(
					String.format(
							"Found activity folder with UUID %s. No data file",
							uuid));
			return null;
		}
	}

	/**
	 * write the datafile for an activity.
	 * 
	 * @param activity
	 *            the activity to write
	 */
	private void writeDataFile(InstalledLiveActivity activity) {
		StringBuilder contents = new StringBuilder();
		contents.append(activity.getIdentifyingName()).append("\n")
				.append(activity.getVersion()).append("\n")
				.append(activity.getLastDeployedDate().getTime()).append("\n")
				.append(activity.getLastActivityState().toString())
				.append("\n")
				.append(activity.getInstallationStatus().toString())
				.append("\n")
				.append(activity.getControllerStartupType().toString());

		File dataFile = getActivityDataFile(activity.getUuid());
		Files.writeFile(dataFile, contents.toString());
	}

	/**
	 * Get the data file for the given activity
	 * 
	 * @param uuid
	 *            the uuid of the activity
	 * 
	 * @return the data file
	 */
	private File getActivityDataFile(String uuid) {
		return new File(activityStorageManager.getBaseActivityLocation(uuid),
				DATAFILE_NAME);
	}
}
