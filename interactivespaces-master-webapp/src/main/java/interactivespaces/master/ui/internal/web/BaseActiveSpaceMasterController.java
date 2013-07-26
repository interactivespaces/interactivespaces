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

import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.master.server.ui.UiSpaceManager;

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
  protected UiActivityManager uiActivityManager;

  /**
   * The UI manager for spaces.
   */
  protected UiSpaceManager uiSpaceManager;

  /**
   * The UI manager for controllers.
   */
  protected UiControllerManager uiControllerManager;

  /**
   * @param activeControllerManager
   *          the activeControllerManager to set
   */
  public void setActiveControllerManager(ActiveControllerManager activeControllerManager) {
    this.activeControllerManager = activeControllerManager;
  }

  /**
   * @param uiActivityManager
   *          the uiActivityManager to set
   */
  public void setUiActivityManager(UiActivityManager uiActivityManager) {
    this.uiActivityManager = uiActivityManager;
  }

  /**
   * @param uiSpaceManager
   *          the uiSpaceManager to set
   */
  public void setUiSpaceManager(UiSpaceManager uiSpaceManager) {
    this.uiSpaceManager = uiSpaceManager;
  }

  /**
   * @param uiControllerManager
   *          the uiControllerManager to set
   */
  public void setUiControllerManager(UiControllerManager uiControllerManager) {
    this.uiControllerManager = uiControllerManager;
  }
}
