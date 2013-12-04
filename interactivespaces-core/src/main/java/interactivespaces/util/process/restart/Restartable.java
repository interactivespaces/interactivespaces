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
 * Something which can be restarted by a {@link RestartStrategy}.
 *
 * @author Keith M. Hughes
 */
public interface Restartable {

  /**
   * Attempt a restart.
   */
  void attemptRestart();

  /**
   * Has the {@link Restartable} restarted?
   *
   * <p>
   * This will be sampled several times by the restarter to make sure that the
   * restart truly has been successful.
   *
   * @return {@code true} if restarted
   */
  boolean isRestarted();

  /**
   * The strategy considers the restart complete.
   *
   * @param success
   *          {@code true} if it is considered a successful restart
   */
  void restartComplete(boolean success);
}
