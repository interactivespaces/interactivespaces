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

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.InteractiveSpacesUtilities;

import java.util.concurrent.ScheduledExecutorService;

/**
 * A {@link RestartStrategy} which will attempt a number of restarts.
 * 
 * @author Keith M. Hughes
 */
public class LimitedRetryRestartStrategy implements RestartStrategy {

	/**
	 * The number of retries to attempt.
	 */
	private int numberRetries;

	/**
	 * The delay between samples, in milliseconds.
	 */
	private long sampleDelay;

	/**
	 * The time after something starts for it to be considered a successful
	 * restart, in milliseconds.
	 */
	private long timeDurationSuccessfulRestart;

	/**
	 * The space environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * @param numberRetries
	 *            the total number of retries which can be attempted
	 * @param sampleDelay
	 *            the time between samples for restart detection in milliseconds
	 * @param timeDurationSuccessfulRestart
	 *            the time the item needs to be running before restart is
	 *            considered a success
	 * @param spaceEnvironment
	 *            the space environment to pick up services from
	 */
	public LimitedRetryRestartStrategy(int numberRetries, long sampleDelay,
			long timeDurationSuccessfulRestart,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.numberRetries = numberRetries;
		this.sampleDelay = sampleDelay;
		this.timeDurationSuccessfulRestart = timeDurationSuccessfulRestart;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public RestartStrategyInstance newInstance(Restartable restartable) {
		LimitedTryRestartStrategyInstance instance = new LimitedTryRestartStrategyInstance(
				restartable, numberRetries, sampleDelay,
				timeDurationSuccessfulRestart,
				spaceEnvironment.getTimeProvider(),
				spaceEnvironment.getExecutorService());
		instance.startRestartAttempts();

		return instance;
	}

	/**
	 * A {@link RestartStrategyInstance} which will attempt a given number of
	 * retries.
	 * 
	 * @author Keith M. Hughes
	 */
	public static class LimitedTryRestartStrategyInstance implements
			RestartStrategyInstance {

		/**
		 * The object being restarted.
		 */
		private Restartable restartable;

		/**
		 * The number of retries left.
		 */
		private int numberRetriesLeft;

		/**
		 * Last timestamp a retry was started.
		 */
		private long lastAttemptTime = 0;

		/**
		 * The time provider.
		 */
		private TimeProvider timeProvider;

		/**
		 * The executor service to give threads.
		 */
		private ScheduledExecutorService executorService;

		/**
		 * Start it off as running.
		 */
		private volatile boolean running = true;

		/**
		 * The delay between samples, in milliseconds.
		 */
		private long sampleDelay;

		/**
		 * The time after something starts for it to be considered a successful
		 * restart, in milliseconds.
		 */
		private long timeDurationSuccessfulRestart;

		/**
		 * @param restartable
		 *            the object being restarted
		 * @param numberRetries
		 *            the total number of retries which can be attempted
		 * @param sampleDelay
		 *            the time between samples for restart detection in
		 *            milliseconds
		 * @param timeDurationSuccessfulRestart
		 *            the time the item needs to be running before restart is
		 *            considered a success
		 * @param timeProvider
		 *            the provider for time
		 * @param executorService
		 *            the thread pool to use
		 */
		public LimitedTryRestartStrategyInstance(Restartable restartable,
				int numberRetries, long sampleDelay,
				long timeDurationSuccessfulRestart, TimeProvider timeProvider,
				ScheduledExecutorService executorService) {
			this.restartable = restartable;
			this.numberRetriesLeft = numberRetries;
			this.sampleDelay = sampleDelay;
			this.timeDurationSuccessfulRestart = timeDurationSuccessfulRestart;
			this.timeProvider = timeProvider;
			this.executorService = executorService;
		}

		/**
		 * Do the first restart attempt.
		 */
		public void startRestartAttempts() {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					attemptRestart();

					while (running && !Thread.interrupted()) {
						InteractiveSpacesUtilities
								.delay(getCurrentSampleDelay());
						repeatedRestartAttempt();
					}

				}
			});
		}

		/**
		 * Attempt to do another restart, but only if necessary.
		 */
		public void repeatedRestartAttempt() {
			if (restartable.isRestarted()) {
				long currentUptimeDuration = timeProvider.getCurrentTime()
						- lastAttemptTime;
				if (currentUptimeDuration > timeDurationSuccessfulRestart) {
					running = false;
					restartable.restartComplete(true);
				}
			} else {
				if (numberRetriesLeft > 0) {
					attemptRestart();
				} else {
					running = false;
					restartable.restartComplete(false);
				}
			}
		}

		/**
		 * Attempt a restart.
		 */
		public void attemptRestart() {
			numberRetriesLeft--;
			lastAttemptTime = timeProvider.getCurrentTime();
			restartable.attemptRestart();
		}

		@Override
		public void quit() {
			running = false;
		}

		@Override
		public boolean isRestarting() {
			return running;
		}

		/**
		 * Get the current delay which should happen between samples.
		 * 
		 * @return the current delay, in milliseconds
		 */
		public long getCurrentSampleDelay() {
			return sampleDelay;
		}
	}
}
