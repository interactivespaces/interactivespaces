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

package interactivespaces.controller.domain.pojo;

import interactivespaces.activity.ActivityControllerStartupType;
import interactivespaces.activity.ActivityState;
import interactivespaces.controller.domain.ActivityInstallationStatus;
import interactivespaces.controller.domain.InstalledLiveActivity;

import java.util.Date;

/**
 * A POJO {@link InstalledLiveActivity}.
 *
 * @author Keith M. Hughes
 */
public class SimpleInstalledLiveActivity implements InstalledLiveActivity {

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
  private String version;

  /**
   * Date the activity was last uploaded.
   */
  private Date lastDeployedDate;

  /**
   * The base location where the activity is installed.
   */
  private String baseInstallationLocation;

  /**
   * Current known status of the installation.
   */
  private ActivityInstallationStatus installationStatus = ActivityInstallationStatus.UNKNOWN;

  /**
   * Last known status of the activity.
   */
  private ActivityState lastActivityState = ActivityState.UNKNOWN;

  /**
   * how the activity should respond to a controller startup.
   */
  private ActivityControllerStartupType controllerStartupType = ActivityControllerStartupType.READY;

  public SimpleInstalledLiveActivity() {
  }

  public SimpleInstalledLiveActivity(InstalledLiveActivity src) {
    uuid = src.getUuid();
    identifyingName = src.getIdentifyingName();
    version = src.getVersion();
    lastDeployedDate = src.getLastDeployedDate();
    baseInstallationLocation = src.getBaseInstallationLocation();
    installationStatus = src.getInstallationStatus();
    lastActivityState = src.getLastActivityState();
    controllerStartupType = src.getControllerStartupType();
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
  public String getVersion() {
    return version;
  }

  @Override
  public void setVersion(String version) {
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
  public String getBaseInstallationLocation() {
    return baseInstallationLocation;
  }

  @Override
  public void setBaseInstallationLocation(String baseInstallationLocation) {
    this.baseInstallationLocation = baseInstallationLocation;
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
  public ActivityControllerStartupType getControllerStartupType() {
    return controllerStartupType;
  }

  @Override
  public void setControllerStartupType(ActivityControllerStartupType controllerStartupType) {
    this.controllerStartupType = controllerStartupType;
  }
}
