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

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.logging.ExtendedLog;
import interactivespaces.logging.StandardExtendedLog;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SimpleServiceRegistry;
import interactivespaces.time.SettableTimeProvider;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.resource.ManagedResource;
import interactivespaces.util.resource.ManagedResources;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An {@link InteractiveSpacesEnvironment} that can be used for standalone running of components.
 *
 * @author Keith M. Hughes
 */
public final class StandaloneInteractiveSpacesEnvironment implements InteractiveSpacesEnvironment {

  /**
   * The number of threads to initialize in the scheduled thread pool.
   */
  private static final int NUM_THREADS_IN_POOL = 100;

  /**
   * Create a new {@link StandaloneInteractiveSpacesEnvironment}.
   *
   * @return the space environment
   */
  public static StandaloneInteractiveSpacesEnvironment newStandaloneInteractiveSpacesEnvironment() {
    StandaloneInteractiveSpacesEnvironment environment = new StandaloneInteractiveSpacesEnvironment();

    environment.systemConfiguration = SimpleConfiguration.newConfiguration();
    environment.executorService = Executors.newScheduledThreadPool(NUM_THREADS_IN_POOL);
    environment.log = new StandardExtendedLog(new Jdk14Logger("test.interactive.spaces"));
    environment.serviceRegistry = new SimpleServiceRegistry(environment);
    environment.timeProvider = new SettableTimeProvider();
    environment.managedResources = new ManagedResources(environment.log);
    environment.managedResources.startupResources();

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
  private ExtendedLog log;

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
  private final Map<String, Object> values = Maps.newHashMap();

  /**
   * The managed resources for this environment.
   */
  private ManagedResources managedResources;

  /**
   * Construct a new environment.
   */
  private StandaloneInteractiveSpacesEnvironment() {
  }

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
  public ExtendedLog getExtendedLog() {
    return log;
  }

  @Override
  public Log getLog(String logName, String level, String filename) {
    // for now just return the system log
    return log;
  }

  @Override
  public boolean modifyLogLevel(Log log, String level) {
    // Not for now
    return false;
  }

  @Override
  public void releaseLog(Log log) {
    // Nothing to do
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
    managedResources.shutdownResourcesAndClear();
    executorService.shutdown();
  }

  /**
   * Set the file system to use.
   *
   * @param filesystem
   *          the file system to use
   */
  public void setFilesystem(InteractiveSpacesFilesystem filesystem) {
    this.filesystem = filesystem;
  }

  /**
   * Set the logger to use.
   *
   * @param log
   *          the logger to use
   */
  public void setLog(Log log) {
    this.log = new StandardExtendedLog(log);
  }

  /**
   * Set the time provider to use if the default isn't appropriate.
   *
   * @param timeProvider
   *          the new time provider
   */
  public void setTimeProvider(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  /**
   * Add a managed resource.
   *
   * @param resource
   *          the resource to add
   */
  public void addManagedResource(ManagedResource resource) {
    managedResources.addResource(resource);
  }
}
