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

import interactivespaces.util.events.EventDelay;
import interactivespaces.util.events.EventFrequency;

import java.util.concurrent.TimeUnit;

/**
 * A collection of scheduled commands which can be shut down as a whole.
 *
 * <p>
 * Despite being in a concurrency class, this class is not threadsafe between adding commands and shutting the lot down.
 *
 * @author Keith M. Hughes
 */
public interface ManagedCommands {

  /**
   * Submit a command to start immediately.
   *
   * @param command
   *          the command to run
   *
   * @return the managed command
   */
  ManagedCommand submit(Runnable command);

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
  ManagedCommand schedule(Runnable command, long delay, TimeUnit unit);

  /**
   * Executes a periodic command executes at the given frequency period between the commencement of one execution and
   * the commencement of the next. This means it is possible for more than one execution to be happening at the same
   * time if any of the command executions run longer than the period.
   *
   * <p>
   * The command will start immediately.
   *
   * <p>
   * If the command throws an exception, it will not be run again. The exception will be logged.
   *
   * @param command
   *          the command to run
   * @param commandFrequency
   *          the frequency at which the commands should happen
   *
   * @return the managed command
   */
  ManagedCommand scheduleAtFixedRate(Runnable command, EventFrequency commandFrequency);

  /**
   * Executes a periodic command executes at the given frequency period between the commencement of one execution and
   * the commencement of the next. This means it is possible for more than one execution to be happening at the same
   * time if any of the command executions run longer than the period.
   *
   * <p>
   * The command will start immediately.
   *
   * <p>
   * If the command throws an exception, the exception will be logged.
   *
   * @param command
   *          the command to run
   * @param commandFrequency
   *          the frequency at which the commands should happen
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it throws an exception
   *
   * @return the managed command
   */
  ManagedCommand scheduleAtFixedRate(Runnable command, EventFrequency commandFrequency, boolean allowTerminate);

  /**
   * Executes a periodic command that starts after the given initial delay, and subsequently with the given delay
   * between the termination of one execution and the commencement of the next. If any execution of the task encounters
   * an exception, the exception is ignored. The task will only terminate via cancellation or termination of the
   * executor.
   *
   * <p>
   * If the command throws an exception, it will not be repeated. The exception will be logged.
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
  ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

  /**
   * Executes a periodic command that starts after the given initial delay, and subsequently with the given delay
   * between the termination of one execution and the commencement of the next. If any execution of the task encounters
   * an exception, the exception is ignored. The task will only terminate via cancellation or termination of the
   * executor.
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
   *          {@code true} if the task should be allowed to terminate if it throws an exception
   *
   * @return the managed command
   */
  ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit,
      boolean allowTerminate);

  /**
   * Executes a periodic command that starts after the given initial delay, and subsequently with the given delay
   * between the termination of one execution and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or
   * termination of the executor.
   *
   * <p>
   * If the command throws an exception, it will not be repeated. The exception will be logged.
   *
   * @param command
   *          the command to run
   * @param commandDelay
   *          the delay between invocations
   *
   * @return the managed command
   */
  ManagedCommand scheduleWithFixedDelay(Runnable command, EventDelay commandDelay);

  /**
   * Executes a periodic command where the time between the termination of one execution and the commencement of the
   * next is specified by the period of the command frequency.
   *
   * <p>
   * The command will start running immediately.
   *
   * <p>
   * If the command throws an exception, the exception will be logged.
   *
   * @param command
   *          the command to run
   * @param commandDelay
   *          the delay between invocations
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it throws an exception
   *
   * @return the managed command
   */
  ManagedCommand scheduleWithFixedDelay(Runnable command, EventDelay commandDelay, boolean allowTerminate);

  /**
   * Executes a periodic command that starts after the given initial delay, and subsequently with the given delay
   * between the termination of one execution and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or
   * termination of the executor.
   *
   * <p>
   * If the command throws an exception, it will not be repeated. The exception will be logged.
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
  ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);

  /**
   * Executes a periodic command that starts after the given initial delay, and subsequently with the given delay
   * between the termination of one execution and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or
   * termination of the executor.
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
   *          {@code true} if the task should be allowed to terminate if it throws an exception
   *
   * @return the managed command
   */
  ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit,
      boolean allowTerminate);
}
