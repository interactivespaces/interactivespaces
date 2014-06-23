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
 * The strategy for how to handle a restart.
 *
 * @param <T>
 *          the type of {@link Restartable}
 *
 * @author Keith M. Hughes
 */
public interface RestartStrategy<T extends Restartable> {

  /**
   * Create a new restart instance.
   *
   * @param restartable
   *          the object to be restarted
   *
   * @return a restart instance which is already working
   */
  RestartStrategyInstance<T> newInstance(T restartable);

  /**
   * Add a new listener to the restart strategy.
   *
   * @param listener
   *          the new listener
   */
  void addRestartStrategyListener(RestartStrategyListener<T> listener);

  /**
   * Remove a listener from the restart strategy.
   *
   * <p>
   * This method does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeRestartStrategyListener(RestartStrategyListener<T> listener);
}
