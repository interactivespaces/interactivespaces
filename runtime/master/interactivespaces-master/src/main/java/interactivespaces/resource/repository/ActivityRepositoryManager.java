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

package interactivespaces.resource.repository;

import interactivespaces.domain.basic.Activity;

import java.io.InputStream;

/**
 * A manager for handling the Interactive Spaces activity repository.
 *
 * <p>
 * The activity repository will handle the actual saving of activities.
 *
 * @author Keith M. Hughes
 */
public interface ActivityRepositoryManager {

  /**
   * Add an activity to the repository.
   *
   * @param activityStream
   *          the stream containing the activity
   *
   * @return the activity loaded
   */
  Activity addActivity(InputStream activityStream);

  /**
   * Calculate and update the content hash for the activity bundle. This updates the activity's hash record with the
   * calculated hash.
   *
   * @param activity
   *          activity for which to update the bundle hash
   */
  void calculateBundleContentHash(Activity activity);
}
