/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityRuntimeStartupType;
import interactivespaces.activity.ActivityState;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.liveactivity.runtime.domain.ActivityInstallationStatus;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import interactivespaces.resource.Version;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A local live activity repository for project-based standalone running.
 *
 * @author Keith M. Hughes
 */
public class StandaloneLocalLiveActivityRepository implements LocalLiveActivityRepository {

  /**
   * The collection live activity information.
   */
  private StandaloneLiveActivityInformationCollection liveActivityInformation;

  /**
   * The installed live activities.
   */
  private Map<String, InstalledLiveActivity> installedLiveActivities = Maps.newHashMap();

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * File support instance to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new repository.
   *
   * @param liveActivityInformation
   *          the collection of live activities to be run by the standalone runner
   * @param spaceEnvironment
   *          the space environment to use
   */
  public StandaloneLocalLiveActivityRepository(StandaloneLiveActivityInformationCollection liveActivityInformation,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.liveActivityInformation = liveActivityInformation;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    for (StandaloneLiveActivityInformation info : liveActivityInformation.getAllActivityInformation()) {
      installedLiveActivities.put(info.getUuid(), newInstalledLiveActivity(info));
    }
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public InstalledLiveActivity newInstalledLiveActivity() {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot create new installed activities");

    // Only for compilation
    return null;
  }

  @Override
  public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
    return Lists.newArrayList(installedLiveActivities.values());
  }

  @Override
  public InstalledLiveActivity getInstalledLiveActivityByUuid(String uuid) {
    return installedLiveActivities.get(uuid);
  }

  @Override
  public InstalledLiveActivity saveInstalledLiveActivity(InstalledLiveActivity activity) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot saveinstalled activities");

    // Only for compilation
    return null;
  }

  @Override
  public void deleteInstalledLiveActivity(InstalledLiveActivity activity) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot delete installed activities");
  }

  /**
   * Get a live activity instance from its info object.
   *
   * @param info
   *          the activity info
   *
   * @return installed live activity instance
   */
  private InstalledLiveActivity newInstalledLiveActivity(StandaloneLiveActivityInformation info) {
    final InstalledLiveActivity liveActivity = new SimpleInstalledLiveActivity();

    Date installedDate = new Date(spaceEnvironment.getTimeProvider().getCurrentTime());

    File activityFile = info.getActivityFilesystem().getInstallFile("activity.xml");

    if (!fileSupport.exists(activityFile)) {
      SimpleInteractiveSpacesException.throwFormattedException(
          "Activity description file %s not found, has the activity been successfully built?",
          activityFile.getAbsoluteFile());
    }

    InputStream activityDescriptionStream = null;
    Version version;
    String identifyingName;
    try {
      activityDescriptionStream = new FileInputStream(activityFile);
      ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
      ActivityDescription activityDescription = reader.readDescription(activityDescriptionStream);
      version = Version.parseVersion(activityDescription.getVersion());
      identifyingName = activityDescription.getIdentifyingName();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not read activity file description from "
          + activityFile.getAbsolutePath(), e);
    } finally {
      Closeables.closeQuietly(activityDescriptionStream);
    }

    liveActivity.setUuid(info.getUuid());
    liveActivity.setIdentifyingName(identifyingName);
    liveActivity.setVersion(version);
    liveActivity.setLastDeployedDate(installedDate);
    liveActivity.setLastActivityState(ActivityState.READY);
    liveActivity.setInstallationStatus(ActivityInstallationStatus.OK);
    liveActivity.setRuntimeStartupType(ActivityRuntimeStartupType.READY);

    return liveActivity;
  }

}
