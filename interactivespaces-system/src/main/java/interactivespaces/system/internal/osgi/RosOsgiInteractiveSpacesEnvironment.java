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

package interactivespaces.system.internal.osgi;

import com.google.common.collect.Maps;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SimpleServiceRegistry;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.time.TimeProvider;

import org.apache.commons.logging.Log;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A {@link InteractiveSpacesEnvironment} which lives in a ROS container.
 *
 * @author Keith M. Hughes
 */
public class RosOsgiInteractiveSpacesEnvironment implements InteractiveSpacesEnvironment {

  /**
   * The system configuration.
   */
  private Configuration systemConfiguration;

  /**
   * The executor service to use for thread pools.
   */
  private ScheduledExecutorService executorService;

  /**
   * The file system for Interactive Spaces.
   */
  private InteractiveSpacesFilesystem filesystem;

  /**
   * Network type for the container.
   *
   * <p>
   * This allows distinguishing between different Interactive Spaces networks,
   * e.g. localdev, prod, fredbot.
   */
  private String networkType;

  /**
   * The time provider for everyone to use.
   */
  private TimeProvider timeProvider;

  /**
   * Values stored in the environment.
   */
  private ConcurrentMap<String, Object> values = Maps.newConcurrentMap();

  /**
   * The service registry.
   */
  private ServiceRegistry serviceRegistry = new SimpleServiceRegistry(this);

  /**
   * The platform logging provider.
   */
  private LoggingProvider loggingProvider;

  @Override
  public Configuration getSystemConfiguration() {
    return systemConfiguration;
  }

  @Override
  public String getNetworkType() {
    return networkType;
  }

  @Override
  public InteractiveSpacesFilesystem getFilesystem() {
    return filesystem;
  }

  @Override
  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public Log getLog() {
    return loggingProvider.getLog();
  }

  @Override
  public Log getLog(String logName, String level) {
    return loggingProvider.getLog(logName, level);
  }

  @Override
  public boolean modifyLogLevel(Log log, String level) {
    return loggingProvider.modifyLogLevel(log, level);
  }

  @Override
  public TimeProvider getTimeProvider() {
    return timeProvider;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  @Override
  public <T> T getValue(String valueName) {
    @SuppressWarnings("unchecked")
    T value = (T) values.get(valueName);

    return value;
  }

  @Override
  public void setValue(String valueName, Object value) {
    values.put(valueName, value);
  }

  @Override
  public void removeValue(String valueName) {
    values.remove(valueName);
  }

  /**
   * @param filesystem
   *          the filesystem to set
   */
  public void setFilesystem(InteractiveSpacesFilesystem filesystem) {
    this.filesystem = filesystem;
  }

  /**
   * The network type for Interactive Spaces.
   *
   * <p>
   * This allows distinguishing between Interactive Spaces networks, e.g.
   * localdev, prod, fredbot.
   *
   * @param networkType
   */
  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  /**
   * @param executorService
   *          the executorService to set
   */
  public void setExecutorService(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * @param systemConfiguration
   *          the systemConfiguration to set
   */
  public void setSystemConfiguration(Configuration systemConfiguration) {
    this.systemConfiguration = systemConfiguration;
  }

  /**
   * @param timeProvider
   *          the timeProvider to set
   */
  public void setTimeProvider(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  /**
   * @param loggingProvider
   *          the loggingProvider to set
   */
  public void setLoggingProvider(LoggingProvider loggingProvider) {
    this.loggingProvider = loggingProvider;
  }
}
