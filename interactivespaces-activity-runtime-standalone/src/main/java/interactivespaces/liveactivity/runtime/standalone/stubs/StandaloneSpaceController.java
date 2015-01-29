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


package interactivespaces.liveactivity.runtime.standalone.stubs;

import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.controller.runtime.BaseSpaceController;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SupportedService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * A space controller that is standalone.
 *
 * @author Trevor Pering
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
    super(spaceEnvironment);
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
}
