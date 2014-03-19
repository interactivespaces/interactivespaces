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

import org.apache.commons.logging.Log;

import java.util.concurrent.Future;

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
public class ManagedCommand {

  /**
   * The command to be run.
   */
  private final Runnable command;

  /**
   * The task of the command.
   */
  private final WrappedTask task;

  /**
   * The managed commands this command is part of.
   */
  private final ManagedCommands managedCommands;

  /**
   * The future associated with this task.
   */
  private Future<?> future;

  /**
   * The logger to use.
   */
  private final Log log;

  /**
   * Construct a managed command for the given wrapped task.
   *
   * @param command
   *          the command to run
   * @param managedCommands
   *          the managed commands that the command is part of
   * @param repeating
   *          {@code true} if this command will be repeating
   * @param allowTerminate
   *          {@code true} if the repeating command should be allowed to
   *          terminate if an exception is thrown
   * @param log
   *          the log to use
   */
  ManagedCommand(Runnable command, ManagedCommands managedCommands, boolean repeating, boolean allowTerminate, Log log) {
    this.command = command;
    this.task = new WrappedTask(command, repeating, allowTerminate);
    this.managedCommands = managedCommands;
    this.log = log;
  }

  /**
   * Set the future for this task.
   *
   * @param future
   *          the future
   */
  void setFuture(Future<?> future) {
    this.future = future;
  }

  /**
   * Cancel the task whether or not it was running.
   */
  public void cancel() {
    synchronized (managedCommands) {
      future.cancel(true);
      managedCommands.removeManagedCommand(this);
    }
  }

  /**
   * Has the command been cancelled?
   *
   * @return {@code true} if cancelled
   */
  public boolean isCancelled() {
    return future.isCancelled();
  }

  /**
   * Is the command done?
   *
   * @return {@code true} if done
   */
  public boolean isDone() {
    return future.isDone();
  }

  /**
   * Get the task for the command.
   *
   * @return the wrapped task
   */
  WrappedTask getTask() {
    return task;
  }

  /**
   * A runnable that logs exceptions.
   *
   * @author Keith M. Hughes
   */
  class WrappedTask implements Runnable {

    /**
     * The actual runnable to be run.
     */
    private final Runnable delegate;

    /**
     * {@code true} if this task is a repeating task.
     */
    private final boolean repeating;

    /**
     * {@code true} if this task should terminate if an exception happens.
     */
    private final boolean allowTerminate;

    /**
     * Construct a new wrapped task.
     *
     * @param delegate
     *          the actual runnable to run
   * @param repeating
   *          {@code true} if this delegate will be repeating
     * @param allowTerminate
     *          {@code true} if the potentially periodic running of the delegate
     *          can stop the periodic running
     */
    WrappedTask(Runnable delegate, boolean repeating, boolean allowTerminate) {
      this.delegate = delegate;
      this.repeating = repeating;
      this.allowTerminate = allowTerminate;
    }

    @Override
    public void run() {
      try {
        delegate.run();
      } catch (Throwable e) {
        log.error("Exception caught during Managed Command", e);

        if (allowTerminate) {
          // This guarantees the future will stop immediately
          throw new RuntimeException();
        }
      } finally {
        if (!repeating) {
          managedCommands.removeManagedCommand(ManagedCommand.this);
        }
      }
    }
  }
}
