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

package interactivespaces.util.concurrency;

import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A collection of scheduled commands which can be shut down as a whole.
 *
 * <p>
 * Despite being in a concurrency class, this class is not threadsafe between
 * adding commands and shutting the lot down.
 *
 * @author Keith M. Hughes
 */
public class ManagedCommands {

  /**
   * All managed commands in collection.
   */
  private final Set<ManagedCommand> managedCommands = Sets.newHashSet();

  /**
   * The executor service for this collection.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The logger for the collection.
   */
  private final Log log;

  /**
   * Construct a managed command collection.
   *
   * @param executorService
   *          the executor service for the command
   * @param log
   *          the logger for the command
   */
  public ManagedCommands(ScheduledExecutorService executorService, Log log) {
    this.executorService = executorService;
    this.log = log;
  }

  /**
   * Submit a command to start immediately.
   *
   * @param command
   *          the command to run
   *
   * @return the managed command
   */
  public synchronized ManagedCommand submit(Runnable command) {
    ManagedCommand managedCommand = new ManagedCommand(command, this, false, false, log);
    managedCommand.setFuture(executorService.submit(managedCommand.getTask()));
    managedCommands.add(managedCommand);

    return managedCommand;
  }

  /**
   * Schedule a new command with a delay.
   *
   * @param command
   *          the command to run
   * @param delay
   *          how soon in the future to run it
   * @param unit
   *          units on how soon to start
   *
   * @return the managed command
   */
  public synchronized ManagedCommand schedule(Runnable command, long delay, TimeUnit unit) {
    ManagedCommand managedCommand = new ManagedCommand(command, this, false, false, log);
    managedCommand.setFuture(executorService.schedule(managedCommand.getTask(), delay, unit));
    managedCommands.add(managedCommand);

    return managedCommand;
  }

  /**
   * Executes a periodic command that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, the exception is ignored. The task will only terminate via
   * cancellation or termination of the executor.
   *
   * <p>
   * If the command throws an exception, it will not be repeated. The exception
   * will be logged.
   *
   * @param command
   *          the command to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param period
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   *
   * @return the managed command
   */
  public ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return scheduleAtFixedRate(command, initialDelay, period, unit, true);
  }

  /**
   * Executes a periodic command that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, the exception is ignored. The task will only terminate via
   * cancellation or termination of the executor.
   *
   * <p>
   * If the command throws an exception, the exception will be logged.
   *
   * @param command
   *          the command to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param period
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it
   *          throws an exception
   *
   * @return the managed command
   */
  public synchronized ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit, boolean allowTerminate) {
    ManagedCommand managedCommand = new ManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleAtFixedRate(managedCommand.getTask(), initialDelay, period, unit));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  /**
   * Executes a periodic command that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task
   * will only terminate via cancellation or termination of the executor.
   *
   * <p>
   * If the command throws an exception, it will not be repeated. The exception
   * will be logged.
   *
   * @param command
   *          the command to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param delay
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   *
   * @return the managed command
   */
  public ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return scheduleWithFixedDelay(command, initialDelay, delay, unit, true);
  }

  /**
   * Executes a periodic command that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task
   * will only terminate via cancellation or termination of the executor.
   *
   * @param command
   *          the command to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param delay
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it
   *          throws an exception
   *
   * @return the managed command
   */
  public synchronized ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit, boolean allowTerminate) {
    ManagedCommand managedCommand = new ManagedCommand(command, this, true, allowTerminate, log);
    managedCommand
        .setFuture(executorService.scheduleWithFixedDelay(managedCommand.getTask(), initialDelay, delay, unit));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  /**
   * Shut down all executing commands or commands which haven't started yet.
   */
  public synchronized void shutdownAll() {
    for (ManagedCommand managedCommand : managedCommands) {
      managedCommand.cancel();
    }
    managedCommands.clear();
  }

  /**
   * Remove a managed command from the collection.
   *
   * @param managedCommand
   *          the command to remove
   */
  synchronized void removeManagedCommand(ManagedCommand managedCommand) {
    managedCommands.remove(managedCommand);
  }
}
