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

package interactivespaces.domain.basic;

/**
 * Mode enum representing the activation mode of a controller.
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public enum SpaceControllerMode {
  /**
   * Enabled controller, the normal state.
   */
  ENABLED("space.controller.mode.enabled"),

  /**
   * Deprecated... indicating that it is marked for removal.
   */
  DEPRECATED("space.controller.mode.deprecated"),

  /**
   * Disabled. Still there, but will not be automatically contacted.
   */
  DISABLED("space.controller.mode.disabled");

  /**
   * Description for this mode, suitable for translations.
   */
  private String description;

  /**
   * Create a new controller mode description.
   *
   * @param description
   *          description for translations
   */
  SpaceControllerMode(String description) {
    this.description = description;
  }

  /**
   * @return description of this mode
   */
  public String getDescription() {
    return description;
  }

  /**
   * Check if the given controller is considered 'live'.
   *
   * @param controller
   *          controller to check for live status
   *
   * @return {@code true} if the controller is live
   */
  public static boolean isControllerEnabled(SpaceController controller) {
    if (controller == null) {
      return false;
    }
    SpaceControllerMode mode = controller.getMode();
    return mode == null || ENABLED.equals(mode);
  }
}
