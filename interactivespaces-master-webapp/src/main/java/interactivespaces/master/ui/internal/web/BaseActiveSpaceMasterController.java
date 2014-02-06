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
import interactivespaces.master.api.MasterApiControllerManager;
import interactivespaces.master.api.MasterApiSpaceManager;
import interactivespaces.master.server.services.ActiveControllerManager;

/**
 * Base support for Spring MVC controllers for the space master who have live
 * components.
 *
 * @author Keith M. Hughes
 */
public class BaseActiveSpaceMasterController extends BaseSpaceMasterController {

  /**
   * Manager for activity operations.
   */
  private ActiveControllerManager activeControllerManager;

  /**
   * The UI manager for activities.
   */
  protected MasterApiActivityManager masterApiActivityManager;

  /**
   * The UI manager for spaces.
   */
  protected MasterApiSpaceManager masterApiSpaceManager;

  /**
   * The UI manager for controllers.
   */
  protected MasterApiControllerManager masterApiControllerManager;

  /**
   * @param activeControllerManager
   *          the activeControllerManager to set
   */
  public void setActiveControllerManager(ActiveControllerManager activeControllerManager) {
    this.activeControllerManager = activeControllerManager;
  }

  /**
   * @param masterApiActivityManager
   *          the masterApiActivityManager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * @param masterApiSpaceManager
   *          the masterApiSpaceManager to set
   */
  public void setMasterApiSpaceManager(MasterApiSpaceManager masterApiSpaceManager) {
    this.masterApiSpaceManager = masterApiSpaceManager;
  }

  /**
   * Set the Master API controller manager.
   *
   * @param masterApiControllerManager
   *          the Master API controller manager to use
   */
  public void setMasterApiControllerManager(MasterApiControllerManager masterApiControllerManager) {
    this.masterApiControllerManager = masterApiControllerManager;
  }
}
