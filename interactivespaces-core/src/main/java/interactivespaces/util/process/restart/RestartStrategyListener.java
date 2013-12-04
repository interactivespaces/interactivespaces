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

package interactivespaces.util.process.restart;

/**
 * A listener for restart events.
 *
 * @author Keith M. Hughes
 */
public interface RestartStrategyListener {

  /**
   * The process has stopped running. A restart is about to be attempted.
   *
   * @param strategy
   *          the strategy this listener is on
   * @param restartable
   *          the item being restarted
   * @param continueRestart
   *          {@code true} if all listeners so far have voted to keep going
   *
   * @return {@code true} if a restart should be attempted according to the
   *         strategy, {@code false} if the restart should be abandoned.
   */
  boolean onRestartAttempt(RestartStrategy strategy, Restartable restartable, boolean continueRestart);

  /**
   * Restart has been successful.
   *
   * @param strategy
   *          the strategy this listener is on
   * @param restartable
   *          the item being restarted
   */
  void onRestartSuccess(RestartStrategy strategy, Restartable restartable);

  /**
   * The restart has failed.
   *
   * @param strategy
   *          the strategy this listener is on
   * @param restartable
   *          the item being restarted
   */
  void onRestartFailure(RestartStrategy strategy, Restartable restartable);
}
