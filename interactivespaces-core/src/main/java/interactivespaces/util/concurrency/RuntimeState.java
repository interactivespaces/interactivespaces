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

package interactivespaces.util.concurrency;

/**
 * The runtime state of a component.
 *
 * @author Keith M. Hughes
 */
public enum RuntimeState {
  /**
   * The component is ready to run, but never has.
   */
  READY(false, false),

  /**
   * The component is running.
   */
  RUNNING(true, false),

  /**
   * The component is shut down.
   */
  SHUTDOWN(false, false),

  /**
   * The component has crashed.
   */
  CRASH(false, true);

  /**
   * {@code true} if the runtime state is considered running.
   */
  private boolean running;

  /**
   * {@code true} if the runtime state is considered an error.
   */
  private boolean error;

  /**
   * Construct a new state.
   *
   * @param running
   *          {@code true} if a running state
   * @param error
   *          {@code true} if an error state
   */
  RuntimeState(boolean running, boolean error) {
    this.running = running;
    this.error = error;
  }

  /**
   * Is the state a running state?
   *
   * @return {@code true} if running
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Is the state an error state?
   *
   * @return {@code true} if an error
   */
  public boolean isError() {
    return error;
  }
}
