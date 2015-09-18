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
 * An activity that is deployed to a runtime environment, e.g. a space controller.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivity extends PersistedObject, Serializable {

  /**
   * Get the UUID of this live activity.
   *
   * @return the uuid
   */
  String getUuid();

  /**
   * Set the UUID of this live activity.
   *
   * @param uuid
   *          the uuid to set
   *
   * @return this live activity
   */
  LiveActivity setUuid(String uuid);

  /**
   * Get the name of this live activity.
   *
   * @return the name
   */
  String getName();

  /**
   * Set the name of this live activity.
   *
   * @param name
   *          the name to set
   *
   * @return this live activity
   */
  LiveActivity setName(String name);

  /**
   * Get the description of this live activity.
   *
   * @return the description
   */
  String getDescription();

  /**
   * Set the description of this live activity.
   *
   * @param description
   *          the description to set
   *
   * @return this live activity
   */
  LiveActivity setDescription(String description);

  /**
   * Get the activity this live activity is based on.
   *
   * @return the activity
   */
  Activity getActivity();

  /**
   * Set the activity this live activity is based on.
   *
   * @param activity
   *          the activity to set
   *
   * @return this live activity
   */
  LiveActivity setActivity(Activity activity);

  /**
   * Get the space controller this live activity is running on.
   *
   * @return the space controller, can be {@code null}
   */
  SpaceController getController();

  /**
   * Set the space controller this live activity is running on.
   *
   * @param controller
   *          the space controller, can be {@code null}
   *
   * @return this live activity
   */
  LiveActivity setController(SpaceController controller);

  /**
   * Get the installation specific configuration.
   *
   * @return the configuration, can be {@code null}
   */
  ActivityConfiguration getConfiguration();

  /**
   * Set the installation specific configuration.
   *
   * @param configuration
   *          the configuration, can be {@code null}
   *
   * @return this live activity
   */
  LiveActivity setConfiguration(ActivityConfiguration configuration);

  /**
   * Get when the live activity was last deployed to the space controller.
   *
   * @return the date the live activity was last deployed
   */
  Date getLastDeployDate();

  /**
   * Set when the live activity was last deployed to the space controller.
   *
   * @param lastDeployDate
   *          the last deployment date, can be {@code null}
   *
   * @return this live activity
   */
  LiveActivity setLastDeployDate(Date lastDeployDate);

  /**
   * Is the deployed live activity out of date with the most recent live activity?
   *
   * @return {@code true} if out of date
   */
  boolean isOutOfDate();

  /**
   * Set the metadata for this live activity.
   *
   * <p>
   * This removes the old metadata completely.
   *
   * @param metadata
   *          the metadata for this live activity, can be {@link null}
   *
   * @return this live activity
   */
  LiveActivity setMetadata(Map<String, Object> metadata);

  /**
   * Get the metadata for this live activity.
   *
   * @return the meta data
   */
  Map<String, Object> getMetadata();
}
