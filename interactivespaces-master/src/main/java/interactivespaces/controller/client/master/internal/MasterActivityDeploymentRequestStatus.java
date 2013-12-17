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

package interactivespaces.controller.client.master.internal;

/**
 * Status of a master deployment request.
 *
 * @author Keith M. Hughes
 */
public enum MasterActivityDeploymentRequestStatus {

  /**
   * Querying for dependencies.
   */
  QUERYING_DEPENDENCIES,

  /**
   * Attempting to satisfy dependencies.
   */
  SATISFYING_DEPENDENCIES,

  /**
   * Deploying the activity.
   */
  DEPLOYING_ACTIVITY,

  /**
   * Deploying the activity is complete, though not clear if successful or not
   * successful.
   */
  DEPLOYMENT_COMPLETE

}
