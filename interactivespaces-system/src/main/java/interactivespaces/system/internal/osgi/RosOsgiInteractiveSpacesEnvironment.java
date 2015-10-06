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

import interactivespaces.configuration.Configuration;
import interactivespaces.logging.ExtendedLog;
import interactivespaces.logging.StandardExtendedLog;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SimpleServiceRegistry;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.InternalInteractiveSpacesEnvironment;
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.time.TimeProvider;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A {@link InteractiveSpacesEnvironment} which lives in a ROS container.
 *
 * @author Keith M. Hughes
 */
public class RosOsgiInteractiveSpacesEnvironment implements InteractiveSpacesEnvironment,
    InternalInteractiveSpacesEnvironment {

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
   * This allows distinguishing between different Interactive Spaces networks, e.g. localdev, prod, fredbot.
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

  /**
   * The extended logger.
   */
  private ExtendedLog log;

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
    return log;
  }

  @Override
  public ExtendedLog getExtendedLog() {
    return log;
  }

  @Override
  public Log getLog(String logName, String level, String filename) {
    // TODO(keith): make this generate extended logs, though they will need to be in a map.
    return loggingProvider.getLog(logName, level, filename);
  }

  @Override
  public boolean modifyLogLevel(Log log, String level) {
    return loggingProvider.modifyLogLevel(log, level);
  }

  @Override
  public void releaseLog(Log log) {
    loggingProvider.releaseLog(log);
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

  @Override
  public void setFilesystem(InteractiveSpacesFilesystem filesystem) {
    this.filesystem = filesystem;
  }

  @Override
  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  @Override
  public void setExecutorService(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public void setSystemConfiguration(Configuration systemConfiguration) {
    this.systemConfiguration = systemConfiguration;
  }

  @Override
  public void changeSystemConfigurationTop(Configuration newTopSystemConfiguration) {
    newTopSystemConfiguration.setParent(systemConfiguration);
    systemConfiguration = newTopSystemConfiguration;
  }

  @Override
  public void setTimeProvider(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  @Override
  public void setLoggingProvider(LoggingProvider loggingProvider) {
    this.loggingProvider = loggingProvider;

    log = new StandardExtendedLog(loggingProvider.getLog());
  }
}
