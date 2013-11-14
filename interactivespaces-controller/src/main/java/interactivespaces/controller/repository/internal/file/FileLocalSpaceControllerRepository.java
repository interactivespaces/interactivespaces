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
import interactivespaces.resource.Version;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Controller repository using Db4o.
 *
 * @author Keith M. Hughes
 */
public class FileLocalSpaceControllerRepository implements LocalSpaceControllerRepository {

  /**
   * Name of the data file for the activity data.
   */
  public static final String DATAFILE_NAME = "activity.data";

  /**
   * Line number in the activity data file giving the identifying name of the
   * activity.
   */
  public static final int LOCATION_IDENTIFYING_NAME = 0;

  /**
   * Line number in the activity data file giving the version of the activity.
   */
  public static final int LOCATION_VERSION = 1;

  /**
   * Line number in the activity data file giving the last deployed date of the
   * activity.
   */
  public static final int LOCATION_LAST_DEPLOYED_DATE = 2;

  /**
   * Line number in the activity data file giving the last activity state of the
   * activity.
   */
  public static final int LOCATION_LAST_ACTIVITY_STATE = 3;

  /**
   * Line number in the activity data file giving the installation status of the
   * activity.
   */
  public static final int LOCATION_INSTALLATION_STATUS = 4;

  /**
   * Line number in the activity data file giving the startup type of the
   * activity.
   */
  public static final int LOCATION_STARTUP_TYPE = 5;

  /**
   * The storage manager for activities.
   */
  private final ActivityStorageManager activityStorageManager;

  /**
   * A map of all activities by their UUID.
   */
  private final Map<String, SimpleInstalledLiveActivity> activitiesByUuid = Maps.newHashMap();

  /**
   * The space environment.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The lock for protecting reads and writes.
   */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a space controller repository.
   *
   * @param activityStorageManager
   *          storage manager for activities
   * @param spaceEnvironment
   *          the space environment
   */
  public FileLocalSpaceControllerRepository(ActivityStorageManager activityStorageManager,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.activityStorageManager = activityStorageManager;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    // Treat this as a write
    lock.writeLock().lock();
    try {
      for (String uuid : activityStorageManager.getAllInstalledActivityUuids()) {
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
      for (SimpleInstalledLiveActivity activity : activitiesByUuid.values()) {
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
  public InstalledLiveActivity saveInstalledLiveActivity(InstalledLiveActivity activity) {
    lock.writeLock().lock();
    try {
      writeDataFile(activity);
      activitiesByUuid.put(activity.getUuid(), new SimpleInstalledLiveActivity(activity));

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
        spaceEnvironment.getLog().warn(
            String.format("Attempt to delete installed activity %s which is doesn't seem to exist", activitiesByUuid));
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Read the datafile for an activity.
   *
   * @param uuid
   *          UUID of the activity
   *
   * @return the activity, or {@code null} if there was no data file
   */
  private SimpleInstalledLiveActivity readDataFile(String uuid) {
    File datafile = getActivityDataFile(uuid);
    if (datafile.isFile()) {
      SimpleInstalledLiveActivity activity = new SimpleInstalledLiveActivity();

      String contents = fileSupport.readFile(datafile);
      String[] lines = contents.split("\\n");

      activity.setUuid(uuid);
      activity.setBaseInstallationLocation(activityStorageManager.getBaseActivityLocation(uuid).getAbsolutePath());
      activity.setIdentifyingName(lines[LOCATION_IDENTIFYING_NAME]);
      activity.setVersion(Version.parseVersion(lines[LOCATION_VERSION]));
      activity.setLastDeployedDate(new Date(Long.parseLong(lines[LOCATION_LAST_DEPLOYED_DATE])));
      activity.setLastActivityState(ActivityState.valueOf(lines[LOCATION_LAST_ACTIVITY_STATE]));
      activity.setInstallationStatus(ActivityInstallationStatus.valueOf(lines[LOCATION_INSTALLATION_STATUS]));
      activity.setControllerStartupType(ActivityControllerStartupType.valueOf(lines[LOCATION_STARTUP_TYPE]));

      return activity;
    } else {
      spaceEnvironment.getLog().warn(String.format("Found activity folder with UUID %s. No data file", uuid));
      return null;
    }
  }

  /**
   * write the datafile for an activity.
   *
   * @param activity
   *          the activity to write
   */
  private void writeDataFile(InstalledLiveActivity activity) {
    StringBuilder contents = new StringBuilder();
    contents.append(activity.getIdentifyingName()).append("\n").append(activity.getVersion()).append("\n")
        .append(activity.getLastDeployedDate().getTime()).append("\n")
        .append(activity.getLastActivityState().toString()).append("\n")
        .append(activity.getInstallationStatus().toString()).append("\n")
        .append(activity.getControllerStartupType().toString());

    File dataFile = getActivityDataFile(activity.getUuid());
    fileSupport.writeFile(dataFile, contents.toString());
  }

  /**
   * Get the data file for the given activity
   *
   * @param uuid
   *          the uuid of the activity
   *
   * @return the data file
   */
  private File getActivityDataFile(String uuid) {
    return new File(activityStorageManager.getBaseActivityLocation(uuid), DATAFILE_NAME);
  }
}
