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

package interactivespaces.util.process;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.restart.RestartStrategy;
import interactivespaces.util.process.restart.RestartStrategyInstance;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A support superclass for {@link NativeApplicationRunner} implementations.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseNativeApplicationRunner implements NativeApplicationRunner {

  /**
   * The default number of milliseconds to attempt a restart.
   */
  public static final int RESTART_DURATION_MAXIMUM_DEFAULT = 10000;

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
  private volatile RestartStrategy<NativeApplicationRunner> restartStrategy;

  /**
   * The strategy instance being used to restart.
   *
   * <p>
   * Will be {@code null} if there is no restart being attempted.
   */
  private RestartStrategyInstance<NativeApplicationRunner> restarter;

  /**
   * When attempting a restart, this is where the native application process will live until we know that startup has
   * been successful.
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
  private String[] commandLine;

  /**
   * Folder which contains the executable.
   */
  private File executableFolder;

  /**
   * Name of the application to run.
   */
  private String executablePath;

  /**
   * {@code true} if the process environment should be cleaned before the process starts.
   */
  private boolean cleanEnvironment;

  /**
   * A map of environment variables set for the runner.
   */
  private Map<String, String> environment = Maps.newHashMap();

  /**
   * The list of command line arguments for the runner.
   */
  private List<String> commandArguments = Lists.newArrayList();

  /**
   * The application runner listeners.
   */
  private final List<NativeApplicationRunnerListener> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * The current state of the runner.
   */
  private final AtomicReference<NativeApplicationRunnerState> runnerState =
      new AtomicReference<NativeApplicationRunnerState>(NativeApplicationRunnerState.NOT_STARTED);

  /**
   * The parser for runners.
   */
  private NativeApplicationRunnerParser runnerParser;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create a native activity runner.
   *
   * @param runnerParser
   *          the paser for runners
   * @param spaceEnvironment
   *          environment for runner
   * @param log
   *          logger for logging
   */
  public BaseNativeApplicationRunner(NativeApplicationRunnerParser runnerParser,
      InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.runnerParser = runnerParser;
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public void setCleanEnvironment(boolean cleanEnvironment) {
    this.cleanEnvironment = cleanEnvironment;
  }

  @Override
  public NativeApplicationRunner setExecutablePath(String executablePath) {
    this.executablePath = executablePath;

    return this;
  }

  @Override
  public NativeApplicationRunner addEnvironmentVariables(Map<String, String> environmentVariables) {
    environment.putAll(environmentVariables);

    return this;
  }

  @Override
  public NativeApplicationRunner addCommandArguments(String... arguments) {
    if (arguments != null) {
      Collections.addAll(commandArguments, arguments);
    }

    return this;
  }

  @Override
  public NativeApplicationRunner parseCommandArguments(String arguments) {
    runnerParser.parseCommandArguments(commandArguments, arguments);

    return this;
  }

  @Override
  public NativeApplicationRunner parseEnvironment(String variables) {
    runnerParser.parseEnvironment(environment, variables);

    return this;
  }

  @Override
  public void configure(NativeApplicationDescription description) {
    executablePath = description.getExecutablePath();
    commandArguments.addAll(description.getArguments());
    environment.putAll(description.getEnvironment());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void configure(Map<String, Object> config) {
    executablePath = (String) config.get(EXECUTABLE_PATHNAME);
    if (executablePath == null) {
      SimpleInteractiveSpacesException.throwFormattedException("Missing required property " + EXECUTABLE_PATHNAME);
    }

    // Build up the command line.
    for (Map.Entry<String, Object> entry : config.entrySet()) {
      String key = entry.getKey();
      switch (key) {
        case EXECUTABLE_PATHNAME:
          break;
        case EXECUTABLE_FLAGS:
          runnerParser.parseCommandArguments(commandArguments, (String) entry.getValue());
          break;
        case EXECUTABLE_ENVIRONMENT:
          runnerParser.parseEnvironment(environment, (String) entry.getValue());
          break;
        case EXECUTABLE_ENVIRONMENT_MAP:
          environment.putAll((Map<String, String>) entry.getValue());
          break;
        default:
          String arg = " --" + key;
          Object value = entry.getValue();
          if (value != null) {
            arg += "=" + value.toString();
          }

          commandArguments.add(arg);
      }
    }
  }

  /**
   * Prepare the runner.
   */
  @VisibleForTesting
  void prepare() {
    List<String> commandLineComponents = Lists.newArrayList();

    commandLineComponents.add(executablePath);

    commandLineComponents.addAll(commandArguments);

    commandLine = commandLineComponents.toArray(new String[commandLineComponents.size()]);

    String executable = commandLine[0];
    int endIndex = executable.lastIndexOf(File.separatorChar);
    if (endIndex < 0) {
      SimpleInteractiveSpacesException.throwFormattedException(
          "Executable path %s has no embedded file separator characters", executable);
    }
    executableFolder = new File(executable.substring(0, endIndex));
  }

  @Override
  public void startup() {
    processLock.lock();
    try {
      if (!runnerState.compareAndSet(NativeApplicationRunnerState.NOT_STARTED, NativeApplicationRunnerState.STARTING)) {
        log.warn("Attempting to start native application runner which is already running");
        return;
      }

      prepare();

      notifyApplicationStarting();

      if (log.isInfoEnabled()) {
        String appLine = Joiner.on(' ').join(commandLine);
        log.info(String.format("Native application starting up %s", appLine));
      }

      process = attemptRun(true);

      handleApplicationRunning();
    } finally {
      processLock.unlock();
    }
  }

  /**
   * Attempt the run.
   *
   * @param firstTime
   *          {@code true} if this is the first attempt
   *
   * @return the process that was created
   *
   * @throws InteractiveSpacesException
   *           was not able to start the process the first time
   */
  private Process attemptRun(boolean firstTime) throws InteractiveSpacesException {
    try {
      ProcessBuilder builder = new ProcessBuilder(commandLine);

      Map<String, String> processEnvironment = builder.environment();
      if (cleanEnvironment) {
        processEnvironment.clear();
      }
      modifyEnvironment(processEnvironment, environment);

      builder.directory(executableFolder);

      log.info(String.format("Starting up native code in folder %s", executableFolder.getAbsolutePath()));

      return builder.start();
    } catch (Exception e) {
      // Placed here so we can get the exception when thrown.
      if (firstTime) {
        runnerState.set(NativeApplicationRunnerState.STARTUP_FAILED);
        handleApplicationStartupFailed();

        InteractiveSpacesException.throwFormattedException(e, "Can't start up native application " + executablePath);
      }

      return null;
    }
  }

  /**
   * Modify the process environment with the contents of the environment map.
   *
   * @param processEnvironment
   *          the process environment being modified
   * @param modificationEnvironment
   *          the environment containing the modifications
   */
  @VisibleForTesting
  void modifyEnvironment(Map<String, String> processEnvironment, Map<String, String> modificationEnvironment) {
    for (Entry<String, String> entry : modificationEnvironment.entrySet()) {
      String value = entry.getValue();
      if (value != null) {
        processEnvironment.put(entry.getKey(), value);
      } else {
        processEnvironment.remove(entry.getKey());
      }
    }
  }

  @Override
  public void shutdown() {
    processLock.lock();
    try {
      NativeApplicationRunnerState currentState = runnerState.get();
      if (currentState == NativeApplicationRunnerState.NOT_STARTED
          || currentState == NativeApplicationRunnerState.SHUTDOWN) {
        log.warn("Shutting down a native application runner which is either not started or is already shut down");
        return;
      }

      if (restarter != null) {
        restarter.quit();
        restarter = null;

        // If there is a restarter, then there may have been an attempt at
        // restart which hasn't made it into the process instance variable yet.
        if (restartProcess != null) {
          restartProcess.destroy();
          restartProcess = null;
        }
      }

      if (process != null) {
        // If the notifier is shutting the application down so we will wait for
        // the next scan of isRunning().
        // Otherwise we will kill the process the impolite way.
        if (!handleApplicationShutdownRequested()) {
          process.destroy();

          process = null;
          handleApplicationShutdown(NativeApplicationRunnerState.SHUTDOWN);
        }
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
        // The process was running normally. Sample to see if it is still
        // running.
        try {
          int exitValue = process.exitValue();

          logProcessResultStreams();

          boolean successfulShutdown = handleProcessExit(exitValue, commandLine);

          // If restarter is working, the outside should be told
          // that we are still "running" until the restarter punts.
          if (startRestarter()) {
            runnerState.set(NativeApplicationRunnerState.RESTARTING);
            return true;
          }

          // No longer running, is OK that it isn't running or no restarter, so
          // signal done.
          handleApplicationShutdown(successfulShutdown ? NativeApplicationRunnerState.SHUTDOWN
              : NativeApplicationRunnerState.CRASHED);
          return false;
        } catch (IllegalThreadStateException e) {
          // Can't get exit value if process is still running.

          logProcessResultStreams();

          return true;
        }
      }

      // If here the process isn't there, so running is dependent on the
      // restarter, if any.
      if (isRestarterActive()) {
        return true;
      } else {
        handleApplicationShutdown(NativeApplicationRunnerState.CRASHED);

        return false;
      }
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public NativeApplicationRunnerState getState() {
    return runnerState.get();
  }

  /**
   * Handle the process result streams for this process, copying the results to the appropriate info or error logs.
   */
  private void logProcessResultStreams() {
    try {
      InputStream inputStream = process.getInputStream();
      String inputString = fileSupport.readAvailableToString(inputStream);
      if (!Strings.isNullOrEmpty(inputString)) {
        log.info(inputString);
      }

      InputStream errorStream = process.getErrorStream();
      String errorString = fileSupport.readAvailableToString(errorStream);
      if (!Strings.isNullOrEmpty(errorString)) {
        log.error(errorString);
      }
    } catch (Exception e) {
      log.error("Error reading native application process streams", e);
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
    RestartStrategy<NativeApplicationRunner> rs = restartStrategy;
    if (rs != null) {
      log.warn("Native application stopped running, attempting restart");

      restartBegin = spaceEnvironment.getTimeProvider().getCurrentTime();
      restarter = rs.newInstance(this);

      return true;
    } else {
      log.warn("Native application stopped running, not attempting restart");
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
          log.error(String.format("Native application would not restart. Maximum duration time %d passed.",
              restartDurationMaximum));
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
   * Handle a process exit.
   *
   * @param exitValue
   *          the value returned by the process
   * @param commands
   *          the commands being run
   *
   * @return {@code true} if was a successful exit, {@code false} if it was some sort of error exit
   */
  public abstract boolean handleProcessExit(int exitValue, String[] commands);

  @Override
  public void attemptRestart() {
    processLock.lock();
    try {
      restartProcess = attemptRun(false);
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
        log.info("Native application restart successful");
        handleApplicationRunning();
      } else {
        handleApplicationShutdown(NativeApplicationRunnerState.RESTART_FAILED);
      }

      restartProcess = null;
      restarter = null;
    } finally {
      processLock.unlock();
    }
  }

  @Override
  public void setRestartStrategy(RestartStrategy<NativeApplicationRunner> restartStrategy) {
    this.restartStrategy = restartStrategy;

    // Add in all listeners that have been registered so far.
    for (NativeApplicationRunnerListener listener : listeners) {
      restartStrategy.addRestartStrategyListener(listener);
    }
  }

  @Override
  public RestartStrategy<NativeApplicationRunner> getRestartStrategy() {
    return restartStrategy;
  }

  @Override
  public void setRestartDurationMaximum(long restartDurationMaximum) {
    this.restartDurationMaximum = restartDurationMaximum;
  }

  @Override
  public void addNativeApplicationRunnerListener(NativeApplicationRunnerListener listener) {
    listeners.add(listener);

    if (restartStrategy != null) {
      restartStrategy.addRestartStrategyListener(listener);
    }
  }

  @Override
  public void removeNativeApplicationRunnerListener(NativeApplicationRunnerListener listener) {
    listeners.remove(listener);
  }

  /**
   * Notify all listeners that the application is starting.
   */
  private void notifyApplicationStarting() {
    for (NativeApplicationRunnerListener listener : listeners) {
      try {
        listener.onNativeApplicationRunnerStarting(this);
      } catch (Exception e) {
        log.error("Error while notifying a listener about native application starting", e);
      }
    }
  }

  /**
   * The application is now running.
   */
  private void handleApplicationRunning() {
    runnerState.set(NativeApplicationRunnerState.RUNNING);

    for (NativeApplicationRunnerListener listener : listeners) {
      try {
        listener.onNativeApplicationRunnerRunning(this);
      } catch (Exception e) {
        log.error("Error while notifying a listener about native application start up", e);
      }
    }
  }

  /**
   * Notify all listeners that a shutdown request has come along.
   *
   * @return {@code true} if some handler initiated the shutdown
   */
  private boolean handleApplicationShutdownRequested() {
    boolean shutdownHandled = false;
    for (NativeApplicationRunnerListener listener : listeners) {
      try {
        shutdownHandled |= listener.onNativeApplicationRunnerShutdownRequested(this);
      } catch (Exception e) {
        log.error("Error while notifying a listener about native application shutdown request", e);
      }
    }

    return shutdownHandled;
  }

  /**
   * Handle an application shutdown.
   *
   * @param finalState
   *          the final state of the runner
   */
  private void handleApplicationShutdown(NativeApplicationRunnerState finalState) {
    runnerState.set(finalState);

    for (NativeApplicationRunnerListener listener : listeners) {
      try {
        listener.onNativeApplicationRunnerShutdown(this);
      } catch (Exception e) {
        log.error("Error while notifying a listener about native application shutdown", e);
      }
    }
  }

  /**
   * Notify all listeners that the application startup failed.
   */
  private void handleApplicationStartupFailed() {
    for (NativeApplicationRunnerListener listener : listeners) {
      try {
        listener.onNativeApplicationRunnerStartupFailed(this);
      } catch (Exception e) {
        log.error("Error while notifying a listener about native application starting", e);
      }
    }
  }

  /**
   * Get the command line for the runner.
   *
   * @return the command line for the runner
   */
  public String[] getCommandLine() {
    return commandLine;
  }

  /**
   * Get the environment for the runner.
   *
   * @return the environment, will be {@code null} if not configured
   */
  public Map<String, String> getEnvironment() {
    return environment;
  }

  /**
   * Get the space environment for the runner.
   *
   * @return the space environment
   */
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  /**
   * Get the logger for the runner.
   *
   * @return logger for the runner
   */
  public Log getLog() {
    return log;
  }
}
