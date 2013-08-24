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

package interactivespaces.system;

import com.google.common.collect.Maps;

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SimpleServiceRegistry;
import interactivespaces.time.TimeProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An {@link InteractiveSpacesEnvironment} that can be used for active testing
 * of components.
 *
 * @author Keith M. Hughes
 */
public class ActiveTestInteractiveSpacesEnvironment implements InteractiveSpacesEnvironment {

  /**
   * Create a new {@link ActiveTestInteractiveSpacesEnvironment}.
   *
   * @return the space environment
   */
  public static ActiveTestInteractiveSpacesEnvironment newActiveTestInteractiveSpacesEnvironment() {
    ActiveTestInteractiveSpacesEnvironment environment = new ActiveTestInteractiveSpacesEnvironment();

    environment.systemConfiguration = SimpleConfiguration.newConfiguration();
    environment.executorService = Executors.newScheduledThreadPool(100);
    environment.log = new Jdk14Logger("test.interactive.spaces");
    environment.serviceRegistry = new SimpleServiceRegistry(environment);

    return environment;
  }

  /**
   * The system configuration.
   */
  private Configuration systemConfiguration;

  /**
   * The container file system.
   */
  private InteractiveSpacesFilesystem filesystem;

  /**
   * The log for the system.
   */
  private Log log;

  /**
   * The executor service.
   */
  private ScheduledExecutorService executorService;

  /**
   * The service registry.
   */
  private ServiceRegistry serviceRegistry;

  /**
   * The time provider.
   */
  private TimeProvider timeProvider;

  /**
   * Simple value map.
   */
  private Map<String, Object> values = Maps.newHashMap();

  @Override
  public Configuration getSystemConfiguration() {
    return systemConfiguration;
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
  public Log getLog(String logName, String level) {
    // for now just return the system log
    return log;
  }

  @Override
  public boolean modifyLogLevel(Log log, String level) {
    // Not for now
    return false;
  }

  @Override
  public String getNetworkType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TimeProvider getTimeProvider() {
    return timeProvider;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(String valueName) {
    return (T) values.get(valueName);
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
   * Shutdown the environment.
   */
  public void shutdown() {
    executorService.shutdown();
  }
}
