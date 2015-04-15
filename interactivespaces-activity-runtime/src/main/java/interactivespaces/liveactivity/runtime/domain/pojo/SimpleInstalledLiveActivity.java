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

package interactivespaces.liveactivity.runtime.domain.pojo;

import interactivespaces.activity.ActivityRuntimeStartupType;
import interactivespaces.activity.ActivityState;
import interactivespaces.liveactivity.runtime.domain.ActivityInstallationStatus;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.Version;

import java.util.Date;

/**
 * A POJO {@link InstalledLiveActivity}.
 *
 * @author Keith M. Hughes
 */
public class SimpleInstalledLiveActivity implements InstalledLiveActivity,
    interactivespaces.controller.domain.InstalledLiveActivity {

  /**
   * UUID of the activity.
   */
  private String uuid;

  /**
   * Identifying name of the activity.
   */
  private String identifyingName;

  /**
   * Version of the activity.
   */
  private Version version;

  /**
   * Date the activity was last uploaded.
   */
  private Date lastDeployedDate;

  /**
   * Current known status of the installation.
   */
  private ActivityInstallationStatus installationStatus = ActivityInstallationStatus.UNKNOWN;

  /**
   * Last known status of the activity.
   */
  private ActivityState lastActivityState = ActivityState.UNKNOWN;

  /**
   * how the activity should respond to a runtime startup.
   */
  private ActivityRuntimeStartupType runtimeStartupType = ActivityRuntimeStartupType.READY;

  /**
   * Construct a blank live activity.
   */
  public SimpleInstalledLiveActivity() {
  }

  /**
   * Copy construct a live activity.
   *
   * @param src
   *          the activity to make a copy of
   */
  public SimpleInstalledLiveActivity(InstalledLiveActivity src) {
    uuid = src.getUuid();
    identifyingName = src.getIdentifyingName();
    version = src.getVersion();
    lastDeployedDate = src.getLastDeployedDate();
    installationStatus = src.getInstallationStatus();
    lastActivityState = src.getLastActivityState();
    runtimeStartupType = src.getRuntimeStartupType();
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String getIdentifyingName() {
    return identifyingName;
  }

  @Override
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public Date getLastDeployedDate() {
    return lastDeployedDate;
  }

  @Override
  public void setLastDeployedDate(Date lastDeployedDate) {
    this.lastDeployedDate = lastDeployedDate;
  }

  @Override
  public ActivityInstallationStatus getInstallationStatus() {
    return installationStatus;
  }

  @Override
  public void setInstallationStatus(ActivityInstallationStatus installationStatus) {
    this.installationStatus = installationStatus;
  }

  @Override
  public ActivityState getLastActivityState() {
    return lastActivityState;
  }

  @Override
  public void setLastActivityState(ActivityState lastActivityState) {
    this.lastActivityState = lastActivityState;
  }

  @Override
  public ActivityRuntimeStartupType getRuntimeStartupType() {
    return runtimeStartupType;
  }

  @Override
  public void setRuntimeStartupType(ActivityRuntimeStartupType runtimeStartupType) {
    this.runtimeStartupType = runtimeStartupType;
  }
}
