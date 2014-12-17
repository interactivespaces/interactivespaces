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

package interactivespaces.controller.runtime;

/**
 * Statuses for operations on Interactive Spaces activities in an space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerActivityStatuses {

  /**
   * Status of a remote activity attempting to deploy.
   *
   * @author Keith M. Hughes
   */
  public enum ControllerActivityDeployStatus {

    /**
     * The activity deployment was successful.
     */
    SUCCESS("controller.activity.deploy.success"),

    /**
     * The activity deployment failed.
     */
    FAILURE("controller.activity.deploy.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    /**
     * Construct the status.
     *
     * @param description
     *          the description
     */
    ControllerActivityDeployStatus(String description) {
      this.description = description;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }

  /**
   * Status of a remote activity attempting to start.
   *
   * @author Keith M. Hughes
   */
  public enum ControllerActivityStartStatus {

    /**
     * The activity started successfully.
     */
    SUCCESS("controller.activity.start.success"),

    /**
     * The activity failed to start.
     */
    FAILURE("controller.activity.start.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    /**
     * Construct the status.
     *
     * @param description
     *          the description
     */
    ControllerActivityStartStatus(String description) {
      this.description = description;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }

  /**
   * Status of a remote activity attempting to activate.
   *
   * @author Keith M. Hughes
   */
  public enum ControllerActivityActivateStatus {

    /**
     * The activity activated successfully.
     */
    SUCCESS("controller.activity.activate.success"),

    /**
     * The activity failed to activate.
     */
    FAILURE("controller.activity.activate.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    /**
     * Construct the status.
     *
     * @param description
     *          the description
     */
    ControllerActivityActivateStatus(String description) {
      this.description = description;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }

  /**
   * Status of a remote activity attempting to deactivate.
   *
   * @author Keith M. Hughes
   */
  public enum ControllerActivityDeactivateStatus {

    /**
     * The activity deactivated successfully.
     */
    SUCCESS("controller.activity.deactivate.success"),

    /**
     * The activity failed to deactivate.
     */
    FAILURE("controller.activity.deactivate.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    /**
     * Construct the status.
     *
     * @param description
     *          the description
     */
    ControllerActivityDeactivateStatus(String description) {
      this.description = description;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }

  /**
   * Status of a remote activity attempting to shut down.
   *
   * @author Keith M. Hughes
   */
  public enum ControllerActivityShutdownStatus {

    /**
     * The activity shut down successfully.
     */
    SUCCESS("controller.activity.shutdown.success"),

    /**
     * The activity failed to shutdown.
     */
    FAILURE("controller.activity.shutdown.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    /**
     * Construct the status.
     *
     * @param description
     *          the description
     */
    ControllerActivityShutdownStatus(String description) {
      this.description = description;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }
}
