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
 * Reference to a Managed Command.
 *
 * <p>
 * These are commands placed into {@link ManagedCommands} which will either be
 * shut down automatically or can be shut down by the user.
 *
 * <p>
 * The API for the Managed Command is very simple. If can be cancelled if it
 * sound be finished early, and it is possible to check its running state.
 *
 * <p>
 * There is no way to restart the command. It must be submitted again.
 *
 * @author Keith M. Hughes
 */
public interface ManagedCommand {

  /**
   * Cancel the command whether or not it was running.
   */
  void cancel();

  /**
   * Has the command been cancelled?
   *
   * @return {@code true} if cancelled
   */
  boolean isCancelled();

  /**
   * Is the command done?
   *
   * @return {@code true} if done
   */
  boolean isDone();
}
