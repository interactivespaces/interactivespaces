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

package interactivespaces.controller.domain;

import interactivespaces.activity.ActivityControllerStartupType;
import interactivespaces.activity.ActivityState;

import java.util.Date;

/**
 * An activity which has been locally installed on the controller.
 * 
 * @author Keith M. Hughes
 */
public interface InstalledLiveActivity {

	/**
	 * Get the UUID of the activity.
	 * 
	 * @return The UUID.
	 */
	String getUuid();

	/**
	 * Set the UUID of the activity.
	 * 
	 * @param uuid
	 *            The UUID.
	 */
	void setUuid(String uuid);

	/**
	 * Get the identifying name of the activity.
	 * 
	 * @return The identifying name.
	 */
	String getIdentifyingName();

	/**
	 * Set the identifying name of the activity.
	 * 
	 * @param identifyingName
	 *            The identifying name of the activity.
	 */
	void setIdentifyingName(String identifyingName);

	/**
	 * Get the version of the activity.
	 * 
	 * @return The activity version.
	 */
	String getVersion();

	/**
	 * Set the version of the activity.
	 * 
	 * @param version
	 *            The version of the activity.
	 */
	void setVersion(String identifyingName);

	/**
	 * Get the date the activity was last deployed.
	 * 
	 * @return the date the activity was last deployed
	 */
	Date getLastDeployedDate();

	/**
	 * Set the date the activity was last deployed.
	 * 
	 * @param lastDeployedDate
	 *            the date the activity was last deployed
	 */
	void setLastDeployedDate(Date lastDeployedDate);

	/**
	 * Get the location of the activity's installation.
	 * 
	 * @return The fully qualified path of the install.
	 */
	String getBaseInstallationLocation();

	/**
	 * Set the location of the activity's installation.
	 * 
	 * @param installationLocation
	 *            the fully qualified path of the install
	 */
	void setBaseInstallationLocation(String installationLocation);

	/**
	 * Get the status of the installation.
	 * 
	 * @return The last known status of the installation.
	 */
	ActivityInstallationStatus getInstallationStatus();

	/**
	 * Set the status of the installation.
	 * 
	 * @param status
	 *            the new status
	 */
	void setInstallationStatus(ActivityInstallationStatus status);

	/**
	 * Get the last known status of the activity.
	 * 
	 * @return the last known status of the activity.
	 */
	ActivityState getLastActivityState();

	/**
	 * Set the last known status of the activity.
	 * 
	 * @param lastActivityState
	 *            the last known state of the activity
	 */
	void setLastActivityState(ActivityState lastActivityState);

	/**
	 * Get how the activity should respond to controller startup.
	 * 
	 * @return how the activity should respond to controller startup
	 */
	ActivityControllerStartupType getControllerStartupType();

	/**
	 * Set how the activity should respond to controller startup.
	 * 
	 * @param controllerStartupType
	 *            how the activity should respond to controller startup
	 */
	void setControllerStartupType(
			ActivityControllerStartupType controllerStartupType);
}
