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

package interactivespaces.system;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.system.core.configuration.CoreConfiguration;
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.time.TimeProvider;

import org.apache.commons.logging.Log;

import java.util.concurrent.ScheduledExecutorService;

/**
 * The Interactive Spaces environment being run in.
 *
 * @author Keith M. Hughes
 */
public interface InteractiveSpacesEnvironment {

  /**
   * Configuration property containing the Interactive Spaces version.
   */
  String CONFIGURATION_INTERACTIVESPACES_VERSION =
      CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_VERSION;

  /**
   * Configuration property giving the Interactive Spaces container type.
   */
  String CONFIGURATION_CONTAINER_TYPE = "interactivespaces.container.type";

  /**
   * Configuration property value for the master Interactive Spaces container
   * type.
   */
  String CONFIGURATION_CONTAINER_TYPE_MASTER = "master";

  /**
   * Configuration property value for the controller Interactive Spaces
   * container type.
   */
  String CONFIGURATION_CONTAINER_TYPE_CONTROLLER = "controller";

  /**
   * Configuration property giving the Interactive Spaces type, e.g. prod, dev,
   * local.
   */
  String CONFIGURATION_NETWORK_TYPE = "interactivespaces.network.type";

  /**
   * Configuration property giving the host ID for the system.
   */
  String CONFIGURATION_HOSTID = "interactivespaces.hostid";

  /**
   * Configuration property giving the hostname for the system.
   */
  String CONFIGURATION_HOSTNAME = "interactivespaces.host";

  /**
   * Configuration property giving the host address for the system.
   */
  String CONFIGURATION_HOST_ADDRESS = "interactivespaces.host.address";

  /**
   * Configuration property which will be {@code true} if the container should
   * be file controllable.
   *
   * <p>
   * This is a suggestion, the container may not be file controllable. This is
   * used to request that the controller be file controllable.
   */
  String CONFIGURATION_CONTAINER_FILE_CONTROLLABLE = "interactivespaces.container.control.file";

  /**
   * Configuration property which says what the time provider should be.
   */
  String CONFIGURATION_PROVIDER_TIME = "interactivespaces.provider.time";

  /**
   * Configuration property value which says what the time provider should be
   * local.
   */
  String CONFIGURATION_VALUE_PROVIDER_TIME_LOCAL = "local";

  /**
   * Configuration property value which says what the time provider should be
   * ntp.
   */
  String CONFIGURATION_VALUE_PROVIDER_TIME_NTP = "ntp";

  /**
   * Configuration property which says what the URL of the NTP time provider
   * should be.
   */
  String CONFIGURATION_PROVIDER_TIME_NTP_URL = "interactivespaces.provider.time.ntp.url";

  /**
   * Configuration property value which says what the default time provider
   * should be.
   */
  String CONFIGURATION_VALUE_PROVIDER_TIME_DEFAULT = CONFIGURATION_VALUE_PROVIDER_TIME_LOCAL;

  /**
   * Configuration property giving the location of the system's permanent data
   * directory.
   */
  String CONFIGURATION_SYSTEM_FILESYSTEM_DIR_DATA = "system.datadir";

  /**
   * Configuration property giving the location of the system's temp data
   * directory.
   */
  String CONFIGURATION_SYSTEM_FILESYSTEM_DIR_TMP = "system.tmpdir";

  /**
   * The log level for warnings and above.
   */
  String LOG_LEVEL_WARN = LoggingProvider.LOG_LEVEL_WARN;

  /**
   * The log level for trace and above.
   */
  String LOG_LEVEL_TRACE = LoggingProvider.LOG_LEVEL_TRACE;

  /**
   * The log level for no logging.
   */
  String LOG_LEVEL_OFF = LoggingProvider.LOG_LEVEL_OFF;

  /**
   * The log level for info and above.
   */
  String LOG_LEVEL_INFO = LoggingProvider.LOG_LEVEL_INFO;

  /**
   * The log level for debug and above.
   */
  String LOG_LEVEL_DEBUG = LoggingProvider.LOG_LEVEL_DEBUG;

  /**
   * The log level for fatal.
   */
  String LOG_LEVEL_FATAL = LoggingProvider.LOG_LEVEL_FATAL;

  /**
   * The log level for error and above.
   */
  String LOG_LEVEL_ERROR = LoggingProvider.LOG_LEVEL_ERROR;

  /**
   * Get the Interactive Spaces system configuration.
   *
   * @return the system configuration
   */
  Configuration getSystemConfiguration();

  /**
   * Get the Interactive Spaces-wide filesystem.
   *
   * @return the filesystem for the installation
   */
  InteractiveSpacesFilesystem getFilesystem();

  /**
   * Get the {@link ScheduledExecutorService} to be used inside Interactive
   * Spaces.
   *
   * <p>
   * An executor service gives thread pools to be used. Interactive Spaces needs
   * to control as many threads as possible, so anything in Interactive Spaces
   * should try and use this service.
   *
   * @return the executor service to be used for all thread usage
   */
  ScheduledExecutorService getExecutorService();

  /**
   * Get the container log.
   *
   * @return the container's log
   */
  Log getLog();

  /**
   * Get a named log.
   *
   * @param logName
   *          the name the log should have
   * @param level
   *          default logging level
   *
   * @return the requested named log
   */
  Log getLog(String logName, String level);

  /**
   * Modify the log level.
   *
   * <p>
   * This method will only work if the level is legal and the log is modifiable.
   *
   * @param log
   *          the log to modify
   * @param level
   *          the new level
   *
   * @return {@code true} if able to modify the log.
   */
  boolean modifyLogLevel(Log log, String level);

  /**
   * Get the network type for the Interactive Spaces container.
   *
   * <p>
   * This allows distinguishing between different Interactive Spaces networks,
   * e.g. localdev, prod, fredbot.
   *
   * @return network type currently used
   */
  String getNetworkType();

  /**
   * Get the time provider to use.
   *
   * @return the time provider
   */
  TimeProvider getTimeProvider();

  /**
   * Get the service registry.
   *
   * @return the service registry
   */
  ServiceRegistry getServiceRegistry();

  /**
   * Get a value from the environment.
   *
   * @param valueName
   *          the name of the value
   * @param <T>
   *          type of target value
   *
   * @return the requested value, or {@code null} if not found
   */
  <T> T getValue(String valueName);

  /**
   * Set a value in the environment.
   *
   * @param valueName
   *          the name of the value
   * @param value
   *          the value for the name
   */
  void setValue(String valueName, Object value);

  /**
   * Remove a value from the environment.
   *
   * <p>
   * This does nothing if there is no value with the given name.
   *
   * @param valueName
   *          the name of the value
   */
  void removeValue(String valueName);
}
