/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.controller.runtime;

import interactivespaces.util.resource.ManagedResource;

/**
 * Controller interface for copying data bundles between master and controller.
 *
 * @author Trevor Pering
 */
public interface ControllerDataBundleManager extends ManagedResource {

  /**
   * Trigger the capture of the controller data bundle.
   *
   * @param destinationUri
   *          URI to sink the data bundle
   */
  void captureControllerDataBundle(String destinationUri);

  /**
   * Trigger the capture of the controller data bundle.
   *
   * @param sourceUri
   *          URI from which to source the data bundle
   */
  void restoreControllerDataBundle(String sourceUri);

  /**
   * Set the space controller instance to manage.
   *
   * @param spaceController
   *          space controller for the data bundle manager
   */
  void setSpaceController(SpaceControllerControl spaceController);

  /**
   * Set the activity storage manager.
   *
   * @param activityStorageManager
   *          activity storage manager for the data bundle manager
   */
  void setActivityStorageManager(ActivityStorageManager activityStorageManager);
}
