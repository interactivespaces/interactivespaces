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

package interactivespaces.workbench.osgi;

import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.system.SimpleInteractiveSpacesEnvironment;
import interactivespaces.system.core.configuration.ConfigurationProvider;
import interactivespaces.system.core.container.ContainerCustomizerProvider;
import interactivespaces.system.core.logging.LoggingProvider;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.ui.WorkbenchUi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.ros.concurrent.DefaultScheduledExecutorService;

import java.util.List;

/**
 * OSGi activator for the Interactive Spaces Workbench.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbenchActivator implements BundleActivator {

  /**
   * The workbench UI, if any.
   */
  private WorkbenchUi ui;

  /**
   * The context for the workbench's OSGi bundle.
   */
  private BundleContext bundleContext;

  /**
   * The platform logging provider.
   */
  private LoggingProvider loggingProvider;

  /**
   * The platform configuration provider.
   */
  private ConfigurationProvider configurationProvider;

  /**
   * The platform container customizer provider.
   *
   * <p>
   * Can be {@code null} if none is provided.
   */
  private ContainerCustomizerProvider containerCustomizerProvider;

  /**
   * The executor service to use.
   */
  private DefaultScheduledExecutorService executorService;

  /**
   * The space environment for the bundle.
   */
  private SimpleInteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    this.bundleContext = bundleContext;

    getCoreServices();

    BundleListener myBundleListener = new BundleListener() {

      @Override
      public void bundleChanged(BundleEvent event) {
        handleBundleChangedEvent(event);
      }
    };

    bundleContext.addBundleListener(myBundleListener);

    try {
      prepareWorkbenchEnvironment();
    } catch (Exception e) {
      loggingProvider.getLog().error("Could not run workbench", e);
    }
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  /**
   * Get all core services needed.
   *
   * <p>
   * These services should have been provided by the OSGi container bootstrap and so will be immediately available. They
   * will never go away since they are only destroyed when bundle 0 goes, which means the entire container is being shut
   * down.
   *
   * @throws Exception
   *           something bad happened
   */
  private void getCoreServices() throws Exception {
    ServiceReference<LoggingProvider> loggingProviderServiceReference =
        bundleContext.getServiceReference(LoggingProvider.class);
    loggingProvider = bundleContext.getService(loggingProviderServiceReference);

    ServiceReference<ConfigurationProvider> configurationProviderServiceReference =
        bundleContext.getServiceReference(ConfigurationProvider.class);
    configurationProvider = bundleContext.getService(configurationProviderServiceReference);

    ServiceReference<ContainerCustomizerProvider> containerCustomizerProviderServiceReference =
        bundleContext.getServiceReference(ContainerCustomizerProvider.class);
    containerCustomizerProvider = bundleContext.getService(containerCustomizerProviderServiceReference);
  }

  /**
   * Prepare the workbench environment.
   */
  public void prepareWorkbenchEnvironment() {

    spaceEnvironment = new SimpleInteractiveSpacesEnvironment();
    spaceEnvironment.setLog(loggingProvider.getLog());
    executorService = new DefaultScheduledExecutorService();
    spaceEnvironment.setExecutorService(executorService);
    SimpleConfiguration configuration = SimpleConfiguration.newConfiguration();
    configuration.setValues(configurationProvider.getInitialConfiguration());

    spaceEnvironment.setSystemConfiguration(configuration);
  }

  /**
   * Handle a bundle change event.
   *
   * @param event
   *          the bundle change event
   */
  private void handleBundleChangedEvent(BundleEvent event) {
    // Make sure the bundle is us.
    if (event.getBundle().equals(bundleContext.getBundle())) {
      if (event.getType() == BundleEvent.STARTED) {
        spaceEnvironment.getExecutorService().execute(new Runnable() {
          @Override
          public void run() {
            runWorkbench();
          }
        });
      }
    }
  }

  /**
   * The bundle is now officially started and the OSGi framework is happy with it.
   */
  protected void runWorkbench() {
    Bundle systemBundle = bundleContext.getBundle(0);
    ClassLoader systemClassLoader = systemBundle.getClass().getClassLoader();

    InteractiveSpacesWorkbench workbench = new InteractiveSpacesWorkbench(spaceEnvironment, systemClassLoader);

    List<String> commandLineArguments = containerCustomizerProvider.getCommandLineArguments();
    if (commandLineArguments.size() == 1
        && InteractiveSpacesWorkbench.COMMAND_LINE_FLAG_GUI.equals(commandLineArguments.get(0))) {
      ui = new WorkbenchUi(workbench);
    } else if (!commandLineArguments.isEmpty()) {
      boolean success = false;
      try {
        success = workbench.doCommands(commandLineArguments);
      } finally {
        shutdownWorkbench(success);
      }
    } else {
      shutdownWorkbench(true);
    }
  }

  /**
   * Shut the workbench down.
   *
   * @param success
   *          {@code true} if should exit with a success status
   */
  private void shutdownWorkbench(boolean success) {
    try {
      // Try and shut down politely.
      bundleContext.getBundle(0).stop();
    } catch (BundleException e) {
      loggingProvider.getLog().error("Error stopping container", e);
    }

    // Make sure exit with a value saying whether we failed or not.
    System.exit(success ? 0 : -1);
  }
}
