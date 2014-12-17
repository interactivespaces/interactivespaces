/*
 * Copyright (C) 2014 Google Inc.
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
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.time.TimeProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * An Interactive spaces environment giving access to portions of the environment for modification.
 *
 * @author Keith M. Hughes
 */
public interface InternalInteractiveSpacesEnvironment {

  /**
   * Set the file system for the environment.
   *
   * @param filesystem
   *          the filesystem
   */
  void setFilesystem(InteractiveSpacesFilesystem filesystem);

  /**
   * Set the network type for Interactive Spaces.
   *
   * <p>
   * This allows distinguishing between Interactive Spaces networks, e.g. localdev, prod, fredbot.
   *
   * @param networkType
   *          the network type
   */
  void setNetworkType(String networkType);

  /**
   * Set the executor service for the environment.
   *
   * @param executorService
   *          the executorService
   */
  void setExecutorService(ScheduledExecutorService executorService);

  /**
   * Set the system configuration.
   *
   * @param systemConfiguration
   *          the system configuration
   */
  void setSystemConfiguration(Configuration systemConfiguration);

  /**
   * Change the system configuration by taking the configuration already available and making it the parent of the
   * configuration supplied here.
   *
   * @param newTopSystemConfiguration
   *          the new top of the system configuration
   */
  void changeSystemConfigurationTop(Configuration newTopSystemConfiguration);

  /**
   * set the time provider.
   *
   * @param timeProvider
   *          the time provider
   */
  void setTimeProvider(TimeProvider timeProvider);

  /**
   * Set the logging provider.
   *
   * @param loggingProvider
   *          the logging provider
   */
  void setLoggingProvider(LoggingProvider loggingProvider);
}
