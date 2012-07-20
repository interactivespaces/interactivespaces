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

package interactivespaces.domain.basic;

import interactivespaces.domain.PersistedObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * An activity which has been installed on a controller somewhere in the
 * environment.
 * 
 * @author Keith M. Hughes
 */
public interface LiveActivity extends PersistedObject, Serializable {

	/**
	 * @return the uuid
	 */
	String getUuid();

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	LiveActivity setUuid(String uuid);

	/**
	 * @return the name
	 */
	String getName();

	/**
	 * @param name
	 *            the name to set
	 */
	LiveActivity setName(String name);

	/**
	 * @return the description
	 */
	String getDescription();

	/**
	 * @param description
	 *            the description to set
	 */
	LiveActivity setDescription(String description);

	/**
	 * @return the activity
	 */
	Activity getActivity();

	/**
	 * @param activity
	 *            the activity to set
	 */
	LiveActivity setActivity(Activity activity);

	/**
	 * Get the controller this activity is running on,
	 * 
	 * @return the controller. Can be null.
	 */
	SpaceController getController();

	/**
	 * Set the controller this activity is running on,
	 * 
	 * @param controller
	 *            The controller. Can be null.
	 */
	LiveActivity setController(SpaceController controller);
	
	/**
	 * Get the installation specific configuration.
	 * 
	 * @return can be {@code null}
	 */
	ActivityConfiguration getConfiguration();
	
	/**
	 * Set the installation specific configuration.
	 * 
	 * @param configuration
	 * 			the configuration, can be {@code null}
	 */
	void setConfiguration(ActivityConfiguration configuration);

	/**
	 * Get when the activity was last deployed to the controller.
	 * 
	 * @return the date the activity was last deployed.
	 */
	Date getLastDeployDate();

	/**
	 * Set when the activity was last deployed to the controller.
	 * 
	 * @param lastDeployDate
	 *            the last deployment date, can be {@code null}
	 */
	void setLastDeployDate(Date lastDeployDate);

	/**
	 * Is the deployed activity out of date with the most recent activity?
	 * 
	 * @return {@code true} if out of date
	 */
	boolean isOutOfDate();
	
	/**
	 * Set the metadata for the live activity.
	 * 
	 * <p>
	 * This removes the old metadata completely.
	 * 
	 * @param metadata
	 * 		the metadata for the live activity (can be {@link null}
	 */
	void setMetadata(Map<String, Object> metadata);
	
	/**
	 * Get the metadata for the live activity.
	 * 
	 * @return the live activity's meta data
	 */
	Map<String, Object> getMetadata();
}
