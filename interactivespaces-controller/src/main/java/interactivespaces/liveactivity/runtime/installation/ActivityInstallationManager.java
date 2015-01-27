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

package interactivespaces.liveactivity.runtime.installation;

import interactivespaces.resource.Version;
import interactivespaces.util.resource.ManagedResource;

import java.util.Date;

/**
 * Manages activity deployment on the controller.
 *
 * @author Keith M. Hughes
 */
public interface ActivityInstallationManager extends ManagedResource {

  /**
   * Copy a packed activity to the controller.
   *
   * @param uuid
   *          UUID of the activity
   * @param uri
   *          URI for a zip file containing the activity
   */
  void copyActivity(String uuid, String uri);

  /**
   * Install the activity to its final location.
   *
   * @param uuid
   *          UUID of the activity
   * @param activityIdentifyingName
   *          identifying name of the activity
   * @param version
   *          version of the activity
   *
   * @return the timestamp from when the activity was installed
   */
  Date installActivity(String uuid, String activityIdentifyingName, Version version);

  /**
   * Delete the packed activity from file system.
   *
   * <p>
   * Does nothing if there is no activity with the given UUID.
   *
   * @param uuid
   *          UUID of the activity
   */
  void removePackedActivity(String uuid);

  /**
   * Delete an activity from the file system.
   *
   * @param uuid
   *          UUID of the activity
   *
   * @return result of the removal
   */
  RemoveActivityResult removeActivity(String uuid);

  /**
   * Add in a new activity installation listener.
   *
   * @param listener
   *          the new listener
   */
  void addActivityInstallationListener(ActivityInstallationListener listener);

  /**
   * Remove an activity installation listener.
   *
   * <p>
   * Does nothing if the listener wasn't there.
   *
   * @param listener
   *          the listener
   */
  void removeActivityInstallationListener(ActivityInstallationListener listener);

  /**
   * Results for trying to remove an activity from the system.
   *
   * @author Keith M. Hughes
   */
  public enum RemoveActivityResult {

    /**
     * Removal was successful.
     */
    SUCCESS,

    /**
     * Removal failed.
     */
    FAILURE,

    /**
     * The activity didn't exist to remove.
     */
    DOESNT_EXIST
  }
}
