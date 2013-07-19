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

package interactivespaces.activity;

/**
 * The control commands for an activity.
 *
 * @author Keith M. Hughes
 */
public interface ActivityControl {

  /**
   * Start up the activity.
   *
   * <p>
   * This method should alter the activity state.
   *
   * <ul>
   * <li>If the startup was successful, the activity state should be
   * {@code ActivityStatus.RUNNING}.</li>
   * <li>If the startup was unsuccessful, the activity state should be
   * {@code ActivityStatus.STARTUP_FAILURE}.</li>
   * </ul>
   */
  void startup();

  /**
   * Shut the activity down.
   *
   * <p>
   * This method should alter the activity state.
   *
   * <ul>
   * <li>If the shutdown was successful, the activity state should be
   * {@code ActivityStatus.READY}.</li>
   * <li>If the shutdown was unsuccessful, the activity state should be
   * {@code ActivityStatus.SHUTDOWN_FAILURE}.</li>
   * </ul>
   *
   * <p>
   * This method can be called several times in a row. Any time after the first
   * call should do nothing.
   */
  void shutdown();

  /**
   * Activate the activity.
   *
   * <p>
   * This method should alter the activity state.
   *
   * <ul>
   * <li>If the activation was successful, the activity state should be
   * {@code ActivityStatus.ACTIVE}.</li>
   * <li>If the activation was unsuccessful, the activity state should be
   * {@code ActivityStatus.ACTIVATE_FAILURE}.</li>
   * </ul>
   */
  void activate();

  /**
   * Activate the activity.
   *
   * <p>
   * This method should alter the activity state.
   *
   * <ul>
   * <li>If the deactivation was successful, the activity state should be
   * {@code ActivityStatus.RUNNING}.</li>
   * <li>If the deactivation was unsuccessful, the activity state should be
   * {@code ActivityStatus.DEACTIVATE_FAILURE}.</li>
   * </ul>
   */
  void deactivate();
}
