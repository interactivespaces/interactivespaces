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

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

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
	 * All tasks in collection.
	 */
	private Set<WrappedTask> tasks = Sets.newHashSet();

	/**
	 * The executor service for this collection.
	 */
	private ScheduledExecutorService executorService;

	/**
	 * Object for locking certain operations.
	 */
	private Object lock = new Object();

	/**
	 * The logger for the collection.
	 */
	private Log log;

	public ManagedCommands(ScheduledExecutorService executorService, Log log) {
		this.executorService = executorService;
		this.log = log;
	}

	/**
	 * Submit a command to start immediately.
	 * 
	 * @param command
	 *            the command to run
	 * 
	 * @return the managed command for the task
	 */
	public ManagedCommand submit(Runnable command) {
		WrappedTask task = newWrappedTask(command, false, false);

		Future<?> future = executorService.submit(task);

		synchronized (lock) {
			task.setFuture(future);

			tasks.add(task);
		}

		return new ManagedCommand(task);
	}

	/**
	 * Schedule a new command with a delay.
	 * 
	 * @param command
	 *            the command to run
	 * @param delay
	 *            how soon in the future to run it
	 * @param unit
	 *            units on how soon to start
	 * 
	 * @return the managed command for the task
	 */
	public ManagedCommand schedule(Runnable command, long delay, TimeUnit unit) {
		WrappedTask task = newWrappedTask(command, false, false);

		Future<?> future = executorService.schedule(task, delay, unit);

		synchronized (lock) {
			task.setFuture(future);

			tasks.add(task);
		}

		return new ManagedCommand(task);
	}

	/**
	 * Executes a periodic command that starts after the given initial delay,
	 * and subsequently with the given delay between the termination of one
	 * execution and the commencement of the next. If any execution of the task
	 * encounters an exception, the exception is ignored. The task will only
	 * terminate via cancellation or termination of the executor.
	 * 
	 * <p>
	 * If the command throws an exception, it will not be repeated. The
	 * exception will be logged.
	 * 
	 * @param command
	 *            the command to run
	 * @param initialDelay
	 *            how soon in the future to run it the first time
	 * @param delay
	 *            how long to delay between subsequent executions
	 * @param unit
	 *            time units for both delays
	 * 
	 * @return the managed command for the task
	 */
	public ManagedCommand scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return scheduleAtFixedRate(command, initialDelay, period, unit, true);
	}

	/**
	 * Executes a periodic command that starts after the given initial delay,
	 * and subsequently with the given delay between the termination of one
	 * execution and the commencement of the next. If any execution of the task
	 * encounters an exception, the exception is ignored. The task will only
	 * terminate via cancellation or termination of the executor.
	 * 
	 * <p>
	 * If the command throws an exception, the exception will be logged.
	 * 
	 * @param command
	 *            the command to run
	 * @param initialDelay
	 *            how soon in the future to run it the first time
	 * @param delay
	 *            how long to delay between subsequent executions
	 * @param unit
	 *            time units for both delays
	 * @param allowTerminate
	 *            {@code true} if the task should be allowed to terminate if it
	 *            throws an exception
	 * 
	 * @return the managed command for the task
	 */
	public ManagedCommand scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit,
			boolean allowTerminate) {
		WrappedTask task = newWrappedTask(command, true, allowTerminate);

		Future<?> future = executorService.scheduleAtFixedRate(task,
				initialDelay, period, unit);

		synchronized (lock) {
			task.setFuture(future);

			tasks.add(task);
		}

		return new ManagedCommand(task);
	}

	/**
	 * Executes a periodic command that starts after the given initial delay,
	 * and subsequently with the given delay between the termination of one
	 * execution and the commencement of the next. If any execution of the task
	 * encounters an exception, subsequent executions are suppressed. Otherwise,
	 * the task will only terminate via cancellation or termination of the
	 * executor.
	 * 
	 * <p>
	 * If the command throws an exception, it will not be repeated. The
	 * exception will be logged.
	 * 
	 * @param command
	 *            the command to run
	 * @param initialDelay
	 *            how soon in the future to run it the first time
	 * @param delay
	 *            how long to delay between subsequent executions
	 * @param unit
	 *            time units for both delays
	 * 
	 * @return the managed command for the task
	 */
	public ManagedCommand scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return scheduleWithFixedDelay(command, initialDelay, delay, unit, true);
	}

	/**
	 * Executes a periodic command that starts after the given initial delay,
	 * and subsequently with the given delay between the termination of one
	 * execution and the commencement of the next. If any execution of the task
	 * encounters an exception, subsequent executions are suppressed. Otherwise,
	 * the task will only terminate via cancellation or termination of the
	 * executor.
	 * 
	 * @param command
	 *            the command to run
	 * @param initialDelay
	 *            how soon in the future to run it the first time
	 * @param delay
	 *            how long to delay between subsequent executions
	 * @param unit
	 *            time units for both delays
	 * @param allowTerminate
	 *            {@code true} if the task should be allowed to terminate if it
	 *            throws an exception
	 * 
	 * @return the managed command for the task
	 */
	public ManagedCommand scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit, boolean allowTerminate) {
		WrappedTask task = newWrappedTask(command, true, allowTerminate);

		Future<?> future = executorService.scheduleWithFixedDelay(task,
				initialDelay, delay, unit);

		synchronized (lock) {
			task.setFuture(future);

			tasks.add(task);
		}

		return new ManagedCommand(task);
	}

	/**
	 * Shut down all executing commands or commands which haven't started yet.
	 */
	public void shutdownAll() {
		synchronized (lock) {
			for (WrappedTask task : tasks) {
				task.cancel();
			}
			tasks.clear();
		}
	}

	/**
	 * Get a wrapped task.
	 * 
	 * @param runnable
	 *            the runnable to be done in the task
	 * @param repeating
	 *            {@code true} if the runnable is repeating
	 * @param allowTerminate
	 *            {@code true} if the task, if it is repeating, should be
	 *            allowed to terminate if it throws an exception
	 * 
	 * @return the wrapped task
	 */
	private WrappedTask newWrappedTask(Runnable runnable, boolean repeating,
			boolean allowTerminate) {
		return new WrappedTask(runnable, repeating, allowTerminate);
	}

	/**
	 * Remove a command from the task collection.
	 * 
	 * @param task
	 *            the task to remove
	 */
	private void removeCommand(WrappedTask task) {
		synchronized (lock) {
			tasks.remove(task);
		}
	}

	/**
	 * Does the collection contain the command.
	 * 
	 * <p>
	 * Only visible for testing
	 * 
	 * @param command
	 *            the command to check for
	 * 
	 * @return {@code true} if command there
	 */
	boolean contains(ManagedCommand command) {
		synchronized (lock) {
			return tasks.contains(command.getTask());
		}
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
		private Runnable delegate;

		/**
		 * {@code true} if the runnable is repeating.
		 */
		private boolean repeating;

		/**
		 * {@code true} if the repeating runnable is allowed to terminate.
		 */
		private boolean allowTerminate;

		/**
		 * The future for this runnable.
		 */
		private Future<?> future;

		public WrappedTask(Runnable delegate, boolean repeating,
				boolean allowTerminate) {
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

				if (repeating && allowTerminate) {
					removeCommand(this);

					// Let the normal mechanisms actually remove the thread.
					throw new RuntimeException();
				}
			}

			if (!repeating) {
				removeCommand(this);
			}
		}

		/**
		 * @param future
		 *            the future to set
		 */
		public void setFuture(Future<?> future) {
			this.future = future;
		}

		/**
		 * Cancel the task.
		 */
		public void cancel() {
			future.cancel(true);
			removeCommand(this);
		}

		/**
		 * Has the task been cancelled?
		 * 
		 * @return {@code true} if cancelled
		 */
		public boolean isCancelled() {
			return future.isCancelled();
		}

		/**
		 * Is the task done?
		 * 
		 * @return {@code true} if done
		 */
		public boolean isDone() {
			return future.isDone();
		}
	}
}
