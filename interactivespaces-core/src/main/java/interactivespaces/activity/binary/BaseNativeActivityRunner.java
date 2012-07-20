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

package interactivespaces.activity.binary;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.process.restart.RestartStrategy;
import interactivespaces.util.process.restart.RestartStrategyInstance;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;

/**
 * A support superclass for {@link NativeActivityRunner} implementations.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseNativeActivityRunner implements NativeActivityRunner {

	/**
	 * The default number of milliseconds to attempt a restart.
	 */
	public static final int RESTART_DURATION_MAXIMUM_DEFAULT = 10000;

	/**
	 * Configuration for the activity.
	 */
	protected Map<String, Object> config;

	/**
	 * Process running the native app.
	 */
	protected Process process;

	/**
	 * Lock for working with the restarter.
	 */
	private Lock restartLock = new ReentrantLock(true);

	/**
	 * When a restart began.
	 */
	private long restartBegin = 0;

	/**
	 * The number of milliseconds to attempt a restart.
	 */
	private long restartDurationMaximum = RESTART_DURATION_MAXIMUM_DEFAULT;

	/**
	 * The strategy for restarting.
	 */
	private RestartStrategy restartStrategy;

	/**
	 * The strategy instance being used to restart.
	 * 
	 * <p>
	 * Will be {@code null} if there is no restart being attempted.
	 */
	private RestartStrategyInstance restarter;

	/**
	 * Process for the restart.
	 */
	private Process restartProcess;

	/**
	 * The space environment.
	 */
	protected InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Logger for the runner.
	 */
	protected Log log;

	/**
	 * The commands to be handed to exec.
	 */
	private String[] commands;

	public BaseNativeActivityRunner(
			InteractiveSpacesEnvironment spaceEnvironment, Log log) {
		this.spaceEnvironment = spaceEnvironment;
		this.log = log;
	}

	@Override
	public void configure(Map<String, Object> config) {
		this.config = config;
	}

	@Override
	public void startup() {
		commands = getCommand();

		if (spaceEnvironment.getLog().isInfoEnabled()) {
			StringBuilder appLine = new StringBuilder();
			for (String c : commands)
				appLine.append(c).append(' ');
			spaceEnvironment.getLog().info(
					String.format("Native activity starting up %s", appLine));
		}

		process = attemptRun();
	}

	/**
	 * Attempt the run.
	 * 
	 * @param command
	 *            the command to run
	 * 
	 * @return the process that was created
	 */
	private Process attemptRun() {
		try {
			return Runtime.getRuntime().exec(commands);
		} catch (IOException e) {
			throw new InteractiveSpacesException("Can't start up activity "
					+ commands, e);
		}
	}

	@Override
	public void shutdown() {
		if (restarter != null) {
			restarter.quit();
			restarter = null;

			if (restartProcess != null) {
				restartProcess.destroy();
				restartProcess = null;
			}
		}

		if (process != null) {

			process.destroy();

			process = null;
		}
	}

	@Override
	public boolean isRunning() {
		if (process != null) {
			try {
				int exitValue = process.exitValue();

				if (handleProcessExit(exitValue, commands)) {
					// If restarter is working, the outside should be told
					// that we are still "running" until the restarter punts.
					return startRestarter();
				} else {
					spaceEnvironment.getLog().error(
							"Activity stopped running, not attempting restart");
				}
			} catch (IllegalThreadStateException e) {
				// Can't get exit value if process is still running.

				return true;
			}
		}

		// If here the process isn't there, so running is dependent on the
		// restarter, if any.
		return isRestarterActive();
	}

	/**
	 * Start the restarter, unless it is running already.
	 * 
	 * @return {@code true} if the restarter is working
	 */
	private boolean startRestarter() {
		restartLock.lock();
		try {

			process = null;

			if (restartStrategy != null) {
				spaceEnvironment.getLog().error(
						"Activity stopped running, attempting restart");

				restartBegin = spaceEnvironment.getTimeProvider()
						.getCurrentTime();
				restarter = restartStrategy.newInstance(this);

				return true;
			} else {
				spaceEnvironment.getLog().error(
						"Activity stopped running, not attempting restart");
			}

			return false;
		} finally {
			restartLock.unlock();
		}
	}

	/**
	 * Is there an active restarter?
	 * 
	 * <p>
	 * Will quit and remove any restarter if it has been running for too long
	 * 
	 * @return {@code true} if a restarter is still attempting a restart
	 */
	private boolean isRestarterActive() {
		restartLock.lock();
		try {
			if (restarter != null) {
				if (restarter.isRestarting()) {
					long restartDuration = spaceEnvironment.getTimeProvider()
							.getCurrentTime() - restartBegin;

					if (restartDuration > restartDurationMaximum) {
						spaceEnvironment
								.getLog()
								.error("Activity would not restart. Maximum duration time passed.");
						restarter.quit();
						restartComplete(false);

						return false;
					}

					return true;
				}
			}

			return false;
		} finally {
			restartLock.unlock();
		}
	}

	/**
	 * Respond to a given return value.
	 * 
	 * @param exitValue
	 *            the value returned by the process
	 * @param commands
	 *            the commands being run
	 * 
	 * @return {@code true} if should attempt a restart.
	 */
	public abstract boolean handleProcessExit(int exitValue, String[] commands);

	@Override
	public void attemptRestart() {
		restartProcess = attemptRun();
	}

	@Override
	public boolean isRestarted() {
		if (restartProcess != null) {
			try {
				restartProcess.exitValue();

				return false;
			} catch (IllegalThreadStateException e) {
				// Can't get exit value if process is still running.

				return true;
			}
		} else {
			return process != null;
		}
	}

	@Override
	public void restartComplete(boolean success) {
		restartLock.lock();
		try {
			if (success) {
				process = restartProcess;
				spaceEnvironment.getLog().info("Restart successful");
			}
			restartProcess = null;
			restarter = null;
		} finally {
			restartLock.unlock();
		}
	}

	@Override
	public void setRestartStrategy(RestartStrategy restartStrategy) {
		this.restartStrategy = restartStrategy;
	}

	@Override
	public void setRestartDurationMaximum(long restartDurationMaximum) {
		this.restartDurationMaximum = restartDurationMaximum;
	}

	/**
	 * Get the app to run from the config.
	 * 
	 * @return the process command line
	 */
	public abstract String[] getCommand();
}