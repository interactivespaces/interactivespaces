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

package interactivespaces.util.process.restart;

/**
 * An instance for a restarting strategy.
 *
 * @author Keith M. Hughes
 */
public interface RestartStrategyInstance {

  /**
   * Quit attempting a restart.
   */
  void quit();

  /**
   * Is the instance trying to restart?
   *
   * @return {@code true} if the restart is being attempted
   */
  boolean isRestarting();

  /**
   * Get the strategy from the instance.
   *
   * @return the strategy
   */
  RestartStrategy getStrategy();

  /**
   * Get the restartable from the instance.
   *
   * @return the restartable
   */
  Restartable getRestartable();
}
