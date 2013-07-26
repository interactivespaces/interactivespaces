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

package interactivespaces.controller.client.node;

/**
 * Statuses for operations on Interactive Spaces activities in an space
 * controller.
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
    SUCCESS("controller.activity.deploy.success"), FAILURE("controller.activity.deploy.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    ControllerActivityDeployStatus(String description) {
      this.description = description;
    }

    /**
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
    SUCCESS("controller.activity.start.success"), FAILURE("controller.activity.start.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    ControllerActivityStartStatus(String description) {
      this.description = description;
    }

    /**
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
    SUCCESS("controller.activity.activate.success"),
    FAILURE("controller.activity.activate.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    ControllerActivityActivateStatus(String description) {
      this.description = description;
    }

    /**
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
    SUCCESS("controller.activity.deactivate.success"), FAILURE(
        "controller.activity.deactivate.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    ControllerActivityDeactivateStatus(String description) {
      this.description = description;
    }

    /**
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
    SUCCESS("controller.activity.shutdown.success"),
    FAILURE("controller.activity.shutdown.failure");

    /**
     * Message ID for the description.
     */
    private String description;

    ControllerActivityShutdownStatus(String description) {
      this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }

}
