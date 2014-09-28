// Copyright 2013 Google Inc. All Rights Reserved.

package interactivespaces.controller.standalone.stubs;

import interactivespaces.activity.Activity;
import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.MinimalLiveActivity;
import interactivespaces.controller.client.node.BaseSpaceController;
import interactivespaces.controller.logging.InteractiveSpacesEnvironmentActivityLogFactory;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SupportedService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.io.Files;
import org.apache.commons.logging.Log;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author peringknife@google.com (Trevor Pering)
 */
public class StandaloneSpaceController extends BaseSpaceController {

  /**
   * File that holds any dynamic extensions to load at runtime.
   */
  private final File serviceExtensionsFile = new File("extensions.txt");

  /**
   * Initialize a controller with the given space environment.
   *
   * @param spaceEnvironment
   *          space environment to use
   * @param nativeActivityRunnerFactory
   *          native app runner factory
   */
  public StandaloneSpaceController(InteractiveSpacesEnvironment spaceEnvironment,
      SimpleNativeActivityRunnerFactory nativeActivityRunnerFactory) {
    super(spaceEnvironment, nativeActivityRunnerFactory);
  }

  @Override
  public void startup() {
    super.startup();
    startupCoreControllerServices();

    loadServiceExtensions();
  }

  /**
   * Load external services specified in the services file.
   */
  private void loadServiceExtensions() {
    if (!serviceExtensionsFile.exists()) {
      getSpaceEnvironment().getLog().warn("No service extensions file: " + serviceExtensionsFile.getAbsolutePath());
      return;
    }

    try {
      List<String> lines = Files.readLines(serviceExtensionsFile, Charset.defaultCharset());
      for (String line : lines) {
        loadServiceByClassName(line);
      }
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error("While reading services file", e);
    }
  }

  /**
   * Load a service by the given class name.
   *
   * @param name
   *          service class name to load
   */
  private void loadServiceByClassName(String name) {
    try {
      getSpaceEnvironment().getLog().info("Loading service " + name);
      Class serviceClass = Class.forName(name);
      Object newInstance = serviceClass.newInstance();
      if (!(newInstance instanceof SupportedService)) {
        getSpaceEnvironment().getLog().error("Loaded server was not of type SupportedService");
        return;
      }
      SupportedService service = (SupportedService) newInstance;
      ServiceRegistry serviceRegistry = getSpaceEnvironment().getServiceRegistry();
      serviceRegistry.registerService(service);
      service.startup();
    } catch (Throwable e) {
      getSpaceEnvironment().getLog().error("Could not start service", e);
    }
  }

  @Override
  public void shutdown() {
    super.shutdown();
    shutdownCoreControllerServices();
  }

  @Override
  protected Log getActivityLog(MinimalLiveActivity activity, Configuration configuration) {
    InteractiveSpacesEnvironmentActivityLogFactory activityLogFactory =
        new InteractiveSpacesEnvironmentActivityLogFactory(getSpaceEnvironment());
    return activityLogFactory.createLogger(activity, configuration.getPropertyString(
        Activity.CONFIGURATION_PROPERTY_LOG_LEVEL, InteractiveSpacesEnvironment.LOG_LEVEL_ERROR));
  }
}
