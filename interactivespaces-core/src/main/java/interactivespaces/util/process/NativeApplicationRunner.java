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

package interactivespaces.util.process;

import interactivespaces.util.process.restart.RestartStrategy;
import interactivespaces.util.process.restart.Restartable;
import interactivespaces.util.resource.ManagedResource;

import java.util.Map;

/**
 * A launcher of apps native to the given system.
 *
 * <p>
 * The configuration needs a property with name {#ACTIVITYNAME} which gives the full descriptor (e.g. path on a Linux
 * system) to the application.
 *
 * @author Keith M. Hughes
 */
public interface NativeApplicationRunner extends ManagedResource, Restartable {

  /**
   * The escape character for the flags configuration.
   */
  char ESCAPE_CHARACTER = '\\';

  /**
   * The equals character for environment variables.
   */
  char EQUALS_CHARACTER = '=';

  /**
   * The name of the property which gives the fully qualified name for the application.
   */
  String EXECUTABLE_PATHNAME = "executablePath";

  /**
   * A set of flags for the application as a string to be parsed.
   */
  String EXECUTABLE_FLAGS = "executableFlags";

  /**
   * A set of environment variables for the application as a string to be parsed.
   */
  String EXECUTABLE_ENVIRONMENT = "executableEnvironment";

  /**
   * A map containing environment variables. Entries with a {@code null} value will be removed from the process
   * environment.
   */
  String EXECUTABLE_ENVIRONMENT_MAP = "executableEnvironmentMap";

  /**
   * The name of the property which gives the fully qualified name for the application.
   *
   * @deprecated use {@link #EXECUTABLE_PATHNAME}
   */
  @Deprecated
  String ACTIVITYNAME = EXECUTABLE_PATHNAME;

  /**
   * A set of flags for the application.
   *
   * @deprecated use {@link #EXECUTABLE_FLAGS}
   */
  @Deprecated
  String FLAGS = EXECUTABLE_FLAGS;

  /**
   * Set whether the process environment should be fully cleaned before the process is started.
   *
   * @param cleanEnvironment
   *          {@code true} if the environment should be cleaned
   */
  void setCleanEnvironment(boolean cleanEnvironment);

  /**
   * Set the executable path for the runner.
   *
   * @param executablePath
   *          the executable path
   *
   * @return this runner
   */
  NativeApplicationRunner setExecutablePath(String executablePath);

  /**
   * Add in a collection of environment variables.
   *
   * @param environmentVariables
   *          a map of new environment variables to add, indexed by variable name
   *
   * @return this runner
   */
  NativeApplicationRunner addEnvironmentVariables(Map<String, String> environmentVariables);

  /**
   * Add in new command line arguments to the runner.
   *
   * <p>
   * The arguments will be supplied to the executable in the order added.
   *
   * @param arguments
   *          the arguments to add
   *
   * @return this runner
   */
  NativeApplicationRunner addCommandArguments(String... arguments);

  /**
   * Parse a string of command line arguments. Arguments are separated by whitespace. The character
   * {@link #ESCAPE_CHARACTER} is used for the escape character.
   *
   * <p>
   * The arguments will be supplied to the executable in the order added.
   *
   * @param arguments
   *          the arguments to add
   *
   * @return this runner
   */
  NativeApplicationRunner parseCommandArguments(String arguments);

  /**
   * Parse a string of command line arguments. Arguments are separated by whitespace. The character
   * {@link #ESCAPE_CHARACTER} is used for the escape character.
   *
   * <p>
   * The arguments will be supplied to the executable in the order added.
   *
   * @param variables
   *          the environment variables to add
   *
   * @return this runner
   */
  NativeApplicationRunner parseEnvironment(String variables);

  /**
   * Configure the runner.
   *
   * @param config
   *          the configuration
   *
   * @deprecated use the individual setters and parsers or a {@link NativeApplicationDescription}
   */
  @Deprecated
  void configure(Map<String, Object> config);

  /**
   * Configure the runner.
   *
   * @param description
   *          the description
   */
  void configure(NativeApplicationDescription description);

  /**
   * Is the native application still running?
   *
   * @return {@code true} if the application is still running
   */
  boolean isRunning();

  /**
   * Get the current state of the runner.
   *
   * @return the current state of the runner
   */
  NativeApplicationRunnerState getState();

  /**
   * Set the restart strategy for the runner.
   *
   * @param restartStrategy
   *          the strategy to be used, can be {@code null}
   */
  void setRestartStrategy(RestartStrategy<NativeApplicationRunner> restartStrategy);

  /**
   * Get the restart strategy for the runner.
   *
   * @return the strategy being used, can be {@link null}
   */
  RestartStrategy<NativeApplicationRunner> getRestartStrategy();

  /**
   * Set how long to attempt a restart.
   *
   * @param restartDurationMaximum
   *          the restart attempt duration in milliseconds
   */
  void setRestartDurationMaximum(long restartDurationMaximum);

  /**
   * Add in a new application runner listener.
   *
   * <p>
   * Any listeners added will be automatically added to any {@link RestartStrategy} no matter when the strategy is
   * added.
   *
   * @param listener
   *          the listener to add
   */
  void addNativeApplicationRunnerListener(NativeApplicationRunnerListener listener);

  /**
   * Remove application runner listener.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeNativeApplicationRunnerListener(NativeApplicationRunnerListener listener);

  /**
   * The state of the native application runner.
   *
   * @author Keith M. Hughes
   */
  public enum NativeApplicationRunnerState {

    /**
     * The runner has never been started.
     */
    NOT_STARTED,

    /**
     * The runner is starting.
     */
    STARTING,

    /**
     * The application cannot be started.
     */
    STARTUP_FAILED,

    /**
     * The application is now running.
     */
    RUNNING,

    /**
     * The application is restarting.
     */
    RESTARTING,

    /**
     * The application restart has failed.
     */
    RESTART_FAILED,

    /**
     * The application is shut down.
     */
    SHUTDOWN,

    /**
     * The application crashed.
     */
    CRASHED,
  }
}
