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

package interactivespaces.master.server.services;

import interactivespaces.controller.SpaceControllerState;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.services.internal.DataBundleState;
import interactivespaces.time.TimeProvider;

import java.util.Date;

/**
 * A space controller "active" in the space.
 *
 * <p>
 * "Active" means that the master knows about it.
 *
 * @author Keith M. Hughes
 */
public class ActiveSpaceController {

  /**
   * The controller being represented.
   */
  private SpaceController controller;

  /**
   * Current known state of the controller.
   */
  private SpaceControllerState state = SpaceControllerState.UNKNOWN;

  /**
   * Timestamp of the last update.
   */
  private Long lastStateUpdate;

  /**
   * Current known data bundle state of the controller.
   */
  private DataBundleState dataBundleState = DataBundleState.NO_REQUEST;

  /**
   * Timestamp of the last data bundle state update.
   */
  private Long lastDataBundleStateUpdate;

  /**
   * The time provider.
   */
  private TimeProvider timeProvider;

  public ActiveSpaceController(SpaceController controller, TimeProvider timeProvider) {
    this.controller = controller;
    this.timeProvider = timeProvider;
  }

  /**
   * @return the controller
   */
  public SpaceController getController() {
    return controller;
  }

  /**
   * Update the controller object contained within.
   *
   * <p>
   * This allows this object access to merged data.
   *
   * @param controller
   *          the potentially updated controller entity
   */
  public void updateController(SpaceController controller) {
    this.controller = controller;
  }

  /**
   * @return the state
   */
  public SpaceControllerState getState() {
    return state;
  }

  /**
   * @param state
   *          the state to set
   */
  public void setState(SpaceControllerState state) {
    this.state = state;

    lastStateUpdate = timeProvider.getCurrentTime();
  }

  /**
   * Get the lastStateUpdate of the last update.
   *
   * @return the lastStateUpdate. Will be {@code null} if the controller has
   *         never been updated.
   */
  public Long getLastStateUpdate() {
    return lastStateUpdate;
  }

  /**
   * Get the lastStateUpdate of the last update as a date.
   *
   * @return the lastStateUpdate. Will be {@code null} if the controller has
   *         never been updated.
   */
  public Date getLastStateUpdateDate() {
    if (lastStateUpdate != null) {
      return new Date(lastStateUpdate);
    } else {
      return null;
    }
  }

  /**
   * @return the data bundle state
   */
  public DataBundleState getDataBundleState() {
    return dataBundleState;
  }

  /**
   * @param dataBundleState
   *          the data bundle state to set
   */
  public void setDataBundleState(DataBundleState dataBundleState) {
    this.dataBundleState = dataBundleState;

    lastDataBundleStateUpdate = timeProvider.getCurrentTime();
  }

  /**
   * Get the lastDataBundleStateUpdate of the last update.
   *
   * @return the lastDataBundleStateUpdate. Will be {@code null} if the controller has
   *         never been updated.
   */
  public Long getLastDataBundleStateUpdate() {
    return lastDataBundleStateUpdate;
  }

  /**
   * Get the lastDataBundleStateUpdate of the last update as a date.
   *
   * @return the lastDataBundleStateUpdate. Will be {@code null} if the controller has
   *         never been updated.
   */
  public Date getLastDataBundleStateUpdateDate() {
    if (lastDataBundleStateUpdate != null) {
      return new Date(lastDataBundleStateUpdate);
    } else {
      return null;
    }
  }
}
