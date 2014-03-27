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

package interactivespaces.master.ui.internal.web;

import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiSpaceControllerManager;

/**
 * Base support for Spring MVC controllers for the space master who have live
 * components.
 *
 * @author Keith M. Hughes
 */
public class BaseActiveSpaceMasterController extends BaseSpaceMasterController {

  /**
   * The UI manager for activities.
   */
  protected MasterApiActivityManager masterApiActivityManager;

  /**
   * The UI manager for controllers.
   */
  protected MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * @param masterApiActivityManager
   *          the masterApiActivityManager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * Set the Master API controller manager.
   *
   * @param masterApiControllerManager
   *          the Master API controller manager to use
   */
  public void setMasterApiSpaceControllerManager(MasterApiSpaceControllerManager masterApiControllerManager) {
    this.masterApiSpaceControllerManager = masterApiControllerManager;
  }
}
