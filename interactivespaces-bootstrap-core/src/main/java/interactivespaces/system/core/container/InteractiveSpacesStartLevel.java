/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.system.core.container;

/**
 * Start level enum for an Interactive Spaces bundle.
 */
public enum InteractiveSpacesStartLevel {

  /**
   * The default OSGi startup level for bundles.
   */
  STARTUP_LEVEL_DEFAULT(1),

  /**
   * The OSGi startup level for bundles which should start after most bundles
   * but not the ones which really require everything running.
   */
  STARTUP_LEVEL_PENULTIMATE(4),

  /**
   * The OSGi startup level for bundles which should start after everything else
   * is started.
   */
  STARTUP_LEVEL_LAST(5);

  /**
   * Integer value for this start level.
   */
  private int startLevel;

  /**
   * Create a new start level enum with the given level.
   *
   * @param startLevel start level for entry
   */
  InteractiveSpacesStartLevel(int startLevel) {
    this.startLevel = startLevel;
  }

  /**
   * Get the integral start level value.
   *
   * @return start level value
   */
  public int getStartLevel() {
    return startLevel;
  }
}
