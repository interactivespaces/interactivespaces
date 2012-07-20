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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
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
 * @since Jul 10, 2012
 */
public class CommandCollection {

	/**
	 * All futures in collection.
	 */
	private List<Future<?>> futures = Collections
			.synchronizedList(new ArrayList<Future<?>>());

	/**
	 * The executor service for this collection.
	 */
	private ScheduledExecutorService executorService;

	public CommandCollection(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Submit a command to start immediately.
	 * 
	 * @param command
	 *            the command to run
	 */
	public void submit(Runnable command) {
		futures.add(executorService.submit(command));
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
	 */
	public void schedule(Runnable command, long delay, TimeUnit unit) {
		futures.add(executorService.schedule(command, delay, unit));
	}

	/**
	 * Executes a periodic command that starts after the given initial delay,
	 * and subsequently with the given delay between the termination of one
	 * execution and the commencement of the next. If any execution of the task
	 * encounters an exception, the exception is ignored. The task will only
	 * terminate via cancellation or termination of the executor.
	 * 
	 * @param command
	 *            the command to run
	 * @param initialDelay
	 *            how soon in the future to run it the first time
	 * @param delay
	 *            how long to delay between subsequent executions
	 * @param unit
	 *            time units for both delays
	 */
	public void scheduleAtFixedRate(Runnable command, long initialDelay,
			long period, TimeUnit unit) {
		futures.add(executorService.scheduleAtFixedRate(command, initialDelay,
				period, unit));
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
	 */
	public void scheduleWithFixedDelay(Runnable command, long initialDelay,
			long delay, TimeUnit unit) {
		futures.add(executorService.scheduleWithFixedDelay(command,
				initialDelay, delay, unit));
	}

	/**
	 * Shut down all executing commands or commands which haven't started yet.
	 */
	public void shutdownAll() {
		for (Future<?> f : futures) {
			f.cancel(true);
		}
		futures.clear();
	}
}
