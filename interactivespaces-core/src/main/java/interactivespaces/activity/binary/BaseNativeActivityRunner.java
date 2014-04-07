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
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.restart.RestartStrategy;
import interactivespaces.util.process.restart.RestartStrategyInstance;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
   * File support instance to use for this activity runner.
   */
  private static final FileSupport FILE_SUPPORT = FileSupportImpl.INSTANCE;

  /**
   * Configuration map for this native activity.
   */
  private Map<String, Object> config;

  /**
   * Process running the native app.
   */
  private Process process;

  /**
   * Lock for working with processes.
   */
  private final Lock processLock = new ReentrantLock(true);

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
  private volatile RestartStrategy restartStrategy;

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
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Logger for the runner.
   */
  private final Log log;

  /**
   * The commands to be handed to exec.
   */
  private String[] commands;

  /**
   * Folder which contains the executable.
   */
  private File executableFolder;

  /**
   * Name of the application to run.
   */
  private String appName;

  /**
   * List of the command flags.
   */
  private String commandFlags;

  /**
   * Create a native activity runner.
   *
   * @param spaceEnvironment
   *          environment for runner
   * @param log
   *          logger for logging
   */
  public BaseNativeActivityRunner(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public void configure(Map<String, Object> config) {
    this.config = config;

    appName = (String) getConfig().get(ACTIVITYNAME);
    if (appName == null) {
      throw new SimpleInteractiveSpacesException("Missing property " + ACTIVITYNAME);
    }

    commandFlags = (String) getConfig().get(FLAGS);
    if (commandFlags == null) {
      throw new SimpleInteractiveSpacesException("Missing property " + FLAGS);
    }
  }

  @Override
  public void startup() {
    processLock.lock();
    try {
      commands = getCommand();

      String executable = commands[0];
      executableFolder = new File(executable.substring(0, executable.lastIndexOf("/")));

      if (spaceEnvironment.getLog().isInfoEnabled()) {
        String appLine = Joiner.on(' ').join(commands);
        spaceEnvironment.getLog().info(String.format("Native activity starting up %s", appLine));
      }

      process = attemptRun();
    } finally {
      processLock.unlock();
    }
  }

  /**
   * Attempt the run.
   *
   * @return the process that was created
   */
  private Process attemptRun() {
    try {
      ProcessBuilder builder = new ProcessBuilder(commands);
      builder.directory(executableFolder);

      spaceEnvironment.getLog().info(
          String.format("Starting up native code in folder %s", executableFolder.getAbsolutePath()));

      return builder.start();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Can't start up activity " + appName, e);
    }
  }

  @Override
  public void shutdown() {
    processLock.lock();
    try {
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
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public boolean isRunning() {
    processLock.lock();
    try {
      if (process != null) {
        try {
          int exitValue = process.exitValue();

          logProcessResultStreams();

          if (handleProcessExit(exitValue, commands)) {
            // If restarter is working, the outside should be told
            // that we are still "running" until the restarter punts.
            return startRestarter();
          } else {
            spaceEnvironment.getLog().error("Activity stopped running, not attempting restart");
          }
        } catch (IllegalThreadStateException e) {
          // Can't get exit value if process is still running.

          logProcessResultStreams();

          return true;
        }
      }

      // If here the process isn't there, so running is dependent on the
      // restarter, if any.
      return isRestarterActive();
    } finally {
      processLock.unlock();
    }
  }

  /**
   * Handle the process result streams for this process, copying the results to
   * the appropriate info or error logs.
   */
  private void logProcessResultStreams() {
    try {
      InputStream inputStream = process.getInputStream();
      String inputString = FILE_SUPPORT.readAvailableToString(inputStream);
      if (!Strings.isNullOrEmpty(inputString)) {
        spaceEnvironment.getLog().info(inputString);
      }

      InputStream errorStream = process.getErrorStream();
      String errorString = FILE_SUPPORT.readAvailableToString(errorStream);
      if (!Strings.isNullOrEmpty(errorString)) {
        spaceEnvironment.getLog().error(errorString);
      }
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Error reading process streams", e);
    }
  }

  /**
   * Start the restarter, unless it is running already.
   *
   * @return {@code true} if the restarter is working
   */
  private boolean startRestarter() {
    process = null;

    // make sure we only sample the restart strategy once in a threadsafe way
    RestartStrategy rs = restartStrategy;
    if (rs != null) {
      spaceEnvironment.getLog().error("Activity stopped running, attempting restart");

      restartBegin = spaceEnvironment.getTimeProvider().getCurrentTime();
      restarter = rs.newInstance(this);

      return true;
    } else {
      spaceEnvironment.getLog().error("Activity stopped running, not attempting restart");
    }

    return false;
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
    if (restarter != null) {
      if (restarter.isRestarting()) {
        long restartDuration = spaceEnvironment.getTimeProvider().getCurrentTime() - restartBegin;

        if (restartDuration > restartDurationMaximum) {
          spaceEnvironment.getLog().error("Activity would not restart. Maximum duration time passed.");
          restarter.quit();
          restartComplete(false);

          return false;
        }

        return true;
      }
    }

    return false;
  }

  /**
   * Respond to a given return value.
   *
   * @param exitValue
   *          the value returned by the process
   * @param commands
   *          the commands being run
   *
   * @return {@code true} if should attempt a restart.
   */
  public abstract boolean handleProcessExit(int exitValue, String[] commands);

  @Override
  public void attemptRestart() {
    processLock.lock();
    try {
      restartProcess = attemptRun();
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public boolean isRestarted() {
    processLock.lock();
    try {
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
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public void restartComplete(boolean success) {
    processLock.lock();
    try {
      if (success) {
        process = restartProcess;
        spaceEnvironment.getLog().info("Restart successful");
      }
      restartProcess = null;
      restarter = null;
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public void setRestartStrategy(RestartStrategy restartStrategy) {
    this.restartStrategy = restartStrategy;
  }

  @Override
  public RestartStrategy getRestartStrategy() {
    return restartStrategy;
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
  protected String[] getCommand() {
    List<String> builder = new ArrayList<String>();

    builder.add(appName);

    for (String arg : commandFlags.split("\\s")) {
      builder.add(arg);
    }

    // Build up the command line.
    for (Map.Entry<String, Object> entry : getConfig().entrySet()) {
      if (ACTIVITYNAME.equals(entry.getKey()) || FLAGS.equals(entry.getKey())) {
        continue;
      }

      String arg = " --" + entry.getKey();
      Object value = entry.getValue();
      if (value != null) {
        arg += "=" + value.toString();
      }

      builder.add(arg);
    }

    return builder.toArray(new String[builder.size()]);
  }

  /**
   * @return configuration for the activity
   */
  public Map<String, Object> getConfig() {
    return config;
  }

  /**
   * @return the space environment
   */
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  /**
   * @return logger for the runner
   */
  public Log getLog() {
    return log;
  }
}
