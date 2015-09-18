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

package interactivespaces.controller;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * The status of a space controller operation.
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public enum SpaceControllerStatus {

  /**
   * Space controller success.
   */
  SUCCESS("success"),

  /**
   * Space controller failure.
   */
  FAILURE("failure");

  /**
   * Static map containing the reverse-lookup from the description to the enum type.
   */
  private static final Map<String, SpaceControllerStatus> DESCRIPTION_MAP = Maps.newHashMap();

  /**
   * Construct the requisite reverse-lookup map.
   */
  static {
    for (SpaceControllerStatus status : SpaceControllerStatus.values()) {
      DESCRIPTION_MAP.put(status.getDescription(), status);
    }
  }

  /**
   * Return a enum value given the associated description.
   *
   * @param description
   *          description to look up
   *
   * @return the matching enum
   */
  public static SpaceControllerStatus fromDescription(String description) {
    return DESCRIPTION_MAP.get(description);
  }

  /**
   * Checks if the status indicates success.
   *
   * @param description
   *          the description of the status
   *
   * @return {@code true} if success
   */
  public static boolean isSuccessDescription(String description) {
    return SUCCESS.equals(fromDescription(description));
  }

  /**
   * String value to use for this constant.
   */
  private final String description;

  /**
   * Construct a status.
   *
   * @param description
   *          the description of the status
   */
  private SpaceControllerStatus(String description) {
    this.description = description;
  }

  /**
   * Get the description for this status.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }
}
