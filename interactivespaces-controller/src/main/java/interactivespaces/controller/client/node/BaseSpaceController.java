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

package interactivespaces.controller.client.node;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.component.CoreExistingActivityComponentFactory;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.MinimalLiveActivity;
import interactivespaces.controller.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.web.client.WebSocketClientService;
import interactivespaces.service.web.client.internal.netty.NettyWebSocketClientService;
import interactivespaces.service.web.server.WebServerService;
import interactivespaces.service.web.server.internal.netty.NettyWebServerService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import org.apache.commons.logging.Log;

/**
 * Base implementation for a space controller.
 *
 * @author Trevor Pering
 */
public abstract class BaseSpaceController implements SpaceController {

  /**
   * Information about the controller
   */
  private final SimpleSpaceController controllerInfo = new SimpleSpaceController();

  /**
   * The Interactive Spaces environment being run under.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * A factory for native app runners.
   */
  private final NativeActivityRunnerFactory nativeActivityRunnerFactory;

  /**
   * The component factory to be used by this controller.
   */
  private ActivityComponentFactory activityComponentFactory;

  /**
   * The IS service for web servers.
   */
  private WebServerService webServerService;

  /**
   * The IS service for web socket clients.
   */
  private WebSocketClientService webSocketClientService;

  /**
   * Initialize a controller with the given space environment.
   *
   * @param spaceEnvironment
   *          space environment to use
   * @param nativeAppRunnerFactory
   *          native app runner factory
   */
  public BaseSpaceController(InteractiveSpacesEnvironment spaceEnvironment,
      NativeActivityRunnerFactory nativeAppRunnerFactory) {
    this.spaceEnvironment = spaceEnvironment;
    this.nativeActivityRunnerFactory = nativeAppRunnerFactory;
  }

  @Override
  public void startup() {
    getSpaceEnvironment().getLog().info("Controller starting up");
    obtainControllerInfo();
    activityComponentFactory = new CoreExistingActivityComponentFactory();
    setEnvironmentValues();
  }

  @Override
  public void shutdown() {
    getSpaceEnvironment().getLog().info("Controller shutting down");
  }

  @Override
  public void initializeActivityInstance(MinimalLiveActivity activity,
      ActivityFilesystem activityFilesystem, Activity instance, Configuration configuration,
      ActivityExecutionContext executionContext) {

    // Set log first to enable logging of any configuration/startup errors.
    instance.setLog(getActivityLog(activity, configuration));

    String uuid = activity.getUuid();
    instance.setController(this);
    instance.setUuid(uuid);

    instance.setConfiguration(configuration);
    instance.setActivityFilesystem(activityFilesystem);
    instance.setSpaceEnvironment(spaceEnvironment);
    instance.setExecutionContext(executionContext);

    initializeActivityConfiguration(configuration, activityFilesystem);

    onActivityInitialization(instance);
  }

  /**
   * Initialize the configuration with any special values needed for running.
   *
   * @param configuration
   *          the configuration to be modified
   * @param activityFilesystem
   *          the activities file system
   */
  private void initializeActivityConfiguration(Configuration configuration,
      ActivityFilesystem activityFilesystem) {
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_INSTALL,
        activityFilesystem.getInstallDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_LOG,
        activityFilesystem.getLogDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_DATA,
        activityFilesystem.getPermanentDataDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_TMP,
        activityFilesystem.getTempDataDirectory().getAbsolutePath());

    // TODO(keith): Move to interactivespaces-system during bootstrap
    InteractiveSpacesFilesystem filesystem = spaceEnvironment.getFilesystem();
    configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_SYSTEM_FILESYSTEM_DIR_DATA,
        filesystem.getDataDirectory().getAbsolutePath());
    configuration.setValue(InteractiveSpacesEnvironment.CONFIGURATION_SYSTEM_FILESYSTEM_DIR_TMP,
        filesystem.getTempDirectory().getAbsolutePath());
  }

  /**
   * Perform any additional instance initialization needed.
   *
   * @param instance
   *          The activity instance to initialize
   */
  protected void onActivityInitialization(Activity instance) {
    // Default is nothing.
  }

  @Override
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  /**
   * Get the logger for the indicated activity.
   *
   * @param activity
   *          activity to log
   * @param configuration
   *          configuration properties
   *
   * @return logger for this activity & configuration
   */
  protected abstract Log getActivityLog(MinimalLiveActivity activity, Configuration configuration);

  /**
   * Get controller information from the configs.
   */
  private void obtainControllerInfo() {
    Configuration systemConfiguration = getSpaceEnvironment().getSystemConfiguration();

    controllerInfo.setUuid(systemConfiguration.getPropertyString(CONFIGURATION_CONTROLLER_UUID));
    controllerInfo
        .setName(systemConfiguration.getPropertyString(CONFIGURATION_CONTROLLER_NAME, ""));
    controllerInfo.setDescription(systemConfiguration.getPropertyString(
        CONFIGURATION_CONTROLLER_DESCRIPTION, ""));
    controllerInfo.setHostId(systemConfiguration
        .getRequiredPropertyString(InteractiveSpacesEnvironment.CONFIGURATION_HOSTID));
  }

  @Override
  public SimpleSpaceController getControllerInfo() {
    return controllerInfo;
  }

  @Override
  public ActivityComponentFactory getActivityComponentFactory() {
    return activityComponentFactory;
  }

  @Override
  public NativeActivityRunnerFactory getNativeActivityRunnerFactory() {
    return nativeActivityRunnerFactory;
  }

  /**
   * Start up the core services that all controllers provide.
   */
  protected void startupCoreControllerServices() {
    ServiceRegistry serviceRegistry = getSpaceEnvironment().getServiceRegistry();

    webServerService = new NettyWebServerService();
    serviceRegistry.registerService(webServerService);
    webServerService.startup();

    webSocketClientService = new NettyWebSocketClientService();
    serviceRegistry.registerService(webSocketClientService);
    webSocketClientService.startup();
  }

  /**
   * Set values in the space environment that the controller provides.
   */
  private void setEnvironmentValues() {
    getSpaceEnvironment().setValue(ENVIRONMENT_CONTROLLER_NATIVE_RUNNER,
        getNativeActivityRunnerFactory());
  }

  /**
   * Shutdown the core services provided by all controllers.
   */
  protected void shutdownCoreControllerServices() {
    ServiceRegistry serviceRegistry = getSpaceEnvironment().getServiceRegistry();

    serviceRegistry.unregisterService(webServerService);
    webServerService.shutdown();

    serviceRegistry.unregisterService(webSocketClientService);
    webSocketClientService.shutdown();
  }
}
